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
import android.content.Context;
import android.content.Intent;
import android.os.Bundle;
import android.text.TextUtils;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;

import com.afwsamples.testdpc.R;
import com.afwsamples.testdpc.common.EditDeleteArrayAdapter;
import com.afwsamples.testdpc.common.ManageAppFragment;

import java.util.ArrayList;
import java.util.List;

import static com.afwsamples.testdpc.common.EditDeleteArrayAdapter.OnDeleteButtonClickListener;
import static com.afwsamples.testdpc.common.EditDeleteArrayAdapter.OnEditButtonClickListener;
import static com.afwsamples.testdpc.common.keyvaluepair.KeyValuePairDialogFragment.ARG_APP_NAME;
import static com.afwsamples.testdpc.common.keyvaluepair.KeyValuePairDialogFragment
        .ARG_INITIAL_VALUE;
import static com.afwsamples.testdpc.common.keyvaluepair.KeyValuePairDialogFragment.ARG_KEY;
import static com.afwsamples.testdpc.common.keyvaluepair.KeyValuePairDialogFragment.DialogType;
import static com.afwsamples.testdpc.common.keyvaluepair.KeyValuePairDialogFragment.RESULT_KEY;
import static com.afwsamples.testdpc.common.keyvaluepair.KeyValuePairDialogFragment.RESULT_TYPE;
import static com.afwsamples.testdpc.common.keyvaluepair.KeyValuePairDialogFragment.RESULT_VALUE;

public class KeyValueBundleFragment extends ManageAppFragment implements
        OnEditButtonClickListener<String>, OnDeleteButtonClickListener<String> {
    private StringEditDeleteArrayAdapter mAdapter;
    /**
     * Key of the editing bundle.
     */
    private String mKey;
    /**
     * The current bundle.
     */
    private Bundle mBundle;
    /**
     * The initial value of the passed in bundle.
     */
    private Bundle mInitialBundle;
    /**
     * Key of entry that is currently editing.
     */
    private String mEditingKey;
    private String mAppName;
    List<String> mKeyList;

    private static final int RESULT_CODE_EDIT_DIALOG = 1;

    private static final int[] SUPPORTED_TYPE = {
            DialogType.BOOL_TYPE,
            DialogType.INT_TYPE,
            DialogType.STRING_TYPE,
            DialogType.STRING_ARRAY_TYPE,
            DialogType.BUNDLE_TYPE,
            DialogType.BUNDLE_ARRAY_TYPE
    };

    /**
     * @param key key of the bundle. Can be null if not needed.
     * @param bundle initial value of the bundle for editing.
     */
    public static KeyValueBundleFragment newInstance(String key, Bundle bundle, String appName) {
        Bundle arguments = new Bundle();
        arguments.putString(ARG_KEY, key);
        arguments.putBundle(ARG_INITIAL_VALUE, bundle);
        arguments.putString(ARG_APP_NAME, appName);
        KeyValueBundleFragment fragment = new KeyValueBundleFragment();
        fragment.setArguments(arguments);
        return fragment;
    }

    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        mAppName = getArguments().getString(ARG_APP_NAME);
        mKey = getArguments().getString(ARG_KEY);
        mBundle = getArguments().getBundle(ARG_INITIAL_VALUE);
        if (mBundle == null) {
            mBundle = new Bundle();
        }
        mInitialBundle = new Bundle(mBundle);
        mKeyList = new ArrayList<>();
        mKeyList.addAll(mBundle.keySet());
    }

    @Override
    public View onCreateView(LayoutInflater layoutInflater, ViewGroup container,
            Bundle savedInstanceState) {
        View view = super.onCreateView(layoutInflater, container, savedInstanceState);
        mManagedAppsSpinner.setVisibility(View.GONE);
        mAdapter = new StringEditDeleteArrayAdapter(getActivity(), mKeyList, this, this);
        mAppListView.setAdapter(mAdapter);
        // header text
        mHeaderView.setVisibility(View.VISIBLE);
        mHeaderView.setText(getActivity().getString(R.string.app_restrictions_info, mAppName,
                mKey));
        return view;
    }

    @Override
    protected void loadData(String pkgName) {}

    @Override
    protected void resetConfig() {
        mAdapter.clear();
        mAdapter.addAll(mInitialBundle.keySet());
    }

    @Override
    protected void saveConfig() {
        Intent intent = new Intent();
        intent.putExtra(RESULT_KEY, mKey);
        intent.putExtra(RESULT_TYPE, DialogType.BUNDLE_TYPE);
        intent.putExtra(RESULT_VALUE, mBundle);
        getTargetFragment().onActivityResult(getTargetRequestCode(), Activity.RESULT_OK, intent);
        getFragmentManager().popBackStack();
    }

    @Override
    protected void addNewRow() {
        showEditDialog(null);
    }

    @Override
    protected void loadDefault() {}

    private void showEditDialog(final String key) {
        mEditingKey = key;
        int type = DialogType.BOOL_TYPE;
        Object value = null;
        if (key != null) {
             value = mBundle.get(key);
            if (value instanceof Boolean) {
                type = DialogType.BOOL_TYPE;
            } else if (value instanceof Integer) {
                type = DialogType.INT_TYPE;
            } else if (value instanceof String) {
                type = DialogType.STRING_TYPE;
            } else if (value instanceof String[]) {
                type = DialogType.STRING_ARRAY_TYPE;
            } else if (value instanceof Bundle) {
                type = DialogType.BUNDLE_TYPE;
            } else if (value instanceof Bundle[]) {
                type = DialogType.BUNDLE_ARRAY_TYPE;
            }
        }
        KeyValuePairDialogFragment dialogFragment =
                KeyValuePairDialogFragment.newInstance(type, true, key, value, SUPPORTED_TYPE,
                        mAppName);
        dialogFragment.setTargetFragment(this, RESULT_CODE_EDIT_DIALOG);
        dialogFragment.show(getFragmentManager(), "dialog");
    }

    @Override
    public void onEditButtonClick(String key) {
        showEditDialog(key);
    }

    @Override
    public void onDeleteButtonClick(String entry) {
        mBundle.remove(entry);
    }

    @Override
    public void onActivityResult(int requestCode, int resultCode, Intent result) {
        if (resultCode != Activity.RESULT_OK) {
            return;
        }
        switch (requestCode) {
            case RESULT_CODE_EDIT_DIALOG:
                int type = result.getIntExtra(KeyValuePairDialogFragment.RESULT_TYPE, 0);
                String key = result.getStringExtra(KeyValuePairDialogFragment.RESULT_KEY);
                updateBundleFromResultIntent(type, key, result);
                if (!TextUtils.isEmpty(mEditingKey)) {
                    mAdapter.remove(mEditingKey);
                }
                mAdapter.add(key);
                mAdapter.notifyDataSetChanged();
                mEditingKey = null;
                break;
        }
    }

    private static class StringEditDeleteArrayAdapter extends EditDeleteArrayAdapter<String> {

        public StringEditDeleteArrayAdapter(Context context, List<String> entries,
                OnEditButtonClickListener onEditButtonClickListener,
                OnDeleteButtonClickListener onDeleteButtonClickListener) {
            super(context, entries, onEditButtonClickListener, onDeleteButtonClickListener);
        }

        @Override
        protected String getDisplayName(String entry) {
            return entry;
        }
    }

    private void updateBundleFromResultIntent(int type, String key, Intent intent) {
        switch (type) {
            case DialogType.BOOL_TYPE:
                mBundle.putBoolean(key, intent.getBooleanExtra(RESULT_VALUE, false));
                break;
            case DialogType.INT_TYPE:
                mBundle.putInt(key, intent.getIntExtra(RESULT_VALUE, 0));
                break;
            case DialogType.STRING_TYPE:
                mBundle.putString(key, intent.getStringExtra(RESULT_VALUE));
                break;
            case DialogType.STRING_ARRAY_TYPE:
                mBundle.putStringArray(key, intent.getStringArrayExtra(RESULT_VALUE));
                break;
            case DialogType.BUNDLE_TYPE:
                mBundle.putBundle(key, intent.getBundleExtra(RESULT_VALUE));
                break;
            case DialogType.BUNDLE_ARRAY_TYPE:
                mBundle.putParcelableArray(key, intent.getParcelableArrayExtra(RESULT_VALUE));
                break;
            default:
                throw new IllegalArgumentException("invalid type:" + type);
        }
    }

}
