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

import android.annotation.TargetApi;
import android.app.Fragment;
import android.app.admin.DevicePolicyManager;
import android.content.ComponentName;
import android.content.Context;
import android.os.Build.VERSION_CODES;
import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import androidx.legacy.app.FragmentTabHost;
import androidx.preference.PreferenceManager;
import com.afwsamples.testdpc.DevicePolicyManagerGateway;
import com.afwsamples.testdpc.DevicePolicyManagerGatewayImpl;
import com.afwsamples.testdpc.R;

/**
 * This fragment provides functionalities to show the same policy fragment for both the managed user
 * and its parent user in separate tabs, if applicable.
 *
 * <p>If there is no parent user (for example, if the managed user is controlled by a Device Owner),
 * the fragment will be shown directly.
 *
 * <p>Please notice that all subclasses of this fragment only support N or above.
 */
@TargetApi(VERSION_CODES.N)
public abstract class ProfileOrParentFragment extends BaseSearchablePolicyPreferenceFragment {
  private static final String LOG_TAG = "ProfileOrParentFragment";

  private static final String EXTRA_PARENT_PROFILE = "com.afwsamples.testdpc.extra.PARENT";

  // Tag to append to the name of the SharedPreferences if we are running as a parent instance.
  private static final String TAG_PARENT = ":parent";

  public abstract static class Container extends Fragment {
    @Override
    public void onCreate(Bundle savedInstanceState) {
      super.onCreate(savedInstanceState);
      // FragmentTabHost needs to be retained to keep track of tabs properly.
      setRetainInstance(true);
    }

    @Override
    public View onCreateView(
        LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState) {
      FragmentTabHost tabHost = new FragmentTabHost(getActivity());
      tabHost.setup(getActivity(), getChildFragmentManager(), container.getId());

      final boolean showDualTabs =
          Util.isManagedProfileOwner(getActivity()) && Util.SDK_INT >= VERSION_CODES.N;

      // Tab for the parent profile
      if (showDualTabs) {
        Bundle parentArgs = new Bundle();
        parentArgs.putBoolean(EXTRA_PARENT_PROFILE, true);
        tabHost.addTab(
            tabHost.newTabSpec("parent").setIndicator(getString(R.string.personal_profile)),
            getContentClass(),
            parentArgs);
      }

      // Tab for the profile/ running user.
      Bundle parentArgs = new Bundle();
      parentArgs.putBoolean(EXTRA_PARENT_PROFILE, false);
      tabHost.addTab(
          tabHost.newTabSpec("profile").setIndicator(getString(R.string.work_profile)),
          getContentClass(),
          parentArgs);

      if (showDualTabs) {
        // Mark work tab as the current one.
        tabHost.setCurrentTab(1);
      } else {
        // We just want to show the fragment for current running user, hide tabs.
        tabHost.setCurrentTab(0);
        tabHost.getTabWidget().setVisibility(View.GONE);
      }
      return tabHost;
    }

    public abstract Class<? extends ProfileOrParentFragment> getContentClass();
  }

  private DevicePolicyManagerGateway mDevicePolicyGateway;
  private boolean mParentInstance;
  private boolean mProfileOwner;
  private boolean mDeviceOwner;

  public ProfileOrParentFragment() {
    super(LOG_TAG);
  }

  /** @return a {@link DevicePolicyManager} instance for the profile this tab should affect. */
  protected final DevicePolicyManager getDpm() {
    return mDevicePolicyGateway.getDevicePolicyManager();
  }

  protected final DevicePolicyManagerGateway getDpmGateway() {
    return mDevicePolicyGateway;
  }

  protected final ComponentName getAdmin() {
    return mDevicePolicyGateway.getAdmin();
  }

  /**
   * @return {@code true} if this tab is supposed to affect the parent profile, {@code false}
   *     otherwise.
   */
  protected final boolean isParentProfileInstance() {
    return mParentInstance;
  }

  /** @return {@code true} if this tab represents a top-level user, {@code false} otherwise. */
  protected final boolean isManagedProfileInstance() {
    return mProfileOwner && !isParentProfileInstance();
  }

  protected boolean isDeviceOwner() {
    return mDeviceOwner;
  }

  protected boolean isProfileOwner() {
    return mProfileOwner;
  }

  @Override
  public void onCreate(Bundle savedInstanceState) {
    // Check arguments- see whether we're supposed to run on behalf of the parent profile.
    final Bundle arguments = getArguments();
    if (arguments != null) {
      mParentInstance = arguments.getBoolean(EXTRA_PARENT_PROFILE, false);
    }

    Context context = getActivity();
    mDevicePolicyGateway = new DevicePolicyManagerGatewayImpl(context);

    // Store whether we are the profile owner for faster lookup.
    mProfileOwner = mDevicePolicyGateway.isProfileOwnerApp();
    mDeviceOwner = mDevicePolicyGateway.isDeviceOwnerApp();

    if (mParentInstance) {
      mDevicePolicyGateway = DevicePolicyManagerGatewayImpl.forParentProfile(context);
    }

    // Put at last to make sure all initializations above are done before subclass's
    // onCreatePreferences is called.
    super.onCreate(savedInstanceState);

    // Switch to parent profile if we are running on their behalf.
    // This needs to be called after super.onCreate because preference manager is set up
    // inside super.onCreate.
    if (mParentInstance) {
      final PreferenceManager pm = getPreferenceManager();
      pm.setSharedPreferencesName(pm.getSharedPreferencesName() + TAG_PARENT);
    }
  }
}
