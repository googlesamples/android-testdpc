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

package com.afwsamples.testdpc.policy;

import android.app.Activity;
import android.app.AlertDialog;
import android.content.pm.PackageManager;
import android.content.pm.ResolveInfo;
import android.os.AsyncTask;
import android.view.View;
import android.widget.AdapterView;
import android.widget.Button;
import android.widget.ListView;
import com.afwsamples.testdpc.R;
import java.util.ArrayList;
import java.util.Collections;
import java.util.List;

public abstract class GetAvailableComponentsTask<T> extends AsyncTask<Void, Void, List<T>> {
  private final Activity mActivity;
  private final int mTitleResId;

  private View mProgressView;
  private ListView mListView;
  private AlertDialog mAlertDialog;
  private Button mPositiveButton;
  private Button mNegativeButton;
  private Button mNeutralButton;

  public GetAvailableComponentsTask(Activity activity, int titleResId) {
    mActivity = activity;
    mTitleResId = titleResId;
  }

  @Override
  protected void onPreExecute() {
    View rootView = View.inflate(mActivity, R.layout.available_components_list, null);
    mAlertDialog =
        new AlertDialog.Builder(mActivity)
            .setTitle(mTitleResId)
            .setView(rootView)
            .setPositiveButton(android.R.string.ok, null)
            .setNeutralButton(R.string.allow_all, null)
            .setNegativeButton(android.R.string.cancel, null)
            .show();
    mProgressView = rootView.findViewById(R.id.progress_layout);
    mListView = (ListView) rootView.findViewById(android.R.id.list);

    mPositiveButton = mAlertDialog.getButton(AlertDialog.BUTTON_POSITIVE);
    mNeutralButton = mAlertDialog.getButton(AlertDialog.BUTTON_NEUTRAL);
    mNegativeButton = mAlertDialog.getButton(AlertDialog.BUTTON_NEGATIVE);

    showProgressBar(true);
  }

  @Override
  protected void onPostExecute(List<T> availableComponentsList) {
    if (mActivity == null || mActivity.isFinishing()) {
      return;
    }
    final List<ResolveInfo> availableComponentsResolveInfoList =
        getResolveInfoListFromAvailableComponents(availableComponentsList);
    PackageManager packageManager = mActivity.getPackageManager();
    Collections.sort(
        availableComponentsResolveInfoList, new ResolveInfo.DisplayNameComparator(packageManager));
    final List<String> permittedComponentsList = getPermittedComponentsList();
    final AvailableComponentsInfoArrayAdapter availableComponentsInfoArrayAdapter =
        new AvailableComponentsInfoArrayAdapter(
            mActivity, availableComponentsResolveInfoList, permittedComponentsList);
    mListView.setAdapter(availableComponentsInfoArrayAdapter);
    mListView.setOnItemClickListener(
        new AdapterView.OnItemClickListener() {
          @Override
          public void onItemClick(AdapterView<?> parent, View view, int pos, long id) {
            availableComponentsInfoArrayAdapter.onItemClick(parent, view, pos, id);
          }
        });

    showProgressBar(false);
    mPositiveButton.setOnClickListener(
        new View.OnClickListener() {
          @Override
          public void onClick(View view) {
            ArrayList<String> selectedComponents =
                availableComponentsInfoArrayAdapter.getSelectedComponents();
            if (selectedComponents != null) {
              setPermittedComponentsList(selectedComponents);
            }
            mAlertDialog.dismiss();
          }
        });
    mNeutralButton.setOnClickListener(
        new View.OnClickListener() {
          @Override
          public void onClick(View view) {
            setPermittedComponentsList(null);
            mAlertDialog.dismiss();
          }
        });
  }

  private void showProgressBar(boolean show) {
    if (show) {
      mProgressView.setVisibility(View.VISIBLE);
      mListView.setVisibility(View.GONE);
    } else {
      mProgressView.setVisibility(View.GONE);
      mListView.setVisibility(View.VISIBLE);
    }
    mPositiveButton.setEnabled(!show);
    mNeutralButton.setEnabled(!show);
    mNegativeButton.setEnabled(!show);
  }

  protected abstract List<ResolveInfo> getResolveInfoListFromAvailableComponents(
      List<T> availableComponentsList);

  protected abstract List<String> getPermittedComponentsList();

  protected abstract void setPermittedComponentsList(List<String> permittedComponentsList);
}
