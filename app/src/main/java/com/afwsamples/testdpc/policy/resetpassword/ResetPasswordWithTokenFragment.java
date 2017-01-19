/*
 * Copyright (C) 2017 The Android Open Source Project
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

package com.afwsamples.testdpc.policy.resetpassword;

import android.annotation.TargetApi;
import android.app.Fragment;
import android.app.KeyguardManager;
import android.app.admin.DevicePolicyManager;
import android.content.ComponentName;
import android.content.Context;
import android.content.Intent;
import android.content.SharedPreferences;
import android.os.Build;
import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Button;
import android.widget.CheckBox;
import android.widget.EditText;
import android.widget.Toast;

import com.afwsamples.testdpc.DeviceAdminReceiver;
import com.afwsamples.testdpc.R;
import com.afwsamples.testdpc.common.ReflectionUtil;

import java.nio.charset.StandardCharsets;
import java.security.NoSuchAlgorithmException;
import java.security.SecureRandom;
import java.util.Base64;

import static android.app.Activity.RESULT_OK;


/**
 * STOPSHIP: remove reflection once SDK is updated.
 */
@TargetApi(Build.VERSION_CODES.O)
public class ResetPasswordWithTokenFragment extends Fragment implements View.OnClickListener {

    private static final String PREFS_NAME = "password-token";
    private static final String TOKEN_NAME = "token";
    private static final int REQUEST_CONFIRM_CREDENTIAL = 1;

    private DevicePolicyManager mDpm;
    private EditText mEdtToken;
    private EditText mEdtUseToken;
    private EditText mEdtTokenStatus;
    private EditText mEdtPassword;
    private Button mBtnNewToken;
    private Button mBtnRemoveToken;
    private Button mBtnSetPassword;
    private CheckBox mChkRequireEntry;
    private CheckBox mChkDoNotRequirePasswordOnBoot;
    private Button mBtnActivateToken;
    private KeyguardManager mKeyguardMgr;

    @Override
    public void onResume() {
        super.onResume();
        reloadTokenInfomation();
    }

    public static byte[] loadPasswordResetTokenFromPreference(Context context) {
        Context directBootContext = context.createDeviceProtectedStorageContext();
        SharedPreferences settings = directBootContext.getSharedPreferences(PREFS_NAME, 0);
        String tokenString = settings.getString(TOKEN_NAME, null);
        if (tokenString != null) {
            return Base64.getDecoder().decode(tokenString.getBytes(StandardCharsets.UTF_8));
        } else {
            return null;
        }
    }

    private void savePasswordResetTokenToPreference(byte[] token) {
        Context directBootContext = getContext().createDeviceProtectedStorageContext();
        /****** WARNING: DO NOT DO THIS IN PRODUCTION CODE. THIS IS INSECURE! ******
         * Do not store unencrypted token directly in device-encrypted storage, which is not
         * protected by user's lockscreen credential. The token should be stored in a way such that
         * it's only derivable by interacting with the backend server.
         */
        SharedPreferences settings = directBootContext.getSharedPreferences(PREFS_NAME, 0);
        SharedPreferences.Editor editor = settings.edit();
        if (token != null) {
            editor.putString(TOKEN_NAME, Base64.getEncoder().encodeToString((token)));
        } else {
            editor.remove(TOKEN_NAME);
        }
        editor.commit();
    }

    private void reloadTokenInfomation() {
        final byte[] token = loadPasswordResetTokenFromPreference(getContext());
        final String tokenString = token != null ? Base64.getEncoder().encodeToString(token)
                : getString(R.string.reset_password_token_none);
        mEdtToken.setText(tokenString);
        mEdtUseToken.setText(tokenString);
        boolean active = dpmIsResetPasswordTokenActive(DeviceAdminReceiver.getComponentName(
                getContext()));
        mEdtTokenStatus.setText(getString(active ? R.string.reset_password_token_active
                : R.string.reset_password_token_inactive));
        mBtnActivateToken.setEnabled(!active && (token != null));
    }


    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        mDpm = getContext().getSystemService(DevicePolicyManager.class);
        mKeyguardMgr = getContext().getSystemService(KeyguardManager.class);
    }

    @Override
    public View onCreateView(LayoutInflater layoutInflater, ViewGroup container,
            Bundle savedInstanceState) {
        View view = layoutInflater.inflate(R.layout.reset_password_token, null);

        mEdtToken = (EditText) view.findViewById(R.id.edtToken);
        mEdtUseToken = (EditText) view.findViewById(R.id.edtUseToken);
        mEdtTokenStatus = (EditText) view.findViewById(R.id.edtTokenStatus);
        mEdtPassword = (EditText) view.findViewById(R.id.edtPassword);

        mChkRequireEntry = (CheckBox) view.findViewById(R.id.require_password_entry_checkbox);
        mChkDoNotRequirePasswordOnBoot = (CheckBox) view.findViewById(
                R.id.dont_require_password_on_boot_checkbox);

        mBtnNewToken = (Button) view.findViewById(R.id.btnNewToken);
        mBtnRemoveToken = (Button) view.findViewById(R.id.btnRemoveToken);
        mBtnActivateToken = (Button) view.findViewById(R.id.btnActivateToken);
        mBtnSetPassword = (Button) view.findViewById(R.id.btnSetPassword);

        mBtnNewToken.setOnClickListener(this);
        mBtnRemoveToken.setOnClickListener(this);
        mBtnActivateToken.setOnClickListener(this);
        mBtnSetPassword.setOnClickListener(this);
        return view;
    }

    @Override
    public void onClick(View v) {
        switch (v.getId()) {
            case R.id.btnNewToken:
                createNewPasswordToken();
                break;
            case R.id.btnRemoveToken:
                removePasswordToken();
                break;
            case R.id.btnActivateToken:
                activatePasswordToken();
                break;
            case R.id.btnSetPassword:
                resetPasswordWithToken();
                break;
        }
    }

    @Override
    public void onActivityResult(int requestCode, int resultCode, Intent data) {
        if (requestCode == REQUEST_CONFIRM_CREDENTIAL) {
            if (resultCode == RESULT_OK) {
                reloadTokenInfomation();
            } else {
                showToast(getString(R.string.activate_reset_password_token_cancelled));
            }
        }
    }

    private byte[] generateRandomPasswordToken() {
        try {
            return SecureRandom.getInstance("SHA1PRNG").generateSeed(32);
        } catch (NoSuchAlgorithmException e) {
            e.printStackTrace();
            return null;
        }
    }
    private void createNewPasswordToken() {
        byte[] token = generateRandomPasswordToken();
        if (!dpmSetResetPasswordToken(DeviceAdminReceiver.getComponentName(getContext()),
                token)) {
            showToast(getString(R.string.set_password_reset_token_failed));
            return;
        }
        savePasswordResetTokenToPreference(token);
        reloadTokenInfomation();
    }

    private void activatePasswordToken() {
        Intent intent = mKeyguardMgr.createConfirmDeviceCredentialIntent(null, null);
        if (intent != null) {
            startActivityForResult(intent, REQUEST_CONFIRM_CREDENTIAL);
        }
    }

    private void removePasswordToken() {
        if (!dpmClearResetPasswordToken(DeviceAdminReceiver.getComponentName(getContext()))) {
            showToast(getString(R.string.clear_password_reset_token_failed));
            return;
        }
        savePasswordResetTokenToPreference(null);
        reloadTokenInfomation();
    }

    private void resetPasswordWithToken() {
        final String tokenString = mEdtUseToken.getText().toString();
        byte[] token;
        try {
            token = Base64.getDecoder().decode(tokenString);
        } catch (IllegalArgumentException e) {
            token = tokenString.getBytes(StandardCharsets.UTF_8);
        }

        final String password = mEdtPassword.getText().toString();
        int flags = 0;
        flags |= mChkRequireEntry.isChecked() ?
                DevicePolicyManager.RESET_PASSWORD_REQUIRE_ENTRY : 0;
        flags |= mChkDoNotRequirePasswordOnBoot.isChecked() ?
                DevicePolicyManager.RESET_PASSWORD_DO_NOT_ASK_CREDENTIALS_ON_BOOT : 0;

        if (token != null) {
            boolean result = dpmResetPasswordWithToken(
                    DeviceAdminReceiver.getComponentName(getContext()),
                    password, token, flags);
            if (result) {
                showToast(getString(R.string.reset_password_with_token_succeed, password));
            } else {
                showToast(getString(R.string.reset_password_with_token_failed));
            }
        } else {
            showToast(getString(R.string.reset_password_no_token));
        }
    }

    private void showToast(String msg) {
        Toast.makeText(getContext(), msg, Toast.LENGTH_LONG).show();
    }

    private boolean dpmSetResetPasswordToken(ComponentName admin, byte[] token) {
        return (Boolean) ReflectionUtil.invoke(mDpm, "setResetPasswordToken", admin, token);
    }

    private boolean dpmClearResetPasswordToken(ComponentName admin) {
        return (Boolean) ReflectionUtil.invoke(mDpm, "clearResetPasswordToken", admin);
    }

    private boolean dpmIsResetPasswordTokenActive(ComponentName admin) {
        return (Boolean) ReflectionUtil.invoke(mDpm, "isResetPasswordTokenActive", admin);
    }

    private boolean dpmResetPasswordWithToken(ComponentName admin, String password, byte[] token,
                                              int flags) {
        return (Boolean) ReflectionUtil.invoke(mDpm, "resetPasswordWithToken",
                new Class<?>[] {ComponentName.class, String.class, byte[].class, int.class},
                admin, password, token, flags);
    }
}
