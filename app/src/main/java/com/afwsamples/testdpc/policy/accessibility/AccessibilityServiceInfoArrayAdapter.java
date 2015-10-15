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

package com.afwsamples.testdpc.policy.accessibility;

import android.accessibilityservice.AccessibilityServiceInfo;
import android.content.Context;
import android.content.pm.ApplicationInfo;
import android.content.pm.ResolveInfo;
import android.graphics.drawable.Drawable;
import android.os.Build;
import android.text.TextUtils;

import com.afwsamples.testdpc.DeviceAdminReceiver;
import com.afwsamples.testdpc.common.ToggleComponentsArrayAdapter;

import java.util.ArrayList;
import java.util.HashSet;
import java.util.List;

/**
 * Displays a list of installed accessibility services with a checkbox for enabling the component.
 * All system accessibility services are enabled by default and can't be disabled.
 */
public class AccessibilityServiceInfoArrayAdapter extends ToggleComponentsArrayAdapter {

    private List<String> mPermittedAccessibilityServices = null;

    private boolean mIsInitialized = false;

    public AccessibilityServiceInfoArrayAdapter(Context context, int resource,
            List<ResolveInfo> objects) {
        super(context, resource, objects);
    }

    /**
     * @return The package names of accessibility services which are selected in the array adapter
     * UI.
     */
    public ArrayList<String> getSelectedAccessibilityServices() {
        ArrayList<String> permittedAccessibilityServicesArrayList = new ArrayList<String>();
        int size = getCount();
        for (int i = 0; i < size; i++) {
            if (mIsComponentCheckedList.get(i)) {
                permittedAccessibilityServicesArrayList.add(getItem(i).serviceInfo.packageName);
            }
        }
        return permittedAccessibilityServicesArrayList;
    }

    public static List<ResolveInfo> getResolveInfoListFromAccessibilityServiceInfoList(
            List<AccessibilityServiceInfo> accessibilityServiceInfoList) {
        HashSet<String> packageSet = new HashSet<String>();
        List<ResolveInfo> resolveInfoList = new ArrayList<ResolveInfo>();
        for (AccessibilityServiceInfo accessibilityServiceInfo: accessibilityServiceInfoList) {
            ResolveInfo resolveInfo = accessibilityServiceInfo.getResolveInfo();
            // Some apps may contain multiple accessibility services. Make sure that the package
            // name is unique in the return list.
            if (!packageSet.contains(resolveInfo.serviceInfo.packageName)) {
                resolveInfoList.add(resolveInfo);
                packageSet.add(resolveInfo.serviceInfo.packageName);
            }
        }
        return resolveInfoList;
    }

    @Override
    protected ApplicationInfo getApplicationInfo(int position) {
        return getItem(position).serviceInfo.applicationInfo;
    }

    @Override
    protected Drawable getApplicationIcon(ApplicationInfo applicationInfo) {
        // Accessibility services refer to the packages in primary profile. so, we
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
        mPermittedAccessibilityServices = mDevicePolicyManager.getPermittedAccessibilityServices(
                DeviceAdminReceiver.getComponentName(getContext()));
        int size = getCount();
        for (int i = 0; i < size; i++) {
            mIsComponentCheckedList.add(isComponentEnabled(getItem(i)));
        }
    }

    /**
     * There are three cases where a given accessibility service in a profile is enabled.
     * 1) There are no restrictions for the permitted accessibility services.
     * 2) A given accessibility service's package name exists in the permitted accessibility
     * service list.
     * 3) A given accessibility service is a system app.
     */
    @Override
    protected boolean isComponentEnabled(ResolveInfo resolveInfo) {
        if (resolveInfo != null && resolveInfo.serviceInfo != null && !TextUtils
                .isEmpty(resolveInfo.serviceInfo.packageName)) {
           if (mPermittedAccessibilityServices == null || isSystemApp(
                    resolveInfo.serviceInfo.applicationInfo)) {
               // null means there are no restrictions. All accessibility services are enabled.
               return true;
            } else {
                return mPermittedAccessibilityServices.contains(
                        resolveInfo.serviceInfo.packageName);
            }
        }
        return false;
    }

    @Override
    protected boolean canModifyComponent(int position) {
        // System accessibility services are always enabled.
        return !isSystemApp(getApplicationInfo(position));
    }

    @Override
    public CharSequence getDisplayName(int position) {
        return getItem(position).loadLabel(mPackageManager);
    }
}
