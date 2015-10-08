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

package com.example.android.testdpc.syncauth;

import android.accounts.Account;
import android.annotation.TargetApi;
import android.app.Activity;
import android.content.Intent;
import android.os.Build;
import android.os.Bundle;
import android.os.PersistableBundle;
import android.view.View;
import android.widget.RadioGroup;
import android.widget.TextView;
import android.widget.Toast;

import com.android.setupwizardlib.SetupWizardLayout;
import com.android.setupwizardlib.view.NavigationBar;
import com.example.android.testdpc.DeviceAdminReceiver;
import com.example.android.testdpc.R;
import com.example.android.testdpc.common.LaunchIntentUtil;
import com.example.android.testdpc.common.ProvisioningStateUtil;

import static android.app.admin.DevicePolicyManager.ACTION_PROVISION_MANAGED_DEVICE;
import static android.app.admin.DevicePolicyManager.ACTION_PROVISION_MANAGED_PROFILE;
import static android.app.admin.DevicePolicyManager.EXTRA_PROVISIONING_ACCOUNT_TO_MIGRATE;
import static android.app.admin.DevicePolicyManager.EXTRA_PROVISIONING_ADMIN_EXTRAS_BUNDLE;
import static android.app.admin.DevicePolicyManager.EXTRA_PROVISIONING_DEVICE_ADMIN_COMPONENT_NAME;
import static android.app.admin.DevicePolicyManager.EXTRA_PROVISIONING_DEVICE_ADMIN_PACKAGE_NAME;

/**
 * Activity to allow the user to choose what type of management should be applied when adding a new
 * account to the Android device which requires management.
 */
public class SetupSyncAuthManagement extends Activity
        implements NavigationBar.NavigationBarListener {
    private static final int REQUEST_PROVISION_MANAGED_PROFILE = 1;
    private static final int REQUEST_PROVISION_DEVICE_OWNER = 2;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

        setContentView(R.layout.setup_sync_auth_management_activity);
        SetupWizardLayout layout = (SetupWizardLayout) findViewById(R.id.setup_wizard_layout);
        NavigationBar navigationBar = layout.getNavigationBar();
        navigationBar.setNavigationBarListener(this);
        navigationBar.getNextButton().setText(R.string.setup_label);

        Account addedAccount = LaunchIntentUtil.getAddedAccount(getIntent());
        if (addedAccount != null) {
            findViewById(R.id.managed_account_desc).setVisibility(View.VISIBLE);
            // Show the user which account needs management.
            TextView managedAccountName = (TextView) findViewById(R.id.managed_account_name);
            managedAccountName.setVisibility(View.VISIBLE);
            managedAccountName.setText(addedAccount.name);
        } else {
            // This is not an expected case, sync-auth is triggered by an account being added so we
            // expect to be told which account to migrate in the launch intent. We don't finish()
            // here as it's still technically feasible to continue.
            Toast.makeText(this, R.string.invalid_launch_intent_no_account, Toast.LENGTH_LONG)
                    .show();
        }
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
            canSetupDeviceOwner = ProvisioningStateUtil.isDeviceUnprovisionedAndNoDeviceOwner(this);
        }

        findViewById(R.id.setup_device_owner).setVisibility(
                canSetupDeviceOwner ? View.VISIBLE : View.GONE);
    }

    /**
     * Initiates the managed profile provisioning. If we already have a managed profile set up on
     * this device, we will get an error dialog in the following provisioning phase.
     */
    private void provisionManagedProfile() {
        Account accountToMigrate = LaunchIntentUtil.getAddedAccount(this.getIntent());

        Intent intent = new Intent(ACTION_PROVISION_MANAGED_PROFILE);
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.M) {
            intent.putExtra(EXTRA_PROVISIONING_DEVICE_ADMIN_COMPONENT_NAME,
                    DeviceAdminReceiver.getComponentName(this));
        } else {
            intent.putExtra(EXTRA_PROVISIONING_DEVICE_ADMIN_PACKAGE_NAME, getPackageName());
        }

        if (accountToMigrate != null) {
            // EXTRA_PROVISIONING_ACCOUNT_TO_MIGRATE only supported in API 22+.
            if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.LOLLIPOP_MR1) {
                // Configure the account to migrate into the managed profile if setup
                // completes.
                intent.putExtra(EXTRA_PROVISIONING_ACCOUNT_TO_MIGRATE, accountToMigrate);
            } else {
                Toast.makeText(this, R.string.migration_not_supported, Toast.LENGTH_SHORT).show();
            }
        }

        // Perculate launch intent extras through to DeviceAdminReceiver so they can be used there.
        PersistableBundle adminExtras = new PersistableBundle();
        LaunchIntentUtil.prepareDeviceAdminExtras(getIntent(), adminExtras);
        intent.putExtra(EXTRA_PROVISIONING_ADMIN_EXTRAS_BUNDLE, adminExtras);

        if (intent.resolveActivity(getPackageManager()) != null) {
            startActivityForResult(intent, REQUEST_PROVISION_MANAGED_PROFILE);
        } else {
            Toast.makeText(this, R.string.provisioning_not_supported, Toast.LENGTH_SHORT).show();
        }
    }

    /**
     * Initiates the managed profile provisioning. If we already have a managed profile set up on
     * this device, we will get an error dialog in the following provisioning phase.
     */
    @TargetApi(Build.VERSION_CODES.M) // ACTION_PROVISION_MANAGED_DEVICE is new for Android M.
    private void provisionDeviceOwner() {
        Intent intent = new Intent(ACTION_PROVISION_MANAGED_DEVICE);
        intent.putExtra(EXTRA_PROVISIONING_DEVICE_ADMIN_COMPONENT_NAME,
                DeviceAdminReceiver.getComponentName(this));

        // Propagate launch intent extras through to DeviceAdminReceiver so they can be used there.
        PersistableBundle adminExtras = new PersistableBundle();
        LaunchIntentUtil.prepareDeviceAdminExtras(getIntent(), adminExtras);
        intent.putExtra(EXTRA_PROVISIONING_ADMIN_EXTRAS_BUNDLE, adminExtras);

        if (intent.resolveActivity(getPackageManager()) != null) {
            startActivityForResult(intent, REQUEST_PROVISION_DEVICE_OWNER);
        } else {
            Toast.makeText(this, R.string.provisioning_not_supported, Toast.LENGTH_SHORT).show();
        }
    }

    @Override
    public void onActivityResult(int requestCode, int resultCode, Intent data) {
        switch (requestCode) {
            case REQUEST_PROVISION_MANAGED_PROFILE:
            case REQUEST_PROVISION_DEVICE_OWNER:
                if (resultCode == Activity.RESULT_OK) {
                    // Success, finish the enclosing activity. NOTE: Only finish once we're done
                    // here, as in synchronous auth cases we don't want the user to return to the
                    // Android setup wizard or add-account flow prematurely.
                    setResult(Activity.RESULT_OK);
                    finish();
                } else {
                    // Something went wrong (either provisioning failed, or the user backed out).
                    // Let the user decide how to proceed.
                    Toast.makeText(this, R.string.provisioning_failed_or_cancelled,
                            Toast.LENGTH_SHORT).show();
                }
                break;
        }
    }

    @Override
    public void onNavigateBack() {
        onBackPressed();
    }

    @Override
    public void onNavigateNext() {
        RadioGroup setupOptions = (RadioGroup) findViewById(R.id.setup_options);
        if (setupOptions.getCheckedRadioButtonId() == R.id.setup_managed_profile) {
            provisionManagedProfile();
        } else {
            provisionDeviceOwner();
        }
    }
}
