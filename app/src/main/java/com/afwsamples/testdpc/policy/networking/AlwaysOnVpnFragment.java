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
import android.content.pm.PackageManager;
import android.os.Build;
import android.os.Bundle;

import com.afwsamples.testdpc.DeviceAdminReceiver;
import com.afwsamples.testdpc.R;
import com.afwsamples.testdpc.common.SelectAppFragment;

/**
 * This fragment provides a setting for always-on VPN apps.
 *
 * <p>APIs exercised:
 * <ul>
 * <li> {@link DevicePolicyManager#setAlwaysOnVpnPackage} </li>
 * <li> {@link DevicePolicyManager#getAlwaysOnVpnPackage} </li>
 * </ul>
 */
@TargetApi(Build.VERSION_CODES.N)
public class AlwaysOnVpnFragment extends SelectAppFragment {

    private DevicePolicyManager mDpm;

    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        mDpm = (DevicePolicyManager) getContext().getSystemService(Context.DEVICE_POLICY_SERVICE);
    }

    @Override
    public void onResume() {
        super.onResume();
        getActivity().getActionBar().setTitle(R.string.set_always_on_vpn);
    }

    @Override
    protected void setSelectedPackage(String pkg) {
        try {
            final ComponentName who = DeviceAdminReceiver.getComponentName(getActivity());
            mDpm.setAlwaysOnVpnPackage(who, pkg, /* lockdownEnabled */ true);
        } catch (PackageManager.NameNotFoundException | UnsupportedOperationException e) {
            if (pkg != null) {
                clearSelectedPackage();
            }
        }
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
