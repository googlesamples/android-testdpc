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

package com.afwsamples.testdpc.policy.certificate;

import android.app.admin.DevicePolicyManager;
import android.content.Context;
import android.os.Bundle;

import com.afwsamples.testdpc.DeviceAdminReceiver;
import com.afwsamples.testdpc.R;
import com.afwsamples.testdpc.common.SelectAppFragment;

/**
 * This fragment provides functionalities related to delegated certificate installer.
 * These include
 * 1) {@link DevicePolicyManager#setCertInstallerPackage}
 * 2) {@link DevicePolicyManager#getCertInstallerPackage}
 */
public class DelegatedCertInstallerFragment extends SelectAppFragment {

    private DevicePolicyManager mDpm;

    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        mDpm = (DevicePolicyManager) getContext().getSystemService(Context.DEVICE_POLICY_SERVICE);
    }

    @Override
    public void onResume() {
        super.onResume();
        getActivity().getActionBar().setTitle(R.string.manage_cert_installer);
    }

    @Override
    protected void setSelectedPackage(String pkgName) {
        mDpm.setCertInstallerPackage(DeviceAdminReceiver.getComponentName(getActivity()), pkgName);
    }

    @Override
    protected void clearSelectedPackage() {
        mDpm.setCertInstallerPackage(DeviceAdminReceiver.getComponentName(getActivity()), null);
    }

    @Override
    protected String getSelectedPackage() {
        return mDpm.getCertInstallerPackage(DeviceAdminReceiver.getComponentName(getActivity()));
    }
}
