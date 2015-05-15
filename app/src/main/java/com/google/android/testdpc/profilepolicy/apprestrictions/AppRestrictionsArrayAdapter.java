/*
 * Copyright 2015 Google Inc. All Rights Reserved.
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

package com.google.android.testdpc.profilepolicy.apprestrictions;

import android.app.AlertDialog;
import android.app.admin.DevicePolicyManager;
import android.content.Context;
import android.content.DialogInterface;
import android.content.RestrictionEntry;
import android.os.Bundle;
import android.text.InputType;
import android.text.TextUtils;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.view.ViewParent;
import android.view.WindowManager;
import android.view.inputmethod.EditorInfo;
import android.widget.AdapterView;
import android.widget.ArrayAdapter;
import android.widget.Button;
import android.widget.EditText;
import android.widget.ImageView;
import android.widget.ListView;
import android.widget.Spinner;
import android.widget.Switch;
import android.widget.Toast;

import com.google.android.testdpc.DeviceAdminReceiver;
import com.google.android.testdpc.R;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;

/**
 * Renders a list of app restrictions with key, value and value-type input fields.
 */
public class AppRestrictionsArrayAdapter extends ArrayAdapter<RestrictionEntry> {

    public static final String TAG = "AppRestrictionsArrayAdapter";

    private static final String[] APP_RESTRICTIONS_TYPES = {"bool", "int", "String", "String[]"};

    private static final int BOOL_TYPE_INDEX = 0;

    private static final int INT_TYPE_INDEX = 1;

    private static final int STRING_TYPE_INDEX = 2;

    private static final int STRING_ARRAY_INDEX = 3;

    private ArrayAdapter<String> mAppRestrictionsTypeArrayAdapter = null;

    private List<RestrictionEntry> mRestrictionEntries = null;

    private List<Boolean> mIsRowInSaveStateList = null;

    // Update the UI upon the change of value-type.
    // int / String: EditText
    // boolean: Switch
    // String[]: Button which text is the RestrictionEntry value. Clicking the button will show a
    // dialog for viewing and editing the String[].
    private AdapterView.OnItemSelectedListener mTypeSpinnerOnItemSelectedListener
            = new AdapterView.OnItemSelectedListener() {
        @Override
        public void onItemSelected(AdapterView<?> parent, View view, final int position, long id) {
            // View hierarchy from root: RelativeLayout -> Spinner -> TextView.
            // view here is a TextView.
            ViewParent parentView = view.getParent().getParent();
            if (parentView instanceof View && ((View) parentView).getTag() != null) {
                AppRestrictionsViewHolder viewHolder
                        = (AppRestrictionsViewHolder) ((View) parentView).getTag();
                switch (position) {
                    case INT_TYPE_INDEX:
                        // Use EditText for int input.
                        viewHolder.strValInput.setInputType(InputType.TYPE_CLASS_NUMBER
                                        | InputType.TYPE_NUMBER_FLAG_SIGNED);
                        if (viewHolder.restrictionEntry.getType()
                                != RestrictionEntry.TYPE_INTEGER) {
                            viewHolder.strValInput.setText("0");
                        }
                        viewHolder.updateViewsVisibility(INT_TYPE_INDEX);
                        break;
                    case BOOL_TYPE_INDEX:
                        if (viewHolder.restrictionEntry.getType()
                                != RestrictionEntry.TYPE_BOOLEAN) {
                            viewHolder.boolValInput.setChecked(false);
                        }
                        viewHolder.updateViewsVisibility(BOOL_TYPE_INDEX);
                        break;
                    case STRING_TYPE_INDEX:
                        viewHolder.strValInput.setInputType(
                                EditorInfo.TYPE_TEXT_FLAG_NO_SUGGESTIONS);
                        if (!(viewHolder.restrictionEntry.getType() == RestrictionEntry.TYPE_STRING
                                || viewHolder.restrictionEntry.getType()
                                == RestrictionEntry.TYPE_CHOICE)) {
                            viewHolder.strValInput.setText("");
                        }
                        viewHolder.updateViewsVisibility(STRING_TYPE_INDEX);
                        break;
                    case STRING_ARRAY_INDEX:
                        viewHolder.strArrValInput.setOnClickListener(
                                mStringArrayButtonOnClickListener);
                        if (viewHolder.restrictionEntry.getAllSelectedStrings() == null) {
                            viewHolder.strArrValInput.setText("[]");
                            viewHolder.strArrValInput.setTag(new String[]{});
                        }
                        viewHolder.updateViewsVisibility(STRING_ARRAY_INDEX);
                        break;
                }
            } else {
                Log.d(TAG, "Fail to find ViewHolder from the type spinner.");
            }
        }

        @Override
        public void onNothingSelected(AdapterView<?> parent) {
            // Nothing to do.
        }
    };

    // Shows a prompt for viewing and editing String[].
    private View.OnClickListener mStringArrayButtonOnClickListener = new View.OnClickListener() {
        @Override
        public void onClick(View view) {
            ViewParent parentView = view.getParent();
            if (parentView instanceof View && ((View) parentView).getTag() != null) {
                final AppRestrictionsViewHolder viewHolder
                        = (AppRestrictionsViewHolder) ((View) parentView).getTag();
                if (!TextUtils.isEmpty(viewHolder.keyInput.getText().toString())) {
                    AlertDialog.Builder builder = new AlertDialog.Builder(getContext());
                    String[] selectedStrings = viewHolder.restrictionEntry.getAllSelectedStrings();
                    ArrayList<String> arrayList;
                    if (selectedStrings != null) {
                        arrayList = new ArrayList<String>(Arrays.asList(selectedStrings));
                    } else {
                        arrayList = new ArrayList<String>(Arrays.asList(new String[]{""}));
                    }
                    final StringArrayInputArrayAdapter stringArrayInputArrayAdapter
                            = new StringArrayInputArrayAdapter(getContext(), -1, arrayList);
                    ListView listView = new ListView(getContext());
                    listView.setAdapter(stringArrayInputArrayAdapter);
                    listView.setDescendantFocusability(ViewGroup.FOCUS_AFTER_DESCENDANTS);
                    builder.setView(listView);
                    builder.setTitle(getContext().getString(R.string.set_str_array_dialog_title,
                            viewHolder.keyInput.getText().toString()));
                    builder.setPositiveButton(android.R.string.ok,
                            new DialogInterface.OnClickListener() {
                                @Override
                                public void onClick(DialogInterface dialog, int which) {
                                    String key = viewHolder.keyInput.getText().toString();
                                    int size = stringArrayInputArrayAdapter.getCount();
                                    if (size > 0 && !TextUtils.isEmpty(key)) {
                                        stringArrayInputArrayAdapter.onSave();
                                        String[] strArr = new String[size];
                                        for (int i = 0; i < size; i++) {
                                            strArr[i] = stringArrayInputArrayAdapter.getItem(i);
                                        }
                                        RestrictionEntry newRestrictionEntry
                                                = new RestrictionEntry(key, strArr);
                                        mRestrictionEntries.set(viewHolder.position,
                                                newRestrictionEntry);
                                        AppRestrictionsArrayAdapter.this.notifyDataSetChanged();
                                    }
                                }
                            });
                    AlertDialog dialog = builder.show();
                    dialog.getWindow().clearFlags(WindowManager.LayoutParams.FLAG_NOT_FOCUSABLE
                            | WindowManager.LayoutParams.FLAG_ALT_FOCUSABLE_IM);
                } else {
                    Toast.makeText(getContext(), R.string.empty_key_error, Toast.LENGTH_SHORT).show();
                }
            } else {
                Log.d(TAG, "Fail to find ViewHolder from the string array button.");
            }
        }
    };

    private View.OnClickListener mEditOrSaveOnClickListener = new View.OnClickListener() {
        @Override
        public void onClick(View v) {
            ViewParent parentView = v.getParent();
            if (parentView instanceof View && ((View) parentView).getTag() != null) {
                AppRestrictionsViewHolder viewHolder
                        = (AppRestrictionsViewHolder) ((View) parentView).getTag();
                saveIfNecessary(viewHolder);
                boolean isInSaveState = !mIsRowInSaveStateList.get(viewHolder.position);
                mIsRowInSaveStateList.set(viewHolder.position, isInSaveState);
                toggleAppRestrictionRowEditableUi(viewHolder.keyInput, viewHolder.strValInput,
                        viewHolder.boolValInput, viewHolder.strArrValInput, viewHolder.type,
                        (ImageView) v, isInSaveState);
            } else {
                Log.d(TAG, "Fail to find ViewHolder from EditOrSave button.");
            }
        }

        private void saveIfNecessary(AppRestrictionsViewHolder viewHolder) {
            if (mIsRowInSaveStateList.get(viewHolder.position)) {
                RestrictionEntry newRestrictionEntry = null;
                String key = viewHolder.keyInput.getText().toString();
                String value = viewHolder.strValInput.getText().toString();
                switch (viewHolder.type.getSelectedItemPosition()) {
                    case BOOL_TYPE_INDEX:
                        newRestrictionEntry = new RestrictionEntry(key,
                                viewHolder.boolValInput.isChecked());
                        break;
                    case INT_TYPE_INDEX:
                        if (TextUtils.isDigitsOnly(value)) {
                            newRestrictionEntry = new RestrictionEntry(key,
                                    Integer.parseInt(value));
                        }
                        break;
                    case STRING_TYPE_INDEX:
                        newRestrictionEntry = new RestrictionEntry(key, value);
                        break;
                    case STRING_ARRAY_INDEX:
                        newRestrictionEntry = new RestrictionEntry(key,
                                (String[]) viewHolder.strArrValInput.getTag());
                        break;
                }
                if (newRestrictionEntry != null) {
                    mRestrictionEntries.set(viewHolder.position, newRestrictionEntry);
                }
            }
        }
    };

    private View.OnClickListener mDeleteRowOnClickListener = new View.OnClickListener() {
        @Override
        public void onClick(View v) {
            ViewParent parentView = v.getParent();
            if (parentView instanceof View && ((View) parentView).getTag() != null) {
                AppRestrictionsViewHolder viewHolder
                        = (AppRestrictionsViewHolder) ((View) parentView).getTag();
                mRestrictionEntries.remove(viewHolder.position);
                mIsRowInSaveStateList.remove(viewHolder.position);
                notifyDataSetChanged();
            }
        }
    };

    public AppRestrictionsArrayAdapter(Context context, int resource,
            List<RestrictionEntry> objects) {
        super(context, resource, objects);
        mRestrictionEntries = objects;
        mAppRestrictionsTypeArrayAdapter = new ArrayAdapter<String>(context,
                android.R.layout.simple_spinner_item, APP_RESTRICTIONS_TYPES);
        mIsRowInSaveStateList = new ArrayList<Boolean>();
        for (int i = 0; i < getCount(); i++) {
            mIsRowInSaveStateList.add(false);
        }
    }

    @Override
    public View getView(final int position, View convertView, ViewGroup parent) {
        if (convertView == null) {
            convertView = LayoutInflater.from(getContext()).inflate(R.layout.app_restrictions_row,
                    parent, false);
        }
        AppRestrictionsViewHolder viewHolder = new AppRestrictionsViewHolder();
        viewHolder.restrictionEntry = getItem(position);
        viewHolder.keyInput = ((EditText) convertView.findViewById(R.id.key));
        viewHolder.strValInput = ((EditText) convertView.findViewById(R.id.value_str));
        viewHolder.strArrValInput = (Button) convertView.findViewById(R.id.value_str_arr);
        viewHolder.boolValInput = ((Switch) convertView.findViewById(R.id.value_bool));
        viewHolder.type = ((Spinner) convertView.findViewById(R.id.type));
        viewHolder.position = position;
        convertView.setTag(viewHolder);

        viewHolder.type.setAdapter(mAppRestrictionsTypeArrayAdapter);
        viewHolder.type.setOnItemSelectedListener(mTypeSpinnerOnItemSelectedListener);
        initAppRestrictionsRowUi(viewHolder.restrictionEntry, convertView, position);
        convertView.findViewById(R.id.delete_row).setOnClickListener(mDeleteRowOnClickListener);
        convertView.findViewById(R.id.edit_or_save_row).setOnClickListener(
                mEditOrSaveOnClickListener);
        return convertView;
    }

    /**
     * Constructs a {@link android.os.Bundle} of app restrictions from the UI and send it to the
     * system by invoking {@link DevicePolicyManager#setApplicationRestrictions(
     * android.content.ComponentName, String, android.os.Bundle)}.
     */
    public void setAppRestrictions(String pkgName) {
        boolean restrictionsAreValid = true;
        if (!TextUtils.isEmpty(pkgName)) {
            Bundle appRestrictions = new Bundle();
            for (int i = 0; i < getCount(); i++) {
                RestrictionEntry restrictionEntry = getItem(i);
                String key = restrictionEntry.getKey();
                if (!TextUtils.isEmpty(key)) {
                    switch (restrictionEntry.getType()) {
                        case RestrictionEntry.TYPE_BOOLEAN:
                            appRestrictions.putBoolean(key, restrictionEntry.getSelectedState());
                            break;
                        case RestrictionEntry.TYPE_INTEGER:
                            appRestrictions.putInt(key, restrictionEntry.getIntValue());
                            break;
                        case RestrictionEntry.TYPE_CHOICE:
                        case RestrictionEntry.TYPE_STRING:
                            appRestrictions.putString(key, restrictionEntry.getSelectedString());
                            break;
                        case RestrictionEntry.TYPE_MULTI_SELECT:
                            appRestrictions.putStringArray(key,
                                    restrictionEntry.getAllSelectedStrings());
                            break;
                    }
                } else {
                    restrictionsAreValid = false;
                    break;
                }
            }
            if (restrictionsAreValid) {
                ((DevicePolicyManager) getContext().getSystemService(Context.DEVICE_POLICY_SERVICE))
                        .setApplicationRestrictions(DeviceAdminReceiver.getComponentName(
                                        getContext()), pkgName, appRestrictions);
                Toast.makeText(getContext(),
                        getContext().getString(R.string.set_app_restrictions_success, pkgName),
                        Toast.LENGTH_SHORT).show();
            }
        } else {
            restrictionsAreValid = false;
        }
        if (!restrictionsAreValid) {
            Toast.makeText(getContext(), R.string.set_app_restrictions_fail, Toast.LENGTH_SHORT)
                    .show();
        }
    }

    /**
     * Adds a new row for collecting a new app restriction entry. By default, an empty key value
     * entry with type String is inserted at the end of the ListView.
     */
    public void addNewRow() {
        mRestrictionEntries.add(new RestrictionEntry("", ""));
        mIsRowInSaveStateList.add(false);
        notifyDataSetChanged();
    }

    private void toggleAppRestrictionRowEditableUi(EditText keyEditText, EditText strValEditText,
            Switch boolValSwitch, Button strArrButton, Spinner typeSpinner, ImageView imageView,
            boolean isInSaveState) {
        if (isInSaveState) {
            keyEditText.setEnabled(true);
            typeSpinner.setEnabled(true);
            strValEditText.setEnabled(true);
            boolValSwitch.setEnabled(true);
            strArrButton.setEnabled(true);
            imageView.setImageResource(android.R.drawable.ic_menu_save);
        } else {
            keyEditText.setEnabled(false);
            typeSpinner.setEnabled(false);
            strValEditText.setEnabled(false);
            boolValSwitch.setEnabled(false);
            strArrButton.setEnabled(false);
            imageView.setImageResource(android.R.drawable.ic_menu_edit);
        }
    }

    private void initAppRestrictionsRowUi(final RestrictionEntry restrictionEntry, View view,
            final int position) {
        EditText keyEditText = ((EditText) view.findViewById(R.id.key));
        EditText strValText = ((EditText) view.findViewById(R.id.value_str));
        Switch boolValSwitch = ((Switch) view.findViewById(R.id.value_bool));
        Button strArrValButton = ((Button) view.findViewById(R.id.value_str_arr));
        Spinner typeSpinner = ((Spinner) view.findViewById(R.id.type));
        String key = restrictionEntry.getKey();
        String value = null;
        switch (restrictionEntry.getType()) {
            case RestrictionEntry.TYPE_BOOLEAN:
                typeSpinner.setSelection(BOOL_TYPE_INDEX);
                boolValSwitch.setChecked(restrictionEntry.getSelectedState());
                boolValSwitch.setVisibility(View.VISIBLE);
                strValText.setVisibility(View.GONE);
                strArrValButton.setVisibility(View.GONE);
                break;
            case RestrictionEntry.TYPE_INTEGER:
                value = Integer.toString(restrictionEntry.getIntValue());
                typeSpinner.setSelection(INT_TYPE_INDEX);
                strValText.setText(value);
                boolValSwitch.setVisibility(View.GONE);
                strValText.setVisibility(View.VISIBLE);
                strArrValButton.setVisibility(View.GONE);
                break;
            case RestrictionEntry.TYPE_STRING:
                value = restrictionEntry.getSelectedString();
                typeSpinner.setSelection(STRING_TYPE_INDEX);
                strValText.setText(value);
                boolValSwitch.setVisibility(View.GONE);
                strValText.setVisibility(View.VISIBLE);
                strArrValButton.setVisibility(View.GONE);
                break;
            case RestrictionEntry.TYPE_CHOICE:
                value = restrictionEntry.getSelectedString();
                typeSpinner.setSelection(STRING_TYPE_INDEX);
                strValText.setText(value);
                boolValSwitch.setVisibility(View.GONE);
                strValText.setVisibility(View.VISIBLE);
                strArrValButton.setVisibility(View.GONE);
                break;
            case RestrictionEntry.TYPE_MULTI_SELECT:
                value = Arrays.toString(restrictionEntry.getAllSelectedStrings());
                typeSpinner.setSelection(STRING_ARRAY_INDEX);
                if (!TextUtils.isEmpty(value)) {
                    strArrValButton.setText(value);
                    strArrValButton.setTag(restrictionEntry.getAllSelectedStrings());
                } else {
                    strArrValButton.setText("[]");
                }
                boolValSwitch.setVisibility(View.GONE);
                strValText.setVisibility(View.GONE);
                strArrValButton.setVisibility(View.VISIBLE);
                break;
        }
        if (!TextUtils.isEmpty(key)) {
            keyEditText.setText(key);
        } else {
            keyEditText.setText("");
        }

        toggleAppRestrictionRowEditableUi(keyEditText, strValText, boolValSwitch, strArrValButton,
                typeSpinner, (ImageView) view.findViewById(R.id.edit_or_save_row),
                mIsRowInSaveStateList.get(position));
    }

    /**
     * A view holder of the application restrictions UI.
     */
    private static class AppRestrictionsViewHolder {

        RestrictionEntry restrictionEntry;

        EditText keyInput;

        EditText strValInput;

        Button strArrValInput;

        Switch boolValInput;

        Spinner type;

        int position;

        public void updateViewsVisibility(int type) {
            int strValInputVisibility = View.GONE;
            int strArrValInputVisibility = View.GONE;
            int boolValInputVisibility = View.GONE;
            if (type == INT_TYPE_INDEX || type == STRING_TYPE_INDEX) {
                strValInputVisibility = View.VISIBLE;
            } else if (type == STRING_ARRAY_INDEX) {
                strArrValInputVisibility = View.VISIBLE;
            } else if (type == BOOL_TYPE_INDEX) {
                boolValInputVisibility = View.VISIBLE;
            }
            strValInput.setVisibility(strValInputVisibility);
            strArrValInput.setVisibility(strArrValInputVisibility);
            boolValInput.setVisibility(boolValInputVisibility);
        }
    }
}