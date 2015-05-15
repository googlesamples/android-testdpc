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

package com.google.android.testdpc.common;

import android.content.Context;
import android.content.pm.ApplicationInfo;
import android.content.pm.PackageManager;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ArrayAdapter;
import android.widget.ImageView;
import android.widget.TextView;

import com.google.android.testdpc.R;

import java.util.List;

/**
 * A simple adapter which takes a list of package names and shows their app icon and app name in a
 * listview.
 */
public class AppInfoArrayAdapter extends ArrayAdapter<String> {
    private PackageManager mPackageManager;
    private int mAppInfoFlags = 0;

    public AppInfoArrayAdapter(Context context, int resource, List<String> pkgNameList,
            boolean includeDisabledApps) {
        super(context, resource, pkgNameList);
        mPackageManager = getContext().getPackageManager();
        if (includeDisabledApps) {
            mAppInfoFlags = PackageManager.GET_UNINSTALLED_PACKAGES;
        }
    }

    public AppInfoArrayAdapter(Context context, int resource, List<String> pkgNameList) {
        this(context, resource, pkgNameList, false /* Don't include disabled apps */);
    }

    @Override
    public View getView(int position, View convertView, ViewGroup parent) {
        if (convertView == null) {
            convertView = LayoutInflater.from(getContext()).inflate(R.layout.app_row, parent,
                    false);
        }

        try {
            ApplicationInfo applicationInfo = mPackageManager.getApplicationInfo(getItem(position),
                    mAppInfoFlags);
            ImageView iconImageView = (ImageView) convertView.findViewById(R.id.pkg_icon);
            iconImageView.setImageDrawable(mPackageManager.getApplicationIcon(applicationInfo));
            TextView pkgNameTextView = (TextView) convertView.findViewById(R.id.pkg_name);
            pkgNameTextView.setText(mPackageManager.getApplicationLabel(applicationInfo));
        } catch (PackageManager.NameNotFoundException e) {
            e.printStackTrace();
            // Returns an empty view in case the package is not found.
            return new View(getContext());
        }
        return convertView;
    }
}
