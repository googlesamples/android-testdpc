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

import android.app.Activity;
import android.app.admin.DevicePolicyManager;
import android.content.ComponentName;
import android.content.Context;
import android.content.pm.ApplicationInfo;
import android.content.pm.PackageManager;
import android.os.Bundle;
import android.util.Log;
import android.widget.ImageView;
import android.widget.TextView;

import com.android.setupwizardlib.SetupWizardLayout;
import com.android.setupwizardlib.view.NavigationBar;

/**
 * This activity is started after managed profile provisioning is complete in
 * {@link DeviceAdminReceiver}. It is responsible for enabling the managed profile and providing a
 * shortcut to the policy management screen.
 */
public class EnableProfileActivity extends Activity implements NavigationBar.NavigationBarListener {

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        // Don't enable the profile again if this activity is being re-initialized.
        if (null == savedInstanceState) {
            // Important: After the profile has been created, the MDM must enable it for corporate
            // apps to become visible in the launcher.
            enableProfile();
        }

        // This is just a user friendly shortcut to the policy management screen of this app.
        setContentView(R.layout.enable_profile_activity);
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
    }

    private void enableProfile() {
        DevicePolicyManager manager = (DevicePolicyManager) getSystemService(
                Context.DEVICE_POLICY_SERVICE);
        ComponentName componentName = DeviceAdminReceiver.getComponentName(this);
        // This is the name for the newly created managed profile.
        manager.setProfileName(componentName, getString(R.string.profile_name));
        // We enable the profile here.
        manager.setProfileEnabled(componentName);
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
