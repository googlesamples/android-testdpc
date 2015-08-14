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

package com.sample.android.testdpc;

import android.annotation.TargetApi;
import android.app.Activity;
import android.app.Fragment;
import android.content.Intent;
import android.os.Build;
import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.RadioGroup;
import android.widget.Toast;

import com.android.setupwizardlib.SetupWizardLayout;
import com.android.setupwizardlib.view.NavigationBar;
import com.sample.android.testdpc.common.ProvisioningStateUtil;

import static android.app.admin.DevicePolicyManager.ACTION_PROVISION_MANAGED_DEVICE;
import static android.app.admin.DevicePolicyManager.ACTION_PROVISION_MANAGED_PROFILE;
import static android.app.admin.DevicePolicyManager.EXTRA_PROVISIONING_DEVICE_ADMIN_COMPONENT_NAME;
import static android.app.admin.DevicePolicyManager.EXTRA_PROVISIONING_DEVICE_ADMIN_PACKAGE_NAME;

/**
 * This {@link Fragment} shows the UI that allows the user to start the setup of a managed profile
 * or configuration of a device-owner if the device is in an appropriate state.
 */
public class SetupManagementFragment extends Fragment
        implements NavigationBar.NavigationBarListener {

    private static final int REQUEST_PROVISION_MANAGED_PROFILE = 1;
    private static final int REQUEST_PROVISION_DEVICE_OWNER = 2;

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container,
            Bundle savedInstanceState) {
        View view = inflater.inflate(R.layout.setup_management_fragment, container, false);
        SetupWizardLayout layout = (SetupWizardLayout) view.findViewById(R.id.setup_wizard_layout);
        NavigationBar navigationBar = layout.getNavigationBar();
        navigationBar.setNavigationBarListener(this);
        navigationBar.getNextButton().setText(R.string.setup_label);
        return view;
    }

    @Override
    public void onResume() {
        super.onResume();

        boolean canSetupDeviceOwner = false;

        // ACTION_PROVISION_MANAGED_DEVICE is new for Android M, cannot enable on Lollipop or
        // earlier.
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.M) {
            // Device owner can only be set very early in the device setup flow, we only enabled the
            // option if we haven't passed the point of no return.
            canSetupDeviceOwner = ProvisioningStateUtil.isDeviceUnprovisionedAndNoDeviceOwner(
                    getActivity());
        }

        getView().findViewById(R.id.setup_device_owner).setVisibility(
                canSetupDeviceOwner ? View.VISIBLE : View.GONE);
    }

    /**
     * Initiates the managed profile provisioning. If we already have a managed profile set up on
     * this device, we will get an error dialog in the following provisioning phase.
     */
    private void provisionManagedProfile() {
        Activity activity = getActivity();

        Intent intent = new Intent(ACTION_PROVISION_MANAGED_PROFILE);
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.M) {
            intent.putExtra(EXTRA_PROVISIONING_DEVICE_ADMIN_COMPONENT_NAME,
                    DeviceAdminReceiver.getComponentName(getActivity()));
        } else {
            intent.putExtra(EXTRA_PROVISIONING_DEVICE_ADMIN_PACKAGE_NAME,
                    getActivity().getPackageName());
        }

        if (intent.resolveActivity(activity.getPackageManager()) != null) {
            startActivityForResult(intent, REQUEST_PROVISION_MANAGED_PROFILE);
        } else {
            Toast.makeText(activity, R.string.provisioning_not_supported, Toast.LENGTH_SHORT)
                    .show();
        }
    }

    /**
     * Initiates the device owner provisioning. If we already have a device owner set up on
     * this device (or the device is no longer in an unprovisioned state) we will get an error
     * dialog in the following provisioning phase.
     */
    @TargetApi(Build.VERSION_CODES.M) // ACTION_PROVISION_MANAGED_DEVICE is new for Android M.
    private void provisionDeviceOwner() {
        Activity activity = getActivity();

        Intent intent = new Intent(ACTION_PROVISION_MANAGED_DEVICE);
        intent.putExtra(EXTRA_PROVISIONING_DEVICE_ADMIN_COMPONENT_NAME,
                DeviceAdminReceiver.getComponentName(getActivity()));

        if (intent.resolveActivity(activity.getPackageManager()) != null) {
            startActivityForResult(intent, REQUEST_PROVISION_DEVICE_OWNER);
        } else {
            Toast.makeText(activity, R.string.provisioning_not_supported, Toast.LENGTH_SHORT)
                    .show();
        }
    }

    @Override
    public void onActivityResult(int requestCode, int resultCode, Intent data) {
        Activity activity = getActivity();

        switch (requestCode) {
            case REQUEST_PROVISION_MANAGED_PROFILE:
            case REQUEST_PROVISION_DEVICE_OWNER:
                if (resultCode == Activity.RESULT_OK) {
                    // Success, finish the enclosing activity.
                    activity.finish();
                } else {
                    // Something went wrong (either provisioning failed, or the user backed out).
                    // Let the user decide how to proceed.
                    Toast.makeText(activity, R.string.provisioning_failed_or_cancelled,
                            Toast.LENGTH_SHORT).show();
                }
                break;
        }
    }

    @Override
    public void onNavigateBack() {
        getActivity().onBackPressed();
    }

    @Override
    public void onNavigateNext() {
        RadioGroup setupOptions = (RadioGroup) getView().findViewById(R.id.setup_options);
        if (setupOptions.getCheckedRadioButtonId() == R.id.setup_managed_profile) {
            provisionManagedProfile();
        } else {
            provisionDeviceOwner();
        }
    }
}
