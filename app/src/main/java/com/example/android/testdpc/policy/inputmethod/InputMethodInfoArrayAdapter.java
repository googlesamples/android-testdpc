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

package com.example.android.testdpc.policy.inputmethod;

import android.content.Context;
import android.content.pm.ApplicationInfo;
import android.content.pm.ResolveInfo;
import android.graphics.drawable.Drawable;
import android.os.Build;
import android.text.TextUtils;
import android.view.inputmethod.InputMethodInfo;

import com.example.android.testdpc.DeviceAdminReceiver;
import com.example.android.testdpc.common.ToggleComponentsArrayAdapter;

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

    public ArrayList<String> getSelectedInputMethods() {
        ArrayList<String> selectedInputMethodsArrayList = new ArrayList<String>();
        int size = getCount();
        for (int i = 0; i < size; i++) {
            if (mIsComponentCheckedList.get(i)) {
                selectedInputMethodsArrayList.add(getItem(i).serviceInfo.packageName);
            }
        }
        return selectedInputMethodsArrayList;
    }

    @Override
    protected ApplicationInfo getApplicationInfo(int position) {
        return getItem(position).serviceInfo.applicationInfo;
    }

    @Override
    protected Drawable getApplicationIcon(ApplicationInfo applicationInfo) {
        // Input methods refer to the packages in primary profile. so, we
        // need to show them unbadged.
        // ApplicationInfo.loadUnbadgedIcon api is added in L-MR1, so can't get unbadged icon.
        if (Build.VERSION.SDK_INT <= Build.VERSION_CODES.LOLLIPOP) {
            return mPackageManager.getApplicationIcon(applicationInfo);
        } else {
            return applicationInfo.loadUnbadgedIcon(mPackageManager);
        }
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
