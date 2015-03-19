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

package com.google.android.testdpc;

import android.app.Activity;
import android.app.admin.DevicePolicyManager;
import android.content.ComponentName;
import android.content.Context;
import android.content.Intent;
import android.content.pm.ApplicationInfo;
import android.content.pm.PackageManager;
import android.os.Bundle;
import android.view.View;
import android.widget.ImageView;

/**
 * This activity is started after provisioning is complete in {@link DeviceAdminReceiver}.
 * It is responsible for enabling the managed profile and providing a shortcut to the policy
 * management screen.
 */
public class EnableProfileActivity extends Activity implements View.OnClickListener {

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
        setContentView(R.layout.activity_setup);
        ImageView appIcon = (ImageView) findViewById(R.id.app_icon);
        try {
            ApplicationInfo applicationInfo = getPackageManager()
                    .getApplicationInfo(getPackageName(), 0 /* Default flags */);
            appIcon.setImageDrawable(getPackageManager().getApplicationIcon(applicationInfo));
        } catch (PackageManager.NameNotFoundException e) {
            e.printStackTrace();
        }
        appIcon.setOnClickListener(this);
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
    public void onClick(View view) {
        switch (view.getId()) {
            case R.id.app_icon: {
                // Opens up the main screen
                startActivity(new Intent(this, PolicyManagementActivity.class));
                finish();
                break;
            }
        }
    }

}
