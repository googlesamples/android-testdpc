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

package com.afwsamples.testdpc.policy;

import android.app.Activity;
import android.content.Intent;
import android.os.Bundle;
import android.view.View;
import com.afwsamples.testdpc.R;
import com.android.setupwizardlib.GlifLayout;

/**
 * Activity that gets launched by the
 * {@link android.app.admin.DevicePolicyManager#ACTION_DPC_POLICY_COMPLIANCE} intent.
 */
public class DpcEnforcePoliciesActivity extends Activity {

    @Override
    public void onCreate(Bundle icicle) {
        super.onCreate(icicle);
        setContentView(R.layout.activity_dpc_policy_compliance);
        GlifLayout layout = findViewById(R.id.dpc_policy_compliance);
        layout.findViewById(R.id.suw_navbar_next).setOnClickListener(nextButton -> {
            setResult(RESULT_OK);
            finish();
        });
    }

    @Override
    public void onBackPressed() {
        setResult(RESULT_CANCELED);
        super.onBackPressed();
    }
}
