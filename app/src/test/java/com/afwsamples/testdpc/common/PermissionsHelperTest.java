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

import static com.google.common.truth.Truth.assertThat;
import static org.junit.Assert.assertFalse;
import static org.junit.Assert.assertTrue;
import static org.robolectric.Shadows.shadowOf;

import android.Manifest.permission;
import android.app.admin.DevicePolicyManager;
import android.content.ComponentName;
import android.content.Context;
import android.content.pm.PermissionInfo;
import android.os.Build.VERSION_CODES;
import androidx.test.core.app.ApplicationProvider;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.robolectric.RobolectricTestRunner;
import org.robolectric.RuntimeEnvironment;
import org.robolectric.annotation.Config;
import org.robolectric.shadows.ShadowLog;

@RunWith(RobolectricTestRunner.class)
@Config(minSdk = VERSION_CODES.M)
public class PermissionsHelperTest {

  private static final ComponentName TESTDPC_ADMIN =
      new ComponentName("com.afwsamples.testdpc", "TestCls");
  private static final ComponentName NON_TESTDPC_ADMIN =
      new ComponentName("TestPkg", "TestCls");

  private final Context mContext = RuntimeEnvironment.application;
  private final DevicePolicyManager mDevicePolicyManager =
      (DevicePolicyManager) ApplicationProvider.getApplicationContext()
          .getSystemService(Context.DEVICE_POLICY_SERVICE);

  // Permission protection levels should be defined by the framework/shadows and should not be set
  // by the tests, however this is not the case now
  private static final String MISSING_PERMISSION = "permission";
  private static final String DANGEROUS_PERMISSION = permission.ACCESS_FINE_LOCATION;
  private static final String NORMAL_PERMISSION = permission.ACCESS_WIFI_STATE;
  private static final String MISSING_INFO_PERMISSION = permission.CHANGE_WIFI_STATE;

  @Test
  public void ensureRequiredPermissions_ifPermissionMissingFromManifest_shouldReturnFalseAndLogError() {
    boolean requiredPermissionsGranted = PermissionsHelper
        .ensureRequiredPermissions(new String[]{MISSING_PERMISSION}, NON_TESTDPC_ADMIN, mContext);

    assertFalse(requiredPermissionsGranted);
    assertTrue(ShadowLog.getLogsForTag(PermissionsHelper.TAG).get(0).msg
        .contains("Missing required permission from manifest: " + MISSING_PERMISSION));
  }

  @Test
  public void ensureRequiredPermissions_ifPermissionIsDangerousAndDpcIsProfileOwner_shouldReturnTrueAndSetPermissionGrantState() {
    addPermissionInfo(DANGEROUS_PERMISSION, PermissionInfo.PROTECTION_DANGEROUS);
    shadowOf(mDevicePolicyManager).setProfileOwner(TESTDPC_ADMIN);

    boolean requiredPermissionsGranted = PermissionsHelper
        .ensureRequiredPermissions(new String[]{DANGEROUS_PERMISSION}, TESTDPC_ADMIN, mContext);

    assertTrue(requiredPermissionsGranted);
    assertThat(mDevicePolicyManager
        .getPermissionGrantState(TESTDPC_ADMIN, mContext.getPackageName(), DANGEROUS_PERMISSION))
        .isEqualTo(DevicePolicyManager.PERMISSION_GRANT_STATE_GRANTED);
  }

  @Test
  public void ensureRequiredPermissions_ifPermissionIsDangerousAndDpcIsDeviceOwner_shouldReturnTrueAndSetPermissionGrantState() {
    addPermissionInfo(DANGEROUS_PERMISSION, PermissionInfo.PROTECTION_DANGEROUS);
    shadowOf(mDevicePolicyManager).setDeviceOwner(TESTDPC_ADMIN);

    boolean requiredPermissionsGranted = PermissionsHelper
        .ensureRequiredPermissions(new String[]{DANGEROUS_PERMISSION}, TESTDPC_ADMIN, mContext);

    assertTrue(requiredPermissionsGranted);
    assertThat(mDevicePolicyManager
        .getPermissionGrantState(TESTDPC_ADMIN, mContext.getPackageName(), DANGEROUS_PERMISSION))
        .isEqualTo(DevicePolicyManager.PERMISSION_GRANT_STATE_GRANTED);
  }

  @Test
  public void ensureRequiredPermissions_ifPermissionIsDangerousAndPermissionGrantStateIsAlreadySet_shouldReturnTrue() {
    addPermissionInfo(DANGEROUS_PERMISSION, PermissionInfo.PROTECTION_DANGEROUS);
    shadowOf(mDevicePolicyManager).setProfileOwner(TESTDPC_ADMIN);
    mDevicePolicyManager.setPermissionGrantState(
        TESTDPC_ADMIN,
        mContext.getPackageName(),
        DANGEROUS_PERMISSION,
        DevicePolicyManager.PERMISSION_GRANT_STATE_GRANTED);

    boolean requiredPermissionsGranted = PermissionsHelper
        .ensureRequiredPermissions(new String[]{DANGEROUS_PERMISSION}, TESTDPC_ADMIN, mContext);

    assertTrue(requiredPermissionsGranted);
    assertThat(mDevicePolicyManager
        .getPermissionGrantState(TESTDPC_ADMIN, mContext.getPackageName(), DANGEROUS_PERMISSION))
        .isEqualTo(DevicePolicyManager.PERMISSION_GRANT_STATE_GRANTED);
  }

  @Test
  public void ensureRequiredPermissions_ifPermissionIsDangerousAndDpcIsNotOwner_shouldReturnFalse() {
    addPermissionInfo(DANGEROUS_PERMISSION, PermissionInfo.PROTECTION_DANGEROUS);
    shadowOf(mDevicePolicyManager).setProfileOwner(NON_TESTDPC_ADMIN);

    boolean requiredPermissionsGranted = PermissionsHelper
        .ensureRequiredPermissions(new String[]{DANGEROUS_PERMISSION}, NON_TESTDPC_ADMIN, mContext);

    assertFalse(requiredPermissionsGranted);
  }

  @Test
  public void ensureRequiredPermissions_ifPermissionInfoNotFound_shouldReturnTrueAndLogError() {
    shadowOf(mDevicePolicyManager).setProfileOwner(TESTDPC_ADMIN);

    boolean requiredPermissionsGranted = PermissionsHelper
        .ensureRequiredPermissions(new String[]{MISSING_INFO_PERMISSION}, TESTDPC_ADMIN, mContext);

    assertTrue(requiredPermissionsGranted);
    assertTrue(ShadowLog.getLogsForTag(PermissionsHelper.TAG).get(0).msg
        .contains("Failed to look up permission."));
  }

  @Test
  public void ensureRequiredPermissions_ifPermissionIsNormal_shouldReturnTrueAndNotSetPermissionGrantState() {
    addPermissionInfo(NORMAL_PERMISSION, PermissionInfo.PROTECTION_NORMAL);
    shadowOf(mDevicePolicyManager).setProfileOwner(TESTDPC_ADMIN);

    boolean requiredPermissionsGranted = PermissionsHelper
        .ensureRequiredPermissions(new String[]{NORMAL_PERMISSION}, TESTDPC_ADMIN, mContext);

    assertTrue(requiredPermissionsGranted);
    assertThat(mDevicePolicyManager
        .getPermissionGrantState(TESTDPC_ADMIN, mContext.getPackageName(), NORMAL_PERMISSION))
        .isEqualTo(DevicePolicyManager.PERMISSION_GRANT_STATE_DEFAULT);
  }

  @Test
  public void ensureRequiredPermissions_ifAllPermissionsAreGranted_shouldReturnTrue() {
    addPermissionInfo(NORMAL_PERMISSION, PermissionInfo.PROTECTION_NORMAL);
    addPermissionInfo(DANGEROUS_PERMISSION, PermissionInfo.PROTECTION_DANGEROUS);
    shadowOf(mDevicePolicyManager).setProfileOwner(TESTDPC_ADMIN);

    boolean requiredPermissionsGranted = PermissionsHelper
        .ensureRequiredPermissions(new String[]{NORMAL_PERMISSION, DANGEROUS_PERMISSION},
            TESTDPC_ADMIN, mContext);

    assertTrue(requiredPermissionsGranted);
    assertThat(mDevicePolicyManager
        .getPermissionGrantState(TESTDPC_ADMIN, mContext.getPackageName(), NORMAL_PERMISSION))
        .isEqualTo(DevicePolicyManager.PERMISSION_GRANT_STATE_DEFAULT);
    assertThat(mDevicePolicyManager
        .getPermissionGrantState(TESTDPC_ADMIN, mContext.getPackageName(), DANGEROUS_PERMISSION))
        .isEqualTo(DevicePolicyManager.PERMISSION_GRANT_STATE_GRANTED);
  }

  @Test
  public void ensureRequiredPermissions_ifAtLeastOnePermissionNotGranted_shouldReturnFalse() {
    addPermissionInfo(NORMAL_PERMISSION, PermissionInfo.PROTECTION_NORMAL);
    addPermissionInfo(DANGEROUS_PERMISSION, PermissionInfo.PROTECTION_DANGEROUS);
    shadowOf(mDevicePolicyManager).setProfileOwner(NON_TESTDPC_ADMIN);

    boolean requiredPermissionsGranted = PermissionsHelper
        .ensureRequiredPermissions(new String[]{NORMAL_PERMISSION, DANGEROUS_PERMISSION},
            NON_TESTDPC_ADMIN, mContext);

    assertFalse(requiredPermissionsGranted);
  }

  private void addPermissionInfo(String permission, int protectionLevel) {
    PermissionInfo permissionInfo = new PermissionInfo();
    permissionInfo.name = permission;
    permissionInfo.protectionLevel = protectionLevel;
    shadowOf(mContext.getPackageManager()).addPermissionInfo(permissionInfo);
  }

}