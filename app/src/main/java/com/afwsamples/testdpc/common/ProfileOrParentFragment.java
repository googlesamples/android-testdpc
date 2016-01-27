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
import android.app.admin.DevicePolicyManager;
import android.content.ComponentName;
import android.content.Context;
import android.os.Bundle;
import android.os.UserManager;
import android.support.v13.app.FragmentTabHost;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.TabHost;

import com.afwsamples.testdpc.DeviceAdminReceiver;
import com.afwsamples.testdpc.R;

/**
 * This fragment provides functionalities to show the same policy fragment for both the managed
 * user and its parent user in separate tabs, if applicable.
 *
 * If there is no parent user (for example, if the managed user is controlled by a Device Owner),
 * the fragment will be shown directly.
 */
public abstract class ProfileOrParentFragment extends Fragment {
    private static final String LOG_TAG = "ProfileOrParentFragment";

    private static final String EXTRA_PARENT_PROFILE = "com.afwsamples.testdpc.extra.PARENT";

    public abstract static class Container extends Fragment {
        @Override
        public void onCreate(Bundle savedInstanceState) {
            super.onCreate(savedInstanceState);

            final UserManager userManager = (UserManager)
                    getActivity().getSystemService(Context.USER_SERVICE);

            if (userManager.getUserProfiles().size() == 1) {
                // No need for a tabbed view if there's just one item.
                try {
                    getFragmentManager()
                            .popBackStack();
                    getFragmentManager()
                            .beginTransaction()
                            .replace(R.id.container, getContentClass().newInstance())
                            .addToBackStack(null)
                            .commit();
                } catch (Fragment.InstantiationException | java.lang.InstantiationException |
                        IllegalAccessException ine) {
                    Log.e(LOG_TAG, "Failed to instantiate " + getContentClass(), ine);
                }
                return;
            }

            // FragmentTabHost needs to be retained to keep track of tabs properly.
            setRetainInstance(true);
        }

        @Override
        public View onCreateView(
                LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState) {
            FragmentTabHost tabHost = new FragmentTabHost(getActivity());
            tabHost.setup(getActivity(), getChildFragmentManager(), View.generateViewId());

            // Tab for the parent profile
            Bundle parentArgs = new Bundle();
            parentArgs.putBoolean(EXTRA_PARENT_PROFILE, true);
            tabHost.addTab(
                    tabHost.newTabSpec("parent").setIndicator(getString(R.string.personal_profile)),
                    getContentClass(), parentArgs);

            // Tab for the work profile
            Bundle workArgs = new Bundle();
            workArgs.putBoolean(EXTRA_PARENT_PROFILE, false);
            tabHost.addTab(
                    tabHost.newTabSpec("profile").setIndicator(getString(R.string.work_profile)),
                    getContentClass(), workArgs);

            // Mark work tab as the current one.
            tabHost.setCurrentTab(1);

            return tabHost;
        }

        public abstract Class<? extends ProfileOrParentFragment> getContentClass();
    }


    private DevicePolicyManager mDevicePolicyManager;
    private ComponentName mAdminComponent;
    private boolean mParentInstance;

    /**
     * @return a {@link DevicePolicyManager} instance for the profile this tab should affect.
     */
    protected final DevicePolicyManager getDpm() {
        return mDevicePolicyManager;
    }

    protected final ComponentName getAdmin() {
        return mAdminComponent;
    }

    /**
     * @return {@code true} if this tab is supposed to affect the parent profile, {@code false}
     *         otherwise.
     */
    protected final boolean isParentProfileInstance() {
        return mParentInstance;
    }

    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

        // Check arguments- see whether we're supposed to run on behalf of the parent profile.
        final Bundle arguments = getArguments();
        if (arguments != null) {
            mParentInstance = arguments.getBoolean(EXTRA_PARENT_PROFILE, false);
        }

        // Get a device policy manager for the current user.
        mDevicePolicyManager = (DevicePolicyManager)
                getActivity().getSystemService(Context.DEVICE_POLICY_SERVICE);

        mAdminComponent = DeviceAdminReceiver.getComponentName(getActivity());

        // Switch to parent profile if we are running on their behalf.
        if (mParentInstance) {
            mDevicePolicyManager = mDevicePolicyManager.getParentProfileInstance(mAdminComponent);
        }
    }
}
