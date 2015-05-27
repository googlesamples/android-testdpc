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

package com.google.android.testdpc.policy.systemupdatepolicy;

import android.app.Fragment;
import android.app.TimePickerDialog;
import android.app.TimePickerDialog.OnTimeSetListener;
import android.app.admin.DevicePolicyManager;
import android.app.admin.SystemUpdatePolicy;
import android.content.Context;
import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Button;
import android.widget.EditText;
import android.widget.LinearLayout;
import android.widget.RadioGroup;
import android.widget.TimePicker;
import android.widget.Toast;

import com.google.android.testdpc.DeviceAdminReceiver;
import com.google.android.testdpc.R;


/**
 * This fragment provides functionalities related to managed system update that are available in a
 * device owner.
 * These includes
 * 1) {@link DevicePolicyManager#setSystemUpdatePolicy}
 * 2) {@link DevicePolicyManager#getSystemUpdatePolicy}
 * 3) {@link SystemUpdatePolicy}
 */
public class SystemUpdatePolicyFragment extends Fragment implements View.OnClickListener,
        RadioGroup.OnCheckedChangeListener {

    private EditText mCurrentSystemUpdatePolicy;

    private RadioGroup mSystemUpdatePolicySelection;

    private LinearLayout mMaintenanceWindowDetails;

    private Button mSetMaintenanceWindowStart;

    private Button mSetMaintenanceWindowEnd;

    private DevicePolicyManager mDpm;

    private int mMaintenanceStart;
    private int mMaintenanceEnd;

    @Override
    public void onResume() {
        super.onResume();
        getActivity().getActionBar().setTitle(R.string.system_update_policy);
        reloadSystemUpdatePolicy();
    }


    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        mDpm = (DevicePolicyManager) getActivity().getSystemService(Context.DEVICE_POLICY_SERVICE);
    }

    @Override
    public View onCreateView(LayoutInflater layoutInflater, ViewGroup container,
            Bundle savedInstanceState) {
        View view = layoutInflater.inflate(R.layout.system_update_policy, null);

        mCurrentSystemUpdatePolicy = (EditText) view.findViewById(
                R.id.system_update_policy_current);
        mSystemUpdatePolicySelection = (RadioGroup) view.findViewById(
                R.id.system_update_policy_selection);
        mMaintenanceWindowDetails = (LinearLayout) view.findViewById(
                R.id.system_update_policy_windowed_details);
        mSetMaintenanceWindowStart = (Button) view.findViewById(
                R.id.system_update_policy_window_start);
        mSetMaintenanceWindowEnd = (Button) view.findViewById(R.id.system_update_policy_window_end);

        mSetMaintenanceWindowStart.setOnClickListener(this);
        mSetMaintenanceWindowEnd.setOnClickListener(this);
        view.findViewById(R.id.system_update_policy_set).setOnClickListener(this);

        mSystemUpdatePolicySelection.setOnCheckedChangeListener(this);

        return view;
    }

    private void selectTime(final boolean isWindowStart) {
        int defaultMinutes = isWindowStart ? mMaintenanceStart : mMaintenanceEnd;
        TimePickerDialog timePicker = new TimePickerDialog(getActivity(), new OnTimeSetListener() {
                @Override
                public void onTimeSet(TimePicker arg0, int hour, int minutes) {
                    if (isWindowStart) {
                        mMaintenanceStart = hour * 60 + minutes;
                    } else {
                        mMaintenanceEnd = hour * 60 + minutes;
                    }
                    updateMaintenanceWindowDisplay();
                }
        }, defaultMinutes / 60, defaultMinutes % 60, true);
        timePicker.show();
    }

    @Override
    public void onClick(View v) {
        switch (v.getId()) {
            case R.id.system_update_policy_window_start:
                selectTime(true);
                break;
            case R.id.system_update_policy_window_end:
                selectTime(false);
                break;
            case R.id.system_update_policy_set:
                setSystemUpdatePolicy();
                reloadSystemUpdatePolicy();
                break;
        }
    }

    private void setSystemUpdatePolicy() {
        SystemUpdatePolicy newPolicy;
        switch (mSystemUpdatePolicySelection.getCheckedRadioButtonId()) {
            case R.id.system_update_policy_automatic:
                newPolicy = SystemUpdatePolicy.createAutomaticInstallPolicy();
                break;
            case R.id.system_update_policy_Windowed:
                newPolicy = SystemUpdatePolicy.createWindowedInstallPolicy(
                        mMaintenanceStart, mMaintenanceEnd);
                break;
            case R.id.system_update_policy_postpone:
                newPolicy = SystemUpdatePolicy.createPostponeInstallPolicy();
                break;
            case R.id.system_update_policy_none:
            default:
                newPolicy = null;
        }
        mDpm.setSystemUpdatePolicy(DeviceAdminReceiver.getComponentName(getActivity()), newPolicy);
    }

    private String formatMinutes(int minutes) {
        return String.format("%02d:%02d", minutes / 60, minutes % 60);
    }

    private void updateMaintenanceWindowDisplay() {
        mSetMaintenanceWindowStart.setText(formatMinutes(mMaintenanceStart));
        mSetMaintenanceWindowEnd.setText(formatMinutes(mMaintenanceEnd));
    }

    private void reloadSystemUpdatePolicy() {
        SystemUpdatePolicy policy = mDpm.getSystemUpdatePolicy();
        String policyDescription = "Unknown";

        if (policy == null) {
            policyDescription = "None";
            mSystemUpdatePolicySelection.check(R.id.system_update_policy_none);
            mMaintenanceWindowDetails.setVisibility(View.INVISIBLE);
        } else {
            switch (policy.getPolicyType()) {
                case SystemUpdatePolicy.TYPE_INSTALL_AUTOMATIC:
                    policyDescription = "Automatic";
                    mSystemUpdatePolicySelection.check(R.id.system_update_policy_automatic);
                    mMaintenanceWindowDetails.setVisibility(View.INVISIBLE);
                    break;
                case SystemUpdatePolicy.TYPE_INSTALL_WINDOWED: {
                    mMaintenanceStart = policy.getInstallWindowStart();
                    mMaintenanceEnd = policy.getInstallWindowEnd();
                    policyDescription = String.format("Windowed: %s-%s",
                            formatMinutes(mMaintenanceStart), formatMinutes(mMaintenanceEnd));
                    updateMaintenanceWindowDisplay();

                    mSystemUpdatePolicySelection.check(R.id.system_update_policy_Windowed);
                    mMaintenanceWindowDetails.setVisibility(View.VISIBLE);
                    break;
                }
                case SystemUpdatePolicy.TYPE_POSTPONE:
                    policyDescription = "Postpone";
                    mSystemUpdatePolicySelection.check(R.id.system_update_policy_postpone);
                    mMaintenanceWindowDetails.setVisibility(View.INVISIBLE);
                    break;
            }
        }
        mCurrentSystemUpdatePolicy.setText(policyDescription);
    }

    @Override
    public void onCheckedChanged(RadioGroup view, int checkedId) {
        if (checkedId == R.id.system_update_policy_Windowed) {
            updateMaintenanceWindowDisplay();
            mMaintenanceWindowDetails.setVisibility(View.VISIBLE);
        } else {
            mMaintenanceWindowDetails.setVisibility(View.INVISIBLE);
        }
    }
}
