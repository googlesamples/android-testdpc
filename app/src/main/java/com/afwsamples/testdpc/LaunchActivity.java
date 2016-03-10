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
import android.content.Intent;
import android.os.Bundle;
import android.widget.Toast;

import com.afwsamples.testdpc.common.LaunchIntentUtil;
import com.afwsamples.testdpc.common.ProvisioningStateUtil;

/**
 * <p>Application launch activity that decides the most appropriate initial activity for the
 * user.
 *
 * <p>Options include:
 * <ol>
 *     <li>If TestDPC is already managing the device or profile, forward to the policy management
 *         activity.
 *     <li>If TestDPC was launched as part of synchronous authentication, forward all intent extras
 *         to the setup activities and wait for that activity to finish; allows in-line management
 *         setup immediately after an account is added (before the end of the Add Account or Setup
 *         Wizard flows).
 *     <li>Otherwise, present the non-sync-auth setup options.
 * </ol>
 */
public class LaunchActivity extends Activity {
    private static final int REQUEST_CODE_SYNC_AUTH = 1;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

        if (savedInstanceState != null) {
            // We should only forward on first time creation.
            finish();
            return;
        }

        Intent intent = new Intent(this, PolicyManagementActivity.class);
        if (ProvisioningStateUtil.isManagedByTestDPC(this)) {
            // Device or profile owner is enforced, allow the user to modify management policies.
            startActivity(intent);
            finish();
        } else if (ProvisioningStateUtil.isManaged(this)) {
            // Device or profile owner is a different app to TestDPC - abort.
            Toast.makeText(this, getString(R.string.other_owner_already_setup_error),
                    Toast.LENGTH_LONG).show();
            setResult(RESULT_CANCELED);
            finish();
        } else if (LaunchIntentUtil.isSynchronousAuthLaunch(getIntent())) {
            // Forward all extras from original launch intent.
            intent.putExtras(getIntent().getExtras());

            // For synchronous auth either Setup Wizard or Add Account will launch this activity
            // with startActivityForResult(), and continue the account/device setup flow once a
            // result is returned - so we need to wait for a result from any activities we launch
            // and return a result based upon the outcome of those activities to whichever activity
            // launched us.
            startActivityForResult(intent, REQUEST_CODE_SYNC_AUTH);
        } else {
            // Either a user launched us, or we're triggered from an NFC provisioning bump - go to
            // pre-M setup options.
            startActivity(intent);
            finish();
        }
    }

    @Override
    protected void onActivityResult(int requestCode, int resultCode, Intent data) {
        if (requestCode == REQUEST_CODE_SYNC_AUTH) {
            // Forward result of activity back to launching activity for sync-auth case.
            setResult(resultCode);
            finish();
        }
    }
}
