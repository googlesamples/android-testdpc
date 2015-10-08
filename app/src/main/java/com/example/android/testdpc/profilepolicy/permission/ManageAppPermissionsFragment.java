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

package com.example.android.testdpc.profilepolicy.permission;

import android.app.admin.DevicePolicyManager;
import android.content.Context;
import android.content.pm.PackageInfo;
import android.content.pm.PackageManager;
import android.content.pm.PermissionInfo;
import android.os.Bundle;
import android.text.TextUtils;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;

import com.example.android.testdpc.DeviceAdminReceiver;
import com.example.android.testdpc.R;
import com.example.android.testdpc.common.ManageAppFragment;

import java.util.ArrayList;
import java.util.List;

/**
 * This fragment shows all installed apps and allows viewing and editing the dangerous application
 * permissions for those apps.
 */
public class ManageAppPermissionsFragment extends ManageAppFragment {
    private static final String TAG = "ManageAppPermissions";

    private DevicePolicyManager mDpm;

    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        mDpm = (DevicePolicyManager) getContext().getSystemService(Context.DEVICE_POLICY_SERVICE);
    }

    @Override
    public View onCreateView(LayoutInflater layoutInflater, ViewGroup container,
                             Bundle savedInstanceState) {
        View view = super.onCreateView(layoutInflater, container, savedInstanceState);
        view.findViewById(R.id.load_default_button).setVisibility(View.GONE);
        view.findViewById(R.id.add_new_row).setVisibility(View.GONE);
        view.findViewById(R.id.manage_app_button_container).setVisibility(View.GONE);
        return view;
    }

    @Override
    protected void loadData(String pkgName) {
        if (!TextUtils.isEmpty(pkgName)) {
            List<String> permissions = new ArrayList<String>();

            PackageInfo info = null;
            try {
                info = mPackageManager.getPackageInfo(pkgName, PackageManager.GET_PERMISSIONS);
            } catch (PackageManager.NameNotFoundException e) {
                Log.e(TAG, "Could not retrieve info about the package: " + pkgName, e);
                return;
            }

            if (info != null && info.requestedPermissions != null) {
                for (String requestedPerm : info.requestedPermissions) {
                    try {
                        PermissionInfo pInfo = mPackageManager.getPermissionInfo(requestedPerm, 0);
                        if (pInfo != null) {
                            if ((pInfo.protectionLevel & PermissionInfo.PROTECTION_MASK_BASE)
                                    == PermissionInfo.PROTECTION_DANGEROUS) {
                                permissions.add(pInfo.name);
                            }
                        }
                    } catch (PackageManager.NameNotFoundException e) {
                        Log.i(TAG, "Could not retrieve info about the permission: "
                                + requestedPerm);
                    }
                }
            }

            List<AppPermissionsArrayAdapter.AppPermission> populatedPermissions = new ArrayList<>();
            for (String permission : permissions) {
                int permissionState = mDpm.getPermissionGrantState(
                        DeviceAdminReceiver.getComponentName(getContext()), pkgName, permission);
                AppPermissionsArrayAdapter.AppPermission populatedPerm =
                        new AppPermissionsArrayAdapter.AppPermission(pkgName, permission,
                                permissionState);
                populatedPermissions.add(populatedPerm);
            }

            loadAppPermissionsList(populatedPermissions);
        }
    }

    private void loadAppPermissionsList(
            List<AppPermissionsArrayAdapter.AppPermission> permissions) {
        if (permissions != null) {
            AppPermissionsArrayAdapter appPermissionsArrayAdapter =
                    new AppPermissionsArrayAdapter(getActivity(), 0, permissions);
            mAppListView.setAdapter(appPermissionsArrayAdapter);
            appPermissionsArrayAdapter.notifyDataSetChanged();
        }
    }
}
