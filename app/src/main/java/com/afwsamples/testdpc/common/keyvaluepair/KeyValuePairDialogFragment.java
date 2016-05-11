/*
 * Copyright (C) 2016 The Android Open Source Project
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *      http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */
package com.afwsamples.testdpc.common.keyvaluepair;

import android.app.Activity;
import android.app.AlertDialog;
import android.app.Dialog;
import android.app.DialogFragment;
import android.app.Fragment;
import android.content.DialogInterface;
import android.content.Intent;
import android.graphics.Color;
import android.os.Bundle;
import android.os.Parcelable;
import android.support.annotation.StringRes;
import android.support.v7.widget.LinearLayoutManager;
import android.support.v7.widget.RecyclerView;
import android.text.TextUtils;
import android.view.LayoutInflater;
import android.view.View;
import android.view.View.OnClickListener;
import android.view.ViewGroup;
import android.widget.AdapterView;
import android.widget.ArrayAdapter;
import android.widget.Button;
import android.widget.EditText;
import android.widget.Spinner;
import android.widget.Switch;
import android.widget.TextView;
import android.widget.Toast;

import com.afwsamples.testdpc.R;
import com.afwsamples.testdpc.common.StringArrayTypeInputAdapter;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.HashSet;
import java.util.Set;

/**
 * A dialog asking user to input key value pair.
 * See {@link DialogType} for supported types.
 * Result will be returned through onActivityResult.
 */
public class KeyValuePairDialogFragment extends DialogFragment {
    static final String ARG_DIALOG_TYPE = "dialog_type";
    static final String ARG_CAN_EDIT_KEY= "can_edit_key";
    static final String ARG_KEY= "key";
    static final String ARG_INITIAL_VALUE = "initial_value";
    static final String ARG_SUPPORTED_TYPE = "supported_type";
    static final String ARG_APP_NAME = "app_name";

    public static final String RESULT_KEY = "key";
    public static final String RESULT_VALUE = "value";
    public static final String RESULT_TYPE = "type";

    private EditText mKeyView;
    private Switch mBoolView;
    private EditText mIntView;
    private EditText mStringView;
    private View mStringArrayContainer;
    private RecyclerView mStringArrayList;
    private Button mConfigureBundle;
    private Button mConfigureBundleArray;
    private View[] mValueViews;
    private Spinner mTypeSpinner;

    private StringArrayTypeInputAdapter mStringArrayAdapter;
    private Set<Integer> mSupportedTypes;
    private int mDialogType;
    private String mAppName;

    public interface DialogType {
        int BOOL_TYPE = 0;
        int INT_TYPE = 1;
        int STRING_TYPE = 2;
        int STRING_ARRAY_TYPE = 3;
        int BUNDLE_TYPE = 4;
        int BUNDLE_ARRAY_TYPE = 5;
    }

    private static final String[] TYPE_DISPLAY_STRING_ARRAY = new String[] {
            "Boolean",
            "Integer",
            "String",
            "String[]",
            "Bundle",
            "Bundle[]"
    };

    private final BundleButtonOnClickListener bundleButtonOnClickListener =
            new BundleButtonOnClickListener();
    private final BundleArrayButtonOnClickListener bundleArrayButtonOnClickListener =
            new BundleArrayButtonOnClickListener();

    public static KeyValuePairDialogFragment newInstance(int dialogType, boolean canEditKey,
            String key, Object value, int[] supportedType, String appName) {
        KeyValuePairDialogFragment fragment = new KeyValuePairDialogFragment();
        Bundle argument = new Bundle();
        argument.putInt(ARG_DIALOG_TYPE, dialogType);
        argument.putBoolean(ARG_CAN_EDIT_KEY, canEditKey);
        argument.putString(ARG_KEY, key);
        if (value != null) {
            switch (dialogType) {
                case DialogType.BOOL_TYPE:
                    argument.putBoolean(ARG_INITIAL_VALUE, (boolean) value);
                    break;
                case DialogType.INT_TYPE:
                    argument.putInt(ARG_INITIAL_VALUE, (int) value);
                    break;
                case DialogType.STRING_TYPE:
                    argument.putString(ARG_INITIAL_VALUE, (String) value);
                    break;
                case DialogType.STRING_ARRAY_TYPE:
                    argument.putStringArray(ARG_INITIAL_VALUE, (String[]) value);
                    break;
                case DialogType.BUNDLE_TYPE:
                    argument.putBundle(ARG_INITIAL_VALUE, (Bundle) value);
                    break;
                case DialogType.BUNDLE_ARRAY_TYPE:
                    argument.putParcelableArray(ARG_INITIAL_VALUE, (Bundle[]) value);
                    break;
            }
        }
        argument.putIntArray(ARG_SUPPORTED_TYPE, supportedType);
        argument.putString(ARG_APP_NAME, appName);
        fragment.setArguments(argument);
        return fragment;
    }

    @Override
    public Dialog onCreateDialog(Bundle savedInstanceState) {
        LayoutInflater inflater = LayoutInflater.from(getActivity());
        View rootView = inflater.inflate(R.layout.basic_key_value_pair, null);
        mKeyView = (EditText) rootView.findViewById(R.id.key);
        mBoolView = (Switch) rootView.findViewById(R.id.value_bool);
        mIntView = (EditText) rootView.findViewById(R.id.value_int);
        mStringView = (EditText) rootView.findViewById(R.id.value_string);
        mStringArrayContainer = rootView.findViewById(R.id.value_str_array_container);
        mStringArrayList = (RecyclerView) rootView.findViewById(R.id.value_str_array_list);
        mConfigureBundle = (Button) rootView.findViewById(R.id.configure_bundle);
        mConfigureBundleArray = (Button) rootView.findViewById(R.id.configure_bundle_array);
        // The order is based on TYPE_DISPLAY_STRING_ARRAY.
        mValueViews = new View[] { mBoolView, mIntView, mStringView, mStringArrayContainer,
                mConfigureBundle, mConfigureBundleArray };
        mTypeSpinner = (Spinner) rootView.findViewById(R.id.type_spinner);
        mConfigureBundle.setOnClickListener(bundleButtonOnClickListener);
        mConfigureBundleArray.setOnClickListener(bundleArrayButtonOnClickListener);

        configureStringUi();
        configureTypeSpinner();
        configureUsingArguments();
        return createAlertDialog(rootView);
    }

    private AlertDialog createAlertDialog(View view) {
        final AlertDialog dialog = new AlertDialog.Builder(getActivity())
                .setView(view)
                .setCancelable(true)
                .setPositiveButton(R.string.restriction_save_label, null)
                .setNegativeButton(R.string.restriction_cancel_label, null)
                .create();
        dialog.setOnShowListener(new DialogInterface.OnShowListener() {
            @Override
            public void onShow(DialogInterface dialogInterface) {
                dialog.getButton(AlertDialog.BUTTON_POSITIVE).setOnClickListener(
                        new OnClickListener() {
                            @Override
                            public void onClick(View view) {
                                String key = mKeyView.getText().toString();
                                if (TextUtils.isEmpty(key)) {
                                    showToast(R.string.key_empty_error);
                                    return;
                                }
                                Intent result = new Intent();
                                result.putExtra(RESULT_TYPE, mDialogType);
                                result.putExtra(RESULT_KEY, key);
                                boolean valid = putValueFromUiToResultIntent(result);
                                if (valid) {
                                    getTargetFragment().onActivityResult(getTargetRequestCode(),
                                            Activity.RESULT_OK, result);
                                    dialog.dismiss();
                                } else {
                                    showToast(R.string.value_not_valid);
                                }
                            }
                        });
            }
        });
        return dialog;
    }

    private boolean putValueFromUiToResultIntent(Intent result) {
        switch (mDialogType) {
            case DialogType.BOOL_TYPE:
                result.putExtra(RESULT_VALUE, mBoolView.isChecked());
                return true;
            case DialogType.INT_TYPE:
                try {
                    result.putExtra(RESULT_VALUE, Integer.valueOf(mIntView.getText().toString()));
                } catch (NumberFormatException ex) {
                    return false;
                }
                return true;
            case DialogType.STRING_TYPE:
                result.putExtra(RESULT_VALUE, mStringView.getText().toString());
                return true;
            case DialogType.STRING_ARRAY_TYPE:
                result.putExtra(RESULT_VALUE, mStringArrayAdapter.getStringList().toArray(new
                        String[0]));
                return true;
            case DialogType.BUNDLE_TYPE:
                Bundle initialBundle = getArguments().getBundle(ARG_INITIAL_VALUE);
                if (initialBundle == null) {
                    initialBundle = new Bundle();
                }
                result.putExtra(RESULT_VALUE, initialBundle);
                return true;
            case DialogType.BUNDLE_ARRAY_TYPE:
                Parcelable[] initialBundleArray =
                        getArguments().getParcelableArray(ARG_INITIAL_VALUE);
                if (initialBundleArray == null) {
                    initialBundleArray = new Bundle[0];
                }
                result.putExtra(RESULT_VALUE, initialBundleArray);
                return true;
        }
        return false;
    }

    private void configureTypeSpinner() {
        ArrayAdapter<String> typeArrayAdapter =
                new ArrayAdapter<String>(getActivity(),
                        android.R.layout.simple_spinner_item, TYPE_DISPLAY_STRING_ARRAY) {
                    @Override
                    public boolean isEnabled(int position) {
                        return mSupportedTypes.contains(position);
                    }

                    @Override
                    public View getDropDownView(int position, View convertView, ViewGroup parent) {
                        TextView textView = (TextView) super.getDropDownView(position, convertView,
                                parent);
                        if (isEnabled(position)) {
                            textView.setTextColor(Color.BLACK);
                        } else {
                            textView.setTextColor(Color.LTGRAY);
                        }
                        return textView;
                    }
                };
        typeArrayAdapter.setDropDownViewResource(android.R.layout.simple_spinner_dropdown_item);
        mTypeSpinner.setAdapter(typeArrayAdapter);
        mTypeSpinner.setOnItemSelectedListener(new AdapterView.OnItemSelectedListener() {
            @Override
            public void onItemSelected(AdapterView<?> adapterView, View view, int pos, long l) {
                setDialogType(pos);
            }

            @Override
            public void onNothingSelected(AdapterView<?> adapterView) {
            }
        });
    }

    private void configureUsingArguments() {
        Bundle arguments = getArguments();
        configureKeyView(arguments.getBoolean(ARG_CAN_EDIT_KEY), arguments.getString(ARG_KEY));
        setSupportedType(arguments.getIntArray(ARG_SUPPORTED_TYPE));
        setDialogType(arguments.getInt(ARG_DIALOG_TYPE));
        populateInitialValue(arguments.get(ARG_INITIAL_VALUE));
        mAppName = arguments.getString(ARG_APP_NAME);
    }

    private void configureKeyView(boolean canEditKeyView, String key) {
        mKeyView.setEnabled(canEditKeyView);
        mKeyView.setText(key);
    }

    private void setSupportedType(int[] supportedTypes) {
        Set<Integer> supportedTypeSet = new HashSet<>();
        for (Integer supportType : supportedTypes) {
            supportedTypeSet.add(supportType);
        }
        mSupportedTypes = supportedTypeSet;
    }

    private void configureStringUi() {
        LinearLayoutManager linearLayoutManager = new LinearLayoutManager(getActivity());
        linearLayoutManager.setOrientation(LinearLayoutManager.VERTICAL);
        mStringArrayList.setLayoutManager(linearLayoutManager);
        mStringArrayAdapter = new StringArrayTypeInputAdapter();
        mStringArrayList.setAdapter(mStringArrayAdapter);
        mStringArrayContainer.findViewById(R.id.add_new_row).setOnClickListener(
                new OnClickListener() {
                    @Override
                    public void onClick(View view) {
                        mStringArrayAdapter.getStringList().add("");
                        mStringArrayAdapter.notifyItemInserted(mStringArrayAdapter.getItemCount());
                    }
                });
    }

    private void setDialogType(int type) {
        mDialogType = type;
        mTypeSpinner.setSelection(type);
        for (int i = 0; i < mValueViews.length; i++) {
            if (mDialogType == i) {
                mValueViews[i].setVisibility(View.VISIBLE);
            } else {
                mValueViews[i].setVisibility(View.GONE);
            }
        }
    }

    private void populateInitialValue(final Object initialValue) {
        if (initialValue == null) {
            return;
        }
        switch (mDialogType) {
            case DialogType.BOOL_TYPE:
                mBoolView.setChecked((boolean) initialValue);
                break;
            case DialogType.INT_TYPE:
                mIntView.setText(String.valueOf((int) initialValue));
                break;
            case DialogType.STRING_TYPE:
                mStringView.setText((String) initialValue);
                break;
            case DialogType.STRING_ARRAY_TYPE:
                mStringArrayAdapter.setStringList(new ArrayList<>(Arrays.asList(
                        (String[]) initialValue)));
                break;
            case DialogType.BUNDLE_TYPE:
                bundleButtonOnClickListener.setBundle((Bundle) initialValue);
                break;
            case DialogType.BUNDLE_ARRAY_TYPE:
                bundleArrayButtonOnClickListener.setBundleArray((Bundle[]) initialValue);
                break;
        }
    }

    private void showFragmentForFurtherInput(Fragment fragment) {
        getFragmentManager()
                .beginTransaction()
                .addToBackStack(KeyValuePairDialogFragment.class.getName())
                .hide(getTargetFragment())
                .add(R.id.container, fragment)
                .commit();
        fragment.setTargetFragment(getTargetFragment(), getTargetRequestCode());
        KeyValuePairDialogFragment.this.dismiss();
    }

    private void showToast(@StringRes int stringResId) {
        Toast.makeText(getActivity(), stringResId, Toast.LENGTH_LONG).show();
    }

    private class BundleButtonOnClickListener implements OnClickListener {
        private Bundle mBundle;

        public void setBundle(Bundle bundle) {
            mBundle = bundle;
        }
        @Override
        public void onClick(View view) {
            String key = mKeyView.getText().toString();
            if (TextUtils.isEmpty(key)) {
                showToast(R.string.key_empty_error);
                return;
            }
            Fragment fragment = KeyValueBundleFragment.newInstance(key, mBundle, mAppName);
            showFragmentForFurtherInput(fragment);
        }
    }

    private class BundleArrayButtonOnClickListener implements OnClickListener {
        private Bundle[] mBundles;

        public void setBundleArray(Bundle[] bundles) {
            mBundles = bundles;
        }
        @Override
        public void onClick(View view) {
            String key = mKeyView.getText().toString();
            if (TextUtils.isEmpty(key)) {
                showToast(R.string.key_empty_error);
                return;
            }
            Fragment fragment = KeyValueBundleArrayFragment.newInstance(key,
                    mBundles, mAppName);
            showFragmentForFurtherInput(fragment);
        }
    }
}
