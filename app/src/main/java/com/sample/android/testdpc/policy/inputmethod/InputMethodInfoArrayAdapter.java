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

package com.sample.android.testdpc.policy.inputmethod;

import android.content.Context;
import android.content.pm.ApplicationInfo;
import android.content.pm.ResolveInfo;
import android.text.TextUtils;
import android.view.inputmethod.InputMethodInfo;

import com.sample.android.testdpc.DeviceAdminReceiver;
import com.sample.android.testdpc.common.ToggleComponentsArrayAdapter;

import java.util.ArrayList;
import java.util.List;

/**
 * Displays a list of installed input methods with a checkbox for enabling the component. All system
 * input methods are enabled by default and can't be disabled.
 */
public class InputMethodInfoArrayAdapter extends ToggleComponentsArrayAdapter {

    private List<String> mPermittedInputType = null;

    public InputMethodInfoArrayAdapter(Context context, int resource, List<ResolveInfo> objects) {
        super(context, resource, objects);
    }

    public static List<ResolveInfo> getResolveInfoListFromInputMethodsInfoList(
            List<InputMethodInfo> inputMethodsInfoList) {
        List<ResolveInfo> inputMethodsResolveInfoList = new ArrayList<ResolveInfo>();
        for (InputMethodInfo inputMethodInfo: inputMethodsInfoList) {
            ResolveInfo resolveInfo = new ResolveInfo();
            resolveInfo.serviceInfo = inputMethodInfo.getServiceInfo();
            resolveInfo.resolvePackageName = inputMethodInfo.getPackageName();
            inputMethodsResolveInfoList.add(resolveInfo);
        }
        return inputMethodsResolveInfoList;
    }

    /**
     * There are three cases where a given input method in a profile is enabled.
     * 1) There is no restriction for the permitted input methods.
     * 2) A given input method's package name exist in the permitted input method list.
     * 3) A given input method is a system input method.
     */
    @Override
    public boolean isComponentEnabled(ResolveInfo resolveInfo) {
        if (resolveInfo != null && resolveInfo.serviceInfo != null && !TextUtils.isEmpty(
                resolveInfo.serviceInfo.packageName)) {
            if (mPermittedInputType == null || isSystemApp(
                    resolveInfo.serviceInfo.applicationInfo)) {
                return true;
            } else {
                return mPermittedInputType.contains(resolveInfo.serviceInfo.packageName);
            }
        }
        return false;
    }

    @Override
    protected boolean canModifyComponent(int position) {
        // System input methods are always enabled.
        return !isSystemApp(getApplicationInfo(position));
    }

    public ArrayList<String> getPermittedAccessibilityServices() {
        ArrayList<String> permittedAccessibilityServicesArrayList = new ArrayList<String>();
        int size = getCount();
        for (int i = 0; i < size; i++) {
            if (mIsComponentCheckedList.get(i)) {
                permittedAccessibilityServicesArrayList.add(getItem(i).serviceInfo.packageName);
            }
        }
        return permittedAccessibilityServicesArrayList;
    }

    @Override
    protected ApplicationInfo getApplicationInfo(int position) {
        return getItem(position).serviceInfo.applicationInfo;
    }

    @Override
    protected void initIsComponentEnabledList() {
        mPermittedInputType = mDevicePolicyManager.getPermittedInputMethods(
                DeviceAdminReceiver.getComponentName(getContext()));
        int size = getCount();
        for (int i = 0; i < size; i++) {
            mIsComponentCheckedList.add(isComponentEnabled(getItem(i)));
        }
    }

    @Override
    public CharSequence getDisplayName(int position) {
        return getItem(position).loadLabel(mPackageManager);
    }
}
