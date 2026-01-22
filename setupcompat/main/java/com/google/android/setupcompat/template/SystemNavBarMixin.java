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

package com.google.android.setupcompat.template;

import static android.view.View.SYSTEM_UI_FLAG_LIGHT_NAVIGATION_BAR;

import android.content.Context;
import android.content.res.TypedArray;
import android.graphics.Color;
import android.os.Build;
import android.os.Build.VERSION;
import android.os.Build.VERSION_CODES;
import android.util.AttributeSet;
import android.view.View;
import android.view.Window;
import androidx.annotation.AttrRes;
import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.annotation.VisibleForTesting;
import com.google.android.setupcompat.PartnerCustomizationLayout;
import com.google.android.setupcompat.R;
import com.google.android.setupcompat.internal.TemplateLayout;
import com.google.android.setupcompat.partnerconfig.PartnerConfig;
import com.google.android.setupcompat.partnerconfig.PartnerConfigHelper;
import com.google.android.setupcompat.util.SystemBarHelper;

/**
 * A {@link Mixin} for setting and getting background color and window compatible with light theme
 * of system navigation bar.
 */
public class SystemNavBarMixin implements Mixin {

  private final TemplateLayout templateLayout;
  @Nullable private final Window windowOfActivity;
  @VisibleForTesting final boolean applyPartnerResources;
  @VisibleForTesting final boolean useFullDynamicColor;
  private int sucSystemNavBarBackgroundColor = 0;

  /**
   * Creates a mixin for managing the system navigation bar.
   *
   * @param layout The layout this Mixin belongs to.
   * @param window The window this activity of Mixin belongs to.*
   */
  public SystemNavBarMixin(@NonNull TemplateLayout layout, @Nullable Window window) {
    this.templateLayout = layout;
    this.windowOfActivity = window;
    this.applyPartnerResources =
        layout instanceof PartnerCustomizationLayout
            && ((PartnerCustomizationLayout) layout).shouldApplyPartnerResource();

    this.useFullDynamicColor =
        layout instanceof PartnerCustomizationLayout
            && ((PartnerCustomizationLayout) layout).useFullDynamicColor();
  }

  /**
   * Creates a mixin for managing the system navigation bar.
   *
   * @param attrs XML attributes given to the layout.
   * @param defStyleAttr The default style attribute as given to the constructor of the layout.
   */
  public void applyPartnerCustomizations(@Nullable AttributeSet attrs, @AttrRes int defStyleAttr) {
    // Support updating system navigation bar background color and is light system navigation bar
    // from O.
    if (Build.VERSION.SDK_INT >= VERSION_CODES.O_MR1) {
      TypedArray a =
          templateLayout
              .getContext()
              .obtainStyledAttributes(attrs, R.styleable.SucSystemNavBarMixin, defStyleAttr, 0);
      sucSystemNavBarBackgroundColor =
          a.getColor(R.styleable.SucSystemNavBarMixin_sucSystemNavBarBackgroundColor, 0);
      setSystemNavBarBackground(sucSystemNavBarBackgroundColor);
      setLightSystemNavBar(
          a.getBoolean(
              R.styleable.SucSystemNavBarMixin_sucLightSystemNavBar, isLightSystemNavBar()));

      // Support updating system navigation bar divider color from P.
      if (VERSION.SDK_INT >= VERSION_CODES.P) {
        // get fallback value from theme
        int[] navBarDividerColorAttr = new int[] {android.R.attr.navigationBarDividerColor};
        TypedArray typedArray =
            templateLayout.getContext().obtainStyledAttributes(navBarDividerColorAttr);
        int defaultColor = typedArray.getColor(/* index= */ 0, /* defValue= */ 0);
        int sucSystemNavBarDividerColor =
            a.getColor(R.styleable.SucSystemNavBarMixin_sucSystemNavBarDividerColor, defaultColor);
        setSystemNavBarDividerColor(sucSystemNavBarDividerColor);
        typedArray.recycle();
      }
      a.recycle();
    }
  }

  /**
   * Sets the background color of navigation bar. The color will be overridden by partner resource
   * if the activity is running in setup wizard flow.
   *
   * @param color The background color of navigation bar.
   */
  public void setSystemNavBarBackground(int color) {
    if (Build.VERSION.SDK_INT >= VERSION_CODES.LOLLIPOP && windowOfActivity != null) {
      if (applyPartnerResources) {
        // If full dynamic color enabled which means this activity is running outside of setup
        // flow, the colors should refer to R.style.SudFullDynamicColorThemeGlifV3.
        if (!useFullDynamicColor) {
          Context context = templateLayout.getContext();
          color =
              PartnerConfigHelper.get(context)
                  .getColor(context, PartnerConfig.CONFIG_NAVIGATION_BAR_BG_COLOR);
        }
      }
      windowOfActivity.setNavigationBarColor(color);
    }
  }

  /** Returns the background color of navigation bar. */
  public int getSystemNavBarBackground() {
    if (Build.VERSION.SDK_INT >= VERSION_CODES.LOLLIPOP && windowOfActivity != null) {
      return windowOfActivity.getNavigationBarColor();
    }
    return Color.BLACK;
  }

  /**
   * Sets the navigation bar to draw in a mode that is compatible with light or dark navigation bar
   * backgrounds. The navigation bar drawing mode will be overridden by partner resource if the
   * activity is running in setup wizard flow.
   *
   * @param isLight true means compatible with light theme, otherwise compatible with dark theme
   */

  public void setLightSystemNavBar(boolean isLight) {
    if (Build.VERSION.SDK_INT >= VERSION_CODES.O && windowOfActivity != null) {
      if (applyPartnerResources) {
        Context context = templateLayout.getContext();
        isLight =
            PartnerConfigHelper.get(context)
                .getBoolean(context, PartnerConfig.CONFIG_LIGHT_NAVIGATION_BAR, false);
      }
      if (isLight) {
        windowOfActivity
            .getDecorView()
            .setSystemUiVisibility(
                windowOfActivity.getDecorView().getSystemUiVisibility()
                    | SYSTEM_UI_FLAG_LIGHT_NAVIGATION_BAR);
      } else {
        windowOfActivity
            .getDecorView()
            .setSystemUiVisibility(
                windowOfActivity.getDecorView().getSystemUiVisibility()
                    & ~SYSTEM_UI_FLAG_LIGHT_NAVIGATION_BAR);
      }
    }
  }

  /**
   * Returns true if the navigation bar icon should be drawn on light background, false if the icons
   * should be drawn light-on-dark.
   */
  public boolean isLightSystemNavBar() {
    if (Build.VERSION.SDK_INT >= VERSION_CODES.O && windowOfActivity != null) {
      return (windowOfActivity.getDecorView().getSystemUiVisibility()
              & SYSTEM_UI_FLAG_LIGHT_NAVIGATION_BAR)
          == SYSTEM_UI_FLAG_LIGHT_NAVIGATION_BAR;
    }
    return true;
  }

  /**
   * Sets the divider color of navigation bar. The color will be overridden by partner resource if
   * the activity is running in setup wizard flow.
   *
   * @param color the default divider color of navigation bar
   */
  public void setSystemNavBarDividerColor(int color) {
    if (Build.VERSION.SDK_INT >= VERSION_CODES.P && windowOfActivity != null) {
      if (applyPartnerResources) {
        Context context = templateLayout.getContext();
        // Do nothing if the old version partner provider did not contain the new config.
        if (PartnerConfigHelper.get(context)
            .isPartnerConfigAvailable(PartnerConfig.CONFIG_NAVIGATION_BAR_DIVIDER_COLOR)) {
          color =
              PartnerConfigHelper.get(context)
                  .getColor(context, PartnerConfig.CONFIG_NAVIGATION_BAR_DIVIDER_COLOR);
        }
      }
      windowOfActivity.setNavigationBarDividerColor(color);
    }
  }

  /**
   * Hides the navigation bar, make the color of the status and navigation bars transparent, and
   * specify {@link View#SYSTEM_UI_FLAG_LAYOUT_FULLSCREEN} flag so that the content is laid-out
   * behind the transparent status bar. This is commonly used with {@link
   * android.app.Activity#getWindow()} to make the navigation and status bars follow the Setup
   * Wizard style.
   *
   * <p>This will only take effect in versions Lollipop or above. Otherwise this is a no-op.
   */
  public void hideSystemBars(final Window window) {
    if (VERSION.SDK_INT >= VERSION_CODES.LOLLIPOP) {
      SystemBarHelper.addVisibilityFlag(window, SystemBarHelper.DEFAULT_IMMERSIVE_FLAGS);
      SystemBarHelper.addImmersiveFlagsToDecorView(window, SystemBarHelper.DEFAULT_IMMERSIVE_FLAGS);

      // Also set the navigation bar and status bar to transparent color. Note that this
      // doesn't work if android.R.boolean.config_enableTranslucentDecor is false.
      window.setNavigationBarColor(Color.TRANSPARENT);
      window.setStatusBarColor(Color.TRANSPARENT);
    }
  }

  /**
   * Reverts the actions of hideSystemBars. Note that this will remove the system UI visibility
   * flags regardless of whether it is originally present. The status bar color is reset to
   * transparent, thus it will show the status bar color set by StatusBarMixin.
   *
   * <p>This will only take effect in versions Lollipop or above. Otherwise this is a no-op.
   */
  public void showSystemBars(final Window window, final Context context) {
    if (VERSION.SDK_INT >= VERSION_CODES.LOLLIPOP) {
      SystemBarHelper.removeVisibilityFlag(window, SystemBarHelper.DEFAULT_IMMERSIVE_FLAGS);
      SystemBarHelper.removeImmersiveFlagsFromDecorView(
          window, SystemBarHelper.DEFAULT_IMMERSIVE_FLAGS);

      if (context != null) {
        if (applyPartnerResources) {
          int partnerNavigationBarColor =
              PartnerConfigHelper.get(context)
                  .getColor(context, PartnerConfig.CONFIG_NAVIGATION_BAR_BG_COLOR);
          window.setStatusBarColor(Color.TRANSPARENT);
          window.setNavigationBarColor(partnerNavigationBarColor);
        } else {
          // noinspection AndroidLintInlinedApi
          TypedArray typedArray =
              context.obtainStyledAttributes(
                  new int[] {android.R.attr.statusBarColor, android.R.attr.navigationBarColor});
          int statusBarColor = typedArray.getColor(0, 0);
          int navigationBarColor = typedArray.getColor(1, 0);
          if (templateLayout instanceof PartnerCustomizationLayout) {
            if (VERSION.SDK_INT >= VERSION_CODES.M) {
              statusBarColor = Color.TRANSPARENT;
            }
            if (VERSION.SDK_INT >= VERSION_CODES.O_MR1) {
              navigationBarColor = sucSystemNavBarBackgroundColor;
            }
          }
          window.setStatusBarColor(statusBarColor);
          window.setNavigationBarColor(navigationBarColor);
          typedArray.recycle();
        }
      }
    }
  }
}
