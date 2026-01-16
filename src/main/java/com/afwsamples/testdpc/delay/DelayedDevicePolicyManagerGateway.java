/*
 * Copyright (C) 2024 The Android Open Source Project
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
package com.afwsamples.testdpc.delay;

import android.app.admin.DevicePolicyManager;
import android.app.admin.NetworkEvent;
import android.app.admin.SecurityLog.SecurityEvent;
import android.content.ComponentName;
import android.content.Context;
import android.content.Intent;
import android.content.pm.PackageManager.NameNotFoundException;
import android.graphics.Bitmap;
import android.os.Bundle;
import android.os.PersistableBundle;
import android.os.UserHandle;
import android.security.AttestedKeyPair;
import android.security.keystore.KeyGenParameterSpec;
import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import com.afwsamples.testdpc.DevicePolicyManagerGateway;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.function.Consumer;

/**
 * Decorator that wraps DevicePolicyManagerGateway to add delay functionality.
 * When delay is enabled, mutating operations are queued instead of executed immediately.
 */
public class DelayedDevicePolicyManagerGateway implements DevicePolicyManagerGateway {

    private final DevicePolicyManagerGateway delegate;
    private final DelayManager delayManager;
    private final Context context;

    public DelayedDevicePolicyManagerGateway(
            DevicePolicyManagerGateway delegate,
            Context context) {
        this.delegate = delegate;
        this.context = context;
        this.delayManager = DelayManager.getInstance(context);
    }

    /**
     * Get the underlying delegate gateway for direct access when needed.
     */
    public DevicePolicyManagerGateway getDelegate() {
        return delegate;
    }

    // ==================== HELPER METHODS ====================

    /**
     * Queue an action if delay is enabled, otherwise execute immediately.
     * For methods with callbacks.
     */
    private <T> void queueOrExecute(
            String actionType,
            String description,
            Object[] params,
            Runnable immediateAction,
            Consumer<T> onSuccess,
            Consumer<Exception> onError) {
        if (delayManager.isDelayEnabled()) {
            String actionData = DPMAction.serialize(params);
            delayManager.queueAction(actionType, actionData, description);
            // Call success callback with null - action is queued, not executed
            if (onSuccess != null) {
                onSuccess.accept(null);
            }
        } else {
            immediateAction.run();
        }
    }

    /**
     * Queue an action if delay is enabled, otherwise execute immediately.
     * For methods without callbacks.
     */
    private void queueOrExecuteVoid(
            String actionType,
            String description,
            Object[] params,
            Runnable immediateAction) {
        if (delayManager.isDelayEnabled()) {
            String actionData = DPMAction.serialize(params);
            delayManager.queueAction(actionType, actionData, description);
        } else {
            immediateAction.run();
        }
    }

    // ==================== READ-ONLY METHODS (pass through directly) ====================

    @NonNull
    @Override
    public ComponentName getAdmin() {
        return delegate.getAdmin();
    }

    @NonNull
    @Override
    public DevicePolicyManager getDevicePolicyManager() {
        return delegate.getDevicePolicyManager();
    }

    @Override
    public boolean isDeviceOwnerApp() {
        return delegate.isDeviceOwnerApp();
    }

    @Override
    public boolean isProfileOwnerApp() {
        return delegate.isProfileOwnerApp();
    }

    @Override
    public boolean isOrganizationOwnedDeviceWithManagedProfile() {
        return delegate.isOrganizationOwnedDeviceWithManagedProfile();
    }

    @Override
    public boolean isHeadlessSystemUserMode() {
        return delegate.isHeadlessSystemUserMode();
    }

    @Override
    public boolean isUserForeground() {
        return delegate.isUserForeground();
    }

    @Override
    public List<UserHandle> listForegroundAffiliatedUsers() {
        return delegate.listForegroundAffiliatedUsers();
    }

    @NonNull
    @Override
    public CharSequence getStartUserSessionMessage() {
        return delegate.getStartUserSessionMessage();
    }

    @NonNull
    @Override
    public CharSequence getEndUserSessionMessage() {
        return delegate.getEndUserSessionMessage();
    }

    @Nullable
    @Override
    public UserHandle getUserHandle(long serialNumber) {
        return delegate.getUserHandle(serialNumber);
    }

    @Override
    public long getSerialNumber(@NonNull UserHandle user) {
        return delegate.getSerialNumber(user);
    }

    @Override
    public boolean isLogoutEnabled() {
        return delegate.isLogoutEnabled();
    }

    @Override
    public boolean isAffiliatedUser() {
        return delegate.isAffiliatedUser();
    }

    @NonNull
    @Override
    public Set<String> getAffiliationIds() {
        return delegate.getAffiliationIds();
    }

    @NonNull
    @Override
    public Set<String> getUserRestrictions() {
        return delegate.getUserRestrictions();
    }

    @Override
    public boolean hasUserRestriction(@NonNull String userRestriction) {
        return delegate.hasUserRestriction(userRestriction);
    }

    @Override
    public boolean isNetworkLoggingEnabled() {
        return delegate.isNetworkLoggingEnabled();
    }

    @Override
    public long getLastNetworkLogRetrievalTime() {
        return delegate.getLastNetworkLogRetrievalTime();
    }

    @Override
    public List<NetworkEvent> retrieveNetworkLogs(long batchToken) {
        return delegate.retrieveNetworkLogs(batchToken);
    }

    @Override
    public boolean isSecurityLoggingEnabled() {
        return delegate.isSecurityLoggingEnabled();
    }

    @Override
    public long getLastSecurityLogRetrievalTime() {
        return delegate.getLastSecurityLogRetrievalTime();
    }

    @Override
    public List<SecurityEvent> retrieveSecurityLogs() {
        return delegate.retrieveSecurityLogs();
    }

    @Override
    public List<SecurityEvent> retrievePreRebootSecurityLogs() {
        return delegate.retrievePreRebootSecurityLogs();
    }

    @Nullable
    @Override
    public CharSequence getOrganizationName() {
        return delegate.getOrganizationName();
    }

    @NonNull
    @Override
    public List<String> getUserControlDisabledPackages() {
        return delegate.getUserControlDisabledPackages();
    }

    @NonNull
    @Override
    public Set<String> getCrossProfilePackages() {
        return delegate.getCrossProfilePackages();
    }

    @Override
    public int getPasswordQuality() {
        return delegate.getPasswordQuality();
    }

    @Override
    public int getRequiredPasswordComplexity() {
        return delegate.getRequiredPasswordComplexity();
    }

    @Override
    public boolean isActivePasswordSufficient() {
        return delegate.isActivePasswordSufficient();
    }

    @Override
    public boolean isActivePasswordSufficientForDeviceRequirement() {
        return delegate.isActivePasswordSufficientForDeviceRequirement();
    }

    @Override
    public boolean isPreferentialNetworkServiceEnabled() {
        return delegate.isPreferentialNetworkServiceEnabled();
    }

    @Override
    public boolean isPackageSuspended(String packageName) throws NameNotFoundException {
        return delegate.isPackageSuspended(packageName);
    }

    @Override
    public boolean isApplicationHidden(String packageName) throws NameNotFoundException {
        return delegate.isApplicationHidden(packageName);
    }

    @Override
    public int getPersonalAppsSuspendedReasons() {
        return delegate.getPersonalAppsSuspendedReasons();
    }

    @NonNull
    @Override
    public List<String> getDisabledSystemApps() {
        return delegate.getDisabledSystemApps();
    }

    @Override
    public String[] getLockTaskPackages() {
        return delegate.getLockTaskPackages();
    }

    @Override
    public int getLockTaskFeatures() {
        return delegate.getLockTaskFeatures();
    }

    @Override
    public boolean isLockTaskPermitted(String packageName) {
        return delegate.isLockTaskPermitted(packageName);
    }

    @Override
    public Bundle getApplicationRestrictions(String packageName) {
        return delegate.getApplicationRestrictions(packageName);
    }

    @Override
    public Bundle getSelfRestrictions() {
        return delegate.getSelfRestrictions();
    }

    @Override
    public int getPermissionGrantState(String packageName, String permission) {
        return delegate.getPermissionGrantState(packageName, permission);
    }

    @Override
    public boolean canAdminGrantSensorsPermissions() {
        return delegate.canAdminGrantSensorsPermissions();
    }

    @Override
    public boolean isLocationEnabled() {
        return delegate.isLocationEnabled();
    }

    @Override
    public CharSequence getDeviceOwnerLockScreenInfo() {
        return delegate.getDeviceOwnerLockScreenInfo();
    }

    @Override
    public int getKeyguardDisabledFeatures() {
        return delegate.getKeyguardDisabledFeatures();
    }

    @Override
    public boolean getCameraDisabled() {
        return delegate.getCameraDisabled();
    }

    @Override
    public boolean getCameraDisabledByAnyAdmin() {
        return delegate.getCameraDisabledByAnyAdmin();
    }

    @Override
    public int getMaximumFailedPasswordsForWipe() {
        return delegate.getMaximumFailedPasswordsForWipe();
    }

    @Override
    public boolean isUninstallBlocked(@NonNull String packageName) {
        return delegate.isUninstallBlocked(packageName);
    }

    @Override
    public boolean isDeviceIdAttestationSupported() {
        return delegate.isDeviceIdAttestationSupported();
    }

    @Override
    public boolean isUniqueDeviceAttestationSupported() {
        return delegate.isUniqueDeviceAttestationSupported();
    }

    @Override
    public boolean hasKeyPair(String alias) {
        return delegate.hasKeyPair(alias);
    }

    @NonNull
    @Override
    public Map<Integer, Set<String>> getKeyPairGrants(@NonNull String alias) {
        return delegate.getKeyPairGrants(alias);
    }

    @NonNull
    @Override
    public List<String> getDelegatedScopes(@NonNull String delegatePackage) {
        return delegate.getDelegatedScopes(delegatePackage);
    }

    @NonNull
    @Override
    public List<String> getDelegatePackages(@NonNull String delegationScope) {
        return delegate.getDelegatePackages(delegationScope);
    }

    @NonNull
    @Override
    public List<String> getMeteredDataDisabledPackages() {
        return delegate.getMeteredDataDisabledPackages();
    }

    @NonNull
    @Override
    public List<UserHandle> getSecondaryUsers() {
        return delegate.getSecondaryUsers();
    }

    @Override
    public long getLastBugReportRequestTime() {
        return delegate.getLastBugReportRequestTime();
    }

    // ==================== MUTATING METHODS (require delay) ====================

    @Override
    public void createAndManageUser(
            @Nullable String name,
            int flags,
            @NonNull Consumer<UserHandle> onSuccess,
            @NonNull Consumer<Exception> onError) {
        queueOrExecute("createAndManageUser",
            "Create user: " + (name != null ? name : "unnamed"),
            new Object[]{name, flags},
            () -> delegate.createAndManageUser(name, flags, onSuccess, onError),
            onSuccess, onError);
    }

    @Override
    public void setUserIcon(
            @NonNull Bitmap icon,
            @NonNull Consumer<Void> onSuccess,
            @NonNull Consumer<Exception> onError) {
        queueOrExecute("setUserIcon", "Set user icon",
            new Object[]{},
            () -> delegate.setUserIcon(icon, onSuccess, onError),
            onSuccess, onError);
    }

    @Override
    public void setStartUserSessionMessage(
            @Nullable CharSequence message,
            @NonNull Consumer<Void> onSuccess,
            @NonNull Consumer<Exception> onError) {
        queueOrExecute("setStartUserSessionMessage",
            "Set start session message",
            new Object[]{message != null ? message.toString() : null},
            () -> delegate.setStartUserSessionMessage(message, onSuccess, onError),
            onSuccess, onError);
    }

    @Override
    public void setEndUserSessionMessage(
            @Nullable CharSequence message,
            @NonNull Consumer<Void> onSuccess,
            @NonNull Consumer<Exception> onError) {
        queueOrExecute("setEndUserSessionMessage",
            "Set end session message",
            new Object[]{message != null ? message.toString() : null},
            () -> delegate.setEndUserSessionMessage(message, onSuccess, onError),
            onSuccess, onError);
    }

    @Override
    public void removeUser(
            @NonNull UserHandle userHandle,
            @NonNull Consumer<Void> onSuccess,
            @NonNull Consumer<Exception> onError) {
        queueOrExecute("removeUser",
            "Remove user",
            new Object[]{userHandle.hashCode()},
            () -> delegate.removeUser(userHandle, onSuccess, onError),
            onSuccess, onError);
    }

    @Override
    public void switchUser(
            @NonNull UserHandle userHandle,
            @NonNull Consumer<Void> onSuccess,
            @NonNull Consumer<Exception> onError) {
        queueOrExecute("switchUser",
            "Switch user",
            new Object[]{userHandle.hashCode()},
            () -> delegate.switchUser(userHandle, onSuccess, onError),
            onSuccess, onError);
    }

    @Override
    public void startUserInBackground(
            @NonNull UserHandle userHandle,
            @NonNull Consumer<Integer> onSuccess,
            @NonNull Consumer<Exception> onError) {
        queueOrExecute("startUserInBackground",
            "Start user in background",
            new Object[]{userHandle.hashCode()},
            () -> delegate.startUserInBackground(userHandle, onSuccess, onError),
            onSuccess, onError);
    }

    @Override
    public void stopUser(
            @NonNull UserHandle userHandle,
            @NonNull Consumer<Integer> onSuccess,
            @NonNull Consumer<Exception> onError) {
        queueOrExecute("stopUser",
            "Stop user",
            new Object[]{userHandle.hashCode()},
            () -> delegate.stopUser(userHandle, onSuccess, onError),
            onSuccess, onError);
    }

    @Override
    public void setLogoutEnabled(
            boolean enabled,
            @NonNull Consumer<Void> onSuccess,
            @NonNull Consumer<Exception> onError) {
        queueOrExecute("setLogoutEnabled",
            (enabled ? "Enable" : "Disable") + " logout",
            new Object[]{enabled},
            () -> delegate.setLogoutEnabled(enabled, onSuccess, onError),
            onSuccess, onError);
    }

    @Override
    public void logoutUser(
            @NonNull Consumer<Integer> onSuccess,
            @NonNull Consumer<Exception> onError) {
        queueOrExecute("logoutUser",
            "Logout user",
            new Object[]{},
            () -> delegate.logoutUser(onSuccess, onError),
            onSuccess, onError);
    }

    @Override
    public void setAffiliationIds(@NonNull Set<String> ids) {
        queueOrExecuteVoid("setAffiliationIds",
            "Set affiliation IDs",
            new Object[]{ids.toArray(new String[0])},
            () -> delegate.setAffiliationIds(ids));
    }

    @Override
    public void setUserRestriction(
            @NonNull String userRestriction,
            boolean enabled,
            @NonNull Consumer<Void> onSuccess,
            @NonNull Consumer<Exception> onError) {
        queueOrExecute("setUserRestriction",
            (enabled ? "Enable" : "Disable") + " restriction: " + userRestriction,
            new Object[]{userRestriction, enabled},
            () -> delegate.setUserRestriction(userRestriction, enabled, onSuccess, onError),
            onSuccess, onError);
    }

    @Override
    public void setUserRestriction(@NonNull String userRestriction, boolean enabled) {
        queueOrExecuteVoid("setUserRestriction",
            (enabled ? "Enable" : "Disable") + " restriction: " + userRestriction,
            new Object[]{userRestriction, enabled},
            () -> delegate.setUserRestriction(userRestriction, enabled));
    }

    @Override
    public void lockNow(@NonNull Consumer<Void> onSuccess, @NonNull Consumer<Exception> onError) {
        queueOrExecute("lockNow",
            "Lock device",
            new Object[]{},
            () -> delegate.lockNow(onSuccess, onError),
            onSuccess, onError);
    }

    @Override
    public void lockNow(int flags, @NonNull Consumer<Void> onSuccess, @NonNull Consumer<Exception> onError) {
        queueOrExecute("lockNowWithFlags",
            "Lock device",
            new Object[]{flags},
            () -> delegate.lockNow(flags, onSuccess, onError),
            onSuccess, onError);
    }

    @Override
    public void reboot(@NonNull Consumer<Void> onSuccess, @NonNull Consumer<Exception> onError) {
        queueOrExecute("reboot",
            "Reboot device",
            new Object[]{},
            () -> delegate.reboot(onSuccess, onError),
            onSuccess, onError);
    }

    @Override
    public void wipeData(int flags, @NonNull Consumer<Void> onSuccess, @NonNull Consumer<Exception> onError) {
        queueOrExecute("wipeData",
            "Wipe data",
            new Object[]{flags},
            () -> delegate.wipeData(flags, onSuccess, onError),
            onSuccess, onError);
    }

    @Override
    public void wipeDevice(int flags, @NonNull Consumer<Void> onSuccess, @NonNull Consumer<Exception> onError) {
        queueOrExecute("wipeDevice",
            "Wipe device",
            new Object[]{flags},
            () -> delegate.wipeDevice(flags, onSuccess, onError),
            onSuccess, onError);
    }

    @Override
    public void requestBugreport(@NonNull Consumer<Void> onSuccess, @NonNull Consumer<Exception> onError) {
        queueOrExecute("requestBugreport",
            "Request bug report",
            new Object[]{},
            () -> delegate.requestBugreport(onSuccess, onError),
            onSuccess, onError);
    }

    @Override
    public void setNetworkLoggingEnabled(
            boolean enabled,
            @NonNull Consumer<Void> onSuccess,
            @NonNull Consumer<Exception> onError) {
        queueOrExecute("setNetworkLoggingEnabled",
            (enabled ? "Enable" : "Disable") + " network logging",
            new Object[]{enabled},
            () -> delegate.setNetworkLoggingEnabled(enabled, onSuccess, onError),
            onSuccess, onError);
    }

    @Override
    public void setNetworkLoggingEnabled(boolean enabled) {
        queueOrExecuteVoid("setNetworkLoggingEnabled",
            (enabled ? "Enable" : "Disable") + " network logging",
            new Object[]{enabled},
            () -> delegate.setNetworkLoggingEnabled(enabled));
    }

    @Override
    public void setSecurityLoggingEnabled(
            boolean enabled,
            @NonNull Consumer<Void> onSuccess,
            @NonNull Consumer<Exception> onError) {
        queueOrExecute("setSecurityLoggingEnabled",
            (enabled ? "Enable" : "Disable") + " security logging",
            new Object[]{enabled},
            () -> delegate.setSecurityLoggingEnabled(enabled, onSuccess, onError),
            onSuccess, onError);
    }

    @Override
    public void setOrganizationName(
            @Nullable CharSequence title,
            @NonNull Consumer<Void> onSuccess,
            @NonNull Consumer<Exception> onError) {
        queueOrExecute("setOrganizationName",
            "Set organization name: " + title,
            new Object[]{title != null ? title.toString() : null},
            () -> delegate.setOrganizationName(title, onSuccess, onError),
            onSuccess, onError);
    }

    @Override
    public void setUserControlDisabledPackages(
            @Nullable List<String> packages,
            @NonNull Consumer<Void> onSuccess,
            @NonNull Consumer<Exception> onError) {
        queueOrExecute("setUserControlDisabledPackages",
            "Set user control disabled packages",
            new Object[]{packages},
            () -> delegate.setUserControlDisabledPackages(packages, onSuccess, onError),
            onSuccess, onError);
    }

    @Override
    public void setCrossProfilePackages(
            @NonNull Set<String> packages,
            @NonNull Consumer<Void> onSuccess,
            @NonNull Consumer<Exception> onError) {
        queueOrExecute("setCrossProfilePackages",
            "Set cross-profile packages",
            new Object[]{packages.toArray(new String[0])},
            () -> delegate.setCrossProfilePackages(packages, onSuccess, onError),
            onSuccess, onError);
    }

    @Override
    public boolean setPermittedInputMethods(
            List<String> packageNames,
            @NonNull Consumer<Void> onSuccess,
            @NonNull Consumer<Exception> onError) {
        if (delayManager.isDelayEnabled()) {
            delayManager.queueAction("setPermittedInputMethods",
                DPMAction.serialize(packageNames),
                "Set permitted input methods");
            if (onSuccess != null) onSuccess.accept(null);
            return true;
        }
        return delegate.setPermittedInputMethods(packageNames, onSuccess, onError);
    }

    @Override
    public boolean setPermittedInputMethods(List<String> packageNames) {
        if (delayManager.isDelayEnabled()) {
            delayManager.queueAction("setPermittedInputMethods",
                DPMAction.serialize(packageNames),
                "Set permitted input methods");
            return true;
        }
        return delegate.setPermittedInputMethods(packageNames);
    }

    @Override
    public void removeActiveAdmin(@NonNull Consumer<Void> onSuccess, @NonNull Consumer<Exception> onError) {
        queueOrExecute("removeActiveAdmin",
            "Remove active admin",
            new Object[]{},
            () -> delegate.removeActiveAdmin(onSuccess, onError),
            onSuccess, onError);
    }

    @Override
    public void clearDeviceOwnerApp(@NonNull Consumer<Void> onSuccess, @NonNull Consumer<Exception> onError) {
        queueOrExecute("clearDeviceOwnerApp",
            "Clear device owner",
            new Object[]{},
            () -> delegate.clearDeviceOwnerApp(onSuccess, onError),
            onSuccess, onError);
    }

    @Override
    public void clearProfileOwner(@NonNull Consumer<Void> onSuccess, @NonNull Consumer<Exception> onError) {
        queueOrExecute("clearProfileOwner",
            "Clear profile owner",
            new Object[]{},
            () -> delegate.clearProfileOwner(onSuccess, onError),
            onSuccess, onError);
    }

    @Override
    public void setPasswordQuality(
            int quality,
            @NonNull Consumer<Void> onSuccess,
            @NonNull Consumer<Exception> onError) {
        queueOrExecute("setPasswordQuality",
            "Set password quality: " + quality,
            new Object[]{quality},
            () -> delegate.setPasswordQuality(quality, onSuccess, onError),
            onSuccess, onError);
    }

    @Override
    public void setRequiredPasswordComplexity(
            int quality,
            @NonNull Consumer<Void> onSuccess,
            @NonNull Consumer<Exception> onError) {
        queueOrExecute("setRequiredPasswordComplexity",
            "Set required password complexity: " + quality,
            new Object[]{quality},
            () -> delegate.setRequiredPasswordComplexity(quality, onSuccess, onError),
            onSuccess, onError);
    }

    @Override
    public void transferOwnership(
            @NonNull ComponentName target,
            @Nullable PersistableBundle bundle,
            @NonNull Consumer<Void> onSuccess,
            @NonNull Consumer<Exception> onError) {
        queueOrExecute("transferOwnership",
            "Transfer ownership to: " + target.flattenToString(),
            new Object[]{target.flattenToString()},
            () -> delegate.transferOwnership(target, bundle, onSuccess, onError),
            onSuccess, onError);
    }

    @Override
    public void setUsbDataSignalingEnabled(
            boolean enabled,
            @NonNull Consumer<Void> onSuccess,
            @NonNull Consumer<Exception> onError) {
        queueOrExecute("setUsbDataSignalingEnabled",
            (enabled ? "Enable" : "Disable") + " USB data signaling",
            new Object[]{enabled},
            () -> delegate.setUsbDataSignalingEnabled(enabled, onSuccess, onError),
            onSuccess, onError);
    }

    @Override
    public void setUsbDataSignalingEnabled(boolean enabled) {
        queueOrExecuteVoid("setUsbDataSignalingEnabled",
            (enabled ? "Enable" : "Disable") + " USB data signaling",
            new Object[]{enabled},
            () -> delegate.setUsbDataSignalingEnabled(enabled));
    }

    @Override
    public void setPreferentialNetworkServiceEnabled(
            boolean enabled,
            @NonNull Consumer<Void> onSuccess,
            @NonNull Consumer<Exception> onError) {
        queueOrExecute("setPreferentialNetworkServiceEnabled",
            (enabled ? "Enable" : "Disable") + " preferential network service",
            new Object[]{enabled},
            () -> delegate.setPreferentialNetworkServiceEnabled(enabled, onSuccess, onError),
            onSuccess, onError);
    }

    @Override
    public void setPackagesSuspended(
            String[] packageNames,
            boolean suspended,
            @NonNull Consumer<String[]> onSuccess,
            @NonNull Consumer<Exception> onError) {
        queueOrExecute("setPackagesSuspended",
            (suspended ? "Suspend" : "Unsuspend") + " packages",
            new Object[]{packageNames, suspended},
            () -> delegate.setPackagesSuspended(packageNames, suspended, onSuccess, onError),
            onSuccess, onError);
    }

    @Override
    public void setApplicationHidden(
            String packageName,
            boolean hidden,
            @NonNull Consumer<Void> onSuccess,
            @NonNull Consumer<Exception> onError) {
        queueOrExecute("setApplicationHidden",
            (hidden ? "Hide" : "Unhide") + " app: " + packageName,
            new Object[]{packageName, hidden},
            () -> delegate.setApplicationHidden(packageName, hidden, onSuccess, onError),
            onSuccess, onError);
    }

    @Override
    public void setPersonalAppsSuspended(
            boolean suspended,
            @NonNull Consumer<Void> onSuccess,
            @NonNull Consumer<Exception> onError) {
        queueOrExecute("setPersonalAppsSuspended",
            (suspended ? "Suspend" : "Unsuspend") + " personal apps",
            new Object[]{suspended},
            () -> delegate.setPersonalAppsSuspended(suspended, onSuccess, onError),
            onSuccess, onError);
    }

    @Override
    public void enableSystemApp(
            String packageName,
            @NonNull Consumer<Void> onSuccess,
            @NonNull Consumer<Exception> onError) {
        queueOrExecute("enableSystemApp",
            "Enable system app: " + packageName,
            new Object[]{packageName},
            () -> delegate.enableSystemApp(packageName, onSuccess, onError),
            onSuccess, onError);
    }

    @Override
    public void enableSystemApp(
            Intent intent,
            @NonNull Consumer<Integer> onSuccess,
            @NonNull Consumer<Exception> onError) {
        queueOrExecute("enableSystemAppByIntent",
            "Enable system app by intent",
            new Object[]{intent.toUri(0)},
            () -> delegate.enableSystemApp(intent, onSuccess, onError),
            onSuccess, onError);
    }

    @Override
    public void setLockTaskPackages(
            String[] packages,
            @NonNull Consumer<Void> onSuccess,
            @NonNull Consumer<Exception> onError) {
        queueOrExecute("setLockTaskPackages",
            "Set lock task packages",
            new Object[]{packages},
            () -> delegate.setLockTaskPackages(packages, onSuccess, onError),
            onSuccess, onError);
    }

    @Override
    public void setLockTaskFeatures(
            int flags,
            @NonNull Consumer<Void> onSuccess,
            @NonNull Consumer<Exception> onError) {
        queueOrExecute("setLockTaskFeatures",
            "Set lock task features: " + flags,
            new Object[]{flags},
            () -> delegate.setLockTaskFeatures(flags, onSuccess, onError),
            onSuccess, onError);
    }

    @Override
    public void setApplicationRestrictions(
            String packageName,
            Bundle settings,
            @NonNull Consumer<Void> onSuccess,
            @NonNull Consumer<Exception> onError) {
        queueOrExecute("setApplicationRestrictions",
            "Set app restrictions: " + packageName,
            new Object[]{packageName},
            () -> delegate.setApplicationRestrictions(packageName, settings, onSuccess, onError),
            onSuccess, onError);
    }

    @Override
    public void setPermissionGrantState(
            String packageName,
            String permission,
            int grantState,
            @NonNull Consumer<Void> onSuccess,
            @NonNull Consumer<Exception> onError) {
        queueOrExecute("setPermissionGrantState",
            "Set permission " + permission + " for " + packageName,
            new Object[]{packageName, permission, grantState},
            () -> delegate.setPermissionGrantState(packageName, permission, grantState, onSuccess, onError),
            onSuccess, onError);
    }

    @Override
    public void setLocationEnabled(
            boolean enabled,
            @NonNull Consumer<Void> onSuccess,
            @NonNull Consumer<Exception> onError) {
        queueOrExecute("setLocationEnabled",
            (enabled ? "Enable" : "Disable") + " location",
            new Object[]{enabled},
            () -> delegate.setLocationEnabled(enabled, onSuccess, onError),
            onSuccess, onError);
    }

    @Override
    public void setDeviceOwnerLockScreenInfo(
            CharSequence info,
            @NonNull Consumer<Void> onSuccess,
            @NonNull Consumer<Exception> onError) {
        queueOrExecute("setDeviceOwnerLockScreenInfo",
            "Set lock screen info",
            new Object[]{info != null ? info.toString() : null},
            () -> delegate.setDeviceOwnerLockScreenInfo(info, onSuccess, onError),
            onSuccess, onError);
    }

    @Override
    public void setKeyguardDisabled(
            boolean disabled,
            @NonNull Consumer<Void> onSuccess,
            @NonNull Consumer<Exception> onError) {
        queueOrExecute("setKeyguardDisabled",
            (disabled ? "Disable" : "Enable") + " keyguard",
            new Object[]{disabled},
            () -> delegate.setKeyguardDisabled(disabled, onSuccess, onError),
            onSuccess, onError);
    }

    @Override
    public void setKeyguardDisabledFeatures(
            int which,
            @NonNull Consumer<Void> onSuccess,
            @NonNull Consumer<Exception> onError) {
        queueOrExecute("setKeyguardDisabledFeatures",
            "Set keyguard disabled features: " + which,
            new Object[]{which},
            () -> delegate.setKeyguardDisabledFeatures(which, onSuccess, onError),
            onSuccess, onError);
    }

    @Override
    public void setCameraDisabled(
            boolean disabled,
            @NonNull Consumer<Void> onSuccess,
            @NonNull Consumer<Exception> onError) {
        queueOrExecute("setCameraDisabled",
            (disabled ? "Disable" : "Enable") + " camera",
            new Object[]{disabled},
            () -> delegate.setCameraDisabled(disabled, onSuccess, onError),
            onSuccess, onError);
    }

    @Override
    public void setStatusBarDisabled(
            boolean disabled,
            @NonNull Consumer<Void> onSuccess,
            @NonNull Consumer<Exception> onError) {
        queueOrExecute("setStatusBarDisabled",
            (disabled ? "Disable" : "Enable") + " status bar",
            new Object[]{disabled},
            () -> delegate.setStatusBarDisabled(disabled, onSuccess, onError),
            onSuccess, onError);
    }

    @Override
    public void setMaximumFailedPasswordsForWipe(
            int max,
            @NonNull Consumer<Void> onSuccess,
            @NonNull Consumer<Exception> onError) {
        queueOrExecute("setMaximumFailedPasswordsForWipe",
            "Set max failed passwords for wipe: " + max,
            new Object[]{max},
            () -> delegate.setMaximumFailedPasswordsForWipe(max, onSuccess, onError),
            onSuccess, onError);
    }

    @Override
    public void installExistingPackage(
            String packageName,
            @NonNull Consumer<Void> onSuccess,
            @NonNull Consumer<Exception> onError) {
        queueOrExecute("installExistingPackage",
            "Install existing package: " + packageName,
            new Object[]{packageName},
            () -> delegate.installExistingPackage(packageName, onSuccess, onError),
            onSuccess, onError);
    }

    @Override
    public void setUninstallBlocked(
            @NonNull String packageName,
            boolean uninstallBlocked,
            @NonNull Consumer<Void> onSuccess,
            @NonNull Consumer<Exception> onError) {
        queueOrExecute("setUninstallBlocked",
            (uninstallBlocked ? "Block" : "Allow") + " uninstall: " + packageName,
            new Object[]{packageName, uninstallBlocked},
            () -> delegate.setUninstallBlocked(packageName, uninstallBlocked, onSuccess, onError),
            onSuccess, onError);
    }

    @Override
    public void setSecureSetting(
            String setting,
            String value,
            @NonNull Consumer<Void> onSuccess,
            @NonNull Consumer<Exception> onError) {
        queueOrExecute("setSecureSetting",
            "Set secure setting: " + setting,
            new Object[]{setting, value},
            () -> delegate.setSecureSetting(setting, value, onSuccess, onError),
            onSuccess, onError);
    }

    @Override
    public void setGlobalSetting(
            String setting,
            String value,
            @NonNull Consumer<Void> onSuccess,
            @NonNull Consumer<Exception> onError) {
        queueOrExecute("setGlobalSetting",
            "Set global setting: " + setting,
            new Object[]{setting, value},
            () -> delegate.setGlobalSetting(setting, value, onSuccess, onError),
            onSuccess, onError);
    }

    @Override
    public void generateKeyPair(
            @NonNull String algorithm,
            @NonNull KeyGenParameterSpec keySpec,
            int idAttestationFlags,
            @NonNull Consumer<AttestedKeyPair> onSuccess,
            @NonNull Consumer<Exception> onError) {
        queueOrExecute("generateKeyPair",
            "Generate key pair: " + algorithm,
            new Object[]{algorithm, idAttestationFlags},
            () -> delegate.generateKeyPair(algorithm, keySpec, idAttestationFlags, onSuccess, onError),
            onSuccess, onError);
    }

    @Override
    public void removeKeyPair(
            @NonNull String alias,
            @NonNull Consumer<Boolean> onSuccess,
            @NonNull Consumer<Exception> onError) {
        queueOrExecute("removeKeyPair",
            "Remove key pair: " + alias,
            new Object[]{alias},
            () -> delegate.removeKeyPair(alias, onSuccess, onError),
            onSuccess, onError);
    }

    @Override
    public void grantKeyPairToApp(
            @NonNull String alias,
            @NonNull String packageName,
            @NonNull Consumer<Boolean> onSuccess,
            @NonNull Consumer<Exception> onError) {
        queueOrExecute("grantKeyPairToApp",
            "Grant key pair " + alias + " to " + packageName,
            new Object[]{alias, packageName},
            () -> delegate.grantKeyPairToApp(alias, packageName, onSuccess, onError),
            onSuccess, onError);
    }

    @Override
    public void revokeKeyPairFromApp(
            @NonNull String alias,
            @NonNull String packageName,
            @NonNull Consumer<Boolean> onSuccess,
            @NonNull Consumer<Exception> onError) {
        queueOrExecute("revokeKeyPairFromApp",
            "Revoke key pair " + alias + " from " + packageName,
            new Object[]{alias, packageName},
            () -> delegate.revokeKeyPairFromApp(alias, packageName, onSuccess, onError),
            onSuccess, onError);
    }

    @Override
    public void setDelegatedScopes(
            @NonNull String delegatePackage,
            @NonNull List<String> scopes,
            @NonNull Consumer<Void> onSuccess,
            @NonNull Consumer<Exception> onError) {
        queueOrExecute("setDelegatedScopes",
            "Set delegated scopes for " + delegatePackage,
            new Object[]{delegatePackage, scopes},
            () -> delegate.setDelegatedScopes(delegatePackage, scopes, onSuccess, onError),
            onSuccess, onError);
    }

    @Override
    public void setMeteredDataDisabledPackages(
            @NonNull List<String> packageNames,
            @NonNull Consumer<List<String>> onSuccess,
            @NonNull Consumer<Exception> onError) {
        queueOrExecute("setMeteredDataDisabledPackages",
            "Set metered data disabled packages",
            new Object[]{packageNames},
            () -> delegate.setMeteredDataDisabledPackages(packageNames, onSuccess, onError),
            onSuccess, onError);
    }
}
