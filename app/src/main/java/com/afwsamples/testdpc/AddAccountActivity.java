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

package com.afwsamples.testdpc;

import android.accounts.AccountManager;
import android.accounts.AuthenticatorException;
import android.accounts.OperationCanceledException;
import android.app.Activity;
import android.app.AlertDialog;
import android.app.admin.DevicePolicyManager;
import android.content.ComponentName;
import android.content.Context;
import android.content.Intent;
import android.os.Bundle;
import android.os.UserManager;
import android.support.v4.os.BuildCompat;
import android.text.TextUtils;
import android.util.Log;
import android.view.View;
import android.widget.EditText;
import android.widget.RadioGroup;
import android.widget.Toast;

import com.android.setupwizardlib.GlifLayout;

import java.io.IOException;

/**
 * This activity is started after provisioning is complete in {@link DeviceAdminReceiver}.
 * It is responsible for adding an account to the managed profile (Profile Owner) or managed device
 * (Device Owner).
 */
public class AddAccountActivity extends Activity {

    private static final String TAG = "AddAccountActivity";
    private static final String GOOGLE_ACCOUNT_TYPE = "com.google";

    public static final String EXTRA_NEXT_ACTIVITY_INTENT = "nextActivityIntent";

    private Intent mNextActivityIntent = null;
    private boolean mDisallowModifyAccounts;

    private UserManager mUserManager;
    private DevicePolicyManager mDevicePolicyManager;
    private ComponentName mAdminComponentName;


    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

        mUserManager = (UserManager) getSystemService(Context.USER_SERVICE);
        mDevicePolicyManager =
                (DevicePolicyManager) getSystemService(Context.DEVICE_POLICY_SERVICE);
        mAdminComponentName = DeviceAdminReceiver.getComponentName(this);

        setContentView(R.layout.activity_add_account);
        GlifLayout layout = findViewById(R.id.setup_wizard_layout);
        layout.findViewById(R.id.suw_navbar_next).setOnClickListener(this::onNavigateNext);

        Bundle extras = getIntent().getExtras();
        if (extras != null) {
            mNextActivityIntent = (Intent) extras.get(EXTRA_NEXT_ACTIVITY_INTENT);
        }
    }

    @Override
    protected void onResume() {
        super.onResume();
        disableUserRestrictions();
    }

    @Override
    protected void onPause() {
        super.onPause();
        restoreUserRestrictions();
    }

    private void addAccount(String accountName) {
        AccountManager accountManager = AccountManager.get(this);
        Bundle bundle = new Bundle();
        if (!TextUtils.isEmpty(accountName)) {
            bundle.putString(AccountManager.KEY_ACCOUNT_NAME, accountName);
        }

        accountManager.addAccount(GOOGLE_ACCOUNT_TYPE, null, null, bundle, this,
                accountManagerFuture -> {
                    try {
                        Bundle result = accountManagerFuture.getResult();
                        String accountNameAdded = result.getString(AccountManager.KEY_ACCOUNT_NAME);
                        Log.d(TAG, "addAccount - accountNameAdded: " + accountNameAdded);
                        if (mNextActivityIntent != null) {
                            startActivity(mNextActivityIntent);
                        }
                        finish();
                    } catch (OperationCanceledException | AuthenticatorException
                            | IOException e) {
                        Log.e(TAG, "addAccount - failed", e);
                        Toast.makeText(AddAccountActivity.this,
                                R.string.fail_to_add_account, Toast.LENGTH_LONG).show();
                    }
                }, null);
    }

    private void disableUserRestrictions() {
        if (BuildCompat.isAtLeastN()) {
            // DPC is allowed to bypass DISALLOW_MODIFY_ACCOUNTS on N or above.
            Log.v(TAG, "skip disabling user restriction on N or above");
            return;
        }
        Log.v(TAG, "disabling user restrictions");
        mDisallowModifyAccounts =
                mUserManager.hasUserRestriction(UserManager.DISALLOW_MODIFY_ACCOUNTS);
        mDevicePolicyManager
                .clearUserRestriction(mAdminComponentName, UserManager.DISALLOW_MODIFY_ACCOUNTS);
    }

    private void restoreUserRestrictions() {
        if (BuildCompat.isAtLeastN()) {
            // DPC is allowed to bypass DISALLOW_MODIFY_ACCOUNTS on N or above.
            Log.v(TAG, "skip restoring user restrictions on N or above");
            return;
        }
        Log.v(TAG, "restoring user restrictions");
        if (mDisallowModifyAccounts) {
            mDevicePolicyManager
                    .addUserRestriction(mAdminComponentName, UserManager.DISALLOW_MODIFY_ACCOUNTS);
        }
    }

    public void onNavigateNext(View nextButton) {
        RadioGroup addAccountOptions = findViewById(R.id.add_account_options);
        switch (addAccountOptions.getCheckedRadioButtonId()) {
            case R.id.add_account:
                addAccount(null);
                break;
            case R.id.add_account_with_name:
                final View view = getLayoutInflater().inflate(R.layout.simple_edittext, null);
                new AlertDialog.Builder(this)
                        .setTitle(R.string.add_account_dialog_title)
                        .setView(view)
                        .setPositiveButton(android.R.string.ok,
                                (dialogInterface, i) -> {
                                    EditText editText = (EditText) view.findViewById(
                                            R.id.input);
                                    String accountName = editText.getText().toString();
                                    addAccount(accountName);
                                })
                        .show();
                break;
            case R.id.add_account_skip:
                if (mNextActivityIntent != null) {
                    startActivity(mNextActivityIntent);
                }
                finish();
                break;
        }
    }
}
