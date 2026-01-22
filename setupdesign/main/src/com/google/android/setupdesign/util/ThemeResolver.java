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

package com.google.android.setupdesign.util;

import android.app.Activity;
import android.content.Intent;
import androidx.annotation.Nullable;
import androidx.annotation.StyleRes;
import com.google.android.setupcompat.util.WizardManagerHelper;
import com.google.android.setupdesign.R;

/**
 * A resolver to resolve the theme from a string or an activity intent, setting options like the
 * default theme and the oldest supported theme. Apps can share the resolver across the entire
 * process by calling {@link #setDefault(ThemeResolver)} in {@link
 * android.app.Application#onCreate()}. If an app needs more granular sharing of the theme default
 * values, additional instances of {@link ThemeResolver} can be created using the builder.
 */
public class ThemeResolver {

  @StyleRes private final int defaultTheme;
  @Nullable private final String oldestSupportedTheme;
  private final boolean useDayNight;
  @Nullable private final ThemeSupplier defaultThemeSupplier;

  @Nullable private static ThemeResolver defaultResolver;

  /**
   * Sets the default instance used for the whole process. Can be null to reset the default to the
   * preset one.
   */
  public static void setDefault(@Nullable ThemeResolver resolver) {
    defaultResolver = resolver;
  }

  /**
   * Returns the default instance, which can be changed using {@link #setDefault(ThemeResolver)}.
   */
  public static ThemeResolver getDefault() {
    if (defaultResolver == null) {
      defaultResolver =
          new ThemeResolver.Builder()
              .setDefaultTheme(R.style.SudThemeGlif_DayNight)
              .setUseDayNight(true)
              .build();
    }
    return defaultResolver;
  }

  private ThemeResolver(
      int defaultTheme,
      @Nullable String oldestSupportedTheme,
      @Nullable ThemeSupplier defaultThemeSupplier,
      boolean useDayNight) {
    this.defaultTheme = defaultTheme;
    this.oldestSupportedTheme = oldestSupportedTheme;
    this.defaultThemeSupplier = defaultThemeSupplier;
    this.useDayNight = useDayNight;
  }

  /**
   * Returns the style for the theme specified in the intent extra. If the specified string theme is
   * older than the oldest supported theme, the default will be returned instead. Note that the
   * default theme is returned without processing -- it may not be a DayNight theme even if {@link
   * #useDayNight} is true.
   */
  @StyleRes
  public int resolve(Intent intent) {
    return resolve(
        intent.getStringExtra(WizardManagerHelper.EXTRA_THEME),
        /* suppressDayNight= */ WizardManagerHelper.isAnySetupWizard(intent));
  }

  /**
   * Returns the style for the given SetupWizard intent. If the specified intent does not include
   * the intent extra {@link WizardManagerHelper#EXTRA_THEME}, the default theme will be returned
   * instead. Note that the default theme is returned without processing -- it may not be a DayNight
   * theme even if {@link #useDayNight} is true.
   */
  @StyleRes
  public int resolve(Intent intent, boolean suppressDayNight) {
    return resolve(intent.getStringExtra(WizardManagerHelper.EXTRA_THEME), suppressDayNight);
  }

  /**
   * Returns the style for the given string theme. If the specified string theme is older than the
   * oldest supported theme, the default will be returned instead. Note that the default theme is
   * returned without processing -- it may not be a DayNight theme even if {@link #useDayNight} is
   * true.
   *
   * @deprecated Use {@link #resolve(String, boolean)} instead
   */
  @Deprecated
  @StyleRes
  public int resolve(@Nullable String theme) {
    return resolve(theme, /* suppressDayNight= */ false);
  }

  /**
   * Returns the style for the given string theme. If the specified string theme is older than the
   * oldest supported theme, the default will be returned instead. Note that the default theme is
   * returned without processing -- it may not be a DayNight theme even if {@link #useDayNight} is
   * true.
   */
  @StyleRes
  public int resolve(@Nullable String theme, boolean suppressDayNight) {
    int themeResource =
        useDayNight && !suppressDayNight ? getDayNightThemeRes(theme) : getThemeRes(theme);
    if (themeResource == 0) {
      if (defaultThemeSupplier != null) {
        theme = defaultThemeSupplier.getTheme();
        themeResource =
            useDayNight && !suppressDayNight ? getDayNightThemeRes(theme) : getThemeRes(theme);
      }
      if (themeResource == 0) {
        return defaultTheme;
      }
    }

    if (oldestSupportedTheme != null && compareThemes(theme, oldestSupportedTheme) < 0) {
      return defaultTheme;
    }
    return themeResource;
  }

  /** Reads the theme from the intent, and applies the resolved theme to the activity. */
  public void applyTheme(Activity activity) {
    activity.setTheme(
        resolve(
            activity.getIntent(),
            /* suppressDayNight= */ WizardManagerHelper.isAnySetupWizard(activity.getIntent())
                && !ThemeHelper.isSetupWizardDayNightEnabled(activity)));
  }

  /**
   * Returns the corresponding DayNight theme resource ID for the given string theme. DayNight
   * themes are themes that will be either light or dark depending on the system setting. For
   * example, the string {@link ThemeHelper#THEME_GLIF_LIGHT} will return
   * {@code @style/SudThemeGlif.DayNight}.
   */
  @StyleRes
  private static int getDayNightThemeRes(@Nullable String theme) {
    if (theme != null) {
      switch (theme) {
        case ThemeHelper.THEME_GLIF_V4_LIGHT:
        case ThemeHelper.THEME_GLIF_V4:
          return R.style.SudThemeGlifV4_DayNight;
        case ThemeHelper.THEME_GLIF_V3_LIGHT:
        case ThemeHelper.THEME_GLIF_V3:
          return R.style.SudThemeGlifV3_DayNight;
        case ThemeHelper.THEME_GLIF_V2_LIGHT:
        case ThemeHelper.THEME_GLIF_V2:
          return R.style.SudThemeGlifV2_DayNight;
        case ThemeHelper.THEME_GLIF_LIGHT:
        case ThemeHelper.THEME_GLIF:
          return R.style.SudThemeGlif_DayNight;
        case ThemeHelper.THEME_MATERIAL_LIGHT:
        case ThemeHelper.THEME_MATERIAL:
          return R.style.SudThemeMaterial_DayNight;
        default:
          // fall through
      }
    }
    return 0;
  }

  /**
   * Returns the theme resource ID for the given string theme. For example, the string {@link
   * ThemeHelper#THEME_GLIF_LIGHT} will return {@code @style/SudThemeGlif.Light}.
   */
  @StyleRes
  private static int getThemeRes(@Nullable String theme) {
    if (theme != null) {
      switch (theme) {
        case ThemeHelper.THEME_GLIF_V4_LIGHT:
          return R.style.SudThemeGlifV4_Light;
        case ThemeHelper.THEME_GLIF_V4:
          return R.style.SudThemeGlifV4;
        case ThemeHelper.THEME_GLIF_V3_LIGHT:
          return R.style.SudThemeGlifV3_Light;
        case ThemeHelper.THEME_GLIF_V3:
          return R.style.SudThemeGlifV3;
        case ThemeHelper.THEME_GLIF_V2_LIGHT:
          return R.style.SudThemeGlifV2_Light;
        case ThemeHelper.THEME_GLIF_V2:
          return R.style.SudThemeGlifV2;
        case ThemeHelper.THEME_GLIF_LIGHT:
          return R.style.SudThemeGlif_Light;
        case ThemeHelper.THEME_GLIF:
          return R.style.SudThemeGlif;
        case ThemeHelper.THEME_MATERIAL_LIGHT:
          return R.style.SudThemeMaterial_Light;
        case ThemeHelper.THEME_MATERIAL:
          return R.style.SudThemeMaterial;
        default:
          // fall through
      }
    }
    return 0;
  }

  /** Compares whether the versions of {@code theme1} and {@code theme2} to check which is newer. */
  private static int compareThemes(String theme1, String theme2) {
    return Integer.valueOf(getThemeVersion(theme1)).compareTo(getThemeVersion(theme2));
  }

  /**
   * Returns the version of the theme. The absolute number of the theme version is not defined, but
   * a larger number in the version indicates a newer theme.
   */
  private static int getThemeVersion(String theme) {
    if (theme != null) {
      switch (theme) {
        case ThemeHelper.THEME_GLIF_V4_LIGHT:
        case ThemeHelper.THEME_GLIF_V4:
          return 5;
        case ThemeHelper.THEME_GLIF_V3_LIGHT:
        case ThemeHelper.THEME_GLIF_V3:
          return 4;
        case ThemeHelper.THEME_GLIF_V2_LIGHT:
        case ThemeHelper.THEME_GLIF_V2:
          return 3;
        case ThemeHelper.THEME_GLIF_LIGHT:
        case ThemeHelper.THEME_GLIF:
          return 2;
        case ThemeHelper.THEME_MATERIAL_LIGHT:
        case ThemeHelper.THEME_MATERIAL:
          return 1;
        default:
          // fall through
      }
    }
    return -1;
  }

  /** Builder class for {@link ThemeResolver}. */
  public static class Builder {
    private ThemeSupplier defaultThemeSupplier;
    @StyleRes private int defaultTheme = R.style.SudThemeGlif_DayNight;
    @Nullable private String oldestSupportedTheme = null;
    private boolean useDayNight = true;

    public Builder() {}

    public Builder(ThemeResolver themeResolver) {
      this.defaultTheme = themeResolver.defaultTheme;
      this.oldestSupportedTheme = themeResolver.oldestSupportedTheme;
      this.useDayNight = themeResolver.useDayNight;
    }

    public Builder setDefaultThemeSupplier(ThemeSupplier defaultThemeSupplier) {
      this.defaultThemeSupplier = defaultThemeSupplier;
      return this;
    }

    public Builder setDefaultTheme(@StyleRes int defaultTheme) {
      this.defaultTheme = defaultTheme;
      return this;
    }

    public Builder setOldestSupportedTheme(String oldestSupportedTheme) {
      this.oldestSupportedTheme = oldestSupportedTheme;
      return this;
    }

    public Builder setUseDayNight(boolean useDayNight) {
      this.useDayNight = useDayNight;
      return this;
    }

    public ThemeResolver build() {
      return new ThemeResolver(
          defaultTheme, oldestSupportedTheme, defaultThemeSupplier, useDayNight);
    }
  }

  public interface ThemeSupplier {
    String getTheme();
  }
}
