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

package com.afwsamples.testdpc.common;

import android.app.admin.DevicePolicyManager;
import android.content.Context;
import android.content.pm.ApplicationInfo;
import android.content.pm.PackageManager;
import android.content.pm.ResolveInfo;
import android.graphics.drawable.Drawable;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.AdapterView;
import android.widget.ArrayAdapter;
import android.widget.CheckBox;
import android.widget.ImageView;
import android.widget.TextView;

import com.afwsamples.testdpc.R;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;

/**
 * A simple array adapter which shows a checkbox, an app icon and the app name for each item in a
 * list of {@link android.content.pm.ResolveInfo}.
 */
public abstract class ToggleComponentsArrayAdapter extends ArrayAdapter<ResolveInfo> implements
        AdapterView.OnItemClickListener {
    public static final String TAG = ToggleComponentsArrayAdapter.class.getSimpleName();

    protected PackageManager mPackageManager;
    protected DevicePolicyManager mDevicePolicyManager;
    protected List<Boolean> mIsComponentCheckedList;

    public ToggleComponentsArrayAdapter(Context context, int resource, List<ResolveInfo> objects) {
        super(context, resource, objects);
        mPackageManager = context.getPackageManager();
        mDevicePolicyManager = (DevicePolicyManager) context.getSystemService(
                Context.DEVICE_POLICY_SERVICE);
        // Init mIsComponentCheckedList
        mIsComponentCheckedList = new ArrayList<>(Arrays.asList(new Boolean[objects.size()]));
    }

    /**
     * Get the display name of the item.
     *
     * @param position The position of a component in this adapter which should handle the click
     *                 event.
     */
    public abstract CharSequence getDisplayName(int position);

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
        iconImageView.setImageDrawable(getApplicationIcon(applicationInfo));
        TextView pkgNameTextView = (TextView) convertView.findViewById(R.id.pkg_name);
        pkgNameTextView.setText(getDisplayName(position));
        CheckBox enableComponentCheckbox = (CheckBox) convertView.findViewById(
                R.id.enable_component_checkbox);
        enableComponentCheckbox.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                // Only handles the onClick event if the component can be enabled or disabled.
                if (canModifyComponent(position)) {
                    mIsComponentCheckedList.set(position, ((CheckBox) v).isChecked());
                } else {
                    ((CheckBox) v).setChecked(mIsComponentCheckedList.get(position));
                }
            }
        });
        enableComponentCheckbox.setChecked(mIsComponentCheckedList.get(position));
        enableComponentCheckbox.setEnabled(canModifyComponent(position));
        return convertView;
    }

    /**
     * Called when an entry of item is clicked.
     *
     * @param parent The AdapterView where the click happened.
     * @param view The view that was clicked on. It must contain the enable component checkbox.
     * @param position The position of a component in this adapter which should handle the click
     *                 event.
     * @param id The row id of the item that was clicked.
     */
    @Override
    public void onItemClick(AdapterView<?> parent, View view, int position, long id) {
        CheckBox enableComponentCheckbox = (CheckBox) view.findViewById(
                R.id.enable_component_checkbox);
        enableComponentCheckbox.performClick();
    }

    /**
     * Gets the {@link android.content.pm.ApplicationInfo} of a service or an activity.
     *
     * @param position The position of the view that requests
     *        {@link android.content.pm.ApplicationInfo}.
     * @return The {@link android.content.pm.ApplicationInfo} of a service or an activity at the
     *         given position. The {@link android.content.pm.ApplicationInfo} should come from
     *         either a direct or indirect query of {@link android.content.pm.PackageManager}.
     */
    protected abstract ApplicationInfo getApplicationInfo(int position);

    /**
     * Update {@link ToggleComponentsArrayAdapter#mIsComponentCheckedList}.
     */
    protected void setIsComponentEnabledList(List<Boolean> isComponentEnabledList) {
        mIsComponentCheckedList = isComponentEnabledList;
    }

    /**
     * Checks whether an activity or service is enabled.
     *
     * @param resolveInfo The service or activity resolve info.
     * @return true if the given activity or service is enabled, false otherwise.
     */
    protected abstract boolean isComponentEnabled(ResolveInfo resolveInfo);

    /**
     * Checks if the component in a given position can be enabled or disabled.
     *
     * @param position The position of the component in this adapter
     * @return true if the component can be enabled or disabled. Otherwise, false.
     */
    protected abstract boolean canModifyComponent(int position);

    protected boolean isSystemApp(ApplicationInfo applicationInfo) {
        return applicationInfo != null
                && (applicationInfo.flags & ApplicationInfo.FLAG_SYSTEM) != 0;
    }

    protected Drawable getApplicationIcon(ApplicationInfo applicationInfo) {
        return mPackageManager.getApplicationIcon(applicationInfo);
    }
}
