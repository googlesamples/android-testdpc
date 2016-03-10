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

import android.app.Activity;
import android.content.pm.ApplicationInfo;
import android.content.pm.PackageManager;
import android.os.Bundle;
import android.util.Log;
import android.view.View;
import android.widget.ImageView;
import android.widget.TextView;

import com.afwsamples.testdpc.common.LaunchIntentUtil;
import com.android.setupwizardlib.SetupWizardLayout;
import com.android.setupwizardlib.view.NavigationBar;

/**
 * This activity is started after device owner provisioning is complete in
 * {@link DeviceAdminReceiver}.
 */
public class EnableDeviceOwnerActivity extends Activity
        implements NavigationBar.NavigationBarListener {

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

        setContentView(R.layout.enable_device_owner_activity);
        SetupWizardLayout layout = (SetupWizardLayout) findViewById(R.id.setup_wizard_layout);
        NavigationBar navigationBar = layout.getNavigationBar();
        navigationBar.setNavigationBarListener(this);
        navigationBar.getNextButton().setText(R.string.finish_button);

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
            findViewById(R.id.managed_account_name_label).setVisibility(View.VISIBLE);

            TextView managedAccountName = (TextView) findViewById(R.id.managed_account_name);
            managedAccountName.setText(addedAccount);
            managedAccountName.setVisibility(View.VISIBLE);
        }
    }

    @Override
    public void onNavigateBack() {
        onBackPressed();
    }

    @Override
    public void onNavigateNext() {
        finish();
    }
}
