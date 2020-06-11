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

package com.afwsamples.testdpc.policy.wifimanagement;

import android.app.AlertDialog;
import android.app.Dialog;
import android.app.DialogFragment;
import android.content.DialogInterface;
import android.net.wifi.WifiConfiguration;
import android.os.Bundle;
import android.text.Editable;
import android.text.TextWatcher;
import android.view.LayoutInflater;
import android.view.View;
import android.view.WindowManager;
import android.widget.AdapterView;
import android.widget.ArrayAdapter;
import android.widget.Button;
import android.widget.EditText;
import android.widget.Spinner;
import android.widget.Toast;

import com.afwsamples.testdpc.R;

/**
 * Displays a dialog for creating a wifi configuration.
 */
public class WifiConfigCreationDialog extends DialogFragment implements
        AdapterView.OnItemSelectedListener, View.OnClickListener, TextWatcher {

    public static final int SECURITY_TYPE_NONE = 0;
    public static final int SECURITY_TYPE_WEP = 1;
    public static final int SECURITY_TYPE_WPA = 2;

    private AlertDialog mAlertDialog;
    private View mPasswordView;
    private EditText mSsidText;
    private EditText mPasswordText;
    private boolean mPasswordDirty;
    private Button mSaveButton;
    private int mSecurityType;
    private Spinner mSecurityChoicesSpinner;
    private WifiConfiguration mOldConfig;
    private Listener mListener;

    public interface Listener {
        void onDismiss();
        void onCancel();
    }

    public static WifiConfigCreationDialog newInstance() {
        WifiConfigCreationDialog dialog = new WifiConfigCreationDialog();
        // For a new config we want a password (if applicable).
        dialog.mPasswordDirty = true;
        return dialog;
    }

    public static WifiConfigCreationDialog newInstance(WifiConfiguration oldConfig,
            Listener listener) {
        WifiConfigCreationDialog dialog = new WifiConfigCreationDialog();
        dialog.mOldConfig = oldConfig;
        dialog.mListener = listener;
        // For a config to be updated we allow the current password if there is one.
        dialog.mPasswordDirty =
                // Security type is neither WPA ...
                !oldConfig.allowedKeyManagement.get(WifiConfiguration.KeyMgmt.WPA_PSK)
                // ... nor WEP.
                && !oldConfig.allowedAuthAlgorithms.get(WifiConfiguration.AuthAlgorithm.SHARED);
        return dialog;
    }

    @Override
    public Dialog onCreateDialog(Bundle savedInstanceState) {
        LayoutInflater inflater = getActivity().getLayoutInflater();

        View dialogView = inflater.inflate(R.layout.wifi_config_dialog, null);
        mPasswordView = dialogView.findViewById(R.id.password_layout);
        mSsidText = (EditText) dialogView.findViewById(R.id.ssid);
        mSsidText.addTextChangedListener(this);
        mPasswordText = (EditText) dialogView.findViewById(R.id.password);
        mPasswordText.addTextChangedListener(this);

        mSecurityChoicesSpinner = (Spinner) dialogView.findViewById(R.id.security);
        mSecurityChoicesSpinner.setOnItemSelectedListener(this);
        ArrayAdapter<CharSequence> adapter = ArrayAdapter.createFromResource(getActivity(),
                R.array.wifi_security_choices, android.R.layout.simple_spinner_item);
        adapter.setDropDownViewResource(android.R.layout.simple_spinner_dropdown_item);
        mSecurityChoicesSpinner.setAdapter(adapter);
        initialize();

        AlertDialog.Builder builder = new AlertDialog.Builder(getActivity())
                .setTitle(R.string.add_network)
                .setView(dialogView)
                // Listener for save button will be set in onStart().
                .setPositiveButton(R.string.wifi_save, null)
                .setNegativeButton(R.string.wifi_cancel, null);

        return builder.create();
    }

    @Override
    public void onStart() {
        super.onStart();
        mAlertDialog = (AlertDialog) getDialog();
        mSaveButton = mAlertDialog.getButton(DialogInterface.BUTTON_POSITIVE);
        mSaveButton.setOnClickListener(this);
        mSaveButton.setEnabled(false);
        Button cancelButton = mAlertDialog.getButton(DialogInterface.BUTTON_NEGATIVE);
        cancelButton.setOnClickListener(this);

        mAlertDialog.getWindow().setSoftInputMode(
                WindowManager.LayoutParams.SOFT_INPUT_STATE_VISIBLE);
    }

    @Override
    public void onClick(View view) {
        if (view == mSaveButton) {
            WifiConfiguration config = new WifiConfiguration();
            config.SSID = getQuotedString(mSsidText.getText().toString());
            updateConfigurationSecurity(config);
            if (mOldConfig != null) {
                config.networkId = mOldConfig.networkId;
            }
            boolean success = WifiConfigUtil.saveWifiConfiguration(getActivity(), config);
            showToast(success ? R.string.wifi_config_success : R.string.wifi_config_fail);
        }
        dismiss();
    }

    @Override
    public void onDismiss(DialogInterface dialog) {
        if (mListener != null) {
            mListener.onDismiss();
        }
    }

    @Override
    public void onCancel(DialogInterface dialog) {
        if (mListener != null) {
            mListener.onCancel();
        }
    }

    private void initialize() {
        if (mOldConfig != null) {
            mSsidText.setText(mOldConfig.SSID.replace("\"", ""));
            mPasswordText.setText("");
            if (mOldConfig.allowedKeyManagement.get(WifiConfiguration.KeyMgmt.WPA_PSK)) {
                mSecurityType = SECURITY_TYPE_WPA;
            } else if (mOldConfig.allowedAuthAlgorithms.get(
                    WifiConfiguration.AuthAlgorithm.SHARED)) {
                mSecurityType = SECURITY_TYPE_WEP;
            } else {
                mSecurityType = SECURITY_TYPE_NONE;
            }
        } else {
            mSsidText.setText("");
            mPasswordText.setText("");
            mSecurityType = SECURITY_TYPE_NONE;
        }
        mSecurityChoicesSpinner.setSelection(mSecurityType);
    }

    private void updateConfigurationSecurity(WifiConfiguration config) {
        switch (mSecurityType) {
            case SECURITY_TYPE_NONE:
                config.allowedKeyManagement.set(WifiConfiguration.KeyMgmt.NONE);
                break;
            case SECURITY_TYPE_WEP: {
                config.allowedKeyManagement.set(WifiConfiguration.KeyMgmt.NONE);
                config.allowedAuthAlgorithms.set(WifiConfiguration.AuthAlgorithm.OPEN);
                config.allowedAuthAlgorithms.set(WifiConfiguration.AuthAlgorithm.SHARED);
                int length = mPasswordText.length();
                if (length != 0) {
                    String password = mPasswordText.getText().toString();
                    // WEP-40, WEP-104, and 256-bit WEP (WEP-232?)
                    if ((length == 10 || length == 26 || length == 58)
                        && password.matches("[0-9A-Fa-f]*")) {
                        config.wepKeys[0] = password;
                    } else {
                        config.wepKeys[0] = getQuotedString(password);
                    }
                    config.wepTxKeyIndex = 0;
                }
            } break;
            case SECURITY_TYPE_WPA: {
                config.allowedKeyManagement.set(WifiConfiguration.KeyMgmt.WPA_PSK);
                int length = mPasswordText.length();
                if (length != 0) {
                    String password = mPasswordText.getText().toString();
                    if (password.matches("[0-9A-Fa-f]{64}")) {
                        config.preSharedKey = password;
                    } else {
                        config.preSharedKey = getQuotedString(password);
                    }
                }
            } break;
        }
    }

    private String getQuotedString(String string) {
        return "\"" + string + "\"";
    }

    @Override
    public void onItemSelected(AdapterView<?> parent, View view, int pos, long id) {
        if (parent.getId() == R.id.security) {
            mSecurityType = pos;
            switch (mSecurityType) {
                case SECURITY_TYPE_NONE:
                    mPasswordView.setVisibility(View.GONE);
                    break;
                case SECURITY_TYPE_WPA:
                    mPasswordText.setHint(mOldConfig == null ?
                            R.string.minimum_limit : R.string.wifi_unchanged);
                    // Fallthrough
                case SECURITY_TYPE_WEP:
                    mPasswordView.setVisibility(View.VISIBLE);
                    mPasswordText.requestFocus();
                    break;
            }
        }
        enableSaveButtonIfAppropriate();
    }

    private void enableSaveButtonIfAppropriate() {
        boolean enabled = (mSsidText.length() != 0);
        enabled = enabled && ((mSecurityType != SECURITY_TYPE_WPA) ||
                mPasswordText.length() >= 8 || (!mPasswordDirty && mOldConfig != null));
        if (mSaveButton != null) {
            mSaveButton.setEnabled(enabled);
        }
    }

    @Override
    public void onNothingSelected(AdapterView<?> parent) {
        // Do nothing.
    }

    @Override
    public void beforeTextChanged(CharSequence s, int start, int count, int after) {
        // Do nothing.
    }

    @Override
    public void onTextChanged(CharSequence s, int start, int before, int count) {
        // Do nothing.
    }

    @Override
    public void afterTextChanged(Editable s) {
        if (mPasswordText.getEditableText() == s) {
            mPasswordDirty = s.length() > 0;
        }
        enableSaveButtonIfAppropriate();
    }

    private void showToast(int msgResId) {
        Toast.makeText(getActivity(), msgResId, Toast.LENGTH_SHORT).show();
    }
}
