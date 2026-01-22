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

package com.google.android.setupcompat.logging;

import static com.google.android.setupcompat.internal.Validations.assertLengthInRange;

import android.annotation.TargetApi;
import android.os.Build;
import android.os.Build.VERSION_CODES;
import android.os.Bundle;
import android.os.Parcel;
import android.os.Parcelable;
import android.os.PersistableBundle;
import androidx.annotation.NonNull;
import androidx.annotation.VisibleForTesting;
import com.google.android.setupcompat.internal.ClockProvider;
import com.google.android.setupcompat.internal.PersistableBundles;
import com.google.android.setupcompat.internal.Preconditions;
import com.google.android.setupcompat.util.ObjectUtils;

/**
 * This class represents a interesting event at a particular point in time. The event is identified
 * by {@link MetricKey} along with {@code timestamp}. It can include additional key-value pairs
 * providing more attributes associated with the given event. Only primitive values are supported
 * for now (int, long, double, float, String).
 */
@TargetApi(VERSION_CODES.Q)
public final class CustomEvent implements Parcelable {
  private static final String BUNDLE_KEY_TIMESTAMP = "CustomEvent_timestamp";
  private static final String BUNDLE_KEY_METRICKEY = "CustomEvent_metricKey";
  private static final String BUNDLE_KEY_BUNDLE_VALUES = "CustomEvent_bundleValues";
  private static final String BUNDLE_KEY_BUNDLE_PII_VALUES = "CustomEvent_pii_bundleValues";
  private static final String BUNDLE_VERSION = "CustomEvent_version";
  private static final int VERSION = 1;

  /** Creates a new instance of {@code CustomEvent}. Null arguments are not allowed. */
  public static CustomEvent create(
      MetricKey metricKey, PersistableBundle bundle, PersistableBundle piiValues) {
    Preconditions.checkArgument(
        Build.VERSION.SDK_INT >= Build.VERSION_CODES.Q,
        "The constructor only support on sdk Q or higher");
    return new CustomEvent(
        ClockProvider.timeInMillis(),
        metricKey,
        // Assert only in factory methods since these methods are directly used by API consumers
        // while constructor is used directly only when data is de-serialized from bundle (which
        // might have been sent by a client using a newer API)
        PersistableBundles.assertIsValid(bundle),
        PersistableBundles.assertIsValid(piiValues));
  }

  /** Creates a new instance of {@code CustomEvent}. Null arguments are not allowed. */
  public static CustomEvent create(MetricKey metricKey, PersistableBundle bundle) {
    Preconditions.checkArgument(
        Build.VERSION.SDK_INT >= Build.VERSION_CODES.Q,
        "The constructor only support on sdk Q or higher.");
    return create(metricKey, bundle, PersistableBundle.EMPTY);
  }

  /** Converts {@link Bundle} into {@link CustomEvent}. */
  public static CustomEvent toCustomEvent(Bundle bundle) {
    return new CustomEvent(
        bundle.getLong(BUNDLE_KEY_TIMESTAMP, /* defaultValue= */ Long.MIN_VALUE),
        MetricKey.toMetricKey(bundle.getBundle(BUNDLE_KEY_METRICKEY)),
        PersistableBundles.fromBundle(bundle.getBundle(BUNDLE_KEY_BUNDLE_VALUES)),
        PersistableBundles.fromBundle(bundle.getBundle(BUNDLE_KEY_BUNDLE_PII_VALUES)));
  }

  /** Converts {@link CustomEvent} into {@link Bundle}. */
  public static Bundle toBundle(CustomEvent customEvent) {
    Preconditions.checkNotNull(customEvent, "CustomEvent cannot be null");
    Bundle bundle = new Bundle();
    bundle.putInt(BUNDLE_VERSION, VERSION);
    bundle.putLong(BUNDLE_KEY_TIMESTAMP, customEvent.timestampMillis());
    bundle.putBundle(BUNDLE_KEY_METRICKEY, MetricKey.fromMetricKey(customEvent.metricKey()));
    bundle.putBundle(BUNDLE_KEY_BUNDLE_VALUES, PersistableBundles.toBundle(customEvent.values()));
    bundle.putBundle(
        BUNDLE_KEY_BUNDLE_PII_VALUES, PersistableBundles.toBundle(customEvent.piiValues()));
    return bundle;
  }

  public static final Creator<CustomEvent> CREATOR =
      new Creator<CustomEvent>() {
        @Override
        public CustomEvent createFromParcel(Parcel in) {
          return new CustomEvent(
              in.readLong(),
              in.readParcelable(MetricKey.class.getClassLoader()),
              in.readPersistableBundle(MetricKey.class.getClassLoader()),
              in.readPersistableBundle(MetricKey.class.getClassLoader()));
        }

        @Override
        public CustomEvent[] newArray(int size) {
          return new CustomEvent[size];
        }
      };

  /** Returns the timestamp of when the event occurred. */
  public long timestampMillis() {
    return timestampMillis;
  }

  /** Returns the identifier of the event. */
  public MetricKey metricKey() {
    return this.metricKey;
  }

  /** Returns the non PII values describing the event. Only primitive values are supported. */
  public PersistableBundle values() {
    return new PersistableBundle(this.persistableBundle);
  }

  /**
   * Returns the PII(Personally identifiable information) values describing the event. These values
   * will not be included in the aggregated logs. Only primitive values are supported.
   */
  public PersistableBundle piiValues() {
    return this.piiValues;
  }

  @Override
  public int describeContents() {
    return 0;
  }

  @Override
  public void writeToParcel(Parcel parcel, int i) {
    parcel.writeLong(timestampMillis);
    parcel.writeParcelable(metricKey, i);
    parcel.writePersistableBundle(persistableBundle);
    parcel.writePersistableBundle(piiValues);
  }

  @Override
  public boolean equals(Object o) {
    if (this == o) {
      return true;
    }
    if (!(o instanceof CustomEvent)) {
      return false;
    }
    CustomEvent that = (CustomEvent) o;
    return timestampMillis == that.timestampMillis
        && ObjectUtils.equals(metricKey, that.metricKey)
        && PersistableBundles.equals(persistableBundle, that.persistableBundle)
        && PersistableBundles.equals(piiValues, that.piiValues);
  }

  @Override
  public int hashCode() {
    return ObjectUtils.hashCode(timestampMillis, metricKey, persistableBundle, piiValues);
  }

  private CustomEvent(
      long timestampMillis,
      MetricKey metricKey,
      PersistableBundle bundle,
      PersistableBundle piiValues) {
    Preconditions.checkArgument(timestampMillis >= 0, "Timestamp cannot be negative.");
    Preconditions.checkNotNull(metricKey, "MetricKey cannot be null.");
    Preconditions.checkNotNull(bundle, "Bundle cannot be null.");
    Preconditions.checkArgument(!bundle.isEmpty(), "Bundle cannot be empty.");
    Preconditions.checkNotNull(piiValues, "piiValues cannot be null.");
    assertPersistableBundleIsValid(bundle);
    this.timestampMillis = timestampMillis;
    this.metricKey = metricKey;
    this.persistableBundle = new PersistableBundle(bundle);
    this.piiValues = new PersistableBundle(piiValues);
  }

  private final long timestampMillis;
  private final MetricKey metricKey;
  private final PersistableBundle persistableBundle;
  private final PersistableBundle piiValues;

  private static void assertPersistableBundleIsValid(PersistableBundle bundle) {
    for (String key : bundle.keySet()) {
      assertLengthInRange(key, "bundle key", MIN_BUNDLE_KEY_LENGTH, MAX_STR_LENGTH);
      Object value = bundle.get(key);
      if (value instanceof String) {
        Preconditions.checkArgument(
            ((String) value).length() <= MAX_STR_LENGTH,
            String.format(
                "Maximum length of string value for key='%s' cannot exceed %s.",
                key, MAX_STR_LENGTH));
      }
    }
  }

  /**
   * Trims the string longer than {@code MAX_STR_LENGTH} character, only keep the first {@code
   * MAX_STR_LENGTH} - 1 characters and attached … in the end.
   */
  @NonNull
  public static String trimsStringOverMaxLength(@NonNull String str) {
    if (str.length() <= MAX_STR_LENGTH) {
      return str;
    } else {
      return String.format("%s…", str.substring(0, MAX_STR_LENGTH - 1));
    }
  }

  @VisibleForTesting static final int MAX_STR_LENGTH = 50;
  @VisibleForTesting static final int MIN_BUNDLE_KEY_LENGTH = 3;
}
