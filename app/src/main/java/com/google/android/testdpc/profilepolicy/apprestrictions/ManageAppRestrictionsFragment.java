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

package com.google.android.testdpc.profilepolicy.apprestrictions;

import android.app.Fragment;
import android.app.admin.DevicePolicyManager;
import android.content.Context;
import android.content.Intent;
import android.content.RestrictionEntry;
import android.content.RestrictionsManager;
import android.content.pm.ApplicationInfo;
import android.content.pm.PackageManager;
import android.os.Bundle;
import android.text.TextUtils;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.AdapterView;
import android.widget.ListView;
import android.widget.Spinner;

import com.google.android.testdpc.DeviceAdminReceiver;
import com.google.android.testdpc.R;
import com.google.android.testdpc.common.AppInfoSpinnerAdapter;

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;
import java.util.Set;

/**
 * This fragment shows all installed apps and allows viewing and editing application restrictions
 * for those apps. It also allows loading the default app restrictions for each of those apps.
 */
public class ManageAppRestrictionsFragment extends Fragment implements View.OnClickListener {

    private Spinner mManagedAppsSpinner;

    private AppRestrictionsArrayAdapter mAppRestrictionsArrayAdapter;

    private ListView mAppRestrictionsListView;

    private PackageManager mPackageManager;

    @Override
    public void onResume() {
        super.onResume();
        getActivity().getActionBar().setTitle(R.string.manage_app_restrictions);
    }


    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        mPackageManager = getActivity().getPackageManager();
    }

    @Override
    public View onCreateView(LayoutInflater layoutInflater, ViewGroup container,
            Bundle savedInstanceState) {
        View view = layoutInflater.inflate(R.layout.manage_app_restrictions, null);
        mManagedAppsSpinner = (Spinner) view.findViewById(R.id.managed_apps_list);
        Intent intent = new Intent(Intent.ACTION_MAIN);
        intent.addCategory(Intent.CATEGORY_LAUNCHER);
        List<ApplicationInfo> managedAppList = getInstalledLaunchableApps();
        Collections.sort(managedAppList,
                new ApplicationInfo.DisplayNameComparator(mPackageManager));
        AppInfoSpinnerAdapter appInfoSpinnerAdapter = new AppInfoSpinnerAdapter(getActivity(),
                R.layout.app_row, R.id.pkg_name, managedAppList);
        mManagedAppsSpinner.setAdapter(appInfoSpinnerAdapter);
        mManagedAppsSpinner.setOnItemSelectedListener(new AdapterView.OnItemSelectedListener() {
            @Override
            public void onItemSelected(AdapterView<?> parent, View view, int position, long id) {
                loadAppRestrictions(
                        ((ApplicationInfo) mManagedAppsSpinner.getSelectedItem()).packageName);
            }

            @Override
            public void onNothingSelected(AdapterView<?> parent) {
                // Do nothing.
            }
        });
        mAppRestrictionsListView = (ListView) view.findViewById(R.id.app_restrictions_list_view);
        view.findViewById(R.id.set_app_restrictions).setOnClickListener(this);
        view.findViewById(R.id.load_default_app_restrictions).setOnClickListener(this);
        view.findViewById(R.id.add_new_row).setOnClickListener(this);

        loadAppRestrictions(
                ((ApplicationInfo) mManagedAppsSpinner.getSelectedItem()).packageName);
        return view;
    }

    private List<ApplicationInfo> getInstalledLaunchableApps() {
        List<ApplicationInfo> managedAppList = mPackageManager.getInstalledApplications(
                0 /* Default flags */);
        List<ApplicationInfo> launchableAppList = new ArrayList<ApplicationInfo>();
        for (ApplicationInfo applicationInfo : managedAppList) {
            if ((mPackageManager.getLaunchIntentForPackage(applicationInfo.packageName)) != null) {
                launchableAppList.add(applicationInfo);
            }
        }
        return launchableAppList;
    }

    private void loadAppRestrictions(String pkgName) {
        if (!TextUtils.isEmpty(pkgName)) {
            DevicePolicyManager devicePolicyManager = (DevicePolicyManager) getActivity()
                    .getSystemService(Context.DEVICE_POLICY_SERVICE);
            Bundle bundle = devicePolicyManager
                    .getApplicationRestrictions(DeviceAdminReceiver.getComponentName(getActivity()),
                            pkgName);

            List<RestrictionEntry> restrictionEntries = new ArrayList<RestrictionEntry>();
            Set<String> keys = bundle.keySet();
            for (String key : keys) {
                Object value = bundle.get(key);
                if (value instanceof Boolean) {
                    restrictionEntries.add(new RestrictionEntry(key, (boolean) value));
                } else if (value instanceof Integer) {
                    restrictionEntries.add(new RestrictionEntry(key, (int) value));
                } else if (value instanceof String) {
                    restrictionEntries.add(new RestrictionEntry(key, (String) value));
                } else if (value instanceof String[]) {
                    restrictionEntries.add(new RestrictionEntry(key, (String[]) value));
                }
            }
            loadAppRestrictionsList(restrictionEntries);
        }
    }

    private void loadDefaultAppRestrictions(String pkgName) {
        if (!TextUtils.isEmpty(pkgName)) {
            RestrictionsManager restrictionsManager = (RestrictionsManager) getActivity()
                    .getSystemService(Context.RESTRICTIONS_SERVICE);
            List<RestrictionEntry> restrictionEntries = null;
            try {
                restrictionEntries = restrictionsManager.getManifestRestrictions(pkgName);
            } catch (NullPointerException e) {
                // This means no default restrictions.
            }
            loadAppRestrictionsList(restrictionEntries);
        }
    }

    private void loadAppRestrictionsList(List<RestrictionEntry> restrictionEntries) {
        if (restrictionEntries != null) {
            mAppRestrictionsArrayAdapter = new AppRestrictionsArrayAdapter(getActivity(),
                    R.id.key,
                    restrictionEntries);
            mAppRestrictionsListView.setAdapter(mAppRestrictionsArrayAdapter);
            mAppRestrictionsArrayAdapter.notifyDataSetChanged();
        }
    }

    @Override
    public void onClick(View v) {
        switch (v.getId()) {
            case R.id.set_app_restrictions:
                mAppRestrictionsArrayAdapter.setAppRestrictions(
                        ((ApplicationInfo) mManagedAppsSpinner.getSelectedItem()).packageName);
                break;
            case R.id.load_default_app_restrictions:
                loadDefaultAppRestrictions(
                        ((ApplicationInfo) mManagedAppsSpinner.getSelectedItem()).packageName);
                break;
            case R.id.add_new_row:
                mAppRestrictionsArrayAdapter.addNewRow();
                break;
        }
    }
}
