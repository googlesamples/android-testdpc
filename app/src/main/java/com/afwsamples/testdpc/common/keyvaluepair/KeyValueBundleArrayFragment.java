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
import android.content.RestrictionEntry;
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
import static com.afwsamples.testdpc.common.keyvaluepair.KeyValuePairDialogFragment
        .ARG_RESTRICTION_ENTRY;
import static com.afwsamples.testdpc.common.keyvaluepair.KeyValuePairDialogFragment.DialogType;
import static com.afwsamples.testdpc.common.keyvaluepair.KeyValuePairDialogFragment.RESULT_ENTRY;
import static com.afwsamples.testdpc.common.keyvaluepair.KeyValuePairDialogFragment.RESULT_KEY;
import static com.afwsamples.testdpc.common.keyvaluepair.KeyValuePairDialogFragment.RESULT_TYPE;
import static com.afwsamples.testdpc.common.keyvaluepair.KeyValuePairDialogFragment.RESULT_VALUE;

public class KeyValueBundleArrayFragment extends ManageAppFragment implements
        OnEditButtonClickListener<Integer>, OnDeleteButtonClickListener<Integer> {
    /**
     * Key of the editing bundle array.
     */
    private String mKey;
    /**
     * The current bundle list.
     */
    private ArrayList<Object> mBundleList;
    /**
     * The initial value of the passed in bundle list.
     */
    private ArrayList<Object> mInitialBundleList;
    private String mAppName;
    private EditDeleteArrayAdapter mAdapter;
    private RestrictionEntry mBundleArrayRestrictionEntry;
    private Integer mEditingItemNumber;
    List<Integer> mBundleNumbers;

    private static final int RESULT_CODE_EDIT_DIALOG = 1;

    private static final int[] SUPPORTED_TYPE = {
            DialogType.BUNDLE_TYPE
    };

    /**
     * @param key key of the bundle. Can be null if not needed.
     * @param bundles initial value of the bundle array for editing.
     */
    public static KeyValueBundleArrayFragment newInstance(String key, Bundle[] bundles,
            RestrictionEntry entry, String appName) {
        Bundle arguments = new Bundle();
        arguments.putString(ARG_KEY, key);
        arguments.putParcelableArray(ARG_INITIAL_VALUE, bundles);
        arguments.putParcelable(ARG_RESTRICTION_ENTRY, entry);
        arguments.putString(ARG_APP_NAME, appName);
        KeyValueBundleArrayFragment fragment = new KeyValueBundleArrayFragment();
        fragment.setArguments(arguments);
        return fragment;
    }

    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        mKey = getArguments().getString(ARG_KEY);
        Object[] bundles = getArguments().getParcelableArray(ARG_INITIAL_VALUE);
        if (bundles == null) {
            bundles = new Bundle[0];
        }
        mBundleArrayRestrictionEntry = (RestrictionEntry) getArguments().getParcelable(
                ARG_RESTRICTION_ENTRY);

        if (mBundleArrayRestrictionEntry != null) {
            // If we received RestrictionEntry with wrong type, we will make new empty BundleArray
            // restriction with same key, title and description
            if (mBundleArrayRestrictionEntry.getType() != RestrictionEntry.TYPE_BUNDLE_ARRAY) {
                RestrictionEntry newBundleEntry = KeyValueUtil.createBundleArrayRestriction(
                        mKey, new RestrictionEntry[0]);
                if (newBundleEntry != null) {
                    newBundleEntry.setTitle(mBundleArrayRestrictionEntry.getTitle());
                    newBundleEntry.setDescription(mBundleArrayRestrictionEntry.getDescription());
                }
                mBundleArrayRestrictionEntry = newBundleEntry;
            }
            RestrictionEntry[] entries = KeyValueUtil.getRestrictionEntries(
                    mBundleArrayRestrictionEntry);
            if (entries == null){
                entries = new RestrictionEntry[0];
            }
            mBundleList = new ArrayList<>(Arrays.asList((Object[])entries));
            mInitialBundleList = KeyValueUtil.cloneRestrictionsList(mBundleList);
        } else {
            mBundleList = new ArrayList<>(Arrays.asList(bundles));
            mInitialBundleList = new ArrayList<>(mBundleList);
        }
        mBundleNumbers = getBundleNumbers(mBundleList);
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
        mBundleList.clear();
        mBundleList = new ArrayList<>(mInitialBundleList);
        mBundleNumbers = getBundleNumbers(mInitialBundleList);
        mAdapter.addAll(mBundleNumbers);
    }

    @Override
    protected void saveConfig() {
        Intent intent = new Intent();
        intent.putExtra(RESULT_KEY, mKey);
        intent.putExtra(RESULT_TYPE, DialogType.BUNDLE_ARRAY_TYPE);
        if (mBundleArrayRestrictionEntry != null) {
            KeyValueUtil.setRestrictionEntries(mBundleArrayRestrictionEntry,
                    mBundleList.toArray(new RestrictionEntry[0]));
            intent.putExtra(RESULT_ENTRY, mBundleArrayRestrictionEntry);
        } else  {
            intent.putExtra(RESULT_VALUE, mBundleList.toArray(new Bundle[0]));
        }
        getTargetFragment().onActivityResult(getTargetRequestCode(), Activity.RESULT_OK, intent);
        getFragmentManager().popBackStack();
    }

    @Override
    protected void addNewRow() {
        // Use same bundle structure as first bundle, if exists
        // In case of empty bundle user will need to add all restrictions manually
        if (mInitialBundleList.size() > 0
                && mInitialBundleList.get(0) != null) {
            if (mBundleArrayRestrictionEntry != null) {
                RestrictionEntry newEntry = KeyValueUtil.cloneRestriction(
                        (RestrictionEntry) mInitialBundleList.get(0));
                mBundleList.add(newEntry);
            } else {
                Bundle bundle = (Bundle)((Bundle) mInitialBundleList.get(0)).clone();
                mBundleList.add(bundle);
            }
        } else {
            Bundle bundle = null;
            if (mBundleArrayRestrictionEntry != null) {
                RestrictionEntry newEntry = KeyValueUtil.createBundleRestriction(
                        mBundleArrayRestrictionEntry.getKey(), new RestrictionEntry[0]);
                mBundleList.add(newEntry);
            } else {
                bundle = new Bundle();
                mBundleList.add(bundle);
            }
        }
        mEditingItemNumber = mBundleList.size()-1;
        mAdapter.add(mEditingItemNumber);
        showEditDialog(mEditingItemNumber);
    }

    @Override
    protected void loadDefault() {}

    @Override
    protected BaseAdapter createListAdapter() {
            mAdapter = new BundleEditDeleteArrayAdapter(getActivity(), mBundleNumbers, this, this);
        return mAdapter;
    }

    private void showEditDialog(final int itemNumber) {
        int type = DialogType.BUNDLE_TYPE;
        Bundle bundle = null;
        RestrictionEntry editingEntry = null;
        mEditingItemNumber = itemNumber;
        if (itemNumber < mBundleList.size()) {
            if (mBundleArrayRestrictionEntry != null) {
                editingEntry = (RestrictionEntry) mBundleList.get(itemNumber);
            } else {
                bundle = (Bundle) mBundleList.get(itemNumber);
            }
            KeyValuePairDialogFragment dialogFragment =
                    KeyValuePairDialogFragment.newInstance(type, false, "Bundle #" + itemNumber,
                            bundle, editingEntry, SUPPORTED_TYPE, mAppName);
            dialogFragment.setTargetFragment(this, RESULT_CODE_EDIT_DIALOG);
            dialogFragment.show(getFragmentManager(), "dialog");
        }
    }

    @Override
    public void onEditButtonClick(Integer entryNumber) {
        showEditDialog(entryNumber);
    }

    @Override
    public void onDeleteButtonClick(Integer entryNumber) {
        mAdapter.clear();
        mBundleList.remove((int)entryNumber);
        mBundleNumbers = getBundleNumbers(mBundleList);
        mAdapter.addAll(mBundleNumbers);
    }

    @Override
    public void onActivityResult(int requestCode, int resultCode, Intent result) {
        if (resultCode != Activity.RESULT_OK) {
            return;
        }
        if (requestCode == RESULT_CODE_EDIT_DIALOG) {
            Bundle value = result.getBundleExtra(RESULT_VALUE);
            RestrictionEntry entry = result.getParcelableExtra(RESULT_ENTRY);
            if (entry != null) {
                mBundleList.set(mEditingItemNumber, entry);
            } else {
                mBundleList.set(mEditingItemNumber, value);
            }
            mAdapter.set(mEditingItemNumber, mEditingItemNumber);
            mEditingItemNumber = null;
        }
    }

    private class BundleEditDeleteArrayAdapter extends EditDeleteArrayAdapter<Integer> {

        public BundleEditDeleteArrayAdapter(Context context, List<Integer> entries,
                OnEditButtonClickListener onEditButtonClickListener,
                OnDeleteButtonClickListener onDeleteButtonClickListener) {
            super(context, entries, onEditButtonClickListener, onDeleteButtonClickListener);
        }

        @Override
        protected String getDisplayName(Integer entryNumber) {
            return String.valueOf("Bundle #" + entryNumber);
        }
    }

    private List<Integer> getBundleNumbers(List<Object> entries) {
        List<Integer> numbers = new ArrayList<>();
        if (entries != null) {
            for (int i=0; i<entries.size(); i++) {
                numbers.add(i);
            }
        }
        return numbers;
    }
}
