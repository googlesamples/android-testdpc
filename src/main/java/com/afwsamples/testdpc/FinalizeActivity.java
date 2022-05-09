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

import static android.app.admin.DevicePolicyManager.EXTRA_PROVISIONING_ADMIN_EXTRAS_BUNDLE;

import android.accounts.Account;
import android.accounts.AccountManager;
import android.app.Activity;
import android.content.pm.ApplicationInfo;
import android.content.pm.PackageManager;
import android.os.Bundle;
import android.util.Log;
import android.view.View;
import android.widget.ImageView;
import android.widget.TextView;
import com.afwsamples.testdpc.common.LaunchIntentUtil;
import com.afwsamples.testdpc.common.Util;
import com.afwsamples.testdpc.provision.ProvisioningUtil;
import com.google.android.setupcompat.template.FooterBarMixin;
import com.google.android.setupcompat.template.FooterButton;
import com.google.android.setupdesign.GlifLayout;

public class FinalizeActivity extends Activity {

  private static final String TAG = FinalizeActivity.class.getSimpleName();

  private GlifLayout mSetupWizardLayout;

  @Override
  protected void onCreate(Bundle savedInstanceState) {
    super.onCreate(savedInstanceState);

    if (savedInstanceState == null) {
      if (Util.isManagedProfileOwner(this)) {
        ProvisioningUtil.enableProfile(this);
      }
    }

    if (ProvisioningUtil.isAutoProvisioningDeviceOwnerMode()) {
        Log.i(TAG, "Automatically provisioning device onwer");
        onNavigateNext(null);
        return;
    }

    setContentView(R.layout.finalize_activity);
    mSetupWizardLayout = findViewById(R.id.setup_wizard_layout);
    FooterBarMixin mixin = mSetupWizardLayout.getMixin(FooterBarMixin.class);
    FooterButton finishButton =
        new FooterButton.Builder(this)
            .setText(R.string.finish_button)
            .setListener(this::onNavigateNext)
            .setButtonType(FooterButton.ButtonType.NEXT)
            .setTheme(R.style.SudGlifButton_Primary)
            .build();
    mixin.setPrimaryButton(finishButton);

    // This is just a user friendly shortcut to the policy management screen of this app.
    ImageView appIcon = findViewById(R.id.app_icon);
    TextView appLabel = findViewById(R.id.app_label);
    try {
      PackageManager packageManager = getPackageManager();
      ApplicationInfo applicationInfo =
          packageManager.getApplicationInfo(getPackageName(), 0 /* Default flags */);
      appIcon.setImageDrawable(packageManager.getApplicationIcon(applicationInfo));
      appLabel.setText(packageManager.getApplicationLabel(applicationInfo));
    } catch (PackageManager.NameNotFoundException e) {
      Log.w("TestDPC", "Couldn't look up our own package?!?!", e);
    }

    // Show the user which account now has management, if specified.
    final String addedAccount = getAddedAccountName();
    if (addedAccount != null) {
      View accountMigrationStatusLayout;
      if (isAccountMigrated(addedAccount)) {
        accountMigrationStatusLayout = findViewById(R.id.account_migration_success);
      } else {
        accountMigrationStatusLayout = findViewById(R.id.account_migration_fail);
      }
      accountMigrationStatusLayout.setVisibility(View.VISIBLE);
      TextView managedAccountName =
          (TextView) accountMigrationStatusLayout.findViewById(R.id.managed_account_name);
      managedAccountName.setText(addedAccount);
    }

    ((TextView) findViewById(R.id.explanation))
        .setText(
            Util.isDeviceOwner(this)
                ? R.string.all_done_explanation_device_owner
                : R.string.all_done_explanation_profile_owner);
  }

  private String getAddedAccountName() {
    String addedAccount = getIntent().getStringExtra(LaunchIntentUtil.EXTRA_ACCOUNT_NAME);
    // Added account infomation may be contained in EXTRA_PROVISIONING_ADMIN_EXTRAS_BUNDLE
    // if FinalizeActivity is started from ACTION_ADMIN_POLICY_COMPLIANCE.
    if (addedAccount == null) {
      addedAccount =
          LaunchIntentUtil.getAddedAccountName(
              getIntent().getParcelableExtra(EXTRA_PROVISIONING_ADMIN_EXTRAS_BUNDLE));
    }
    return addedAccount;
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

  public void onNavigateNext(View nextButton) {
    setResult(RESULT_OK);
    finish();
  }
}
