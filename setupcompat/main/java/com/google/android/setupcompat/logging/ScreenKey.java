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

import static com.google.android.setupcompat.internal.Validations.assertLengthInRange;

import android.content.Context;
import android.os.Bundle;
import android.os.Parcel;
import android.os.Parcelable;
import androidx.annotation.NonNull;
import androidx.annotation.VisibleForTesting;
import com.google.android.setupcompat.internal.Preconditions;
import com.google.android.setupcompat.util.ObjectUtils;
import java.util.regex.Pattern;

/**
 * A screen key represents a validated “string key” that is associated with the values reported by
 * the API consumer.
 */
public class ScreenKey implements Parcelable {

  @VisibleForTesting(otherwise = VisibleForTesting.PACKAGE_PRIVATE)
  public static final String SCREEN_KEY_BUNDLE_NAME_KEY = "ScreenKey_name";
  @VisibleForTesting(otherwise = VisibleForTesting.PACKAGE_PRIVATE)
  public static final String SCREEN_KEY_BUNDLE_PACKAGE_KEY = "ScreenKey_package";
  @VisibleForTesting(otherwise = VisibleForTesting.PACKAGE_PRIVATE)
  public static final String SCREEN_KEY_BUNDLE_VERSION_KEY = "ScreenKey_version";
  private static final int INVALID_VERSION = -1;
  private static final int VERSION = 1;

  /**
   * Creates a new instance of {@link ScreenKey}.
   *
   * @param name screen name to identify what the metric belongs to. It should be in the range of
   * 5-50 characters, only alphanumeric characters are allowed.
   * @param context context associated to metric screen, uses to generate package name.
   */
  public static ScreenKey of(@NonNull String name, @NonNull Context context) {
    Preconditions.checkNotNull(context, "Context can not be null.");
    return ScreenKey.of(name, context.getPackageName());
  }

  private static ScreenKey of(@NonNull String name, @NonNull String packageName) {
    Preconditions.checkArgument(
        SCREEN_PACKAGENAME_PATTERN.matcher(packageName).matches(),
        "Invalid ScreenKey#package, only alpha numeric characters are allowed.");
    assertLengthInRange(
        name, "ScreenKey.name", MIN_SCREEN_NAME_LENGTH, MAX_SCREEN_NAME_LENGTH);
    Preconditions.checkArgument(
        SCREEN_NAME_PATTERN.matcher(name).matches(),
        "Invalid ScreenKey#name, only alpha numeric characters are allowed.");

    return new ScreenKey(name, packageName);
  }

  /**
   * Converts {@link ScreenKey} into {@link Bundle}.
   * Throw {@link NullPointerException} if the screenKey is null.
   */
  public static Bundle toBundle(ScreenKey screenKey) {
    Preconditions.checkNotNull(screenKey, "ScreenKey cannot be null.");
    Bundle bundle = new Bundle();
    bundle.putInt(SCREEN_KEY_BUNDLE_VERSION_KEY, VERSION);
    bundle.putString(SCREEN_KEY_BUNDLE_NAME_KEY, screenKey.getName());
    bundle.putString(SCREEN_KEY_BUNDLE_PACKAGE_KEY, screenKey.getPackageName());
    return bundle;
  }

  /**
   * Converts {@link Bundle} into {@link ScreenKey}.
   * Throw {@link NullPointerException} if the bundle is null.
   * Throw {@link IllegalArgumentException} if the bundle version is unsupported.
   */
  public static ScreenKey fromBundle(Bundle bundle) {
    Preconditions.checkNotNull(bundle, "Bundle cannot be null");

    int version = bundle.getInt(SCREEN_KEY_BUNDLE_VERSION_KEY, INVALID_VERSION);
    if (version == 1) {
      return ScreenKey.of(
          bundle.getString(SCREEN_KEY_BUNDLE_NAME_KEY),
          bundle.getString(SCREEN_KEY_BUNDLE_PACKAGE_KEY));
    } else {
      // Invalid version
      throw new IllegalArgumentException("Unsupported version: " + version);
    }
  }

  public static final Creator<ScreenKey> CREATOR =
      new Creator<>() {
        @Override
        public ScreenKey createFromParcel(Parcel in) {
          return new ScreenKey(in.readString(), in.readString());
        }

        @Override
        public ScreenKey[] newArray(int size) {
          return new ScreenKey[size];
        }
      };

  /** Returns the name of the screen key. */
  public String getName() {
    return name;
  }

  /** Returns the package name of the screen key. */
  public String getPackageName() {
    return packageName;
  }

  @Override
  public int describeContents() {
    return 0;
  }

  @Override
  public void writeToParcel(Parcel parcel, int i) {
    parcel.writeString(name);
    parcel.writeString(packageName);
  }

  @Override
  public boolean equals(Object o) {
    if (this == o) {
      return true;
    }
    if (!(o instanceof ScreenKey)) {
      return false;
    }
    ScreenKey screenKey = (ScreenKey) o;
    return ObjectUtils.equals(name, screenKey.name)
        && ObjectUtils.equals(packageName, screenKey.packageName);
  }

  @Override
  public int hashCode() {
    return ObjectUtils.hashCode(name, packageName);
  }

  @NonNull
  @Override
  public String toString() {
    return "ScreenKey {name="
        + getName()
        + ", package="
        + getPackageName()
        + "}";
  }

  private ScreenKey(String name, String packageName) {
    this.name = name;
    this.packageName = packageName;
  }

  private final String name;
  private final String packageName;

  private static final int MIN_SCREEN_NAME_LENGTH = 5;
  private static final int MAX_SCREEN_NAME_LENGTH = 50;
  private static final Pattern SCREEN_NAME_PATTERN = Pattern.compile("^[a-zA-Z][a-zA-Z0-9_]+");
  private static final Pattern SCREEN_PACKAGENAME_PATTERN =
      Pattern.compile("^([a-z]+[.])+[a-zA-Z][a-zA-Z0-9]+");
}
