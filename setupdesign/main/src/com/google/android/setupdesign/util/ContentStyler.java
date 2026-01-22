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

import android.content.Context;
import android.os.Build;
import android.os.Build.VERSION;
import android.os.Build.VERSION_CODES;
import android.view.Gravity;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageView;
import android.widget.ImageView.ScaleType;
import android.widget.TextView;
import androidx.annotation.Nullable;
import com.google.android.setupcompat.partnerconfig.PartnerConfig;
import com.google.android.setupcompat.partnerconfig.PartnerConfigHelper;
import com.google.android.setupdesign.R;
import com.google.android.setupdesign.util.TextViewPartnerStyler.TextPartnerConfigs;
import java.util.Locale;

/**
 * Applies the partner style of content to the given TextView {@code contentText}. The theme should
 * set partner heavy theme first, and then the partner style of content would be applied. The user
 * can also check if the {@code contentText} should apply partner heavy theme before calling this
 * method.
 */
public final class ContentStyler {
  public static void applyBodyPartnerCustomizationStyle(TextView contentText) {
    // TODO: Remove the check of applying the heavy theme.
    if (!PartnerStyleHelper.shouldApplyPartnerHeavyThemeResource(contentText)) {
      return;
    }

    TextViewPartnerStyler.applyPartnerCustomizationStyle(
        contentText,
        new TextPartnerConfigs(
            PartnerConfig.CONFIG_CONTENT_TEXT_COLOR,
            PartnerConfig.CONFIG_CONTENT_LINK_TEXT_COLOR,
            PartnerConfig.CONFIG_CONTENT_TEXT_SIZE,
            PartnerConfig.CONFIG_CONTENT_FONT_FAMILY,
            /* textFontWeightConfig= */ null,
            PartnerConfig.CONFIG_DESCRIPTION_LINK_FONT_FAMILY,
            /* textMarginTopConfig= */ null,
            /* textMarginBottomConfig= */ null,
            ContentStyler.getPartnerContentTextGravity(contentText.getContext())));
  }

  /**
   * Applies the partner heavy style of content info to the given views including content info
   * container, content info icon and content info text, the given views should be included in a
   * layout which the same view hierarchy with {@link R.layout#sud_content_info}.
   *
   * @param infoContainer A view the container resource of content info
   * @param infoIcon A image view the icon resource of content info
   * @param infoText A text view content info resource
   */
  public static void applyInfoPartnerCustomizationStyle(
      @Nullable View infoContainer, @Nullable ImageView infoIcon, TextView infoText) {
    // TODO: Remove the check of applying the heavy theme.
    if (!PartnerStyleHelper.shouldApplyPartnerHeavyThemeResource(infoText)) {
      return;
    }

    Context context = infoText.getContext();

    boolean textSizeConfigAvailable =
        PartnerConfigHelper.get(context)
            .isPartnerConfigAvailable(PartnerConfig.CONFIG_CONTENT_INFO_TEXT_SIZE);
    boolean fontFamilyConfigAvailable =
        PartnerConfigHelper.get(context)
            .isPartnerConfigAvailable(PartnerConfig.CONFIG_CONTENT_INFO_FONT_FAMILY);
    boolean linkFontFamilyConfigAvailable =
        PartnerConfigHelper.get(context)
            .isPartnerConfigAvailable(PartnerConfig.CONFIG_DESCRIPTION_LINK_FONT_FAMILY);

    TextViewPartnerStyler.applyPartnerCustomizationStyle(
        infoText,
        new TextPartnerConfigs(
            /* textColorConfig= */ null,
            /* textLinkedColorConfig= */ null,
            textSizeConfigAvailable ? PartnerConfig.CONFIG_CONTENT_INFO_TEXT_SIZE : null,
            fontFamilyConfigAvailable ? PartnerConfig.CONFIG_CONTENT_INFO_FONT_FAMILY : null,
            /* textFontWeightConfig= */ null,
            linkFontFamilyConfigAvailable
                ? PartnerConfig.CONFIG_DESCRIPTION_LINK_FONT_FAMILY
                : null,
            /* textMarginTopConfig= */ null,
            /* textMarginBottomConfig= */ null,
            /* textGravity= */ 0));

    // TODO: Move CONFIG_CONTENT_INFO_LINE_SPACING_EXTRA to TextPartnerConfigs for
    // customize
    boolean isAtLeastP = VERSION.SDK_INT >= VERSION_CODES.P;
    if (isAtLeastP
        && PartnerConfigHelper.get(context)
            .isPartnerConfigAvailable(PartnerConfig.CONFIG_CONTENT_INFO_LINE_SPACING_EXTRA)) {
      int textLineSpacingExtraInPx =
          (int)
              PartnerConfigHelper.get(context)
                  .getDimension(context, PartnerConfig.CONFIG_CONTENT_INFO_LINE_SPACING_EXTRA);

      float infoTextSizeInPx = infoText.getTextSize();
      if (textSizeConfigAvailable) {
        float textSizeInPx =
            PartnerConfigHelper.get(context)
                .getDimension(context, PartnerConfig.CONFIG_CONTENT_INFO_TEXT_SIZE, 0);
        if (textSizeInPx > 0) {
          infoTextSizeInPx = textSizeInPx;
        }
      }

      if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.P) {
        infoText.setLineHeight(Math.round(textLineSpacingExtraInPx + infoTextSizeInPx));
      }
    }

    if (infoIcon != null) {
      ViewGroup.LayoutParams lp = infoIcon.getLayoutParams();

      if (PartnerConfigHelper.get(context)
          .isPartnerConfigAvailable(PartnerConfig.CONFIG_CONTENT_INFO_ICON_SIZE)) {
        int oldHeight = lp.height;
        lp.height =
            (int)
                PartnerConfigHelper.get(context)
                    .getDimension(context, PartnerConfig.CONFIG_CONTENT_INFO_ICON_SIZE);
        // The scale ratio is lp.height/oldHeight, but the lp.height and oldHeight are all integer type.
        // The division between integer will loss the decimal part, so need to do multiple first.
        lp.width = lp.width * lp.height / oldHeight;
        infoIcon.setScaleType(ScaleType.FIT_CENTER);
      }

      boolean partnerConfigAvailable =
          PartnerConfigHelper.get(context)
              .isPartnerConfigAvailable(PartnerConfig.CONFIG_CONTENT_INFO_ICON_MARGIN_END);
      if (partnerConfigAvailable && lp instanceof ViewGroup.MarginLayoutParams) {
        final ViewGroup.MarginLayoutParams mlp = (ViewGroup.MarginLayoutParams) lp;
        int endMargin =
            (int)
                PartnerConfigHelper.get(context)
                    .getDimension(context, PartnerConfig.CONFIG_CONTENT_INFO_ICON_MARGIN_END);
        mlp.setMargins(mlp.leftMargin, mlp.topMargin, endMargin, mlp.bottomMargin);
      }
    }

    if (infoContainer != null) {
      float paddingTop;
      if (PartnerConfigHelper.get(context)
          .isPartnerConfigAvailable(PartnerConfig.CONFIG_CONTENT_INFO_PADDING_TOP)) {
        paddingTop =
            PartnerConfigHelper.get(context)
                .getDimension(context, PartnerConfig.CONFIG_CONTENT_INFO_PADDING_TOP);
      } else {
        paddingTop = infoContainer.getPaddingTop();
      }

      float paddingBottom;
      if (PartnerConfigHelper.get(context)
          .isPartnerConfigAvailable(PartnerConfig.CONFIG_CONTENT_INFO_PADDING_BOTTOM)) {
        paddingBottom =
            PartnerConfigHelper.get(context)
                .getDimension(context, PartnerConfig.CONFIG_CONTENT_INFO_PADDING_BOTTOM);
      } else {
        paddingBottom = infoContainer.getPaddingBottom();
      }

      if (paddingTop != infoContainer.getPaddingTop()
          || paddingBottom != infoContainer.getPaddingBottom()) {
        infoContainer.setPadding(0, (int) paddingTop, 0, (int) paddingBottom);
      }
    }
  }

  /**
   * Returns the layout margin start from partner config. If the activity of given {@code context}
   * does not enable the partner heavy theme, then returns the default value from GlifTheme.
   *
   * @param context The context of a GlifLayout activity.
   */
  public static float getPartnerContentMarginStart(Context context) {
    // default value is GlifTheme layout margin start.
    // That is the attr sudMarginStart, and the value is sud_layout_margin_sides.
    float result = context.getResources().getDimension(R.dimen.sud_layout_margin_sides);
    if (PartnerConfigHelper.get(context)
        .isPartnerConfigAvailable(PartnerConfig.CONFIG_LAYOUT_MARGIN_START)) {
      result =
          PartnerConfigHelper.get(context)
              .getDimension(context, PartnerConfig.CONFIG_LAYOUT_MARGIN_START, result);
    }
    return result;
  }

  private static int getPartnerContentTextGravity(Context context) {
    String gravity =
        PartnerConfigHelper.get(context)
            .getString(context, PartnerConfig.CONFIG_CONTENT_LAYOUT_GRAVITY);
    if (gravity == null) {
      return 0;
    }
    switch (gravity.toLowerCase(Locale.ROOT)) {
      case "center":
        return Gravity.CENTER;
      case "start":
        return Gravity.START;
      case "end":
        return Gravity.END;
      default:
        return 0;
    }
  }

  private ContentStyler() {}
}
