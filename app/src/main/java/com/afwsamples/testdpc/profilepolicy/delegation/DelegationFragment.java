/*
 * Copyright (C) 2016 The Android Open Source Project
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

package com.afwsamples.testdpc.profilepolicy.delegation;

import android.annotation.TargetApi;
import android.app.admin.DevicePolicyManager;
import android.content.Context;
import android.content.pm.ApplicationInfo;
import android.os.Build;
import android.os.Bundle;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.BaseAdapter;
import android.widget.CheckBox;
import android.widget.Toast;

import com.afwsamples.testdpc.DeviceAdminReceiver;
import com.afwsamples.testdpc.R;
import com.afwsamples.testdpc.common.ManageAppFragment;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;

/**
 * This fragment lets the user select an app that will receive delegate privileges for a number of
 * scopes. The specific scopes are selected through checkboxes.
 */
public class DelegationFragment extends ManageAppFragment {
    private static final String TAG = DelegationFragment.class.getSimpleName();

    private DevicePolicyManager mDpm;

    /**
     * Model for representing the scopes delegated to the selected app.
     */
    List<DelegationScope> mDelegations = DelegationScope.defaultDelegationScopes();

    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        mDpm = (DevicePolicyManager) getActivity().getSystemService(Context.DEVICE_POLICY_SERVICE);

        getActivity().getActionBar().setTitle(R.string.generic_delegation);
    }

    @Override
    public View onCreateView(LayoutInflater layoutInflater, ViewGroup container,
            Bundle savedInstanceState) {
        View view = super.onCreateView(layoutInflater, container, savedInstanceState);
        view.findViewById(R.id.add_new_row).setVisibility(View.GONE);
        view.findViewById(R.id.reset_app).setVisibility(View.GONE);
        return view;
    }

    /**
     * Query the DevicePolicyManager for the delegation scopes granted to pkgName.
     */
    @TargetApi(Build.VERSION_CODES.O)
    private void readScopesFromDpm(String pkgName) {
        // Get the scopes delegated to pkgName.
        List<String> scopes = mDpm.getDelegatedScopes(
                DeviceAdminReceiver.getComponentName(getActivity()), pkgName);
        Log.i(TAG, pkgName + " | " + Arrays.toString(scopes.toArray()));

        // Update our model.
        for (DelegationScope delegationScope : mDelegations) {
            delegationScope.granted = scopes.contains(delegationScope.scope);
        }
    }

    /**
     * Read the delegation scopes to be granted from the checkboxes in the screen and update
     * {@link #mDelegations}.
     */
    private List<String> readScopesFromUi() {
        List<String> scopes = new ArrayList<>();
        for (int i = 0; i < mDelegations.size(); ++i) {
            // Update mDelegations from the checkboxes.
            CheckBox checkBox = (CheckBox) mAppListView.getChildAt(i)
                    .findViewById(R.id.checkbox_delegation_scope);
            mDelegations.get(i).granted = checkBox.isChecked();
            // Fill in the list with the scopes to be delegated.
            if (mDelegations.get(i).granted) {
                scopes.add(mDelegations.get(i).scope);
            }
        }
        return scopes;
    }

    @Override
    protected void onSpinnerItemSelected(ApplicationInfo info) {
        final String pkgName = info.packageName;
        if (pkgName == null) {
            return;
        }

        // Get the scopes delegated to pkgName.
        readScopesFromDpm(pkgName);

        // Update UI to reflect any changes.
        mAdapter.notifyDataSetChanged();
    }

    @Override
    @TargetApi(Build.VERSION_CODES.O)
    protected void saveConfig() {
        // Get selected package name.
        final ApplicationInfo info = (ApplicationInfo) mManagedAppsSpinner.getSelectedItem();
        final String pkgName = info.packageName;

        // Update model from changes in the UI and get the scopes that are to be delegated.
        final List<String> scopes = readScopesFromUi();

        // Set the delegated scopes.
        mDpm.setDelegatedScopes(
                DeviceAdminReceiver.getComponentName(getActivity()), pkgName, scopes);
        Toast.makeText(getActivity(), getString(R.string.delegation_success),
                Toast.LENGTH_SHORT).show();

        Log.i(TAG, Arrays.toString(scopes.toArray()) + " | " + pkgName);
    }

    @Override
    protected BaseAdapter createListAdapter() {
        return new DelegationScopesArrayAdapter(getActivity(), 0, mDelegations);
    }

    @Override
    protected void resetConfig() {}

    @Override
    protected void addNewRow() {}

    @Override
    protected void loadDefault() {}

    /**
     * Simple wrapper to encapsulate the state of a delegation scope.
     */
    static class DelegationScope {
        final String scope;
        boolean granted;

        DelegationScope(String scope) {
            this.scope = scope;
            this.granted = false;
        }

        @TargetApi(Build.VERSION_CODES.O)
        static List<DelegationScope> defaultDelegationScopes() {
            List<DelegationScope> defaultDelegations = new ArrayList<>();
            defaultDelegations.add(
                    new DelegationScope(DevicePolicyManager.DELEGATION_CERT_INSTALL));
            defaultDelegations.add(
                    new DelegationScope(DevicePolicyManager.DELEGATION_APP_RESTRICTIONS));
            defaultDelegations.add(
                    new DelegationScope(DevicePolicyManager.DELEGATION_BLOCK_UNINSTALL));
            defaultDelegations.add(
                    new DelegationScope(DevicePolicyManager.DELEGATION_PERMISSION_GRANT));
            defaultDelegations.add(
                    new DelegationScope(DevicePolicyManager.DELEGATION_PACKAGE_ACCESS));
            defaultDelegations.add(
                    new DelegationScope(DevicePolicyManager.DELEGATION_ENABLE_SYSTEM_APP));
            return defaultDelegations;
        }
    }
}
