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

import android.app.admin.DevicePolicyManager;
import android.app.admin.NetworkEvent;
import android.app.admin.SecurityLog.SecurityEvent;
import android.content.ComponentName;
import android.content.Intent;
import android.content.IntentFilter;
import android.content.pm.PackageManager;
import android.content.pm.PackageManager.NameNotFoundException;
import android.graphics.Bitmap;
import android.os.Bundle;
import android.os.PersistableBundle;
import android.os.UserHandle;
import android.os.UserManager;
import android.security.AttestedKeyPair;
import android.security.keystore.KeyGenParameterSpec;
import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.function.Consumer;

/**
 * Interface used to abstract calls to {@link android.app.admin.DevicePolicyManager}.
 *
 * <p>Each of its methods takes 2 callbacks: one called when the underlying call succeeds, the other
 * one called when it throws an exception.
 */
public interface DevicePolicyManagerGateway {

  /** Gets the admin component associated with the {@link android.app.admin.DevicePolicyManager}. */
  @NonNull
  ComponentName getAdmin();

  /** Gets the {@link android.app.admin.DevicePolicyManager}. */
  @NonNull
  DevicePolicyManager getDevicePolicyManager();

  /** See {@link android.app.admin.DevicePolicyManager#isDeviceOwnerApp(String)}. */
  boolean isDeviceOwnerApp();

  /** See {@link android.app.admin.DevicePolicyManager#isProfileOwnerApp(String)}. */
  boolean isProfileOwnerApp();

  // TODO(b/171350084): use in other places
  /**
   * See {@link
   * android.app.admin.DevicePolicyManager#isOrganizationOwnedDeviceWithManagedProfile()}.
   */
  boolean isOrganizationOwnedDeviceWithManagedProfile();

  /** See {@link android.os.UserManager#isHeadlessSystemUserMode()}. */
  boolean isHeadlessSystemUserMode();

  /** See {@link android.os.UserManager#isUserForeground()}. */
  boolean isUserForeground();

  /** See {@link android.app.admin.DevicePolicyManager#listForegroundAffiliatedUsers()}. */
  List<UserHandle> listForegroundAffiliatedUsers();

  /**
   * See {@link
   * android.app.admin.DevicePolicyManager#createAndManageUser(android.content.ComponentName,
   * String, android.content.ComponentName, android.os.PersistableBundle, int)}.
   */
  void createAndManageUser(
      @Nullable String name,
      int flags,
      @NonNull Consumer<UserHandle> onSuccess,
      @NonNull Consumer<Exception> onError);

  /**
   * See {@link android.app.admin.DevicePolicyManager#setUserIcon(android.content.ComponentName,
   * android.graphics.Bitmap)}.
   */
  void setUserIcon(
      @NonNull Bitmap icon,
      @NonNull Consumer<Void> onSuccess,
      @NonNull Consumer<Exception> onError);

  /**
   * See {@link
   * android.app.admin.DevicePolicyManager#setStartUserSessionMessage(android.content.ComponentName,
   * CharSequence)}.
   */
  void setStartUserSessionMessage(
      @Nullable CharSequence message,
      @NonNull Consumer<Void> onSuccess,
      @NonNull Consumer<Exception> onError);

  /**
   * See {@link
   * android.app.admin.DevicePolicyManager#getStartUserSessionMessage(android.content.ComponentName)}.
   */
  @NonNull
  CharSequence getStartUserSessionMessage();

  /**
   * See {@link
   * android.app.admin.DevicePolicyManager#setEndUserSessionMessage(android.content.ComponentName,
   * CharSequence)}.
   */
  void setEndUserSessionMessage(
      @Nullable CharSequence message,
      @NonNull Consumer<Void> onSuccess,
      @NonNull Consumer<Exception> onError);

  /**
   * See {@link
   * android.app.admin.DevicePolicyManager#getEndUserSessionMessage(android.content.ComponentName)}.
   */
  @NonNull
  CharSequence getEndUserSessionMessage();

  /** @see {@link android.os.UserManager#getUserForSerialNumber(long)}. */
  @Nullable
  UserHandle getUserHandle(long serialNumber);

  /** @see {@link android.os.UserManager#getSerialNumber(UserHandle)}. */
  long getSerialNumber(@NonNull UserHandle user);

  /**
   * See {@link android.app.admin.DevicePolicyManager#removeUser(android.content.ComponentName,
   * UserHandle)}.
   */
  void removeUser(
      @NonNull UserHandle userHandle,
      @NonNull Consumer<Void> onSuccess,
      @NonNull Consumer<Exception> onError);

  /**
   * See {@link android.app.admin.DevicePolicyManager#switchUser(android.content.ComponentName,
   * UserHandle)}.
   */
  void switchUser(
      @NonNull UserHandle userHandle,
      @NonNull Consumer<Void> onSuccess,
      @NonNull Consumer<Exception> onError);

  /**
   * See {@link
   * android.app.admin.DevicePolicyManager#startUserInBackground(android.content.ComponentName,
   * UserHandle)}.
   */
  void startUserInBackground(
      @NonNull UserHandle userHandle,
      @NonNull Consumer<Integer> onSuccess,
      @NonNull Consumer<Exception> onError);

  /**
   * See {@link android.app.admin.DevicePolicyManager#stopUser(android.content.ComponentName,
   * UserHandle)}.
   */
  void stopUser(
      @NonNull UserHandle userHandle,
      @NonNull Consumer<Integer> onSuccess,
      @NonNull Consumer<Exception> onError);

  /** See {@link android.app.admin.DevicePolicyManager#isLogoutEnabled()}. */
  boolean isLogoutEnabled();

  /**
   * See android.app.admin.DevicePolicyManager#setLogoutEnabled(
   * android.content.ComponentName, boolean)
   */
  void setLogoutEnabled(
      boolean enabled, @NonNull Consumer<Void> onSuccess, @NonNull Consumer<Exception> onError);

  /**
   * See {@link android.app.admin.DevicePolicyManager#logoutUser(android.content.ComponentName)}.
   */
  void logoutUser(
      @NonNull Consumer<Integer> onSuccess,
      @NonNull Consumer<Exception> onError);

  /** See {@link android.app.admin.DevicePolicyManager#isAffiliatedUser()}. */
  boolean isAffiliatedUser();

  /**
   * See {@link
   * android.app.admin.DevicePolicyManager#setAffiliationIds(android.content.ComponentName, Set)}.
   */
  void setAffiliationIds(@NonNull Set<String> ids);

  /**
   * See {@link
   * android.app.admin.DevicePolicyManager#getAffiliationIds(android.content.ComponentName)}.
   */
  @NonNull
  Set<String> getAffiliationIds();

  /**
   * See {@link
   * android.app.admin.DevicePolicyManager#getUserRestrictions(android.content.ComponentName)}.
   */
  @NonNull
  Set<String> getUserRestrictions();

  /**
   * See {@link
   * android.app.admin.DevicePolicyManager#setUserRestriction(android.content.ComponentName,
   * String)} and {@link
   * android.app.admin.DevicePolicyManager#clearUserRestriction(android.content.ComponentName,
   * String)}.
   */
  void setUserRestriction(
      @NonNull String userRestriction,
      boolean enabled,
      @NonNull Consumer<Void> onSuccess,
      @NonNull Consumer<Exception> onError);

  /**
   * Same as {@link #setUserRestriction(String, boolean, Consumer, Consumer)}, but ignoring
   * callbacks.
   */
  void setUserRestriction(@NonNull String userRestriction, boolean enabled);

  /** See {@link android.os.UserManager#hasUserRestriction(String)}. */
  boolean hasUserRestriction(@NonNull String userRestriction);

  /** See {@link android.app.admin.DevicePolicyManager#lockNow()}. */
  void lockNow(@NonNull Consumer<Void> onSuccess, @NonNull Consumer<Exception> onError);

  /** See {@link android.app.admin.DevicePolicyManager#lockNow(int)}. */
  void lockNow(int flags, @NonNull Consumer<Void> onSuccess, @NonNull Consumer<Exception> onError);

  /** See {@link android.app.admin.DevicePolicyManager#reboot()}. */
  void reboot(@NonNull Consumer<Void> onSuccess, @NonNull Consumer<Exception> onError);

  /** See {@link android.app.admin.DevicePolicyManager#wipeData(int)}. */
  void wipeData(int flags, @NonNull Consumer<Void> onSuccess, @NonNull Consumer<Exception> onError);

  /** See {@link android.app.admin.DevicePolicyManager#wipeDevice(int)}. */
  void wipeDevice(
      int flags, @NonNull Consumer<Void> onSuccess, @NonNull Consumer<Exception> onError);

  /**
   * See {@link
   * android.app.admin.DevicePolicyManager#requestBugreport(android.content.ComponentName)}.
   */
  void requestBugreport(@NonNull Consumer<Void> onSuccess, @NonNull Consumer<Exception> onError);

  /**
   * See {@link android.app.admin.DevicePolicyManager#getLastBugReportRequestTime()}.
   */
  long getLastBugReportRequestTime();

  /**
   * See {@link
   * android.app.admin.DevicePolicyManager#setNetworkLoggingEnabled(android.content.ComponentName,
   * boolean)}.
   */
  void setNetworkLoggingEnabled(
      boolean enabled, @NonNull Consumer<Void> onSuccess, @NonNull Consumer<Exception> onError);

  /** Same as {@link #setNetworkLoggingEnabled(boolean, Consumer, Consumer)}, but ignoring callbacks. */
  void setNetworkLoggingEnabled(boolean enabled);

  /**
   * See {@link
   * android.app.admin.DevicePolicyManager#isNetworkLoggingEnabled(android.content.ComponentName)}.
   */
  boolean isNetworkLoggingEnabled();

  /**
   * See {@link android.app.admin.DevicePolicyManager#getLastNetworkLogRetrievalTime()}.
   */
  long getLastNetworkLogRetrievalTime();

  /**
   * See {@link android.app.admin.DevicePolicyManager#retrieveNetworkLogs(android.content.ComponentName, long)}.
   */
  List<NetworkEvent> retrieveNetworkLogs(long batchToken);

  /**
   * See {@link
   * android.app.admin.DevicePolicyManager#setSecurityLoggingEnabled(android.content.ComponentName,
   * boolean)}.
   */
  void setSecurityLoggingEnabled(
      boolean enabled, @NonNull Consumer<Void> onSuccess, @NonNull Consumer<Exception> onError);

  /**
   * See {@link
   * android.app.admin.DevicePolicyManager#isSecurityLoggingEnabled(android.content.ComponentName)}.
   */
  boolean isSecurityLoggingEnabled();

  /**
   * See {@link android.app.admin.DevicePolicyManager#getLastSecurityLogRetrievalTime()}.
   */
  long getLastSecurityLogRetrievalTime();

  /**
   * See {@link android.app.admin.DevicePolicyManager#retrieveSecurityLogs(android.content.ComponentName)}.
   */
  List<SecurityEvent> retrieveSecurityLogs();

  /**
   * See {@link android.app.admin.DevicePolicyManager#retrievePreRebootSecurityLogs(android.content.ComponentName)}.
   */
  List<SecurityEvent> retrievePreRebootSecurityLogs();

  /**
   * See {@link
   * android.app.admin.DevicePolicyManager#setOrganizationName(android.content.ComponentName,
   * String)}.
   */
  void setOrganizationName(
      @Nullable CharSequence title,
      @NonNull Consumer<Void> onSuccess,
      @NonNull Consumer<Exception> onError);

  /**
   * See {@link
   * android.app.admin.DevicePolicyManager#getOrganizationName(android.content.ComponentName)}.
   */
  @Nullable
  CharSequence getOrganizationName();

  /**
   * See {@link
   * android.app.admin.DevicePolicyManager#setUserControlDisabledPackages(android.content.ComponentName,
   * List<String>)}.
   */
  void setUserControlDisabledPackages(
      @Nullable List<String> packages,
      @NonNull Consumer<Void> onSuccess,
      @NonNull Consumer<Exception> onError);

  /**
   * See {@link
   * android.app.admin.DevicePolicyManager#getUserControlDisabledPackages(android.content.ComponentName)}.
   */
  @NonNull
  List<String> getUserControlDisabledPackages();

  /**
   * See {@link
   * android.app.admin.DevicePolicyManager#setCrossProfilePackages(android.content.ComponentName,
   * Set<String>)}.
   */
  void setCrossProfilePackages(
      @NonNull Set<String> packages,
      @NonNull Consumer<Void> onSuccess,
      @NonNull Consumer<Exception> onError);

  /**
   * See {@link
   * android.app.admin.DevicePolicyManager#getCrossProfilePackages(android.content.ComponentName)}.
   */
  @NonNull
  Set<String> getCrossProfilePackages();

  /**
   * See {@link android.app.admin.DevicePolicyManager#setPermittedInputMethods(
   * android.content.ComponentName, List)}.
   */
  boolean setPermittedInputMethods(
      List<String> packageNames,
      @NonNull Consumer<Void> onSuccess,
      @NonNull Consumer<Exception> onError);

  /**
   * See {@link android.app.admin.DevicePolicyManager#setPermittedInputMethods(
   * android.content.ComponentName, List)}.
   */
  boolean setPermittedInputMethods(List<String> packageNames);

  /**
   * See {@link
   * android.app.admin.DevicePolicyManager#removeActiveAdmin(android.content.ComponentName)}.
   */
  void removeActiveAdmin(@NonNull Consumer<Void> onSuccess, @NonNull Consumer<Exception> onError);

  /**
   * See {@link
   * android.app.admin.DevicePolicyManager#clearDeviceOwnerApp(android.content.ComponentName)}.
   */
  void clearDeviceOwnerApp(@NonNull Consumer<Void> onSuccess, @NonNull Consumer<Exception> onError);

  /**
   * See {@link
   * android.app.admin.DevicePolicyManager#clearProfileOwner(android.content.ComponentName)}.
   */
  void clearProfileOwner(@NonNull Consumer<Void> onSuccess, @NonNull Consumer<Exception> onError);

  /**
   * See {@link android.app.admin.DevicePolicyManager#setPasswordQuality(ComponentName, int)
   */
  void setPasswordQuality(
      int quality, @NonNull Consumer<Void> onSuccess, @NonNull Consumer<Exception> onError);

  /**
   * See {@link android.app.admin.DevicePolicyManager#getPasswordQuality(ComponentName)
   */
  int getPasswordQuality();

  /**
   * See {@link android.app.admin.DevicePolicyManager#setRequiredPasswordComplexity(int)
   */
  void setRequiredPasswordComplexity(
      int quality, @NonNull Consumer<Void> onSuccess, @NonNull Consumer<Exception> onError);

  /**
   * See {@link android.app.admin.DevicePolicyManager#getRequiredPasswordComplexity()
   */
  int getRequiredPasswordComplexity();

  /**
   * See {@link android.app.admin.DevicePolicyManager#isActivePasswordSufficient()
   */
  boolean isActivePasswordSufficient();

  /**
   * See {@link android.app.admin.DevicePolicyManager#isActivePasswordSufficientForDeviceRequirement()
   */
  boolean isActivePasswordSufficientForDeviceRequirement();

  /**
   * See {@link android.app.admin.DevicePolicyManager#transferOwnership(ComponentName,
   * ComponentName, android.os.PersistableBundle)}.
   */
  void transferOwnership(
      @NonNull ComponentName target,
      @Nullable PersistableBundle bundle,
      @NonNull Consumer<Void> onSuccess,
      @NonNull Consumer<Exception> onError);

  /**
   * See android.app.admin.DevicePolicyManager#setUsbDataSignalingEnabled(
   * android.content.ComponentName, boolean)
   */
  void setUsbDataSignalingEnabled(
      boolean enabled, @NonNull Consumer<Void> onSuccess, @NonNull Consumer<Exception> onError);

  /**
   * Same as {@link #setUsbDataSignalingEnabled(boolean, Consumer, Consumer)}, but ignoring
   * callbacks.
   */
  void setUsbDataSignalingEnabled(boolean enabled);

  /**
   * See {@link
   * android.app.admin.DevicePolicyManager#canUsbDataSignalingBeDisabled()(ComponentName)}.
   */
  boolean canUsbDataSignalingBeDisabled();

  /**
   * See {@link
   * android.app.admin.DevicePolicyManager#setPreferentialNetworkServiceEnabled(ComponentName,
   * boolean)}.
   */
  void setPreferentialNetworkServiceEnabled(
      boolean enabled, @NonNull Consumer<Void> onSuccess, @NonNull Consumer<Exception> onError);

  /**
   * See {@link
   * android.app.admin.DevicePolicyManager#isPreferentialNetworkServiceEnabled(ComponentName)}.
   */
  boolean isPreferentialNetworkServiceEnabled();

  /**
   * See {@link android.app.admin.DevicePolicyManager#setPackagesSuspended(ComponentName, String[],
   * boolean)}.
   */
  void setPackagesSuspended(
      String[] packageNames,
      boolean suspended,
      @NonNull Consumer<String[]> onSuccess,
      @NonNull Consumer<Exception> onError);

  /**
   * See {@link android.app.admin.DevicePolicyManager#isPackageSuspended(ComponentName, String)}.
   */
  boolean isPackageSuspended(String packageName) throws NameNotFoundException;

  /**
   * See {@link android.app.admin.DevicePolicyManager#setApplicationHidden(ComponentName, String,
   * boolean)}.
   */
  void setApplicationHidden(
      String packageName,
      boolean suspended,
      @NonNull Consumer<Void> onSuccess,
      @NonNull Consumer<Exception> onError);

  /**
   * See {@link android.app.admin.DevicePolicyManager#isApplicationHidden(ComponentName, String)}.
   */
  boolean isApplicationHidden(String packageName) throws NameNotFoundException;

  /**
   * See {@link android.app.admin.DevicePolicyManager#setPersonalAppsSuspended(ComponentName,
   * boolean)}.
   */
  void setPersonalAppsSuspended(
      boolean suspended, @NonNull Consumer<Void> onSuccess, @NonNull Consumer<Exception> onError);

  /**
   * See {@link
   * android.app.admin.DevicePolicyManager#getPersonalAppsSuspendedReasons(ComponentName)}.
   */
  int getPersonalAppsSuspendedReasons();

  // TODO(b/171350084): use on CosuConfig
  /** See {@link android.app.admin.DevicePolicyManager#enableSystemApp(ComponentName, String)}. */
  void enableSystemApp(
      String packageName, @NonNull Consumer<Void> onSuccess, @NonNull Consumer<Exception> onError);

  /** See {@link android.app.admin.DevicePolicyManager#enableSystemApp(ComponentName, Intent)}. */
  void enableSystemApp(
      Intent intent, @NonNull Consumer<Integer> onSuccess, @NonNull Consumer<Exception> onError);

  /** Queries {@link PackageManager} to get the list of apps that are disabled for the user. */
  @NonNull
  List<String> getDisabledSystemApps();

  // TODO(b/171350084): use in other places
  /**
   * See {@link android.app.admin.DevicePolicyManager#setLockTaskPackages(ComponentName, String[])}.
   */
  void setLockTaskPackages(
      String[] packages, @NonNull Consumer<Void> onSuccess, @NonNull Consumer<Exception> onError);

  /** See {@link android.app.admin.DevicePolicyManager#getLockTaskPackages(ComponentName)}. */
  String[] getLockTaskPackages();

  /** See {@link android.app.admin.DevicePolicyManager#setLockTaskFeatures(ComponentName, int)}. */
  void setLockTaskFeatures(
      int flags, @NonNull Consumer<Void> onSuccess, @NonNull Consumer<Exception> onError);

  /** See {@link android.app.admin.DevicePolicyManager#getLockTaskFeatures(ComponentName)}. */
  int getLockTaskFeatures();

  /**
   * See {@link android.app.admin.DevicePolicyManager#isLockTaskPermitted(ComponentName, String)}.
   */
  boolean isLockTaskPermitted(String packageName);

  /**
   * See {@link android.app.admin.DevicePolicyManager#setApplicationRestrictions(ComponentName,
   * String, Bundle)}.
   */
  void setApplicationRestrictions(
      String packageName,
      Bundle settings,
      @NonNull Consumer<Void> onSuccess,
      @NonNull Consumer<Exception> onError);

  /**
   * See {@link android.app.admin.DevicePolicyManager#getApplicationRestrictions(ComponentName,
   * String)}.
   */
  Bundle getApplicationRestrictions(String packageName);

  /**
   * Gets this app's own restrictions using {@link UserManager#getApplicationRestrictions(String)}.
   */
  Bundle getSelfRestrictions();

  /**
   * See {@link android.app.admin.DevicePolicyManager#setPermissionGrantState(ComponentName, String,
   * String, int)}.
   */
  void setPermissionGrantState(
      String packageName,
      String permission,
      int grantState,
      @NonNull Consumer<Void> onSuccess,
      @NonNull Consumer<Exception> onError);

  /**
   * See {@link android.app.admin.DevicePolicyManager#getPermissionGrantState(ComponentName, String,
   * String)}.
   */
  int getPermissionGrantState(String packageName, String permission);

  /** See {@link android.app.admin.DevicePolicyManager#canAdminGrantSensorsPermissions()}. */
  boolean canAdminGrantSensorsPermissions();

  /**
   * See {@link android.app.admin.DevicePolicyManager#setLocationEnabled(ComponentName, boolean)}.
   */
  void setLocationEnabled(
      boolean enabled, @NonNull Consumer<Void> onSuccess, @NonNull Consumer<Exception> onError);

  /** See {@link android.location.LocationManager#isLocationEnabled()}. */
  boolean isLocationEnabled();

  /**
   * See {@link android.app.admin.DevicePolicyManager#setDeviceOwnerLockScreenInfo(ComponentName,
   * CharSequence)}.
   */
  void setDeviceOwnerLockScreenInfo(
      CharSequence info, @NonNull Consumer<Void> onSuccess, @NonNull Consumer<Exception> onError);

  /** See {@link android.app.admin.DevicePolicyManager#getDeviceOwnerLockScreenInfo()}. */
  CharSequence getDeviceOwnerLockScreenInfo();

  /**
   * See {@link android.app.admin.DevicePolicyManager#setKeyguardDisabled(ComponentName, boolean)}.
   */
  void setKeyguardDisabled(
      boolean disabled, @NonNull Consumer<Void> onSuccess, @NonNull Consumer<Exception> onError);

  /**
   * See {@link android.app.admin.DevicePolicyManager#setKeyguardDisabledFeatures(ComponentName,
   * int)}.
   */
  void setKeyguardDisabledFeatures(
      int which, @NonNull Consumer<Void> onSuccess, @NonNull Consumer<Exception> onError);

  /**
   * See {@link android.app.admin.DevicePolicyManager#getKeyguardDisabledFeatures(ComponentName)}.
   */
  int getKeyguardDisabledFeatures();

  /**
   * See {@link android.app.admin.DevicePolicyManager#setCameraDisabled(ComponentName, boolean)}.
   */
  void setCameraDisabled(
      boolean disabled, @NonNull Consumer<Void> onSuccess, @NonNull Consumer<Exception> onError);

  /** See {@link android.app.admin.DevicePolicyManager#getCameraDisabled(ComponentName)}. */
  boolean getCameraDisabled();

  /**
   * Same as {@link android.app.admin.DevicePolicyManager#getCameraDisabled(ComponentName)} but
   * passing {@code null}.
   */
  boolean getCameraDisabledByAnyAdmin();

  /**
   * See {@link android.app.admin.DevicePolicyManager#setStatusBarDisabled(ComponentName, boolean)}.
   */
  void setStatusBarDisabled(
      boolean disabled, @NonNull Consumer<Void> onSuccess, @NonNull Consumer<Exception> onError);

  /**
   * See {@link android.app.admin.DevicePolicyManager#setScreenCaptureDisabled(ComponentName,
   * boolean)}.
   */
  void setScreenCaptureDisabled(
      boolean disabled, @NonNull Consumer<Void> onSuccess, @NonNull Consumer<Exception> onError);

  /**
   * See {@link android.app.admin.DevicePolicyManager#setMaximumFailedPasswordsForWipe(ComponentName, int)}.
   */
  void setMaximumFailedPasswordsForWipe(int max, @NonNull Consumer<Void> onSuccess,
      @NonNull Consumer<Exception> onError);

  /**
   * See {@link android.app.admin.DevicePolicyManager#getMaximumFailedPasswordsForWipe(ComponentName)}.
   */
  int getMaximumFailedPasswordsForWipe();

  /**
   * See {@link android.app.admin.DevicePolicyManager#installExistingPackage(ComponentName, String)}.
   */
  void installExistingPackage(String packageName, @NonNull Consumer<Void> onSuccess,
      @NonNull Consumer<Exception> onError);

  /**
   * See {@link android.app.admin.DevicePolicyManager#setUninstallBlocked(ComponentName, String, boolean)};
   */
  void setUninstallBlocked(@NonNull String packageName, boolean uninstallBlocked,
      @NonNull Consumer<Void> onSuccess, @NonNull Consumer<Exception> onError);

  /**
   * See {@link android.app.admin.DevicePolicyManager#isUninstallBlocked(ComponentName, String)};
   */
  boolean isUninstallBlocked(@NonNull String packageName);

  /**
   * See {@link android.app.admin.DevicePolicyManager#setSecureSetting(ComponentName, String,
   * String)}.
   */
  void setSecureSetting(String setting, String value, @NonNull Consumer<Void> onSuccess,
      @NonNull Consumer<Exception> onError);

  /**
   * See {@link android.app.admin.DevicePolicyManager#setGlobalSetting(ComponentName, String,
   * String)}.
   */
  void setGlobalSetting(String setting, String value, @NonNull Consumer<Void> onSuccess,
      @NonNull Consumer<Exception> onError);

  /**
   * See {@link android.app.admin.DevicePolicyManager#isDeviceIdAttestationSupported())}.
   */
  boolean isDeviceIdAttestationSupported();

  /**
   * See {@link android.app.admin.DevicePolicyManager#isUniqueDeviceAttestationSupported())}.
   */
  boolean isUniqueDeviceAttestationSupported();

  /**
   * See {@link android.app.admin.DevicePolicyManager#hasKeyPair(String))}.
   */
  boolean hasKeyPair(String alias);

  /**
   * See {@link android.app.admin.DevicePolicyManager#generateKeyPair(ComponentName, String,
   * android.security.keystore.KeyGenParameterSpec, int))}.
   */
  void generateKeyPair(@NonNull String algorithm, @NonNull KeyGenParameterSpec keySpec,
      int idAttestationFlags, @NonNull Consumer<AttestedKeyPair> onSuccess,
      @NonNull Consumer<Exception> onError);

  /**
   * See {@link android.app.admin.DevicePolicyManager#removeKeyPair(ComponentName, String))}.
   */
  void removeKeyPair(@NonNull String alias, @NonNull Consumer<Boolean> onSuccess,
      @NonNull Consumer<Exception> onError);

  /**
   * See {@link android.app.admin.DevicePolicyManager#grantKeyPairToApp(ComponentName, String, String)};
   */
  void grantKeyPairToApp(@NonNull String alias, @NonNull String packageName, 
      @NonNull Consumer<Boolean> onSuccess, @NonNull Consumer<Exception> onError);

  /**
   * See {@link android.app.admin.DevicePolicyManager#getKeyPairGrants(String)}
   */
  @NonNull Map<Integer, Set<String>> getKeyPairGrants(@NonNull String alias);

  /**
   * See {@link android.app.admin.DevicePolicyManager#revokeKeyPairFromApp(ComponentName, String, String)};
   */
  void revokeKeyPairFromApp(@NonNull String alias, @NonNull String packageName, 
      @NonNull Consumer<Boolean> onSuccess, @NonNull Consumer<Exception> onError);

  /**
   * See {@link android.app.admin.DevicePolicyManager#setDelegatedScopes(ComponentName, String, List<String>)};
   */
  void setDelegatedScopes(@NonNull String delegatePackage, @NonNull List<String> scopes,
      @NonNull Consumer<Void> onSuccess, @NonNull Consumer<Exception> onError);

  /**
   * See {@link android.app.admin.DevicePolicyManager#getDelegatedScopes(ComponentName, String)};
   */
  @NonNull List<String> getDelegatedScopes(@NonNull String delegatePackage);

  /**
   * See {@link android.app.admin.DevicePolicyManager#getDelegatePackages(ComponentName, String)};
   */
  @NonNull List<String> getDelegatePackages(@NonNull String delegationScope);

  /**
   * See {@link android.app.admin.DevicePolicyManager#setMeteredDataDisabledPackages(ComponentName,
   * List<String>)};
   */
  @NonNull
  void setMeteredDataDisabledPackages(@NonNull List<String> packageNames,
      @NonNull Consumer<List<String>> onSuccess, @NonNull Consumer<Exception> onError);

  /**
   * See {@link android.app.admin.DevicePolicyManager#getMeteredDataDisabledPackages(ComponentName)};
   */
  @NonNull List<String> getMeteredDataDisabledPackages();

  /**
   * See {@link android.app.admin.DevicePolicyManager#getSecondaryUsers(ComponentName)};
   */
  @NonNull List<UserHandle> getSecondaryUsers();

  /**
   * See {@link android.app.admin.DevicePolicyManager#addPersistentPreferredActivity(ComponentName, IntentFilter, ComponentName)};
   */
  void addPersistentPreferredActivity(ComponentName activityComponentName, IntentFilter filter, @NonNull Consumer<Boolean> onSuccess, @NonNull Consumer<Exception> onError);

  /**
   * See {@link android.app.admin.DevicePolicyManager#clearPackagePersistentPreferredActivities(ComponentName, String)};
   */
  void clearPackagePersistentPreferredActivities(@NonNull String packageName, 
      @NonNull Consumer<Boolean> onSuccess, @NonNull Consumer<Exception> onError);

  /**
   * Used on error callbacks to indicate a {@link android.app.admin.DevicePolicyManager} method call
   * failed.
   */
  @SuppressWarnings("serial")
  public static class InvalidResultException extends Exception {

    private final String mMethod;
    private final String mResult;

    /**
     * Default constructor.
     *
     * @param result result of the method call.
     * @param method method name template.
     * @param args method arguments.
     */
    public InvalidResultException(
        @NonNull String result, @NonNull String method, @NonNull Object... args) {
      mResult = result;
      mMethod = String.format(method, args);
    }

    @Override
    public String toString() {
      return "DPM." + mMethod + " returned " + mResult;
    }
  }

  /**
   * Used on error callbacks to indicate a {@link android.app.admin.DevicePolicyManager} method call
   * that returned {@code false}.
   */
  @SuppressWarnings("serial")
  public static final class FailedOperationException extends InvalidResultException {

    /**
     * Default constructor.
     *
     * @param method method name template.
     * @param args method arguments.
     */
    public FailedOperationException(@NonNull String method, @NonNull Object... args) {
      super("false", method, args);
    }
  }

  /**
   * Used on error callbacks to indicate a {@link android.app.admin.DevicePolicyManager} method call
   * that returned a user-related error.
   */
  @SuppressWarnings("serial")
  public static final class FailedUserOperationException extends InvalidResultException {

    /**
     * Default constructor.
     *
     * @param status user-related opeartion status.
     * @param method method name template.
     * @param args method arguments.
     */
    public FailedUserOperationException(
        int status, @NonNull String method, @NonNull Object... args) {
      super(userStatusToString(status), method, args);
    }

    private static String userStatusToString(int status) {
      switch (status) {
        case UserManager.USER_OPERATION_SUCCESS:
          return "SUCCESS";
        case UserManager.USER_OPERATION_ERROR_CURRENT_USER:
          return "ERROR_CURRENT_USER";
        case UserManager.USER_OPERATION_ERROR_LOW_STORAGE:
          return "ERROR_LOW_STORAGE";
        case UserManager.USER_OPERATION_ERROR_MANAGED_PROFILE:
          return "ERROR_MAX_MANAGED_PROFILE";
        case UserManager.USER_OPERATION_ERROR_MAX_RUNNING_USERS:
          return "ERROR_MAX_RUNNING_USERS";
        case UserManager.USER_OPERATION_ERROR_MAX_USERS:
          return "ERROR_MAX_USERS";
        case UserManager.USER_OPERATION_ERROR_UNKNOWN:
          return "ERROR_UNKNOWN";
        default:
          return "INVALID_STATUS:" + status;
      }
    }
  }
}
