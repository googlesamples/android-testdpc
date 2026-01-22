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

import static android.view.View.SYSTEM_UI_FLAG_LIGHT_STATUS_BAR;

import android.content.Context;
import android.content.res.TypedArray;
import android.graphics.Color;
import android.graphics.drawable.ColorDrawable;
import android.graphics.drawable.Drawable;
import android.os.Build;
import android.os.Build.VERSION_CODES;
import android.util.AttributeSet;
import android.view.View;
import android.view.Window;
import android.widget.LinearLayout;
import androidx.annotation.AttrRes;
import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import com.google.android.setupcompat.PartnerCustomizationLayout;
import com.google.android.setupcompat.R;
import com.google.android.setupcompat.partnerconfig.PartnerConfig;
import com.google.android.setupcompat.partnerconfig.PartnerConfigHelper;
import com.google.android.setupcompat.view.StatusBarBackgroundLayout;

/**
 * A {@link Mixin} for setting and getting background color, and window compatible light/dark theme
 * of status bar.
 */
public class StatusBarMixin implements Mixin {

  private final PartnerCustomizationLayout partnerCustomizationLayout;
  private StatusBarBackgroundLayout statusBarLayout;
  private LinearLayout linearLayout;
  private final View decorView;

  /**
   * Creates a mixin for managing status bar.
   *
   * @param partnerCustomizationLayout The layout this Mixin belongs to.
   * @param window The window this activity of Mixin belongs to.
   * @param attrs XML attributes given to the layout.
   * @param defStyleAttr The default style attribute as given to the constructor of the layout.
   */
  public StatusBarMixin(
      @NonNull PartnerCustomizationLayout partnerCustomizationLayout,
      @NonNull Window window,
      @Nullable AttributeSet attrs,
      @AttrRes int defStyleAttr) {

    this.partnerCustomizationLayout = partnerCustomizationLayout;

    View sucLayoutStatus = partnerCustomizationLayout.findManagedViewById(R.id.suc_layout_status);
    if (sucLayoutStatus == null) {
      throw new NullPointerException("sucLayoutStatus cannot be null in StatusBarMixin");
    }

    if (sucLayoutStatus instanceof StatusBarBackgroundLayout) {
      statusBarLayout = (StatusBarBackgroundLayout) sucLayoutStatus;
    } else {
      linearLayout = (LinearLayout) sucLayoutStatus;
    }

    decorView = window.getDecorView();

    // Support updating system status bar background color and is light system status bar from M.
    if (Build.VERSION.SDK_INT >= VERSION_CODES.M) {
      // Override the color of status bar to transparent such that the color of
      // StatusBarBackgroundLayout can be seen.
      window.setStatusBarColor(Color.TRANSPARENT);
      TypedArray a =
          partnerCustomizationLayout
              .getContext()
              .obtainStyledAttributes(attrs, R.styleable.SucStatusBarMixin, defStyleAttr, 0);
      setLightStatusBar(
          a.getBoolean(R.styleable.SucStatusBarMixin_sucLightStatusBar, isLightStatusBar()));
      setStatusBarBackground(a.getDrawable(R.styleable.SucStatusBarMixin_sucStatusBarBackground));
      a.recycle();
    }
  }

  /**
   * Sets the background color of status bar. The color will be overridden by partner resource if
   * the activity is running in setup wizard flow.
   *
   * @param color The background color of status bar.
   */
  public void setStatusBarBackground(int color) {
    setStatusBarBackground(new ColorDrawable(color));
  }

  /**
   * Sets the background image of status bar. The drawable will be overridden by partner resource if
   * the activity is running in setup wizard flow.
   *
   * @param background The drawable of status bar.
   */
  public void setStatusBarBackground(Drawable background) {
    if (partnerCustomizationLayout.shouldApplyPartnerResource()) {
      // If full dynamic color enabled which means this activity is running outside of setup
      // flow, the colors should refer to R.style.SudFullDynamicColorThemeGlifV3.
      if (!partnerCustomizationLayout.useFullDynamicColor()) {
      Context context = partnerCustomizationLayout.getContext();
      background =
          PartnerConfigHelper.get(context)
              .getDrawable(context, PartnerConfig.CONFIG_STATUS_BAR_BACKGROUND);
      }
    }

    if (statusBarLayout == null) {
      linearLayout.setBackgroundDrawable(background);
    } else {
      statusBarLayout.setStatusBarBackground(background);
    }
  }

  /** Returns the background of status bar. */
  public Drawable getStatusBarBackground() {
    if (statusBarLayout == null) {
      return linearLayout.getBackground();
    } else {
      return statusBarLayout.getStatusBarBackground();
    }
  }

  /**
   * Sets the status bar to draw in a mode that is compatible with light or dark status bar
   * backgrounds. The status bar drawing mode will be overridden by partner resource if the activity
   * is running in setup wizard flow.
   *
   * @param isLight true means compatible with light theme, otherwise compatible with dark theme
   */
  public void setLightStatusBar(boolean isLight) {
    if (Build.VERSION.SDK_INT >= VERSION_CODES.M) {
      if (partnerCustomizationLayout.shouldApplyPartnerResource()) {
        Context context = partnerCustomizationLayout.getContext();
        isLight =
            PartnerConfigHelper.get(context)
                .getBoolean(context, PartnerConfig.CONFIG_LIGHT_STATUS_BAR, false);
      }

      if (isLight) {
        decorView.setSystemUiVisibility(
            decorView.getSystemUiVisibility() | SYSTEM_UI_FLAG_LIGHT_STATUS_BAR);
      } else {
        decorView.setSystemUiVisibility(
            decorView.getSystemUiVisibility() & ~SYSTEM_UI_FLAG_LIGHT_STATUS_BAR);
      }
    }
  }

  /**
   * Returns true if status bar icons should be drawn on light background, false if the icons should
   * be light-on-dark.
   */
  public boolean isLightStatusBar() {
    if (Build.VERSION.SDK_INT >= VERSION_CODES.M) {
      return (decorView.getSystemUiVisibility() & SYSTEM_UI_FLAG_LIGHT_STATUS_BAR)
          == SYSTEM_UI_FLAG_LIGHT_STATUS_BAR;
    }
    return true;
  }
}
