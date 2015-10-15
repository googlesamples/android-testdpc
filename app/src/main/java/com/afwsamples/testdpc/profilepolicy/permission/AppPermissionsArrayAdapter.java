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
import android.content.Context;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
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
        extends ArrayAdapter<AppPermissionsArrayAdapter.AppPermission> {

    private final DevicePolicyManager mDpm;

    public AppPermissionsArrayAdapter(Context context, int resource,
                                      List<AppPermission> objects) {
        super(context, resource, objects);
        mDpm = (DevicePolicyManager) getContext().getSystemService(Context.DEVICE_POLICY_SERVICE);
    }

    @Override
    public View getView(final int position, View convertView, ViewGroup parent) {
        AppPermissionsArrayAdapter.AppPermission permission = getItem(position);

        if (convertView == null) {
            convertView = LayoutInflater.from(getContext()).inflate(R.layout.permission_row,
                    parent, false);
        }

        ViewHolder viewHolder = new ViewHolder(convertView);
        viewHolder.permissionName.setText(permission.permissionName);
        switch (permission.permissionState) {
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
        viewHolder.permissionGroup.setOnCheckedChangeListener(
                new RadioGroup.OnCheckedChangeListener() {
                    @Override
                    public void onCheckedChanged(RadioGroup radioGroup, int i) {
                        AppPermission permission = getItem(position);
                        switch (i) {
                            case R.id.permission_allow:
                                permission.permissionState =
                                        DevicePolicyManager.PERMISSION_GRANT_STATE_GRANTED;
                                break;
                            case R.id.permission_default:
                                permission.permissionState =
                                        DevicePolicyManager.PERMISSION_GRANT_STATE_DEFAULT;
                                break;
                            case R.id.permission_deny:
                                permission.permissionState =
                                        DevicePolicyManager.PERMISSION_GRANT_STATE_DENIED;
                                break;
                        }
                        mDpm.setPermissionGrantState(
                                DeviceAdminReceiver.getComponentName(getContext()),
                                permission.pkgName, permission.permissionName,
                                permission.permissionState);
                    }
                });

        convertView.setTag(viewHolder);
        return convertView;
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

    private final class ViewHolder {
        final public TextView permissionName;
        final public RadioGroup permissionGroup;

        public ViewHolder(View view)
        {
            this.permissionName = (TextView) view.findViewById(R.id.permission_name);
            this.permissionGroup = (RadioGroup) view.findViewById(R.id.permission_group);
        }
    }
}