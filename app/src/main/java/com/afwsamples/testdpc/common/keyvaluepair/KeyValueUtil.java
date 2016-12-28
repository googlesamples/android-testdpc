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

package com.afwsamples.testdpc.common.keyvaluepair;


import android.annotation.TargetApi;
import android.content.RestrictionEntry;
import android.os.Build;

import java.util.ArrayList;

/**
 * KeyValue dialogs utility functions.
 */

public class KeyValueUtil {

    public static int getTypeIndexFromRestrictionType(int restrictionType) {
        switch (restrictionType) {
            case RestrictionEntry.TYPE_BOOLEAN:
                return KeyValuePairDialogFragment.DialogType.BOOL_TYPE;
            case RestrictionEntry.TYPE_INTEGER:
                return KeyValuePairDialogFragment.DialogType.INT_TYPE;
            case RestrictionEntry.TYPE_STRING:
                return KeyValuePairDialogFragment.DialogType.STRING_TYPE;
            case RestrictionEntry.TYPE_MULTI_SELECT:
                return KeyValuePairDialogFragment.DialogType.STRING_ARRAY_TYPE;
            case RestrictionEntry.TYPE_CHOICE:
                return KeyValuePairDialogFragment.DialogType.CHOICE_TYPE;
            case RestrictionEntry.TYPE_BUNDLE:
                return KeyValuePairDialogFragment.DialogType.BUNDLE_TYPE;
            case RestrictionEntry.TYPE_BUNDLE_ARRAY:
                return KeyValuePairDialogFragment.DialogType.BUNDLE_ARRAY_TYPE;
            default:
                throw new AssertionError("Unknown restriction type");
        }
    }

    @TargetApi(Build.VERSION_CODES.M)
    public static RestrictionEntry[] getRestrictionEntries(RestrictionEntry restrictionEntry) {
        return restrictionEntry.getRestrictions();
    }

    @TargetApi(Build.VERSION_CODES.M)
    public static void setRestrictionEntries(RestrictionEntry restrictionEntry,
                                       RestrictionEntry[] restrictions) {
        restrictionEntry.setRestrictions(restrictions);
    }

    @TargetApi(Build.VERSION_CODES.M)
    private static RestrictionEntry cloneBundleRestriction(RestrictionEntry oldEntry) {
        RestrictionEntry newEntry = null;
        if (oldEntry != null) {
            RestrictionEntry[] newRestrictions = null;
            RestrictionEntry[] oldRestrictions = oldEntry.getRestrictions();
            if (oldRestrictions != null && oldRestrictions.length > 0) {
                newRestrictions = new RestrictionEntry[oldRestrictions.length];
                for (int i=0; i<oldRestrictions.length; i++) {
                    newRestrictions[i] = cloneRestriction(oldRestrictions[i]);
                }
            }
            String key = oldEntry.getKey();
            newEntry = RestrictionEntry.createBundleEntry(key, newRestrictions);
        }
        return newEntry;
    }

    @TargetApi(Build.VERSION_CODES.M)
    private static RestrictionEntry cloneBundleArrayRestriction(RestrictionEntry oldEntry) {
        RestrictionEntry newEntry = null;
        if (oldEntry != null) {
            RestrictionEntry[] newRestrictions = null;
            RestrictionEntry[] oldRestrictions = oldEntry.getRestrictions();
            if (oldRestrictions != null && oldRestrictions.length > 0) {
                newRestrictions = new RestrictionEntry[oldRestrictions.length];
                for (int i=0; i<oldRestrictions.length; i++) {
                    newRestrictions[i] = cloneRestriction(oldRestrictions[i]);
                }
            }
            String key = oldEntry.getKey();
            newEntry = RestrictionEntry.createBundleArrayEntry(key, newRestrictions);
        }
        return newEntry;
    }

    public static RestrictionEntry cloneRestriction(RestrictionEntry oldEntry) {
        RestrictionEntry newEntry = null;
        if (oldEntry != null) {
            String key = oldEntry.getKey();
            switch (oldEntry.getType()) {
                case RestrictionEntry.TYPE_BOOLEAN:
                    newEntry = new RestrictionEntry(key,
                            oldEntry.getSelectedState());
                    break;
                case RestrictionEntry.TYPE_INTEGER:
                    newEntry = new RestrictionEntry(key,
                            oldEntry.getIntValue());
                    break;
                case RestrictionEntry.TYPE_STRING:
                    newEntry = new RestrictionEntry(RestrictionEntry.TYPE_STRING,key);
                    newEntry.setSelectedString(oldEntry.getSelectedString());
                    break;
                case RestrictionEntry.TYPE_MULTI_SELECT:
                    newEntry = new RestrictionEntry(key,
                            oldEntry.getAllSelectedStrings());
                    break;
                case RestrictionEntry.TYPE_CHOICE:
                    newEntry = new RestrictionEntry(RestrictionEntry.TYPE_CHOICE,key);
                    newEntry.setSelectedString(oldEntry.getSelectedString());
                    newEntry.setChoiceEntries(oldEntry.getChoiceEntries());
                    newEntry.setChoiceValues(oldEntry.getChoiceValues());
                    break;
                case RestrictionEntry.TYPE_BUNDLE:
                    newEntry = cloneBundleRestriction(oldEntry);
                    break;
                case RestrictionEntry.TYPE_BUNDLE_ARRAY:
                    newEntry = cloneBundleArrayRestriction(oldEntry);
                    break;
                default:
                    throw new AssertionError("Unknown restriction type");
            }

            if (newEntry != null) {
                newEntry.setTitle(oldEntry.getTitle());
                newEntry.setDescription(oldEntry.getDescription());
            }
        }
        return newEntry;
    }

    @TargetApi(Build.VERSION_CODES.M)
    public static RestrictionEntry createBundleRestriction(String key,
            RestrictionEntry[] restrictions) {
        return RestrictionEntry.createBundleEntry(key, restrictions);
    }

    @TargetApi(Build.VERSION_CODES.M)
    public static RestrictionEntry createBundleArrayRestriction(String key,
            RestrictionEntry[] restrictions) {
        return RestrictionEntry.createBundleArrayEntry(key, restrictions);
    }

    public static ArrayList<Object> cloneRestrictionsList(
            ArrayList<Object> originalList) {
        ArrayList<Object> newList = null;
        if (originalList != null) {
            newList = new ArrayList<>();
            for (Object entry : originalList) {
                newList.add(KeyValueUtil.cloneRestriction((RestrictionEntry) entry));
            }
        }
        return newList;
    }
}
