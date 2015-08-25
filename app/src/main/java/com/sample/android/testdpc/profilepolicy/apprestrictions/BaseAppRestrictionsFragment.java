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

package com.sample.android.testdpc.profilepolicy.apprestrictions;

import android.app.admin.DevicePolicyManager;
import android.content.Context;
import android.content.RestrictionEntry;
import android.content.RestrictionsManager;
import android.os.Bundle;
import android.os.Parcelable;
import android.text.TextUtils;
import android.view.View;
import android.widget.Toast;

import com.sample.android.testdpc.DeviceAdminReceiver;
import com.sample.android.testdpc.R;
import com.sample.android.testdpc.common.ManageAppFragment;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;
import java.util.Set;

/**
 * Base class for fragments showing application restrictions.
 */
public abstract class BaseAppRestrictionsFragment extends ManageAppFragment
        implements View.OnClickListener {

    protected List<RestrictionEntry> mRestrictionEntries;
    protected DevicePolicyManager mDevicePolicyManager;
    protected RestrictionsManager mRestrictionsManager;

    private AppRestrictionsArrayAdapter mAppRestrictionsArrayAdapter;
    private int mActionBarTitleResId;

    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setRetainInstance(true);
        mDevicePolicyManager = (DevicePolicyManager) getActivity().getSystemService(
                Context.DEVICE_POLICY_SERVICE);
        mRestrictionsManager = (RestrictionsManager) getActivity().getSystemService(
                Context.RESTRICTIONS_SERVICE);
    }

    @Override
    public void onResume() {
        super.onResume();
        mActionBarTitleResId = R.string.manage_app_restrictions;
        getActivity().getActionBar().setTitle(mActionBarTitleResId);
    }

    protected void updateViewVisibilities(View view) {
        view.findViewById(R.id.save_app).setOnClickListener(this);
        view.findViewById(R.id.reset_app).setOnClickListener(this);
        view.findViewById(R.id.add_new_row).setOnClickListener(this);
    }

    @Override
    protected void loadData(String pkgName) {
        if (!TextUtils.isEmpty(pkgName)) {
            Bundle bundle = mDevicePolicyManager.getApplicationRestrictions(
                    DeviceAdminReceiver.getComponentName(getActivity()), pkgName);

            loadAppRestrictionsList(convertBundleToRestrictions(bundle));
        }
    }

    protected void loadAppRestrictionsList(RestrictionEntry[] restrictionEntries) {
        if (restrictionEntries != null) {
            mRestrictionEntries =
                    new ArrayList<RestrictionEntry>(Arrays.asList(restrictionEntries));
            mAppRestrictionsArrayAdapter = new AppRestrictionsArrayAdapter(getActivity(),
                    mRestrictionEntries, this, getCurrentAppName());
            mAppListView.setAdapter(mAppRestrictionsArrayAdapter);
            mAppRestrictionsArrayAdapter.notifyDataSetChanged();
        }
    }

    protected RestrictionEntry[] convertBundleToRestrictions(Bundle restrictionBundle) {
        List<RestrictionEntry> restrictionEntries = new ArrayList<RestrictionEntry>();
        Set<String> keys = restrictionBundle.keySet();
        for (String key : keys) {
            Object value = restrictionBundle.get(key);
            if (value instanceof Boolean) {
                restrictionEntries.add(new RestrictionEntry(key, (boolean) value));
            } else if (value instanceof Integer) {
                restrictionEntries.add(new RestrictionEntry(key, (int) value));
            } else if (value instanceof String) {
                RestrictionEntry entry = new RestrictionEntry(RestrictionEntry.TYPE_STRING, key);
                entry.setSelectedString((String) value);
                restrictionEntries.add(entry);
            } else if (value instanceof String[]) {
                restrictionEntries.add(new RestrictionEntry(key, (String[]) value));
            } else if (value instanceof Bundle) {
                restrictionEntries.add(RestrictionEntry.createBundleEntry(
                        key, convertBundleToRestrictions((Bundle) value)));
            } else if (value instanceof Parcelable[]) {
                Parcelable[] parcelableArray = (Parcelable[]) value;
                int length = parcelableArray.length;
                RestrictionEntry[] entriesArray = new RestrictionEntry[length];
                for (int i = 0; i < entriesArray.length; ++i) {
                    entriesArray[i] = RestrictionEntry.createBundleEntry(key,
                            convertBundleToRestrictions((Bundle) parcelableArray[i]));
                }
                restrictionEntries.add(RestrictionEntry.createBundleArrayEntry(key, entriesArray));
            }
        }
        return restrictionEntries.toArray(new RestrictionEntry[0]);
    }

    public void saveNestedRestrictions(int restrictionPosition,
            List<RestrictionEntry> restrictionEntries) {
        mRestrictionEntries.get(restrictionPosition).setRestrictions(
                restrictionEntries.toArray(new RestrictionEntry[0]));
    }

    @Override
    public void onClick(View v) {
        switch (v.getId()) {
            case R.id.reset_app:
                mAppRestrictionsArrayAdapter.resetAppRestrictions();
                break;
            case R.id.add_new_row:
                mAppRestrictionsArrayAdapter.addNewEntry();
                break;
        }
    }

    public void setActionBarTitleResId(int resId) {
        mActionBarTitleResId = resId;
    }

    public int getActionBarTitleResId() {
        return mActionBarTitleResId;
    }

    /**
     * Return the name of the application whose restrictions are currently displayed.
     */
    protected abstract String getCurrentAppName();

    protected void showToast(String msg) {
        Toast.makeText(getContext(), msg, Toast.LENGTH_SHORT).show();
    }
}
