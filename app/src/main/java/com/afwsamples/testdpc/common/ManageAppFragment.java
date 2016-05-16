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

package com.afwsamples.testdpc.common;

import android.content.pm.ApplicationInfo;
import android.widget.SpinnerAdapter;

import com.afwsamples.testdpc.R;

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;

public abstract class ManageAppFragment extends BaseManageComponentFragment<ApplicationInfo> {
    @Override
    protected SpinnerAdapter createSpinnerAdapter() {
        List<ApplicationInfo> managedAppList = getInstalledOrLaunchableApps();
        Collections.sort(managedAppList,
                new ApplicationInfo.DisplayNameComparator(mPackageManager));
        return new AppInfoSpinnerAdapter(getActivity(), R.layout.app_row, R.id.pkg_name,
                managedAppList);
    }


    private List<ApplicationInfo> getInstalledOrLaunchableApps() {
        List<ApplicationInfo> installedApps = mPackageManager.getInstalledApplications(
                0 /* Default flags */);
        List<ApplicationInfo> filteredAppList = new ArrayList<>();
        for (ApplicationInfo applicationInfo : installedApps) {
            if (mPackageManager.getLaunchIntentForPackage(applicationInfo.packageName) != null
                    || (applicationInfo.flags & ApplicationInfo.FLAG_SYSTEM) == 0) {
                filteredAppList.add(applicationInfo);
            }
        }
        return filteredAppList;
    }
}
