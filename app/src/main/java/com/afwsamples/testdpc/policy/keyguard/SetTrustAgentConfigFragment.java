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

package com.afwsamples.testdpc.policy.keyguard;

import android.annotation.TargetApi;
import android.app.Activity;
import android.app.admin.DevicePolicyManager;
import android.content.ComponentName;
import android.content.Context;
import android.content.Intent;
import android.content.pm.PackageManager;
import android.content.pm.ResolveInfo;
import android.os.Build;
import android.os.Bundle;
import android.os.PersistableBundle;
import android.text.TextUtils;
import android.view.View;
import android.widget.BaseAdapter;
import android.widget.Toast;

import com.afwsamples.testdpc.DeviceAdminReceiver;
import com.afwsamples.testdpc.R;
import com.afwsamples.testdpc.common.BundleUtil;
import com.afwsamples.testdpc.common.EditDeleteArrayAdapter;
import com.afwsamples.testdpc.common.ManageResolveInfoFragment;
import com.afwsamples.testdpc.common.keyvaluepair.KeyValuePairDialogFragment;

import java.util.ArrayList;
import java.util.List;

import static com.afwsamples.testdpc.common.EditDeleteArrayAdapter.OnDeleteButtonClickListener;
import static com.afwsamples.testdpc.common.EditDeleteArrayAdapter.OnEditButtonClickListener;
import static com.afwsamples.testdpc.common.keyvaluepair.KeyValuePairDialogFragment.RESULT_VALUE;

@TargetApi(Build.VERSION_CODES.M)
public class SetTrustAgentConfigFragment extends ManageResolveInfoFragment
        implements View.OnClickListener, OnEditButtonClickListener<String>,
        OnDeleteButtonClickListener<String> {
    /**
     * ResolveInfo of configuring trust agent.
     */
    private ResolveInfo mResolveInfo;
    /**
     * The current bundle.
     */
    private PersistableBundle mBundle;
    /**
     * The initial value of the passed in bundle.
     */
    private PersistableBundle mInitialBundle;
    private List<String> mKeyList = new ArrayList<>();
    /**
     * The current editing key.
     */
    private String mEditingKey;

    private EditDeleteArrayAdapter<String> mAdapter;
    private int mType;

    private DevicePolicyManager mDevicePolicyManager;
    private PackageManager mPackageManager;

    private static final String KEY_TYPE = "type";
    private static final int RESULT_CODE_EDIT_DIALOG = 1;
    private static final int[] SUPPORTED_TYPE = {
            KeyValuePairDialogFragment.DialogType.BOOL_TYPE,
            KeyValuePairDialogFragment.DialogType.INT_TYPE,
            KeyValuePairDialogFragment.DialogType.STRING_TYPE,
            KeyValuePairDialogFragment.DialogType.STRING_ARRAY_TYPE,
            KeyValuePairDialogFragment.DialogType.BUNDLE_TYPE,
    };

    public interface Type {
        int PARENT = 0;
        int SELF = 1;
    }

    public static SetTrustAgentConfigFragment newInstance(int type) {
        Bundle args = new Bundle();
        args.putInt(KEY_TYPE, type);
        SetTrustAgentConfigFragment fragment = new SetTrustAgentConfigFragment();
        fragment.setArguments(args);
        return fragment;
    }

    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        mType = getArguments().getInt(KEY_TYPE);
        mDevicePolicyManager = getDevicePolicyManagerFromType(mType);
        mPackageManager = getActivity().getPackageManager();
    }

    @Override
    protected List<ResolveInfo> loadResolveInfoList() {
        PackageManager pm = getActivity().getPackageManager();
        Intent trustAgentIntent = new Intent("android.service.trust.TrustAgentService");
        List<ResolveInfo> resolveInfos = pm.queryIntentServices(trustAgentIntent,
                PackageManager.GET_META_DATA);

        List<ResolveInfo> agents = new ArrayList<>();
        final int count = resolveInfos.size();
        for (int i = 0; i < count; i++) {
            ResolveInfo resolveInfo = resolveInfos.get(i);
            if (resolveInfo.serviceInfo == null) continue;
            agents.add(resolveInfo);
        }
        return agents;
    }

    @Override
    protected BaseAdapter createListAdapter() {
        mAdapter = new StringEditDeleteArrayAdapter(getActivity(), mKeyList, this, this);
        return mAdapter;
    }

    @Override
    protected void onSpinnerItemSelected(ResolveInfo resolveInfo) {
        mResolveInfo = resolveInfo;
        ComponentName componentName = getComponentName(resolveInfo);
        ComponentName admin = getAdmin();
        List<PersistableBundle> resultList =
                mDevicePolicyManager.getTrustAgentConfiguration(admin, componentName);
        if (resultList == null || resultList.size() == 0) {
            mBundle = new PersistableBundle();
        } else {
            mBundle = resultList.get(0);
        }
        mInitialBundle = new PersistableBundle(mBundle);
        mAdapter.clear();
        mAdapter.addAll(mBundle.keySet());
    }

    private ComponentName getComponentName(ResolveInfo resolveInfo) {
        return new ComponentName(resolveInfo.serviceInfo.packageName, resolveInfo.serviceInfo.name);
    }

    @Override
    protected void resetConfig() {
        mBundle = new PersistableBundle(mInitialBundle);
        mAdapter.clear();
        mAdapter.addAll(mBundle.keySet());
    }

    @Override
    protected void saveConfig() {
        setTrustAgentConfiguration();
        Toast.makeText(getActivity(), R.string.saved_trust_agent_config, Toast.LENGTH_LONG).show();
    }

    @Override
    protected void addNewRow() {
        showEditDialog(null);
    }

    @Override
    protected void loadDefault() {
    }

    private void setTrustAgentConfiguration() {
        ComponentName componentName = getComponentName(mResolveInfo);
        mDevicePolicyManager.setTrustAgentConfiguration(getAdmin(),
                componentName, mBundle);
    }

    private void showEditDialog(final String key) {
        mEditingKey = key;
        int type = KeyValuePairDialogFragment.DialogType.BOOL_TYPE;
        Object value = null;
        if (key != null) {
            value = mBundle.get(key);
            if (value instanceof Boolean) {
                type = KeyValuePairDialogFragment.DialogType.BOOL_TYPE;
            } else if (value instanceof Integer) {
                type = KeyValuePairDialogFragment.DialogType.INT_TYPE;
            } else if (value instanceof String) {
                type = KeyValuePairDialogFragment.DialogType.STRING_TYPE;
            } else if (value instanceof String[]) {
                type = KeyValuePairDialogFragment.DialogType.STRING_ARRAY_TYPE;
            } else if (value instanceof PersistableBundle) {
                type = KeyValuePairDialogFragment.DialogType.BUNDLE_TYPE;
                value = BundleUtil.persistableBundleToBundle((PersistableBundle) value);
            }
        }
        KeyValuePairDialogFragment dialogFragment =
                KeyValuePairDialogFragment.newInstance(type, true, key, value, SUPPORTED_TYPE,
                        mResolveInfo.loadLabel(mPackageManager).toString());
        dialogFragment.setTargetFragment(this, RESULT_CODE_EDIT_DIALOG);
        dialogFragment.show(getFragmentManager(), "dialog");
    }

    private ComponentName getAdmin() {
        return DeviceAdminReceiver.getComponentName(getActivity());
    }

    @Override
    public void onEditButtonClick(String key) {
        showEditDialog(key);
    }

    @Override
    public void onDeleteButtonClick(String entry) {
        mBundle.remove(entry);
    }

    @Override
    public void onActivityResult(int requestCode, int resultCode, Intent result) {
        if (resultCode != Activity.RESULT_OK) {
            return;
        }
        switch (requestCode) {
            case RESULT_CODE_EDIT_DIALOG:
                int type = result.getIntExtra(KeyValuePairDialogFragment.RESULT_TYPE, 0);
                String key = result.getStringExtra(KeyValuePairDialogFragment.RESULT_KEY);
                updateBundleFromResultIntent(type, key, result);
                if (!TextUtils.isEmpty(mEditingKey)) {
                    mAdapter.remove(mEditingKey);
                }
                mAdapter.add(key);
                mEditingKey = null;
                break;
        }
    }

    private void updateBundleFromResultIntent(int type, String key, Intent intent) {
        switch (type) {
            case KeyValuePairDialogFragment.DialogType.BOOL_TYPE:
                mBundle.putBoolean(key, intent.getBooleanExtra(RESULT_VALUE, false));
                break;
            case KeyValuePairDialogFragment.DialogType.INT_TYPE:
                mBundle.putInt(key, intent.getIntExtra(RESULT_VALUE, 0));
                break;
            case KeyValuePairDialogFragment.DialogType.STRING_TYPE:
                mBundle.putString(key, intent.getStringExtra(RESULT_VALUE));
                break;
            case KeyValuePairDialogFragment.DialogType.STRING_ARRAY_TYPE:
                mBundle.putStringArray(key, intent.getStringArrayExtra(RESULT_VALUE));
                break;
            case KeyValuePairDialogFragment.DialogType.BUNDLE_TYPE:
                Bundle bundle = intent.getParcelableExtra(RESULT_VALUE);
                mBundle.putPersistableBundle(key, BundleUtil.bundleToPersistableBundle(bundle));
                break;
            default:
                throw new IllegalArgumentException("invalid type:" + type);
        }
    }

    @TargetApi(Build.VERSION_CODES.N)
    private DevicePolicyManager getDevicePolicyManagerFromType(int type) {
        DevicePolicyManager devicePolicyManager =
                (DevicePolicyManager) getActivity().getSystemService(Context.DEVICE_POLICY_SERVICE);
        if (type == Type.SELF) {
            return devicePolicyManager;
        } else {
            return devicePolicyManager.getParentProfileInstance(getAdmin());
        }
    }

    private static class StringEditDeleteArrayAdapter extends EditDeleteArrayAdapter<String> {
        public StringEditDeleteArrayAdapter(Context context, List<String> entries,
                OnEditButtonClickListener onEditButtonClickListener,
                OnDeleteButtonClickListener onDeleteButtonClickListener) {
            super(context, entries, onEditButtonClickListener, onDeleteButtonClickListener);
        }

        @Override
        protected String getDisplayName(String entry) {
            return entry;
        }
    }
}
