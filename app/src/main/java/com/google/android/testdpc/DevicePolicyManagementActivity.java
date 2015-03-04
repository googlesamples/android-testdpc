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
import android.os.Bundle;

import com.google.android.testdpc.profileowner.ProfilePolicyManagementFragment;

/**
 * An entry activity which shows a setup profile fragment if the app is not a profile owner.
 * Otherwise, a profile policy management fragment is shown.
 */
public class DevicePolicyManagementActivity extends Activity {

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);

        DevicePolicyManager devicePolicyManager = (DevicePolicyManager) getSystemService(
                DEVICE_POLICY_SERVICE);

        if (savedInstanceState == null) {
            String packageName = getPackageName();
            if (devicePolicyManager.isProfileOwnerApp(packageName)) {
                getFragmentManager().beginTransaction()
                        .add(R.id.container, new ProfilePolicyManagementFragment())
                        .commit();
            } else {
                getFragmentManager().beginTransaction()
                        .add(R.id.container, new SetupProfileFragment())
                        .commit();
            }
        }
    }
}
