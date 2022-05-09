/*
 * Copyright (C) 2018 The Android Open Source Project
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
package com.afwsamples.testdpc.policy.keymanagement;

import android.annotation.SuppressLint;
import android.annotation.TargetApi;
import android.app.Activity;
import android.app.AlertDialog;
import android.app.admin.DevicePolicyManager;
import android.content.ComponentName;
import android.content.Context;
import android.os.AsyncTask;
import android.os.Build.VERSION_CODES;
import android.security.AttestedKeyPair;
import android.security.keystore.KeyGenParameterSpec;
import android.security.keystore.KeyProperties;
import android.security.keystore.StrongBoxUnavailableException;
import android.util.Base64;
import android.util.Log;
import android.view.View;
import android.widget.TextView;
import android.widget.Toast;
import com.afwsamples.testdpc.R;
import com.afwsamples.testdpc.policy.utils.Attestation;
import com.afwsamples.testdpc.policy.utils.AuthorizationList;
import com.afwsamples.testdpc.policy.utils.CertificateUtils;
import java.io.IOException;
import java.security.cert.Certificate;
import java.security.cert.CertificateException;
import java.security.cert.CertificateParsingException;
import java.security.cert.X509Certificate;
import java.util.ArrayList;
import java.util.List;
import javax.security.auth.x500.X500Principal;
import org.bouncycastle.operator.OperatorCreationException;

public class GenerateKeyAndCertificateTask extends AsyncTask<Void, Integer, AttestedKeyPair> {
  public static final String TAG = "PolicyManagement";

  final String mAlias;
  final boolean mIsUserSelectable;
  private final byte[] mAttestationChallenge;
  private final int mIdAttestationFlags;
  private final boolean mUseStrongBox;
  private final boolean mGenerateEcKey;
  private final ComponentName mAdminComponentName;
  private final DevicePolicyManager mDevicePolicyManager;
  private final Activity mActivity;

  public GenerateKeyAndCertificateTask(
      KeyGenerationParameters params, Activity activity, ComponentName admin) {
    mAlias = params.alias;
    mIsUserSelectable = params.isUserSelectable;
    mAttestationChallenge = params.attestationChallenge;
    mIdAttestationFlags = params.idAttestationFlags;
    mUseStrongBox = params.useStrongBox;
    mGenerateEcKey = params.generateEcKey;
    mActivity = activity;
    mAdminComponentName = admin;
    mDevicePolicyManager =
        (DevicePolicyManager) activity.getSystemService(Context.DEVICE_POLICY_SERVICE);
  }

  @TargetApi(VERSION_CODES.P)
  @Override
  protected AttestedKeyPair doInBackground(Void... voids) {
    try {
      KeyGenParameterSpec.Builder keySpecBuilder =
          new KeyGenParameterSpec.Builder(
                  mAlias, KeyProperties.PURPOSE_SIGN | KeyProperties.PURPOSE_VERIFY)
              .setDigests(KeyProperties.DIGEST_SHA256)
              .setIsStrongBoxBacked(mUseStrongBox);

      if (mAttestationChallenge != null) {
        keySpecBuilder.setAttestationChallenge(mAttestationChallenge);
      }

      if (mGenerateEcKey) {
        keySpecBuilder.setKeySize(256);
      } else {
        // RSA key
        keySpecBuilder
            .setSignaturePaddings(
                KeyProperties.SIGNATURE_PADDING_RSA_PSS, KeyProperties.SIGNATURE_PADDING_RSA_PKCS1)
            .setKeySize(2048);
      }

      String keyAlgorithm;
      if (mGenerateEcKey) {
        keyAlgorithm = KeyProperties.KEY_ALGORITHM_EC;
      } else {
        keyAlgorithm = KeyProperties.KEY_ALGORITHM_RSA;
      }

      KeyGenParameterSpec keySpec = keySpecBuilder.build();
      AttestedKeyPair keyPair =
          mDevicePolicyManager.generateKeyPair(
              mAdminComponentName, keyAlgorithm, keySpec, mIdAttestationFlags);

      if (keyPair == null) {
        return null;
      }

      List<Certificate> attestationRecord = keyPair.getAttestationRecord();
      if (attestationRecord != null) {
        Log.i(TAG, "Attestation record:");
        for (Certificate cert : attestationRecord) {
          Log.i(TAG, Base64.encodeToString(cert.getEncoded(), Base64.NO_WRAP));
        }
        Log.i(TAG, "End of attestation record.");
      }

      X500Principal subject = new X500Principal("CN=TestDPC, O=Android, C=US");
      // Self-signed certificate: Same subject and issuer.
      X509Certificate selfSigned =
          CertificateUtils.createCertificate(keyPair.getKeyPair(), subject, subject);

      List<Certificate> certs = new ArrayList<Certificate>();
      certs.add(selfSigned);

      if (!mDevicePolicyManager.setKeyPairCertificate(
          mAdminComponentName, mAlias, certs, mIsUserSelectable)) {
        return null;
      }

      return keyPair;
    } catch (CertificateException | OperatorCreationException | IOException e) {
      Log.e(TAG, "Failed to create certificate", e);
    } catch (StrongBoxUnavailableException e) {
      Log.e(TAG, "StrongBox unavailable", e);
    } catch (SecurityException e) {
      Log.e(TAG, "Not permitted to generate key", e);
    }

    return null;
  }

  @Override
  protected void onPostExecute(AttestedKeyPair keyPair) {
    if (keyPair != null) {
      showToast(R.string.key_generation_successful, mAlias);
      showKeyGenerationResult(keyPair);
    } else {
      showToast(R.string.key_generation_failed, mAlias);
    }
  }

  private void showToast(int msgId, String extra) {
    if (mActivity.isFinishing()) {
      return;
    }

    Toast.makeText(
            mActivity, mActivity.getResources().getString(msgId) + " " + extra, Toast.LENGTH_SHORT)
        .show();
  }

  @TargetApi(VERSION_CODES.P)
  @SuppressLint("SetTextI18n")
  private void showKeyGenerationResult(AttestedKeyPair keyPair) {
    if (mActivity == null || mActivity.isFinishing() || keyPair == null) {
      return;
    }
    View keyGenResultView =
        mActivity.getLayoutInflater().inflate(R.layout.key_generation_result, null);

    List<Certificate> attestationChain = keyPair.getAttestationRecord();
    TextView attestationDetailsView = keyGenResultView.findViewById(R.id.attestation_details);

    if ((attestationChain != null) && (attestationChain.size() > 0)) {
      try {
        StringBuilder attestationDetails = new StringBuilder();

        Attestation attestationRecord =
            new Attestation((X509Certificate) keyPair.getAttestationRecord().get(0));
        attestationDetails.append(
            mActivity.getText(R.string.attestation_challenge_description) + "\n");
        attestationDetails.append(new String(attestationRecord.getAttestationChallenge()) + "\n");
        AuthorizationList teeList = attestationRecord.getTeeEnforced();
        if (teeList != null) {
          attestationDetails.append(
              mActivity.getText(R.string.device_serial_number_description) + "\n");
          attestationDetails.append(teeList.getSerialNumber() + "\n");
          attestationDetails.append(mActivity.getText(R.string.device_imei_description) + "\n");
          attestationDetails.append(teeList.getImei() + "\n");
          attestationDetails.append(mActivity.getText(R.string.device_meid_description) + "\n");
          attestationDetails.append(teeList.getMeid() + "\n");
          attestationDetails.append("Individual Attestation:" + "\n");
          attestationDetails.append(teeList.isIndividualAttestation() + "\n");
        }

        Certificate root = attestationChain.get(attestationChain.size() - 1);

        attestationDetails.append(
            String.format(
                "%s: %d\n",
                mActivity.getText(R.string.attestation_chain_length_description),
                keyPair.getAttestationRecord().size()));

        attestationDetails.append(
            String.format(
                "%s\n%s\n",
                mActivity.getText(R.string.attestation_root_description),
                ((X509Certificate) root).getSubjectX500Principal().getName()));

        attestationDetailsView.setText(attestationDetails);
      } catch (CertificateParsingException e) {
        Log.e(TAG, "Failed parsing attestation record", e);
        attestationDetailsView.setText("<INVALID>");
      }
    } else {
      attestationDetailsView.setText("<none>");
    }

    new AlertDialog.Builder(mActivity)
        .setTitle(R.string.key_generation_successful)
        .setView(keyGenResultView)
        .setPositiveButton(android.R.string.ok, null)
        .show();
  }
}
