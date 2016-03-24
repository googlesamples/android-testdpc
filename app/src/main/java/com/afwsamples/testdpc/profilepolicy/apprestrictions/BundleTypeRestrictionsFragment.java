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

package com.afwsamples.testdpc.profilepolicy.apprestrictions;

import android.annotation.TargetApi;
import android.content.RestrictionEntry;
import android.os.Build;
import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ListView;
import android.widget.TextView;

import com.afwsamples.testdpc.R;

/**
 * This fragment shows nested restrictions of Bundle and Bundle array type restrictions.
 */
@TargetApi(Build.VERSION_CODES.M)
public class BundleTypeRestrictionsFragment extends BaseAppRestrictionsFragment {
    private static final String ARG_RESTRICTION_ENTRY = "argRestrictionEntry";
    private static final String ARG_RESTRICTION_POSITION = "argRestrictionPosition";
    private static final String ARG_FRAGMENT_TITLE = "argFragmentTitle";
    private static final String ARG_APP_NAME = "argAppName";

    private RestrictionEntry mRestrictionEntry;
    private int mRestrictionPosition;
    private String mFragmentTitle;
    private String mAppName;

    public static BundleTypeRestrictionsFragment newInstance(RestrictionEntry restrictionEntry,
            int restrictionPosition, String fragmentTitle, String appName) {
        Bundle args = new Bundle();
        args.putParcelable(ARG_RESTRICTION_ENTRY, restrictionEntry);
        args.putInt(ARG_RESTRICTION_POSITION, restrictionPosition);
        args.putString(ARG_FRAGMENT_TITLE, fragmentTitle);
        args.putString(ARG_APP_NAME, appName);

        BundleTypeRestrictionsFragment fragment = new BundleTypeRestrictionsFragment();
        fragment.setArguments(args);
        return fragment;
    }

    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        mRestrictionEntry = getArguments().getParcelable(ARG_RESTRICTION_ENTRY);
        mRestrictionPosition = getArguments().getInt(ARG_RESTRICTION_POSITION);
        mFragmentTitle = getArguments().getString(ARG_FRAGMENT_TITLE);
        mAppName = getArguments().getString(ARG_APP_NAME);
    }

    @Override
    public void onResume() {
        super.onResume();
        int resId;
        if (mRestrictionEntry.getType() == RestrictionEntry.TYPE_BUNDLE) {
            resId = R.string.define_bundle;
        } else {
            resId = R.string.define_bundle_array;
        }
        setActionBarTitleResId(resId);
        getActivity().getActionBar().setTitle(resId);
    }

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container,
            Bundle savedInstanceState) {
        View view = inflater.inflate(R.layout.manage_apps, null);

        mAppListView = (ListView) view.findViewById(R.id.app_list_view);
        view.findViewById(R.id.managed_apps_list).setVisibility(View.GONE);
        view.findViewById(R.id.header_text).setVisibility(View.VISIBLE);
        ((TextView) view.findViewById(R.id.header_text)).setText(getActivity().getString(
                R.string.app_restrictions_info, mAppName, mFragmentTitle));
        updateViewVisibilities(view);

        loadAppRestrictionsList(mRestrictionEntry.getRestrictions());

        return view;
    }

    @Override
    protected String getCurrentAppName() {
        return mAppName;
    }

    @Override
    public void onClick(View v) {
        switch (v.getId()) {
            case R.id.save_app:
                final BaseAppRestrictionsFragment parentFragment =
                        (BaseAppRestrictionsFragment) getTargetFragment();
                parentFragment.saveNestedRestrictions(mRestrictionPosition,
                        mRestrictionEntries);
                getActivity().getFragmentManager().popBackStack();
                break;
            default:
                super.onClick(v);
        }
    }

    @Override
    public void onDestroy() {
        super.onDestroy();
        resetActionBarTitle();
    }

    private void resetActionBarTitle() {
        getActivity().getActionBar().setTitle(
                ((BaseAppRestrictionsFragment) getTargetFragment()).getActionBarTitleResId());
    }
}
