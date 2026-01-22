/*
 * Copyright 2018 The Android Open Source Project
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *     http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */

package com.google.android.setupcompat.partnerconfig;

import android.content.Context;
import android.content.pm.PackageManager;
import android.content.pm.PackageManager.NameNotFoundException;
import android.content.res.Resources;
import android.os.Build;
import android.os.Build.VERSION_CODES;
import android.os.Bundle;
import android.util.Log;
import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.annotation.VisibleForTesting;

/**
 * A potentially cross-package resource entry, which can then be retrieved using {@link
 * PackageManager#getResourcesForApplication}. This class can also be sent across to other packages
 * on IPC via the Bundle representation.
 */
public final class ResourceEntry {

  private static final String TAG = ResourceEntry.class.getSimpleName();

  @VisibleForTesting static final String KEY_FALLBACK_CONFIG = "fallbackConfig";

  @VisibleForTesting static final String KEY_PACKAGE_NAME = "packageName";
  @VisibleForTesting static final String KEY_RESOURCE_NAME = "resourceName";
  @VisibleForTesting static final String KEY_RESOURCE_ID = "resourceId";

  private final String packageName;
  private final String resourceName;
  private final int resourceId;

  /**
   * The {@link Resources} for accessing a specific package's resources. This is {@code null} only
   * if the deprecated constructor {@link #ResourceEntry(String, String, int)} is used.
   */
  @Nullable private final Resources resources;

  /**
   * Creates a {@code ResourceEntry} object from a provided bundle or the fallback resource if
   * partner resource not found and the {@code fallbackConfig} key exists in provided bundle.
   * Returns {@code null} if fallback package is not found or the {@code bundle} doesn't contain
   * packageName, resourceName, or resourceId.
   *
   * @param context the context need to retrieve the {@link Resources}
   * @param bundle the source bundle needs to have all the information for a {@code ResourceEntry}
   */
  @Nullable
  public static ResourceEntry fromBundle(@NonNull Context context, Bundle bundle) {
    String packageName;
    String resourceName;
    int resourceId;
    if (!bundle.containsKey(KEY_PACKAGE_NAME)
        || !bundle.containsKey(KEY_RESOURCE_NAME)
        || !bundle.containsKey(KEY_RESOURCE_ID)) {
      return null;
    }
    packageName = bundle.getString(KEY_PACKAGE_NAME);
    resourceName = bundle.getString(KEY_RESOURCE_NAME);
    resourceId = bundle.getInt(KEY_RESOURCE_ID);
    try {
      return new ResourceEntry(
          packageName, resourceName, resourceId, getResourcesByPackageName(context, packageName));
    } catch (NameNotFoundException e) {
      Bundle fallbackBundle = bundle.getBundle(KEY_FALLBACK_CONFIG);
      if (fallbackBundle != null) {
        Log.w(TAG, packageName + " not found, " + resourceName + " fallback to default value");
        return fromBundle(context, fallbackBundle);
      }
    }
    return null;
  }

  /** @deprecated Use {@link #ResourceEntry(String, String, int, Resources)} instead. */
  @Deprecated
  public ResourceEntry(String packageName, String resourceName, int resourceId) {
    this(packageName, resourceName, resourceId, /* resources= */ null);
  }

  public ResourceEntry(
      String packageName, String resourceName, int resourceId, @Nullable Resources resources) {
    this.packageName = packageName;
    this.resourceName = resourceName;
    this.resourceId = resourceId;
    this.resources = resources;
  }

  public String getPackageName() {
    return this.packageName;
  }

  public String getResourceName() {
    return this.resourceName;
  }

  public int getResourceId() {
    return this.resourceId;
  }

  /**
   * Returns a {@link Resources} for accessing specific package's resources. It will be {@code null}
   * when the {@link #ResourceEntry(String, String, int)} is used).
   */
  public Resources getResources() {
    return resources;
  }

  /**
   * Returns a bundle representation of this resource entry, which can then be sent over IPC.
   *
   * @see #fromBundle(Context, Bundle)
   */
  public Bundle toBundle() {
    Bundle result = new Bundle();
    result.putString(KEY_PACKAGE_NAME, packageName);
    result.putString(KEY_RESOURCE_NAME, resourceName);
    result.putInt(KEY_RESOURCE_ID, resourceId);
    return result;
  }

  private static Resources getResourcesByPackageName(Context context, String packageName)
      throws NameNotFoundException {
    PackageManager manager = context.getPackageManager();
    if (Build.VERSION.SDK_INT >= VERSION_CODES.N) {
      return manager.getResourcesForApplication(
          manager.getApplicationInfo(packageName, PackageManager.MATCH_DISABLED_COMPONENTS));
    } else {
      return manager.getResourcesForApplication(
          manager.getApplicationInfo(packageName, PackageManager.GET_DISABLED_COMPONENTS));
    }
  }
}
