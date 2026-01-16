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

import android.content.Context;
import android.util.Log;
import com.afwsamples.testdpc.DevicePolicyManagerGateway;
import com.afwsamples.testdpc.DevicePolicyManagerGatewayImpl;

/**
 * Executes pending DPM actions when their delay timers expire.
 */
public class ActionExecutor {
    private static final String TAG = "ActionExecutor";

    private final Context context;
    private final DevicePolicyManagerGateway gateway;
    private final DelayManager delayManager;

    public ActionExecutor(Context context) {
        this.context = context;
        this.gateway = new DevicePolicyManagerGatewayImpl(context);
        this.delayManager = DelayManager.getInstance(context);
    }

    /**
     * Execute a single pending change.
     * @return true if execution succeeded, false otherwise
     */
    public boolean execute(PendingChange change) {
        Log.i(TAG, "Executing action: " + change.actionType + " - " + change.description);

        try {
            Object[] params = DPMAction.deserialize(change.actionData);

            switch (change.actionType) {
                case "disableDelay":
                    delayManager.disableDelayDirect();
                    break;

                case "setCameraDisabled":
                    gateway.setCameraDisabled(
                        DPMAction.getBoolean(params, 0),
                        v -> {}, e -> {});
                    break;

                case "setUserRestriction":
                    gateway.setUserRestriction(
                        DPMAction.getString(params, 0),
                        DPMAction.getBoolean(params, 1));
                    break;

                case "setNetworkLoggingEnabled":
                    gateway.setNetworkLoggingEnabled(DPMAction.getBoolean(params, 0));
                    break;

                case "setSecurityLoggingEnabled":
                    gateway.setSecurityLoggingEnabled(
                        DPMAction.getBoolean(params, 0),
                        v -> {}, e -> {});
                    break;

                case "setLocationEnabled":
                    gateway.setLocationEnabled(
                        DPMAction.getBoolean(params, 0),
                        v -> {}, e -> {});
                    break;

                case "setKeyguardDisabled":
                    gateway.setKeyguardDisabled(
                        DPMAction.getBoolean(params, 0),
                        v -> {}, e -> {});
                    break;

                case "setStatusBarDisabled":
                    gateway.setStatusBarDisabled(
                        DPMAction.getBoolean(params, 0),
                        v -> {}, e -> {});
                    break;

                case "setUsbDataSignalingEnabled":
                    gateway.setUsbDataSignalingEnabled(DPMAction.getBoolean(params, 0));
                    break;

                case "lockNow":
                    gateway.lockNow(v -> {}, e -> {});
                    break;

                case "reboot":
                    gateway.reboot(v -> {}, e -> {});
                    break;

                case "setPasswordQuality":
                    gateway.setPasswordQuality(
                        DPMAction.getInt(params, 0),
                        v -> {}, e -> {});
                    break;

                case "setRequiredPasswordComplexity":
                    gateway.setRequiredPasswordComplexity(
                        DPMAction.getInt(params, 0),
                        v -> {}, e -> {});
                    break;

                case "setLockTaskFeatures":
                    gateway.setLockTaskFeatures(
                        DPMAction.getInt(params, 0),
                        v -> {}, e -> {});
                    break;

                case "setKeyguardDisabledFeatures":
                    gateway.setKeyguardDisabledFeatures(
                        DPMAction.getInt(params, 0),
                        v -> {}, e -> {});
                    break;

                case "setMaximumFailedPasswordsForWipe":
                    gateway.setMaximumFailedPasswordsForWipe(
                        DPMAction.getInt(params, 0),
                        v -> {}, e -> {});
                    break;

                case "setApplicationHidden":
                    gateway.setApplicationHidden(
                        DPMAction.getString(params, 0),
                        DPMAction.getBoolean(params, 1),
                        v -> {}, e -> {});
                    break;

                case "setUninstallBlocked":
                    gateway.setUninstallBlocked(
                        DPMAction.getString(params, 0),
                        DPMAction.getBoolean(params, 1),
                        v -> {}, e -> {});
                    break;

                case "enableSystemApp":
                    gateway.enableSystemApp(
                        DPMAction.getString(params, 0),
                        v -> {}, e -> {});
                    break;

                case "installExistingPackage":
                    gateway.installExistingPackage(
                        DPMAction.getString(params, 0),
                        v -> {}, e -> {});
                    break;

                case "setOrganizationName":
                    gateway.setOrganizationName(
                        DPMAction.getString(params, 0),
                        v -> {}, e -> {});
                    break;

                case "setDeviceOwnerLockScreenInfo":
                    gateway.setDeviceOwnerLockScreenInfo(
                        DPMAction.getString(params, 0),
                        v -> {}, e -> {});
                    break;

                case "setSecureSetting":
                    gateway.setSecureSetting(
                        DPMAction.getString(params, 0),
                        DPMAction.getString(params, 1),
                        v -> {}, e -> {});
                    break;

                case "setGlobalSetting":
                    gateway.setGlobalSetting(
                        DPMAction.getString(params, 0),
                        DPMAction.getString(params, 1),
                        v -> {}, e -> {});
                    break;

                case "setLockTaskPackages":
                    gateway.setLockTaskPackages(
                        DPMAction.getStringArray(params, 0),
                        v -> {}, e -> {});
                    break;

                case "setPackagesSuspended":
                    gateway.setPackagesSuspended(
                        DPMAction.getStringArray(params, 0),
                        DPMAction.getBoolean(params, 1),
                        v -> {}, e -> {});
                    break;

                case "setPermittedInputMethods":
                    gateway.setPermittedInputMethods(DPMAction.getStringList(params, 0));
                    break;

                case "setAffiliationIds":
                    gateway.setAffiliationIds(DPMAction.getStringSet(params, 0));
                    break;

                case "setLogoutEnabled":
                    gateway.setLogoutEnabled(
                        DPMAction.getBoolean(params, 0),
                        v -> {}, e -> {});
                    break;

                case "setPersonalAppsSuspended":
                    gateway.setPersonalAppsSuspended(
                        DPMAction.getBoolean(params, 0),
                        v -> {}, e -> {});
                    break;

                case "setPreferentialNetworkServiceEnabled":
                    gateway.setPreferentialNetworkServiceEnabled(
                        DPMAction.getBoolean(params, 0),
                        v -> {}, e -> {});
                    break;

                case "removeActiveAdmin":
                    gateway.removeActiveAdmin(v -> {}, e -> {});
                    break;

                case "clearDeviceOwnerApp":
                    gateway.clearDeviceOwnerApp(v -> {}, e -> {});
                    break;

                case "clearProfileOwner":
                    gateway.clearProfileOwner(v -> {}, e -> {});
                    break;

                case "wipeData":
                    gateway.wipeData(DPMAction.getInt(params, 0), v -> {}, e -> {});
                    break;

                case "wipeDevice":
                    gateway.wipeDevice(DPMAction.getInt(params, 0), v -> {}, e -> {});
                    break;

                default:
                    Log.w(TAG, "Unknown action type: " + change.actionType);
                    return false;
            }

            Log.i(TAG, "Successfully executed: " + change.description);
            return true;

        } catch (Exception e) {
            Log.e(TAG, "Failed to execute action: " + change.description, e);
            return false;
        }
    }
}
