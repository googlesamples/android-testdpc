package com.afwsamples.testdpc.common;

import android.content.Context;
import android.os.Build.VERSION_CODES;
import com.afwsamples.testdpc.R;
import com.google.android.setupdesign.util.ThemeHelper;
import com.google.android.setupdesign.util.ThemeResolver;

/** Common utility functions for Theming. */
public final class ThemeUtil {

  /**
   * Set the correct Theme for the SUW screen
   *
   * @param default theme string .
   */
  public static void setTheme(Context context, String themeName) {
    int defaultTheme;
    if (Util.SDK_INT < VERSION_CODES.TIRAMISU) {
      defaultTheme =
          ThemeHelper.isSetupWizardDayNightEnabled(context)
              ? R.style.SudThemeGlifV3_DayNight
              : R.style.SudThemeGlifV3_Light;
    } else {
      defaultTheme =
          ThemeHelper.isSetupWizardDayNightEnabled(context)
              ? R.style.SudThemeGlifV4_DayNight
              : R.style.SudThemeGlifV4_Light;
    }

    // a. set GlifTheme based on suw intent extra & SUW daynight flag.
    ThemeResolver THEME_RESOLVER =
        new ThemeResolver.Builder(ThemeResolver.getDefault())
            .setDefaultTheme(defaultTheme)
            .setUseDayNight(true)
            .build();

    // If outside suw (themeName=null); the themeResolver will fallback
    // to the default theme directly, resolve theme resource based on the day-night flag.
    int themeResId =
        THEME_RESOLVER.resolve(themeName, !ThemeHelper.isSetupWizardDayNightEnabled(context));

    // setTheme for this activity.
    context.setTheme(themeResId);

    // b. overlay color attrs to dynamic color for GlifTheme.
    ThemeHelper.trySetDynamicColor(context);
  }
}
