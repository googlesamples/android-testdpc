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

package com.afwsamples.testdpc.policy;

import static com.google.common.truth.Truth.assertThat;
import static org.robolectric.Shadows.shadowOf;

import android.app.ActivityManager;
import android.app.admin.DevicePolicyManager;
import android.content.ComponentName;
import android.content.Context;
import android.content.Intent;
import android.os.Build;
import com.afwsamples.testdpc.DeviceAdminReceiver;
import java.util.HashMap;
import java.lang.reflect.Field;
import org.junit.After;
import org.junit.Before;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.robolectric.RobolectricTestRunner;
import org.robolectric.RuntimeEnvironment;
import org.robolectric.annotation.Config;
import org.robolectric.util.FragmentTestUtil;


@RunWith(RobolectricTestRunner.class)
@Config(minSdk = Build.VERSION_CODES.P)
public class PolicyManagementFragmentTest{
  private final Context mContext = RuntimeEnvironment.application;
  private final DevicePolicyManager mDevicePolicyManager =
      mContext.getSystemService(DevicePolicyManager.class);
  private PolicyManagementFragment mPolicyFragment = new PolicyManagementFragment();

  private static final String RELAUNCH_IN_LOCK_TASK = "relaunch_in_lock_task";

  @Before
  public void setup() throws Exception {
    shadowOf(mDevicePolicyManager).setDeviceOwner(
        new ComponentName(mContext.getApplicationContext(), DeviceAdminReceiver.class)
    );
  }

  @After
  public void tearDown() throws Exception {
    resetFileProviderMap();
  }

  @Test
  public void relaunchInLockTaskMode_lockTaskModeNone_launchWithCorrectIntent() {
    relaunchWithTaskMode(ActivityManager.LOCK_TASK_MODE_NONE);

    mPolicyFragment.findPreference(RELAUNCH_IN_LOCK_TASK).performClick();

    Intent intent = shadowOf(RuntimeEnvironment.application).getNextStartedActivity();
    assertThat(containsFlag(intent,Intent.FLAG_ACTIVITY_NEW_TASK)).isTrue();
    assertThat(containsFlag(intent,Intent.FLAG_ACTIVITY_MULTIPLE_TASK)).isTrue();
  }

  @Test
  public void relaunchInLockTaskMode_lockTaskModeLocked_launchWithCorrectIntent() {
    relaunchWithTaskMode(ActivityManager.LOCK_TASK_MODE_LOCKED);

    mPolicyFragment.findPreference(RELAUNCH_IN_LOCK_TASK).performClick();

    Intent intent = shadowOf(RuntimeEnvironment.application).getNextStartedActivity();
    assertThat(containsFlag(intent,Intent.FLAG_ACTIVITY_NEW_TASK)).isTrue();
    assertThat(containsFlag(intent,Intent.FLAG_ACTIVITY_MULTIPLE_TASK)).isFalse();
  }

  private void relaunchWithTaskMode(int lockTaskModeState) {
    shadowOf(mContext.getSystemService(ActivityManager.class))
        .setLockTaskModeState(lockTaskModeState);
    FragmentTestUtil.startFragment(mPolicyFragment);
  }

  private static boolean containsFlag(Intent intent, int flag) {
    return (intent.getFlags() & flag) != 0;
  }

  /**
   * This is a workaround for fixing {@link android.support.v4.content.FileProvider} throws
   * "java.lang.IllegalArgumentException: Failed to find configured root that contains ..." when
   * there are more than one test cases using FileProvider. For more details, see b/122474286.
   */
  private static void resetFileProviderMap() throws Exception {
    Class<?> clazz = Class.forName("androidx.core.content.FileProvider");
    Field field = clazz.getDeclaredField("sCache");
    field.setAccessible(true);
    field.set(null, new HashMap<>());
  }
}

