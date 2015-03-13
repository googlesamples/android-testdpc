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

package com.google.android.testdpc.profilepolicy.addsystemapps;

import android.app.AlertDialog;
import android.content.DialogInterface;
import android.content.Intent;
import android.os.Bundle;
import android.text.TextUtils;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.AdapterView;
import android.widget.Button;
import android.widget.EditText;
import android.widget.Toast;

import com.google.android.testdpc.DeviceAdminReceiver;
import com.google.android.testdpc.R;
import com.google.android.testdpc.common.IntentOrIntentFilterFragment;

/**
 * An UI class that helps constructing intents for enabling system apps.
 *
 * Contains sample codes for testing
 * {@link android.app.admin.DevicePolicyManager#enableSystemApp(android.content.ComponentName,
 * android.content.Intent)}
 */
public class EnableSystemAppsByIntentFragment extends IntentOrIntentFilterFragment {

    @Override
    public void onResume() {
        super.onResume();
        getActivity().getActionBar().setTitle(R.string.enable_system_apps_title);
    }

    @Override
    public View onCreateView(LayoutInflater layoutInflater, ViewGroup container,
            Bundle savedInstanceState) {
        View view = super.onCreateView(layoutInflater, container, savedInstanceState);
        Button addButton = (Button) view.findViewById(R.id.btn_add);
        // Hide data schemes UI because this is not useful for building an intent.
        view.findViewById(R.id.data_schemes_container).setVisibility(View.GONE);
        // There is only one action for an intent. Probably don't need an add button.
        mAddActionButton.setVisibility(View.GONE);
        addButton.setText(R.string.enable);
        return view;
    }

    @Override
    public void onClick(View v) {
        boolean isClickHandled = false;
        switch (v.getId()) {
            case R.id.btn_add:
                Intent intent = getIntent();
                if (intent == null) {
                    Toast.makeText(getActivity(), R.string.enable_system_apps_failure_msg,
                            Toast.LENGTH_SHORT).show();
                } else {
                    try {
                        mDevicePolicyManager.enableSystemApp(
                                DeviceAdminReceiver.getComponentName(getActivity()), intent);
                        Toast.makeText(getActivity(), R.string.enable_system_apps_attempt_msg,
                                Toast.LENGTH_SHORT).show();
                    } catch (IllegalArgumentException e) {
                        Toast.makeText(getActivity(), e.getLocalizedMessage(), Toast.LENGTH_SHORT)
                                .show();
                    }
                }
                isClickHandled = true;
                break;
        }
        // Pass the event to super class if it isn't handled.
        if (!isClickHandled) {
            super.onClick(v);
        }
    }

    @Override
    public void onItemSelected(AdapterView<?> parent, View view, int position, long id) {
        switch (parent.getId()) {
            case R.id.list_of_actions:
                if (position == ACTIONS_LIST.length - 1) {
                    showCustomActionInputDialog();
                } else {
                    mActions.clear();
                    mActions.add(ACTIONS_LIST[position]);
                    updateStatusTextView();
                }
                return;
            default:
                break;
        }
        super.onItemSelected(parent, view, position, id);
    }

    /**
     * Constructs an intent from user input. This intent is used for enabling system apps.
     *
     * @return a user constructed intent.
     */
    protected Intent getIntent() {
        if (mActions.isEmpty() && mCategories.isEmpty()) {
            return null;
        }
        Intent intent = new Intent();
        if (!mActions.isEmpty()) {
            // Only one action for an intent.
            intent.setAction(mActions.iterator().next());
        }
        for (String category : mCategories) {
            intent.addCategory(category);
        }
        return intent;
    }

    @Override
    protected void updateStatusTextView() {
        StringBuilder stringBuilder = new StringBuilder();
        if (!mActions.isEmpty()) {
            stringBuilder.append(getString(R.string.actions_title));
            stringBuilder.append("\n");
        }
        for (String action : mActions) {
            stringBuilder.append(action);
            stringBuilder.append("\n");
        }
        if (!mCategories.isEmpty()) {
            stringBuilder.append("\n");
            stringBuilder.append(getString(R.string.categories_title));
            stringBuilder.append("\n");
        }
        for (String category : mCategories) {
            stringBuilder.append(category);
            stringBuilder.append("\n");
        }
        if (!mDataSchemes.isEmpty()) {
            stringBuilder.append("\n");
            stringBuilder.append(getString(R.string.data_schemes_title));
            stringBuilder.append("\n");
        }
        for (String dataScheme : mDataSchemes) {
            stringBuilder.append(dataScheme);
            stringBuilder.append("\n");
        }
        mStatusTextView.setText(stringBuilder.toString());
    }

    private void showCustomActionInputDialog() {
        LayoutInflater layoutInflater = LayoutInflater.from(getActivity());
        View customInputView = layoutInflater.inflate(R.layout.simple_edittext, null);
        final EditText customInputEditText = (EditText) customInputView.findViewById(R.id.input);
        AlertDialog.Builder customInputViewAlertBuilder = new AlertDialog.Builder(getActivity());
        customInputViewAlertBuilder.setView(customInputView);
        customInputViewAlertBuilder.setTitle(getString(R.string.actions_title));
        customInputViewAlertBuilder.setOnCancelListener(new DialogInterface.OnCancelListener() {

            @Override
            public void onCancel(DialogInterface dialog) {
                mActionsSpinner.setSelection(0);
                dialog.dismiss();
            }
        });
        customInputViewAlertBuilder.setPositiveButton(R.string.add,
                new DialogInterface.OnClickListener() {
                    @Override
                    public void onClick(DialogInterface dialog, int which) {
                        String action = customInputEditText.getText().toString();
                        if (!TextUtils.isEmpty(action)) {
                            mActions.clear();
                            mActions.add(action);
                            updateStatusTextView();
                        } else {
                            // Fail to add an empty action, set the spinner to select the first
                            // entry.
                            Toast.makeText(getActivity(), R.string.invalid_system_apps_action,
                                    Toast.LENGTH_SHORT).show();
                            mActionsSpinner.setSelection(0);
                        }
                        dialog.dismiss();
                    }
                });
        customInputViewAlertBuilder.setNegativeButton(android.R.string.cancel,
                new DialogInterface.OnClickListener() {
                    @Override
                    public void onClick(DialogInterface dialog, int which) {
                        mActionsSpinner.setSelection(0);
                        dialog.dismiss();
                    }
                });
        customInputViewAlertBuilder.show();
    }
}
