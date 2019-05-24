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

import android.content.Context;
import android.os.AsyncTask;
import android.security.KeyChain;
import android.security.KeyChainException;
import android.security.keystore.KeyProperties;
import android.util.Log;

import com.afwsamples.testdpc.R;

import java.security.InvalidKeyException;
import java.security.NoSuchAlgorithmException;
import java.security.PrivateKey;
import java.security.PublicKey;
import java.security.Signature;
import java.security.SignatureException;
import java.security.cert.X509Certificate;

public class SignAndVerifyTask extends AsyncTask<String, Integer, String> {
    public static final String TAG = "PolicyManagement";
    private Context mContext;
    private ShowToastCallback mCallback;

    public SignAndVerifyTask(Context context, ShowToastCallback callback) {
        mContext = context;
        mCallback = callback;
    }

    @Override
    protected String doInBackground(String... aliases) {
        String alias = aliases[0];
        try {
            PrivateKey privateKey = KeyChain.getPrivateKey(mContext, alias);

            final String algorithmIdentifier;
            if (privateKey.getAlgorithm().equals(KeyProperties.KEY_ALGORITHM_RSA)) {
                algorithmIdentifier = "SHA256withRSA";
            } else {
                algorithmIdentifier = "SHA256withECDSA";
            }

            byte[] data = new String("hello").getBytes();
            Signature signer = Signature.getInstance(algorithmIdentifier);
            signer.initSign(privateKey);
            signer.update(data);
            byte[] signature = signer.sign();

            X509Certificate cert = KeyChain.getCertificateChain(mContext, alias)[0];
            PublicKey publicKey = cert.getPublicKey();
            Signature verifier = Signature.getInstance(algorithmIdentifier);
            verifier.initVerify(publicKey);
            verifier.update(data);
            if (verifier.verify(signature)) {
                return cert.getSubjectX500Principal().getName();
            }
        } catch (KeyChainException e) {
            Log.e(TAG, "Error getting key", e);
        } catch (InterruptedException e) {
            Log.e(TAG, "Interrupted while getting the key", e);
        } catch (NoSuchAlgorithmException e) {
            Log.e(TAG, "Algorithm not supported", e);
        } catch (SignatureException e) {
            Log.e(TAG, "Failed signing with key", e);
        } catch (InvalidKeyException e) {
            Log.e(TAG, "Provided alias resolves to an invalid key", e);
        }
        return null;
    }

    @Override
    protected void onPostExecute(String result) {
        if (result != null) {
            mCallback.showToast(R.string.key_usage_successful, result);
        } else {
            mCallback.showToast(R.string.key_usage_failed);
        }
    }
};
