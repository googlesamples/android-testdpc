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

package com.afwsamples.testdpc.policy.networking;

import android.annotation.TargetApi;
import android.app.admin.DevicePolicyManager;
import android.content.ComponentName;
import android.content.Context;
import android.content.Intent;
import android.content.pm.PackageManager;
import android.content.pm.ResolveInfo;
import android.net.VpnService;
import android.os.Build.VERSION_CODES;
import android.os.Bundle;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.CheckBox;
import android.widget.EditText;
import android.widget.Toast;
import com.afwsamples.testdpc.DeviceAdminReceiver;
import com.afwsamples.testdpc.R;
import com.afwsamples.testdpc.common.SelectAppFragment;
import com.afwsamples.testdpc.common.Util;
import com.google.common.base.Joiner;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.HashSet;
import java.util.List;
import java.util.Set;
import java.util.stream.Collectors;

/**
 * This fragment provides a setting for always-on VPN apps.
 *
 * <p>APIs exercised:
 *
 * <ul>
 *   <li>{@link DevicePolicyManager#setAlwaysOnVpnPackage}
 *   <li>{@link DevicePolicyManager#getAlwaysOnVpnPackage}
 * </ul>
 */
@TargetApi(VERSION_CODES.N)
public class AlwaysOnVpnFragment extends SelectAppFragment {
  private static final String TAG = "AlwaysOnVpnFragment";

  private DevicePolicyManager mDpm;

  private CheckBox mLockdown;
  private EditText mExemptedPackages;

  private static final Intent VPN_INTENT = new Intent(VpnService.SERVICE_INTERFACE);
  private ComponentName mWho;

  @Override
  public void onCreate(Bundle savedInstanceState) {
    super.onCreate(savedInstanceState);
    mDpm = (DevicePolicyManager) getContext().getSystemService(Context.DEVICE_POLICY_SERVICE);
    mWho = DeviceAdminReceiver.getComponentName(getActivity());
  }

  @Override
  public void onResume() {
    super.onResume();
    getActivity().getActionBar().setTitle(R.string.set_always_on_vpn);
  }

  @Override
  public View onCreateView(
      LayoutInflater layoutInflater, ViewGroup container, Bundle savedInstanceState) {
    final View view = super.onCreateView(layoutInflater, container, savedInstanceState);

    if (Util.SDK_INT >= VERSION_CODES.Q) {
      final ViewGroup extension = getExtensionLayout(view);
      extension.setVisibility(View.VISIBLE);
      layoutInflater.inflate(R.layout.lockdown_settings, extension);
      mLockdown = view.findViewById(R.id.enable_lockdown);
      mExemptedPackages = view.findViewById(R.id.exempted_packages);
      mLockdown.setOnCheckedChangeListener(
          (unused, checked) -> mExemptedPackages.setEnabled(checked));
    }
    return view;
  }

  @Override
  protected List<String> createAppList() {
    Set<String> apps = new HashSet<>();
    PackageManager pm = getActivity().getPackageManager();
    List<ResolveInfo> serviceInfos = pm.queryIntentServices(VPN_INTENT, 0);
    for (ResolveInfo serviceInfo : serviceInfos) {
      if (serviceInfo.serviceInfo == null) {
        continue;
      }
      apps.add(serviceInfo.serviceInfo.packageName);
    }
    return new ArrayList<>(apps);
  }

  @Override
  protected void reloadSelectedPackage() {
    super.reloadSelectedPackage();
    if (Util.SDK_INT >= VERSION_CODES.Q) {
      updateLockdown();
    }
  }

  @TargetApi(VERSION_CODES.Q)
  private void updateLockdown() {
    mLockdown.setChecked(mDpm.isAlwaysOnVpnLockdownEnabled(mWho));
    final Set<String> exemptedPackages = mDpm.getAlwaysOnVpnLockdownWhitelist(mWho);
    mExemptedPackages.setText(
        exemptedPackages != null ? Joiner.on(",").join(exemptedPackages) : "");
  }

  @Override
  protected void setSelectedPackage(String pkg) {
    try {
      if (Util.SDK_INT >= VERSION_CODES.Q) {
        setAlwaysOnVpnPackageQPlus(pkg);
      } else {
        mDpm.setAlwaysOnVpnPackage(mWho, pkg, /* lockdownEnabled */ true);
      }
    } catch (PackageManager.NameNotFoundException e) {
      final String text = "Package not found: " + e.getMessage();
      Toast.makeText(getActivity(), text, Toast.LENGTH_SHORT).show();
      Log.e(TAG, "setAlwaysOnVpnPackage:", e);
    } catch (UnsupportedOperationException e) {
      Toast.makeText(getActivity(), "App doesn't support always-on VPN", Toast.LENGTH_SHORT).show();
      Log.e(TAG, "setAlwaysOnVpnPackage:", e);
    }
  }

  @TargetApi(VERSION_CODES.Q)
  private void setAlwaysOnVpnPackageQPlus(String pkg) throws PackageManager.NameNotFoundException {
    final boolean lockdown = mLockdown.isChecked();
    final Set<String> packages =
        lockdown
            ? Arrays.stream(mExemptedPackages.getText().toString().split(","))
                .map(String::trim)
                .filter(s -> !s.isEmpty())
                .collect(Collectors.toSet())
            : null;
    mDpm.setAlwaysOnVpnPackage(mWho, pkg, lockdown, packages);
  }

  @Override
  protected void clearSelectedPackage() {
    setSelectedPackage(null);
  }

  @Override
  protected String getSelectedPackage() {
    return mDpm.getAlwaysOnVpnPackage(DeviceAdminReceiver.getComponentName(getActivity()));
  }
}
