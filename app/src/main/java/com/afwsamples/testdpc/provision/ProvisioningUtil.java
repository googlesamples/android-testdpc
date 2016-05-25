/*
 * Copyright (C) 2016 The Android Open Source Project
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

package com.afwsamples.testdpc.provision;

import android.app.admin.DevicePolicyManager;
import android.content.ComponentName;
import android.content.Context;

import com.afwsamples.testdpc.DeviceAdminReceiver;
import com.afwsamples.testdpc.R;

public class ProvisioningUtil {
    public static void enableProfile(Context context) {
        DevicePolicyManager manager = (DevicePolicyManager) context.getSystemService(
                Context.DEVICE_POLICY_SERVICE);
        ComponentName componentName = DeviceAdminReceiver.getComponentName(context);
        // This is the name for the newly created managed profile.
        manager.setProfileName(componentName, context.getString(R.string.profile_name));
        // We enable the profile here.
        manager.setProfileEnabled(componentName);
    }
}
