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

import static android.app.admin.DevicePolicyManager.EXTRA_PROVISIONING_ALLOWED_PROVISIONING_MODES;
import static android.app.admin.DevicePolicyManager.EXTRA_PROVISIONING_MODE;
import static android.app.admin.DevicePolicyManager.PROVISIONING_MODE_FULLY_MANAGED_DEVICE;
import static android.app.admin.DevicePolicyManager.PROVISIONING_MODE_MANAGED_PROFILE;
import static android.app.admin.DevicePolicyManager.PROVISIONING_MODE_MANAGED_PROFILE_ON_PERSONAL_DEVICE;

import android.annotation.SuppressLint;
import android.app.Activity;
import android.content.Intent;
import android.os.Bundle;
import android.util.Log;
import android.view.View;
import android.view.ViewGroup;
import android.widget.LinearLayout;
import com.afwsamples.testdpc.R;
import com.afwsamples.testdpc.common.Util;

import java.util.ArrayList;

/**
 * Activity that gets launched by the {@link
 * android.app.admin.DevicePolicyManager#ACTION_GET_PROVISIONING_MODE} intent.
 */
@SuppressLint("NewApi")
public class GetProvisioningModeActivity extends Activity {

  private static final String TAG = GetProvisioningModeActivity.class.getSimpleName();

  @Override
  public void onCreate(Bundle icicle) {
    super.onCreate(icicle);

    if (ProvisioningUtil.isAutoProvisioningDeviceOwnerMode()) {
        Log.i(TAG, "Automatically provisioning device onwer");
        onDoButtonClick(null);
    }

    setContentView(R.layout.activity_get_provisioning_mode);
    final LinearLayout layout = findViewById(R.id.dpc_login);
    showRelevantProvisioningOptions(layout);
  }

  @Override
  public void onBackPressed() {
    setResult(RESULT_CANCELED);
    super.onBackPressed();
  }

  private void showRelevantProvisioningOptions(ViewGroup container) {
    hideAllOptions(container);
    hideDivider(container);
    ArrayList<Integer> allowedProvisioningModes = getAllowedProvisioningModes();
    if (containsDoOption(allowedProvisioningModes)) {
      showDoOption(container);
    }
    if (containsPoOption(allowedProvisioningModes)) {
      if (containsDoOption(allowedProvisioningModes)) {
        showDivider(container);
      }
      showPoOption(container);
    }
  }

  private ArrayList<Integer> getAllowedProvisioningModes() {
    ArrayList<Integer> allowedProvisioningModes =
        getIntent().getIntegerArrayListExtra(EXTRA_PROVISIONING_ALLOWED_PROVISIONING_MODES);
    if (allowedProvisioningModes == null || allowedProvisioningModes.isEmpty()) {
      allowedProvisioningModes = new ArrayList<>();
      allowedProvisioningModes.add(PROVISIONING_MODE_MANAGED_PROFILE);
      allowedProvisioningModes.add(PROVISIONING_MODE_FULLY_MANAGED_DEVICE);
    }
    return allowedProvisioningModes;
  }

  private boolean containsDoOption(ArrayList<Integer> allowedProvisioningModes) {
    return allowedProvisioningModes.contains(PROVISIONING_MODE_FULLY_MANAGED_DEVICE);
  }

  private boolean containsPoOption(ArrayList<Integer> allowedProvisioningModes) {
    return allowedProvisioningModes.contains(PROVISIONING_MODE_MANAGED_PROFILE)
        || allowedProvisioningModes.contains(PROVISIONING_MODE_MANAGED_PROFILE_ON_PERSONAL_DEVICE);
  }

  private void hideAllOptions(ViewGroup container) {
    container.findViewById(R.id.po_option).setVisibility(View.GONE);
    container.findViewById(R.id.do_option).setVisibility(View.GONE);
  }

  private void hideDivider(ViewGroup container) {
    container.findViewById(R.id.divider).setVisibility(View.GONE);
  }

  private void showPoOption(ViewGroup container) {
    container.findViewById(R.id.po_option).setVisibility(View.VISIBLE);
    container.findViewById(R.id.po_selection_button).setOnClickListener(this::onPoButtonClick);
  }

  private void showDoOption(ViewGroup container) {
    container.findViewById(R.id.do_option).setVisibility(View.VISIBLE);
    container.findViewById(R.id.do_selection_button).setOnClickListener(this::onDoButtonClick);
  }

  private void showDivider(ViewGroup container) {
    container.findViewById(R.id.divider).setVisibility(View.VISIBLE);
  }

  private void onDoButtonClick(View button) {
    final Intent intent = new Intent();
    intent.putExtra(EXTRA_PROVISIONING_MODE, PROVISIONING_MODE_FULLY_MANAGED_DEVICE);
    finishWithIntent(intent);
  }

  private void onPoButtonClick(View button) {
    final Intent intent = new Intent();
    intent.putExtra(EXTRA_PROVISIONING_MODE, PROVISIONING_MODE_MANAGED_PROFILE);
    finishWithIntent(intent);
  }

  private void finishWithIntent(Intent intent) {
    setResult(RESULT_OK, intent);
    finish();
  }
}
