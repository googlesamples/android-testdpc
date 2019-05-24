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
import android.content.Context;
import android.content.DialogInterface;
import android.content.pm.ApplicationInfo;
import android.content.pm.PackageManager;
import android.content.pm.ResolveInfo;
import android.os.Build.VERSION_CODES;
import android.util.Log;
import android.widget.Toast;
import com.afwsamples.testdpc.DeviceAdminReceiver;
import com.afwsamples.testdpc.R;
import com.afwsamples.testdpc.common.ToggleComponentsArrayAdapter;
import java.util.ArrayList;
import java.util.List;

public class MeteredDataRestrictionInfoAdapter extends ToggleComponentsArrayAdapter
        implements DialogInterface.OnClickListener {
    private static final String TAG = MeteredDataRestrictionInfoAdapter.class.getSimpleName();

    private final Context mContext;
    private final List<String> mRestrictedPkgs;

    public MeteredDataRestrictionInfoAdapter(Context context, List<ResolveInfo> resolveInfos,
            List<String> restrictedPkgs) {
        super(context, R.id.pkg_name, resolveInfos);
        mContext = context;
        mRestrictedPkgs = restrictedPkgs;
        setIsComponentEnabledList(createIsComponentEnabledList());
    }

    @Override
    public boolean isComponentEnabled(ResolveInfo resolveInfo) {
        return mRestrictedPkgs.contains(resolveInfo.resolvePackageName);
    }

    @Override
    protected ApplicationInfo getApplicationInfo(int position) {
        try {
            return mPackageManager.getApplicationInfo(getItem(position).resolvePackageName,
                    0 /* Default flags */);
        } catch (PackageManager.NameNotFoundException e) {
            Log.e(TAG, "getApplicationInfo: ", e);
        }
        return null;
    }

    private List<Boolean> createIsComponentEnabledList() {
        final List<Boolean> isComponentEnabledList = new ArrayList<>();
        int size = getCount();
        for (int i = 0; i < size; ++i) {
            isComponentEnabledList.add(isComponentEnabled(getItem(i)));
        }
        return isComponentEnabledList;
    }

    @Override
    protected boolean canModifyComponent(int position) {
        return true;
    }

    @Override
    public CharSequence getDisplayName(int position) {
        return mPackageManager.getApplicationLabel(getApplicationInfo(position));
    }

    @Override
    public void onClick(DialogInterface dialog, int which) {
        if (which == DialogInterface.BUTTON_POSITIVE) {
            final List<String> pkgNames = new ArrayList<>();
            final int size = mIsComponentCheckedList.size();
            for (int i = 0; i < size; ++i) {
                if (mIsComponentCheckedList.get(i)) {
                    pkgNames.add(getItem(i).resolvePackageName);
                }
            }
            setMeteredDataRestrictedPkgs(pkgNames);
        }
    }

    @TargetApi(VERSION_CODES.P)
    private void setMeteredDataRestrictedPkgs(List<String> pkgNames) {
        final List<String> excludedPkgs = mDevicePolicyManager.setMeteredDataDisabledPackages(
            DeviceAdminReceiver.getComponentName(mContext), pkgNames);

        if (!excludedPkgs.isEmpty()) {
            Toast.makeText(mContext, mContext.getString(
                R.string.metered_data_restriction_failed_pkgs, excludedPkgs),
                Toast.LENGTH_LONG).show();
        }
    }
}
