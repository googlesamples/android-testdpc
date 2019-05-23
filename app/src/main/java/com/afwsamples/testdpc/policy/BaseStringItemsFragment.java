/*
 * Copyright (C) 2017 The Android Open Source Project
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
import android.app.AlertDialog;
import android.content.Context;
import android.os.Build.VERSION_CODES;
import android.os.Bundle;
import android.support.annotation.StringRes;
import android.text.TextUtils;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.BaseAdapter;
import android.widget.EditText;
import android.widget.SpinnerAdapter;
import android.widget.Toast;
import com.afwsamples.testdpc.R;
import com.afwsamples.testdpc.common.BaseManageComponentFragment;
import com.afwsamples.testdpc.common.EditDeleteArrayAdapter;
import java.util.ArrayList;
import java.util.Collection;
import java.util.List;

/**
 * Base fragment for allowing he user to see / edit / delete a list of strings.
 */
public abstract class BaseStringItemsFragment extends BaseManageComponentFragment<Void>
        implements EditDeleteArrayAdapter.OnEditButtonClickListener<String> {

    private final int mFragmentTitleResId;
    private final int mDialogTitleResId;
    private final int mEmptyItemResId;

    private List<String> mItems = new ArrayList<>();
    private List<String> mLastItems = new ArrayList<>();
    private EditDeleteArrayAdapter<String> mItemArrayAdapter;

    public BaseStringItemsFragment(int fragmentTitleResId, int dialogTitleResId,
            int emptyItemResId) {
        mFragmentTitleResId = fragmentTitleResId;
        mDialogTitleResId = dialogTitleResId;
        mEmptyItemResId = emptyItemResId;
    }

    @Override
    public View onCreateView(LayoutInflater layoutInflater, ViewGroup container,
            Bundle savedInstanceState) {
        View view = super.onCreateView(layoutInflater, container, savedInstanceState);
        mManagedAppsSpinner.setVisibility(View.INVISIBLE);  // We don't need the Spinner.

        loadDefault();
        return view;
    }

    @Override
    public void onResume() {
        super.onResume();
        getActivity().getActionBar().setTitle(mFragmentTitleResId);
    }

    @Override
    protected SpinnerAdapter createSpinnerAdapter() {
        return null;  // We don't need a spinner.
    }

    @Override
    protected void onSpinnerItemSelected(Void item) {
    }

    @Override
    protected BaseAdapter createListAdapter() {
        mItemArrayAdapter = new ItemEntryArrayAdapter(
                getActivity(), mItems, this);
        return mItemArrayAdapter;
    }

    @Override
    protected void resetConfig() {
        mItemArrayAdapter.clear();
        mItemArrayAdapter.addAll(mLastItems);
    }

    @Override
    @TargetApi(VERSION_CODES.O)
    protected void saveConfig() {
        saveItems(mItems);
        mLastItems = new ArrayList<>(mItems);
    }

    @Override
    protected void addNewRow() {
        onEditButtonClick(null);
    }

    @Override
    @TargetApi(VERSION_CODES.O)
    protected void loadDefault() {
        mItemArrayAdapter.clear();
        mItemArrayAdapter.addAll(loadItems());
        mLastItems = new ArrayList<>(mItems);
    }

    @Override
    public void onEditButtonClick(final String existingEntry) {
        View view = LayoutInflater.from(getActivity()).inflate(R.layout.simple_edittext, null);
        final EditText input = (EditText) view.findViewById(R.id.input);
        if (existingEntry != null) {
            input.setText(existingEntry);
        }

        final AlertDialog dialog = new AlertDialog.Builder(getActivity())
                .setTitle(mDialogTitleResId)
                .setView(view)
                .setPositiveButton(android.R.string.ok, null)
                .setNegativeButton(android.R.string.cancel, null)
                .create();
        dialog.setOnShowListener(
                dialogInterface -> dialog.getButton(AlertDialog.BUTTON_POSITIVE).setOnClickListener(
                        okButtonView -> {
                            String item = input.getText().toString();
                            if (TextUtils.isEmpty(item)) {
                                showToast(mEmptyItemResId);
                                return;
                            }
                            if (existingEntry != null) {
                                mItemArrayAdapter.remove(existingEntry);
                            }
                            mItemArrayAdapter.add(item);
                            dialog.dismiss();
                        }));
        dialog.show();
    }

    protected abstract void saveItems(List<String> items);

    protected abstract Collection<String> loadItems();

    private void showToast(@StringRes int stringResId) {
        Toast.makeText(getActivity(), stringResId, Toast.LENGTH_LONG).show();
    }

    static class ItemEntryArrayAdapter extends EditDeleteArrayAdapter<String> {
        ItemEntryArrayAdapter(
                Context context,
                List<String> entries,
                OnEditButtonClickListener onEditButtonClickListener) {
            super(context, entries, onEditButtonClickListener, null);
        }

        @Override
        protected String getDisplayName(String entry) {
            return entry;
        }
    }
}
