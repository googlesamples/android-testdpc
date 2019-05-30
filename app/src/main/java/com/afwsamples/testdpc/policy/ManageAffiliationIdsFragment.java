/*
 * Copyright (C) 2018 The Android Open Source Project
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
import android.app.admin.DevicePolicyManager;
import android.content.ComponentName;
import android.content.Context;
import android.os.Build.VERSION_CODES;
import android.os.Bundle;
import androidx.collection.ArraySet;
import com.afwsamples.testdpc.DeviceAdminReceiver;
import com.afwsamples.testdpc.R;
import java.util.Collection;
import java.util.List;
import java.util.Set;

/**
 * Allows the user to see / edit / delete affiliation ids.
 * See {@link DevicePolicyManager#setAffiliationIds(ComponentName, Set)}
 */
public class ManageAffiliationIdsFragment extends BaseStringItemsFragment {

    private DevicePolicyManager mDevicePolicyManager;
    private ComponentName mAdminComponent;

    public ManageAffiliationIdsFragment() {
        super(R.string.manage_affiliation_ids, R.string.enter_affiliation_id,
                R.string.affiliation_id_empty_error);
    }

    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

        mDevicePolicyManager = (DevicePolicyManager) getActivity().getSystemService(
                Context.DEVICE_POLICY_SERVICE);
        mAdminComponent = DeviceAdminReceiver.getComponentName(getActivity());
    }

    @TargetApi(VERSION_CODES.O)
    @Override
    protected Collection<String> loadItems() {
        return mDevicePolicyManager.getAffiliationIds(mAdminComponent);
    }

    @TargetApi(VERSION_CODES.O)
    @Override
    protected void saveItems(List<String> items) {
        mDevicePolicyManager.setAffiliationIds(mAdminComponent, new ArraySet<>(items));
    }

}
