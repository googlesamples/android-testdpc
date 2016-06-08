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

package com.afwsamples.testdpc.profilepolicy.apprestrictions;

import android.app.admin.DevicePolicyManager;
import android.content.ComponentName;
import android.content.Context;
import android.content.pm.PackageInfo;
import android.content.pm.PackageManager;
import android.content.pm.PackageManager.NameNotFoundException;
import android.content.pm.Signature;
import android.os.Bundle;
import android.os.Handler;
import android.os.Message;
import android.os.RemoteException;
import android.preference.PreferenceManager;
import android.util.Log;

import java.util.ArrayList;
import java.util.HashSet;
import java.util.List;
import java.util.Set;

/**
 * Handles the message passed from another package to set the application restrictions.
 *
 * The package name must be provided, along with the application restrictions bundle to set.
 * To clear the application restrictions, an empty bundle should be passed.
 */
public class AppRestrictionsProxyHandler extends Handler {

    private static final String TAG = "AppRestrictionsProxy";
    private static final int MSG_SET_APPLICATION_RESTRICTIONS = 1;
    private static final int MSG_CAN_SET_APPLICATION_RESTRICTIONS = 2;
    private static final int MSG_GET_APPLICATION_RESTRICTIONS = 3;

    private static final String APPLICATION_RESTRICTIONS_MANAGING_PACKAGE_SIGNATURES_KEY =
            "application_restrictions_managing_package_signatures";
    private static final String APPLICATION_RESTRICTIONS_MANAGING_PACKAGE_KEY =
            "application_restrictions_managing_package";

    public static final String KEY_APPLICATION_RESTRICTIONS = "applicationRestrictions";
    public static final String KEY_PACKAGE_NAME = "packageName";
    public static final String KEY_CAN_SET_APPLICATION_RESTRICTIONS
            = "canSetApplicationRestrictions";

    private final Context mContext;
    private final ComponentName mAdmin;

    public AppRestrictionsProxyHandler(Context context, ComponentName admin) {
        mContext = context;
        mAdmin = admin;
    }

    @Override
    public void handleMessage(Message msg) {
        switch (msg.what) {
            case MSG_SET_APPLICATION_RESTRICTIONS: {
                ensureCallerSignature(msg.sendingUid);
                String packageName = msg.getData().getString(KEY_PACKAGE_NAME);
                Bundle appRestrictions = msg.getData().getBundle(KEY_APPLICATION_RESTRICTIONS);
                setApplicationRestrictions(packageName, appRestrictions);
                break;
            }
            case MSG_CAN_SET_APPLICATION_RESTRICTIONS: {
                String callingPackage = mContext.getPackageManager().getNameForUid(msg.sendingUid);
                String managingPackage = getApplicationRestrictionsManagingPackage(mContext);
                Bundle responseBundle = new Bundle();
                responseBundle.putBoolean(KEY_CAN_SET_APPLICATION_RESTRICTIONS,
                        callingPackage != null && callingPackage.equals(managingPackage));
                Message response = Message.obtain();
                response.setData(responseBundle);
                try {
                    msg.replyTo.send(response);
                } catch (RemoteException e) {
                    Log.e(TAG, "Unable to respond to canSetApplicationRestrictions.", e);
                }
                break;
            }
            case MSG_GET_APPLICATION_RESTRICTIONS: {
                ensureCallerSignature(msg.sendingUid);
                String packageName = msg.getData().getString(KEY_PACKAGE_NAME);
                Bundle appRestrictions = getApplicationRestrictions(packageName);
                Bundle responseBundle = new Bundle();
                responseBundle.putBundle(KEY_APPLICATION_RESTRICTIONS, appRestrictions);
                Message response = Message.obtain();
                response.setData(responseBundle);
                try {
                    msg.replyTo.send(response);
                } catch (RemoteException e) {
                    Log.e(TAG, "Unable to respond to getApplicationRestrictions.", e);
                }
                break;
            }
            default:
                throw new IllegalArgumentException("Unknown 'what': " + msg.what);
        }
    }

    /**
     * Called by a profile owner or device owner to grant permission to a package to manage
     * application restrictions for the calling user via the {@link AppRestrictionsProxy}.
     *
     * This permission is persistent until it is later cleared by calling this method with a
     * {@code null} value.
     *
     * The supplied application restriction managing package must be installed when calling this
     * API, otherwise an {@link IllegalArgumentException} will be thrown.
     */
    public static void setApplicationRestrictionsManagingPackage(Context context,
            String packageName) {
        if (packageName == null) {
            PreferenceManager.getDefaultSharedPreferences(context).edit()
                    .putStringSet(APPLICATION_RESTRICTIONS_MANAGING_PACKAGE_SIGNATURES_KEY, null)
                    .putString(APPLICATION_RESTRICTIONS_MANAGING_PACKAGE_KEY, null)
                    .apply();
            return;
        }
        Signature[] signatures;
        try {
            PackageManager packageManager = context.getPackageManager();
            PackageInfo packageInfo =
                    packageManager.getPackageInfo(packageName, PackageManager.GET_SIGNATURES);
            if (packageInfo == null) {
                throw new IllegalArgumentException("Package info could not be retrieved for " +
                        "package " + packageName + ".");
            }
            signatures = packageInfo.signatures;
            if (signatures == null) {
                throw new IllegalArgumentException("Package info did not contain signatures " +
                        "for package " + packageName + ".");
            }
        } catch (NameNotFoundException e) {
            throw new IllegalArgumentException("Cannot set " + packageName + " as application " +
                    "restriction managing package as it is not installed.", e);
        }
        Set<String> signatureSet = new HashSet<>();
        for (Signature signature : signatures) {
            signatureSet.add(signature.toCharsString());
        }

        PreferenceManager.getDefaultSharedPreferences(context).edit()
                .putStringSet(APPLICATION_RESTRICTIONS_MANAGING_PACKAGE_SIGNATURES_KEY,
                        signatureSet)
                .putString(APPLICATION_RESTRICTIONS_MANAGING_PACKAGE_KEY, packageName)
                .apply();
    }

    /**
     * Called by a profile owner or device owner to retrieve the application restrictions managing
     * package for the current user, or {@code null} if none is set.
     */
    public static String getApplicationRestrictionsManagingPackage(Context context){
        return PreferenceManager.getDefaultSharedPreferences(context)
                .getString(APPLICATION_RESTRICTIONS_MANAGING_PACKAGE_KEY, null);
    }

    private void setApplicationRestrictions(String packageName, Bundle appRestrictions){
        if (packageName == null) {
            throw new IllegalArgumentException("packageName cannot be null.");
        }
        if (appRestrictions == null) {
            throw new IllegalArgumentException("applicationRestrictions bundle " +
                    "cannot be null.");
        }
        Log.d(TAG, "Setting application restrictions for package " + packageName);
        DevicePolicyManager devicePolicyManager = (DevicePolicyManager)
                mContext.getSystemService(Context.DEVICE_POLICY_SERVICE);
        devicePolicyManager.setApplicationRestrictions(mAdmin, packageName,
                appRestrictions);
    }

    private Bundle getApplicationRestrictions(String packageName){
        if (packageName == null) {
            throw new IllegalArgumentException("packageName cannot be null.");
        }
        DevicePolicyManager devicePolicyManager = (DevicePolicyManager)
                mContext.getSystemService(Context.DEVICE_POLICY_SERVICE);
        return devicePolicyManager.getApplicationRestrictions(mAdmin, packageName);
    }

    /**
     * Checks that the message sent through the bound service was sent by the same package as
     * declared in {@link #setApplicationRestrictionsManagingPackage(Context, String)}, and
     * that its signature has not changed since it was set.
     *
     * @param callerUid the UID of the caller
     *
     * @throws SecurityException if the DPC hasn't given permission to the caller to manage
     * application restrictions, or if the calling package's signature has changed since it was
     * set.
     */
    private void ensureCallerSignature(int callerUid) {
        String appRestrictionsManagingPackage = getApplicationRestrictionsManagingPackage(mContext);
        if (appRestrictionsManagingPackage == null) {
            Log.e(TAG, "There is no app restrictions managing package");
            return;
        }
        PackageManager packageManager = mContext.getPackageManager();
        String callingPackageName = packageManager.getNameForUid(callerUid);
        if (!appRestrictionsManagingPackage.equals(callingPackageName)) {
            Log.e(TAG, "Caller is not app restrictions managing package");
            return;
        }

        Set<String> storedSignatures = PreferenceManager.getDefaultSharedPreferences(mContext)
                .getStringSet(APPLICATION_RESTRICTIONS_MANAGING_PACKAGE_SIGNATURES_KEY, null);
        if (storedSignatures == null) {
            throw new IllegalStateException(
                    "App restrictions managing package signatures have not been stored.");
        }
        Signature[] callingPackageSignatures;
        try {
            PackageInfo packageInfo = packageManager
                    .getPackageInfo(callingPackageName, PackageManager.GET_SIGNATURES);
            if (packageInfo == null) {
                throw new IllegalArgumentException("Package info could not be retrieved for " +
                        "package " + callingPackageName + ".");
            }
            callingPackageSignatures = packageInfo.signatures;
            if (callingPackageSignatures == null) {
                throw new IllegalArgumentException("Package info did not contain signatures " +
                        "for package " + callingPackageName + ".");
            }
        } catch (NameNotFoundException e) {
            throw new SecurityException(e);
        }
        List<Signature> expectedSignatures = new ArrayList<>(storedSignatures.size());
        for (String signatureString : storedSignatures) {
            expectedSignatures.add(new Signature(signatureString));
        }
        for (Signature callingSignature : callingPackageSignatures) {
            for (Signature expectedSignature : expectedSignatures) {
                if (expectedSignature.equals(callingSignature)) {
                    return;
                }
            }
        }
        throw new SecurityException("Calling package signature doesn't match");
    }
}