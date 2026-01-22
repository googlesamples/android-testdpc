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

import android.app.Activity;
import android.os.Bundle;
import android.os.Parcel;
import android.os.Parcelable;
import androidx.annotation.NonNull;
import com.google.android.setupcompat.internal.Preconditions;
import com.google.android.setupcompat.util.ObjectUtils;
import java.util.regex.Pattern;

/**
 * A metric key represents a validated “string key” and a "screen name" that is associated with the
 * values reported by the API consumer.
 */
public final class MetricKey implements Parcelable {

  private static final String METRIC_KEY_BUNDLE_NAME_KEY = "MetricKey_name";
  private static final String METRIC_KEY_BUNDLE_SCREEN_NAME_KEY = "MetricKey_screenName";
  private static final String METRIC_KEY_BUNDLE_VERSION = "MetricKey_version";
  private static final int VERSION = 1;

  /**
   * Creates a new instance of MetricKey.
   *
   * @param name metric name to identify what we log
   * @param activity activity of metric screen, uses to generate screenName
   */
  public static MetricKey get(@NonNull String name, @NonNull Activity activity) {
    String screenName = activity.getComponentName().getClassName();
    assertLengthInRange(name, "MetricKey.name", MIN_METRIC_KEY_LENGTH, MAX_METRIC_KEY_LENGTH);
    Preconditions.checkArgument(
        METRIC_KEY_PATTERN.matcher(name).matches(),
        "Invalid MetricKey, only alpha numeric characters are allowed.");
    return new MetricKey(name, screenName);
  }

  /**
   * Creates a new instance of MetricKey.
   *
   * <p>NOTE:
   *
   * <ul>
   *   <li>Length of {@code name} should be in range of 5-30 characters, only alpha numeric
   *       characters are allowed.
   *   <li>Length of {@code screenName} should be in range of 5-50 characters, only alpha numeric
   *       characters are allowed.
   * </ul>
   */
  public static MetricKey get(@NonNull String name, @NonNull String screenName) {
    // We only checked the length of customized screen name, by the reason if the screenName match
    // to the class name skip check it
    if (!SCREEN_COMPONENTNAME_PATTERN.matcher(screenName).matches()) {
      assertLengthInRange(
          screenName, "MetricKey.screenName", MIN_SCREEN_NAME_LENGTH, MAX_SCREEN_NAME_LENGTH);
      Preconditions.checkArgument(
          SCREEN_NAME_PATTERN.matcher(screenName).matches(),
          "Invalid ScreenName, only alpha numeric characters are allowed.");
    }

    assertLengthInRange(name, "MetricKey.name", MIN_METRIC_KEY_LENGTH, MAX_METRIC_KEY_LENGTH);
    Preconditions.checkArgument(
        METRIC_KEY_PATTERN.matcher(name).matches(),
        "Invalid MetricKey, only alpha numeric characters are allowed.");

    return new MetricKey(name, screenName);
  }

  /** Converts {@link MetricKey} into {@link Bundle}. */
  public static Bundle fromMetricKey(MetricKey metricKey) {
    Preconditions.checkNotNull(metricKey, "MetricKey cannot be null.");
    Bundle bundle = new Bundle();
    bundle.putInt(METRIC_KEY_BUNDLE_VERSION, VERSION);
    bundle.putString(METRIC_KEY_BUNDLE_NAME_KEY, metricKey.name());
    bundle.putString(METRIC_KEY_BUNDLE_SCREEN_NAME_KEY, metricKey.screenName());
    return bundle;
  }

  /** Converts {@link Bundle} into {@link MetricKey}. */
  public static MetricKey toMetricKey(Bundle bundle) {
    Preconditions.checkNotNull(bundle, "Bundle cannot be null");
    return MetricKey.get(
        bundle.getString(METRIC_KEY_BUNDLE_NAME_KEY),
        bundle.getString(METRIC_KEY_BUNDLE_SCREEN_NAME_KEY));
  }

  public static final Creator<MetricKey> CREATOR =
      new Creator<MetricKey>() {
        @Override
        public MetricKey createFromParcel(Parcel in) {
          return new MetricKey(in.readString(), in.readString());
        }

        @Override
        public MetricKey[] newArray(int size) {
          return new MetricKey[size];
        }
      };

  /** Returns the name of the metric key. */
  public String name() {
    return name;
  }

  /** Returns the name of the metric key. */
  public String screenName() {
    return screenName;
  }

  @Override
  public int describeContents() {
    return 0;
  }

  @Override
  public void writeToParcel(Parcel parcel, int i) {
    parcel.writeString(name);
    parcel.writeString(screenName);
  }

  @Override
  public boolean equals(Object o) {
    if (this == o) {
      return true;
    }
    if (!(o instanceof MetricKey)) {
      return false;
    }
    MetricKey metricKey = (MetricKey) o;
    return ObjectUtils.equals(name, metricKey.name)
        && ObjectUtils.equals(screenName, metricKey.screenName);
  }

  @Override
  public int hashCode() {
    return ObjectUtils.hashCode(name, screenName);
  }

  private MetricKey(String name, String screenName) {
    this.name = name;
    this.screenName = screenName;
  }

  private final String name;
  private final String screenName;

  private static final int MIN_SCREEN_NAME_LENGTH = 5;
  private static final int MIN_METRIC_KEY_LENGTH = 5;
  private static final int MAX_SCREEN_NAME_LENGTH = 50;
  private static final int MAX_METRIC_KEY_LENGTH = 30;
  private static final Pattern METRIC_KEY_PATTERN = Pattern.compile("^[a-zA-Z][a-zA-Z0-9_]+");
  private static final Pattern SCREEN_COMPONENTNAME_PATTERN =
      Pattern.compile("^([a-z]+[.])+[A-Z][a-zA-Z0-9]+");
  private static final Pattern SCREEN_NAME_PATTERN = Pattern.compile("^[a-zA-Z][a-zA-Z0-9_]+");
}
