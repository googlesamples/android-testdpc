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
import android.os.Bundle;

import com.sample.android.testdpc.policy.PolicyManagementFragment;

/**
 * An entry activity that shows a profile setup fragment if the app is not a profile or device
 * owner. Otherwise, a policy management fragment is shown.
 */
public class PolicyManagementActivity extends Activity {

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);

        if (savedInstanceState == null) {
            DevicePolicyManager devicePolicyManager = (DevicePolicyManager) getSystemService(
                    DEVICE_POLICY_SERVICE);

            String packageName = getPackageName();
            if (devicePolicyManager.isProfileOwnerApp(packageName)
                    || devicePolicyManager.isDeviceOwnerApp(packageName)) {
                getFragmentManager().beginTransaction().add(R.id.container,
                        new PolicyManagementFragment()).commit();
            } else {
                getFragmentManager().beginTransaction().add(R.id.container,
                        new SetupManagementFragment()).commit();
            }
        }
    }
}
