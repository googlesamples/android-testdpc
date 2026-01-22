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

import static com.google.android.setupcompat.partnerconfig.PartnerConfigHelper.isFontWeightEnabled;

import android.annotation.SuppressLint;
import android.content.Context;
import android.graphics.Typeface;
import android.util.TypedValue;
import android.view.ViewGroup;
import android.widget.LinearLayout;
import android.widget.TextView;
import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import com.google.android.setupcompat.partnerconfig.PartnerConfig;
import com.google.android.setupcompat.partnerconfig.PartnerConfigHelper;
import com.google.android.setupdesign.view.RichTextView;

/** Helper class to apply partner configurations to a textView. */
final class TextViewPartnerStyler {

  /** Normal font weight. */
  private static final int FONT_WEIGHT_NORMAL = 400;

  /** Applies given partner configurations {@code textPartnerConfigs} to the {@code textView}. */
  @SuppressLint("NewApi") // Applying partner config should be guarded before Android S
  public static void applyPartnerCustomizationStyle(
      @NonNull TextView textView, @NonNull TextPartnerConfigs textPartnerConfigs) {

    if (textView == null || textPartnerConfigs == null) {
      return;
    }

    Context context = textView.getContext();
    if (textPartnerConfigs.getTextColorConfig() != null
        && PartnerConfigHelper.get(context)
            .isPartnerConfigAvailable(textPartnerConfigs.getTextColorConfig())) {
      int textColor =
          PartnerConfigHelper.get(context)
              .getColor(context, textPartnerConfigs.getTextColorConfig());
      if (textColor != 0) {
        textView.setTextColor(textColor);
      }
    }

    if (textPartnerConfigs.getTextLinkedColorConfig() != null
        && PartnerConfigHelper.get(context)
            .isPartnerConfigAvailable(textPartnerConfigs.getTextLinkedColorConfig())
        && !PartnerStyleHelper.useDynamicColor(textView)) {
      int linkTextColor =
          PartnerConfigHelper.get(context)
              .getColor(context, textPartnerConfigs.getTextLinkedColorConfig());
      if (linkTextColor != 0) {
        textView.setLinkTextColor(linkTextColor);
      }
    }

    if (textPartnerConfigs.getTextSizeConfig() != null
        && PartnerConfigHelper.get(context)
            .isPartnerConfigAvailable(textPartnerConfigs.getTextSizeConfig())) {
      float textSize =
          PartnerConfigHelper.get(context)
              .getDimension(context, textPartnerConfigs.getTextSizeConfig(), 0);
      if (textSize > 0) {
        textView.setTextSize(TypedValue.COMPLEX_UNIT_PX, textSize);
      }
    }

    Typeface fontFamily = null;
    if (textPartnerConfigs.getTextFontFamilyConfig() != null
        && PartnerConfigHelper.get(context)
            .isPartnerConfigAvailable(textPartnerConfigs.getTextFontFamilyConfig())) {
      String fontFamilyName =
          PartnerConfigHelper.get(context)
              .getString(context, textPartnerConfigs.getTextFontFamilyConfig());
      fontFamily = Typeface.create(fontFamilyName, Typeface.NORMAL);
    }

    Typeface font;
    if (isFontWeightEnabled(context)
        && textPartnerConfigs.getTextFontWeightConfig() != null
        && PartnerConfigHelper.get(context)
            .isPartnerConfigAvailable(textPartnerConfigs.getTextFontWeightConfig())) {
      int weight =
          PartnerConfigHelper.get(context)
              .getInteger(
                  context, textPartnerConfigs.getTextFontWeightConfig(), FONT_WEIGHT_NORMAL);
      if (fontFamily == null) {
        fontFamily = textView.getTypeface();
      }
      font = Typeface.create(fontFamily, weight, /* italic= */ false);
    } else {
      font = fontFamily;
    }

    if (font != null) {
      textView.setTypeface(font);
    }

    if (textView instanceof RichTextView && textPartnerConfigs.getLinkTextFontFamilyConfig() != null
        && PartnerConfigHelper.get(context)
        .isPartnerConfigAvailable(textPartnerConfigs.getLinkTextFontFamilyConfig())) {
      String linkFontFamilyName =
          PartnerConfigHelper.get(context)
              .getString(context, textPartnerConfigs.getLinkTextFontFamilyConfig());
      Typeface linkFont = Typeface.create(linkFontFamilyName, Typeface.NORMAL);
      if (linkFont != null) {
        ((RichTextView) textView).setSpanTypeface(linkFont);
      }
    }

    applyPartnerCustomizationVerticalMargins(textView, textPartnerConfigs);
    textView.setGravity(textPartnerConfigs.getTextGravity());
  }

  /**
   * Applies given partner configurations {@code textPartnerConfigs} to the {@code textView}.
   *
   * @param textView A text view would apply the gravity
   * @param textPartnerConfigs A partner conflagrations contains text gravity would be set
   */
  public static void applyPartnerCustomizationLightStyle(
      @NonNull TextView textView, @NonNull TextPartnerConfigs textPartnerConfigs) {

    if (textView == null || textPartnerConfigs == null) {
      return;
    }

    applyPartnerCustomizationVerticalMargins(textView, textPartnerConfigs);
    textView.setGravity(textPartnerConfigs.getTextGravity());
  }

  private static void applyPartnerCustomizationVerticalMargins(
      @NonNull TextView textView, @NonNull TextPartnerConfigs textPartnerConfigs) {
    if (textPartnerConfigs.getTextMarginTop() != null
        || textPartnerConfigs.getTextMarginBottom() != null) {
      Context context = textView.getContext();
      int topMargin;
      int bottomMargin;
      final ViewGroup.LayoutParams lp = textView.getLayoutParams();
      if (lp instanceof LinearLayout.LayoutParams) {
        final LinearLayout.LayoutParams mlp = (LinearLayout.LayoutParams) lp;
        if (textPartnerConfigs.getTextMarginTop() != null
            && PartnerConfigHelper.get(context)
                .isPartnerConfigAvailable(textPartnerConfigs.getTextMarginTop())) {
          topMargin =
              (int)
                  PartnerConfigHelper.get(context)
                      .getDimension(context, textPartnerConfigs.getTextMarginTop());
        } else {
          topMargin = mlp.topMargin;
        }

        if (textPartnerConfigs.getTextMarginBottom() != null
            && PartnerConfigHelper.get(context)
                .isPartnerConfigAvailable(textPartnerConfigs.getTextMarginBottom())) {
          bottomMargin =
              (int)
                  PartnerConfigHelper.get(context)
                      .getDimension(context, textPartnerConfigs.getTextMarginBottom());
        } else {
          bottomMargin = mlp.bottomMargin;
        }
        mlp.setMargins(mlp.leftMargin, topMargin, mlp.rightMargin, bottomMargin);
        textView.setLayoutParams(lp);
      }
    }
  }

  /** Keeps the partner conflagrations for a textView. */
  public static class TextPartnerConfigs {
    private final PartnerConfig textColorConfig;
    private final PartnerConfig textLinkedColorConfig;
    private final PartnerConfig textSizeConfig;
    private final PartnerConfig textFontFamilyConfig;
    private final PartnerConfig textFontWeightConfig;
    private final PartnerConfig textLinkFontFamilyConfig;
    private final PartnerConfig textMarginTopConfig;
    private final PartnerConfig textMarginBottomConfig;
    private final int textGravity;

    public TextPartnerConfigs(
        @Nullable PartnerConfig textColorConfig,
        @Nullable PartnerConfig textLinkedColorConfig,
        @Nullable PartnerConfig textSizeConfig,
        @Nullable PartnerConfig textFontFamilyConfig,
        @Nullable PartnerConfig textFontWeightConfig,
        @Nullable PartnerConfig textLinkFontFamilyConfig,
        @Nullable PartnerConfig textMarginTopConfig,
        @Nullable PartnerConfig textMarginBottomConfig,
        int textGravity) {
      this.textColorConfig = textColorConfig;
      this.textLinkedColorConfig = textLinkedColorConfig;
      this.textSizeConfig = textSizeConfig;
      this.textFontFamilyConfig = textFontFamilyConfig;
      this.textFontWeightConfig = textFontWeightConfig;
      this.textLinkFontFamilyConfig = textLinkFontFamilyConfig;
      this.textMarginTopConfig = textMarginTopConfig;
      this.textMarginBottomConfig = textMarginBottomConfig;
      this.textGravity = textGravity;
    }

    public PartnerConfig getTextColorConfig() {
      return textColorConfig;
    }

    public PartnerConfig getTextLinkedColorConfig() {
      return textLinkedColorConfig;
    }

    public PartnerConfig getTextSizeConfig() {
      return textSizeConfig;
    }

    public PartnerConfig getTextFontFamilyConfig() {
      return textFontFamilyConfig;
    }

    public PartnerConfig getTextFontWeightConfig() {
      return textFontWeightConfig;
    }

    public PartnerConfig getLinkTextFontFamilyConfig() {
      return textLinkFontFamilyConfig;
    }

    public PartnerConfig getTextMarginTop() {
      return textMarginTopConfig;
    }

    public PartnerConfig getTextMarginBottom() {
      return textMarginBottomConfig;
    }

    public int getTextGravity() {
      return textGravity;
    }
  }

  private TextViewPartnerStyler() {}
}
