/*
 * Copyright (C) 2017 The Android Open Source Project
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

package com.afwsamples.testdpc.policy.keyguard;

import android.annotation.TargetApi;
import android.app.AlertDialog;
import android.app.admin.DevicePolicyManager;
import android.content.ComponentName;
import android.content.Context;
import android.os.Build;
import android.os.Bundle;
import android.support.annotation.StringRes;
import android.text.TextUtils;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.BaseAdapter;
import android.widget.EditText;
import android.widget.SpinnerAdapter;
import android.widget.Toast;
import com.afwsamples.testdpc.DeviceAdminReceiver;
import com.afwsamples.testdpc.R;
import com.afwsamples.testdpc.common.BaseManageComponentFragment;
import com.afwsamples.testdpc.common.EditDeleteArrayAdapter;
import java.text.DateFormat;
import java.util.ArrayList;
import java.util.Date;
import java.util.List;

@TargetApi(28)
public class PasswordBlacklistFragment extends BaseManageComponentFragment<Void>
        implements EditDeleteArrayAdapter.OnEditButtonClickListener<String> {

    public PasswordConstraintsFragment passwordConstraintsFragment;

    private List<String> mPasswordBlacklist = new ArrayList<>();
    private DevicePolicyManager mDevicePolicyManager;
    private ComponentName mAdminComponent;
    private EditDeleteArrayAdapter<String> mPasswordBlacklistArrayAdapter;

    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        mAdminComponent = DeviceAdminReceiver.getComponentName(getActivity());
    }

    public void setDpm(DevicePolicyManager dpm) {
        mDevicePolicyManager = dpm;
    }

    @Override
    public View onCreateView(LayoutInflater layoutInflater, ViewGroup container,
                             Bundle savedInstanceState) {
        View view = super.onCreateView(layoutInflater, container, savedInstanceState);
        mManagedAppsSpinner.setVisibility(View.INVISIBLE);  // We don't need the Spinner.
        return view;
    }

    @Override
    public void onResume() {
        super.onResume();
        getActivity().getActionBar().setTitle(R.string.password_blacklist_manager_title);
    }

    @Override
    public void onPause() {
        super.onPause();
        if (passwordConstraintsFragment != null) {
            // Blacklist has changed so updated the preferences
            // TODO: there must be a neater way to do this but onResume isn't called on navigation
            passwordConstraintsFragment.refreshBlacklistPreferences();
        }
    }

    @Override
    protected SpinnerAdapter createSpinnerAdapter() {
        return null;  // We don't need a spinner.
    }

    @Override
    protected void onSpinnerItemSelected(Void item) {
    }

    @Override
    protected BaseAdapter createListAdapter() {
        mPasswordBlacklistArrayAdapter = new PasswordBlacklistEntryArrayAdapter(
                getActivity(), mPasswordBlacklist, this);
        return mPasswordBlacklistArrayAdapter;
    }

    @Override
    protected void resetConfig() {
        mPasswordBlacklistArrayAdapter.clear();
    }

    @Override
    protected void saveConfig() {
        // Name the blacklist with the timestamp when it was set
        final String name = DateFormat.getDateTimeInstance().format(new Date());
        if (!setBlacklist(mDevicePolicyManager, mAdminComponent, name, mPasswordBlacklist)) {
            showToast(R.string.password_blacklist_save_failed);
            return;
        }

        showToast(R.string.password_blacklist_saved);
    }

    @Override
    protected void addNewRow() {
        onEditButtonClick(null);
    }

    @Override
    protected void loadDefault() {
        mPasswordBlacklistArrayAdapter.clear();
    }

    @Override
    public void onEditButtonClick(final String existingEntry) {
        View view = LayoutInflater.from(getActivity()).inflate(R.layout.simple_edittext, null);
        final EditText input = view.findViewById(R.id.input);
        if (existingEntry != null) {
            input.setText(existingEntry);
        }

        final AlertDialog dialog = new AlertDialog.Builder(getActivity())
                .setTitle(R.string.password_blacklist_add)
                .setView(view)
                .setPositiveButton(android.R.string.ok, null)
                .setNegativeButton(android.R.string.cancel, null)
                .create();
        dialog.setOnShowListener(
                dialogInterface -> dialog.getButton(AlertDialog.BUTTON_POSITIVE).setOnClickListener(
                        okButtonView -> {
                            String password = input.getText().toString();
                            if (TextUtils.isEmpty(password)) {
                                showToast(R.string.password_blacklist_add_empty_error);
                                return;
                            }
                            if (existingEntry != null) {
                                mPasswordBlacklistArrayAdapter.remove(existingEntry);
                            }
                            mPasswordBlacklistArrayAdapter.add(password);
                            dialog.dismiss();
                        }));
        dialog.show();
    }

    private void showToast(@StringRes int stringResId) {
        Toast.makeText(getActivity(), stringResId, Toast.LENGTH_LONG).show();
    }

    static class PasswordBlacklistEntryArrayAdapter extends EditDeleteArrayAdapter<String> {
        PasswordBlacklistEntryArrayAdapter(
                Context context,
                List<String> entries,
                OnEditButtonClickListener onEditButtonClickListener) {
            super(context, entries, onEditButtonClickListener, null);
        }

        @Override
        protected String getDisplayName(String entry) {
            return entry;
        }
    }

    @TargetApi(28)
    static boolean setBlacklist(DevicePolicyManager dpm, ComponentName admin, String name,
                                List<String> blacklist) {
        return dpm.setPasswordBlacklist(admin, name, blacklist);
    }

    @TargetApi(28)
    static String getBlacklistName(DevicePolicyManager dpm, ComponentName admin) {
        return dpm.getPasswordBlacklistName(admin);
    }
}