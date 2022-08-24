/*
 * Copyright (C) 2020 The Android Open Source Project
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

import android.annotation.TargetApi;
import android.app.Fragment;
import android.app.admin.DevicePolicyManager;
import android.content.ComponentName;
import android.os.Build.VERSION_CODES;
import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Button;
import android.widget.EditText;
import android.widget.TextView;
import com.google.common.base.Joiner;
import java.util.Collections;
import java.util.Locale;
import java.util.Set;

/**
 * This fragment provides the ability to allow-list cross profile packages
 *
 * <p>APIs exercised:
 *
 * <ul>
 *   <li>{@link DevicePolicyManager#setCrossProfilePackages(ComponentName, Set)}
 *   <li>{@link DevicePolicyManager#getCrossProfilePackages(ComponentName)}
 * </ul>
 */
@TargetApi(VERSION_CODES.R)
public class CrossProfileAppsAllowlistFragment extends Fragment {
  private static final String DELIMITER = "\n";

  private View mInflatedView;
  private EditText mAppNameEditText;
  private Button mResetButton;
  private Button mAddButton;
  private Button mRemoveButton;
  private TextView mAppsList;

  private DevicePolicyManager mDevicePolicyManager;
  private ComponentName mAdminComponent;

  @Override
  public View onCreateView(
      LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState) {
    mDevicePolicyManager = getActivity().getSystemService(DevicePolicyManager.class);
    mAdminComponent = DeviceAdminReceiver.getComponentName(getActivity());
    mInflatedView = inflater.inflate(R.layout.cross_profile_apps_allowlist, container, false);

    mAppNameEditText = mInflatedView.findViewById(R.id.cross_profile_app_allowlist_input);
    mResetButton = mInflatedView.findViewById(R.id.cross_profile_app_allowlist_reset_button);
    mAddButton = mInflatedView.findViewById(R.id.cross_profile_app_allowlist_add_button);
    mRemoveButton = mInflatedView.findViewById(R.id.cross_profile_app_allowlist_remove_button);
    mAppsList = mInflatedView.findViewById(R.id.cross_profile_app_list);

    setOnClickListeners();
    updateCrossProfileAppsList();

    return mInflatedView;
  }

  private void setOnClickListeners() {
    mResetButton.setOnClickListener(view -> resetApps());
    mAddButton.setOnClickListener(
        view ->
            addApp(mAppNameEditText.getText().toString().toLowerCase(Locale.getDefault()).trim()));
    mRemoveButton.setOnClickListener(
        view ->
            removeApp(
                mAppNameEditText.getText().toString().toLowerCase(Locale.getDefault()).trim()));
  }

  private void resetApps() {
    mDevicePolicyManager.setCrossProfilePackages(mAdminComponent, Collections.emptySet());
    updateCrossProfileAppsList();
  }

  private void addApp(String app) {
    Set<String> currentApps = mDevicePolicyManager.getCrossProfilePackages(mAdminComponent);
    currentApps.add(app);
    mDevicePolicyManager.setCrossProfilePackages(mAdminComponent, currentApps);
    updateCrossProfileAppsList();
  }

  private void removeApp(String app) {
    Set<String> currentApps = mDevicePolicyManager.getCrossProfilePackages(mAdminComponent);
    currentApps.remove(app);
    mDevicePolicyManager.setCrossProfilePackages(mAdminComponent, currentApps);
    updateCrossProfileAppsList();
  }

  private void updateCrossProfileAppsList() {
    Set<String> currentApps = mDevicePolicyManager.getCrossProfilePackages(mAdminComponent);
    if (currentApps.isEmpty()) {
      mAppsList.setText(R.string.cross_profile_apps_no_allowlisted_apps);
    } else {
      mAppsList.setText(Joiner.on(DELIMITER).join(currentApps));
    }
  }
}
