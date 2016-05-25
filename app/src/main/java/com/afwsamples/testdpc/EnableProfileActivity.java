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

import android.accounts.Account;
import android.accounts.AccountManager;
import android.app.Activity;
import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.content.IntentFilter;
import android.content.pm.ApplicationInfo;
import android.content.pm.PackageManager;
import android.os.Bundle;
import android.support.v4.content.LocalBroadcastManager;
import android.util.Log;
import android.view.View;
import android.widget.Button;
import android.widget.ImageView;
import android.widget.TextView;

import com.afwsamples.testdpc.common.LaunchIntentUtil;
import com.afwsamples.testdpc.provision.CheckInState;
import com.afwsamples.testdpc.provision.ProvisioningUtil;
import com.android.setupwizardlib.SetupWizardLayout;
import com.android.setupwizardlib.view.NavigationBar;

import static com.afwsamples.testdpc.provision.CheckInState.FIRST_ACCOUNT_READY_PROCESSED_ACTION;

/**
 * This activity is started after managed profile provisioning is complete in
 * {@link DeviceAdminReceiver}. There could be two cases:
 * 1. If we are not going to add account now, we will then enable profile immediately.
 * 2. If we have just added account, we need to wait for the FIRST_ACCOUNT_READY broadcast before
 *    enabling the profile. The broadcast indicates that the account has been synced with Google
 *    and is ready for use.
 */
public class EnableProfileActivity extends Activity implements NavigationBar.NavigationBarListener {
    private CheckInStateReceiver mCheckInStateReceiver;
    private Button mFinishButton;
    private SetupWizardLayout mSetupWizardLayout;

    private boolean mEnableProfileNow;

    public static final String EXTRA_ENABLE_PROFILE_NOW = "enable_profile_now";
    private static final IntentFilter sIntentFilter =
            new IntentFilter(FIRST_ACCOUNT_READY_PROCESSED_ACTION);

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        mEnableProfileNow = getIntent().getBooleanExtra(EXTRA_ENABLE_PROFILE_NOW, false);
        if (savedInstanceState == null && mEnableProfileNow) {
            ProvisioningUtil.enableProfile(this);
        }
        setContentView(R.layout.enable_profile_activity);
        mSetupWizardLayout = (SetupWizardLayout) findViewById(R.id.setup_wizard_layout);
        NavigationBar navigationBar = mSetupWizardLayout.getNavigationBar();
        navigationBar.getBackButton().setEnabled(false);
        navigationBar.setNavigationBarListener(this);
        mFinishButton = navigationBar.getNextButton();
        mFinishButton.setText(R.string.finish_button);

        mCheckInStateReceiver = new CheckInStateReceiver();

        // This is just a user friendly shortcut to the policy management screen of this app.
        ImageView appIcon = (ImageView) findViewById(R.id.app_icon);
        TextView appLabel = (TextView) findViewById(R.id.app_label);
        try {
            PackageManager packageManager = getPackageManager();
            ApplicationInfo applicationInfo = packageManager.getApplicationInfo(
                    getPackageName(), 0 /* Default flags */);
            appIcon.setImageDrawable(packageManager.getApplicationIcon(applicationInfo));
            appLabel.setText(packageManager.getApplicationLabel(applicationInfo));
        } catch (PackageManager.NameNotFoundException e) {
            Log.w("TestDPC", "Couldn't look up our own package?!?!", e);
        }

        // Show the user which account now has management, if specified.
        String addedAccount = getIntent().getStringExtra(LaunchIntentUtil.EXTRA_ACCOUNT_NAME);
        if (addedAccount != null) {
            View accountMigrationStatusLayout;
            if (isAccountMigrated(addedAccount)) {
                accountMigrationStatusLayout = findViewById(R.id.account_migration_success);
            } else {
                accountMigrationStatusLayout = findViewById(R.id.account_migration_fail);
            }
            accountMigrationStatusLayout.setVisibility(View.VISIBLE);
            TextView managedAccountName = (TextView) accountMigrationStatusLayout.findViewById(
                    R.id.managed_account_name);
            managedAccountName.setText(addedAccount);
        }
    }

    @Override
    protected void onResume() {
        super.onResume();
        LocalBroadcastManager.getInstance(this).registerReceiver(mCheckInStateReceiver,
                sIntentFilter);
        // In case the broadcast is sent before we register the receiver.
        CheckInState checkInState = new CheckInState(this);
        refreshUi(mEnableProfileNow || checkInState.isFirstAccountReady() /* enableFinish */);
    }

    @Override
    protected void onStop() {
        super.onStop();
        LocalBroadcastManager.getInstance(this).unregisterReceiver(mCheckInStateReceiver);
    }

    private boolean isAccountMigrated(String addedAccount) {
        Account[] accounts = AccountManager.get(this).getAccounts();
        for (Account account : accounts) {
            if (addedAccount.equalsIgnoreCase(account.name)) {
                return true;
            }
        }
        return false;
    }

    private void refreshUi(boolean enableFinish) {
        if (enableFinish) {
            mSetupWizardLayout.hideProgressBar();
        } else {
            mSetupWizardLayout.showProgressBar();
        }
        mSetupWizardLayout.setHeaderText(
                (enableFinish)
                        ? R.string.finish_setup
                        : R.string.waiting_for_first_account_check_in);
        mFinishButton.setEnabled(enableFinish);
    }

    @Override
    public void onNavigateBack() {
        onBackPressed();
    }

    @Override
    public void onNavigateNext() {
        finish();
    }

    class CheckInStateReceiver extends BroadcastReceiver {
        @Override
        public void onReceive(Context context, Intent intent) {
            // Processed the first check-in broadcast, allow user to tap the finish button.
            refreshUi(true);
        }
    }
}
