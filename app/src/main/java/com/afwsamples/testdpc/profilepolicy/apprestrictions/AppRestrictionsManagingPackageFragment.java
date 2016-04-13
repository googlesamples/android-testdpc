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

package com.afwsamples.testdpc.profilepolicy.apprestrictions;

import android.annotation.TargetApi;
import android.app.admin.DevicePolicyManager;
import android.content.Context;
import android.os.Build;
import android.os.Bundle;

import com.afwsamples.testdpc.DeviceAdminReceiver;
import com.afwsamples.testdpc.R;
import com.afwsamples.testdpc.common.SelectAppFragment;

import java.lang.IllegalArgumentException;

/**
 * This fragment lets the user select an app that can manage application restrictions for the
 * current user. Related APIs:
 * 1) {@link DevicePolicyManager#setApplicationRestrictionsManagingPackage}
 * 2) {@link DevicePolicyManager#getApplicationRestrictionsManagingPackage}
 * 3) {@link DevicePolicyManager#isCallerApplicationRestrictionsManagingPackage}
 */
@TargetApi(Build.VERSION_CODES.N)
public class AppRestrictionsManagingPackageFragment extends SelectAppFragment {

    private DevicePolicyManager mDpm;
    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        mDpm = (DevicePolicyManager) getContext().getSystemService(Context.DEVICE_POLICY_SERVICE);
    }

    @Override
    public void onResume() {
        super.onResume();
        getActivity().getActionBar().setTitle(R.string.app_restrictions_managing_package);
    }

    @Override
    protected void setSelectedPackage(String pkgName) {
        try {
            mDpm.setApplicationRestrictionsManagingPackage(
                    DeviceAdminReceiver.getComponentName(getActivity()), pkgName);
            // TODO: Catch NameNotFoundException instead when NYC SDK
            // setApplicationRestrictionsManagingPackage starts to throw NameNotFoundException
        } catch (Exception nnpe) {
            throw new IllegalArgumentException(nnpe);
        }
    }

    @Override
    protected void clearSelectedPackage() {
        setSelectedPackage(null);
    }

    @Override
    protected String getSelectedPackage() {
        return mDpm.getApplicationRestrictionsManagingPackage(
                DeviceAdminReceiver.getComponentName(getActivity()));
    }
}
