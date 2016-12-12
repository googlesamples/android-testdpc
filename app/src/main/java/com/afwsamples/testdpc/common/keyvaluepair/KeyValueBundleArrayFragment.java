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

package com.afwsamples.testdpc.common.keyvaluepair;

import android.app.Activity;
import android.content.Context;
import android.content.Intent;
import android.content.pm.ApplicationInfo;
import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.BaseAdapter;

import com.afwsamples.testdpc.R;
import com.afwsamples.testdpc.common.EditDeleteArrayAdapter;
import com.afwsamples.testdpc.common.ManageAppFragment;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;

import static com.afwsamples.testdpc.common.EditDeleteArrayAdapter.OnDeleteButtonClickListener;
import static com.afwsamples.testdpc.common.EditDeleteArrayAdapter.OnEditButtonClickListener;
import static com.afwsamples.testdpc.common.keyvaluepair.KeyValuePairDialogFragment.ARG_APP_NAME;
import static com.afwsamples.testdpc.common.keyvaluepair.KeyValuePairDialogFragment
        .ARG_INITIAL_VALUE;
import static com.afwsamples.testdpc.common.keyvaluepair.KeyValuePairDialogFragment.ARG_KEY;
import static com.afwsamples.testdpc.common.keyvaluepair.KeyValuePairDialogFragment.DialogType;
import static com.afwsamples.testdpc.common.keyvaluepair.KeyValuePairDialogFragment.RESULT_KEY;
import static com.afwsamples.testdpc.common.keyvaluepair.KeyValuePairDialogFragment.RESULT_TYPE;
import static com.afwsamples.testdpc.common.keyvaluepair.KeyValuePairDialogFragment.RESULT_VALUE;

public class KeyValueBundleArrayFragment extends ManageAppFragment implements
        OnEditButtonClickListener<Bundle>, OnDeleteButtonClickListener<Bundle> {
    /**
     * Key of the editing bundle array.
     */
    private String mKey;
    /**
     * The current bundle list.
     */
    private List<Bundle> mBundleList;
    /**
     * The initial value of the passed in bundle list.
     */
    private List<Bundle> mInitialBundleList;
    private String mAppName;
    private BundleEditDeleteArrayAdapter mAdapter;

    private static final int RESULT_CODE_EDIT_DIALOG = 1;

    private static final int[] SUPPORTED_TYPE = {
            DialogType.BUNDLE_TYPE,
    };

    /**
     * @param key key of the bundle. Can be null if not needed.
     * @param bundles initial value of the bundle array for editing.
     */
    public static KeyValueBundleArrayFragment newInstance(String key, Bundle[] bundles,
            String appName) {
        Bundle arguments = new Bundle();
        arguments.putString(ARG_KEY, key);
        arguments.putParcelableArray(ARG_INITIAL_VALUE, bundles);
        arguments.putString(ARG_APP_NAME, appName);
        KeyValueBundleArrayFragment fragment = new KeyValueBundleArrayFragment();
        fragment.setArguments(arguments);
        return fragment;
    }

    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        mKey = getArguments().getString(ARG_KEY);
        Bundle[] bundles = (Bundle[]) getArguments().getParcelableArray(ARG_INITIAL_VALUE);
        if (bundles == null) {
            bundles = new Bundle[0];
        }
        mBundleList = new ArrayList<>(Arrays.asList(bundles));
        mInitialBundleList = new ArrayList<>(mBundleList);
        mAppName = getArguments().getString(ARG_APP_NAME);
    }

    @Override
    public View onCreateView(LayoutInflater layoutInflater, ViewGroup container,
            Bundle savedInstanceState) {
        View view = super.onCreateView(layoutInflater, container, savedInstanceState);
        mManagedAppsSpinner.setVisibility(View.GONE);
        // header text
        mHeaderView.setVisibility(View.VISIBLE);
        mHeaderView.setText(getActivity().getString(
                R.string.app_restrictions_info, mAppName, mKey));
        return view;
    }

    @Override
    protected void onSpinnerItemSelected(ApplicationInfo appInfo) {}

    @Override
    protected void resetConfig() {
        mAdapter.clear();
        mAdapter.addAll(mInitialBundleList);
    }

    @Override
    protected void saveConfig() {
        Intent intent = new Intent();
        intent.putExtra(RESULT_KEY, mKey);
        intent.putExtra(RESULT_TYPE, DialogType.BUNDLE_ARRAY_TYPE);
        intent.putExtra(RESULT_VALUE, mBundleList.toArray(new Bundle[0]));
        getTargetFragment().onActivityResult(getTargetRequestCode(), Activity.RESULT_OK, intent);
        getFragmentManager().popBackStack();
    }

    @Override
    protected void addNewRow() {
        Bundle bundle = null;
        // Use same bundle structure as first bundle, if exists
        // In case of empty bundle user will need to add all restrictions manually
        if (mInitialBundleList != null && mInitialBundleList.size() > 0 && mInitialBundleList.get(0) != null) {
            bundle = (Bundle) mInitialBundleList.get(0).clone();
        } else {
            bundle = new Bundle();
        }
        mAdapter.add(bundle);
        showEditDialog(bundle);
    }

    @Override
    protected void loadDefault() {}

    @Override
    protected BaseAdapter createListAdapter() {
        mAdapter = new BundleEditDeleteArrayAdapter(getActivity(), mBundleList, this, this);
        return mAdapter;
    }

    private void showEditDialog(final Bundle bundle) {
        int type = DialogType.BUNDLE_TYPE;
        int index = mBundleList.indexOf(bundle);
        KeyValuePairDialogFragment dialogFragment =
                KeyValuePairDialogFragment.newInstance(type, false, "Bundle #" + index,
                        bundle, SUPPORTED_TYPE, mAppName);
        dialogFragment.setTargetFragment(this, RESULT_CODE_EDIT_DIALOG);
        dialogFragment.show(getFragmentManager(), "dialog");
    }

    @Override
    public void onEditButtonClick(Bundle bundle) {
        showEditDialog(bundle);
    }

    @Override
    public void onDeleteButtonClick(Bundle bundle) {
        mBundleList.remove(bundle);
    }

    @Override
    public void onActivityResult(int requestCode, int resultCode, Intent result) {
        if (resultCode != Activity.RESULT_OK) {
            return;
        }
        if (requestCode == RESULT_CODE_EDIT_DIALOG) {
            Bundle value = result.getBundleExtra(RESULT_VALUE);
            int index = mBundleList.indexOf(value);
            mAdapter.set(index, value);
        }
    }

    private class BundleEditDeleteArrayAdapter extends EditDeleteArrayAdapter<Bundle> {

        public BundleEditDeleteArrayAdapter(Context context, List<Bundle> entries,
                OnEditButtonClickListener onEditButtonClickListener,
                OnDeleteButtonClickListener onDeleteButtonClickListener) {
            super(context, entries, onEditButtonClickListener, onDeleteButtonClickListener);
        }

        @Override
        protected String getDisplayName(Bundle entry) {
            return String.valueOf("Bundle #" + mBundleList.indexOf(entry));
        }
    }
}
