/*
 * Copyright (C) 2020 The Android Open Source Project
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

package com.afwsamples.testdpc;

import static com.afwsamples.testdpc.common.Util.isAtLeastT;

import android.app.admin.DevicePolicyManager;
import android.app.admin.NetworkEvent;
import android.app.admin.SecurityLog.SecurityEvent;
import android.content.ComponentName;
import android.content.Context;
import android.content.Intent;
import android.content.pm.ApplicationInfo;
import android.content.pm.PackageManager;
import android.content.pm.PackageManager.NameNotFoundException;
import android.graphics.Bitmap;
import android.location.LocationManager;
import android.os.Bundle;
import android.os.PersistableBundle;
import android.os.Process;
import android.os.UserHandle;
import android.os.UserManager;
import android.security.keystore.KeyGenParameterSpec;
import android.security.AttestedKeyPair;
import android.util.Log;
import androidx.annotation.NonNull;

import com.afwsamples.testdpc.common.ReflectionUtil;
import com.afwsamples.testdpc.common.Util;
import com.google.common.base.Joiner;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collections;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.function.Consumer;
import java.util.stream.Collectors;

public final class DevicePolicyManagerGatewayImpl implements DevicePolicyManagerGateway {

  private static final String TAG = "DevicePolicyManagerGate";

  private final DevicePolicyManager mDevicePolicyManager;
  private final UserManager mUserManager;
  private final ComponentName mAdminComponentName;
  private final PackageManager mPackageManager;
  private final LocationManager mLocationManager;

  public DevicePolicyManagerGatewayImpl(@NonNull Context context) {
    this(
        context.getSystemService(DevicePolicyManager.class),
        context.getSystemService(UserManager.class),
        context.getPackageManager(),
        context.getSystemService(LocationManager.class),
        DeviceAdminReceiver.getComponentName(context));
  }

  public DevicePolicyManagerGatewayImpl(
      @NonNull DevicePolicyManager dpm,
      @NonNull UserManager um,
      @NonNull PackageManager pm,
      @NonNull LocationManager lm,
      @NonNull ComponentName admin) {
    mDevicePolicyManager = dpm;
    mUserManager = um;
    mPackageManager = pm;
    mLocationManager = lm;
    mAdminComponentName = admin;

    Log.d(TAG, "constructor: admin=" + mAdminComponentName + ", dpm=" + dpm);
  }

  public static DevicePolicyManagerGatewayImpl forParentProfile(@NonNull Context context) {
    ComponentName admin = DeviceAdminReceiver.getComponentName(context);
    DevicePolicyManager dpm =
        context.getSystemService(DevicePolicyManager.class).getParentProfileInstance(admin);
    UserManager um = context.getSystemService(UserManager.class);
    PackageManager pm = context.getPackageManager();
    LocationManager lm = context.getSystemService(LocationManager.class);
    return new DevicePolicyManagerGatewayImpl(dpm, um, pm, lm, admin);
  }

  @Override
  public ComponentName getAdmin() {
    return mAdminComponentName;
  }

  @Override
  public DevicePolicyManager getDevicePolicyManager() {
    return mDevicePolicyManager;
  }

  @Override
  public boolean isProfileOwnerApp() {
    if (mAdminComponentName == null) {
      return false;
    }
    return mDevicePolicyManager.isProfileOwnerApp(mAdminComponentName.getPackageName());
  }

  @Override
  public boolean isDeviceOwnerApp() {
    if (mAdminComponentName == null) {
      return false;
    }

    return mDevicePolicyManager.isDeviceOwnerApp(mAdminComponentName.getPackageName());
  }

  @Override
  public boolean isOrganizationOwnedDeviceWithManagedProfile() {
    return mDevicePolicyManager.isOrganizationOwnedDeviceWithManagedProfile();
  }

  @Override
  public boolean isHeadlessSystemUserMode() {
    Util.requireAndroidS();

    return mUserManager.isHeadlessSystemUserMode();
  }

  @Override
  public boolean isUserForeground() {
    Util.requireAndroidS();

    return mUserManager.isUserForeground();
  }

  @Override
  public List<UserHandle> listForegroundAffiliatedUsers() {
    Util.requireAndroidS();

    return mDevicePolicyManager.listForegroundAffiliatedUsers();
  }

  @Override
  public void createAndManageUser(
      String name, int flags, Consumer<UserHandle> onSuccess, Consumer<Exception> onError) {
    Log.d(TAG, "createAndManageUser(" + name + ", " + flags + ")");

    try {
      UserHandle userHandle =
          mDevicePolicyManager.createAndManageUser(
              mAdminComponentName, name, mAdminComponentName, /* adminExtras= */ null, flags);
      if (userHandle != null) {
        onSuccess.accept(userHandle);
      } else {
        onError.accept(
            new InvalidResultException("null", "createAndManageUser(%s, %d)", name, flags));
      }
    } catch (Exception e) {
      onError.accept(e);
    }
  }

  @Override
  public void setUserIcon(Bitmap icon, Consumer<Void> onSuccess, Consumer<Exception> onError) {
    Log.d(TAG, "setUserIcon(" + icon + ")");
    try {
      mDevicePolicyManager.setUserIcon(mAdminComponentName, icon);
      onSuccess.accept(null);
    } catch (Exception e) {
      onError.accept(e);
    }
  }

  @Override
  public void setStartUserSessionMessage(
      CharSequence message, Consumer<Void> onSuccess, Consumer<Exception> onError) {
    Log.d(TAG, "setStartUserSessionMessage(" + message + ")");
    try {
      mDevicePolicyManager.setStartUserSessionMessage(mAdminComponentName, message);
      onSuccess.accept(null);
    } catch (Exception e) {
      onError.accept(e);
    }
  }

  @Override
  public CharSequence getStartUserSessionMessage() {
    return mDevicePolicyManager.getStartUserSessionMessage(mAdminComponentName);
  }

  @Override
  public void setEndUserSessionMessage(
      CharSequence message, Consumer<Void> onSuccess, Consumer<Exception> onError) {
    Log.d(TAG, "setEndUserSessionMessage(" + message + ")");
    try {
      mDevicePolicyManager.setEndUserSessionMessage(mAdminComponentName, message);
      onSuccess.accept(null);
    } catch (Exception e) {
      onError.accept(e);
    }
  }

  @Override
  public CharSequence getEndUserSessionMessage() {
    return mDevicePolicyManager.getEndUserSessionMessage(mAdminComponentName);
  }

  @Override
  public UserHandle getUserHandle(long serialNumber) {
    return mUserManager.getUserForSerialNumber(serialNumber);
  }

  @Override
  public long getSerialNumber(UserHandle user) {
    return mUserManager.getSerialNumberForUser(user);
  }

  @Override
  public void removeUser(
      UserHandle userHandle, Consumer<Void> onSuccess, Consumer<Exception> onError) {
    Log.d(TAG, "removeUser(" + userHandle + ")");

    try {
      boolean success = mDevicePolicyManager.removeUser(mAdminComponentName, userHandle);
      if (success) {
        onSuccess.accept(null);
      } else {
        onError.accept(new InvalidResultException("false", "removeUser(%s)", userHandle));
      }
    } catch (Exception e) {
      onError.accept(e);
    }
  }

  @Override
  public void switchUser(
      UserHandle userHandle, Consumer<Void> onSuccess, Consumer<Exception> onError) {
    Log.d(TAG, "switchUser(" + userHandle + ")");

    try {
      boolean success = mDevicePolicyManager.switchUser(mAdminComponentName, userHandle);
      if (success) {
        onSuccess.accept(null);
      } else {
        onError.accept(new FailedOperationException("switchUser(%s)", userHandle));
      }
    } catch (Exception e) {
      onError.accept(e);
    }
  }

  @Override
  public void startUserInBackground(
      UserHandle userHandle, Consumer<Integer> onSuccess, Consumer<Exception> onError) {
    Log.d(TAG, "startUserInBackground(" + userHandle + ")");
    try {
      int status = mDevicePolicyManager.startUserInBackground(mAdminComponentName, userHandle);
      if (status == UserManager.USER_OPERATION_SUCCESS) {
        onSuccess.accept(status);
      } else {
        onError.accept(
            new FailedUserOperationException(status, "startUserInBackground(%s)", userHandle));
      }
    } catch (Exception e) {
      onError.accept(e);
    }
  }

  @Override
  public void stopUser(
      UserHandle userHandle, Consumer<Integer> onSuccess, Consumer<Exception> onError) {
    Log.d(TAG, "stopUser(" + userHandle + ")");
    try {
      int status = mDevicePolicyManager.stopUser(mAdminComponentName, userHandle);
      if (status == UserManager.USER_OPERATION_SUCCESS) {
        onSuccess.accept(status);
      } else {
        onError.accept(new FailedUserOperationException(status, "stopUser(%s)", userHandle));
      }
    } catch (Exception e) {
      onError.accept(e);
    }
  }

  @Override
  public boolean isLogoutEnabled() {
    boolean isIt = mDevicePolicyManager.isLogoutEnabled();
    Log.d(TAG, "isLogoutEnabled(): " + isIt);
    return isIt;
  }

  @Override
  public void setLogoutEnabled(boolean enabled, Consumer<Void> onSuccess,
      Consumer<Exception> onError) {
      Log.d(TAG, "setLogoutEnabled(" + enabled + ")");
      try {
          mDevicePolicyManager.setLogoutEnabled(mAdminComponentName, enabled);
          onSuccess.accept(null);
        } catch (Exception e) {
          onError.accept(e);
        }
  }

  @Override
  public void logoutUser(Consumer<Integer> onSuccess, Consumer<Exception> onError) {
    UserHandle userHandle = Process.myUserHandle();
    Log.d(TAG, "logoutUser(" + userHandle + ")");
    try {
      int status = mDevicePolicyManager.logoutUser(mAdminComponentName);
      if (status == UserManager.USER_OPERATION_SUCCESS) {
        onSuccess.accept(status);
      } else {
        onError.accept(new FailedUserOperationException(status, "logoutUser(%s)", userHandle));
      }
    } catch (Exception e) {
      onError.accept(e);
    }
  }

  @Override
  public boolean isAffiliatedUser() {
    boolean isIt = mDevicePolicyManager.isAffiliatedUser();
    Log.d(TAG, "isAffiliatedUser(): " + isIt);
    return isIt;
  }

  @Override
  public void setAffiliationIds(Set<String> ids) {
    Log.d(TAG, "setAffiliationIds(" + ids + ")");
    mDevicePolicyManager.setAffiliationIds(mAdminComponentName, ids);
  }

  @Override
  public Set<String> getAffiliationIds() {
    Set<String> ids = mDevicePolicyManager.getAffiliationIds(mAdminComponentName);
    Log.d(TAG, "getAffiliationIds(): " + ids);
    return ids;
  }

  @Override
  public Set<String> getUserRestrictions() {
    Log.d(TAG, "getUserRestrictions()");
    Bundle restrictions = mDevicePolicyManager.getUserRestrictions(mAdminComponentName);
    return restrictions.keySet().stream()
        .filter(k -> restrictions.getBoolean(k))
        .collect(Collectors.toSet());
  }

  @Override
  public void setUserRestriction(
      String userRestriction,
      boolean enabled,
      Consumer<Void> onSuccess,
      Consumer<Exception> onError) {
    Log.d(TAG, "setUserRestriction(" + userRestriction + ", " + enabled + ")");

    try {
      if (enabled) {
        mDevicePolicyManager.addUserRestriction(mAdminComponentName, userRestriction);
      } else {
        mDevicePolicyManager.clearUserRestriction(mAdminComponentName, userRestriction);
      }
      onSuccess.accept(null);
    } catch (Exception e) {
      onError.accept(e);
    }
  }

  @Override
  public void setUserRestriction(String userRestriction, boolean enabled) {
    String message = String.format("setUserRestriction(%s, %b)", userRestriction, enabled);
    setUserRestriction(
        userRestriction, enabled, (v) -> onSuccessLog(message), (e) -> onErrorLog(e, message));
  }

  @Override
  public boolean hasUserRestriction(String userRestriction) {
    if (Log.isLoggable(TAG, Log.VERBOSE)) {
      Log.v(TAG, "hasUserRestriction(" + userRestriction + ")");
    }
    return mUserManager.hasUserRestriction(userRestriction);
  }

  @Override
  public void lockNow(Consumer<Void> onSuccess, Consumer<Exception> onError) {
    Log.d(TAG, "lockNow()");

    try {
      mDevicePolicyManager.lockNow();
      onSuccess.accept(null);
    } catch (Exception e) {
      onError.accept(e);
    }
  }

  @Override
  public void lockNow(int flags, Consumer<Void> onSuccess, Consumer<Exception> onError) {
    Log.d(TAG, "lockNow(" + flags + ")");

    try {
      mDevicePolicyManager.lockNow(flags);
      onSuccess.accept(null);
    } catch (Exception e) {
      onError.accept(e);
    }
  }

  @Override
  public void reboot(Consumer<Void> onSuccess, Consumer<Exception> onError) {
    Log.d(TAG, "reboot()");

    try {
      mDevicePolicyManager.reboot(mAdminComponentName);
      onSuccess.accept(null);
    } catch (Exception e) {
      onError.accept(e);
    }
  }

  @Override
  public void wipeData(int flags, Consumer<Void> onSuccess, Consumer<Exception> onError) {
    Log.d(TAG, "wipeData(" + flags + ")");

    try {
      mDevicePolicyManager.wipeData(flags);
      onSuccess.accept(null);
    } catch (Exception e) {
      onError.accept(e);
    }
  }

  @Override
  public void requestBugreport(Consumer<Void> onSuccess, Consumer<Exception> onError) {
    Log.d(TAG, "requestBugreport(");

    try {
      boolean success = mDevicePolicyManager.requestBugreport(mAdminComponentName);
      if (success) {
        onSuccess.accept(null);
      } else {
        onError.accept(new FailedOperationException("requestBugreport()"));
      }
    } catch (Exception e) {
      onError.accept(e);
    }
  }

  @Override
  public long getLastBugReportRequestTime() {
      return callMethodThatReturnsLongUsingReflection("getLastBugReportRequestTime");
  }

  @Override
  public void setNetworkLoggingEnabled(
      boolean enabled, Consumer<Void> onSuccess, Consumer<Exception> onError) {
    Log.d(TAG, "setNetworkLoggingEnabled(" + enabled + ")");

    try {
      mDevicePolicyManager.setNetworkLoggingEnabled(mAdminComponentName, enabled);
      onSuccess.accept(null);
    } catch (Exception e) {
      onError.accept(e);
    }
  }

  @Override
  public void setNetworkLoggingEnabled(boolean enabled) {
    String message = String.format("setNetworkLogging(%b)", enabled);
    setNetworkLoggingEnabled(enabled, (v) -> onSuccessLog(message), (e) -> onErrorLog(e, message));
  }

  @Override
  public boolean isNetworkLoggingEnabled() {
    boolean isIt = mDevicePolicyManager.isNetworkLoggingEnabled(mAdminComponentName);
    Log.d(TAG, "isNetworkLoggingEnabled(): " + isIt);
    return isIt;
  }

  @Override
  public long getLastNetworkLogRetrievalTime() {
    return callMethodThatReturnsLongUsingReflection("getLastNetworkLogRetrievalTime");
  }

  @Override
  public List<NetworkEvent> retrieveNetworkLogs(long batchToken) {
    List<NetworkEvent> events = mDevicePolicyManager.retrieveNetworkLogs(mAdminComponentName,
        batchToken);
    Log.d(TAG, "retrieveNetworkLogs(): returned "
            + (events == null ? "null" : (events.size() + " events")));
    return events;
  }

  @Override
  public void setSecurityLoggingEnabled(
      boolean enabled, Consumer<Void> onSuccess, Consumer<Exception> onError) {
    Log.d(TAG, "setSecurityLoggingEnabled(" + enabled + ")");

    try {
      mDevicePolicyManager.setSecurityLoggingEnabled(mAdminComponentName, enabled);
      onSuccess.accept(null);
    } catch (Exception e) {
      onError.accept(e);
    }
  }

  @Override
  public boolean isSecurityLoggingEnabled() {
    boolean isIt = mDevicePolicyManager.isSecurityLoggingEnabled(mAdminComponentName);
    Log.d(TAG, "isSecurityLoggingEnabled(): " + isIt);
    return isIt;
  }

  @Override
  public long getLastSecurityLogRetrievalTime() {
    return callMethodThatReturnsLongUsingReflection("getLastSecurityLogRetrievalTime");
  }

  @Override
  public List<SecurityEvent> retrieveSecurityLogs() {
    List<SecurityEvent> events = mDevicePolicyManager.retrieveSecurityLogs(mAdminComponentName);
    Log.d(TAG, "retrieveSecurityLogs(): returned "
        + (events == null ? "null" : (events.size() + " events")));
    return events;
  }

  @Override
  public List<SecurityEvent> retrievePreRebootSecurityLogs() {
    List<SecurityEvent> events =
        mDevicePolicyManager.retrievePreRebootSecurityLogs(mAdminComponentName);
    Log.d(TAG, "retrievePreRebootSecurityLogs(): returned " + events.size() + " events");
    return events;
  }

  @Override
  public void setOrganizationName(
      CharSequence title, Consumer<Void> onSuccess, Consumer<Exception> onError) {
    Log.d(TAG, "setOrganizationName(" + title + ")");

    try {
      mDevicePolicyManager.setOrganizationName(mAdminComponentName, title);
      onSuccess.accept(null);
    } catch (Exception e) {
      onError.accept(e);
    }
  }

  @Override
  public CharSequence getOrganizationName() {
    return mDevicePolicyManager.getOrganizationName(mAdminComponentName);
  }

  @Override
  public void setUserControlDisabledPackages(
      List<String> packages, Consumer<Void> onSuccess, Consumer<Exception> onError) {
    Log.d(TAG, "setUserControlDisabledPackages(" + packages + ")");

    try {
      mDevicePolicyManager.setUserControlDisabledPackages(mAdminComponentName, packages);
      onSuccess.accept(null);
    } catch (Exception e) {
      onError.accept(e);
    }
  }

  @Override
  public List<String> getUserControlDisabledPackages() {
    return mDevicePolicyManager.getUserControlDisabledPackages(mAdminComponentName);
  }

  @Override
  public boolean setPermittedInputMethods(List<String> packageNames) {
    String inputMethods = packageNames != null ? Joiner.on(",").join(packageNames) : "";
    String message = "setPermittedInputMethods(" + inputMethods + ")";
    return setPermittedInputMethods(
        packageNames, (v) -> onSuccessLog(message), (e) -> onErrorLog(e, message));
  }

  @Override
  public boolean setPermittedInputMethods(
      List<String> packageNames, Consumer<Void> onSuccess, Consumer<Exception> onError) {
    boolean result = false;
    try {
      result = mDevicePolicyManager.setPermittedInputMethods(mAdminComponentName, packageNames);
      onSuccess.accept(null);
    } catch (Exception e) {
      onError.accept(e);
    }
    return result;
  }

  @Override
  public void setUsbDataSignalingEnabled(
      boolean enabled, @NonNull Consumer<Void> onSuccess, @NonNull Consumer<Exception> onError) {
    Log.d(TAG, "setUsbDataSignalingEnabled(" + enabled + ")");
    Util.requireAndroidS();

    try {
      mDevicePolicyManager.setUsbDataSignalingEnabled(enabled);
      onSuccess.accept(null);
    } catch (Exception e) {
      Log.wtf(TAG, "Error calling setUsbDataSignalingEnabled()", e);
      onError.accept(e);
    }
  }

  @Override
  public void setUsbDataSignalingEnabled(boolean enabled) {
    String message = String.format("setUsbDataSignalingEnabled(%b)", enabled);
    setUsbDataSignalingEnabled(
        enabled, (v) -> onSuccessLog(message), (e) -> onErrorLog(e, message));
  }

  @Override
  public void setPreferentialNetworkServiceEnabled(
      boolean enabled, Consumer<Void> onSuccess, Consumer<Exception> onError) {
    Log.d(TAG, "setPreferentialNetworkServiceEnabled(" + enabled + ")");
    Util.requireAndroidS();

    try {
      mDevicePolicyManager.setPreferentialNetworkServiceEnabled(enabled);
      onSuccess.accept(null);
    } catch (Exception e) {
      Log.wtf(TAG, "Error calling setPreferentialNetworkServiceEnabled()", e);
      onError.accept(e);
    }
  }

  @Override
  public boolean isPreferentialNetworkServiceEnabled() {
    Util.requireAndroidS();

    if (isAtLeastT()) {
      return mDevicePolicyManager.isPreferentialNetworkServiceEnabled();
    }

    return false;
  }

  @Override
  public void removeActiveAdmin(Consumer<Void> onSuccess, Consumer<Exception> onError) {
    Log.d(TAG, "removeActiveAdmin()");

    try {
      mDevicePolicyManager.removeActiveAdmin(mAdminComponentName);
      onSuccess.accept(null);
    } catch (Exception e) {
      onError.accept(e);
    }
  }

  @Override
  public void clearDeviceOwnerApp(Consumer<Void> onSuccess, Consumer<Exception> onError) {
    Log.d(TAG, "clearDeviceOwnerApp()");

    try {
      mDevicePolicyManager.clearDeviceOwnerApp(mAdminComponentName.getPackageName());
      onSuccess.accept(null);
    } catch (Exception e) {
      onError.accept(e);
    }
  }

  @Override
  public void clearProfileOwner(Consumer<Void> onSuccess, Consumer<Exception> onError) {
    Log.d(TAG, "clearProfileOwner()");

    try {
      mDevicePolicyManager.clearProfileOwner(mAdminComponentName);
      onSuccess.accept(null);
    } catch (Exception e) {
      onError.accept(e);
    }
  }

  @Override
  public void setPasswordQuality(
      int quality, Consumer<Void> onSuccess, Consumer<Exception> onError) {
    Log.d(TAG, "setPasswordQuality(" + quality + ")");

    try {
      mDevicePolicyManager.setPasswordQuality(mAdminComponentName, quality);
      onSuccess.accept(null);
    } catch (Exception e) {
      onError.accept(e);
    }
  }

  @Override
  public int getPasswordQuality() {
    int quality = mDevicePolicyManager.getPasswordQuality(mAdminComponentName);
    Log.d(TAG, "getPasswordQuality(): " + quality);
    return quality;
  }

  @Override
  public boolean isActivePasswordSufficient() {
    boolean isIt = mDevicePolicyManager.isActivePasswordSufficient();
    Log.d(TAG, "isActivePasswordSufficient(): " + isIt);
    return isIt;
  }

  @Override
  public boolean isActivePasswordSufficientForDeviceRequirement() {
    boolean isIt = mDevicePolicyManager.isActivePasswordSufficientForDeviceRequirement();
    Log.d(TAG, "isActivePasswordSufficientForDeviceRequirement(): " + isIt);
    return isIt;
  }

  @Override
  public void setRequiredPasswordComplexity(
      int complexity, Consumer<Void> onSuccess, Consumer<Exception> onError) {
    Log.d(TAG, "setRequiredPasswordComplexity(" + complexity + ")");

    try {
      mDevicePolicyManager.setRequiredPasswordComplexity(complexity);
      onSuccess.accept(null);
    } catch (Exception e) {
      onError.accept(e);
    }
  }

  @Override
  public int getRequiredPasswordComplexity() {
    int complexity = mDevicePolicyManager.getRequiredPasswordComplexity();
    Log.d(TAG, "getRequiredPasswordComplexity(): " + complexity);
    return complexity;
  }

  @Override
  public void transferOwnership(
      ComponentName target,
      PersistableBundle bundle,
      Consumer<Void> onSuccess,
      Consumer<Exception> onError) {
    Log.d(TAG, "transferOwnership(" + target + ", " + bundle + ")");

    try {
      mDevicePolicyManager.transferOwnership(mAdminComponentName, target, bundle);
      onSuccess.accept(null);
    } catch (Exception e) {
      onError.accept(e);
    }
  }

  @Override
  public void setPackagesSuspended(
      String[] packageNames,
      boolean suspended,
      Consumer<String[]> onSuccess,
      Consumer<Exception> onError) {
    Log.d(TAG, "setPackagesSuspended(" + Arrays.toString(packageNames) + ": " + suspended + ")");

    try {
      String[] result =
          mDevicePolicyManager.setPackagesSuspended(mAdminComponentName, packageNames, suspended);
      onSuccess.accept(result);
    } catch (Exception e) {
      onError.accept(e);
    }
  }

  @Override
  public boolean isPackageSuspended(String packageName) throws NameNotFoundException {
    return mDevicePolicyManager.isPackageSuspended(mAdminComponentName, packageName);
  }

  @Override
  public void setPersonalAppsSuspended(
      boolean suspended, Consumer<Void> onSuccess, Consumer<Exception> onError) {
    Log.d(TAG, "setPersonalAppsSuspended(" + suspended + ")");

    try {
      mDevicePolicyManager.setPersonalAppsSuspended(mAdminComponentName, suspended);
      onSuccess.accept(null);
    } catch (Exception e) {
      onError.accept(e);
    }
  }

  @Override
  public int getPersonalAppsSuspendedReasons() {
    return mDevicePolicyManager.getPersonalAppsSuspendedReasons(mAdminComponentName);
  }

  @Override
  public void enableSystemApp(
      String packageName, Consumer<Void> onSuccess, Consumer<Exception> onError) {
    Log.d(TAG, "enableSystemApp(" + packageName + ")");

    try {
      mDevicePolicyManager.enableSystemApp(mAdminComponentName, packageName);
      onSuccess.accept(null);
    } catch (Exception e) {
      onError.accept(e);
    }
  }

  @Override
  public void enableSystemApp(
      Intent intent, Consumer<Integer> onSuccess, Consumer<Exception> onError) {
    Log.d(TAG, "enableSystemApp(" + intent + ")");

    try {
      int result = mDevicePolicyManager.enableSystemApp(mAdminComponentName, intent);
      Log.d(TAG, "returning " + result + " activities");
      onSuccess.accept(result);
    } catch (Exception e) {
      onError.accept(e);
    }
  }

  @Override
  public List<String> getDisabledSystemApps() {
    // Disabled system apps list = {All system apps} - {Enabled system apps}
    List<String> disabledSystemApps = new ArrayList<String>();
    // This list contains both enabled and disabled apps.
    List<ApplicationInfo> allApps =
        mPackageManager.getInstalledApplications(PackageManager.GET_UNINSTALLED_PACKAGES);
    Collections.sort(allApps, new ApplicationInfo.DisplayNameComparator(mPackageManager));
    // This list contains all enabled apps.
    List<ApplicationInfo> enabledApps =
        mPackageManager.getInstalledApplications(0 /* Default flags */);
    Set<String> enabledAppsPkgNames = new HashSet<String>();
    for (ApplicationInfo applicationInfo : enabledApps) {
      enabledAppsPkgNames.add(applicationInfo.packageName);
    }
    for (ApplicationInfo applicationInfo : allApps) {
      // Interested in disabled system apps only.
      if (!enabledAppsPkgNames.contains(applicationInfo.packageName)
          && (applicationInfo.flags & ApplicationInfo.FLAG_SYSTEM) != 0) {
        disabledSystemApps.add(applicationInfo.packageName);
      }
    }
    Log.d(TAG, "getDisabledSystemApps(): returning " + disabledSystemApps.size() + " apps");
    return disabledSystemApps;
  }

  @Override
  public void setLockTaskPackages(
      String[] packages, Consumer<Void> onSuccess, Consumer<Exception> onError) {
    Log.d(TAG, "setLockTaskPackages(" + Arrays.toString(packages) + ")");
    try {
      mDevicePolicyManager.setLockTaskPackages(mAdminComponentName, packages);
      onSuccess.accept(null);
    } catch (Exception e) {
      onError.accept(e);
    }
  }

  @Override
  public void setApplicationHidden(
      String packageName, boolean hidden, Consumer<Void> onSuccess, Consumer<Exception> onError) {
    Log.d(TAG, "setApplicationHidden(" + packageName + ", " + hidden + ")");

    try {
      mDevicePolicyManager.setApplicationHidden(mAdminComponentName, packageName, hidden);
      onSuccess.accept(null);
    } catch (Exception e) {
      onError.accept(e);
    }
  }

  @Override
  public boolean isApplicationHidden(String packageName) throws NameNotFoundException {
    return mDevicePolicyManager.isApplicationHidden(mAdminComponentName, packageName);
  }

  @Override
  public String[] getLockTaskPackages() {
    return mDevicePolicyManager.getLockTaskPackages(mAdminComponentName);
  }

  @Override
  public void setLockTaskFeatures(
      int flags, Consumer<Void> onSuccess, Consumer<Exception> onError) {
    String features = Util.lockTaskFeaturesToString(flags);
    Log.d(TAG, "setLockTaskFeatures(" + features + ")");
    try {
      mDevicePolicyManager.setLockTaskFeatures(mAdminComponentName, flags);
      onSuccess.accept(null);
    } catch (Exception e) {
      onError.accept(e);
    }
  }

  @Override
  public int getLockTaskFeatures() {
    int flags = mDevicePolicyManager.getLockTaskFeatures(mAdminComponentName);
    Log.d(
        TAG, "getLockTaskFeatures(): " + Util.lockTaskFeaturesToString(flags) + " (" + flags + ")");
    return flags;
  }

  @Override
  public boolean isLockTaskPermitted(String packageName) {
    return mDevicePolicyManager.isLockTaskPermitted(packageName);
  }

  @Override
  public Bundle getApplicationRestrictions(String packageName) {
    return mDevicePolicyManager.getApplicationRestrictions(mAdminComponentName, packageName);
  }

  @Override
  public Bundle getSelfRestrictions() {
    return mUserManager.getApplicationRestrictions(mAdminComponentName.getPackageName());
  }

  @Override
  public void setApplicationRestrictions(
      String packageName, Bundle settings, Consumer<Void> onSuccess, Consumer<Exception> onError) {
    Log.d(TAG, "setApplicationRestrictions(" + packageName + ")");
    try {
      mDevicePolicyManager.setApplicationRestrictions(mAdminComponentName, packageName, settings);
      onSuccess.accept(null);
    } catch (Exception e) {
      onError.accept(e);
    }
  }

  @Override
  public void setPermissionGrantState(
      String packageName,
      String permission,
      int grantState,
      Consumer<Void> onSuccess,
      Consumer<Exception> onError) {
    String stateName = Util.grantStateToString(grantState);
    Log.d(TAG, "setPermissionGrantState(" + packageName + ", " + permission + "): " + stateName);
    try {
      boolean success =
          mDevicePolicyManager.setPermissionGrantState(
              mAdminComponentName, packageName, permission, grantState);
      if (success) {
        onSuccess.accept(null);
      } else {
        onError.accept(
            new FailedOperationException(
                "setPermissionGrantState(%s, %s, %s)", packageName, permission, stateName));
      }
    } catch (Exception e) {
      onError.accept(e);
    }
  }

  @Override
  public int getPermissionGrantState(String packageName, String permission) {
    int grantState =
        mDevicePolicyManager.getPermissionGrantState(mAdminComponentName, packageName, permission);
    Log.d(
        TAG,
        "getPermissionGrantState("
            + packageName
            + ", "
            + permission
            + "): "
            + Util.grantStateToString(grantState));
    return grantState;
  }

  @Override
  public boolean canAdminGrantSensorsPermissions() {
    boolean can = mDevicePolicyManager.canAdminGrantSensorsPermissions();
    Log.d(TAG, "canAdminGrantSensorsPermissions(): " + can);

    return can;
  }

  @Override
  public void setLocationEnabled(
      boolean enabled, Consumer<Void> onSuccess, Consumer<Exception> onError) {
    Log.d(TAG, "setLocationEnabled(" + enabled + ")");
    try {
      mDevicePolicyManager.setLocationEnabled(mAdminComponentName, enabled);
      onSuccess.accept(null);
    } catch (Exception e) {
      onError.accept(e);
    }
  }

  @Override
  public boolean isLocationEnabled() {
    return mLocationManager.isLocationEnabled();
  }

  @Override
  public void setDeviceOwnerLockScreenInfo(
      CharSequence info, Consumer<Void> onSuccess, Consumer<Exception> onError) {
    Log.d(TAG, "setDeviceOwnerLockScreenInfo(" + info + ")");
    try {
      mDevicePolicyManager.setDeviceOwnerLockScreenInfo(mAdminComponentName, info);
      onSuccess.accept(null);
    } catch (Exception e) {
      onError.accept(e);
    }
  }

  public void setKeyguardDisabled(
      boolean disabled, Consumer<Void> onSuccess, Consumer<Exception> onError) {
    Log.d(TAG, "setKeyguardDisabled(" + disabled + ")");
    try {
      if (mDevicePolicyManager.setKeyguardDisabled(mAdminComponentName, disabled)) {
        onSuccess.accept(null);
      } else {
        onError.accept(new InvalidResultException("false", "setKeyguardDisabled(%b)", disabled));
      }
    } catch (Exception e) {
      onError.accept(e);
    }
  }

  @Override
  public void setKeyguardDisabledFeatures(
      int which, Consumer<Void> onSuccess, Consumer<Exception> onError) {
    String features = Util.keyguardDisabledFeaturesToString(which);
    Log.d(TAG, "setKeyguardDisabledFeatures(" + features + ")");
    try {
      mDevicePolicyManager.setKeyguardDisabledFeatures(mAdminComponentName, which);
      onSuccess.accept(null);
    } catch (Exception e) {
      onError.accept(e);
    }
  }

  public int getKeyguardDisabledFeatures() {
    int which = mDevicePolicyManager.getKeyguardDisabledFeatures(mAdminComponentName);
    Log.d(
        TAG,
        "getKeyguardDisabledFeatures(): "
            + Util.keyguardDisabledFeaturesToString(which)
            + " ("
            + which
            + ")");
    return which;
  }

  @Override
  public void setCameraDisabled(
      boolean disabled, Consumer<Void> onSuccess, Consumer<Exception> onError) {
    Log.d(TAG, "setCameraDisabled(" + disabled + ")");
    try {
      mDevicePolicyManager.setCameraDisabled(mAdminComponentName, disabled);
      onSuccess.accept(null);
    } catch (Exception e) {
      onError.accept(e);
    }
  }

  @Override
  public boolean getCameraDisabled() {
    return mDevicePolicyManager.getCameraDisabled(mAdminComponentName);
  }

  @Override
  public boolean getCameraDisabledByAnyAdmin() {
    return mDevicePolicyManager.getCameraDisabled(/* admin= */ null);
  }

  @Override
  public void setStatusBarDisabled(
      boolean disabled, Consumer<Void> onSuccess, Consumer<Exception> onError) {
    Log.d(TAG, "setStatusBarDisabled(" + disabled + ")");
    try {
      mDevicePolicyManager.setStatusBarDisabled(mAdminComponentName, disabled);
      onSuccess.accept(null);
    } catch (Exception e) {
      onError.accept(e);
    }
  }

  @Override
  public void setMaximumFailedPasswordsForWipe(int max, Consumer<Void> onSuccess,
      Consumer<Exception> onError) {
    Log.d(TAG, "setMaximumFailedPasswordsForWipe(" + max + ")");
    try {
      mDevicePolicyManager.setMaximumFailedPasswordsForWipe(mAdminComponentName, max);
      onSuccess.accept(null);
    } catch (Exception e) {
      onError.accept(e);
    }
  }

  @Override
  public int getMaximumFailedPasswordsForWipe() {
    return mDevicePolicyManager.getMaximumFailedPasswordsForWipe(mAdminComponentName);
  }

  @Override
  public CharSequence getDeviceOwnerLockScreenInfo() {
    return mDevicePolicyManager.getDeviceOwnerLockScreenInfo();
  }

  @Override
  public void installExistingPackage(String packageName, Consumer<Void> onSuccess,
      Consumer<Exception> onError) {
    Log.d(TAG, "installExistingPackage(" + packageName + ")");
    try {
      mDevicePolicyManager.installExistingPackage(mAdminComponentName, packageName);
      onSuccess.accept(null);
    } catch (Exception e) {
      onError.accept(e);
    }
  }

  @Override
  public void setUninstallBlocked(String packageName, boolean uninstallBlocked,
      Consumer<Void> onSuccess, Consumer<Exception> onError) {
    Log.d(TAG, "setUninstallBlocked(" + packageName + ", " + uninstallBlocked + ")");
    try {
      mDevicePolicyManager.setUninstallBlocked(mAdminComponentName, packageName, uninstallBlocked);
      onSuccess.accept(null);
    } catch (Exception e) {
      onError.accept(e);
    }
  }

  @Override
  public boolean isUninstallBlocked(String packageName) {
    boolean isIt = mDevicePolicyManager.isUninstallBlocked(mAdminComponentName, packageName);
    Log.d(TAG, "isUninstallBlocked(" + packageName + "): " + isIt);
    return isIt;
  }

  @Override
  public void setSecureSetting(String setting, String value, @NonNull Consumer<Void> onSuccess,
      @NonNull Consumer<Exception> onError) {
    Log.d(TAG, "setSecureSetting(" + setting + "=" + value + ")");
    try {
      mDevicePolicyManager.setSecureSetting(mAdminComponentName, setting, value);
      onSuccess.accept(null);
    } catch (Exception e) {
      onError.accept(e);
    }
  }

  @Override
  public void setGlobalSetting(String setting, String value, @NonNull Consumer<Void> onSuccess,
      @NonNull Consumer<Exception> onError) {
    Log.d(TAG, "setGlobalSetting(" + setting + "=" + value + ")");
    try {
      mDevicePolicyManager.setGlobalSetting(mAdminComponentName, setting, value);
      onSuccess.accept(null);
    } catch (Exception e) {
      onError.accept(e);
    }
  }

  @Override
  public boolean isDeviceIdAttestationSupported() {
    boolean isIt = mDevicePolicyManager.isDeviceIdAttestationSupported();
    Log.d(TAG, "isDeviceIdAttestationSupported(): " + isIt);
    return isIt;
  }

  @Override
  public boolean isUniqueDeviceAttestationSupported() {
    boolean isIt = mDevicePolicyManager.isUniqueDeviceAttestationSupported();
    Log.d(TAG, "isUniqueDeviceAttestationSupported(): " + isIt);
    return isIt;
  }

  @Override
  public boolean hasKeyPair(String alias) {
    boolean hasIt = mDevicePolicyManager.hasKeyPair(alias);
    Log.d(TAG, "hasKeyPair(" + alias + "): " + hasIt);
    return hasIt;
  }

  @Override
  public void generateKeyPair(String algorithm, KeyGenParameterSpec keySpec, int idAttestationFlags,
      Consumer<AttestedKeyPair> onSuccess, Consumer<Exception> onError) {
    Log.d(TAG, "generateKeyPair(alg=" + algorithm + ", spec=" + keySpec + ", flags=" 
      + idAttestationFlags + ")");
    try {
      AttestedKeyPair pair = mDevicePolicyManager.generateKeyPair(mAdminComponentName, algorithm,
          keySpec,idAttestationFlags);
      onSuccess.accept(pair);
    } catch (Exception e) {
      onError.accept(e);
    }
  }

  @Override
  public void removeKeyPair(String alias, Consumer<Boolean> onSuccess,
      Consumer<Exception> onError) {
    Log.d(TAG, "removeKeyPair(" + alias + ")");
    try {
      boolean removed = mDevicePolicyManager.removeKeyPair(mAdminComponentName, alias);
      Log.d(TAG, "removed: " + removed);
      onSuccess.accept(removed);
    } catch (Exception e) {
      onError.accept(e);
    }
  }

  @Override
  public void grantKeyPairToApp(String alias, String packageName, Consumer<Boolean> onSuccess,
      Consumer<Exception> onError) {
    Log.d(TAG, "grantKeyPairToApp(" + alias + ", " + packageName + ")");
    try {
      boolean removed = mDevicePolicyManager.grantKeyPairToApp(mAdminComponentName, alias,
          packageName);
      Log.d(TAG, "granted: " + removed);
      onSuccess.accept(removed);
    } catch (Exception e) {
      onError.accept(e);
    }
  }

  @Override
  public Map<Integer, Set<String>> getKeyPairGrants(String alias) {
    Map<Integer, Set<String>> grants = mDevicePolicyManager.getKeyPairGrants(alias);
    Log.d(TAG, "getKeyPairGrants(" + alias + "): " + grants);
    return grants;
  }

  @Override
  public void revokeKeyPairFromApp(String alias, String packageName, Consumer<Boolean> onSuccess,
      Consumer<Exception> onError) {
    Log.d(TAG, "revokeKeyPairFromApp(" + alias + ", " + packageName + ")");
    try {
      boolean removed = mDevicePolicyManager.revokeKeyPairFromApp(mAdminComponentName, alias,
          packageName);
      Log.d(TAG, "revoked: " + removed);
      onSuccess.accept(removed);
    } catch (Exception e) {
      onError.accept(e);
    }
  }

  @Override
  public void setDelegatedScopes(String delegatePackage, List<String> scopes,
      Consumer<Void> onSuccess, Consumer<Exception> onError) {
    Log.d(TAG, "setDelegatedScopes(" + delegatePackage + ", " + scopes + ")");
    try {
      mDevicePolicyManager.setDelegatedScopes(mAdminComponentName, delegatePackage, scopes);
      Log.d(TAG, "set successfully");
      onSuccess.accept(null);
    } catch (Exception e) {
      onError.accept(e);
    }
  }

  @Override
  public List<String> getDelegatedScopes(String delegatePackage) {
    List<String> scopes = mDevicePolicyManager.getDelegatedScopes(mAdminComponentName,
        delegatePackage);
    Log.d(TAG, "getDelegatedScopes(" + delegatePackage + "): " + scopes + ")");
    return scopes;
  }

  @Override
  public List<String> getDelegatePackages(String delegationScope) {
    List<String> packages = mDevicePolicyManager.getDelegatePackages(mAdminComponentName,
        delegationScope);
    Log.d(TAG, "getDelegatedPackages(" + packages + "): " + packages + ")");
    return packages;
  }

  @Override
  public void setMeteredDataDisabledPackages(
      List<String> packageNames, Consumer<List<String>> onSuccess, Consumer<Exception> onError) {
    Log.d(TAG, "setMeteredDataDisabledPackages(packageNames): " + packageNames);

    try {
      mDevicePolicyManager.setMeteredDataDisabledPackages(mAdminComponentName, packageNames);
      Log.d(TAG, "set successfully");
      onSuccess.accept(packageNames);
    } catch (Exception e) {
      onError.accept(e);
    }
  }

  @Override
  public List<String> getMeteredDataDisabledPackages() {
    List<String> meteredDataDisabledPackages =
        mDevicePolicyManager.getMeteredDataDisabledPackages(mAdminComponentName);
    Log.d(TAG, "getMeteredDataDisabledPackages(): " + meteredDataDisabledPackages);
    return meteredDataDisabledPackages;
  }

  @Override
  public String toString() {
    return "DevicePolicyManagerGatewayImpl[" + mAdminComponentName + "]";
  }

  private long callMethodThatReturnsLongUsingReflection(String method) {
      try {
          long result = ReflectionUtil.invoke(mDevicePolicyManager, method);
          Log.d(TAG, method + "(): " + result);
          return result;
      } catch (Exception e) {
          Log.e(TAG, "Error calling " + method, e);
          return -1;
      }
  }

  private static void onSuccessLog(String template, Object... args) {
    Util.onSuccessLog(TAG, template, args);
  }

  private static void onErrorLog(Exception e, String template, Object... args) {
    Util.onErrorLog(TAG, e, template, args);
  }
}
