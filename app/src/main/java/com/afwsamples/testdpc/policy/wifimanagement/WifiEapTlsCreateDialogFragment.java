package com.afwsamples.testdpc.policy.wifimanagement;

import android.app.Activity;
import android.app.AlertDialog;
import android.app.Dialog;
import android.app.DialogFragment;
import android.content.ActivityNotFoundException;
import android.content.DialogInterface;
import android.content.Intent;
import android.database.Cursor;
import android.net.Uri;
import android.net.wifi.WifiConfiguration;
import android.net.wifi.WifiEnterpriseConfig;
import android.os.Bundle;
import android.provider.MediaStore;
import android.text.TextUtils;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.widget.EditText;
import android.widget.TextView;
import android.widget.Toast;

import com.afwsamples.testdpc.R;
import com.afwsamples.testdpc.common.CertificateUtil;

import java.io.IOException;
import java.io.InputStream;
import java.security.KeyStoreException;
import java.security.NoSuchAlgorithmException;
import java.security.PrivateKey;
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

    private WifiConfiguration mWifiConfiguration;
    private Uri mCaCertUri;
    private Uri mUserCertUri;

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

    private void populateUi() {
        if (mWifiConfiguration == null) {
            return;
        }
        if (!TextUtils.isEmpty(mWifiConfiguration.SSID)) {
            mSsidEditText.setText(mWifiConfiguration.SSID.replace("\"", ""));
        }
        mIdentityEditText.setText(mWifiConfiguration.enterpriseConfig.getIdentity());
        // Both ca cert and client are not populated in the WifiConfiguration object.
        updateSelectedCert(mCaCertTextView, null);
        updateSelectedCert(mUserCertTextView, null);
    }

    private boolean extractInputDataAndSave() {
        String ssid = mSsidEditText.getText().toString();
        if (TextUtils.isEmpty(ssid)) {
            mSsidEditText.setError(getString(R.string.error_missing_ssid));
            return false;
        } else {
            mSsidEditText.setError(null);
        }
        if (mCaCertUri == null) {
            showToast(R.string.error_missing_ca_cert);
            return false;
        }
        if (mUserCertUri == null) {
            showToast(R.string.error_missing_client_cert);
            return false;
        }
        X509Certificate caCert = parseX509Certificate(mCaCertUri);
        String certPassword = mCertPasswordEditText.getText().toString();
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
            return false;
        }
        String identity = mIdentityEditText.getText().toString();
        boolean success = saveWifiConfiguration(ssid, caCert, parseInfo.privateKey,
                parseInfo.certificate, identity);
        if (success) {
            showToast(R.string.wifi_configs_header);
            return true;
        } else {
            showToast(R.string.wifi_config_fail);
        }
        return false;
    }

    private boolean saveWifiConfiguration(String ssid, X509Certificate caCert,
            PrivateKey privateKey, X509Certificate userCert, String identity) {
        mWifiConfiguration.SSID = ssid;
        mWifiConfiguration.allowedKeyManagement.set(WifiConfiguration.KeyMgmt.WPA_EAP);
        mWifiConfiguration.allowedKeyManagement.set(WifiConfiguration.KeyMgmt.IEEE8021X);
        WifiEnterpriseConfig enterpriseConfig = new WifiEnterpriseConfig();
        enterpriseConfig.setEapMethod(WifiEnterpriseConfig.Eap.TLS);
        enterpriseConfig.setCaCertificate(caCert);
        enterpriseConfig.setClientKeyEntry(privateKey, userCert);
        if (!TextUtils.isEmpty(identity)) {
            enterpriseConfig.setIdentity(identity);
        }
        mWifiConfiguration.enterpriseConfig = enterpriseConfig;
        return WifiConfigUtil.saveWifiConfiguration(getActivity(), mWifiConfiguration);
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

    private void updateSelectedCert(TextView textView, Uri uri) {
        String displayName = null;
        if (uri == null) {
            displayName = getString(R.string.selected_certificate_none);
        } else {
            final String[] projection = {MediaStore.MediaColumns.DISPLAY_NAME};
            Cursor cursor = getActivity().getContentResolver().query(uri, projection,
                    null, null, null);
            if (cursor != null) {
                try {
                    if (cursor.moveToFirst()) {
                        displayName = cursor.getString(0);
                    }
                } finally {
                    cursor.close();
                }
            }
            if (TextUtils.isEmpty(getString(R.string.wifi_unknown_cert))) {
                displayName = getString(R.string.wifi_unknown_cert);
            }
        }
        String selectedText = getString(R.string.selected_certificate, displayName);
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
                    updateSelectedCert(mCaCertTextView, mCaCertUri);
                    break;
                case REQUEST_USER_CERT:
                    mUserCertUri = intent.getData();
                    updateSelectedCert(mUserCertTextView, mUserCertUri);
                    break;
            }
        }
    }
}
