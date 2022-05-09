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

package com.afwsamples.testdpc.common;

import android.content.ContentResolver;
import android.net.Uri;
import android.util.Log;
import java.io.IOException;
import java.io.InputStream;
import java.security.KeyStore;
import java.security.KeyStoreException;
import java.security.NoSuchAlgorithmException;
import java.security.PrivateKey;
import java.security.UnrecoverableKeyException;
import java.security.cert.CertificateException;
import java.security.cert.X509Certificate;
import java.util.Collections;
import java.util.Enumeration;

public class CertificateUtil {
  private static final String TAG = "CertificateUtil";

  /**
   * By enumerating the entries in a pkcs12 cert, find out the first entry that contain both private
   * key and certificate.
   *
   * @param contentResolver
   * @param uri uri of pkcs12 cert
   * @param password cert password
   * @return {@link PKCS12ParseInfo} which contains alias, x509 cert and private key, null if no
   *     such an entry.
   * @throws KeyStoreException
   * @throws NoSuchAlgorithmException
   * @throws IOException
   * @throws CertificateException
   * @throws UnrecoverableKeyException
   */
  public static PKCS12ParseInfo parsePKCS12Certificate(
      ContentResolver contentResolver, Uri uri, String password)
      throws KeyStoreException, NoSuchAlgorithmException, IOException, CertificateException,
          UnrecoverableKeyException {
    InputStream inputStream = contentResolver.openInputStream(uri);
    KeyStore keystore = KeyStore.getInstance("PKCS12");
    keystore.load(inputStream, password.toCharArray());
    Enumeration<String> aliases = keystore.aliases();
    // Find an entry contains both private key and user cert.
    for (String alias : Collections.list(aliases)) {
      PrivateKey privateKey = (PrivateKey) keystore.getKey(alias, "".toCharArray());
      if (privateKey == null) {
        continue;
      }
      X509Certificate clientCertificate = (X509Certificate) keystore.getCertificate(alias);
      if (clientCertificate == null) {
        continue;
      }
      Log.d(TAG, "parsePKCS12Certificate: " + alias + " is selected");
      return new PKCS12ParseInfo(alias, clientCertificate, privateKey);
    }
    return null;
  }

  public static class PKCS12ParseInfo {
    public String alias;
    public X509Certificate certificate;
    public PrivateKey privateKey;

    public PKCS12ParseInfo(String alias, X509Certificate certificate, PrivateKey privateKey) {
      this.alias = alias;
      this.certificate = certificate;
      this.privateKey = privateKey;
    }
  }
}
