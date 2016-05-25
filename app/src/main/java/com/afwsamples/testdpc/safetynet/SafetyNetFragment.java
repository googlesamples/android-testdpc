/*
 * Copyright 2016 The Android Open Source Project
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

package com.afwsamples.testdpc.safetynet;

import android.app.AlertDialog;
import android.app.Dialog;
import android.app.DialogFragment;
import android.content.Context;
import android.net.ConnectivityManager;
import android.net.NetworkInfo;
import android.os.Bundle;
import android.support.annotation.ColorInt;
import android.support.annotation.NonNull;
import android.support.annotation.Nullable;
import android.support.v4.content.ContextCompat;
import android.util.Base64;
import android.view.LayoutInflater;
import android.view.View;
import android.widget.TextView;

import com.afwsamples.testdpc.R;
import com.google.android.gms.common.ConnectionResult;
import com.google.android.gms.common.api.GoogleApiClient;
import com.google.android.gms.common.api.ResultCallbacks;
import com.google.android.gms.common.api.Status;
import com.google.android.gms.safetynet.SafetyNet;

import org.json.JSONException;
import org.json.JSONObject;

import java.security.SecureRandom;

import static com.google.android.gms.safetynet.SafetyNetApi.AttestationResult;

/**
 * Demonstrate how to use SafetyNet API to check device compatibility.
 * Please notice that you should verifying the payload in your server.
 * For more details, please check http://developer.android.com/training/safetynet/index.html.
 */
public class SafetyNetFragment extends DialogFragment implements
        GoogleApiClient.ConnectionCallbacks,
        GoogleApiClient.OnConnectionFailedListener {
    private GoogleApiClient mGoogleApiClient;
    private TextView mMessageView;
    private @ColorInt int BLACK, DARK_RED;

    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        // Kick start the checking
        mGoogleApiClient = buildGoogleApiClient();
    }

    @Override
    public void onStart() {
        super.onStart();
        updateMessageView(R.string.safetynet_running, false);
        mGoogleApiClient.connect();
    }

    @Override
    public void onStop() {
        super.onStop();
        mGoogleApiClient.disconnect();
    }

    @Override
    public Dialog onCreateDialog(Bundle savedInstanceState) {
        BLACK = ContextCompat.getColor(getActivity(), R.color.text_black);
        DARK_RED = ContextCompat.getColor(getActivity(), R.color.dark_red);
        LayoutInflater inflater = LayoutInflater.from(getActivity());
        View rootView = inflater.inflate(R.layout.safety_net_attest_dialog, null);
        mMessageView = (TextView) rootView.findViewById(R.id.message_view);

        return new AlertDialog.Builder(getActivity())
                .setView(rootView)
                .setTitle(R.string.safetynet_dialog_title)
                .setNeutralButton(android.R.string.ok, null)
                .create();
    }

    @Override
    public void onConnected(@Nullable Bundle bundle) {
        if (hasInternetConnection()) {
            runSaftyNetTest();
        } else {
            updateMessageView(R.string.safetynet_fail_reason_no_internet, true);
        }
    }

    @Override
    public void onConnectionSuspended(int i) {
        updateMessageView(R.string.cancel_safetynet_msg, true);
    }

    @Override
    public void onConnectionFailed(@NonNull ConnectionResult connectionResult) {
        if (connectionResult.getErrorCode() == ConnectionResult.SERVICE_VERSION_UPDATE_REQUIRED) {
            updateMessageView(R.string.safetynet_fail_reason_gmscore_upgrade, true);
        } else {
            updateMessageView(getString(R.string.safetynet_fail_reason_error_code,
                    connectionResult.getErrorCode()), true);
        }
    }

    private GoogleApiClient buildGoogleApiClient() {
        return new GoogleApiClient.Builder(getActivity())
                .addApi(SafetyNet.API)
                .addConnectionCallbacks(this)
                .addOnConnectionFailedListener(this)
                .build();
    }

    /**
     * For simplicity, we generate the nonce in the client. However, it should be generated on the
     * server for anti-replay protection.
     */
    private byte[] generateNonce() {
        byte[] nonce = new byte[32];
        SecureRandom secureRandom = new SecureRandom();
        secureRandom.nextBytes(nonce);
        return nonce;
    }

    private void runSaftyNetTest() {
        final byte[] nonce = generateNonce();
        SafetyNet.SafetyNetApi.attest(mGoogleApiClient, nonce)
                .setResultCallback(new ResultCallbacks<AttestationResult>() {
                    @Override
                    public void onSuccess(@NonNull AttestationResult attestationResult) {
                        if (isDetached()) {
                            return;
                        }
                        final String jws = attestationResult.getJwsResult();
                        try {
                            final JSONObject jsonObject = retrievePayloadFromJws(jws);
                            final String jsonString = jsonObject.toString(4);
                            final String verifyOnServerString
                                    = getString(R.string.safetynet_verify_on_server);
                            updateMessageView(verifyOnServerString + "\n" + jsonString, false);
                        } catch (JSONException ex) {
                            updateMessageView(R.string.safetynet_fail_reason_invalid_jws, true);
                        }
                    }

                    @Override
                    public void onFailure(@NonNull Status status) {
                        if (isDetached()) {
                            return;
                        }
                        updateMessageView(R.string.safetynet_fail_to_run_api, true);
                    }
                });
    }

    private void updateMessageView(int message, boolean isError) {
        updateMessageView(getString(message), isError);
    }

    private void updateMessageView(String message, boolean isError) {
        mMessageView.setText(message);
        mMessageView.setTextColor((isError) ? DARK_RED : BLACK);
    }

    private boolean hasInternetConnection() {
        ConnectivityManager cm =
                (ConnectivityManager) getActivity().getSystemService(Context.CONNECTIVITY_SERVICE);
        NetworkInfo activeNetwork = cm.getActiveNetworkInfo();
        return activeNetwork != null && activeNetwork.isConnected();
    }

    private static JSONObject retrievePayloadFromJws(String jws) throws JSONException {
        String[] parts = jws.split("\\.");
        if (parts.length != 3) {
            throw new JSONException("Invalid JWS");
        }
        return new JSONObject(new String(Base64.decode(parts[1], Base64.URL_SAFE)));
    }
}
