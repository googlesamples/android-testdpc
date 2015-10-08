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
package com.example.android.testdpc.profilepolicy.crossprofileintentfilter;

import android.app.admin.DevicePolicyManager;
import android.content.IntentFilter;
import android.content.IntentFilter.MalformedMimeTypeException;
import android.os.Bundle;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ArrayAdapter;
import android.widget.LinearLayout;
import android.widget.Spinner;
import android.widget.Toast;

import com.example.android.testdpc.DeviceAdminReceiver;
import com.example.android.testdpc.R;
import com.example.android.testdpc.common.IntentOrIntentFilterFragment;

import java.util.Arrays;
import java.util.Set;

/**
 * Contains sample codes for testing
 * {@link DevicePolicyManager#addCrossProfileIntentFilter(android.content.ComponentName,
 * android.content.IntentFilter, int)} and
 * {@link DevicePolicyManager#clearCrossProfileIntentFilters(android.content.ComponentName)}
 */
public class AddCrossProfileIntentFilterFragment extends IntentOrIntentFilterFragment {

    private static final String TAG = "AddCrossProfileIntentFilterFragment";
    /**
     * See
     * {@link android.app.admin.DevicePolicyManager#FLAG_MANAGED_CAN_ACCESS_PARENT} and
     * {@link android.app.admin.DevicePolicyManager#FLAG_PARENT_CAN_ACCESS_MANAGED}.
     */
    private static final String[] CROSS_PROFILE_INTENT_DIRECTIONS = new String[]{
            "FLAG_MANAGED_CAN_ACCESS_PARENT",
            "FLAG_PARENT_CAN_ACCESS_MANAGED"
    };

    private static final String NEW_LINE = System.getProperty("line.separator");

    private Spinner mCrossProfileDirectionSpinner;

    @Override
    public void onResume() {
        super.onResume();
        getActivity().getActionBar().setTitle(R.string.add_cross_profile_intents_title);
    }

    @Override
    public View onCreateView(LayoutInflater layoutInflater, ViewGroup container,
            Bundle savedInstanceState) {
        LinearLayout crossProfileIntentFilterContainer = (LinearLayout) layoutInflater
                .inflate(R.layout.cross_profile_intent, null);
        View intentView = super.onCreateView(layoutInflater, container, savedInstanceState);
        crossProfileIntentFilterContainer.addView(intentView);
        // Prepare spinner that indicates the cross profile intent direction
        mCrossProfileDirectionSpinner = (Spinner) crossProfileIntentFilterContainer.findViewById(
                R.id.cross_profile_intent_direction);
        mCrossProfileDirectionSpinner.setAdapter(new ArrayAdapter<String>(getActivity(),
                android.R.layout.simple_spinner_item,
                Arrays.asList(CROSS_PROFILE_INTENT_DIRECTIONS)));
        updateIntentFilterFragmentUi();
        return crossProfileIntentFilterContainer;
    }

    @Override
    public void onClick(View v) {
        boolean isClickHandled = false;
        switch (v.getId()) {
            case R.id.btn_add:
                IntentFilter intentFilter = getIntentFilter();
                if (intentFilter != null) {
                    mDevicePolicyManager.addCrossProfileIntentFilter(
                            DeviceAdminReceiver.getComponentName(getActivity()),
                            getIntentFilter(),
                            getAddCrossProfileIntentFilterFlag());
                    Toast.makeText(getActivity(),
                            getString(R.string.add_cross_profile_intent_success_msg),
                            Toast.LENGTH_SHORT).show();
                    // Prepare for the next cross-profile intent filter.
                    clearIntentOrIntentFilter();
                    updateStatusTextView();
                } else {
                    Toast.makeText(getActivity(),
                            getString(R.string.add_cross_profile_intent_failed_msg),
                            Toast.LENGTH_SHORT).show();
                }
                isClickHandled = true;
                break;
            default:
                break;
        }
        // If the click event is not handled, pass it to the super class.
        if (!isClickHandled) {
            super.onClick(v);
        }
    }

    @Override
    protected void updateStatusTextView() {
        StringBuilder stringBuilder = new StringBuilder();

        if (!mActions.isEmpty()) {
            stringBuilder.append(getString(R.string.actions_title)).append(NEW_LINE);
            dumpSet(stringBuilder, mActions);
            stringBuilder.append(NEW_LINE);
        }

        if (!mCategories.isEmpty()) {
            stringBuilder.append(getString(R.string.categories_title)).append(NEW_LINE);
            dumpSet(stringBuilder, mCategories);
            stringBuilder.append(NEW_LINE);
        }

        if (!mDataSchemes.isEmpty()) {
            stringBuilder.append(getString(R.string.data_schemes_title)).append(NEW_LINE);
            dumpSet(stringBuilder, mDataSchemes);
            stringBuilder.append(NEW_LINE);
        }

        if (!mDataTypes.isEmpty()) {
            stringBuilder.append(getString(R.string.data_types_title)).append(NEW_LINE);
            dumpSet(stringBuilder, mDataTypes);
        }

        mStatusTextView.setText(stringBuilder.toString());
    }

    private void dumpSet(StringBuilder builder, Set<?> set) {
        for (Object obj : set) {
            builder.append(obj.toString()).append(NEW_LINE);
        }
    }

    private int getAddCrossProfileIntentFilterFlag() {
        return mCrossProfileDirectionSpinner.getSelectedItemPosition() == 0
                ? DevicePolicyManager.FLAG_MANAGED_CAN_ACCESS_PARENT
                : DevicePolicyManager.FLAG_PARENT_CAN_ACCESS_MANAGED;
    }

    /**
     * Constructs an intent filter from user input. This intent-filter is used for cross-profile
     * intent.
     *
     * @return a user constructed intent filter.
     */
    private IntentFilter getIntentFilter() {
        if (mActions.isEmpty() && mCategories.isEmpty() && mDataSchemes.isEmpty()
                && mDataTypes.isEmpty()) {
            return null;
        }
        IntentFilter intentFilter = new IntentFilter();
        for (String action : mActions) {
            intentFilter.addAction(action);
        }
        for (String category : mCategories) {
            intentFilter.addCategory(category);
        }
        for (String dataScheme : mDataSchemes) {
            intentFilter.addDataScheme(dataScheme);
        }
        for (String dataType : mDataTypes) {
            try {
                intentFilter.addDataType(dataType);
            } catch (MalformedMimeTypeException e) {
                Log.e(TAG, "Malformed mime type: " + e);
                return null;
            }
        }
        return intentFilter;
    }

    private void updateIntentFilterFragmentUi() {
        mAddActionButton.setEnabled(true);
        mAddActionButton.setEnabled(true);
        mDataSchemesSpinner.setEnabled(true);
        mAddDataSchemeAction.setEnabled(true);
    }
}
