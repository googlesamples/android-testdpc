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
import android.content.RestrictionEntry;
import android.os.Build;
import android.os.Build.VERSION_CODES;
import android.os.Bundle;
import android.util.Log;
import java.util.Arrays;
import java.util.List;

/**
 * Ported from {@link android.content.RestrictionsManager}.
 */
public class RestrictionManagerCompat {
    private static final String TAG = "RestrictionManager";

    /**
     * Converts a list of restrictions to the corresponding bundle, using the following mapping:
     * <table>
     *     <tr><th>RestrictionEntry</th><th>Bundle</th></tr>
     *     <tr><td>{@link RestrictionEntry#TYPE_BOOLEAN}</td><td>{@link Bundle#putBoolean}</td></tr>
     *     <tr><td>{@link RestrictionEntry#TYPE_CHOICE},
     *     {@link RestrictionEntry#TYPE_MULTI_SELECT}</td>
     *     <td>{@link Bundle#putStringArray}</td></tr>
     *     <tr><td>{@link RestrictionEntry#TYPE_INTEGER}</td><td>{@link Bundle#putInt}</td></tr>
     *     <tr><td>{@link RestrictionEntry#TYPE_STRING}</td><td>{@link Bundle#putString}</td></tr>
     *     <tr><td>{@link RestrictionEntry#TYPE_BUNDLE}</td><td>{@link Bundle#putBundle}</td></tr>
     *     <tr><td>{@link RestrictionEntry#TYPE_BUNDLE_ARRAY}</td>
     *     <td>{@link Bundle#putParcelableArray}</td></tr>
     * </table>
     * TYPE_BUNDLE and TYPE_BUNDLE_ARRAY are supported from api level 23 onwards.
     * @param entries list of restrictions
     */
    public static Bundle convertRestrictionsToBundle(List<RestrictionEntry> entries) {
        final Bundle bundle = new Bundle();
        for (RestrictionEntry entry : entries) {
            addRestrictionToBundle(bundle, entry);
        }
        return bundle;
    }

    private static Bundle addRestrictionToBundle(Bundle bundle, RestrictionEntry entry) {
        switch (entry.getType()) {
            case RestrictionEntry.TYPE_BOOLEAN:
                bundle.putBoolean(entry.getKey(), entry.getSelectedState());
                break;
            case RestrictionEntry.TYPE_CHOICE:
            case RestrictionEntry.TYPE_MULTI_SELECT:
                bundle.putStringArray(entry.getKey(), entry.getAllSelectedStrings());
                break;
            case RestrictionEntry.TYPE_INTEGER:
                bundle.putInt(entry.getKey(), entry.getIntValue());
                break;
            case RestrictionEntry.TYPE_STRING:
            case RestrictionEntry.TYPE_NULL:
                bundle.putString(entry.getKey(), entry.getSelectedString());
                break;
            case RestrictionEntry.TYPE_BUNDLE:
                addBundleRestrictionToBundle(bundle, entry);
                break;
            case RestrictionEntry.TYPE_BUNDLE_ARRAY:
                addBundleArrayRestrictionToBundle(bundle, entry);
                break;
            default:
                throw new IllegalArgumentException(
                        "Unsupported restrictionEntry type: " + entry.getType());
        }
        return bundle;
    }

    @TargetApi(VERSION_CODES.M)
    private static void addBundleRestrictionToBundle(Bundle bundle, RestrictionEntry entry) {
        if (Build.VERSION.SDK_INT >= VERSION_CODES.M) {
            RestrictionEntry[] restrictions = entry.getRestrictions();
            Bundle childBundle = convertRestrictionsToBundle(Arrays.asList(restrictions));
            bundle.putBundle(entry.getKey(), childBundle);
        } else {
            Log.w(TAG, "addBundleRestrictionToBundle is called in pre-M");
        }
    }

    @TargetApi(VERSION_CODES.M)
    private static void addBundleArrayRestrictionToBundle(Bundle bundle, RestrictionEntry entry) {
        if (Build.VERSION.SDK_INT >= VERSION_CODES.M) {
            RestrictionEntry[] bundleRestrictionArray = entry.getRestrictions();
            Bundle[] bundleArray = new Bundle[bundleRestrictionArray.length];
            for (int i = 0; i < bundleRestrictionArray.length; i++) {
                RestrictionEntry[] bundleRestrictions =
                        bundleRestrictionArray[i].getRestrictions();
                if (bundleRestrictions == null) {
                    // Non-bundle entry found in bundle array.
                    Log.w(TAG, "addRestrictionToBundle: " +
                            "Non-bundle entry found in bundle array");
                    bundleArray[i] = new Bundle();
                } else {
                    bundleArray[i] = convertRestrictionsToBundle(Arrays.asList(
                            bundleRestrictions));
                }
            }
            bundle.putParcelableArray(entry.getKey(), bundleArray);
        } else {
            Log.w(TAG, "addBundleArrayRestrictionToBundle is called in pre-M");
        }
    }
}
