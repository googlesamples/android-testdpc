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

package com.afwsamples.testdpc;

import android.annotation.TargetApi;
import android.app.Activity;
import android.app.Fragment;
import android.content.ClipData;
import android.content.ContentResolver;
import android.content.Intent;
import android.net.Uri;
import android.os.Build;
import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.View;
import android.view.View.OnClickListener;
import android.view.ViewGroup;
import android.widget.Button;
import android.widget.RadioGroup;
import android.widget.TextView;
import android.widget.Toast;

import com.afwsamples.testdpc.common.ProvisioningStateUtil;
import com.android.setupwizardlib.SetupWizardLayout;
import com.android.setupwizardlib.view.NavigationBar;

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
    private static final int REQUEST_GET_LOGO = 3;

    // TODO: use DevicePolicyManager.EXTRA_PROVISIONING_LOGO_URI once it's available
    private static final String EXTRA_PROVISIONING_LOGO_URI
            = "android.app.extra.PROVISIONING_LOGO_URI";

    private Button mNavigationNextButton;
    private Button mChooseLogoButton;

    private Uri mLogoUri = null;

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container,
            Bundle savedInstanceState) {
        View view = inflater.inflate(R.layout.setup_management_fragment, container, false);
        SetupWizardLayout layout = (SetupWizardLayout) view.findViewById(R.id.setup_wizard_layout);
        NavigationBar navigationBar = layout.getNavigationBar();
        navigationBar.setNavigationBarListener(this);
        navigationBar.getBackButton().setText(R.string.exit);
        mNavigationNextButton = navigationBar.getNextButton();
        mNavigationNextButton.setText(R.string.setup_label);

        mChooseLogoButton = (Button) view.findViewById(R.id.choose_logo_button);
        // The extra logo uri is supported only from N
        if (ProvisioningStateUtil.versionIsAtLeastN() && canAnAppHandleGetContent()) {
            mChooseLogoButton.setVisibility(View.VISIBLE);
            mChooseLogoButton.setOnClickListener(new OnClickListener() {
                    @Override
                    public void onClick(View v) {
                        startActivityForResult(getGetContentIntent(), REQUEST_GET_LOGO);
                    }
                });
        }
        if (savedInstanceState != null) {
            mLogoUri = (Uri) savedInstanceState.getParcelable(EXTRA_PROVISIONING_LOGO_URI);
        }
        if (mLogoUri == null) {
            mLogoUri = Uri.parse(ContentResolver.SCHEME_ANDROID_RESOURCE + "://"
                    + getActivity().getPackageName() + "/" + R.drawable.ic_launcher);
        }
        return view;
    }

    @Override
    public void onSaveInstanceState(Bundle outState) {
        outState.putParcelable(EXTRA_PROVISIONING_LOGO_URI, mLogoUri);
    }

    @Override
    public void onResume() {
        super.onResume();

        getActivity().getActionBar().hide();
        if (!setProvisioningMethodsVisibility()) {
            showNoProvisioningPossibleUI();
        }
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
        specifyLogoUri(intent);
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
        specifyLogoUri(intent);
        if (intent.resolveActivity(activity.getPackageManager()) != null) {
            startActivityForResult(intent, REQUEST_PROVISION_DEVICE_OWNER);
        } else {
            Toast.makeText(activity, R.string.provisioning_not_supported, Toast.LENGTH_SHORT)
                    .show();
        }
    }

    private void specifyLogoUri(Intent intent) {
        intent.putExtra(EXTRA_PROVISIONING_LOGO_URI, mLogoUri);
        if (mLogoUri.getScheme().equals(ContentResolver.SCHEME_CONTENT)) {
            intent.setFlags(Intent.FLAG_GRANT_READ_URI_PERMISSION);
            intent.setClipData(ClipData.newUri(getActivity().getContentResolver(), "", mLogoUri));
        }
    }

    private void showNoProvisioningPossibleUI() {
        mNavigationNextButton.setVisibility(View.GONE);
        TextView textView = (TextView) getView().findViewById(R.id.setup_management_message_id);
        textView.setText(R.string.provisioning_not_possible);
    }

    /**
     * Set visibility of all provisioning methods
     *
     * @return false if none of the provisioning method is visible
     */
    private boolean setProvisioningMethodsVisibility() {
        boolean hasProvisioningOption = false;
        hasProvisioningOption |= setVisibility(ACTION_PROVISION_MANAGED_PROFILE,
                R.id.setup_managed_profile);
        hasProvisioningOption |= setVisibility(ACTION_PROVISION_MANAGED_DEVICE,
                R.id.setup_device_owner);
        return hasProvisioningOption;
    }

    private boolean setVisibility(String action, int radioButtonId) {
        final int visibility = ProvisioningStateUtil.isProvisioningAllowed(getActivity(), action)
                ? View.VISIBLE : View.GONE;
        getView().findViewById(radioButtonId).setVisibility(visibility);
        return visibility == View.VISIBLE;
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
            case REQUEST_GET_LOGO:
                if (data != null && data.getData() != null) {
                    mLogoUri = data.getData();
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

    private Intent getGetContentIntent() {
        Intent getContent = new Intent(Intent.ACTION_GET_CONTENT);
        getContent.setType("image/*");
        return getContent;
    }

    private boolean canAnAppHandleGetContent() {
        return getGetContentIntent().resolveActivity(getActivity().getPackageManager()) != null;
    }
}
