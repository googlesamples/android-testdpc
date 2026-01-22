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

package com.google.android.setupcompat.template;

import static com.google.android.setupcompat.partnerconfig.PartnerConfigHelper.isFontWeightEnabled;

import android.annotation.SuppressLint;
import android.annotation.TargetApi;
import android.content.Context;
import android.content.res.ColorStateList;
import android.content.res.TypedArray;
import android.graphics.Color;
import android.graphics.PorterDuff.Mode;
import android.graphics.Typeface;
import android.graphics.drawable.Drawable;
import android.graphics.drawable.GradientDrawable;
import android.graphics.drawable.InsetDrawable;
import android.graphics.drawable.LayerDrawable;
import android.graphics.drawable.RippleDrawable;
import android.os.Build;
import android.os.Build.VERSION_CODES;
import android.util.StateSet;
import android.util.TypedValue;
import android.view.ViewGroup;
import android.widget.Button;
import androidx.annotation.ColorInt;
import androidx.annotation.Nullable;
import com.google.android.setupcompat.R;
import com.google.android.setupcompat.internal.FooterButtonPartnerConfig;
import com.google.android.setupcompat.internal.Preconditions;
import com.google.android.setupcompat.partnerconfig.PartnerConfig;
import com.google.android.setupcompat.partnerconfig.PartnerConfigHelper;
import java.util.HashMap;

/** Utils for updating the button style. */
public class FooterButtonStyleUtils {
  private static final float DEFAULT_DISABLED_ALPHA = 0.26f;

  // android.graphics.fonts.FontStyle.FontStyle#FONT_WEIGHT_NORMAL
  private static final int FONT_WEIGHT_NORMAL = 400;

  private static final HashMap<Integer, ColorStateList> defaultTextColor = new HashMap<>();

  /** Apply the partner primary button style to given {@code button}. */
  public static void applyPrimaryButtonPartnerResource(
      Context context, Button button, boolean applyDynamicColor) {

    FooterButtonPartnerConfig footerButtonPartnerConfig =
        new FooterButtonPartnerConfig.Builder(null)
            .setPartnerTheme(R.style.SucPartnerCustomizationButton_Primary)
            .setButtonBackgroundConfig(PartnerConfig.CONFIG_FOOTER_PRIMARY_BUTTON_BG_COLOR)
            .setButtonDisableAlphaConfig(PartnerConfig.CONFIG_FOOTER_BUTTON_DISABLED_ALPHA)
            .setButtonDisableBackgroundConfig(PartnerConfig.CONFIG_FOOTER_BUTTON_DISABLED_BG_COLOR)
            .setButtonDisableTextColorConfig(
                PartnerConfig.CONFIG_FOOTER_PRIMARY_BUTTON_DISABLED_TEXT_COLOR)
            .setButtonRadiusConfig(PartnerConfig.CONFIG_FOOTER_BUTTON_RADIUS)
            .setButtonRippleColorAlphaConfig(PartnerConfig.CONFIG_FOOTER_BUTTON_RIPPLE_COLOR_ALPHA)
            .setTextColorConfig(PartnerConfig.CONFIG_FOOTER_PRIMARY_BUTTON_TEXT_COLOR)
            .setMarginStartConfig(PartnerConfig.CONFIG_FOOTER_PRIMARY_BUTTON_MARGIN_START)
            .setTextSizeConfig(PartnerConfig.CONFIG_FOOTER_BUTTON_TEXT_SIZE)
            .setButtonMinHeight(PartnerConfig.CONFIG_FOOTER_BUTTON_MIN_HEIGHT)
            .setTextTypeFaceConfig(PartnerConfig.CONFIG_FOOTER_BUTTON_FONT_FAMILY)
            .setTextWeightConfig(PartnerConfig.CONFIG_FOOTER_BUTTON_FONT_WEIGHT)
            .setTextStyleConfig(PartnerConfig.CONFIG_FOOTER_BUTTON_TEXT_STYLE)
            .build();
    applyButtonPartnerResources(
        context,
        button,
        applyDynamicColor,
        /* isButtonIconAtEnd= */ true,
        footerButtonPartnerConfig);
  }

  /** Apply the partner secondary button style to given {@code button}. */
  public static void applySecondaryButtonPartnerResource(
      Context context, Button button, boolean applyDynamicColor) {

    int defaultTheme = R.style.SucPartnerCustomizationButton_Secondary;
    int color =
        PartnerConfigHelper.get(context)
            .getColor(context, PartnerConfig.CONFIG_FOOTER_SECONDARY_BUTTON_BG_COLOR);
    if (color != Color.TRANSPARENT) {
      defaultTheme = R.style.SucPartnerCustomizationButton_Primary;
    }
    // Setup button partner config
    FooterButtonPartnerConfig footerButtonPartnerConfig =
        new FooterButtonPartnerConfig.Builder(null)
            .setPartnerTheme(defaultTheme)
            .setButtonBackgroundConfig(PartnerConfig.CONFIG_FOOTER_SECONDARY_BUTTON_BG_COLOR)
            .setButtonDisableAlphaConfig(PartnerConfig.CONFIG_FOOTER_BUTTON_DISABLED_ALPHA)
            .setButtonDisableBackgroundConfig(PartnerConfig.CONFIG_FOOTER_BUTTON_DISABLED_BG_COLOR)
            .setButtonDisableTextColorConfig(
                PartnerConfig.CONFIG_FOOTER_SECONDARY_BUTTON_DISABLED_TEXT_COLOR)
            .setButtonRadiusConfig(PartnerConfig.CONFIG_FOOTER_BUTTON_RADIUS)
            .setButtonRippleColorAlphaConfig(PartnerConfig.CONFIG_FOOTER_BUTTON_RIPPLE_COLOR_ALPHA)
            .setTextColorConfig(PartnerConfig.CONFIG_FOOTER_SECONDARY_BUTTON_TEXT_COLOR)
            .setMarginStartConfig(PartnerConfig.CONFIG_FOOTER_SECONDARY_BUTTON_MARGIN_START)
            .setTextSizeConfig(PartnerConfig.CONFIG_FOOTER_BUTTON_TEXT_SIZE)
            .setButtonMinHeight(PartnerConfig.CONFIG_FOOTER_BUTTON_MIN_HEIGHT)
            .setTextTypeFaceConfig(PartnerConfig.CONFIG_FOOTER_BUTTON_FONT_FAMILY)
            .setTextWeightConfig(PartnerConfig.CONFIG_FOOTER_BUTTON_FONT_WEIGHT)
            .setTextStyleConfig(PartnerConfig.CONFIG_FOOTER_BUTTON_TEXT_STYLE)
            .build();
    applyButtonPartnerResources(
        context,
        button,
        applyDynamicColor,
        /* isButtonIconAtEnd= */ false,
        footerButtonPartnerConfig);
  }

  static void applyButtonPartnerResources(
      Context context,
      Button button,
      boolean applyDynamicColor,
      boolean isButtonIconAtEnd,
      FooterButtonPartnerConfig footerButtonPartnerConfig) {

    // Save default text color for the partner config disable button text color not available.
    saveButtonDefaultTextColor(button);

    // If dynamic color enabled, these colors won't be overrode by partner config.
    // Instead, these colors align with the current theme colors.
    if (!applyDynamicColor) {
      // use default disable color util we support the partner disable text color
      if (button.isEnabled()) {
        FooterButtonStyleUtils.updateButtonTextEnabledColorWithPartnerConfig(
            context, button, footerButtonPartnerConfig.getButtonTextColorConfig());
      } else {
        FooterButtonStyleUtils.updateButtonTextDisabledColorWithPartnerConfig(
            context, button, footerButtonPartnerConfig.getButtonDisableTextColorConfig());
      }
      FooterButtonStyleUtils.updateButtonBackgroundWithPartnerConfig(
          context,
          button,
          footerButtonPartnerConfig.getButtonBackgroundConfig(),
          footerButtonPartnerConfig.getButtonDisableAlphaConfig(),
          footerButtonPartnerConfig.getButtonDisableBackgroundConfig());
    }
    FooterButtonStyleUtils.updateButtonRippleColorWithPartnerConfig(
        context,
        button,
        applyDynamicColor,
        footerButtonPartnerConfig.getButtonTextColorConfig(),
        footerButtonPartnerConfig.getButtonRippleColorAlphaConfig());
    FooterButtonStyleUtils.updateButtonMarginStartWithPartnerConfig(
        context, button, footerButtonPartnerConfig.getButtonMarginStartConfig());
    FooterButtonStyleUtils.updateButtonTextSizeWithPartnerConfig(
        context, button, footerButtonPartnerConfig.getButtonTextSizeConfig());
    FooterButtonStyleUtils.updateButtonMinHeightWithPartnerConfig(
        context, button, footerButtonPartnerConfig.getButtonMinHeightConfig());
    FooterButtonStyleUtils.updateButtonTypeFaceWithPartnerConfig(
        context,
        button,
        footerButtonPartnerConfig.getButtonTextTypeFaceConfig(),
        footerButtonPartnerConfig.getButtonTextWeightConfig(),
        footerButtonPartnerConfig.getButtonTextStyleConfig());
    FooterButtonStyleUtils.updateButtonRadiusWithPartnerConfig(
        context, button, footerButtonPartnerConfig.getButtonRadiusConfig());
    FooterButtonStyleUtils.updateButtonIconWithPartnerConfig(
        context, button, footerButtonPartnerConfig.getButtonIconConfig(), isButtonIconAtEnd);
  }

  static void updateButtonTextEnabledColorWithPartnerConfig(
      Context context, Button button, PartnerConfig buttonEnableTextColorConfig) {
    @ColorInt
    int color = PartnerConfigHelper.get(context).getColor(context, buttonEnableTextColorConfig);
    updateButtonTextEnabledColor(button, color);
  }

  static void updateButtonTextEnabledColor(Button button, @ColorInt int textColor) {
    if (textColor != Color.TRANSPARENT) {
      button.setTextColor(ColorStateList.valueOf(textColor));
    }
  }

  static void updateButtonTextDisabledColorWithPartnerConfig(
      Context context, Button button, PartnerConfig buttonDisableTextColorConfig) {
    if (PartnerConfigHelper.get(context).isPartnerConfigAvailable(buttonDisableTextColorConfig)) {
      @ColorInt
      int color = PartnerConfigHelper.get(context).getColor(context, buttonDisableTextColorConfig);
      updateButtonTextDisabledColor(button, color);
    } else {
      updateButtonTextDisableDefaultColor(button, getButtonDefaultTextCorlor(button));
    }
  }

  static void updateButtonTextDisabledColor(Button button, @ColorInt int textColor) {
    if (textColor != Color.TRANSPARENT) {
      button.setTextColor(ColorStateList.valueOf(textColor));
    }
  }

  static void updateButtonTextDisableDefaultColor(Button button, ColorStateList disabledTextColor) {
    button.setTextColor(disabledTextColor);
  }

  @TargetApi(VERSION_CODES.Q)
  static void updateButtonBackgroundWithPartnerConfig(
      Context context,
      Button button,
      PartnerConfig buttonBackgroundConfig,
      PartnerConfig buttonDisableAlphaConfig,
      PartnerConfig buttonDisableBackgroundConfig) {
    Preconditions.checkArgument(
        Build.VERSION.SDK_INT >= Build.VERSION_CODES.Q,
        "Update button background only support on sdk Q or higher");
    @ColorInt
    int color = PartnerConfigHelper.get(context).getColor(context, buttonBackgroundConfig);
    float disabledAlpha =
        PartnerConfigHelper.get(context).getFraction(context, buttonDisableAlphaConfig, 0f);
    @ColorInt
    int disabledColor =
        PartnerConfigHelper.get(context).getColor(context, buttonDisableBackgroundConfig);

    updateButtonBackgroundTintList(context, button, color, disabledAlpha, disabledColor);
  }

  @TargetApi(VERSION_CODES.Q)
  static void updateButtonBackgroundTintList(
      Context context,
      Button button,
      @ColorInt int color,
      float disabledAlpha,
      @ColorInt int disabledColor) {
    int[] DISABLED_STATE_SET = {-android.R.attr.state_enabled};
    int[] ENABLED_STATE_SET = {};

    if (color != Color.TRANSPARENT) {
      if (disabledAlpha <= 0f) {
        // if no partner resource, fallback to theme disable alpha
        TypedArray a = context.obtainStyledAttributes(new int[] {android.R.attr.disabledAlpha});
        float alpha = a.getFloat(0, DEFAULT_DISABLED_ALPHA);
        a.recycle();
        disabledAlpha = alpha;
      }
      if (disabledColor == Color.TRANSPARENT) {
        // if no partner resource, fallback to button background color
        disabledColor = color;
      }

      // Set text color for ripple.
      ColorStateList colorStateList =
          new ColorStateList(
              new int[][] {DISABLED_STATE_SET, ENABLED_STATE_SET},
              new int[] {convertRgbToArgb(disabledColor, disabledAlpha), color});

      // b/129482013: When a LayerDrawable is mutated, a new clone of its children drawables are
      // created, but without copying the state from the parent drawable. So even though the
      // parent is getting the correct drawable state from the view, the children won't get those
      // states until a state change happens.
      // As a workaround, we mutate the drawable and forcibly set the state to empty, and then
      // refresh the state so the children will have the updated states.
      button.getBackground().mutate().setState(new int[0]);
      button.refreshDrawableState();
      button.setBackgroundTintList(colorStateList);
    }
  }

  @TargetApi(VERSION_CODES.Q)
  static void updateButtonRippleColorWithPartnerConfig(
      Context context,
      Button button,
      boolean applyDynamicColor,
      PartnerConfig buttonTextColorConfig,
      PartnerConfig buttonRippleColorAlphaConfig) {
    if (Build.VERSION.SDK_INT >= VERSION_CODES.LOLLIPOP) {

      @ColorInt int textDefaultColor;
      if (applyDynamicColor) {
        // Get dynamic text color
        textDefaultColor = button.getTextColors().getDefaultColor();
      } else {
        // Get partner text color.
        textDefaultColor =
            PartnerConfigHelper.get(context).getColor(context, buttonTextColorConfig);
      }
      float alpha =
          PartnerConfigHelper.get(context).getFraction(context, buttonRippleColorAlphaConfig);
      updateButtonRippleColor(context, button, textDefaultColor, alpha);
    }
  }

  private static void updateButtonRippleColor(
      Context context, Button button, @ColorInt int textColor, float rippleAlpha) {
    // RippleDrawable is available after sdk 21. And because on lower sdk the RippleDrawable is
    // unavailable. Since Stencil customization provider only works on Q+, there is no need to
    // perform any customization for versions 21.
    if (Build.VERSION.SDK_INT >= VERSION_CODES.LOLLIPOP) {
      RippleDrawable rippleDrawable = getRippleDrawable(button);
      if (rippleDrawable == null) {
        return;
      }

      int[] pressedState = {android.R.attr.state_pressed};
      int[] focusState = {android.R.attr.state_focused};
      int argbColor = convertRgbToArgb(textColor, rippleAlpha);

      // Set text color for ripple.
      ColorStateList colorStateList =
          new ColorStateList(
              new int[][] {pressedState, focusState, StateSet.NOTHING},
              new int[] {argbColor, argbColor, Color.TRANSPARENT});
      if (PartnerConfigHelper.isGlifExpressiveEnabled(context)
          && button instanceof MaterialFooterActionButton) {
        MaterialFooterActionButton materialButton = (MaterialFooterActionButton) button;
        materialButton.setRippleColor(colorStateList);
      } else {
        rippleDrawable.setColor(colorStateList);
      }
    }
  }

  static void updateButtonMarginStartWithPartnerConfig(
      Context context, Button button, PartnerConfig buttonMarginStartConfig) {
    ViewGroup.LayoutParams lp = button.getLayoutParams();
    boolean partnerConfigAvailable =
        PartnerConfigHelper.get(context).isPartnerConfigAvailable(buttonMarginStartConfig);
    if (partnerConfigAvailable && lp instanceof ViewGroup.MarginLayoutParams) {
      final ViewGroup.MarginLayoutParams mlp = (ViewGroup.MarginLayoutParams) lp;
      int startMargin =
          (int) PartnerConfigHelper.get(context).getDimension(context, buttonMarginStartConfig);
      mlp.setMargins(startMargin, mlp.topMargin, mlp.rightMargin, mlp.bottomMargin);
    }
  }

  static void updateButtonTextSizeWithPartnerConfig(
      Context context, Button button, PartnerConfig buttonTextSizeConfig) {
    float size = PartnerConfigHelper.get(context).getDimension(context, buttonTextSizeConfig);
    if (size > 0) {
      button.setTextSize(TypedValue.COMPLEX_UNIT_PX, size);
    }
  }

  static void updateButtonMinHeightWithPartnerConfig(
      Context context, Button button, PartnerConfig buttonMinHeightConfig) {
    if (PartnerConfigHelper.get(context).isPartnerConfigAvailable(buttonMinHeightConfig)) {
      float size = PartnerConfigHelper.get(context).getDimension(context, buttonMinHeightConfig);
      if (size > 0) {
        button.setMinHeight((int) size);
      }
    }
  }

  @SuppressLint("NewApi") // Applying partner config should be guarded before Android S
  static void updateButtonTypeFaceWithPartnerConfig(
      Context context,
      Button button,
      PartnerConfig buttonTextTypeFaceConfig,
      PartnerConfig buttonTextWeightConfig,
      PartnerConfig buttonTextStyleConfig) {
    String fontFamilyName =
        PartnerConfigHelper.get(context).getString(context, buttonTextTypeFaceConfig);

    int textStyleValue = Typeface.NORMAL;
    if (PartnerConfigHelper.get(context).isPartnerConfigAvailable(buttonTextStyleConfig)) {
      textStyleValue =
          PartnerConfigHelper.get(context)
              .getInteger(context, buttonTextStyleConfig, Typeface.NORMAL);
    }

    Typeface font;
    int textWeightValue;
    if (isFontWeightEnabled(context)
        && PartnerConfigHelper.get(context).isPartnerConfigAvailable(buttonTextWeightConfig)) {
      textWeightValue =
          PartnerConfigHelper.get(context)
              .getInteger(context, buttonTextWeightConfig, FONT_WEIGHT_NORMAL);
      Typeface fontFamily = Typeface.create(fontFamilyName, textStyleValue);
      font = Typeface.create(fontFamily, textWeightValue, /* italic= */ false);
    } else {
      font = Typeface.create(fontFamilyName, textStyleValue);
    }
    if (font != null) {
      button.setTypeface(font);
    }
  }

  static void updateButtonRadiusWithPartnerConfig(
      Context context, Button button, PartnerConfig buttonRadiusConfig) {
    if (Build.VERSION.SDK_INT >= VERSION_CODES.N) {
      float radius = PartnerConfigHelper.get(context).getDimension(context, buttonRadiusConfig);
      if (PartnerConfigHelper.isGlifExpressiveEnabled(context)
          && button instanceof MaterialFooterActionButton) {
        MaterialFooterActionButton materialButton = (MaterialFooterActionButton) button;
        materialButton.setCornerRadius((int) radius);
      } else {
        GradientDrawable gradientDrawable = getGradientDrawable(button);
        if (gradientDrawable != null) {
          gradientDrawable.setCornerRadius(radius);
        }
      }
    }
  }

  static void updateButtonIconWithPartnerConfig(
      Context context, Button button, PartnerConfig buttonIconConfig, boolean isButtonIconAtEnd) {
    if (button == null) {
      return;
    }
    Drawable icon = null;
    if (buttonIconConfig != null) {
      icon = PartnerConfigHelper.get(context).getDrawable(context, buttonIconConfig);
    }
    setButtonIcon(button, icon, isButtonIconAtEnd);
  }

  private static void setButtonIcon(Button button, Drawable icon, boolean isButtonIconAtEnd) {
    if (button == null) {
      return;
    }

    if (icon != null) {
      // TODO: b/120488979 - restrict the icons to a reasonable size
      int h = icon.getIntrinsicHeight();
      int w = icon.getIntrinsicWidth();
      icon.setBounds(0, 0, w, h);
    }

    Drawable iconStart = null;
    Drawable iconEnd = null;
    if (isButtonIconAtEnd) {
      iconEnd = icon;
    } else {
      iconStart = icon;
    }
    button.setCompoundDrawablesRelative(iconStart, null, iconEnd, null);
  }

  static void updateButtonBackground(Button button, @ColorInt int color) {
    button.getBackground().mutate().setColorFilter(color, Mode.SRC_ATOP);
  }

  private static void saveButtonDefaultTextColor(Button button) {
    defaultTextColor.put(button.getId(), button.getTextColors());
  }

  private static ColorStateList getButtonDefaultTextCorlor(Button button) {
    if (!defaultTextColor.containsKey(button.getId())) {
      throw new IllegalStateException("There is no saved default color for button");
    }
    return defaultTextColor.get(button.getId());
  }

  static void clearSavedDefaultTextColor() {
    defaultTextColor.clear();
  }

  /** Gets {@code GradientDrawable} from given {@code button}. */
  @Nullable
  public static GradientDrawable getGradientDrawable(Button button) {
    // RippleDrawable is available after sdk 21, InsetDrawable#getDrawable is available after
    // sdk 19. So check the sdk is higher than sdk 21 and since Stencil customization provider only
    // works on Q+, there is no need to perform any customization for versions 21.
    if (Build.VERSION.SDK_INT >= VERSION_CODES.LOLLIPOP) {
      Drawable drawable = button.getBackground();
      if (drawable instanceof InsetDrawable) {
        LayerDrawable layerDrawable = (LayerDrawable) ((InsetDrawable) drawable).getDrawable();
        return (GradientDrawable) layerDrawable.getDrawable(0);
      } else if (drawable instanceof RippleDrawable) {
        if (((RippleDrawable) drawable).getDrawable(0) instanceof GradientDrawable) {
          return (GradientDrawable) ((RippleDrawable) drawable).getDrawable(0);
        }
        InsetDrawable insetDrawable = (InsetDrawable) ((RippleDrawable) drawable).getDrawable(0);
        return (GradientDrawable) insetDrawable.getDrawable();
      }
    }
    return null;
  }

  @Nullable
  static RippleDrawable getRippleDrawable(Button button) {
    // RippleDrawable is available after sdk 21. And because on lower sdk the RippleDrawable is
    // unavailable. Since Stencil customization provider only works on Q+, there is no need to
    // perform any customization for versions 21.
    if (Build.VERSION.SDK_INT >= VERSION_CODES.LOLLIPOP) {
      Drawable drawable = button.getBackground();
      if (drawable instanceof InsetDrawable) {
        return (RippleDrawable) ((InsetDrawable) drawable).getDrawable();
      } else if (drawable instanceof RippleDrawable) {
        return (RippleDrawable) drawable;
      }
    }
    return null;
  }

  @ColorInt
  private static int convertRgbToArgb(@ColorInt int color, float alpha) {
    return Color.argb((int) (alpha * 255), Color.red(color), Color.green(color), Color.blue(color));
  }

  private FooterButtonStyleUtils() {}
}
