/*
 * Copyright (C) 2019 The Android Open Source Project
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

import static android.app.admin.DevicePolicyManager.EXTRA_PROVISIONING_MODE;
import static android.app.admin.DevicePolicyManager.PROVISIONING_MODE_FULLY_MANAGED_DEVICE;
import static android.app.admin.DevicePolicyManager.PROVISIONING_MODE_MANAGED_PROFILE;

import android.app.Activity;
import android.content.Intent;
import android.os.Bundle;
import android.view.View;
import android.widget.RadioGroup;
import com.afwsamples.testdpc.R;
import com.android.setupwizardlib.GlifLayout;

/**
 * Activity that gets launched by the
 * {@link android.app.admin.DevicePolicyManager#ACTION_GET_PROVISIONING_MODE} intent.
 */
public class DpcLoginActivity extends Activity {

    @Override
    public void onCreate(Bundle icicle) {
        super.onCreate(icicle);
        setContentView(R.layout.activity_dpc_login);
        GlifLayout layout = findViewById(R.id.dpc_login);
        layout.findViewById(R.id.suw_navbar_next).setOnClickListener(this::onNavigateNext);
    }

    @Override
    public void onBackPressed() {
        setResult(RESULT_CANCELED);
        super.onBackPressed();
    }

    private void onNavigateNext(View nextButton) {
        final Intent intent = new Intent();
        RadioGroup dpcLoginOptions = findViewById(R.id.dpc_login_options);
        switch (dpcLoginOptions.getCheckedRadioButtonId()) {
            case R.id.dpc_login_do:
                intent.putExtra(EXTRA_PROVISIONING_MODE, PROVISIONING_MODE_FULLY_MANAGED_DEVICE);
                finishWithIntent(intent);
                return;
            case R.id.dpc_login_po:
                intent.putExtra(EXTRA_PROVISIONING_MODE, PROVISIONING_MODE_MANAGED_PROFILE);
                finishWithIntent(intent);
                return;
            default:
                finish();
        }
    }

    private void finishWithIntent(Intent intent) {
        setResult(RESULT_OK, intent);
        finish();
    }
}
