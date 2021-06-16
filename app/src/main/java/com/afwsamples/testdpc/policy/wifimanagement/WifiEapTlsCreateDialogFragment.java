package com.afwsamples.testdpc.policy.wifimanagement;

import static com.afwsamples.testdpc.common.Util.SDK_INT;

import android.app.Activity;
import android.app.AlertDialog;
import android.app.Dialog;
import android.app.DialogFragment;
import android.app.admin.DevicePolicyManager;
import android.content.ActivityNotFoundException;
import android.content.DialogInterface;
import android.content.Intent;
import android.database.Cursor;
import android.net.Uri;
import android.net.wifi.WifiConfiguration;
import android.net.wifi.WifiEnterpriseConfig;
import android.os.Build.VERSION_CODES;
import android.os.Bundle;
import android.provider.MediaStore;
import android.security.KeyChain;
import android.security.KeyChainAliasCallback;
import android.text.TextUtils;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.widget.Button;
import android.widget.EditText;
import android.widget.TextView;
import android.widget.Toast;

import com.afwsamples.testdpc.R;
import com.afwsamples.testdpc.common.CertificateUtil;

import java.io.IOException;
import java.io.InputStream;
import java.security.KeyStoreException;
import java.security.NoSuchAlgorithmException;
import java.security.UnrecoverableKeyException;
import java.security.cert.CertificateException;
import java.security.cert.CertificateFactory;
import java.security.cert.X509Certificate;

/**
 * Dialog for adding/editing EAP-TLS Wifi.
 * We currently only support CA cert in x.509 format and client cert in PKCS12 format.
 */
public class WifiEapTlsCreateDialogFragment extends DialogFragment {

    private final static int REQUEST_CA_CERT = 1;
    private final static int REQUEST_USER_CERT = 2;
    private final static String ARG_CONFIG = "config";
    private static final String TAG = "wifi_eap_tls";

    private DevicePolicyManager mDpm;

    private WifiConfiguration mWifiConfiguration;
    private Uri mCaCertUri;
    private Uri mUserCertUri;
    private String mUserCertAlias;

    private EditText mSsidEditText;
    private TextView mCaCertTextView;
    private TextView mUserCertTextView;
    private EditText mCertPasswordEditText;
    private EditText mIdentityEditText;

    public static WifiEapTlsCreateDialogFragment newInstance(WifiConfiguration config) {
        Bundle arguments = new Bundle();
        arguments.putParcelable(ARG_CONFIG, config);
        WifiEapTlsCreateDialogFragment fragment = new WifiEapTlsCreateDialogFragment();
        fragment.setArguments(arguments);
        return fragment;
    }

    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        mDpm = getActivity().getSystemService(DevicePolicyManager.class);
        mWifiConfiguration = getArguments().getParcelable(ARG_CONFIG);
        if (mWifiConfiguration == null) {
            mWifiConfiguration = new WifiConfiguration();
        }
    }

    @Override
    public Dialog onCreateDialog(Bundle savedInstanceState) {
        LayoutInflater inflater = LayoutInflater.from(getActivity());
        View rootView = inflater.inflate(R.layout.eap_tls_wifi_config_dialog, null);

        rootView.findViewById(R.id.import_ca_cert).setOnClickListener(
                new ImportButtonOnClickListener(REQUEST_CA_CERT, "application/x-x509-ca-cert"));
        rootView.findViewById(R.id.import_user_cert).setOnClickListener(
                new ImportButtonOnClickListener(REQUEST_USER_CERT, "application/x-pkcs12"));

        final Button selectUserCertButton = rootView.findViewById(R.id.select_user_cert);
        if (SDK_INT >= VERSION_CODES.S) {
            selectUserCertButton.setOnClickListener(this::onSelectClientCertClicked);
        } else {
            // KeyChain keys aren't supported.
            selectUserCertButton.setVisibility(View.GONE);
        }
        mCaCertTextView = (TextView) rootView.findViewById(R.id.selected_ca_cert);
        mUserCertTextView = (TextView) rootView.findViewById(R.id.selected_user_cert);
        mSsidEditText = (EditText) rootView.findViewById(R.id.ssid);
        mCertPasswordEditText = (EditText) rootView.findViewById(R.id.wifi_client_cert_password);
        mIdentityEditText = (EditText) rootView.findViewById(R.id.wifi_identity);
        populateUi();
        final AlertDialog dialog = new AlertDialog.Builder(getActivity())
                .setTitle(R.string.create_eap_tls_wifi_configuration)
                .setView(rootView)
                .setPositiveButton(R.string.wifi_save, null)
                .setNegativeButton(R.string.wifi_cancel, null)
                .create();
        dialog.setOnShowListener(new DialogInterface.OnShowListener() {
            @Override
            public void onShow(DialogInterface dialogInterface) {
                dialog.getButton(AlertDialog.BUTTON_POSITIVE).setOnClickListener(
                        new View.OnClickListener() {
                            @Override
                            public void onClick(View view) {
                                // Only dismiss the dialog when we saved the config.
                                if (extractInputDataAndSave()) {
                                    dialog.dismiss();
                                }
                            }
                        });
            }
        });
        return dialog;
    }

    private void onSelectClientCertClicked(View view) {
        KeyChain.choosePrivateKeyAlias(getActivity(), alias -> {
            if (alias == null) {
                // No value was chosen.
                return;
            }
            mUserCertAlias = alias;
            mUserCertUri = null;

            getActivity().runOnUiThread(() ->
                    updateSelectedCert(mUserCertTextView, /* uri= */ null, alias));
        }, /* keyTypes[] */ null, /* issuers[] */ null, /* uri */ null, /* alias */ null);
    }

    private void populateUi() {
        if (mWifiConfiguration == null) {
            return;
        }
        if (!TextUtils.isEmpty(mWifiConfiguration.SSID)) {
            mSsidEditText.setText(mWifiConfiguration.SSID.replace("\"", ""));
        }
        mIdentityEditText.setText(mWifiConfiguration.enterpriseConfig.getIdentity());
        // Both ca cert and client are not populated in the WifiConfiguration object.
        updateSelectedCert(mCaCertTextView, null, null);
        updateSelectedCert(mUserCertTextView, null, null);
    }

    private boolean extractInputDataAndSave() {
        String ssid = mSsidEditText.getText().toString();
        if (TextUtils.isEmpty(ssid)) {
            mSsidEditText.setError(getString(R.string.error_missing_ssid));
            return false;
        } else {
            mSsidEditText.setError(null);
        }

        mWifiConfiguration.SSID = ssid;
        mWifiConfiguration.allowedKeyManagement.set(WifiConfiguration.KeyMgmt.WPA_EAP);
        mWifiConfiguration.allowedKeyManagement.set(WifiConfiguration.KeyMgmt.IEEE8021X);

        mWifiConfiguration.enterpriseConfig = extractEnterpriseConfig();

        if (mWifiConfiguration.enterpriseConfig == null) {
            return false;
        }

        boolean success = WifiConfigUtil.saveWifiConfiguration(getActivity(), mWifiConfiguration);
        if (success) {
            showToast(R.string.wifi_configs_header);
            return true;
        } else {
            showToast(R.string.wifi_config_fail);
        }
        return false;
    }

    private WifiEnterpriseConfig extractEnterpriseConfig() {
        WifiEnterpriseConfig config = new WifiEnterpriseConfig();
        config.setEapMethod(WifiEnterpriseConfig.Eap.TLS);
        String identity = mIdentityEditText.getText().toString();

        if (!TextUtils.isEmpty(identity)) {
            config.setIdentity(identity);
        }

        if (mCaCertUri == null) {
            showToast(R.string.error_missing_ca_cert);
            return null;
        }
        config.setCaCertificate(parseX509Certificate(mCaCertUri));

        if (mUserCertUri != null) {
            final String certPassword = mCertPasswordEditText.getText().toString();
            CertificateUtil.PKCS12ParseInfo parseInfo = null;
            try {
                parseInfo = CertificateUtil.parsePKCS12Certificate(
                        getActivity().getContentResolver(), mUserCertUri, certPassword);
            } catch (KeyStoreException | NoSuchAlgorithmException | IOException |
                    CertificateException | UnrecoverableKeyException e) {
                Log.e(TAG, "Fail to parse the input certificate: ", e);
            }
            if (parseInfo == null) {
                showToast(R.string.error_missing_client_cert);
                return null;
            }
            config.setClientKeyEntry(parseInfo.privateKey, parseInfo.certificate);
        } else if (mUserCertAlias != null) {
            if (!mDpm.grantKeyPairToWifiAuth(mUserCertAlias)) {
                showToast(R.string.error_cannot_grant_to_wifi);
                return null;
            }
            config.setClientKeyPairAlias(mUserCertAlias);
        } else {
            showToast(R.string.error_missing_client_cert);
            return null;
        }

        return config;
    }

    /**
     * @param uri of the x509 certificate
     * @return the X509Certificate object
     */
    private X509Certificate parseX509Certificate(Uri uri) {
        try {
            CertificateFactory factory = CertificateFactory.getInstance("X.509");
            InputStream inputStream = getActivity().getContentResolver().openInputStream(uri);
            return (X509Certificate) factory.generateCertificate(inputStream);
        } catch (IOException | CertificateException ex) {
            Log.e(TAG, "parseX509Certificate: ", ex);
            return null;
        }
    }

    private class ImportButtonOnClickListener implements View.OnClickListener {
        private int mRequestCode;
        private String mMimeType;

        public ImportButtonOnClickListener(int requestCode, String mimeType) {
            mRequestCode = requestCode;
            mMimeType = mimeType;
        }

        @Override
        public void onClick(View view) {
            Intent certIntent = new Intent(Intent.ACTION_GET_CONTENT);
            certIntent.setTypeAndNormalize(mMimeType);
            try {
                startActivityForResult(certIntent, mRequestCode);
            } catch (ActivityNotFoundException e) {
                Log.e(TAG, "no file picker: ", e);
            }
        }
    }

    private void updateSelectedCert(TextView textView, Uri uri, String alias) {
        final String selectedText;
        if (uri != null) {
            String displayName = null;
            final String[] projection = {MediaStore.MediaColumns.DISPLAY_NAME};
            try (Cursor cursor = getActivity().getContentResolver().query(
                    uri, projection, null, null, null)) {
                if (cursor != null && cursor.moveToFirst()) {
                    displayName = cursor.getString(0);
                }
            }
            if (TextUtils.isEmpty(displayName)) {
                displayName = getString(R.string.wifi_unknown_cert);
            }
            selectedText = getString(R.string.selected_certificate, displayName);
        } else if (alias != null) {
            selectedText = getString(R.string.selected_keychain_certificate, alias);
        } else {
            selectedText = getString(R.string.selected_certificate_none);
        }
        textView.setText(selectedText);
    }

    private void showToast(int message) {
        Toast.makeText(getActivity(), message, Toast.LENGTH_LONG).show();
    }

    @Override
    public void onActivityResult(int requestCode, int resultCode, Intent intent) {
        if (resultCode == Activity.RESULT_OK) {
            switch (requestCode) {
                case REQUEST_CA_CERT:
                    mCaCertUri = intent.getData();
                    updateSelectedCert(mCaCertTextView, mCaCertUri, /* alias= */ null);
                    break;
                case REQUEST_USER_CERT:
                    mUserCertUri = intent.getData();
                    mUserCertAlias = null;
                    updateSelectedCert(mUserCertTextView, mUserCertUri, /* alias= */ null);
                    break;
            }
        }
    }
}
