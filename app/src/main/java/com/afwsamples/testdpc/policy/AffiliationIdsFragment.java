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

package com.afwsamples.testdpc.policy;

import android.annotation.TargetApi;
import android.app.AlertDialog;
import android.app.admin.DevicePolicyManager;
import android.content.ComponentName;
import android.content.Context;
import android.os.Build;
import android.os.Bundle;
import android.support.annotation.StringRes;
import android.support.v4.util.ArraySet;
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
import com.afwsamples.testdpc.common.Util;

import java.util.ArrayList;
import java.util.List;
import java.util.Set;

/**
 * Allows the user to see / edit / delete affiliation ids.
 * See {@link DevicePolicyManager#setAffiliationIds(ComponentName, Set)}
 */
public class AffiliationIdsFragment extends BaseManageComponentFragment<Void>
        implements EditDeleteArrayAdapter.OnEditButtonClickListener<String> {

    private List<String> mAffiliationIds = new ArrayList<>();
    private List<String> mLastAffiliationIds = new ArrayList<>();
    private DevicePolicyManager mDevicePolicyManager;
    private ComponentName mAdminComponent;
    private EditDeleteArrayAdapter<String> mAffiliationIdsArrayAdapter;

    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

        mDevicePolicyManager = (DevicePolicyManager) getActivity().getSystemService(
                Context.DEVICE_POLICY_SERVICE);
        mAdminComponent = DeviceAdminReceiver.getComponentName(getActivity());
    }

    @Override
    public View onCreateView(LayoutInflater layoutInflater, ViewGroup container,
            Bundle savedInstanceState) {
        View view = super.onCreateView(layoutInflater, container, savedInstanceState);
        mManagedAppsSpinner.setVisibility(View.INVISIBLE);  // We don't need the Spinner.

        loadDefault();
        return view;
    }

    @Override
    public void onResume() {
        super.onResume();
        getActivity().getActionBar().setTitle(R.string.manage_affiliation_ids);
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
        mAffiliationIdsArrayAdapter = new AffiliationIdEntryArrayAdapter(
                getActivity(), mAffiliationIds, this);
        return mAffiliationIdsArrayAdapter;
    }

    @Override
    protected void resetConfig() {
        mAffiliationIdsArrayAdapter.clear();
        mAffiliationIdsArrayAdapter.addAll(mLastAffiliationIds);
    }

    @Override
    @TargetApi(Build.VERSION_CODES.O)
    protected void saveConfig() {
        Util.setAffiliationIds(
                mDevicePolicyManager,
                DeviceAdminReceiver.getComponentName(getActivity()),
                new ArraySet<>(mAffiliationIds));
        mLastAffiliationIds = new ArrayList<>(mAffiliationIds);
    }

    @Override
    protected void addNewRow() {
        onEditButtonClick(null);
    }

    @Override
    @TargetApi(Build.VERSION_CODES.O)
    protected void loadDefault() {
        mAffiliationIdsArrayAdapter.clear();
        mAffiliationIdsArrayAdapter.addAll(
                Util.getAffiliationIds(mDevicePolicyManager, mAdminComponent));
        mLastAffiliationIds = new ArrayList<>(mAffiliationIds);
    }

    @Override
    public void onEditButtonClick(final String existingEntry) {
        View view = LayoutInflater.from(getActivity()).inflate(R.layout.simple_edittext, null);
        final EditText input = (EditText) view.findViewById(R.id.input);
        if (existingEntry != null) {
            input.setText(existingEntry);
        }

        final AlertDialog dialog = new AlertDialog.Builder(getActivity())
                .setTitle(R.string.enter_affiliation_id)
                .setView(view)
                .setPositiveButton(android.R.string.ok, null)
                .setNegativeButton(android.R.string.cancel, null)
                .create();
        dialog.setOnShowListener(
                dialogInterface -> dialog.getButton(AlertDialog.BUTTON_POSITIVE).setOnClickListener(
                        okButtonView -> {
                            String affiliationId = input.getText().toString();
                            if (TextUtils.isEmpty(affiliationId)) {
                                showToast(R.string.affiliation_id_empty_error);
                                return;
                            }
                            if (existingEntry != null) {
                                mAffiliationIdsArrayAdapter.remove(existingEntry);
                            }
                            mAffiliationIdsArrayAdapter.add(affiliationId);
                            dialog.dismiss();
                        }));
        dialog.show();
    }

    private void showToast(@StringRes int stringResId) {
        Toast.makeText(getActivity(), stringResId, Toast.LENGTH_LONG).show();
    }

    static class AffiliationIdEntryArrayAdapter extends EditDeleteArrayAdapter<String> {
        AffiliationIdEntryArrayAdapter(
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
}
