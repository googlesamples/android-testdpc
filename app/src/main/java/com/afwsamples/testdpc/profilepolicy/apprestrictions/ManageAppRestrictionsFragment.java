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
import com.afwsamples.testdpc.common.keyvaluepair.KeyValueUtil;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;
import java.util.Set;

import static com.afwsamples.testdpc.common.keyvaluepair.KeyValuePairDialogFragment.RESULT_ENTRY;
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

    protected void loadAppRestrictionsList(List<RestrictionEntry> restrictionEntries) {
        if (restrictionEntries != null) {
            mAppRestrictionsArrayAdapter.clear();
            mAppRestrictionsArrayAdapter.addAll(restrictionEntries);
        }
    }

    protected ArrayList<RestrictionEntry> convertBundleToRestrictions(
            Bundle restrictionBundle, ArrayList<RestrictionEntry> manifestRestrictionEntries) {
        ArrayList<RestrictionEntry> restrictionEntries = new ArrayList<>();
        Set<String> keys = restrictionBundle.keySet();
        for (String key : keys) {
            Object value = restrictionBundle.get(key);
            RestrictionEntry currentEntry = findRestrictionEntryByKey(key,
                manifestRestrictionEntries);
            if (value instanceof Boolean) {
                if (currentEntry == null
                        || currentEntry.getType() != RestrictionEntry.TYPE_BOOLEAN) {
                    currentEntry = new RestrictionEntry(key, (boolean) value);
                } else {
                    currentEntry.setSelectedState((boolean) value);
                }
                restrictionEntries.add(currentEntry);
            } else if (value instanceof Integer) {
                if (currentEntry == null
                        || currentEntry.getType() != RestrictionEntry.TYPE_INTEGER) {
                    currentEntry = new RestrictionEntry(key, (int) value);
                } else {
                    currentEntry.setIntValue((int) value);
                }
                restrictionEntries.add(currentEntry);
            } else if (value instanceof String) {
                // DevicePolicyManager returns Choice restriction as string
                // We will find correct type in app manifest restrictions
                if (currentEntry == null
                    || (currentEntry.getType() != RestrictionEntry.TYPE_STRING
                        && currentEntry.getType() != RestrictionEntry.TYPE_CHOICE)) {
                    currentEntry = new RestrictionEntry(RestrictionEntry.TYPE_STRING, key);
                    currentEntry.setSelectedString((String) value);
                } else {
                    currentEntry.setSelectedString((String) value);
                }
                restrictionEntries.add(currentEntry);
            } else if (value instanceof String[]) {
                if (currentEntry == null || currentEntry.getType() != RestrictionEntry.TYPE_MULTI_SELECT) {
                    currentEntry = new RestrictionEntry(key, (String[]) value);
                } else {
                    currentEntry.setAllSelectedStrings((String[]) value);
                }
                restrictionEntries.add(currentEntry);
            } else if (value instanceof Bundle) {
                    addBundleEntryToRestrictions(
                            restrictionEntries, key, (Bundle) value, currentEntry);
            } else if (value instanceof Parcelable[]) {
                addBundleArrayToRestrictions(restrictionEntries, key, (Parcelable[]) value, currentEntry);
            }
        }
        return restrictionEntries;
    }

    private RestrictionEntry findRestrictionEntryByKey(
            String key, List<RestrictionEntry> manifestRestrictions) {
        if (!TextUtils.isEmpty(key) && manifestRestrictions != null) {
            for (RestrictionEntry entry : manifestRestrictions) {
                if (entry != null && key.equals(entry.getKey())) {
                    return entry;
                }
            }
        }
        return null;
    }

    @TargetApi(Build.VERSION_CODES.M)
    private void addBundleEntryToRestrictions(List<RestrictionEntry> restrictionEntries,
            String key, Bundle value, RestrictionEntry currentEntry) {
        if (currentEntry == null || currentEntry.getType() != RestrictionEntry.TYPE_BUNDLE) {
            currentEntry = RestrictionEntry.createBundleEntry(
                    key, convertBundleToRestrictions(value, null).toArray(new RestrictionEntry[0]));
        } else {
            currentEntry.setRestrictions(
                convertBundleToRestrictions(value,
                    new ArrayList<RestrictionEntry>(Arrays.asList(
                            currentEntry.getRestrictions()))).toArray(new RestrictionEntry[0]));
        }
        restrictionEntries.add(currentEntry);
    }

    @TargetApi(Build.VERSION_CODES.M)
    private void addBundleArrayToRestrictions(List<RestrictionEntry> restrictionEntries,
            String key, Parcelable[] value, RestrictionEntry manifestBundleArrayEntry) {
        int length = value.length;
        String bundleKey = key;
        RestrictionEntry bundleRestrictionFromManifest = null;
        ArrayList<RestrictionEntry> bundleRestrictionEntriesFromManifest = null;
        if (manifestBundleArrayEntry != null
                && manifestBundleArrayEntry.getType() == RestrictionEntry.TYPE_BUNDLE_ARRAY) {
            // In manifest restrictions BundleArray can contain one Bundle only
            // to define bundles structure.
            RestrictionEntry[] bundles = manifestBundleArrayEntry.getRestrictions();
            if (bundles != null && bundles.length > 0 && bundles[0] != null) {
                bundleRestrictionFromManifest = bundles[0];
            }
        }
        // Make Bundles
        RestrictionEntry[] entriesArray = new RestrictionEntry[length];
        for (int i = 0; i < entriesArray.length; ++i) {
            if (bundleRestrictionFromManifest != null) {
                // We need to clone here to have new copy of manifest restrictions for each bundle
                bundleRestrictionFromManifest = KeyValueUtil.cloneRestriction(
                        bundleRestrictionFromManifest);
                bundleKey = bundleRestrictionFromManifest.getKey();
                bundleRestrictionEntriesFromManifest = new ArrayList<>(
                    Arrays.asList(bundleRestrictionFromManifest.getRestrictions()));
            }
            entriesArray[i] = RestrictionEntry.createBundleEntry(bundleKey,
                 convertBundleToRestrictions((Bundle) value[i],
                     bundleRestrictionEntriesFromManifest).toArray(new RestrictionEntry[0]));
            if (entriesArray[i] != null && bundleRestrictionFromManifest != null) {
                entriesArray[i].setTitle(bundleRestrictionFromManifest.getTitle());
                entriesArray[i].setDescription(bundleRestrictionFromManifest.getDescription());
            }
        }
        // Set Bundles to Bundle array
        if (manifestBundleArrayEntry == null
                || manifestBundleArrayEntry.getType() != RestrictionEntry.TYPE_BUNDLE_ARRAY) {
            manifestBundleArrayEntry = RestrictionEntry.createBundleArrayEntry(key, entriesArray);
        } else {
            manifestBundleArrayEntry.setRestrictions(entriesArray);
        }
        restrictionEntries.add(manifestBundleArrayEntry);
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
        if (mEditingRestrictionEntry != null) {
            key = mEditingRestrictionEntry.getKey();
            type = KeyValueUtil.getTypeIndexFromRestrictionType(mEditingRestrictionEntry.getType());
        } else {
            mEditingRestrictionEntry = new RestrictionEntry(key, false);
        }

        int[] supportType = (Build.VERSION.SDK_INT < Build.VERSION_CODES.M)
                ? SUPPORTED_TYPES_PRE_M
                : SUPPORTED_TYPES;
        KeyValuePairDialogFragment dialogFragment =
                KeyValuePairDialogFragment.newInstance(type, true, key, value,
                        mEditingRestrictionEntry, supportType, getCurrentAppName());
        dialogFragment.setTargetFragment(this, RESULT_CODE_EDIT_DIALOG);
        dialogFragment.show(getFragmentManager(), "dialog");
    }

    @Override
    public void onActivityResult(int requestCode, int resultCode, Intent result) {
        if (resultCode != Activity.RESULT_OK) {
            return;
        }

        switch (requestCode) {
            case RESULT_CODE_EDIT_DIALOG:
                RestrictionEntry newRestrictionEntry = result.getParcelableExtra(RESULT_ENTRY);
                if (newRestrictionEntry != null) {
                    if (mEditingRestrictionEntry != null
                            && mEditingRestrictionEntry.getKey().equals(newRestrictionEntry.getKey())) {
                        mAppRestrictionsArrayAdapter.remove(mEditingRestrictionEntry);
                    }
                    mAppRestrictionsArrayAdapter.add(newRestrictionEntry);
                    mAppRestrictionsArrayAdapter.notifyDataSetChanged();
                }
                mEditingRestrictionEntry = null;
                break;
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
            // We will use restrictions from app manifest as basis and will update values
            // saved by user via DevicePolicyManager
            // Also, we will load custom restrictions added by user (not defined in app manifest)
            ArrayList<RestrictionEntry> manifestRestrictions = getManifestRestrictions(pkgName);
            convertTypeNullToString(manifestRestrictions);
            loadAppRestrictionsList(convertBundleToRestrictions(bundle, manifestRestrictions));
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
                loadAppRestrictionsList(manifestRestrictions);
            }
        }
    }

    private ArrayList<RestrictionEntry> getManifestRestrictions(String pkgName) {
        ArrayList<RestrictionEntry> manifestRestrictions = null;
        if (!TextUtils.isEmpty(pkgName)) {
            try {
                manifestRestrictions = new ArrayList<>(mRestrictionsManager.getManifestRestrictions(
                    pkgName));
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
        if (restrictionEntries == null)
            return;
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
                RestrictionManagerCompat.convertRestrictionsToBundle(mRestrictionEntries));
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