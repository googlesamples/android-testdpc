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
import android.content.pm.PackageManager;
import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.AdapterView;
import android.widget.BaseAdapter;
import android.widget.ListView;
import android.widget.Spinner;
import android.widget.SpinnerAdapter;
import android.widget.TextView;
import com.afwsamples.testdpc.R;

/** This fragment shows a spinner of all allowed component and a list of associated properties. */
public abstract class BaseManageComponentFragment<T> extends Fragment
    implements View.OnClickListener {

  protected PackageManager mPackageManager;
  protected Spinner mManagedAppsSpinner;
  protected TextView mHeaderView;
  protected ListView mAppListView;
  protected BaseAdapter mAdapter;

  @Override
  public void onResume() {
    super.onResume();
    getActivity().getActionBar().setTitle(R.string.manage_apps);
  }

  @Override
  public void onCreate(Bundle savedInstanceState) {
    super.onCreate(savedInstanceState);
    mPackageManager = getActivity().getPackageManager();
  }

  @Override
  public View onCreateView(
      LayoutInflater layoutInflater, ViewGroup container, Bundle savedInstanceState) {
    View view = layoutInflater.inflate(R.layout.manage_apps, null);

    mHeaderView = (TextView) view.findViewById(R.id.header_text);
    mManagedAppsSpinner = (Spinner) view.findViewById(R.id.managed_apps_list);
    mManagedAppsSpinner.setAdapter(createSpinnerAdapter());
    mManagedAppsSpinner.setOnItemSelectedListener(
        new AdapterView.OnItemSelectedListener() {
          @Override
          public void onItemSelected(AdapterView<?> parent, View view, int position, long id) {
            onSpinnerItemSelected(getSpinnerSelectedItem());
          }

          @Override
          public void onNothingSelected(AdapterView<?> parent) {
            // Do nothing.
          }
        });
    mAppListView = (ListView) view.findViewById(R.id.app_list_view);
    mAdapter = createListAdapter();
    mAppListView.setAdapter(mAdapter);
    view.findViewById(R.id.save_app).setOnClickListener(this);
    view.findViewById(R.id.reset_app).setOnClickListener(this);
    view.findViewById(R.id.add_new_row).setOnClickListener(this);
    view.findViewById(R.id.load_default_button).setOnClickListener(this);
    return view;
  }

  @Override
  public void onActivityCreated(Bundle savedInstanceState) {
    super.onActivityCreated(savedInstanceState);
    onSpinnerItemSelected(getSpinnerSelectedItem());
  }

  protected abstract SpinnerAdapter createSpinnerAdapter();

  protected abstract BaseAdapter createListAdapter();

  /**
   * Populates the adapter for app_list_view with data for this application.
   *
   * @param item the selected spinner item.
   */
  protected abstract void onSpinnerItemSelected(T item);

  @Override
  public void onClick(View view) {
    int id = view.getId();
    if (id == R.id.reset_app) {
      resetConfig();
    } else if (id == R.id.save_app) {
      saveConfig();
    } else if (id == R.id.add_new_row) {
      addNewRow();
    } else if (id == R.id.load_default_button) {
      loadDefault();
    }
  }

  protected abstract void resetConfig();

  protected abstract void saveConfig();

  protected abstract void addNewRow();

  protected abstract void loadDefault();

  @SuppressWarnings("unchecked")
  private T getSpinnerSelectedItem() {
    return (T) mManagedAppsSpinner.getSelectedItem();
  }
}
