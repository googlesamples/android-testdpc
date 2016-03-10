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

package com.afwsamples.testdpc.profilepolicy.permission;

import android.app.admin.DevicePolicyManager;
import android.content.ComponentName;
import android.content.Context;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.view.ViewParent;
import android.widget.ArrayAdapter;
import android.widget.RadioGroup;
import android.widget.TextView;

import com.afwsamples.testdpc.DeviceAdminReceiver;
import com.afwsamples.testdpc.R;

import java.util.List;

/**
 * Renders a list app permissions with allow/deny radio buttons.
 */
public class AppPermissionsArrayAdapter
        extends ArrayAdapter<AppPermissionsArrayAdapter.AppPermission>
        implements RadioGroup.OnCheckedChangeListener {

    private final DevicePolicyManager mDpm;
    private final ComponentName mAdminComponentName;

    public AppPermissionsArrayAdapter(Context context, int resource,
            List<AppPermission> objects) {
        super(context, resource, objects);
        mDpm = (DevicePolicyManager) getContext().getSystemService(Context.DEVICE_POLICY_SERVICE);
        mAdminComponentName = DeviceAdminReceiver.getComponentName(context);
    }

    @Override
    public View getView(final int position, View convertView, ViewGroup parent) {
        AppPermissionsViewHolder viewHolder;
        if (convertView == null || !(convertView.getTag() instanceof AppPermissionsViewHolder)) {
            convertView = LayoutInflater.from(getContext()).inflate(R.layout.permission_row,
                    parent, false);

            viewHolder = new AppPermissionsViewHolder();
            viewHolder.permissionName = (TextView) convertView.findViewById(R.id.permission_name);
            viewHolder.permissionGroup =
                    (RadioGroup) convertView.findViewById(R.id.permission_group);
            viewHolder.permissionGroup.setOnCheckedChangeListener(this);

            convertView.setTag(viewHolder);
        } else {
            viewHolder = (AppPermissionsViewHolder) convertView.getTag();
        }

        viewHolder.appPermission = getItem(position);
        viewHolder.permissionName.setText(viewHolder.appPermission.permissionName);
        viewHolder.permissionGroup.setTag(viewHolder.appPermission);
        switch (viewHolder.appPermission.permissionState) {
            case DevicePolicyManager.PERMISSION_GRANT_STATE_GRANTED:
                viewHolder.permissionGroup.check(R.id.permission_allow);
                break;
            case DevicePolicyManager.PERMISSION_GRANT_STATE_DEFAULT:
                viewHolder.permissionGroup.check(R.id.permission_default);
                break;
            case  DevicePolicyManager.PERMISSION_GRANT_STATE_DENIED:
                viewHolder.permissionGroup.check(R.id.permission_deny);
                break;
        }
        return convertView;
    }

    @Override
    public void onCheckedChanged(RadioGroup radioGroup, int checkedId) {
        final AppPermission appPermission = (AppPermission) radioGroup.getTag();
        switch (checkedId) {
            case R.id.permission_allow: {
                appPermission.permissionState = DevicePolicyManager.PERMISSION_GRANT_STATE_GRANTED;
            } break;
            case R.id.permission_default: {
                appPermission.permissionState = DevicePolicyManager.PERMISSION_GRANT_STATE_DEFAULT;
            } break;
            case R.id.permission_deny: {
                appPermission.permissionState = DevicePolicyManager.PERMISSION_GRANT_STATE_DENIED;
            } break;
        }
        mDpm.setPermissionGrantState(mAdminComponentName,
                appPermission.pkgName,
                appPermission.permissionName,
                appPermission.permissionState);
    }

    /**
     * A wrapper class for app permissions.
     */
    static class AppPermission {

        final String pkgName;
        final String permissionName;
        int permissionState;

        public AppPermission(String pkgName, String permissionName, int permissionState) {
            this.pkgName = pkgName;
            this.permissionName = permissionName;
            this.permissionState = permissionState;
        }
    }

    private final class AppPermissionsViewHolder {
        TextView permissionName;
        RadioGroup permissionGroup;
        AppPermission appPermission;
    }
}