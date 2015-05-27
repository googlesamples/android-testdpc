/*
 * Copyright (C) 2015 The Android Open Source Project
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

package com.google.android.testdpc.common;

import android.app.Activity;
import android.app.AlertDialog;
import android.app.Fragment;
import android.app.admin.DevicePolicyManager;
import android.content.Context;
import android.content.DialogInterface;
import android.content.Intent;
import android.os.Bundle;
import android.text.TextUtils;
import android.text.method.ScrollingMovementMethod;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.AdapterView;
import android.widget.ArrayAdapter;
import android.widget.Button;
import android.widget.EditText;
import android.widget.Spinner;
import android.widget.TextView;

import com.google.android.testdpc.R;

import java.util.Arrays;
import java.util.HashSet;

/**
 * A base class which handles the construction of an intent or an intent filter.
 */
public abstract class IntentOrIntentFilterFragment extends Fragment implements
        View.OnClickListener,
        AdapterView.OnItemSelectedListener {

    private static final String CUSTOM = "Custom";
    private static final int CUSTOM_INPUT_TYPE_ACTIONS = 0;
    private static final int CUSTOM_INPUT_TYPE_CATEGORIES = 1;
    private static final int CUSTOM_INPUT_TYPE_SCHEMES = 2;
    /**
     * A list of common actions
     */
    protected static final String[] ACTIONS_LIST = new String[]{
            Intent.ACTION_MAIN,
            Intent.ACTION_VIEW,
            Intent.ACTION_ATTACH_DATA,
            Intent.ACTION_EDIT,
            Intent.ACTION_PICK,
            Intent.ACTION_CHOOSER,
            Intent.ACTION_GET_CONTENT,
            Intent.ACTION_DIAL,
            Intent.ACTION_CALL,
            Intent.ACTION_SEND,
            Intent.ACTION_SENDTO,
            Intent.ACTION_ANSWER,
            Intent.ACTION_INSERT,
            Intent.ACTION_DELETE,
            Intent.ACTION_RUN,
            Intent.ACTION_SYNC,
            Intent.ACTION_PICK_ACTIVITY,
            Intent.ACTION_SEARCH,
            Intent.ACTION_WEB_SEARCH,
            Intent.ACTION_FACTORY_TEST,
            CUSTOM
    };

    /**
     * A list of common categories
     */
    protected static final String[] CATEGORIES_LIST = new String[]{
            Intent.CATEGORY_DEFAULT,
            Intent.CATEGORY_BROWSABLE,
            Intent.CATEGORY_TAB,
            Intent.CATEGORY_ALTERNATIVE,
            Intent.CATEGORY_SELECTED_ALTERNATIVE,
            Intent.CATEGORY_LAUNCHER,
            Intent.CATEGORY_INFO,
            Intent.CATEGORY_HOME,
            Intent.CATEGORY_PREFERENCE,
            Intent.CATEGORY_TEST,
            Intent.CATEGORY_CAR_DOCK,
            Intent.CATEGORY_DESK_DOCK,
            Intent.CATEGORY_LE_DESK_DOCK,
            Intent.CATEGORY_HE_DESK_DOCK,
            Intent.CATEGORY_CAR_MODE,
            Intent.CATEGORY_APP_MARKET,
            CUSTOM
    };

    /**
     * A list of common data schemes
     */
    protected static final String[] DATA_SCHEMES_LIST = new String[]{
            "http",
            "https",
            "tel",
            "mailto",
            "geo",
            CUSTOM
    };

    protected DevicePolicyManager mDevicePolicyManager;

    protected Spinner mActionsSpinner;

    protected Spinner mCategoriesSpinner;

    protected Spinner mDataSchemesSpinner;

    protected HashSet<String> mActions = new HashSet<String>();

    protected HashSet<String> mCategories = new HashSet<String>();

    protected HashSet<String> mDataSchemes = new HashSet<String>();

    protected Button mAddActionButton;

    protected Button mAddDataSchemeAction;

    protected Button mClearButton;

    protected Button mAddButton;

    protected TextView mStatusTextView;

    @Override
    public View onCreateView(LayoutInflater layoutInflater, ViewGroup container,
            Bundle savedInstanceState) {
        Activity activity = getActivity();

        mDevicePolicyManager = (DevicePolicyManager) activity
                .getSystemService(Context.DEVICE_POLICY_SERVICE);

        View intentOrIntentFilterView = layoutInflater.inflate(
                R.layout.add_intent_or_intent_filter, null);

        // Prepare add actions view
        mActionsSpinner = (Spinner) intentOrIntentFilterView.findViewById(
                R.id.list_of_actions);
        mActionsSpinner.setAdapter(new ArrayAdapter<String>(activity,
                android.R.layout.simple_spinner_item, Arrays.asList(ACTIONS_LIST)));
        mActionsSpinner.setOnItemSelectedListener(this);
        mAddActionButton = (Button) intentOrIntentFilterView.findViewById(
                R.id.btn_add_action);
        mAddActionButton.setOnClickListener(this);

        // Prepare add categories view
        mCategoriesSpinner = (Spinner) intentOrIntentFilterView.findViewById(
                R.id.list_of_categories);
        mCategoriesSpinner.setAdapter(new ArrayAdapter<String>(activity,
                android.R.layout.simple_spinner_item, Arrays.asList(CATEGORIES_LIST)));
        mCategoriesSpinner.setOnItemSelectedListener(this);
        Button addCategoryButton = (Button) intentOrIntentFilterView.findViewById(
                R.id.btn_add_category);
        addCategoryButton.setOnClickListener(this);

        // Prepare add data schemes view
        mDataSchemesSpinner = (Spinner) intentOrIntentFilterView.findViewById(
                R.id.list_of_data_schemes);
        mDataSchemesSpinner.setAdapter(new ArrayAdapter<String>(activity,
                android.R.layout.simple_spinner_item, Arrays.asList(DATA_SCHEMES_LIST)));
        mDataSchemesSpinner.setOnItemSelectedListener(this);
        mAddDataSchemeAction = (Button) intentOrIntentFilterView.findViewById(
                R.id.btn_add_data_scheme);
        mAddDataSchemeAction.setOnClickListener(this);

        mStatusTextView = (TextView) intentOrIntentFilterView.findViewById(
                R.id.tv_intent_filter);
        mStatusTextView.setMovementMethod(new ScrollingMovementMethod());

        // Prepare custom view for collecting custom action, category or data type
        mAddButton = (Button) intentOrIntentFilterView.findViewById(R.id.btn_add);
        mAddButton.setOnClickListener(this);

        mClearButton = (Button) intentOrIntentFilterView.findViewById(R.id.btn_clear);
        mClearButton.setOnClickListener(this);

        return intentOrIntentFilterView;
    }

    @Override
    public void onClick(View v) {
        int position = 0;
        switch (v.getId()) {
            case R.id.btn_add_action:
                position = mActionsSpinner.getSelectedItemPosition();
                mActions.add(ACTIONS_LIST[position]);
                updateStatusTextView();
                break;
            case R.id.btn_add_category:
                position = mCategoriesSpinner.getSelectedItemPosition();
                mCategories.add(CATEGORIES_LIST[position]);
                updateStatusTextView();
                break;
            case R.id.btn_add_data_scheme:
                position = mDataSchemesSpinner.getSelectedItemPosition();
                mDataSchemes.add(DATA_SCHEMES_LIST[position]);
                updateStatusTextView();
                break;
            case R.id.btn_clear:
                clearIntentOrIntentFilter();
                updateStatusTextView();
                break;
            default:
                break;
        }
    }

    protected abstract void updateStatusTextView();

    /**
     * Cleans up the intent or the intent filter lists
     */
    protected void clearIntentOrIntentFilter() {
        mActions.clear();
        mAddActionButton.setEnabled(true);
        mCategories.clear();
        mDataSchemes.clear();
    }

    @Override
    public void onItemSelected(AdapterView<?> parent, View view, int position, long id) {
        switch (parent.getId()) {
            case R.id.list_of_actions:
                if (position == ACTIONS_LIST.length - 1) {
                    showCustomInputDialog(getString(R.string.actions_title),
                            CUSTOM_INPUT_TYPE_ACTIONS);
                }
                break;
            case R.id.list_of_categories:
                if (position == CATEGORIES_LIST.length - 1) {
                    showCustomInputDialog(getString(R.string.categories_title),
                            CUSTOM_INPUT_TYPE_CATEGORIES);
                }
                break;
            case R.id.list_of_data_schemes:
                if (position == DATA_SCHEMES_LIST.length - 1) {
                    showCustomInputDialog(getString(R.string.data_schemes_title),
                            CUSTOM_INPUT_TYPE_SCHEMES);
                }
                break;
            default:
                break;
        }
    }

    @Override
    public void onNothingSelected(AdapterView<?> parent) {
        // Nothing to do.
    }

    private void showCustomInputDialog(String title, final int customInputType) {
        LayoutInflater layoutInflater = LayoutInflater.from(getActivity());
        View customInputView = layoutInflater.inflate(
                R.layout.simple_edittext, null);
        final EditText customInputEditText = (EditText) customInputView.findViewById(
                R.id.input);
        AlertDialog.Builder customInputViewAlertBuilder = new AlertDialog.Builder(getActivity());
        customInputViewAlertBuilder.setView(customInputView);
        if (!TextUtils.isEmpty(title)) {
            customInputViewAlertBuilder.setTitle(title);
        }

        customInputViewAlertBuilder.setOnCancelListener(new DialogInterface.OnCancelListener() {

            @Override
            public void onCancel(DialogInterface dialog) {
                resetSpinners(customInputType);
                dialog.dismiss();
            }
        });
        customInputViewAlertBuilder
                .setPositiveButton(R.string.add, new DialogInterface.OnClickListener() {
                    @Override
                    public void onClick(DialogInterface dialog, int which) {
                        switch (customInputType) {
                            case CUSTOM_INPUT_TYPE_ACTIONS:
                                mActions.add(customInputEditText.getText().toString());
                                break;
                            case CUSTOM_INPUT_TYPE_CATEGORIES:
                                mCategories.add(customInputEditText.getText().toString());
                                break;
                            case CUSTOM_INPUT_TYPE_SCHEMES:
                                mDataSchemes.add(customInputEditText.getText().toString());
                                break;
                            default:
                                break;
                        }
                        resetSpinners(customInputType);
                        dialog.dismiss();
                        updateStatusTextView();
                    }
                });
        customInputViewAlertBuilder
                .setNegativeButton(android.R.string.cancel, new DialogInterface.OnClickListener() {
                    @Override
                    public void onClick(DialogInterface dialog, int which) {
                        resetSpinners(customInputType);
                        dialog.dismiss();
                    }
                });
        customInputViewAlertBuilder.show();
    }

    private void resetSpinners(int customInputType) {
        switch (customInputType) {
            case CUSTOM_INPUT_TYPE_ACTIONS:
                mActionsSpinner.setSelection(0);
                break;
            case CUSTOM_INPUT_TYPE_CATEGORIES:
                mCategoriesSpinner.setSelection(0);
                break;
            case CUSTOM_INPUT_TYPE_SCHEMES:
                mDataSchemesSpinner.setSelection(0);
                break;
            default:
                break;
        }
    }
}
