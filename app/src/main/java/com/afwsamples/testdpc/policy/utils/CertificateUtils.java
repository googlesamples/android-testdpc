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

package com.afwsamples.testdpc.policy.utils;

import org.bouncycastle.asn1.x500.X500Name;
import org.bouncycastle.asn1.x509.SubjectPublicKeyInfo;
import org.bouncycastle.cert.X509CertificateHolder;
import org.bouncycastle.cert.X509v3CertificateBuilder;
import org.bouncycastle.operator.OperatorCreationException;
import org.bouncycastle.operator.jcajce.JcaContentSignerBuilder;
import java.io.ByteArrayInputStream;
import java.io.IOException;
import java.math.BigInteger;
import java.security.KeyPair;
import java.security.SecureRandom;
import java.security.cert.CertificateException;
import java.security.cert.CertificateFactory;
import java.security.cert.X509Certificate;
import java.util.Date;
import javax.security.auth.x500.X500Principal;

public class CertificateUtils {
    /** Creates a self-signed X.509 certificate, given a key pair, subject and issuer. */
    public static X509Certificate createCertificate(
            KeyPair keyPair, X500Principal subject, X500Principal issuer)
            throws OperatorCreationException, CertificateException, IOException {
        // Make the certificate valid for two days.
        long millisPerDay = 24 * 60 * 60 * 1000;
        long now = System.currentTimeMillis();
        Date start = new Date(now - millisPerDay);
        Date end = new Date(now + millisPerDay);

        // Assign a random serial number.
        byte[] serialBytes = new byte[16];
        new SecureRandom().nextBytes(serialBytes);
        BigInteger serialNumber = new BigInteger(1, serialBytes);

        // Create the certificate builder
        X509v3CertificateBuilder x509cg =
                new X509v3CertificateBuilder(
                        X500Name.getInstance(issuer.getEncoded()),
                        serialNumber,
                        start,
                        end,
                        X500Name.getInstance(subject.getEncoded()),
                        SubjectPublicKeyInfo.getInstance(keyPair.getPublic().getEncoded()));

        // Choose a signature algorithm matching the key format.
        String keyAlgorithm = keyPair.getPrivate().getAlgorithm();
        String signatureAlgorithm;
        if (keyAlgorithm.equals("RSA")) {
            signatureAlgorithm = "SHA256withRSA";
        } else if (keyAlgorithm.equals("EC")) {
            signatureAlgorithm = "SHA256withECDSA";
        } else {
            throw new IllegalArgumentException("Unknown key algorithm " + keyAlgorithm);
        }

        // Sign the certificate and generate it.
        X509CertificateHolder x509holder =
                x509cg.build(
                        new JcaContentSignerBuilder(signatureAlgorithm)
                                .build(keyPair.getPrivate()));
        CertificateFactory certFactory = CertificateFactory.getInstance("X.509");
        X509Certificate x509c =
                (X509Certificate)
                        certFactory.generateCertificate(
                                new ByteArrayInputStream(x509holder.getEncoded()));
        return x509c;
    }
}
