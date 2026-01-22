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

import static com.google.android.setupcompat.util.BuildCompatUtils.isAtLeastS;

import android.content.Context;
import android.graphics.Typeface;
import android.graphics.drawable.Drawable;
import android.graphics.drawable.VectorDrawable;
import android.os.Build;
import android.os.Build.VERSION;
import android.os.Build.VERSION_CODES;
import android.util.Log;
import android.util.TypedValue;
import android.view.ViewGroup;
import android.view.ViewGroup.LayoutParams;
import android.view.ViewTreeObserver;
import android.widget.FrameLayout;
import android.widget.ImageView;
import android.widget.ImageView.ScaleType;
import android.widget.LinearLayout;
import android.widget.ProgressBar;
import android.widget.TextView;
import androidx.annotation.Nullable;
import androidx.annotation.VisibleForTesting;
import androidx.vectordrawable.graphics.drawable.VectorDrawableCompat;
import com.google.android.setupcompat.partnerconfig.PartnerConfig;
import com.google.android.setupcompat.partnerconfig.PartnerConfigHelper;
import com.google.android.setupdesign.R;
import com.google.android.setupdesign.util.TextViewPartnerStyler.TextPartnerConfigs;

/**
 * Applies the partner customization for the header area widgets. The user needs to check if the
 * header area widgets should apply partner heavy theme or light theme before calling these methods.
 */
public final class HeaderAreaStyler {

  private static final String TAG = "HeaderAreaStyler";

  @VisibleForTesting
  static final String WARN_TO_USE_DRAWABLE =
      "To achieve scaling icon in SetupDesign lib, should use vector drawable icon from ";

  /**
   * Applies the partner style of header text to the given textView {@code header}.
   *
   * @param header A header text would apply partner style
   */
  public static void applyPartnerCustomizationHeaderStyle(@Nullable TextView header) {

    if (header == null) {
      return;
    }
    TextViewPartnerStyler.applyPartnerCustomizationStyle(
        header,
        new TextPartnerConfigs(
            PartnerConfig.CONFIG_HEADER_TEXT_COLOR,
            /* textLinkedColorConfig= */ null,
            PartnerConfig.CONFIG_HEADER_TEXT_SIZE,
            PartnerConfig.CONFIG_HEADER_FONT_FAMILY,
            PartnerConfig.CONFIG_HEADER_FONT_WEIGHT,
            /* textLinkFontFamilyConfig= */ null,
            PartnerConfig.CONFIG_HEADER_TEXT_MARGIN_TOP,
            PartnerConfig.CONFIG_HEADER_TEXT_MARGIN_BOTTOM,
            PartnerStyleHelper.getLayoutGravity(header.getContext())));
  }

  /**
   * Applies the partner heavy style of description text to the given textView {@code description}.
   *
   * @param description A description text would apply partner heavy style
   */
  public static void applyPartnerCustomizationDescriptionHeavyStyle(
      @Nullable TextView description) {

    if (description == null) {
      return;
    }
    TextViewPartnerStyler.applyPartnerCustomizationStyle(
        description,
        new TextPartnerConfigs(
            PartnerConfig.CONFIG_DESCRIPTION_TEXT_COLOR,
            PartnerConfig.CONFIG_DESCRIPTION_LINK_TEXT_COLOR,
            PartnerConfig.CONFIG_DESCRIPTION_TEXT_SIZE,
            PartnerConfig.CONFIG_DESCRIPTION_FONT_FAMILY,
            PartnerConfig.CONFIG_DESCRIPTION_FONT_WEIGHT,
            PartnerConfig.CONFIG_DESCRIPTION_LINK_FONT_FAMILY,
            PartnerConfig.CONFIG_DESCRIPTION_TEXT_MARGIN_TOP,
            PartnerConfig.CONFIG_DESCRIPTION_TEXT_MARGIN_BOTTOM,
            PartnerStyleHelper.getLayoutGravity(description.getContext())));
  }

  public static void applyPartnerCustomizationAccountStyle(
      ImageView avatar, TextView name, LinearLayout container) {
    if (avatar == null || name == null) {
      return;
    }

    Context context = avatar.getContext();

    ViewGroup.LayoutParams lpIcon = avatar.getLayoutParams();
    if (lpIcon instanceof ViewGroup.MarginLayoutParams) {
      ViewGroup.MarginLayoutParams mlp = (ViewGroup.MarginLayoutParams) lpIcon;

      int rightMargin =
          (int)
              PartnerConfigHelper.get(context)
                  .getDimension(context, PartnerConfig.CONFIG_ACCOUNT_AVATAR_MARGIN_END);
      mlp.setMargins(mlp.leftMargin, mlp.topMargin, rightMargin, mlp.bottomMargin);
    }

    int maxHeight =
        (int)
            PartnerConfigHelper.get(context)
                .getDimension(context, PartnerConfig.CONFIG_ACCOUNT_AVATAR_SIZE,
                context.getResources().getDimension(R.dimen.sud_account_avatar_max_height));
    avatar.setMaxHeight(maxHeight);

    int textSize =
        (int)
            PartnerConfigHelper.get(context)
                .getDimension(
                    context,
                    PartnerConfig.CONFIG_ACCOUNT_NAME_TEXT_SIZE,
                    context.getResources().getDimension(R.dimen.sud_account_name_text_size));
    name.setTextSize(TypedValue.COMPLEX_UNIT_PX, textSize);

    String textFamily =
            PartnerConfigHelper.get(context)
                .getString(context, PartnerConfig.CONFIG_ACCOUNT_NAME_FONT_FAMILY);
    Typeface font = Typeface.create(textFamily, Typeface.NORMAL);
    if (font != null) {
      name.setTypeface(font);
    }

    int gravity = PartnerStyleHelper.getLayoutGravity(container.getContext());
    container.setGravity(gravity);
  }

  /**
   * Applies the partner style of header area to the given layout {@code headerArea}. The theme
   * should set partner heavy theme first, and then the partner style of header would be applied. Ａs
   * for the margin bottom of header, it would also be appied when heavy theme parter config is
   * enabled.
   *
   * @param headerArea A ViewGroup would apply the partner style of header area
   */
  public static void applyPartnerCustomizationHeaderAreaStyle(ViewGroup headerArea) {
    if (headerArea == null) {
      return;
    }

    Context context = headerArea.getContext();
    int color =
        PartnerConfigHelper.get(context)
            .getColor(context, PartnerConfig.CONFIG_HEADER_AREA_BACKGROUND_COLOR);
    headerArea.setBackgroundColor(color);

    if (PartnerConfigHelper.get(context)
        .isPartnerConfigAvailable(PartnerConfig.CONFIG_HEADER_CONTAINER_MARGIN_BOTTOM)) {
      final ViewGroup.LayoutParams lp = headerArea.getLayoutParams();
      if (lp instanceof ViewGroup.MarginLayoutParams) {
        final ViewGroup.MarginLayoutParams mlp = (ViewGroup.MarginLayoutParams) lp;

        int bottomMargin =
            (int)
                PartnerConfigHelper.get(context)
                    .getDimension(context, PartnerConfig.CONFIG_HEADER_CONTAINER_MARGIN_BOTTOM);
        mlp.setMargins(mlp.leftMargin, mlp.topMargin, mlp.rightMargin, bottomMargin);
        headerArea.setLayoutParams(lp);
      }
    }
  }

  public static void applyPartnerCustomizationProgressBarStyle(@Nullable ProgressBar progressBar) {
    if (progressBar == null) {
      return;
    }
    Context context = progressBar.getContext();
    final ViewGroup.LayoutParams lp = progressBar.getLayoutParams();

    if (lp instanceof ViewGroup.MarginLayoutParams) {
      final ViewGroup.MarginLayoutParams mlp = (ViewGroup.MarginLayoutParams) lp;
      int marginTop = mlp.topMargin;
      if (PartnerConfigHelper.get(context)
          .isPartnerConfigAvailable(PartnerConfig.CONFIG_PROGRESS_BAR_MARGIN_TOP)) {
        marginTop =
            (int)
                PartnerConfigHelper.get(context)
                    .getDimension(
                        context,
                        PartnerConfig.CONFIG_PROGRESS_BAR_MARGIN_TOP,
                        context.getResources().getDimension(R.dimen.sud_progress_bar_margin_top));
      }
      int marginBottom = mlp.bottomMargin;
      if (PartnerConfigHelper.get(context)
          .isPartnerConfigAvailable(PartnerConfig.CONFIG_PROGRESS_BAR_MARGIN_BOTTOM)) {
        marginBottom =
            (int)
                PartnerConfigHelper.get(context)
                    .getDimension(
                        context,
                        PartnerConfig.CONFIG_PROGRESS_BAR_MARGIN_BOTTOM,
                        context
                            .getResources()
                            .getDimension(R.dimen.sud_progress_bar_margin_bottom));
      }

      if (marginTop != mlp.topMargin || marginBottom != mlp.bottomMargin) {
        mlp.setMargins(mlp.leftMargin, marginTop, mlp.rightMargin, marginBottom);
      }
    }
  }

  /**
   * Applies the partner style of header icon to the given {@code iconImage}. It needs to check if
   * it should apply partner resource first, and then the partner icon size would be applied.
   *
   * @param iconImage A ImageView would apply the partner style of header icon
   * @param iconContainer The container of the header icon
   */
  public static void applyPartnerCustomizationIconStyle(
      @Nullable ImageView iconImage, FrameLayout iconContainer) {
    if (iconImage == null || iconContainer == null) {
      return;
    }

    Context context = iconImage.getContext();
    int reducedIconHeight = 0;
    int gravity = PartnerStyleHelper.getLayoutGravity(context);
    // Skip the partner customization when isGlifExpressiveEnabled is true
    if (gravity != 0 && !PartnerConfigHelper.isGlifExpressiveEnabled(context)) {
      setGravity(iconImage, gravity);
    }

    if (PartnerConfigHelper.get(context).isPartnerConfigAvailable(PartnerConfig.CONFIG_ICON_SIZE)) {
      checkImageType(iconImage);

      final ViewGroup.LayoutParams lpIcon = iconImage.getLayoutParams();

      lpIcon.height =
          (int)
              PartnerConfigHelper.get(context)
                  .getDimension(context, PartnerConfig.CONFIG_ICON_SIZE);

      lpIcon.width = LayoutParams.WRAP_CONTENT;
      iconImage.setScaleType(ScaleType.FIT_CENTER);

      Drawable drawable = iconImage.getDrawable();
      if (drawable != null && drawable.getIntrinsicWidth() > (2 * drawable.getIntrinsicHeight())) {
        int fixedIconHeight =
            (int) context.getResources().getDimension(R.dimen.sud_horizontal_icon_height);
        if (lpIcon.height > fixedIconHeight) {
          reducedIconHeight = lpIcon.height - fixedIconHeight;
          lpIcon.height = fixedIconHeight;
        }
      }
    }

    final ViewGroup.LayoutParams lp = iconContainer.getLayoutParams();
    boolean partnerConfigAvailable =
        PartnerConfigHelper.get(context)
            .isPartnerConfigAvailable(PartnerConfig.CONFIG_ICON_MARGIN_TOP);
    if (partnerConfigAvailable && lp instanceof ViewGroup.MarginLayoutParams) {
      final ViewGroup.MarginLayoutParams mlp = (ViewGroup.MarginLayoutParams) lp;
      int topMargin =
          (int)
              PartnerConfigHelper.get(context)
                  .getDimension(context, PartnerConfig.CONFIG_ICON_MARGIN_TOP);
      topMargin += reducedIconHeight;
      mlp.setMargins(mlp.leftMargin, topMargin, mlp.rightMargin, mlp.bottomMargin);
    }
  }

  /**
   * Applies the partner style of back button to center align the icon. It needs to check if it
   * should apply partner resource first, and then adjust the margin top according to the partner
   * icon size.
   *
   * @param buttonContainer The container of the back button
   */
  public static void applyPartnerCustomizationBackButtonStyle(FrameLayout buttonContainer) {
    if (buttonContainer == null) {
      return;
    }

    Context context = buttonContainer.getContext();
    int heightDifference = 0;

    // Calculate the difference of icon height & button height
    ViewGroup.LayoutParams lpButtonContainer = buttonContainer.getLayoutParams();
    int backButtonHeight =
        (int) context.getResources().getDimension(R.dimen.sud_glif_expressive_back_button_height);
    int iconHeight = getPartnerConfigDimension(context, PartnerConfig.CONFIG_ICON_SIZE, 0);
    if (iconHeight > backButtonHeight) {
      heightDifference = iconHeight - backButtonHeight;
    }

    // Adjust margin top of button container to vertically center align the icon
      ViewGroup.MarginLayoutParams mlp = (ViewGroup.MarginLayoutParams) lpButtonContainer;
      // The default topMargin should align with the icon margin top
      int topMargin =
          getPartnerConfigDimension(context, PartnerConfig.CONFIG_ICON_MARGIN_TOP, mlp.topMargin);
    int adjustedTopMargin = topMargin;
    if (heightDifference != 0) {
      adjustedTopMargin = topMargin + heightDifference / 2;
    }

    if (adjustedTopMargin != mlp.topMargin) {
      FrameLayout.LayoutParams params =
          new FrameLayout.LayoutParams(LayoutParams.WRAP_CONTENT, LayoutParams.WRAP_CONTENT);
      params.setMargins(mlp.leftMargin, adjustedTopMargin, mlp.rightMargin, mlp.bottomMargin);
      buttonContainer.setLayoutParams(params);
    }
  }

  private static int getPartnerConfigDimension(
      Context context, PartnerConfig config, int defaultValue) {
    if (PartnerConfigHelper.get(context).isPartnerConfigAvailable(config)) {
      return (int) PartnerConfigHelper.get(context).getDimension(context, config);
    }

    return defaultValue;
  }

  private static void checkImageType(ImageView imageView) {
    ViewTreeObserver vto = imageView.getViewTreeObserver();
    vto.addOnPreDrawListener(
        new ViewTreeObserver.OnPreDrawListener() {
          @Override
          public boolean onPreDraw() {
            imageView.getViewTreeObserver().removeOnPreDrawListener(this);
            // TODO: Remove when Partners all used Drawable icon image and never use
            if (isAtLeastS()
                && !(imageView.getDrawable() == null
                    || (VERSION.SDK_INT >= VERSION_CODES.LOLLIPOP
                        && imageView.getDrawable() instanceof VectorDrawable)
                    || imageView.getDrawable() instanceof VectorDrawableCompat)
                && (Build.TYPE.equals("userdebug") || Build.TYPE.equals("eng"))) {
              Log.w(TAG, WARN_TO_USE_DRAWABLE + imageView.getContext().getPackageName());
            }
            return true;
          }
        });
  }

  private static void setGravity(ImageView icon, int gravity) {
    if (icon.getLayoutParams() instanceof FrameLayout.LayoutParams) {
      FrameLayout.LayoutParams layoutParams = (FrameLayout.LayoutParams) icon.getLayoutParams();
      layoutParams.gravity = gravity;
      icon.setLayoutParams(layoutParams);
    }
  }

  private HeaderAreaStyler() {}
}
