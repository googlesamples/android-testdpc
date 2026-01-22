/*
 * Copyright (C) 2022 The Android Open Source Project
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

import android.annotation.TargetApi;
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
import com.google.android.setupcompat.logging.internal.SetupMetricsLoggingConstants.EventType;
import com.google.android.setupcompat.util.ObjectUtils;

/**
 * This class represents a setup metric event at a particular point in time.
 * The event is identified by {@link EventType} along with a string name. It can include
 * additional key-value pairs providing more attributes associated with the given event. Only
 * primitive values are supported for now (int, long, boolean, String).
 */
@TargetApi(VERSION_CODES.Q)
public class SetupMetric implements Parcelable {
  @VisibleForTesting(otherwise = VisibleForTesting.PACKAGE_PRIVATE)
  public static final String SETUP_METRIC_BUNDLE_VERSION_KEY = "SetupMetric_version";
  @VisibleForTesting(otherwise = VisibleForTesting.PACKAGE_PRIVATE)
  public static final String SETUP_METRIC_BUNDLE_NAME_KEY = "SetupMetric_name";
  @VisibleForTesting(otherwise = VisibleForTesting.PACKAGE_PRIVATE)
  public static final String SETUP_METRIC_BUNDLE_TYPE_KEY = "SetupMetric_type";
  @VisibleForTesting(otherwise = VisibleForTesting.PACKAGE_PRIVATE)
  public static final String SETUP_METRIC_BUNDLE_VALUES_KEY = "SetupMetric_values";
  private static final int VERSION = 1;
  private static final int INVALID_VERSION = -1;

  public static final String SETUP_METRIC_BUNDLE_OPTIN_KEY = "opt_in";
  public static final String SETUP_METRIC_BUNDLE_ERROR_KEY = "error";
  public static final String SETUP_METRIC_BUNDLE_TIMESTAMP_KEY = "timestamp";


  /**
   * A convenient function to create a setup event with event type {@link EventType#IMPRESSION}
   * @param name A name represents this impression
   * @return A {@link SetupMetric}
   * @throws IllegalArgumentException if the {@code name} is empty.
   */
  @NonNull
  public static SetupMetric ofImpression(@NonNull String name) {
    Bundle bundle = new Bundle();
    bundle.putLong(SETUP_METRIC_BUNDLE_TIMESTAMP_KEY, ClockProvider.timeInMillis());
    return new SetupMetric(VERSION, name, EventType.IMPRESSION,
        PersistableBundles.fromBundle(bundle));
  }

  /**
   * A convenient function to create a setup event with event type {@link EventType#OPT_IN}
   * @param name A name represents this opt-in
   * @param status Opt-in status in {@code true} or {@code false}
   * @return A {@link SetupMetric}
   * @throws IllegalArgumentException if the {@code name} is empty.
   */
  @NonNull
  public static SetupMetric ofOptIn(@NonNull String name, boolean status) {
    Bundle bundle = new Bundle();
    bundle.putBoolean(SETUP_METRIC_BUNDLE_OPTIN_KEY, status);
    bundle.putLong(SETUP_METRIC_BUNDLE_TIMESTAMP_KEY, ClockProvider.timeInMillis());
    return new SetupMetric(VERSION, name, EventType.OPT_IN, PersistableBundles.fromBundle(bundle));
  }

  /**
   * A convenient function to create a setup event with event type
   * {@link EventType#WAITING_START}
   * @param name A task name causes this waiting duration
   * @return A {@link SetupMetric}
   * @throws IllegalArgumentException if the {@code name} is empty.
   */
  @NonNull
  public static SetupMetric ofWaitingStart(@NonNull String name) {
    Bundle bundle = new Bundle();
    bundle.putLong(SETUP_METRIC_BUNDLE_TIMESTAMP_KEY, ClockProvider.timeInMillis());
    return new SetupMetric(VERSION, name, EventType.WAITING_START,
        PersistableBundles.fromBundle(bundle));
  }

  /**
   * A convenient function to create a setup event with event type
   * {@link EventType#WAITING_END}
   * @param name A task name causes this waiting duration
   * @return A {@link SetupMetric}
   * @throws IllegalArgumentException if the {@code name} is empty.
   */
  @NonNull
  public static SetupMetric ofWaitingEnd(@NonNull String name) {
    Bundle bundle = new Bundle();
    bundle.putLong(SETUP_METRIC_BUNDLE_TIMESTAMP_KEY, ClockProvider.timeInMillis());
    return new SetupMetric(VERSION, name, EventType.WAITING_END,
        PersistableBundles.fromBundle(bundle));
  }

  /**
   * A convenient function to create a setup event with event type {@link EventType#ERROR}
   * @param name A name represents this error
   * @param errorCode A error code
   * @return A {@link SetupMetric}
   * @throws IllegalArgumentException if the {@code name} is empty.
   */
  @NonNull
  public static SetupMetric ofError(@NonNull String name, int errorCode) {
    Bundle bundle = new Bundle();
    bundle.putInt(SETUP_METRIC_BUNDLE_ERROR_KEY, errorCode);
    bundle.putLong(SETUP_METRIC_BUNDLE_TIMESTAMP_KEY, ClockProvider.timeInMillis());
    return new SetupMetric(VERSION, name, EventType.ERROR, PersistableBundles.fromBundle(bundle));
  }

  /** Converts {@link SetupMetric} into {@link Bundle}. */
  @NonNull
  public static Bundle toBundle(@NonNull SetupMetric setupMetric) {
    Preconditions.checkNotNull(setupMetric, "SetupMetric cannot be null.");
    Bundle bundle = new Bundle();
    bundle.putInt(SETUP_METRIC_BUNDLE_VERSION_KEY, VERSION);
    bundle.putString(SETUP_METRIC_BUNDLE_NAME_KEY, setupMetric.name);
    bundle.putInt(SETUP_METRIC_BUNDLE_TYPE_KEY, setupMetric.type);
    bundle.putBundle(
        SETUP_METRIC_BUNDLE_VALUES_KEY, PersistableBundles.toBundle(setupMetric.values));
    return bundle;
  }

  /**
   * Converts {@link Bundle} into {@link SetupMetric}.
   * Throw {@link IllegalArgumentException} if the bundle version is unsupported.
   */
  @NonNull
  public static SetupMetric fromBundle(@NonNull Bundle bundle) {
    Preconditions.checkNotNull(bundle, "Bundle cannot be null");
    int version = bundle.getInt(SETUP_METRIC_BUNDLE_VERSION_KEY, INVALID_VERSION);
    if (version == 1) {
      return new SetupMetric(
          bundle.getInt(SETUP_METRIC_BUNDLE_VERSION_KEY),
          bundle.getString(SETUP_METRIC_BUNDLE_NAME_KEY),
          bundle.getInt(SETUP_METRIC_BUNDLE_TYPE_KEY),
          PersistableBundles.fromBundle(bundle.getBundle(SETUP_METRIC_BUNDLE_VALUES_KEY)));
    } else {
      throw new IllegalArgumentException("Unsupported version: " + version);
    }
  }

  private SetupMetric(
      int version, String name, @EventType int type, @NonNull PersistableBundle values) {
    Preconditions.checkArgument(
        name != null && name.length() != 0,
        "name cannot be null or empty.");
    this.version = version;
    this.name = name;
    this.type = type;
    this.values = values;
  }

  private final int version;
  private final String name;
  @EventType private final int type;
  private final PersistableBundle values;

  public int getVersion() {
    return version;
  }

  public String getName() {
    return name;
  }

  @EventType
  public int getType() {
    return type;
  }

  public PersistableBundle getValues() {
    return values;
  }

  public static final Creator<SetupMetric> CREATOR =
      new Creator<>() {
        @Override
        public SetupMetric createFromParcel(@NonNull Parcel in) {
          return new SetupMetric(in.readInt(),
              in.readString(),
              in.readInt(),
              in.readPersistableBundle(SetupMetric.class.getClassLoader()));
        }

        @Override
        public SetupMetric[] newArray(int size) {
          return new SetupMetric[size];
        }
      };

  @Override
  public int describeContents() {
    return 0;
  }

  @Override
  public void writeToParcel(Parcel parcel, int flags) {
    parcel.writeString(name);
    parcel.writeInt(type);
    parcel.writePersistableBundle(values);
  }

  @Override
  public boolean equals(Object o) {
    if (this == o) {
      return true;
    }
    if (!(o instanceof SetupMetric)) {
      return false;
    }
    SetupMetric that = (SetupMetric) o;
    return ObjectUtils.equals(name, that.name)
        && ObjectUtils.equals(type, that.type)
        && PersistableBundles.equals(values, that.values);
  }

  @Override
  public int hashCode() {
    return ObjectUtils.hashCode(name, type, values);
  }

  @NonNull
  @Override
  public String toString() {
    return "SetupMetric {name="
        + getName()
        + ", type="
        + getType()
        + ", bundle="
        + getValues().toString()
        + "}";
  }
}
