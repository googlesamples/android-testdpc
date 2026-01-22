/*
 * Copyright (C) 2021 The Android Open Source Project
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

package com.google.android.setupcompat.util;

import android.os.Build;
import androidx.annotation.ChecksSdkIntAtLeast;

/**
 * An util class to check whether the current OS version is higher or equal to sdk version of
 * device.
 */
public final class BuildCompatUtils {

  private static final int VANILLA_ICE_CREAM = 35;

  /**
   * Implementation of BuildCompat.isAtLeastR() suitable for use in Setup
   *
   * @return Whether the current OS version is higher or equal to R.
   */
  @ChecksSdkIntAtLeast(api = Build.VERSION_CODES.R)
  public static boolean isAtLeastR() {
    return Build.VERSION.SDK_INT >= Build.VERSION_CODES.R;
  }

  /**
   * Implementation of BuildCompat.isAtLeastS() suitable for use in Setup
   *
   * @return Whether the current OS version is higher or equal to S.
   */
  @ChecksSdkIntAtLeast(api = Build.VERSION_CODES.S)
  public static boolean isAtLeastS() {
    return Build.VERSION.SDK_INT >= Build.VERSION_CODES.S;
  }

  /**
   * Implementation of BuildCompat.isAtLeastT() suitable for use in Setup
   *
   * @return Whether the current OS version is higher or equal to T.
   */
  @ChecksSdkIntAtLeast(api = Build.VERSION_CODES.TIRAMISU)
  public static boolean isAtLeastT() {
    return Build.VERSION.SDK_INT >= Build.VERSION_CODES.TIRAMISU;
  }

  /**
   * Implementation of BuildCompat.isAtLeastU() suitable for use in Setup
   *
   * @return Whether the current OS version is higher or equal to U.
   */
  @ChecksSdkIntAtLeast(api = Build.VERSION_CODES.UPSIDE_DOWN_CAKE)
  public static boolean isAtLeastU() {
    return Build.VERSION.SDK_INT >= Build.VERSION_CODES.UPSIDE_DOWN_CAKE;
  }

  /**
   * Implementation of BuildCompat.isAtLeastV() suitable for use in Setup
   *
   * @return Whether the current OS version is higher or equal to V.
   */
  @ChecksSdkIntAtLeast(api = VANILLA_ICE_CREAM)
  public static boolean isAtLeastV() {
    return Build.VERSION.SDK_INT >= VANILLA_ICE_CREAM;
  }

  private BuildCompatUtils() {}
}
