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

package com.afwsamples.testdpc.policy.locktask;

import android.content.Context;
import android.content.pm.ApplicationInfo;
import android.content.pm.ResolveInfo;
import android.text.TextUtils;

import com.afwsamples.testdpc.common.ToggleComponentsArrayAdapter;

import java.util.ArrayList;
import java.util.List;

/**
 * An array adapter which takes a {@link java.util.ArrayList<android.content.pm.ResolveInfo>} and
 * renders its items into a listview. Each entry contains a checkbox, an app icon and the app name.
 * The checkbox is used to indicate whether the lock task for that app is permitted or not.
 */
public class LockTaskAppInfoArrayAdapter extends ToggleComponentsArrayAdapter {

    public LockTaskAppInfoArrayAdapter(Context context, int resource, List<ResolveInfo> objects) {
        super(context, resource, objects);
    }

    @Override
    public boolean isComponentEnabled(ResolveInfo resolveInfo) {
        if (resolveInfo != null && resolveInfo.activityInfo != null &&!TextUtils.isEmpty(
                resolveInfo.activityInfo.packageName)) {
            return mDevicePolicyManager.isLockTaskPermitted(resolveInfo.activityInfo.packageName);
        }
        return false;
    }

    /**
     * Invoke to get a list of the permitted lock tasks.
     */
    public String[] getLockTaskList() {
        ArrayList<String> lockTaskEnabledArrayList = new ArrayList<String>();
        int size = getCount();
        for (int i = 0; i < size; i++) {
            if (mIsComponentCheckedList.get(i)) {
                lockTaskEnabledArrayList.add(getItem(i).activityInfo.packageName);
            }
        }
        String[] lockTaskEnabledArray = new String[lockTaskEnabledArrayList.size()];
        return lockTaskEnabledArrayList.toArray(lockTaskEnabledArray);
    }

    @Override
    protected ApplicationInfo getApplicationInfo(int position) {
        return getItem(position).activityInfo.applicationInfo;
    }

    @Override
    protected void initIsComponentEnabledList() {
        int size = getCount();
        for (int i = 0; i < size; i++) {
            mIsComponentCheckedList.add(isComponentEnabled(getItem(i)));
        }
    }

    @Override
    protected boolean canModifyComponent(int position) {
        // Any app can be set as a lock task.
        return true;
    }

    @Override
    public CharSequence getDisplayName(int position) {
        return getItem(position).loadLabel(mPackageManager);
    }
}
