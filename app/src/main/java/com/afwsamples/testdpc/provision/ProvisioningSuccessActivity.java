/*
 * Copyright (C) 2017 The Android Open Source Project
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

import android.app.Activity;
import android.content.Intent;
import android.os.Bundle;
import android.util.Log;
import android.widget.Toast;

import com.afwsamples.testdpc.R;

/**
 * Activity that gets launched by the
 * {@link android.app.admin.DevicePolicyManager#ACTION_PROVISIONING_SUCCESSFUL} intent.
 */
public class ProvisioningSuccessActivity extends Activity {
    private static final String TAG = "ProvisioningSuccess";

    @Override
    public void onCreate(Bundle icicle) {
        super.onCreate(icicle);
        Log.e(TAG, "ProvisioningSuccessActivity.onCreate() recieved ");

        PostProvisioningTask task = new PostProvisioningTask(this);
        if (!task.performPostProvisioningOperations(getIntent())) {
            finish();
            return;
        }

        Intent launchIntent = task.getPostProvisioningLaunchIntent(getIntent());
        if (launchIntent != null) {
            startActivity(launchIntent);
        } else {
            Log.e(TAG, "ProvisioningSuccessActivity.onCreate() invoked, but ownership "
                    + "not assigned");
            Toast.makeText(this, R.string.device_admin_receiver_failure, Toast.LENGTH_LONG)
                    .show();
        }
        finish();
    }
}
