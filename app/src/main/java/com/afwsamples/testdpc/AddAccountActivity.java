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
import android.accounts.AccountManagerCallback;
import android.accounts.AccountManagerFuture;
import android.accounts.AuthenticatorException;
import android.accounts.OperationCanceledException;
import android.app.Activity;
import android.app.AlertDialog;
import android.content.DialogInterface;
import android.content.Intent;
import android.os.Bundle;
import android.text.TextUtils;
import android.util.Log;
import android.view.View;
import android.widget.EditText;
import android.widget.RadioGroup;
import android.widget.Toast;

import com.android.setupwizardlib.SetupWizardLayout;
import com.android.setupwizardlib.view.NavigationBar;

import java.io.IOException;

/**
 * This activity is started after provisioning is complete in {@link DeviceAdminReceiver}.
 * It is responsible for adding an account to the managed profile (Profile Owner) or managed device
 * (Device Owner).
 */
public class AddAccountActivity extends Activity implements NavigationBar.NavigationBarListener {

    private static final String TAG = "AddAccountActivity";
    private static final String GOOGLE_ACCOUNT_TYPE = "com.google";

    public static final String EXTRA_NEXT_ACTIVITY_INTENT = "nextActivityIntent";

    private Intent mNextActivityIntent = null;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

        setContentView(R.layout.activity_add_account);
        SetupWizardLayout layout = (SetupWizardLayout) findViewById(R.id.setup_wizard_layout);
        layout.getNavigationBar().setNavigationBarListener(this);
        NavigationBar navigationBar = layout.getNavigationBar();
        navigationBar.getBackButton().setEnabled(false);

        Bundle extras = getIntent().getExtras();
        if (extras != null) {
            mNextActivityIntent = (Intent) extras.get(EXTRA_NEXT_ACTIVITY_INTENT);
        }
        if (mNextActivityIntent == null) {
            Log.e(TAG, EXTRA_NEXT_ACTIVITY_INTENT + " extra must be provided");
            finish();
        }
    }

    private void addAccount(String accountName) {
        AccountManager accountManager = AccountManager.get(this);
        Bundle bundle = new Bundle();
        if (!TextUtils.isEmpty(accountName)) {
            bundle.putString(AccountManager.KEY_ACCOUNT_NAME, accountName);
        }

        accountManager.addAccount(GOOGLE_ACCOUNT_TYPE, null, null, bundle, this,
                new AccountManagerCallback<Bundle>() {
                    @Override
                    public void run(AccountManagerFuture<Bundle> accountManagerFuture) {
                        boolean isAccountAdded = false;
                        String accountNameAdded = null;
                        try {
                            Bundle bundle = accountManagerFuture.getResult();
                            if (bundle.containsKey(AccountManager.KEY_ACCOUNT_NAME)) {
                                isAccountAdded = true;
                                accountNameAdded =
                                        bundle.getString(AccountManager.KEY_ACCOUNT_NAME);
                            }
                        } catch (OperationCanceledException | AuthenticatorException
                                | IOException e) {
                            Log.e(TAG, "addAccount - failed", e);
                            Toast.makeText(AddAccountActivity.this,
                                    R.string.fail_to_add_account, Toast.LENGTH_LONG).show();
                            return;
                        }
                        Log.d(TAG, "addAccount - isAccountAdded: " + isAccountAdded
                                + ", accountNameAdded: " + accountNameAdded);
                        startActivity(mNextActivityIntent);
                        finish();
                    }
                }, null);
    }

    @Override
    public void onNavigateNext() {
        RadioGroup addAccountOptions = (RadioGroup) findViewById(R.id.add_account_options);
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
                                new DialogInterface.OnClickListener() {
                                    @Override
                                    public void onClick(DialogInterface dialogInterface, int i) {
                                        EditText editText = (EditText) view.findViewById(
                                                R.id.input);
                                        String accountName = editText.getText().toString();
                                        addAccount(accountName);
                                    }
                                })
                        .show();
                break;
            case R.id.add_account_skip:
                mNextActivityIntent.putExtra(EnableProfileActivity.EXTRA_ENABLE_PROFILE_NOW, true);
                startActivity(mNextActivityIntent);
                finish();
                break;
        }
    }

    @Override
    public void onNavigateBack() {}
}
