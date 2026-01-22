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

package com.google.android.setupcompat.internal;

import android.app.Activity;
import android.os.Bundle;
import androidx.annotation.StringDef;
import com.google.android.setupcompat.internal.FocusChangedMetricHelper.Constants.ExtraKey;
import java.lang.annotation.Retention;
import java.lang.annotation.RetentionPolicy;

/**
 * A help class response to generate extra bundle and capture screen name for interruption metric.
 */
public class FocusChangedMetricHelper {
  private FocusChangedMetricHelper() {}

  public static final String getScreenName(Activity activity) {
    return activity.getComponentName().toShortString();
  }

  public static final Bundle getExtraBundle(
      Activity activity, TemplateLayout layout, boolean hasFocus) {
    Bundle bundle = new Bundle();

    bundle.putString(ExtraKey.PACKAGE_NAME, activity.getComponentName().getPackageName());
    bundle.putString(ExtraKey.SCREEN_NAME, activity.getComponentName().getShortClassName());
    bundle.putInt(ExtraKey.HASH_CODE, layout.hashCode());
    bundle.putBoolean(ExtraKey.HAS_FOCUS, hasFocus);
    bundle.putLong(ExtraKey.TIME_IN_MILLIS, System.currentTimeMillis());

    return bundle;
  }

  /**
   * Constant values used by {@link
   * com.google.android.setupcompat.internal.FocusChangedMetricHelper}.
   */
  public static final class Constants {
    @Retention(RetentionPolicy.SOURCE)
    @StringDef({
      ExtraKey.PACKAGE_NAME,
      ExtraKey.SCREEN_NAME,
      ExtraKey.HASH_CODE,
      ExtraKey.HAS_FOCUS,
      ExtraKey.TIME_IN_MILLIS
    })
    public @interface ExtraKey {

      /** This key will be used to save the package name. */
      String PACKAGE_NAME = "packageName";

      /** This key will be used to save the activity name. */
      String SCREEN_NAME = "screenName";

      /**
       * This key will be used to save the has code of {@link
       * com.google.android.setupcompat.PartnerCustomizationLayout}.
       */
      String HASH_CODE = "hash";

      /**
       * This key will be used to save whether the window which is including the {@link
       * com.google.android.setupcompat.PartnerCustomizationLayout}. has focus or not.
       */
      String HAS_FOCUS = "focus";

      /** This key will be use to save the time stamp in milliseconds. */
      String TIME_IN_MILLIS = "timeInMillis";
    }

    private Constants() {}
  }
}
