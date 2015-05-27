/*
 * Copyright 2015 Google Inc. All Rights Reserved.
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

package com.google.android.testdpc.profilepolicy.crossprofilewidgetprovider;

import android.app.admin.DevicePolicyManager;
import android.appwidget.AppWidgetManager;
import android.appwidget.AppWidgetProviderInfo;
import android.content.Context;

import com.google.android.testdpc.DeviceAdminReceiver;

import java.util.ArrayList;
import java.util.HashSet;
import java.util.List;
import java.util.Set;

/**
 * Builds the cross-profile widget providers list.
 */
public class ManageCrossProfileWidgetProviderUtil {

    private static ManageCrossProfileWidgetProviderUtil sInstance;

    private Context mContext;

    private AppWidgetManager mAppWidgetManager;

    private DevicePolicyManager mDevicePolicyManager;

    private ManageCrossProfileWidgetProviderUtil(Context context) {
        mContext = context.getApplicationContext();
        mAppWidgetManager = AppWidgetManager.getInstance(mContext);
        mDevicePolicyManager = (DevicePolicyManager) mContext.getSystemService(
                Context.DEVICE_POLICY_SERVICE);
    }

    public static synchronized ManageCrossProfileWidgetProviderUtil getInstance(Context context) {
        if (sInstance == null) {
            sInstance = new ManageCrossProfileWidgetProviderUtil(context);
        }
        return sInstance;
    }

    /**
     * Gets a list of work profile apps that have disabled cross-profile widget providers.
     *
     * @return A list of package names which have disabled cross-profile widget providers.
     */
    public List<String> getDisabledCrossProfileWidgetProvidersList() {
        Set<String> enabledCrossProfileWidgetProvidersList = new HashSet<String>(
                mDevicePolicyManager.getCrossProfileWidgetProviders(
                        DeviceAdminReceiver.getComponentName(mContext)));
        // Cross-profile widgets are enabled by their package name. Use set to avoid duplicates.
        Set<String> disabledCrossProfileWidgetProvidersPackageNameSet = new HashSet<String>();
        List<AppWidgetProviderInfo> appWidgetProviderInfoList = mAppWidgetManager
                .getInstalledProviders();
        for (AppWidgetProviderInfo appWidgetProviderInfo : appWidgetProviderInfoList) {
            String appWidgetProviderPackage = appWidgetProviderInfo.provider.getPackageName();
            if (!enabledCrossProfileWidgetProvidersList.contains(appWidgetProviderPackage)) {
                disabledCrossProfileWidgetProvidersPackageNameSet.add(appWidgetProviderPackage);
            }
        }
        return new ArrayList<String>(disabledCrossProfileWidgetProvidersPackageNameSet);
    }
}