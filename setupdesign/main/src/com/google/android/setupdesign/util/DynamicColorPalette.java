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

package com.google.android.setupdesign.util;

import android.content.Context;
import androidx.annotation.ColorInt;
import androidx.annotation.IntDef;
import com.google.android.setupdesign.R;
import java.lang.annotation.Retention;
import java.lang.annotation.RetentionPolicy;

/** The class to get dynamic colors. */
public final class DynamicColorPalette {

  private DynamicColorPalette() {}

  /** Dynamic color category. */
  @Retention(RetentionPolicy.SOURCE)
  @IntDef({
    ColorType.ACCENT,
    ColorType.PRIMARY_TEXT,
    ColorType.SECONDARY_TEXT,
    ColorType.DISABLED_OPTION,
    ColorType.ERROR_WARNING,
    ColorType.SUCCESS_DONE,
    ColorType.FALLBACK_ACCENT,
    ColorType.BACKGROUND,
    ColorType.SURFACE,
  })
  public @interface ColorType {
    int ACCENT = 0;
    int PRIMARY_TEXT = 1;
    int SECONDARY_TEXT = 2;
    int DISABLED_OPTION = 3;
    int ERROR_WARNING = 4;
    int SUCCESS_DONE = 5;
    int FALLBACK_ACCENT = 6;
    int BACKGROUND = 7;
    int SURFACE = 8;
  }

  @ColorInt
  public static int getColor(Context context, @ColorType int dynamicColorCategory) {
    int colorRes = 0;

    switch (dynamicColorCategory) {
      case ColorType.ACCENT:
        colorRes = R.color.sud_dynamic_color_accent_glif_v3;
        break;
      case ColorType.PRIMARY_TEXT:
        colorRes = R.color.sud_system_primary_text;
        break;
      case ColorType.SECONDARY_TEXT:
        colorRes = R.color.sud_system_secondary_text;
        break;
      case ColorType.DISABLED_OPTION:
        colorRes = R.color.sud_system_tertiary_text_inactive;
        break;
      case ColorType.ERROR_WARNING:
        colorRes = R.color.sud_system_error_warning;
        break;
      case ColorType.SUCCESS_DONE:
        colorRes = R.color.sud_system_success_done;
        break;
      case ColorType.FALLBACK_ACCENT:
        colorRes = R.color.sud_system_fallback_accent;
        break;
      case ColorType.BACKGROUND:
        colorRes = R.color.sud_system_background_surface;
        break;
      case ColorType.SURFACE:
        colorRes = R.color.sud_system_surface;
        break;
        // fall out
    }

    return context.getResources().getColor(colorRes);
  }
}
