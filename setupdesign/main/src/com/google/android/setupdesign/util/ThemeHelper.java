/*
 * Copyright (C) 2015 The Android Open Source Project
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
import android.content.Context;
import android.content.Intent;
import android.os.Build.VERSION;
import android.os.Build.VERSION_CODES;
import androidx.annotation.NonNull;
import androidx.annotation.StyleRes;
import com.google.android.setupcompat.PartnerCustomizationLayout;
import com.google.android.setupcompat.partnerconfig.PartnerConfigHelper;
import com.google.android.setupcompat.util.BuildCompatUtils;
import com.google.android.setupcompat.util.Logger;
import com.google.android.setupcompat.util.WizardManagerHelper;
import com.google.android.setupdesign.R;
import java.util.Objects;

/** The helper class holds the constant names of themes and util functions */
public final class ThemeHelper {

  private static final Logger LOG = new Logger("ThemeHelper");

  /**
   * Passed in a setup wizard intent as {@link WizardManagerHelper#EXTRA_THEME}. This is the dark
   * variant of the theme used in setup wizard for Nougat MR1.
   */
  public static final String THEME_GLIF = "glif";

  /**
   * Passed in a setup wizard intent as {@link WizardManagerHelper#EXTRA_THEME}. This is the default
   * theme used in setup wizard for Nougat MR1.
   */
  public static final String THEME_GLIF_LIGHT = "glif_light";

  /**
   * Passed in a setup wizard intent as {@link WizardManagerHelper#EXTRA_THEME}. This is the dark
   * variant of the theme used in setup wizard for O DR.
   */
  public static final String THEME_GLIF_V2 = "glif_v2";

  /**
   * Passed in a setup wizard intent as {@link WizardManagerHelper#EXTRA_THEME}. This is the default
   * theme used in setup wizard for O DR.
   */
  public static final String THEME_GLIF_V2_LIGHT = "glif_v2_light";

  /**
   * Passed in a setup wizard intent as {@link WizardManagerHelper#EXTRA_THEME}. This is the dark
   * variant of the theme used in setup wizard for P.
   */
  public static final String THEME_GLIF_V3 = "glif_v3";

  /**
   * Passed in a setup wizard intent as {@link WizardManagerHelper#EXTRA_THEME}. This is the default
   * theme used in setup wizard for P.
   */
  public static final String THEME_GLIF_V3_LIGHT = "glif_v3_light";

  /**
   * Passed in a setup wizard intent as {@link WizardManagerHelper#EXTRA_THEME}. This is the dark
   * variant of the theme used in setup wizard for T.
   */
  public static final String THEME_GLIF_V4 = "glif_v4";

  /**
   * Passed in a setup wizard intent as {@link WizardManagerHelper#EXTRA_THEME}. This is the default
   * theme used in setup wizard for T.
   */
  public static final String THEME_GLIF_V4_LIGHT = "glif_v4_light";

  /**
   * Passed in a setup wizard intent as {@link WizardManagerHelper#EXTRA_THEME}. This is the dark
   * variant of the theme used in setup wizard for Android W.
   */
  public static final String THEME_GLIF_EXPRESSIVE = "glif_expressive";

  /**
   * Passed in a setup wizard intent as {@link WizardManagerHelper#EXTRA_THEME}. This is the default
   * theme used in setup wizard for Android W.
   */
  public static final String THEME_GLIF_EXPRESSIVE_LIGHT = "glif_expressive_light";

  public static final String THEME_HOLO = "holo";
  public static final String THEME_HOLO_LIGHT = "holo_light";
  public static final String THEME_MATERIAL = "material";
  public static final String THEME_MATERIAL_LIGHT = "material_light";

  /**
   * Checks the intent whether the extra indicates that the light theme should be used or not. If
   * the theme is not specified in the intent, or the theme specified is unknown, the value def will
   * be returned. Note that day-night themes are not taken into account by this method.
   *
   * @param intent The intent used to start the activity, which the theme extra will be read from.
   * @param def The default value if the theme is not specified.
   * @return True if the activity started by the given intent should use light theme.
   */
  public static boolean isLightTheme(Intent intent, boolean def) {
    final String theme = intent.getStringExtra(WizardManagerHelper.EXTRA_THEME);
    return isLightTheme(theme, def);
  }

  /**
   * Checks whether {@code theme} represents a light or dark theme. If the theme specified is
   * unknown, the value def will be returned. Note that day-night themes are not taken into account
   * by this method.
   *
   * @param theme The theme as specified from an intent sent from setup wizard.
   * @param def The default value if the theme is not known.
   * @return True if {@code theme} represents a light theme.
   */
  public static boolean isLightTheme(String theme, boolean def) {
    if (THEME_HOLO_LIGHT.equals(theme)
        || THEME_MATERIAL_LIGHT.equals(theme)
        || THEME_GLIF_LIGHT.equals(theme)
        || THEME_GLIF_V2_LIGHT.equals(theme)
        || THEME_GLIF_V3_LIGHT.equals(theme)
        || THEME_GLIF_V4_LIGHT.equals(theme)
        || THEME_GLIF_EXPRESSIVE_LIGHT.equals(theme)) {
      return true;
    } else if (THEME_HOLO.equals(theme)
        || THEME_MATERIAL.equals(theme)
        || THEME_GLIF.equals(theme)
        || THEME_GLIF_V2.equals(theme)
        || THEME_GLIF_V3.equals(theme)
        || THEME_GLIF_V4.equals(theme)
        || THEME_GLIF_EXPRESSIVE.equals(theme)) {
      return false;
    } else {
      return def;
    }
  }

  /**
   * Reads the theme from the intent, and applies the theme to the activity as resolved by {@link
   * ThemeResolver#getDefault()}.
   *
   * <p>If you require extra theme attributes, consider overriding {@link
   * android.app.Activity#onApplyThemeResource} in your activity and call {@link
   * android.content.res.Resources.Theme#applyStyle(int, boolean)} using your theme overlay.
   *
   * <pre>{@code
   * protected void onApplyThemeResource(Theme theme, int resid, boolean first) {
   *     super.onApplyThemeResource(theme, resid, first);
   *     theme.applyStyle(R.style.MyThemeOverlay, true);
   * }
   * }</pre>
   *
   * @param activity the activity to get the intent from and apply the resulting theme to.
   */
  public static void applyTheme(Activity activity) {
    ThemeResolver.getDefault().applyTheme(activity);
  }

  /**
   * Checks whether SetupWizard supports the DayNight theme during setup flow; if it returns false,
   * setup flow is always light theme.
   *
   * @return true if the SetupWizard is listening to system DayNight theme setting.
   */
  public static boolean isSetupWizardDayNightEnabled(@NonNull Context context) {
    return PartnerConfigHelper.isSetupWizardDayNightEnabled(context);
  }

  /**
   * Returns true if the partner provider of SetupWizard is ready to support more partner configs.
   */
  public static boolean shouldApplyExtendedPartnerConfig(@NonNull Context context) {
    return PartnerConfigHelper.shouldApplyExtendedPartnerConfig(context);
  }

  /** Returns true if the SetupWizard is flow enabled "Material You(Glifv4)" style. */
  public static boolean shouldApplyMaterialYouStyle(@NonNull Context context) {
    return PartnerConfigHelper.shouldApplyMaterialYouStyle(context);
  }

  /** Returns {@code true} if this {@code context} should apply dynamic color. */
  public static boolean shouldApplyDynamicColor(@NonNull Context context) {
    return PartnerConfigHelper.isSetupWizardDynamicColorEnabled(context);
  }

  /** Returns {@code true} if this {@code context} should applied Glif expressive style. */
  public static boolean shouldApplyGlifExpressiveStyle(@NonNull Context context) {
    return PartnerConfigHelper.isGlifExpressiveEnabled(context);
  }

  /**
   * Returns a theme resource id if the {@link com.google.android.setupdesign.GlifLayout} should
   * apply dynamic color.
   *
   * <p>Otherwise returns {@code 0}.
   */
  @StyleRes
  public static int getDynamicColorTheme(@NonNull Context context) {
    @StyleRes int resId = 0;

    // Don't return the dynamic theme when glif expressive is enabled.
    if (shouldApplyGlifExpressiveStyle(context)) {
      return resId;
    }

    Activity activity;
    try {
      activity = PartnerCustomizationLayout.lookupActivityFromContext(context);
    } catch (IllegalArgumentException ex) {
      LOG.e(Objects.requireNonNull(ex.getMessage()));
      return resId;
    }

    boolean isSetupFlow = WizardManagerHelper.isAnySetupWizard(activity.getIntent());
    boolean isDayNightEnabled = isSetupWizardDayNightEnabled(context);

    boolean isSUWFullDynamicColorEnabled =
        PartnerConfigHelper.isSetupWizardFullDynamicColorEnabled(context);

    if (!isSetupFlow || (BuildCompatUtils.isAtLeastU() && isSUWFullDynamicColorEnabled)) {
      // return theme for outside setup flow
      resId =
          isDayNightEnabled
              ? R.style.SudFullDynamicColorTheme_DayNight
              : R.style.SudFullDynamicColorTheme_Light;
      LOG.atInfo(
          "Return "
              + (isDayNightEnabled
                  ? "SudFullDynamicColorTheme_DayNight"
                  : "SudFullDynamicColorTheme_Light"));
    } else {
      // return theme for inside setup flow
      resId =
          isDayNightEnabled
              ? R.style.SudDynamicColorTheme_DayNight
              : R.style.SudDynamicColorTheme_Light;
    }

    LOG.atDebug(
        "Gets the dynamic accentColor: [Light] "
            + colorIntToHex(context, R.color.sud_dynamic_color_accent_glif_v3_light)
            + ", "
            + (BuildCompatUtils.isAtLeastS()
                ? colorIntToHex(context, android.R.color.system_accent1_600)
                : "n/a")
            + ", [Dark] "
            + colorIntToHex(context, R.color.sud_dynamic_color_accent_glif_v3_dark)
            + ", "
            + (BuildCompatUtils.isAtLeastS()
                ? colorIntToHex(context, android.R.color.system_accent1_100)
                : "n/a"));

    return resId;
  }

  /** Returns a default theme resource id which provides by setup wizard. */
  @StyleRes
  public static int getSuwDefaultTheme(@NonNull Context context) {
    boolean isDayNightEnabled = ThemeHelper.isSetupWizardDayNightEnabled(context);
    String themeName = PartnerConfigHelper.getSuwDefaultThemeString(context);
    @StyleRes int defaultTheme;

    if (shouldApplyGlifExpressiveStyle(context)) {
      LOG.atInfo(
          "Return "
              + (isDayNightEnabled
                  ? "SudThemeGlifExpressive_DayNight"
                  : "SudThemeGlifExpressive_Light"));
      return isDayNightEnabled
          ? R.style.SudThemeGlifExpressive_DayNight
          : R.style.SudThemeGlifExpressive_Light;
    }

    String themeResToString = "";
    if (VERSION.SDK_INT < VERSION_CODES.O) {
      defaultTheme = isDayNightEnabled ? R.style.SudThemeGlif_DayNight : R.style.SudThemeGlif_Light;
      themeResToString = isDayNightEnabled ? "SudThemeGlif_DayNight" : "SudThemeGlif_Light";
    } else if (VERSION.SDK_INT < VERSION_CODES.P) {
      defaultTheme =
          isDayNightEnabled ? R.style.SudThemeGlifV2_DayNight : R.style.SudThemeGlifV2_Light;
      themeResToString = isDayNightEnabled ? "SudThemeGlifV2_DayNight" : "SudThemeGlifV2_Light";
    } else if (VERSION.SDK_INT < VERSION_CODES.TIRAMISU) {
      defaultTheme =
          isDayNightEnabled ? R.style.SudThemeGlifV3_DayNight : R.style.SudThemeGlifV3_Light;
      themeResToString = isDayNightEnabled ? "SudThemeGlifV3_DayNight" : "SudThemeGlifV3_Light";
    } else {
      defaultTheme =
          isDayNightEnabled ? R.style.SudThemeGlifV4_DayNight : R.style.SudThemeGlifV4_Light;
      themeResToString = isDayNightEnabled ? "SudThemeGlifV4_DayNight" : "SudThemeGlifV4_Light";
    }
    LOG.atInfo("Default theme: " + themeResToString + ", return theme: " + themeName);

    return new ThemeResolver.Builder()
        .setDefaultTheme(defaultTheme)
        .setUseDayNight(isSetupWizardDayNightEnabled(context))
        .build()
        .resolve(themeName, /* suppressDayNight= */ !isSetupWizardDayNightEnabled(context));
  }

  /** Returns {@code true} if the SUW theme is set. */
  public static boolean trySetSuwTheme(@NonNull Context context) {
    @StyleRes int theme = getSuwDefaultTheme(context);
    Activity activity;
    try {
      activity = PartnerCustomizationLayout.lookupActivityFromContext(context);
    } catch (IllegalArgumentException ex) {
      LOG.e(Objects.requireNonNull(ex.getMessage()));
      return false;
    }
    // Apply theme
    if (theme != 0) {
      activity.setTheme(theme);
    } else {
      LOG.w("Error occurred on getting suw default theme.");
      return false;
    }

    if (!BuildCompatUtils.isAtLeastS()) {
      LOG.w("Skip set theme with dynamic color, it is require platform version at least S.");
      return true;
    }

    // Don't apply dynamic theme when glif expressive is enabled.
    if (shouldApplyGlifExpressiveStyle(context)) {
      LOG.w("Skip set theme with dynamic color, due to glif expressive sytle enabled.");
      return true;
    }

    return trySetDynamicColor(context);
  }

  /** Returns {@code true} if the dynamic color is set. */
  public static boolean trySetDynamicColor(@NonNull Context context) {
    if (!BuildCompatUtils.isAtLeastS()) {
      LOG.w("Dynamic color require platform version at least S.");
      return false;
    }

    // Don't apply dynamic theme when glif expressive is enabled.
    if (shouldApplyGlifExpressiveStyle(context)) {
      LOG.w("Dynamic color theme isn't needed to set in glif expressive theme.");
      return false;
    }

    if (!shouldApplyDynamicColor(context)) {
      LOG.w("SetupWizard does not support the dynamic color or supporting status unknown.");
      return false;
    }

    Activity activity;
    try {
      activity = PartnerCustomizationLayout.lookupActivityFromContext(context);
    } catch (IllegalArgumentException ex) {
      LOG.e(Objects.requireNonNull(ex.getMessage()));
      return false;
    }

    @StyleRes int resId = getDynamicColorTheme(context);
    if (resId != 0) {
      activity.setTheme(resId);
    } else {
      LOG.w("Error occurred on getting dynamic color theme.");
      return false;
    }

    return true;
  }

  private static String colorIntToHex(Context context, int colorInt) {
    return String.format("#%06X", (0xFFFFFF & context.getResources().getColor(colorInt)));
  }

  private ThemeHelper() {}
}
