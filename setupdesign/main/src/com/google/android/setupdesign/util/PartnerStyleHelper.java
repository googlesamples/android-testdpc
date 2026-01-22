/*
 * Copyright (C) 2019 The Android Open Source Project
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
import android.content.res.TypedArray;
import android.os.Build;
import android.view.Gravity;
import android.view.View;
import com.google.android.setupcompat.PartnerCustomizationLayout;
import com.google.android.setupcompat.internal.TemplateLayout;
import com.google.android.setupcompat.partnerconfig.PartnerConfig;
import com.google.android.setupcompat.partnerconfig.PartnerConfigHelper;
import com.google.android.setupcompat.util.WizardManagerHelper;
import com.google.android.setupdesign.GlifLayout;
import com.google.android.setupdesign.R;
import java.util.Locale;

/** The helper reads styles from the partner configurations. */
public final class PartnerStyleHelper {

  private static final String TAG = "PartnerStyleHelper";
  /**
   * Returns the partner configuration of layout gravity, usually apply to widgets in header area.
   */
  public static int getLayoutGravity(Context context) {
    String gravity =
        PartnerConfigHelper.get(context).getString(context, PartnerConfig.CONFIG_LAYOUT_GRAVITY);
    if (gravity == null) {
      return 0;
    }
    switch (gravity.toLowerCase(Locale.ROOT)) {
      case "center":
        return Gravity.CENTER;
      case "start":
        return Gravity.START;
      default:
        return 0;
    }
  }

  /** Returns the given layout if apply partner heavy theme. */
  public static boolean isPartnerHeavyThemeLayout(TemplateLayout layout) {
    if (!(layout instanceof GlifLayout)) {
      return false;
    }
    return ((GlifLayout) layout).shouldApplyPartnerHeavyThemeResource();
  }

  /** Returns the given layout if apply partner light theme. */
  public static boolean isPartnerLightThemeLayout(TemplateLayout layout) {
    if (!(layout instanceof PartnerCustomizationLayout)) {
      return false;
    }
    return ((PartnerCustomizationLayout) layout).shouldApplyPartnerResource();
  }

  /**
   * Returns if the current layout/activity of the given {@code view} applies partner customized
   * configurations or not.
   *
   * @param view A PartnerCustomizationLayout view, would be used to get the activity and context.
   */
  public static boolean shouldApplyPartnerResource(View view) {
    if (view == null) {
      return false;
    }
    if (view instanceof PartnerCustomizationLayout) {
      return isPartnerLightThemeLayout((PartnerCustomizationLayout) view);
    }
    return shouldApplyPartnerResource(view.getContext());
  }

  private static boolean shouldApplyPartnerResource(Context context) {
    if (Build.VERSION.SDK_INT < Build.VERSION_CODES.Q) {
      return false;
    }

    if (!PartnerConfigHelper.get(context).isAvailable()) {
      return false;
    }

    Activity activity = null;
    try {
      activity = PartnerCustomizationLayout.lookupActivityFromContext(context);
      if (activity != null) {
        TemplateLayout layout = findLayoutFromActivity(activity);
        if (layout instanceof PartnerCustomizationLayout) {
          return ((PartnerCustomizationLayout) layout).shouldApplyPartnerResource();
        }
      }
    } catch (IllegalArgumentException | ClassCastException ex) {
      // fall through
    }

    // try best to get partner resource settings from attrs
    boolean isSetupFlow = false;
    if (activity != null) {
      isSetupFlow = WizardManagerHelper.isAnySetupWizard(activity.getIntent());
    }
    TypedArray a =
        context.obtainStyledAttributes(
            new int[] {com.google.android.setupcompat.R.attr.sucUsePartnerResource});
    boolean usePartnerResource = a.getBoolean(0, true);
    a.recycle();

    return isSetupFlow || usePartnerResource;
  }

  /**
   * Returns if the current layout/activity applies heavy partner customized configurations or not.
   *
   * @param view A view would be used to get the activity and context.
   */
  public static boolean shouldApplyPartnerHeavyThemeResource(View view) {
    if (view == null) {
      return false;
    }
    if (view instanceof GlifLayout) {
      return isPartnerHeavyThemeLayout((GlifLayout) view);
    }
    return shouldApplyPartnerHeavyThemeResource(view.getContext());
  }

  static boolean shouldApplyPartnerHeavyThemeResource(Context context) {
    try {
      Activity activity = PartnerCustomizationLayout.lookupActivityFromContext(context);
      TemplateLayout layout = findLayoutFromActivity(activity);
      if (layout instanceof GlifLayout) {
        return ((GlifLayout) layout).shouldApplyPartnerHeavyThemeResource();
      }
    } catch (IllegalArgumentException | ClassCastException ex) {
      // fall through
    }

    // try best to get partner resource settings from attr
    TypedArray a = context.obtainStyledAttributes(new int[] {R.attr.sudUsePartnerHeavyTheme});
    boolean usePartnerHeavyTheme = a.getBoolean(0, false);
    a.recycle();
    usePartnerHeavyTheme =
        usePartnerHeavyTheme || PartnerConfigHelper.shouldApplyExtendedPartnerConfig(context);

    return shouldApplyPartnerResource(context) && usePartnerHeavyTheme;
  }

  /**
   * Returns if the current layout/activity applies dynamic color configurations or not.
   *
   * @param view A GlifLayout view would be used to get the activity and context.
   */
  public static boolean useDynamicColor(View view) {
    if (view == null) {
      return false;
    }
    return getDynamicColorPatnerConfig(view.getContext());
  }

  static boolean getDynamicColorPatnerConfig(Context context) {
    try {
      Activity activity = PartnerCustomizationLayout.lookupActivityFromContext(context);
      TemplateLayout layout = findLayoutFromActivity(activity);
      if (layout instanceof GlifLayout) {
        return ((GlifLayout) layout).shouldApplyDynamicColor();
      }
      return PartnerConfigHelper.isSetupWizardFullDynamicColorEnabled(activity);
    } catch (IllegalArgumentException | ClassCastException ex) {
      // fall through
    }

    return false;
  }

  private static TemplateLayout findLayoutFromActivity(Activity activity) {
    if (activity == null) {
      return null;
    }
    // This only worked after activity setContentView, otherwise it will return null
    View rootView = activity.findViewById(R.id.suc_layout_status);
    return rootView != null ? (TemplateLayout) rootView.getParent() : null;
  }

  private PartnerStyleHelper() {}
}
