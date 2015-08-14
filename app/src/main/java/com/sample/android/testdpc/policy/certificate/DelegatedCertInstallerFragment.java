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

package com.sample.android.testdpc.policy.certificate;

import android.app.Fragment;
import android.app.admin.DevicePolicyManager;
import android.content.Context;
import android.content.pm.ApplicationInfo;
import android.content.pm.PackageManager;
import android.os.Bundle;
import android.text.TextUtils;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.AdapterView;
import android.widget.AdapterView.OnItemClickListener;
import android.widget.EditText;
import android.widget.ListView;

import com.sample.android.testdpc.DeviceAdminReceiver;
import com.sample.android.testdpc.R;
import com.sample.android.testdpc.common.AppInfoArrayAdapter;

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;


/**
 * This fragment provides functionalities related to delegated certificate installer.
 * These include
 * 1) {@link DevicePolicyManager#setCertInstallerPackage}
 * 2) {@link DevicePolicyManager#getCertInstallerPackage}
 */
public class DelegatedCertInstallerFragment extends Fragment implements View.OnClickListener,
        OnItemClickListener {

    private EditText mCurrentDelegatedCertInstaller;
    private EditText mNewDelegatedCertInstaller;
    private ListView mAppList;
    private ArrayList<String> mAppPackages;
    private DevicePolicyManager mDpm;

    @Override
    public void onResume() {
        super.onResume();
        getActivity().getActionBar().setTitle(R.string.manage_cert_installer);
        reloadDelegatedCertInstaller();
    }


    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        mDpm = (DevicePolicyManager) getActivity().getSystemService(Context.DEVICE_POLICY_SERVICE);
        mAppPackages = new ArrayList<String>();
    }

    @Override
    public View onCreateView(LayoutInflater layoutInflater, ViewGroup container,
            Bundle savedInstanceState) {
        View view = layoutInflater.inflate(R.layout.delegated_cert_installer, null);

        mCurrentDelegatedCertInstaller = (EditText) view.findViewById(
                R.id.delegated_cert_installer_current);
        mNewDelegatedCertInstaller = (EditText) view.findViewById(
                R.id.delegated_cert_installer_new);
        mAppList = (ListView) view.findViewById(R.id.delegated_cert_installer_app_list);
        populateApps();

        view.findViewById(R.id.delegated_cert_installer_set).setOnClickListener(this);
        view.findViewById(R.id.delegated_cert_installer_clear).setOnClickListener(this);
        mAppList.setOnItemClickListener(this);

        return view;
    }

    private void populateApps() {
        PackageManager pm = getActivity().getPackageManager();
        List<ApplicationInfo> allApps = pm.getInstalledApplications(0 /* No flag */);
        Collections.sort(allApps, new ApplicationInfo.DisplayNameComparator(pm));
        mAppPackages.clear();
        for(ApplicationInfo info : allApps) {
            if ((pm.getLaunchIntentForPackage(info.packageName)) != null) {
                mAppPackages.add(info.packageName);
            }
        }
        AppInfoArrayAdapter appInfoArrayAdapter = new AppInfoArrayAdapter(getActivity(),
                R.id.pkg_name, mAppPackages, true);
        mAppList.setAdapter(appInfoArrayAdapter);
    }


    @Override
    public void onClick(View v) {
        String newCertInstaller = null;
        switch (v.getId()) {
            case R.id.delegated_cert_installer_set:
                newCertInstaller = mNewDelegatedCertInstaller.getText().toString();
                break;
            case R.id.delegated_cert_installer_clear:
                mNewDelegatedCertInstaller.setText("");
                newCertInstaller = "";
                break;
        }
        if (newCertInstaller != null) {
            mDpm.setCertInstallerPackage(DeviceAdminReceiver.getComponentName(getActivity()),
                    TextUtils.isEmpty(newCertInstaller) ? null : newCertInstaller);
            reloadDelegatedCertInstaller();
        }
    }

    private void reloadDelegatedCertInstaller() {
        String certInstaller = mDpm.getCertInstallerPackage(
                DeviceAdminReceiver.getComponentName(getActivity()));
        if (certInstaller == null) {
            mCurrentDelegatedCertInstaller.setText("None");
        } else {
            mCurrentDelegatedCertInstaller.setText(certInstaller);
        }
    }

    @Override
    public void onItemClick(AdapterView<?> parent, View view, int position, long id) {
        String packageName = mAppPackages.get(position);
        mNewDelegatedCertInstaller.setText(packageName);
    }

}
