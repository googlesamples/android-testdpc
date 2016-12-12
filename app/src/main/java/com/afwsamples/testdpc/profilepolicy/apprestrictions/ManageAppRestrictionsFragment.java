/*
 * Copyright (C) 2015 The Android Open Source Project
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

import android.annotation.TargetApi;
import android.app.Activity;
import android.app.admin.DevicePolicyManager;
import android.content.Context;
import android.content.Intent;
import android.content.RestrictionEntry;
import android.content.RestrictionsManager;
import android.content.pm.ApplicationInfo;
import android.os.Build;
import android.os.Bundle;
import android.os.Parcelable;
import android.text.TextUtils;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.BaseAdapter;
import android.widget.Toast;

import com.afwsamples.testdpc.DeviceAdminReceiver;
import com.afwsamples.testdpc.R;
import com.afwsamples.testdpc.common.EditDeleteArrayAdapter;
import com.afwsamples.testdpc.common.ManageAppFragment;
import com.afwsamples.testdpc.common.RestrictionManagerCompat;
import com.afwsamples.testdpc.common.keyvaluepair.KeyValuePairDialogFragment;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;
import java.util.Set;

import static com.afwsamples.testdpc.common.keyvaluepair.KeyValuePairDialogFragment.RESULT_VALUE;

/**
 * This fragment shows all installed apps and allows viewing and editing application restrictions
 * for those apps. It also allows loading the default app restrictions for each of those apps.
 */
public class ManageAppRestrictionsFragment extends ManageAppFragment
        implements EditDeleteArrayAdapter.OnEditButtonClickListener<RestrictionEntry> {
    private List<RestrictionEntry> mRestrictionEntries = new ArrayList<>();
    private List<RestrictionEntry> mLastRestrictionEntries;
    private DevicePolicyManager mDevicePolicyManager;
    private RestrictionsManager mRestrictionsManager;
    private EditDeleteArrayAdapter<RestrictionEntry> mAppRestrictionsArrayAdapter;
    private RestrictionEntry mEditingRestrictionEntry;

    private static final int RESULT_CODE_EDIT_DIALOG = 1;

    private static final int[] SUPPORTED_TYPES = {
            KeyValuePairDialogFragment.DialogType.BOOL_TYPE,
            KeyValuePairDialogFragment.DialogType.INT_TYPE,
            KeyValuePairDialogFragment.DialogType.STRING_TYPE,
            KeyValuePairDialogFragment.DialogType.STRING_ARRAY_TYPE,
            KeyValuePairDialogFragment.DialogType.BUNDLE_TYPE,
            KeyValuePairDialogFragment.DialogType.BUNDLE_ARRAY_TYPE,
            KeyValuePairDialogFragment.DialogType.CHOICE_TYPE
    };
    private static final int[] SUPPORTED_TYPES_PRE_M = {
            KeyValuePairDialogFragment.DialogType.BOOL_TYPE,
            KeyValuePairDialogFragment.DialogType.INT_TYPE,
            KeyValuePairDialogFragment.DialogType.STRING_TYPE,
            KeyValuePairDialogFragment.DialogType.STRING_ARRAY_TYPE,
    };

    public static ManageAppRestrictionsFragment newInstance() {
        ManageAppRestrictionsFragment fragment = new ManageAppRestrictionsFragment();
        fragment.setArguments(null);
        return fragment;
    }

    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setRetainInstance(true);
        mDevicePolicyManager = (DevicePolicyManager) getActivity().getSystemService(
                Context.DEVICE_POLICY_SERVICE);
        mRestrictionsManager = (RestrictionsManager) getActivity().getSystemService(
                Context.RESTRICTIONS_SERVICE);
    }

    @Override
    public void onActivityCreated(Bundle savedInstanceState) {
        super.onActivityCreated(savedInstanceState);
        getActivity().getActionBar().setTitle(R.string.manage_app_restrictions);
    }

    protected void loadAppRestrictionsList(RestrictionEntry[] restrictionEntries) {
        if (restrictionEntries != null) {
            mAppRestrictionsArrayAdapter.clear();
            mAppRestrictionsArrayAdapter.addAll(Arrays.asList(restrictionEntries));
        }
    }

    protected RestrictionEntry[] convertBundleToRestrictions(Bundle restrictionBundle, Bundle manifestRestrictionsBundle) {
        List<RestrictionEntry> restrictionEntries = new ArrayList<>();
        Set<String> keys = restrictionBundle.keySet();
        for (String key : keys) {
            Object value = restrictionBundle.get(key);
            if (value instanceof Boolean) {
                restrictionEntries.add(new RestrictionEntry(key, (boolean) value));
            } else if (value instanceof Integer) {
                restrictionEntries.add(new RestrictionEntry(key, (int) value));
            } else if (value instanceof String) {
                // DevicePolicyManager returns Choice restriction string
                // We need to find corresponding restriction in app manifest restrictions
                // and, if it has type Choice, use entries and values defined by app
                RestrictionEntry entry = null;
                Bundle manifestRestrictionData = null;
                if (manifestRestrictionsBundle != null) {
                    manifestRestrictionData = manifestRestrictionsBundle.getBundle(key);
                }
                if (manifestRestrictionData != null
                        && manifestRestrictionData.containsKey(RestrictionManagerCompat.CHOICE_SELECTED_VALUE)) {
                    String[] choiceEntries = manifestRestrictionData.getStringArray(RestrictionManagerCompat.CHOICE_ENTRIES);
                    String[] choiceValues = manifestRestrictionData.getStringArray(RestrictionManagerCompat.CHOICE_VALUES);
                    entry = new RestrictionEntry(RestrictionEntry.TYPE_CHOICE, key);
                    entry.setChoiceEntries(choiceEntries);
                    entry.setChoiceValues(choiceValues);
                } else {
                    entry = new RestrictionEntry(RestrictionEntry.TYPE_STRING, key);
                }
                entry.setSelectedString((String) value);
                restrictionEntries.add(entry);
            } else if (value instanceof String[]) {
                restrictionEntries.add(new RestrictionEntry(key, (String[]) value));
            } else if (value instanceof Bundle) {
                // We are using Bundle to store Choice restriction for UI
                if (((Bundle) value).containsKey(RestrictionManagerCompat.CHOICE_SELECTED_VALUE)) {
                    addChoiceEntryToRestrictions(restrictionEntries, key, (Bundle) value);
                } else {
                    Bundle manifestRestrictionData = null;
                    if (manifestRestrictionsBundle != null) {
                        manifestRestrictionData = manifestRestrictionsBundle.getBundle(key);
                    }
                    addBundleEntryToRestrictions(
                            restrictionEntries, key, (Bundle) value, manifestRestrictionData);
                }
            } else if (value instanceof Parcelable[]) {
                Parcelable[] manifestRestrictionData = null;
                if (manifestRestrictionsBundle != null) {
                    manifestRestrictionData = manifestRestrictionsBundle.getParcelableArray(key);
                }
                addBundleArrayToRestrictions(restrictionEntries, key, (Parcelable[]) value, manifestRestrictionData);
            }
        }
        return restrictionEntries.toArray(new RestrictionEntry[0]);
    }

    @TargetApi(Build.VERSION_CODES.M)
    private void addBundleEntryToRestrictions(List<RestrictionEntry> restrictionEntries,
            String key, Bundle value, Bundle manifestRestrictionsBundle) {
        restrictionEntries.add(RestrictionEntry.createBundleEntry(
                key, convertBundleToRestrictions(value, manifestRestrictionsBundle)));
    }

    @TargetApi(Build.VERSION_CODES.M)
    private void addBundleArrayToRestrictions(List<RestrictionEntry> restrictionEntries,
            String key, Parcelable[] value, Parcelable[] manifestRestrictionsBundleArray) {
        int length = value.length;
        RestrictionEntry[] entriesArray = new RestrictionEntry[length];
        for (int i = 0; i < entriesArray.length; ++i) {
            Parcelable manifestRestrictionsBundle = null;
            if (manifestRestrictionsBundleArray != null && manifestRestrictionsBundleArray.length > i) {
                manifestRestrictionsBundle = manifestRestrictionsBundleArray[i];
            }
            entriesArray[i] = RestrictionEntry.createBundleEntry(key,
                    convertBundleToRestrictions((Bundle) value[i], (Bundle) manifestRestrictionsBundle));
        }
        restrictionEntries.add(RestrictionEntry.createBundleArrayEntry(key, entriesArray));
    }

    private void addChoiceEntryToRestrictions(List<RestrictionEntry> restrictionEntries,
                                              String key, Bundle value) {
        RestrictionEntry choiceEntry = new RestrictionEntry(RestrictionEntry.TYPE_CHOICE, key);
        choiceEntry.setSelectedString(value.getString(RestrictionManagerCompat.CHOICE_SELECTED_VALUE));
        choiceEntry.setChoiceEntries(value.getStringArray(RestrictionManagerCompat.CHOICE_ENTRIES));
        choiceEntry.setChoiceValues(value.getStringArray(RestrictionManagerCompat.CHOICE_VALUES));
        restrictionEntries.add(choiceEntry);
    }

    protected void showToast(String msg) {
        Toast.makeText(getActivity(), msg, Toast.LENGTH_SHORT).show();
    }

    @Override
    public void onEditButtonClick(RestrictionEntry restrictionEntry) {
        showEditDialog(restrictionEntry);
    }

    private void showEditDialog(final RestrictionEntry restrictionEntry) {
        mEditingRestrictionEntry = restrictionEntry;
        int type = KeyValuePairDialogFragment.DialogType.BOOL_TYPE;
        Object value = null;
        String key = "";
        if (restrictionEntry != null) {
            key = restrictionEntry.getKey();
            type = getTypeIndexFromRestrictionType(restrictionEntry.getType());
            switch (restrictionEntry.getType()) {
                case RestrictionEntry.TYPE_BOOLEAN:
                    value = restrictionEntry.getSelectedState();
                    break;
                case RestrictionEntry.TYPE_INTEGER:
                    value = restrictionEntry.getIntValue();
                    break;
                case RestrictionEntry.TYPE_STRING:
                    value = restrictionEntry.getSelectedString();
                    // UI uses value to find restriction type.
                    // If string restrictions has null value, it will be displayed as boolean
                    // To avoid this we should set empty string as value
                    if (value == null) {
                        value = "";
                    }
                    break;
                case RestrictionEntry.TYPE_MULTI_SELECT:
                    value = restrictionEntry.getAllSelectedStrings();
                    break;
                case RestrictionEntry.TYPE_BUNDLE:
                    value = RestrictionManagerCompat.convertRestrictionsToBundle(Arrays.asList(
                            getRestrictionEntries(restrictionEntry)), true);
                    break;
                case RestrictionEntry.TYPE_BUNDLE_ARRAY:
                    RestrictionEntry[] restrictionEntries = getRestrictionEntries(restrictionEntry);
                    Bundle[] bundles = new Bundle[restrictionEntries.length];
                    for (int i = 0; i < restrictionEntries.length; i++) {
                        bundles[i] =
                                RestrictionManagerCompat.convertRestrictionsToBundle(Arrays.asList(
                                        getRestrictionEntries(restrictionEntries[i])), true);
                    }
                    value = bundles;
                    break;
                case RestrictionEntry.TYPE_CHOICE:
                    value = RestrictionManagerCompat.convertChoiceRestrictionToBundle(restrictionEntry);
                    break;
            }
        }
        int[] supportType = (Build.VERSION.SDK_INT < Build.VERSION_CODES.M)
                ? SUPPORTED_TYPES_PRE_M
                : SUPPORTED_TYPES;
        KeyValuePairDialogFragment dialogFragment =
                KeyValuePairDialogFragment.newInstance(type, true, key, value, supportType,
                        getCurrentAppName());
        dialogFragment.setTargetFragment(this, RESULT_CODE_EDIT_DIALOG);
        dialogFragment.show(getFragmentManager(), "dialog");
    }

    private int getTypeIndexFromRestrictionType(int restrictionType) {
        switch (restrictionType) {
            case RestrictionEntry.TYPE_BOOLEAN:
                return KeyValuePairDialogFragment.DialogType.BOOL_TYPE;
            case RestrictionEntry.TYPE_INTEGER:
                return KeyValuePairDialogFragment.DialogType.INT_TYPE;
            case RestrictionEntry.TYPE_STRING:
                return KeyValuePairDialogFragment.DialogType.STRING_TYPE;
            case RestrictionEntry.TYPE_MULTI_SELECT:
                return KeyValuePairDialogFragment.DialogType.STRING_ARRAY_TYPE;
            case RestrictionEntry.TYPE_BUNDLE:
                return KeyValuePairDialogFragment.DialogType.BUNDLE_TYPE;
            case RestrictionEntry.TYPE_BUNDLE_ARRAY:
                return KeyValuePairDialogFragment.DialogType.BUNDLE_ARRAY_TYPE;
            case RestrictionEntry.TYPE_CHOICE:
                return KeyValuePairDialogFragment.DialogType.CHOICE_TYPE;
            default:
                throw new AssertionError("Unknown restriction type");
        }
    }

    @Override
    public void onActivityResult(int requestCode, int resultCode, Intent result) {
        if (resultCode != Activity.RESULT_OK) {
            return;
        }
        RestrictionEntry newRestrictionEntry;
        switch (requestCode) {
            case RESULT_CODE_EDIT_DIALOG:
                int type = result.getIntExtra(KeyValuePairDialogFragment.RESULT_TYPE, 0);
                String key = result.getStringExtra(KeyValuePairDialogFragment.RESULT_KEY);
                int editedRestrictionType = getRestrictionTypeFromDialogType(type);
                if (mEditingRestrictionEntry == null || mEditingRestrictionEntry.getType() != editedRestrictionType) {
                    newRestrictionEntry = new RestrictionEntry(getRestrictionTypeFromDialogType(type),
                            key);
                    updateRestrictionEntryFromResultIntent(newRestrictionEntry, result);
                    mAppRestrictionsArrayAdapter.remove(mEditingRestrictionEntry);
                    mAppRestrictionsArrayAdapter.add(newRestrictionEntry);
                } else {
                    updateRestrictionEntryFromResultIntent(mEditingRestrictionEntry, result);
                }
                mEditingRestrictionEntry = null;
                break;
        }
    }

    // TYPE_BUNDLE and TYPE_BUNDLE_ARRAY are only supported from M onward. It is blocked in the
    // UI side.
    @TargetApi(Build.VERSION_CODES.M)
    private void updateRestrictionEntryFromResultIntent(RestrictionEntry restrictionEntry,
            Intent intent) {
        switch (restrictionEntry.getType()) {
            case RestrictionEntry.TYPE_BOOLEAN:
                restrictionEntry.setSelectedState(intent.getBooleanExtra(RESULT_VALUE, false));
                break;
            case RestrictionEntry.TYPE_INTEGER:
                restrictionEntry.setIntValue(intent.getIntExtra(RESULT_VALUE, 0));
                break;
            case RestrictionEntry.TYPE_STRING:
                restrictionEntry.setSelectedString(intent.getStringExtra(RESULT_VALUE));
                break;
            case RestrictionEntry.TYPE_MULTI_SELECT:
                restrictionEntry.setAllSelectedStrings(intent.getStringArrayExtra(RESULT_VALUE));
                break;
            case RestrictionEntry.TYPE_BUNDLE: {
                Bundle bundle = intent.getBundleExtra(RESULT_VALUE);
                restrictionEntry.setRestrictions(convertBundleToRestrictions(bundle, null));
                break;
            }
            case RestrictionEntry.TYPE_BUNDLE_ARRAY: {
                Parcelable[] bundleArray = intent.getParcelableArrayExtra(RESULT_VALUE);
                RestrictionEntry[] restrictionEntryArray = new RestrictionEntry[bundleArray.length];
                for (int i = 0; i< bundleArray.length; i++) {
                    restrictionEntryArray[i] = RestrictionEntry.createBundleEntry(String.valueOf(i),
                            convertBundleToRestrictions((Bundle) bundleArray[i], null));
                }
                restrictionEntry.setRestrictions(restrictionEntryArray);
                break;
            }
            case RestrictionEntry.TYPE_CHOICE:
                restrictionEntry.setSelectedString(intent.getStringExtra(RESULT_VALUE));
                break;
        }
    }

    private int getRestrictionTypeFromDialogType(int typeIndex) {
        switch (typeIndex) {
            case KeyValuePairDialogFragment.DialogType.BOOL_TYPE:
                return RestrictionEntry.TYPE_BOOLEAN;
            case KeyValuePairDialogFragment.DialogType.INT_TYPE:
                return RestrictionEntry.TYPE_INTEGER;
            case KeyValuePairDialogFragment.DialogType.STRING_TYPE:
                return RestrictionEntry.TYPE_STRING;
            case KeyValuePairDialogFragment.DialogType.STRING_ARRAY_TYPE:
                return RestrictionEntry.TYPE_MULTI_SELECT;
            case KeyValuePairDialogFragment.DialogType.BUNDLE_TYPE:
                return RestrictionEntry.TYPE_BUNDLE;
            case KeyValuePairDialogFragment.DialogType.BUNDLE_ARRAY_TYPE:
                return RestrictionEntry.TYPE_BUNDLE_ARRAY;
            case KeyValuePairDialogFragment.DialogType.CHOICE_TYPE:
                return RestrictionEntry.TYPE_CHOICE;
            default:
                throw new AssertionError("Unknown type index");
        }
    }

    @Override
    protected void addNewRow() {
        showEditDialog(null);
    }

    @Override
    public View onCreateView(LayoutInflater layoutInflater, ViewGroup container,
            Bundle savedInstanceState) {
        View view = super.onCreateView(layoutInflater, container, savedInstanceState);
        View loadDefaultButton = view.findViewById(R.id.load_default_button);
        loadDefaultButton.setVisibility(View.VISIBLE);
        return view;
    }

    @Override
    protected BaseAdapter createListAdapter() {
        mAppRestrictionsArrayAdapter = new RestrictionEntryEditDeleteArrayAdapter(getActivity(),
                mRestrictionEntries, this, null);
        return mAppRestrictionsArrayAdapter;
    }

    @Override
    protected void onSpinnerItemSelected(ApplicationInfo appInfo) {
        String pkgName = appInfo.packageName;
        if (!TextUtils.isEmpty(pkgName)) {
            Bundle bundle = mDevicePolicyManager.getApplicationRestrictions(
                    DeviceAdminReceiver.getComponentName(getActivity()), pkgName);
            // We need restrictions from app manifest to get entries and values for Choice restrictions
            List<RestrictionEntry> manifestRestrictions = getManifestRestrictions(pkgName);
            Bundle manifestRestrictionsBundle = null;
            if (manifestRestrictions != null) {
                manifestRestrictionsBundle = RestrictionManagerCompat.convertRestrictionsToBundle(manifestRestrictions, true);
            }
            loadAppRestrictionsList(convertBundleToRestrictions(bundle, manifestRestrictionsBundle));
            mLastRestrictionEntries = new ArrayList<>(mRestrictionEntries);
        }
    }

    /**
     * Return the name of the application whose restrictions are currently displayed.
     */
    private String getCurrentAppName() {
        ApplicationInfo applicationInfo =
                (ApplicationInfo) mManagedAppsSpinner.getSelectedItem();
        return (String) getActivity().getPackageManager().getApplicationLabel(
                applicationInfo);
    }

    private void loadManifestAppRestrictions(String pkgName) {
        if (!TextUtils.isEmpty(pkgName)) {
            List<RestrictionEntry> manifestRestrictions = getManifestRestrictions(pkgName);
            if (manifestRestrictions != null) {
                loadAppRestrictionsList(manifestRestrictions.toArray(new RestrictionEntry[0]));
            }
        }
    }

    private List<RestrictionEntry> getManifestRestrictions(String pkgName) {
        List<RestrictionEntry> manifestRestrictions = null;
        if (!TextUtils.isEmpty(pkgName)) {
            try {
                manifestRestrictions = mRestrictionsManager.getManifestRestrictions(pkgName);
                convertTypeNullToString(manifestRestrictions);
            } catch (NullPointerException e) {
                // This means no default restrictions.
            }
        }

        return manifestRestrictions;
    }

    /**
     * TODO (b/23378519): Remove this method and add support for type null.
     */
    private void convertTypeNullToString(List<RestrictionEntry> restrictionEntries) {
        for (RestrictionEntry entry : restrictionEntries) {
            if (entry.getType() == RestrictionEntry.TYPE_NULL) {
                entry.setType(RestrictionEntry.TYPE_STRING);
            }
        }
    }

    @Override
    protected void saveConfig() {
        String pkgName =
                ((ApplicationInfo) mManagedAppsSpinner.getSelectedItem()).packageName;
        mDevicePolicyManager.setApplicationRestrictions(
                DeviceAdminReceiver.getComponentName(getActivity()), pkgName,
                RestrictionManagerCompat.convertRestrictionsToBundle(mRestrictionEntries, false));
        mLastRestrictionEntries = new ArrayList<>(mRestrictionEntries);
        showToast(getString(R.string.set_app_restrictions_success, pkgName));
    }

    @Override
    protected void resetConfig() {
        mAppRestrictionsArrayAdapter.clear();
        mAppRestrictionsArrayAdapter.addAll(mLastRestrictionEntries);
    }

    @Override
    protected void loadDefault() {
        loadManifestAppRestrictions(
                ((ApplicationInfo) mManagedAppsSpinner.getSelectedItem()).packageName);
    }

    @TargetApi(Build.VERSION_CODES.M)
    private RestrictionEntry[] getRestrictionEntries(RestrictionEntry restrictionEntry) {
        return restrictionEntry.getRestrictions();
    }

    public class RestrictionEntryEditDeleteArrayAdapter extends
            EditDeleteArrayAdapter<RestrictionEntry> {

        public RestrictionEntryEditDeleteArrayAdapter(Context context,
                List<RestrictionEntry> entries,
                OnEditButtonClickListener onEditButtonClickListener,
                OnDeleteButtonClickListener onDeleteButtonClickListener) {
            super(context, entries, onEditButtonClickListener, onDeleteButtonClickListener);
        }

        @Override
        protected String getDisplayName(RestrictionEntry entry) {
            return entry.getKey();
        }
    }
}