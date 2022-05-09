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

import com.google.common.io.BaseEncoding;
import java.security.cert.CertificateParsingException;
import org.bouncycastle.asn1.ASN1Encodable;
import org.bouncycastle.asn1.ASN1Sequence;

public class RootOfTrust {
  private static final int VERIFIED_BOOT_KEY_INDEX = 0;
  private static final int DEVICE_LOCKED_INDEX = 1;
  private static final int VERIFIED_BOOT_STATE_INDEX = 2;

  public static final int KM_VERIFIED_BOOT_VERIFIED = 0;
  public static final int KM_VERIFIED_BOOT_SELF_SIGNED = 1;
  public static final int KM_VERIFIED_BOOT_UNVERIFIED = 2;
  public static final int KM_VERIFIED_BOOT_FAILED = 3;

  private final byte[] verifiedBootKey;
  private final boolean deviceLocked;
  private final int verifiedBootState;

  public RootOfTrust(ASN1Encodable asn1Encodable) throws CertificateParsingException {
    if (!(asn1Encodable instanceof ASN1Sequence)) {
      throw new CertificateParsingException(
          "Expected sequence for root of trust, found " + asn1Encodable.getClass().getName());
    }

    ASN1Sequence sequence = (ASN1Sequence) asn1Encodable;
    verifiedBootKey = Asn1Utils.getByteArrayFromAsn1(sequence.getObjectAt(VERIFIED_BOOT_KEY_INDEX));
    deviceLocked = Asn1Utils.getBooleanFromAsn1(sequence.getObjectAt(DEVICE_LOCKED_INDEX));
    verifiedBootState =
        Asn1Utils.getIntegerFromAsn1(sequence.getObjectAt(VERIFIED_BOOT_STATE_INDEX));
  }

  public static String verifiedBootStateToString(int verifiedBootState) {
    switch (verifiedBootState) {
      case KM_VERIFIED_BOOT_VERIFIED:
        return "Verified";
      case KM_VERIFIED_BOOT_SELF_SIGNED:
        return "Self-signed";
      case KM_VERIFIED_BOOT_UNVERIFIED:
        return "Unverified";
      case KM_VERIFIED_BOOT_FAILED:
        return "Failed";
      default:
        return "Unknown";
    }
  }

  public byte[] getVerifiedBootKey() {
    return verifiedBootKey;
  }

  public boolean isDeviceLocked() {
    return deviceLocked;
  }

  public int getVerifiedBootState() {
    return verifiedBootState;
  }

  @Override
  public String toString() {
    return new StringBuilder()
        .append("\nVerified boot Key: ")
        .append(BaseEncoding.base64().encode(verifiedBootKey))
        .append("\nDevice locked: ")
        .append(deviceLocked)
        .append("\nVerified boot state: ")
        .append(verifiedBootStateToString(verifiedBootState))
        .toString();
  }
}
