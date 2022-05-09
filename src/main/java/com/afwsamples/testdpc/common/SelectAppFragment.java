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

import android.app.Fragment;
import android.content.pm.ApplicationInfo;
import android.content.pm.PackageManager;
import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.AdapterView;
import android.widget.AdapterView.OnItemClickListener;
import android.widget.EditText;
import android.widget.ListView;
import com.afwsamples.testdpc.R;
import java.util.ArrayList;
import java.util.Collections;
import java.util.List;

/**
 * This fragment shows the list of apps, and allows the user to select one of them to perform a
 * certain operation as defined by {@link #setSelectedPackage}. Alternatively the user can manually
 * specify an app's package name, in case the app has not been installed yet.
 */
public abstract class SelectAppFragment extends Fragment
    implements View.OnClickListener, OnItemClickListener {

  private EditText mCurrentSelectedPackage;
  private EditText mNewSelectedPackage;
  private ListView mAppListView;
  private List<String> mAppPackages;

  @Override
  public void onResume() {
    super.onResume();
    reloadSelectedPackage();
  }

  @Override
  public void onCreate(Bundle savedInstanceState) {
    super.onCreate(savedInstanceState);
    mAppPackages = createAppList();
  }

  @Override
  public View onCreateView(
      LayoutInflater layoutInflater, ViewGroup container, Bundle savedInstanceState) {
    View view = layoutInflater.inflate(R.layout.select_app, null);

    mCurrentSelectedPackage = view.findViewById(R.id.selected_package_current);
    mNewSelectedPackage = view.findViewById(R.id.selected_package_new);
    mAppListView = view.findViewById(R.id.select_app_list);
    AppInfoArrayAdapter appInfoArrayAdapter =
        new AppInfoArrayAdapter(getActivity(), R.id.pkg_name, mAppPackages, true);
    mAppListView.setAdapter(appInfoArrayAdapter);
    view.findViewById(R.id.selected_package_set).setOnClickListener(this);
    view.findViewById(R.id.selected_package_clear).setOnClickListener(this);
    mAppListView.setOnItemClickListener(this);
    return view;
  }

  /** @return a list of apps that users are allowed to select from. */
  protected List<String> createAppList() {
    List<String> appList = new ArrayList<>();
    PackageManager pm = getActivity().getPackageManager();
    List<ApplicationInfo> allApps = pm.getInstalledApplications(0 /* No flag */);
    Collections.sort(allApps, new ApplicationInfo.DisplayNameComparator(pm));
    for (ApplicationInfo info : allApps) {
      if ((pm.getLaunchIntentForPackage(info.packageName)) != null) {
        appList.add(info.packageName);
      }
    }
    return appList;
  }

  protected ViewGroup getExtensionLayout(View rootView) {
    return rootView.findViewById(R.id.extension);
  }

  @Override
  public void onClick(View v) {
    int id = v.getId();
    if (id == R.id.selected_package_set) {
      setSelectedPackage(mNewSelectedPackage.getText().toString());
      reloadSelectedPackage();
    } else if (id == R.id.selected_package_clear) {
      mNewSelectedPackage.setText("");
      clearSelectedPackage();
      reloadSelectedPackage();
    }
  }

  protected void reloadSelectedPackage() {
    String selectedPackage = getSelectedPackage();
    if (selectedPackage == null) {
      mCurrentSelectedPackage.setText("");
    } else {
      mCurrentSelectedPackage.setText(selectedPackage);
    }
  }

  @Override
  public void onItemClick(AdapterView<?> parent, View view, int position, long id) {
    String packageName = mAppPackages.get(position);
    mNewSelectedPackage.setText(packageName);
  }

  /** Sets the given package name as the new selected package. */
  protected abstract void setSelectedPackage(String pkgName);

  /** Clears the currently selected package. */
  protected abstract void clearSelectedPackage();

  /**
   * Get the currently selected package, or {@code null} if no package has been set or if it has
   * been cleared.
   */
  protected abstract String getSelectedPackage();
}
