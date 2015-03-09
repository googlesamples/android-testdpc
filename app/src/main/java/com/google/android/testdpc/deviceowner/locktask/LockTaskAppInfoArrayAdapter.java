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

package com.google.android.testdpc.deviceowner.locktask;

import android.app.admin.DevicePolicyManager;
import android.content.Context;
import android.content.pm.ApplicationInfo;
import android.content.pm.PackageManager;
import android.content.pm.ResolveInfo;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ArrayAdapter;
import android.widget.ImageView;
import android.widget.Switch;
import android.widget.TextView;

import com.google.android.testdpc.R;

import java.util.ArrayList;
import java.util.List;

/**
 * An array adapter which takes a {@link java.util.ArrayList<android.content.pm.ResolveInfo>} and
 * renders them into a listview. Each entry contains a switch, an app icon and the app name. The
 * switch is used to indicate whether the lock task for that app is permitted or not.
 */
public class LockTaskAppInfoArrayAdapter extends ArrayAdapter<ResolveInfo> {

    private final PackageManager mPackageManager;

    private final DevicePolicyManager mDevicePolicyManager;

    private final ArrayList<Boolean> mIsLockTaskPermittedList = new ArrayList<Boolean>();

    public LockTaskAppInfoArrayAdapter(Context context, int resource,
            List<ResolveInfo> objects) {
        super(context, resource, objects);
        mPackageManager = getContext().getPackageManager();
        mDevicePolicyManager = (DevicePolicyManager) getContext().getSystemService(
                Context.DEVICE_POLICY_SERVICE);
        int size = getCount();
        for (int i = 0; i < size; i++) {
            mIsLockTaskPermittedList.add(mDevicePolicyManager.isLockTaskPermitted(
                    getItem(i).activityInfo.packageName));
        }
    }

    @Override
    public View getView(final int position, View convertView, ViewGroup parent) {
        ApplicationInfo applicationInfo = getItem(position).activityInfo.applicationInfo;
        if (convertView == null) {
            convertView = LayoutInflater.from(getContext()).inflate(R.layout.lock_task_app_row,
                    parent, false);
        }
        ImageView iconImageView = (ImageView) convertView.findViewById(R.id.pkg_icon);
        iconImageView.setImageDrawable(mPackageManager.getApplicationIcon(applicationInfo));
        TextView pkgNameTextView = (TextView) convertView.findViewById(R.id.pkg_name);
        pkgNameTextView.setText(mPackageManager.getApplicationLabel(applicationInfo));
        final Switch lockTaskPermittedSwitch = (Switch) convertView.findViewById(
                R.id.enable_lock_task_switch);
        lockTaskPermittedSwitch.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                mIsLockTaskPermittedList.set(position, lockTaskPermittedSwitch.isChecked());
            }
        });
        lockTaskPermittedSwitch.setChecked(mIsLockTaskPermittedList.get(position));
        return convertView;
    }

    /**
     * Invoke to save the lock task state for apps.
     */
    public String[] getLockTaskList() {
        ArrayList<String> lockTaskEnabledArrayList = new ArrayList<String>();
        int size = getCount();
        for (int i = 0; i < size; i++) {
            if (mIsLockTaskPermittedList.get(i)) {
                lockTaskEnabledArrayList.add(getItem(i).activityInfo.packageName);
            }
        }
        String[] lockTaskEnabledArray = new String[lockTaskEnabledArrayList.size()];
        return lockTaskEnabledArrayList.toArray(lockTaskEnabledArray);
    }
}
