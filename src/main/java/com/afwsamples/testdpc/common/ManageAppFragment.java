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
import android.content.Context;
import android.content.RestrictionEntry;
import android.content.RestrictionsManager;
import android.widget.SpinnerAdapter;
import com.afwsamples.testdpc.R;
import java.util.ArrayList;
import java.util.Collections;
import java.util.HashSet;
import java.util.List;
import java.util.Set;

public abstract class ManageAppFragment extends BaseManageComponentFragment<ApplicationInfo> {
  /** List of packages always shown in the app list. */
  private static final Set<String> ALLOWLISTED_APPS = new HashSet<>();

  static {
    // GmsCore
    ALLOWLISTED_APPS.add("com.google.android.gms");
  }

  @Override
  protected SpinnerAdapter createSpinnerAdapter() {
    List<ApplicationInfo> managedAppList = getInstalledOrLaunchableApps();
    Collections.sort(managedAppList, new ApplicationInfo.DisplayNameComparator(mPackageManager));
    return new AppInfoSpinnerAdapter(
        getActivity(), R.layout.app_row, R.id.pkg_name, managedAppList);
  }

  /**
   * Additionally filter apps returned in the list, return {@code true} to keep the app in the list,
   * {@code false} to exclude it.
   */
  protected boolean filterApp(ApplicationInfo info) {
    return true;
  }

  private List<ApplicationInfo> getInstalledOrLaunchableApps() {
    List<ApplicationInfo> installedApps =
        mPackageManager.getInstalledApplications(0 /* Default flags */);
    List<ApplicationInfo> filteredAppList = new ArrayList<>();
    for (ApplicationInfo applicationInfo : installedApps) {
      if (mPackageManager.getLaunchIntentForPackage(applicationInfo.packageName) != null
          || (applicationInfo.flags & ApplicationInfo.FLAG_SYSTEM) == 0
          || ALLOWLISTED_APPS.contains(applicationInfo.packageName)) {
        if (filterApp(applicationInfo)) {
          filteredAppList.add(applicationInfo);
        }
      }
      // Adding system apps with Restrictions and without LaunchIntentForPackage
      if (!filteredAppList.contains(applicationInfo) &&
              filterApp(applicationInfo)) {
        try {
          List<RestrictionEntry> manifestRestrictions = null;
          RestrictionsManager mRestrictionsManager =
                  (RestrictionsManager) getActivity().getSystemService(Context.RESTRICTIONS_SERVICE);
          manifestRestrictions = mRestrictionsManager.getManifestRestrictions(applicationInfo.packageName);
          if (manifestRestrictions != null && !manifestRestrictions.isEmpty()) {
            filteredAppList.add(applicationInfo);
          }
        } catch (NullPointerException e) {
          // This means no default restrictions.
        }
      }
    }
    return filteredAppList;
  }
}
