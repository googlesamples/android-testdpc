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

package com.afwsamples.testdpc.policy;

import android.content.Context;
import android.content.pm.ApplicationInfo;
import android.content.pm.ResolveInfo;
import android.graphics.drawable.Drawable;
import android.os.Build;
import android.text.TextUtils;

import com.afwsamples.testdpc.R;
import com.afwsamples.testdpc.common.ToggleComponentsArrayAdapter;

import java.util.ArrayList;
import java.util.List;

/**
 * Displays a list of available components with a checkbox for enabling the component.
 * All components belonging to system packages are enabled by default and can't be disabled.
 */
public class AvailableComponentsInfoArrayAdapter extends ToggleComponentsArrayAdapter {
    private List<String> mPermittedPackageNames = null;

    public AvailableComponentsInfoArrayAdapter(Context context,
            List<ResolveInfo> resolveInfoList, List<String> permittedPackageNames) {
        super(context, R.id.pkg_name, resolveInfoList);
        mPermittedPackageNames = permittedPackageNames;
        setIsComponentEnabledList(createIsComponentEnabledList());
    }

    /**
     * There are three cases where a given component in a profile is enabled.
     * 1) There is no restriction on the given component.
     * 2) The given component's package name exist in the permitted package list.
     * 3) The given component belong to a system package.
     */
    @Override
    public boolean isComponentEnabled(ResolveInfo resolveInfo) {
        if (resolveInfo != null && resolveInfo.serviceInfo != null && !TextUtils.isEmpty(
                resolveInfo.serviceInfo.packageName)) {
            if (mPermittedPackageNames == null || isSystemApp(
                    resolveInfo.serviceInfo.applicationInfo)) {
                return true;
            } else {
                return mPermittedPackageNames.contains(resolveInfo.serviceInfo.packageName);
            }
        }
        return false;
    }

    @Override
    protected boolean canModifyComponent(int position) {
        // Components in a system package are always enabled.
        return !isSystemApp(getApplicationInfo(position));
    }

    public ArrayList<String> getSelectedComponents() {
        ArrayList<String> selectedComponentsArrayList = new ArrayList<String>();
        int size = getCount();
        for (int i = 0; i < size; i++) {
            if (mIsComponentCheckedList.get(i)) {
                selectedComponentsArrayList.add(getItem(i).serviceInfo.packageName);
            }
        }
        return selectedComponentsArrayList;
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

    private List<Boolean> createIsComponentEnabledList() {
        List<Boolean> isComponentEnabledList = new ArrayList<>();
        int size = getCount();
        for (int i = 0; i < size; i++) {
            isComponentEnabledList.add(isComponentEnabled(getItem(i)));
        }
        return isComponentEnabledList;
    }

    @Override
    public CharSequence getDisplayName(int position) {
        return getItem(position).loadLabel(mPackageManager);
    }
}