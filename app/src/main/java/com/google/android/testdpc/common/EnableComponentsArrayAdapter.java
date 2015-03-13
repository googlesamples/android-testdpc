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

package com.google.android.testdpc.common;

import android.app.admin.DevicePolicyManager;
import android.content.Context;
import android.content.pm.ApplicationInfo;
import android.content.pm.PackageManager;
import android.content.pm.ResolveInfo;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ArrayAdapter;
import android.widget.CheckBox;
import android.widget.ImageView;
import android.widget.TextView;

import com.google.android.testdpc.R;

import java.util.ArrayList;
import java.util.List;

/**
 * A simple array adapter which contains a checkbox, an app icon and the app name. This is mainly
 * used to display the enable state of activities and services.
 */
public abstract class EnableComponentsArrayAdapter extends ArrayAdapter<ResolveInfo> {
    public static final String TAG = EnableComponentsArrayAdapter.class.getSimpleName();

    protected PackageManager mPackageManager;
    protected DevicePolicyManager mDevicePolicyManager;
    protected ArrayList<Boolean> mIsComponentEnabledList = new ArrayList<Boolean>();

    public EnableComponentsArrayAdapter(Context context, int resource, List<ResolveInfo> objects) {
        super(context, resource, objects);
        mPackageManager = context.getPackageManager();
        mDevicePolicyManager = (DevicePolicyManager) context.getSystemService(
                Context.DEVICE_POLICY_SERVICE);
        initIsComponentEnabledList();
    }

    @Override
    public View getView(final int position, View convertView, ViewGroup parent) {
        ApplicationInfo applicationInfo = getApplicationInfo(position);
        if (applicationInfo == null) {
            Log.e(TAG, "Fail to retrieve application info for the entry: " + position);
            return null;
        }

        if (convertView == null) {
            convertView = LayoutInflater.from(getContext()).inflate(R.layout.enable_component_row,
                    parent, false);
        }
        ImageView iconImageView = (ImageView) convertView.findViewById(R.id.pkg_icon);
        iconImageView.setImageDrawable(mPackageManager.getApplicationIcon(applicationInfo));
        TextView pkgNameTextView = (TextView) convertView.findViewById(R.id.pkg_name);
        pkgNameTextView.setText(mPackageManager.getApplicationLabel(applicationInfo));
        CheckBox enableComponentCheckbox = (CheckBox) convertView.findViewById(
                R.id.enable_component_checkbox);
        enableComponentCheckbox.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                mIsComponentEnabledList.set(position, ((CheckBox) v).isChecked());
            }
        });
        enableComponentCheckbox.setChecked(mIsComponentEnabledList.get(position));
        return convertView;
    }

    /**
     * Called when an entry of item is clicked. This is UX an improvement to enable / disable a
     * component when a row is clicked.
     *
     * @param view The view that the click event originated. It must contain the enable component
     *             checkbox.
     * @param position The position of a component in this adapter which should handle the click
     *                 event.
     */
    public void onItemClick(View view, int position) {
        CheckBox enableComponentCheckbox = (CheckBox) view.findViewById(
                R.id.enable_component_checkbox);
        enableComponentCheckbox.performClick();
    }

    /**
     * Gets the {@link android.content.pm.ApplicationInfo} of a service or an activity.
     *
     * @param position the position of the view that requests
     *        {@link android.content.pm.ApplicationInfo}.
     * @return The {@link android.content.pm.ApplicationInfo} of a service or an activity at the
     *         give position. The {@link android.content.pm.ApplicationInfo} should come from either
     *         a direct or indirect query of {@link android.content.pm.PackageManager}.
     */
    protected abstract ApplicationInfo getApplicationInfo(int position);

    /**
     * Initializes the {@link EnableComponentsArrayAdapter#mIsComponentEnabledList}.
     */
    protected abstract void initIsComponentEnabledList();

    /**
     * Checks whether an activity or service is enabled.
     * @param resolveInfo The service or activity resolve info.
     * @return true if the given activity or service is enabled, false otherwise.
     */
    protected abstract boolean isComponentEnabled(ResolveInfo resolveInfo);

    protected boolean isSystemApp(ApplicationInfo applicationInfo) {
        return applicationInfo != null
                && (applicationInfo.flags & ApplicationInfo.FLAG_SYSTEM) != 0;
    }
}
