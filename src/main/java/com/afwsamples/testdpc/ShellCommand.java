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

import static com.afwsamples.testdpc.util.flags.Flags.command;
import static com.afwsamples.testdpc.util.flags.Flags.namedParam;
import static com.afwsamples.testdpc.util.flags.Flags.optional;
import static com.afwsamples.testdpc.util.flags.Flags.ordinalParam;
import static com.afwsamples.testdpc.util.flags.Flags.repeated;

import android.annotation.TargetApi;
import android.app.admin.ConnectEvent;
import android.app.admin.DnsEvent;
import android.app.admin.NetworkEvent;
import android.app.admin.SecurityLog.SecurityEvent;
import android.content.ComponentName;
import android.content.Context;
import android.content.pm.PackageManager.NameNotFoundException;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.os.Build.VERSION_CODES;
import android.os.Bundle;
import android.os.Handler;
import android.os.HandlerThread;
import android.os.Process;
import android.os.UserHandle;
import android.security.AttestedKeyPair;
import android.security.keystore.KeyGenParameterSpec;
import android.security.keystore.KeyProperties;
import android.util.Log;
import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import com.afwsamples.testdpc.common.Util;
import com.afwsamples.testdpc.policy.SecurityLogsFragment;
import com.afwsamples.testdpc.util.flags.Flags;
import java.io.File;
import java.io.PrintWriter;
import java.net.InetAddress;
import java.text.SimpleDateFormat;
import java.util.Arrays;
import java.util.Collection;
import java.util.Date;
import java.util.Iterator;
import java.util.LinkedHashSet;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.concurrent.TimeUnit;
import java.util.stream.Collectors;

/**
 * Provides a CLI (command-line interface) to TestDPC through {@code dumpsys}.
 *
 * <p>Usage: {@code adb shell dumpsys activity service --user USER_ID com.afwsamples.testdpc CMD}.
 */
final class ShellCommand {
  private static final String TAG = "TestDPCShellCommand";

  private final Context mContext;
  private final PrintWriter mWriter;
  private final String[] mArgs;
  private final DevicePolicyManagerGateway mDevicePolicyManagerGateway;

  @Nullable // dynamically created on post() method
  private Handler mHandler;

  public ShellCommand(
      @NonNull Context context, @NonNull PrintWriter writer, @Nullable String[] args) {
    mContext = context;
    mWriter = writer;
    mArgs = args;
    mDevicePolicyManagerGateway = new DevicePolicyManagerGatewayImpl(context);
    Log.d(TAG, "constructor: pid=" + Process.myPid() + ", process name=" + Util.myProcessName()
        + ", args=" + Arrays.toString(args));
  }

  private static String suspendedToString(boolean suspended) {
    return suspended ? "SUSPENDED" : "NOT SUSPENDED";
  }

  private static String hiddenToString(boolean hidden) {
    return hidden ? "HIDDEN" : "VISIBLE";
  }

  private static String permittedToString(boolean permitted) {
    return permitted ? "PERMITTED" : "NOT PERMITTED";
  }

  public void run() {
    Flags flags = new Flags(mWriter);

    flags.registerCustomParser(
        UserHandle.class,
        (string, validator) -> {
          long serialNumber;

          try {
            serialNumber = Long.parseLong(string);
          } catch (NumberFormatException e) {
            return validator.invalid("UserHandle must be a long integer.");
          }

          UserHandle userHandle = mDevicePolicyManagerGateway.getUserHandle(serialNumber);
          if (userHandle == null) {
            return validator.invalid(String.format("User %d does not exist.", serialNumber));
          }

          return validator.valid(userHandle);
        });
    flags.registerCustomParser(
        File.class,
        (string, validator) -> {
          File file = UserIconContentProvider.getFile(mContext, string);

          if (!file.isFile()) {
            return validator.invalid(String.format("Could not open file %s.", string));
          }

          return validator.valid(file);
        });
    flags.registerCustomParser(
        KeyValue.class,
        (string, validator) -> {
          if (string.contains("=")) {
            String[] parts = string.split("=");
            return validator.valid(
                new KeyValue(parts.length > 0 ? parts[0] : "", parts.length > 1 ? parts[1] : ""));
          }

          return validator.invalid(
              String.format(
                  "Key-value type must contain '=' separator, found only: '%s'.", string));
        });

    flags.addCommand(command("dump", this::dumpState).setDescription("Dump internal state."));
    flags.addCommand(
        command(
                "create-user",
                this::createUser,
                ordinalParam(String.class, "name"),
                optional(namedParam(int.class, "flags")))
            .setDescription("Create a user with the optional flags and name."));
    flags.addCommand(
        command("set-user-icon", this::setUserIcon, ordinalParam(File.class, "file"))
            .setDescription(
                "Set the user icon using the bitmap located at the given file, which must be"
                    + " located in the user's `UserIcons` directory. For user 0, you can use `adb"
                    + " push` to push a local file to that directory"
                    + " (/storage/emulated/0/Android/data/com.afwsamples.testdpc/files/Pictures/UserIcons),"
                    + " but for other users you need to switch to that user and use its content"
                    + " provider (for example, `adb shell content write --user 10 --uri"
                    + " content://com.afwsamples.testdpc.usericoncontentprovider/icon.png <"
                    + " /tmp/icon.png`)."));
    flags.addCommand(
        command(
                "set-start-user-session-message",
                this::setStartUserSessionMessage,
                ordinalParam(String.class, "message"))
            .setDescription("Set the message shown when a user is switched to"));
    flags.addCommand(
        command("get-start-user-session-message", this::getStartUserSessionMessage)
            .setDescription("Get the message shown when a user is switched to"));
    flags.addCommand(
        command(
                "set-end-user-session-message",
                this::setEndUserSessionMessage,
                ordinalParam(String.class, "message"))
            .setDescription("Set the message shown when a user is switched of"));
    flags.addCommand(
        command("get-end-user-session-message", this::getEndUserSessionMessage)
            .setDescription("Get the message shown when a user is switched of"));
    flags.addCommand(
        command(
                "remove-user",
                this::removeUser,
                ordinalParam(UserHandle.class, "user-serial-number"))
            .setDescription("Remove the given user."));
    flags.addCommand(
        command(
                "switch-user",
                this::switchUser,
                ordinalParam(UserHandle.class, "user-serial-number"))
            .setDescription("Switch the given user to foreground."));
    flags.addCommand(
        command(
                "start-user-in-background",
                this::startUserInBackground,
                ordinalParam(UserHandle.class, "user-serial-number"))
            .setDescription("Switch the given user to foreground."));
    flags.addCommand(
            command(
                    "is-logout-enabled",
                    this::isLogoutEnabled)
                .setDescription("Whether logout is enabled."));
    flags.addCommand(
            command(
                    "set-logout-enabled",
                    this::setLogoutEnabled,
                    ordinalParam(boolean.class, "enabled"))
                .setDescription("Set whether logout is enabled."));
    flags.addCommand(
            command(
                    "logout-user",
                    this::logoutUser)
                .setDescription("Logout the current user."));
    flags.addCommand(
        command("is-user-affiliated", this::isUserAffiliated)
            .setDescription("Check if the user is affiliated with the device."));
    flags.addCommand(
        command(
                "set-affiliation-ids",
                this::setAffiliationIds,
                repeated(ordinalParam(String.class, "ids")))
            .setDescription("Set the user affiliation ids (or clear them if no ids are passed)."));
    flags.addCommand(
        command("get-affiliation-ids", this::getAffiliationIds)
            .setDescription("Get the user affiliation ids."));
    flags.addCommand(
        command("list-user-restrictions", this::listUserRestrictions)
            .setDescription("List the user restrictions."));
    flags.addCommand(
        command(
                "set-user-restriction",
                this::setUserRestriction,
                ordinalParam(String.class, "restriction"),
                ordinalParam(boolean.class, "enabled"))
            .setDescription("Set the given user restriction."));
    flags.addCommand(
        command("lock-now", this::lockNow, optional(namedParam(int.class, "flags")))
            .setDescription("Lock the device (now! :-)."));
    flags.addCommand(command("reboot", this::reboot).setDescription("Reboot the device."));
    flags.addCommand(
        command("wipe-data", this::wipeData, optional(namedParam(int.class, "flags")))
            .setDescription("Factory reset the device."));
    flags.addCommand(
                command("request-bugreport", this::requestBugreport)
                        .setDescription("Request a bug report."));
    flags.addCommand(
            command("get-last-bugreport-request-time", this::getLastBugReportRequestTime)
                    .setDescription("Prints the last time the device owner request a bugreport."));
    flags.addCommand(
        command(
                "set-network-logging-enabled",
                this::setNetworkLoggingEnabled,
                ordinalParam(boolean.class, "enabled"))
            .setDescription("Enable / disable network logging."));
    flags.addCommand(
            command("is-network-logging-enabled", this::isNetworkLoggingEnabled)
                .setDescription("Checks whether network logging is enabled."));
    flags.addCommand(
            command("get-last-network-log-retrieval-time", this::getLastNetworkLogRetrievalTime)
                    .setDescription("Prints the last time the device owner retrieved the network log."));
    flags.addCommand(
            command("retrieve-network-logs", this::retrieveNetworkLogs,
                    ordinalParam(Long.class, "batch-token"))
                    .setDescription("Retrieves the network logs."));
    flags.addCommand(
            command(
                    "set-security-logging-enabled",
                    this::setSecurityLoggingEnabled,
                    ordinalParam(boolean.class, "enabled"))
                .setDescription("Enable / disable security logging."));
    flags.addCommand(
            command("is-security-logging-enabled", this::isSecurityLoggingEnabled)
                .setDescription("Checks whether security logging is enabled."));
    flags.addCommand(
            command("get-last-security-log-retrieval-time", this::getLastSecurityLogRetrievalTime)
                    .setDescription("Prints the last time the device owner retrieved the security log."));
    flags.addCommand(
            command("retrieve-security-logs", this::retrieveSecurityLogs)
                    .setDescription("Retrieves the security logs."));
    flags.addCommand(
            command("retrieve-pre-reboot-security-logs", this::retrievePreRebootSecurityLogs)
                    .setDescription("Retrieves the pre-reboot security logs."));
    flags.addCommand(
        command("clear-organization-name", this::clearOrganizationName)
            .setDescription("Clear the organisation name."));
    flags.addCommand(
        command(
                "set-organization-name",
                this::setOrganizationName,
                ordinalParam(String.class, "name"))
            .setDescription("Set the organisation name."));
    flags.addCommand(
        command("get-organization-name", this::getOrganizationName)
            .setDescription("Get the organization name."));
    flags.addCommand(
        command(
                "set-user-control-disabled-packages",
                this::setUserControlDisabledPackages,
                repeated(ordinalParam(String.class, "packages")))
            .setDescription(
                "Set the packages that the user cannot force stop or clear data for. Provide an"
                    + " empty list to reset."));
    flags.addCommand(
        command("get-user-control-disabled-packages", this::getUserControlDisabledPackages)
            .setDescription("Get the packages that the user cannot force stop or clear data for."));
    flags.addCommand(
        command("remove-active-admin", this::removeActiveAdmin)
            .setDescription("Remove TestDPC as an active admin."));
    flags.addCommand(
        command("clear-device-owner", this::clearDeviceOwner)
            .setDescription("Clear TestDPC as device owner."));
    flags.addCommand(
        command("clear-profile-owner", this::clearProfileOwner)
            .setDescription("Clear TestDPC as profile owner."));
    flags.addCommand(
        command(
                "set-password-quality",
                this::setPasswordQuality,
                ordinalParam(int.class, "quality"))
            .setDescription("Set the password quality."));
    flags.addCommand(
        command("get-password-quality", this::getPasswordQuality)
            .setDescription("Get the password quality."));
    flags.addCommand(
        command("is-active-password-sufficient", this::isActivePasswordSufficient)
            .setDescription("Checks if user's password is sufficient."));
    flags.addCommand(
        command("is-active-password-sufficient-for-device-requirement", this::isActivePasswordSufficientForDeviceRequirement)
            .setDescription("Checks if user's password is sufficient for device requirement."));
    flags.addCommand(
        command(
                "set-required-password-complexity",
                this::setRequiredPasswordComplexity,
                ordinalParam(int.class, "complexity"))
            .setDescription("Set the required password complexity."));
    flags.addCommand(
        command("get-required-password-complexity", this::getRequiredPasswordComplexity)
            .setDescription("Get required the password complexity."));
    flags.addCommand(
        command("transfer-ownership", this::transferOwnership, ordinalParam(String.class, "admin"))
            .setDescription("Transfer ownership to the given admin."));
    flags.addCommand(
        command(
                "set-suspended-packages",
                this::setSuspendedPackages,
                ordinalParam(boolean.class, "suspended"),
                repeated(ordinalParam(String.class, "packageNames")))
            .setDescription("Suspend / unsuspend the given packages."));
    flags.addCommand(
        command(
                "is-suspended-packages",
                this::isSuspendedPackage,
                repeated(ordinalParam(String.class, "packageNames")))
            .setDescription("Check if the given packages are suspended."));
    flags.addCommand(
        command(
                "set-personal-apps-suspended",
                this::setPersonalAppsSuspended,
                ordinalParam(boolean.class, "suspended"))
            .setDescription("Suspend / unsuspend personal apps."));
    flags.addCommand(
        command(
                "enable-system-app",
                this::enableSystemApp,
                ordinalParam(String.class, "packageName"))
            .setDescription("Enable the given system app."));
    flags.addCommand(
        command("list-disabled-system-apps", this::listDisabledSystemApps)
            .setDescription("List the disabled system apps."));
    flags.addCommand(
        command("get-personal-apps-suspended-reasons", this::getPersonalAppsSuspendedReasons)
            .setDescription("Get the reasons for suspending personal apps."));
    flags.addCommand(
        command(
                "set-hidden-package",
                this::setHiddenPackage,
                ordinalParam(String.class, "package"),
                ordinalParam(boolean.class, "hidden"))
            .setDescription("Hide / unhide the given package."));
    flags.addCommand(
        command("is-hidden-package", this::isHiddenPackage, ordinalParam(String.class, "package"))
            .setDescription("Check if the given package is hidden."));
    flags.addCommand(
        command(
                "set-lock-task-packages",
                this::setLockTaskPackages,
                repeated(ordinalParam(String.class, "packages")))
            .setDescription("Set the packages allowed to have tasks locked."));
    flags.addCommand(
        command("get-lock-task-packages", this::getLockTaskPackages)
            .setDescription("Get the packages allowed to have tasks locked."));
    flags.addCommand(
        command(
                "is-lock-task-permitted",
                this::isLockTaskPermitted,
                repeated(ordinalParam(String.class, "packages")))
            .setDescription("Check if the given packages are allowed to have tasks locked."));
    flags.addCommand(
        command(
                "set-lock-task-features",
                this::setLockTaskFeatures,
                ordinalParam(int.class, "flags"))
            .setDescription("Set the lock task features."));
    flags.addCommand(
        command("get-lock-task-features", this::getLockTaskFeatures)
            .setDescription("Get the lock task features."));
    flags.addCommand(
        command(
                "set-app-restrictions",
                this::setAppRestrictions,
                ordinalParam(String.class, "package"),
                repeated(ordinalParam(KeyValue.class, "restrictions")))
            .setDescription(
                "Set the application restrictions (provided as key=value strings) for the given app"
                    + " (or clear if no values provided)."));
    flags.addCommand(
        command(
                "get-app-restrictions",
                this::getAppRestrictions,
                repeated(ordinalParam(String.class, "packages")))
            .setDescription(
                "Get the application restrictions for the given apps (or for TestDPC when empty,"
                    + " using UserManager)."));
    flags.addCommand(
        command(
                "set-permission-grant-state",
                this::setPermissionGrantState,
                ordinalParam(String.class, "package"),
                ordinalParam(String.class, "permission"),
                ordinalParam(int.class, "state"))
            .setDescription("Set the grant state for the given package and permission."));
    flags.addCommand(
        command(
                "get-permission-grant-state",
                this::getPermissionGrantState,
                ordinalParam(String.class, "package"),
                ordinalParam(String.class, "permission"))
            .setDescription("Get the grant state for the given package and persmission."));
    flags.addCommand(
        command("can-admin-grant-sensors-permissions", this::canAdminGrantSensorsPermissions)
            .setDescription("Checks whether the admin can grant sensor permissions."));
    flags.addCommand(
        command(
                "set-location-enabled",
                this::setLocationEnabled,
                ordinalParam(boolean.class, "enabled"))
            .setDescription("Set location enabled for the user."));
    flags.addCommand(
        command("is-location-enabled", this::isLocationEnabled)
            .setDescription("Get whether location is enabled for the user."));
    flags.addCommand(
        command("clear-device-owner-lockscreen-info", this::clearDeviceOwnerLockScreenInfo)
            .setDescription("Clear the device owner lock screen info."));
    flags.addCommand(
        command(
                "set-device-owner-lockscreen-info",
                this::setDeviceOwnerLockScreenInfo,
                ordinalParam(String.class, "info"))
            .setDescription("Set the device owner lock screen info."));
    flags.addCommand(
        command("get-device-owner-lockscreen-info", this::getDeviceOwnerLockScreenInfo)
            .setDescription("Get the device owner lock screen info."));
    flags.addCommand(
        command(
                "set-keyguard-disabled",
                this::setKeyguardDisabled,
                ordinalParam(boolean.class, "disabled"))
            .setDescription("Set keyguard disabled."));
    flags.addCommand(
        command(
                "set-keyguard-disabled-features",
                this::setKeyguardDisabledFeatures,
                ordinalParam(int.class, "flags"))
            .setDescription("Set the keyguard disabled features."));
    flags.addCommand(
        command("get-keyguard-disabled-features", this::getKeyguardDisabledFeatures)
            .setDescription("Get the keyguard disabled features."));
    flags.addCommand(
        command(
                "set-camera-disabled",
                this::setCameraDisabled,
                ordinalParam(boolean.class, "disabled"))
            .setDescription("Set camera disabled."));
    flags.addCommand(
        command("get-camera-disabled", this::getCameraDisabled)
            .setDescription("Get camera disabled."));
    flags.addCommand(
            command(
                    "set-status-bar-disabled",
                    this::setStatusBarDisabled,
                    ordinalParam(boolean.class, "disabled"))
                .setDescription("Set status bar disabled."));
    flags.addCommand(
        command(
                "set-max-failed-passwords",
                this::setMaximumFailedPasswordsForWipe,
                ordinalParam(int.class, "max"))
            .setDescription("Set maximum number of failed passwords before user is wiped."));
    flags.addCommand(
        command("get-max-failed-passwords", this::getMaximumFailedPasswordsForWipe)
            .setDescription("Get maximum number of failed passwords before user is wiped."));
    flags.addCommand(
        command("install-existing-package", this::installExistingPackage,
                ordinalParam(String.class, "package"))
            .setDescription("Installs the existing package for this user."));
    flags.addCommand(
        command("set-uninstall-blocked", this::setUninstallBlocked,
                ordinalParam(String.class, "package"),
                ordinalParam(boolean.class, "blocked"))
            .setDescription("Sets whether the given package can be uninstalled."));
    flags.addCommand(
        command("is-uninstall-blocked", this::isUninstallBlocked,
                ordinalParam(String.class, "package"))
            .setDescription("Checks whether the given package can be uninstalled."));
    flags.addCommand(
        command("set-secure-setting", this::setSecureSetting,
                ordinalParam(String.class, "setting"),
                ordinalParam(String.class, "value"))
            .setDescription("Sets the given Settings.SECURE setting for this user."));
    flags.addCommand(
        command("set-global-setting", this::setGlobalSetting,
                ordinalParam(String.class, "setting"),
                ordinalParam(String.class, "value"))
            .setDescription("Sets the given Settings.GLOBAL setting for this user."));
    flags.addCommand(
        command("has-key-pair", this::hasKeyPair,
                ordinalParam(String.class, "alias"))
            .setDescription("Checks if a certificate key with the given alias is installed."));
    flags.addCommand(
        command("generate-device-attestation-key-pair", this::generateDeviceAttestationKeyPair,
                ordinalParam(String.class, "alias"),
                optional(namedParam(int.class, "flags")))
            .setDescription("Generates a device attestation key."));
    flags.addCommand(
        command("remove-key-pair", this::removeKeyPair,
                ordinalParam(String.class, "alias"))
            .setDescription("Removes the certificate key with the given alias."));
    flags.addCommand(
        command("grant-key-pair-to-app", this::grantKeyPairToApp,
                ordinalParam(String.class, "alias"),
                ordinalParam(String.class, "packageName"))
            .setDescription("Grants a certificate key to an app."));
    flags.addCommand(
        command("get-key-pair-grants", this::getKeyPairGrants,
                ordinalParam(String.class, "alias"))
            .setDescription("Lists the apps that were granted the given certificate key."));
    flags.addCommand(
        command("revoke-key-pair-from-app", this::revokeKeyPairFromApp,
                ordinalParam(String.class, "alias"),
                ordinalParam(String.class, "packageName"))
            .setDescription("Revokes a certificate key from an app."));
    flags.addCommand(
        command("set-delegated-scopes", this::setDelegatedScopes,
                ordinalParam(String.class, "packageName"),
                repeated(ordinalParam(String.class, "scopes")))
            .setDescription("Delegates the given scopes to an app."));
    flags.addCommand(
        command("get-delegated-scopes", this::getDelegatedScopes,
                ordinalParam(String.class, "packageName"))
            .setDescription("Gets the scopes delgated to an app."));
    flags.addCommand(
        command("get-delegate-packages", this::getDelegatePackages,
                ordinalParam(String.class, "scope"))
            .setDescription("Gets the apps that were delegate a given scope."));

    // Separator for S / pre-S commands - do NOT remove line to avoid cherry-pick conflicts

    if (Util.isAtLeastS()) {
      flags.addCommand(
          command(
                  "set-usb-data-signaling-enabled",
                  this::setUsbDataSignalingEnabled,
                  ordinalParam(boolean.class, "enabled"))
              .setDescription("Enable / disable USB data signaling."));
      flags.addCommand(
          command(
                  "set-permitted-input-methods-parent",
                  this::setPermittedInputMethodsOnParent,
                  repeated(ordinalParam(String.class, "methods")))
              .setDescription("Set the permitted input methods in the parent's device admin."));
      flags.addCommand(
          command("list-foreground-users", this::listForegroundUsers)
              .setDescription("List the users running in the foreground."));
      flags.addCommand(
          command("is-foreground-user", this::isForegroundUser)
              .setDescription("Checks if the calling user is running in the foreground."));
      flags.addCommand(
          command(
                  "set-metered-data-disabled-packages",
                  this::setMeteredDataDisabledPackages,
                  repeated(ordinalParam(String.class, "disabled-packages")))
              .setDescription("Restricts packages from using metered data."));
      flags.addCommand(
          command("get-metered-data-disabled-packages", this::getMeteredDataDisabledPackages)
              .setDescription("List the packages restricted from using metered data."));
    }

    try {
      flags.run(mArgs);
    } catch (Exception e) {
      // Must explicitly catch and show generic exceptions (like NumberFormatException parsing
      // args), otherwise they'dbe logcat'ed on AndroidRuntime and not surfaced to caller
      onError(e, "error executing %s", Arrays.toString(mArgs));
    }
  }

  private void dumpState() {
    mWriter.printf("isDeviceOwner: %b\n", mDevicePolicyManagerGateway.isDeviceOwnerApp());
    mWriter.printf("isProfileOwner: %b\n", mDevicePolicyManagerGateway.isProfileOwnerApp());
    mWriter.printf(
        "isOrganizationOwnedDeviceWithManagedProfile: %b\n",
        mDevicePolicyManagerGateway.isOrganizationOwnedDeviceWithManagedProfile());
    if (Util.isAtLeastS()) {
      mWriter.printf(
          "isHeadlessSystemUserMode: %b\n", mDevicePolicyManagerGateway.isHeadlessSystemUserMode());
      mWriter.printf("isUserForeground: %b\n", mDevicePolicyManagerGateway.isUserForeground());
    }
    mWriter.printf("isDeviceIdAttestationSupported: %b\n",
        mDevicePolicyManagerGateway.isDeviceIdAttestationSupported());
    mWriter.printf("isUniqueDeviceAttestationSupported: %b\n",
        mDevicePolicyManagerGateway.isUniqueDeviceAttestationSupported());
  }

  private void createUser(String name, int flags) {
    Log.i(TAG, "createUser(): name=" + name + ", flags=" + flags);
    mDevicePolicyManagerGateway.createAndManageUser(
        name,
        flags,
        (u) -> onSuccess("User created: %s", toString(u)),
        (e) -> onError(e, "Error creating user %s", name));
  }

  private void setUserIcon(File file) {
    String absolutePath = file.getAbsolutePath();
    Log.i(TAG, "setUserIcon(): path=" + absolutePath);
    Bitmap icon = BitmapFactory.decodeFile(absolutePath, /* bmOptions= */ null);
    if (icon == null) {
      mWriter.printf("Could not create bitmap from file %s\n", absolutePath);
      return;
    }
    mDevicePolicyManagerGateway.setUserIcon(
        icon,
        (v) -> onSuccess("User icon created from file %s", absolutePath),
        (e) -> onError(e, "Error creating user icon from file %s", absolutePath));
  }

  private void setStartUserSessionMessage(String message) {
    Log.i(TAG, "setStartUserSessionMessage(): " + message);
    mDevicePolicyManagerGateway.setStartUserSessionMessage(
        message,
        (v) -> onSuccess("Set start user session message to '%s'", message),
        (e) -> onError(e, "Error setting start user session message to '%s'", message));
  }

  private void getStartUserSessionMessage() {
    CharSequence message = mDevicePolicyManagerGateway.getStartUserSessionMessage();
    mWriter.printf("%s\n", message);
  }

  private void setEndUserSessionMessage(String message) {
    Log.i(TAG, "setEndUserSessionMessage(): " + message);
    mDevicePolicyManagerGateway.setEndUserSessionMessage(
        message,
        (v) -> onSuccess("Set end user session message to '%s'", message),
        (e) -> onError(e, "Error setting end user session message to '%s'", message));
  }

  private void getEndUserSessionMessage() {
    CharSequence message = mDevicePolicyManagerGateway.getEndUserSessionMessage();
    mWriter.printf("%s\n", message);
  }

  private void removeUser(UserHandle userHandle) {
    mDevicePolicyManagerGateway.removeUser(
        userHandle,
        (v) -> onSuccess("User %s removed", userHandle),
        (e) -> onError(e, "Error removing user %s", userHandle));
  }

  private void switchUser(UserHandle userHandle) {
    mDevicePolicyManagerGateway.switchUser(
        userHandle,
        (v) -> onSuccess("User %s switched", userHandle),
        (e) -> onError(e, "Error switching user %s", userHandle));
  }

  private void startUserInBackground(UserHandle userHandle) {
    mDevicePolicyManagerGateway.startUserInBackground(
        userHandle,
        (v) -> onSuccess("User %s started in background", userHandle),
        (e) -> onError(e, "Error starting user %s in background", userHandle));
  }

  private void stopUser(UserHandle userHandle) {
    mDevicePolicyManagerGateway.stopUser(
        userHandle,
        (v) -> onSuccess("User %s stopped", userHandle),
        (e) -> onError(e, "Error stopping user %s", userHandle));
  }

  private void isLogoutEnabled() {
      mWriter.println(mDevicePolicyManagerGateway.isLogoutEnabled());
  }

  private void setLogoutEnabled(boolean enabled) {
      mDevicePolicyManagerGateway.setLogoutEnabled(enabled,
          (v) -> onSuccess("Logout enabled set to %b", enabled),
          (e) -> onError(e, "Error setting logout enabled to %b", enabled));
  }

  private void logoutUser() {
      UserHandle userHandle = Process.myUserHandle();
      mDevicePolicyManagerGateway.logoutUser(
          (v) -> onSuccess("User %s logged out", userHandle),
          (e) -> onError(e, "Error logging out user %s", userHandle));
  }

  private void getAffiliationIds() {
    Set<String> ids = mDevicePolicyManagerGateway.getAffiliationIds();
    if (ids.isEmpty()) {
      mWriter.println("no affiliation ids");
      return;
    }
    mWriter.printf("%d affiliation ids: %s\n", ids.size(), ids);
  }

  private void setAffiliationIds(String[] ids) {
    Set<String> idSet = getOrderedSortedSet(ids);
    Log.i(TAG, "setAffiliationIds(): ids=" + idSet);
    mDevicePolicyManagerGateway.setAffiliationIds(idSet);

    getAffiliationIds();
  }

  private void isUserAffiliated() {
    mWriter.println(mDevicePolicyManagerGateway.isAffiliatedUser());
  }

  private void listUserRestrictions() {
    Log.i(TAG, "listUserRestrictions()");

    printCollection("user restriction", mDevicePolicyManagerGateway.getUserRestrictions());
  }

  private void setUserRestriction(String userRestriction, boolean enabled) {
    Log.i(TAG, "setUserRestriction(" + userRestriction + ", " + enabled + ")");
    mDevicePolicyManagerGateway.setUserRestriction(
        userRestriction,
        enabled,
        (v) -> onSuccess("User restriction '%s' set to %b", userRestriction, enabled),
        (e) -> onError(e, "Error setting user restriction '%s' to %b", userRestriction, enabled));
  }

  private void lockNow(Integer flags) {
    if (flags == null) {
      Log.i(TAG, "lockNow()");
      mDevicePolicyManagerGateway.lockNow(
          (v) -> onSuccess("Device locked"), (e) -> onError(e, "Error locking device"));
    } else {
      Log.i(TAG, "lockNow(" + flags + ")");
      mDevicePolicyManagerGateway.lockNow(
          flags, (v) -> onSuccess("Device locked"), (e) -> onError(e, "Error locking device"));
    }
  }

  private void reboot() {
    Log.i(TAG, "reboot()");
    mDevicePolicyManagerGateway.reboot(
        (v) -> onSuccess("Device rebooted"), (e) -> onError(e, "Error rebooting device"));
  }

  private void wipeData(int flags) {
    Log.i(TAG, "wipeData()");
    mDevicePolicyManagerGateway.wipeData(
        flags, (v) -> onSuccess("Data wiped"), (e) -> onError(e, "Error wiping data"));
  }

  private void requestBugreport() {
    Log.i(TAG, "requestBugreport()");
    mDevicePolicyManagerGateway.requestBugreport(
        (v) -> onSuccess("Bugreport requested"), (e) -> onError(e, "Error requesting bugreport"));
  }

  private void getLastBugReportRequestTime() {
      printTime(mDevicePolicyManagerGateway.getLastBugReportRequestTime());
  }

  private void setNetworkLoggingEnabled(boolean enabled) {
    Log.i(TAG, "setNetworkLoggingEnabled(" + enabled + ")");
    mDevicePolicyManagerGateway.setNetworkLoggingEnabled(
        enabled,
        (v) -> onSuccess("Network logging enabled set to %b", enabled),
        (e) -> onError(e, "Error setting network logging enabled to %b", enabled));
  }

  private void isNetworkLoggingEnabled() {
    mWriter.println(mDevicePolicyManagerGateway.isNetworkLoggingEnabled());
  }

  private void getLastNetworkLogRetrievalTime() {
    printTime(mDevicePolicyManagerGateway.getLastNetworkLogRetrievalTime());
  }

  private void retrieveNetworkLogs(long batchToken) {
    List<NetworkEvent> events = mDevicePolicyManagerGateway.retrieveNetworkLogs(batchToken);
    if (events == null || events.isEmpty()) {
      mWriter.println("N/A");
      return;
    }
    mWriter.printf("%d events:\n", events.size());
    for (int i = 0; i < events.size(); i++) {
        NetworkEvent event = events.get(i);
        if (event instanceof DnsEvent) {
          DnsEvent de = (DnsEvent) event;
          String addresses = de.getInetAddresses().stream()
              .map(InetAddress::toString).collect(Collectors.joining(","));
          mWriter.printf("\t%d:DnsEvent id=%d pkg=%s hostname=%s addresses=%s\n",
              i, event.getId(), event.getPackageName(), de.getHostname(), addresses);
        } else if (event instanceof ConnectEvent) {
          ConnectEvent ce = (ConnectEvent) event;
          mWriter.printf("\t%d:ConnectEvent id=%d pkg=%s address=%s port=%d\n",
              i, ce.getId(), ce.getPackageName(), ce.getInetAddress(), ce.getPort());
        } else {
          mWriter.printf("\t%d:Unknown id=%d pkg=%s\n", i, event.getId(), event.getPackageName());
        }
    }
  }

  private void setSecurityLoggingEnabled(boolean enabled) {
      Log.i(TAG, "setSecurityLoggingEnabled(" + enabled + ")");
      mDevicePolicyManagerGateway.setSecurityLoggingEnabled(
          enabled,
          (v) -> onSuccess("Security logging enabled set to %b", enabled),
          (e) -> onError(e, "Error setting security logging enabled to %b", enabled));
  }

  private void isSecurityLoggingEnabled() {
      mWriter.println(mDevicePolicyManagerGateway.isSecurityLoggingEnabled());
  }

  private void getLastSecurityLogRetrievalTime() {
      printTime(mDevicePolicyManagerGateway.getLastSecurityLogRetrievalTime());
  }

  private void retrieveSecurityLogs() {
    printSecurityLogs(mDevicePolicyManagerGateway.retrieveSecurityLogs());
  }

  private void retrievePreRebootSecurityLogs() {
    printSecurityLogs(mDevicePolicyManagerGateway.retrievePreRebootSecurityLogs());
  }

  private void clearOrganizationName() {
    setOrganizationName("");
  }

  private void setOrganizationName(String name) {
    Log.i(TAG, "setOrganizationName(" + name + ")");
    mDevicePolicyManagerGateway.setOrganizationName(
        name,
        (v) -> onSuccess("Organization name set to %s", name),
        (e) -> onError(e, "Error setting Organization name to %s", name));
  }

  private void getOrganizationName() {
    CharSequence title = mDevicePolicyManagerGateway.getOrganizationName();
    if (title == null) {
      mWriter.println("Not set");
      return;
    }
    mWriter.println(title);
  }

  private void setUserControlDisabledPackages(String[] packages) {
    List<String> packagesList = Arrays.asList(packages);
    Log.i(TAG, "setUserControlDisabledPackages(" + packagesList + ")");

    mDevicePolicyManagerGateway.setUserControlDisabledPackages(
        packagesList,
        (v) -> onSuccess("User-control disabled packages set to %s", packagesList),
        (e) -> onError(e, "Error setting User-control disabled packages to %s", packagesList));
  }

  private void getUserControlDisabledPackages() {
    List<String> pkgs = mDevicePolicyManagerGateway.getUserControlDisabledPackages();
    pkgs.forEach(mWriter::println);
  }

  private void removeActiveAdmin() {
    Log.i(TAG, "removeActiveAdmin()");

    ComponentName admin = mDevicePolicyManagerGateway.getAdmin();
    mDevicePolicyManagerGateway.removeActiveAdmin(
        (v) -> onSuccess("Removed %s as an active admin", admin),
        (e) -> onError(e, "Error removing %s as admin", admin));
  }

  private void clearDeviceOwner() {
    Log.i(TAG, "clearDeviceOwner()");

    String pkg = mDevicePolicyManagerGateway.getAdmin().getPackageName();
    mDevicePolicyManagerGateway.clearDeviceOwnerApp(
        (v) -> onSuccess("Removed %s as device owner", pkg),
        (e) -> onError(e, "Error removing %s as device owner", pkg));
  }

  private void clearProfileOwner() {
    Log.i(TAG, "clearProfileOwner()");

    String pkg = mDevicePolicyManagerGateway.getAdmin().getPackageName();
    mDevicePolicyManagerGateway.clearProfileOwner(
        (v) -> onSuccess("Removed %s as profile owner", pkg),
        (e) -> onError(e, "Error removing %s as profile owner", pkg));
  }

  private void setPermittedInputMethodsOnParent(String[] inputMethod) {
    List<String> inputMethodsList = Arrays.asList(inputMethod);
    Log.i(TAG, "setPermittedInputMethodsOnParent(" + inputMethodsList + ")");

    DevicePolicyManagerGateway parentDpmGateway =
        DevicePolicyManagerGatewayImpl.forParentProfile(mContext);
    parentDpmGateway.setPermittedInputMethods(inputMethodsList);
  }

  private void listForegroundUsers() {
    List<UserHandle> users = mDevicePolicyManagerGateway.listForegroundAffiliatedUsers();
    if (users.isEmpty()) {
      mWriter.println("none");
      return;
    }
    int size = users.size();
    mWriter.printf("%d user%s:\n", size, (size > 1 ? "s" : ""));
    users.forEach(u -> mWriter.printf("\t%s\n", u));
  }

  private void isForegroundUser() {
    mWriter.println(mDevicePolicyManagerGateway.isUserForeground());
  }

  private void setPasswordQuality(int quality) {
    String qualityString = Util.passwordQualityToString(quality);
    Log.i(TAG, "setPasswordQuality(" + quality + "/" + qualityString + ")");
    mDevicePolicyManagerGateway.setPasswordQuality(
        quality,
        (v) -> onSuccess("Set password quality to %s (%d)", qualityString, quality),
        (e) -> onError(e, "Error setting password quality to %s (%d)", qualityString, quality));
  }

  private void getPasswordQuality() {
    int quality = mDevicePolicyManagerGateway.getPasswordQuality();

    mWriter.printf("%s (%d)\n", Util.passwordQualityToString(quality), quality);
  }

  private void isActivePasswordSufficient() {
    boolean isIt = mDevicePolicyManagerGateway.isActivePasswordSufficient();

    mWriter.printf("%b\n", isIt);
  }

  private void isActivePasswordSufficientForDeviceRequirement() {
    boolean isIt = mDevicePolicyManagerGateway.isActivePasswordSufficientForDeviceRequirement();

    mWriter.printf("%b\n", isIt);
  }

  private void setRequiredPasswordComplexity(int complexity) {
    String complexityString = Util.requiredPasswordComplexityToString(complexity);
    Log.i(TAG, "setRequiredPasswordComplexity(" + complexity + "/" + complexityString + ")");
    mDevicePolicyManagerGateway.setRequiredPasswordComplexity(
        complexity,
        (v) ->
            onSuccess("Set required password complexity to %s (%d)", complexityString, complexity),
        (e) ->
            onError(
                e,
                "Error setting required password complexity to %s (%d)",
                complexityString,
                complexity));
  }

  private void getRequiredPasswordComplexity() {
    int complexity = mDevicePolicyManagerGateway.getRequiredPasswordComplexity();

    mWriter.printf("%s (%d)\n", Util.requiredPasswordComplexityToString(complexity), complexity);
  }

  private void transferOwnership(String flatTarget) {
    ComponentName target = ComponentName.unflattenFromString(flatTarget);

    Log.i(TAG, "transferOwnership(" + target + ")");

    mDevicePolicyManagerGateway.transferOwnership(
        target,
        /* bundle= */ null,
        (v) -> onSuccess("Ownership transferred to %s", flatTarget),
        (e) -> onError(e, "Error transferring ownership to %s", flatTarget));
  }

  private void setUsbDataSignalingEnabled(boolean enabled) {
    Log.i(TAG, "setUsbDataSignalingEnabled(" + enabled + ")");
    mDevicePolicyManagerGateway.setUsbDataSignalingEnabled(
        enabled,
        (v) -> onSuccess("USB data signaling set to %b", enabled),
        (e) -> onError(e, "Error setting USB data signaling to %b", enabled));
  }

  private void setSuspendedPackages(boolean suspended, String[] packageNames) {
    String printableNames = Arrays.toString(packageNames);
    String printableStatus = suspendedToString(suspended);

    Log.i(TAG, "setSuspendedPackages(" + printableNames + "): " + printableStatus);

    mDevicePolicyManagerGateway.setPackagesSuspended(
        packageNames,
        suspended,
        (v) ->
            onSuccess(
                "Set %s (but not %s) to %s", printableNames, Arrays.toString(v), printableStatus),
        (e) -> onError(e, "Error setting %s to %s", printableNames, printableStatus));
  }

  private void isSuspendedPackage(String[] packageNames) {
    for (String packageName : packageNames) {
      try {
        boolean suspended = mDevicePolicyManagerGateway.isPackageSuspended(packageName);
        mWriter.printf("%s: %s\n", packageName, suspendedToString(suspended));
      } catch (NameNotFoundException e) {
        mWriter.printf("Invalid package name: %s\n", packageName);
      }
    }
  }

  private void setHiddenPackage(String packageName, boolean hidden) {
    String printableStatus = hiddenToString(hidden);

    Log.i(TAG, "setHiddenPackages(" + packageName + "): " + printableStatus);
    mDevicePolicyManagerGateway.setApplicationHidden(
        packageName,
        hidden,
        (v) -> onSuccess("Set %s as %s", packageName, printableStatus),
        (e) -> onError(e, "Error settings %s as %s", packageName, printableStatus));
  }

  private void isHiddenPackage(String packageName) {
    try {
      boolean hidden = mDevicePolicyManagerGateway.isApplicationHidden(packageName);
      mWriter.printf("%s: %s\n", packageName, hiddenToString(hidden));
    } catch (NameNotFoundException e) {
      mWriter.printf("Invalid package name: %s\n", packageName);
    }
  }

  private void setPersonalAppsSuspended(boolean suspended) {
    String printableStatus = suspendedToString(suspended);

    Log.i(TAG, "setPersonalAppsSuspended(): " + printableStatus);

    mDevicePolicyManagerGateway.setPersonalAppsSuspended(
        suspended,
        (v) -> onSuccess("Set personal apps to %s", printableStatus),
        (e) -> onError(e, "Error setting personal apps to %s", printableStatus));
  }

  private void getPersonalAppsSuspendedReasons() {
    int reasons = mDevicePolicyManagerGateway.getPersonalAppsSuspendedReasons();
    String printableReasons = Util.personalAppsSuspensionReasonToString(reasons);

    mWriter.printf("%s (%d)\n", printableReasons, reasons);
  }

  private void enableSystemApp(String packageName) {
    Log.i(TAG, "enableSystemApp(): " + packageName);
    mDevicePolicyManagerGateway.enableSystemApp(
        packageName,
        (v) -> onSuccess("Enabled system apps %s", packageName),
        (e) -> onError(e, "Error enabling systen app%s", packageName));
  }

  private void listDisabledSystemApps() {
    List<String> disabledSystemApps = mDevicePolicyManagerGateway.getDisabledSystemApps();

    Log.i(TAG, "listDisabledSystemApps(): " + disabledSystemApps);

    printCollection("disabled system app", disabledSystemApps);
  }

  private void setLockTaskPackages(String[] packages) {
    String printableNames = Arrays.toString(packages);

    Log.i(TAG, "setLockTaskPackages(): " + printableNames);

    mDevicePolicyManagerGateway.setLockTaskPackages(
        packages,
        (v) -> onSuccess("Set lock tasks packages to %s", printableNames),
        (e) -> onError(e, "Error settings lock task packages to %s", printableNames));
  }

  private void getLockTaskPackages() {
    String[] packages = mDevicePolicyManagerGateway.getLockTaskPackages();
    if (packages.length == 0) {
      mWriter.println("no lock task packages");
      return;
    }
    mWriter.println(Arrays.toString(packages));
  }

  private void setLockTaskFeatures(int flags) {
    String features = Util.lockTaskFeaturesToString(flags);
    Log.i(TAG, "setLockTaskFeatures(" + flags + "): setting to " + features);

    mDevicePolicyManagerGateway.setLockTaskFeatures(
        flags,
        (v) -> onSuccess("Set lock tasks features to %s", features),
        (e) -> onError(e, "Error settings lock task features to %s", features));
  }

  private void getLockTaskFeatures() {
    int flags = mDevicePolicyManagerGateway.getLockTaskFeatures();
    String features = Util.lockTaskFeaturesToString(flags);

    mWriter.printf("%s (%d)\n", features, flags);
  }

  private void setAppRestrictions(String packageName, KeyValue[] restrictions) {
    Bundle settings = new Bundle();
    for (KeyValue restriction : restrictions) {
      settings.putString(restriction.key, restriction.value);
    }
    mDevicePolicyManagerGateway.setApplicationRestrictions(
        packageName,
        settings,
        (v) -> onSuccess("Set %d app restrictions for %s", settings.size(), packageName),
        (e) -> onError(e, "Error setting app restrictions for %s", packageName));
  }

  private void getAppRestrictions(String[] packageNames) {
    if (packageNames.length == 1) {
      printAppRestrictions(
          mContext.getPackageName(),
          "UserManager",
          mDevicePolicyManagerGateway.getSelfRestrictions());
      return;
    }

    for (String packageName : packageNames) {
      Bundle settings = mDevicePolicyManagerGateway.getApplicationRestrictions(packageName);
      printAppRestrictions(packageName, "DevicePolicyManager", settings);
    }
  }

  private void printAppRestrictions(String packageName, String source, Bundle settings) {
    if (settings == null || settings.isEmpty()) {
      mWriter.printf("No app restrictions (from %s) for %s\n", source, packageName);
      return;
    }
    int size = settings.size();
    String pluralSuffix = size > 1 ? "s" : "";
    mWriter.printf("%d app restriction%s%s for %s\n", size, pluralSuffix, source, packageName);
    for (String key : settings.keySet()) {
      Object value = settings.get(key);
      mWriter.printf("  %s = %s\n", key, value);
    }
  }

  private void setPermissionGrantState(String packageName, String permission, int grantState) {
    String grantName = Util.grantStateToString(grantState);
    mDevicePolicyManagerGateway.setPermissionGrantState(
        packageName,
        permission,
        grantState,
        (v) -> onSuccess("Set %s state on %s to %s", permission, packageName, grantName),
        (e) ->
            onError(e, "Error setting %s state on %s to %s", packageName, permission, grantName));
  }

  private void getPermissionGrantState(String packageName, String permission) {
    int grantState = mDevicePolicyManagerGateway.getPermissionGrantState(packageName, permission);
    mWriter.printf(
        "%s state for %s: %s\n", permission, packageName, Util.grantStateToString(grantState));
  }

  private void canAdminGrantSensorsPermissions() {
    mWriter.println(mDevicePolicyManagerGateway.canAdminGrantSensorsPermissions());
  }

  private void setLocationEnabled(boolean enabled) {
    mDevicePolicyManagerGateway.setLocationEnabled(
        enabled,
        (v) -> onSuccess("Set location enabled to %b", enabled),
        (e) -> onError(e, "Error setting location enabled to %b", enabled));
  }

  private void isLocationEnabled() {
    boolean enabled = mDevicePolicyManagerGateway.isLocationEnabled();
    mWriter.printf("Location enabled: %b\n", enabled);
  }

  private void clearDeviceOwnerLockScreenInfo() {
    setDeviceOwnerLockScreenInfo("");
  }

  private void setDeviceOwnerLockScreenInfo(String info) {
    mDevicePolicyManagerGateway.setDeviceOwnerLockScreenInfo(
        info,
        (v) -> onSuccess("Set lock screen info to '%s'", info),
        (e) -> onError(e, "Error setting lock screen info to '%s'", info));
  }

  private void getDeviceOwnerLockScreenInfo() {
    CharSequence info = mDevicePolicyManagerGateway.getDeviceOwnerLockScreenInfo();
    mWriter.printf("Lock screen info: %s\n", info);
  }

  private void setKeyguardDisabled(boolean disabled) {
    mDevicePolicyManagerGateway.setKeyguardDisabled(
        disabled,
        (v) -> onSuccess("Set keyguard disabled to %b", disabled),
        (e) -> onError(e, "Error setting keyguard disabled to %b", disabled));
  }

  private void setKeyguardDisabledFeatures(int flags) {
    String features = Util.keyguardDisabledFeaturesToString(flags);
    Log.i(TAG, "setKeyguardDisabledFeatures(" + flags + "): setting to " + features);

    mDevicePolicyManagerGateway.setKeyguardDisabledFeatures(
        flags,
        (v) -> onSuccess("Set keyguard features to %s", features),
        (e) -> onError(e, "Error settings keyguard features to %s", features));
  }

  private void getKeyguardDisabledFeatures() {
    int flags = mDevicePolicyManagerGateway.getKeyguardDisabledFeatures();
    String features = Util.keyguardDisabledFeaturesToString(flags);

    mWriter.printf("%s (%d)\n", features, flags);
  }

  private void setCameraDisabled(boolean disabled) {
    mDevicePolicyManagerGateway.setCameraDisabled(
        disabled,
        (v) -> onSuccess("Set camera disabled to %b", disabled),
        (e) -> onError(e, "Error setting camera disabled to %b", disabled));
  }

  private void getCameraDisabled() {
    mWriter.printf(
        "By %s: %b\n",
        mDevicePolicyManagerGateway.getAdmin().flattenToShortString(),
        mDevicePolicyManagerGateway.getCameraDisabled());
    mWriter.printf("By any admin: %b\n", mDevicePolicyManagerGateway.getCameraDisabledByAnyAdmin());
  }

  private void setMaximumFailedPasswordsForWipe(int max) {
    mDevicePolicyManagerGateway.setMaximumFailedPasswordsForWipe(max,
        (v) -> onSuccess("Set maximum failed password for wipe to %d", max),
        (e) -> onError(e, "Error setting maximum failed password for wipe to %d", max));
  }

  private void getMaximumFailedPasswordsForWipe() {
    mWriter.println(mDevicePolicyManagerGateway.getMaximumFailedPasswordsForWipe());
  }

  private void setStatusBarDisabled(boolean disabled) {
    mDevicePolicyManagerGateway.setStatusBarDisabled(
        disabled,
        (v) -> onSuccess("Set status bar disabled to %b", disabled),
        (e) -> onError(e, "Error setting status bar disabled to %b", disabled));
  }

  private void isLockTaskPermitted(String[] packageNames) {
    for (String packageName : packageNames) {
      boolean permitted = mDevicePolicyManagerGateway.isLockTaskPermitted(packageName);
      mWriter.printf("%s: %s\n", packageName, permittedToString(permitted));
    }
  }

  private void installExistingPackage(String packageName) {
    mDevicePolicyManagerGateway.installExistingPackage(packageName,
        (v) -> onSuccess("Installed existing package %s", packageName),
        (e) -> onError(e, "Error installing existing package %s", packageName));
  }

  private void setUninstallBlocked(String packageName, boolean uninstallBlocked) {
    mDevicePolicyManagerGateway.setUninstallBlocked(packageName, uninstallBlocked,
        (v) -> onSuccess("%s uninstall of pacakge %s", (uninstallBlocked ? "Blocked" : "Unblocked"),
            packageName),
        (e) -> onError(e, "Error setting uninstal-blocked of pacakge %s to %b", packageName,
            uninstallBlocked));
  }

  private void isUninstallBlocked(String packageName) {
    boolean isIt = mDevicePolicyManagerGateway.isUninstallBlocked(packageName);
    Log.d(TAG, "isUninstallBlocked(" + packageName + "): " + isIt);
    mWriter.println(isIt);
  }

  private void setSecureSetting(String setting, String value) {
      mDevicePolicyManagerGateway.setSecureSetting(setting, value,
          (v) -> onSuccess("Set secure setting '%s' to '%s'", setting, value),
          (e) -> onError(e, "Error setting secure setting '%s' to '%s'", setting, value));
  }

  private void setGlobalSetting(String setting, String value) {
      mDevicePolicyManagerGateway.setGlobalSetting(setting, value,
          (v) -> onSuccess("Set global setting '%s' to '%s'", setting, value),
          (e) -> onError(e, "Error setting global setting '%s' to '%s'", setting, value));
  }

  private void hasKeyPair(String alias) {
    mWriter.println(mDevicePolicyManagerGateway.hasKeyPair(alias));
  }

  private void generateDeviceAttestationKeyPair(String alias, int flags) {
    // Cannot call dpm.generateKeyPair() on main thread
    warnAboutAsyncCall();
    post(()->handleDeviceAttestationKeyPair(alias, flags));
  }

  private void removeKeyPair(String alias) {
    mDevicePolicyManagerGateway.removeKeyPair(alias,
        (v) -> onSuccess("%s certificate withalias %s", (v ? "Removed" : "Didn't remove"), alias),
        (e) -> onError(e, "Error removing certificate with alias %s", alias));
  }

  private void grantKeyPairToApp(String alias, String packageName) {
    mDevicePolicyManagerGateway.grantKeyPairToApp(alias, packageName,
        (v) -> onSuccess("%s certificate with alias %s to app %s", (v ? "Granted" : "Didn't grant"),
            alias, packageName),
        (e) -> onError(e, "Error granting certificate with alias %s to app %s", alias,
            packageName));
  }

  private void getKeyPairGrants(String alias) {
    Map<Integer, Set<String>> grants = mDevicePolicyManagerGateway.getKeyPairGrants(alias);
    if (grants.isEmpty()) {
      mWriter.printf("%s not granted to any app\n", alias);
      return;
    }

    mWriter.printf("%s granted to %d apps, listed by uid:\n", alias, grants.size());
    for (Map.Entry<Integer, Set<String>> app : grants.entrySet()) {
      int uid = app.getKey();
      Set<String> packages = app.getValue();
      mWriter.printf("\t%d: ", uid);
      packages.forEach((p) -> mWriter.printf("%s ", p));
      mWriter.println();
    }
  }

  private void revokeKeyPairFromApp(String alias, String packageName) {
    mDevicePolicyManagerGateway.revokeKeyPairFromApp(alias, packageName,
        (v) -> onSuccess("%s certificate with alias %s to app %s", (v ? "Revoked" : "Didn't revoke"),
            alias, packageName),
        (e) -> onError(e, "Error revoking certificate with alias %s to app %s", alias,
            packageName));
  }

  private void getDelegatedScopes(String delegatePackage) {
    List<String> scopes = mDevicePolicyManagerGateway.getDelegatedScopes(delegatePackage);
    Log.d(TAG, "getDelegatedScopes(" + delegatePackage + "): " + scopes);
    printCollection("scope", scopes);
  }

  private void getDelegatePackages(String delegationScope) {
    List<String> packages  = mDevicePolicyManagerGateway.getDelegatePackages(delegationScope);
    Log.d(TAG, "getDelegatePackages(" + delegationScope + "): " + packages);
    printCollection("package", packages);
  }

  private void setDelegatedScopes(String delegatePackage, String[] scopesArray) {
    List<String> scopes = Arrays.asList(scopesArray);
    mDevicePolicyManagerGateway.setDelegatedScopes(delegatePackage, scopes,
        (v) -> onSuccess("Set %d scopes (%s) to app %s", scopes.size(), scopes, delegatePackage),
        (e) -> onError(e, "Error setting %s scopes to app %s", scopesArray, delegatePackage));
  }

  private void setMeteredDataDisabledPackages(String[] packageNames) {
    List<String> disabledPackages = Arrays.asList(packageNames);
    mDevicePolicyManagerGateway.setMeteredDataDisabledPackages(
        disabledPackages,
        (v) ->
            onSuccess(
                "Restricted following packages from using metered data: %s", disabledPackages),
        (e) ->
            onError(e, "Error restricting following packages from using metered data: %s", disabledPackages));
  }

  private void getMeteredDataDisabledPackages() {
    List<String> disabledPackages = mDevicePolicyManagerGateway.getMeteredDataDisabledPackages();
    Log.d(TAG, "getMeteredDataDisabledPackages(): " + disabledPackages);
    printCollection("disabled-packages", disabledPackages);
  }

  private void warnAboutAsyncCall() {
    mWriter.printf("Command will be executed asynchronally; use `adb logcat %s *:s` for result\n",
        TAG);
  }

  private void handleDeviceAttestationKeyPair(String alias, int flags) {
    KeyGenParameterSpec keySpec = buildRsaKeySpecWithKeyAttestation(alias);
    String algorithm = "RSA";
    mDevicePolicyManagerGateway.generateKeyPair(algorithm, keySpec, flags,
        (v) -> onSuccessLog("Generated key: %s", v),
        (e) -> onErrorLog(e, "Error generating key with alias %s, flags %d, and spec %s",
            alias, flags, keySpec));
  }

  private void post(Runnable r) {
    if (mHandler == null) {
      HandlerThread handlerThread = new HandlerThread("ShellCommandThread");
      Log.i(TAG, "Starting " + handlerThread);
      handlerThread.start();
      mHandler = new Handler(handlerThread.getLooper());
    }
    Log.d(TAG, "posting runnable");
    mHandler.post(r);
  }

  private static void onSuccessLog(String template, Object... args) {
    Util.onSuccessLog(TAG, template, args);
  }

  private static void onErrorLog(Exception e, String template, Object... args) {
    Util.onErrorLog(TAG, e, template, args);
  }

  private void onSuccess(@NonNull String pattern, @Nullable Object... args) {
    String msg = String.format(pattern, args);
    Log.d(TAG, msg);
    mWriter.println(msg);
  }

  private void onError(@NonNull Exception e, @NonNull String pattern, @Nullable Object... args) {
    String msg = String.format(pattern, args);
    Log.e(TAG, msg, e);
    mWriter.printf("%s: %s\n", msg, e);
  }

  private void printCollection(String nameOnSingular, Collection<String> collection) {
    if (collection.isEmpty()) {
      mWriter.printf("No %ss\n", nameOnSingular);
      return;
    }
    int size = collection.size();
    mWriter.printf("%d %s%s:\n", size, nameOnSingular, size == 1 ? "" : "s");
    collection.forEach((s) -> mWriter.printf("  %s\n", s));
  }

  private void printTime(long time) {
    String formattedDate = time > 0 ? new java.sql.Date(time).toString() : "N/A";
    mWriter.printf("%d (%s)\n", time, formattedDate);
  }

  @TargetApi(VERSION_CODES.N)
  @SuppressWarnings("SimpleDateFormat")
  private void printSecurityLogs(List<SecurityEvent> events) {
    if (events == null || events.isEmpty()) {
      mWriter.println("N/A");
      return;
    }
    mWriter.printf("%d events:\n", events.size());
    SimpleDateFormat formatter = new SimpleDateFormat("MM-dd HH:mm:ss.SSS");
    for (int i = 0; i < events.size(); i++) {
      SecurityEvent event = events.get(i);
      StringBuilder sb = new StringBuilder();
      if (Util.SDK_INT >= VERSION_CODES.P) {
        sb.append(event.getId() + ": ");
      }
      sb.append(SecurityLogsFragment.getStringEventTagFromId(event.getTag()));
      sb.append(" (")
          .append(formatter.format(new Date(TimeUnit.NANOSECONDS.toMillis(event.getTimeNanos()))))
          .append("): ");
      SecurityLogsFragment.printData(sb, event.getData());
      mWriter.printf("%s\n", sb.toString());
    }
  }

  private String toString(UserHandle user) {
    return user.toString() + " serial=" + mDevicePolicyManagerGateway.getSerialNumber(user);
  }

  private Set<String> getOrderedSortedSet(String[] args) {
    return new LinkedHashSet<>(Arrays.asList(args));
  }

  // Copied from CTS' KeyGenerationUtils
  private static KeyGenParameterSpec buildRsaKeySpecWithKeyAttestation(String alias) {
    return new KeyGenParameterSpec.Builder(alias,
        KeyProperties.PURPOSE_SIGN | KeyProperties.PURPOSE_VERIFY)
            .setKeySize(2048)
            .setDigests(KeyProperties.DIGEST_SHA256)
            .setSignaturePaddings(KeyProperties.SIGNATURE_PADDING_RSA_PSS,
                KeyProperties.SIGNATURE_PADDING_RSA_PKCS1)
            .setIsStrongBoxBacked(false)
            .setAttestationChallenge(new byte[] {
                'a', 'b', 'c'
            })
            .build();
  }

  private static final class KeyValue {
    private final String key;
    private final String value;

    private KeyValue(String key, String value) {
      this.key = key;
      this.value = value;
    }

    public String key() {
      return key;
    }

    public String value() {
      return value;
    }
  }
}
