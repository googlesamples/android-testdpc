/*
 * Copyright (C) 2024 The Android Open Source Project
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

import android.annotation.SuppressLint;
import android.content.Context;
import android.content.res.Resources;
import android.content.res.Resources.NotFoundException;
import android.os.Build;
import android.os.Build.VERSION_CODES;
import android.view.WindowManager;
import android.view.WindowMetrics;
import androidx.annotation.LayoutRes;
import com.google.android.setupcompat.partnerconfig.PartnerConfig;
import com.google.android.setupcompat.partnerconfig.PartnerConfigHelper;

/**
 * A helper class to support force two pane feature on portrait orientation. This will inflate the
 * layout from xml resource which concatenates with _two_pane suffix.
 */
public final class ForceTwoPaneHelper {

  // Refer Support different screen sizes as guideline that any device that the width >= 840 will
  // consider as large screen, b/322117552#comment15 mentioned that the apply 2 pane layouts based
  // on width >= 840dp as screen breakpoints.
  //
  // https://developer.android.com/guide/topics/large-screens/support-different-screen-sizes
  private static final int DEFAULT_ADAPT_WINDOW_WIDTH = 840;

  private static final Logger LOG = new Logger("ForceTwoPaneHelper");

  /** A string to be a suffix of resource name which is associating to force two pane feature. */
  public static final String FORCE_TWO_PANE_SUFFIX = "_two_pane";

  /**
   * Returns true to indicate the forced two pane feature is enabled, otherwise, returns false. This
   * feature is supported from Sdk U while the feature enabled from SUW side.
   */
  public static boolean isForceTwoPaneEnable(Context context) {
    return Build.VERSION.SDK_INT >= VERSION_CODES.UPSIDE_DOWN_CAKE
        && PartnerConfigHelper.isForceTwoPaneEnabled(context);
  }

  /**
   * Returns true if satisfied 1) enable force two-pane feature, 2) portrait mode, 3) width >=
   * setup_compat_two_pane_adapt_window_width, forced to show in two-pane style, otherwise, returns
   * false.
   */
  public static boolean shouldForceTwoPane(Context context) {
    if (!isForceTwoPaneEnable(context)) {
      return false;
    }

    if (context == null) {
      return false;
    }

    WindowManager windowManager = context.getSystemService(WindowManager.class);
    if (windowManager != null) {
      WindowMetrics windowMetrics = windowManager.getCurrentWindowMetrics();
      if (windowMetrics.getBounds().width() > windowMetrics.getBounds().height()) {
        // Return false for portrait mode
        return false;
      }

      int widthInDp = (int) (windowMetrics.getBounds().width() / windowMetrics.getDensity());
      int adaptWindowWidth =
          PartnerConfigHelper.get(context)
              .getInteger(
                  context,
                  PartnerConfig.CONFIG_TWO_PANE_ADAPT_WINDOW_WIDTH,
                  DEFAULT_ADAPT_WINDOW_WIDTH);
      return widthInDp >= adaptWindowWidth;
    }

    return false;
  }

  /**
   * Returns a layout which is picking up from the layout resources with _two_pane suffix. Fallback
   * to origin resource id if the layout resource not available. For example, pass an
   * glif_sud_template resource id and it will return glif_sud_template_two_pane resource id if it
   * available.
   */
  @LayoutRes
  @SuppressLint("DiscouragedApi")
  public static int getForceTwoPaneStyleLayout(Context context, int template) {
    if (!isForceTwoPaneEnable(context)) {
      return template;
    }

    if (template == Resources.ID_NULL) {
      return template;
    }

    try {
      String layoutResName = context.getResources().getResourceEntryName(template);
      int twoPaneLayoutId =
          context
              .getResources()
              .getIdentifier(
                  layoutResName + FORCE_TWO_PANE_SUFFIX, "layout", context.getPackageName());
      if (twoPaneLayoutId != Resources.ID_NULL) {
        return twoPaneLayoutId;
      }
    } catch (NotFoundException ignore) {
      LOG.w("Resource id 0x" + Integer.toHexString(template) + " is not found");
    }

    return template;
  }

  private ForceTwoPaneHelper() {}
}
