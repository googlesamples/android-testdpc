/*
 * Copyright (C) 2020 The Android Open Source Project
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

import android.annotation.TargetApi;
import android.content.Context;
import android.content.res.TypedArray;
import android.os.Build.VERSION_CODES;
import android.view.View;
import android.view.ViewGroup;
import androidx.annotation.Nullable;
import com.google.android.setupcompat.partnerconfig.PartnerConfig;
import com.google.android.setupcompat.partnerconfig.PartnerConfigHelper;
import com.google.android.setupdesign.R;

/**
 * Applies the partner style of layout to the given View {@code view}. The user needs to check if
 * the {@code view} should apply partner heavy theme before calling this method.
 */
public final class LayoutStyler {

  /**
   * Applies the partner layout padding style to the given view {@code view}. The theme should set
   * partner heavy theme config first, and then the partner layout style would be applied.
   *
   * @param view A view would be applied partner layout padding style
   */
  @TargetApi(VERSION_CODES.JELLY_BEAN_MR1)
  public static void applyPartnerCustomizationLayoutPaddingStyle(@Nullable View view) {
    if (view == null) {
      return;
    }

    Context context = view.getContext();
    boolean partnerMarginStartAvailable =
        PartnerConfigHelper.get(context)
            .isPartnerConfigAvailable(PartnerConfig.CONFIG_LAYOUT_MARGIN_START);
    boolean partnerMarginEndAvailable =
        PartnerConfigHelper.get(context)
            .isPartnerConfigAvailable(PartnerConfig.CONFIG_LAYOUT_MARGIN_END);

    // TODO: After all users added the check before calling the API, this check can be
    // deleted.
    if (PartnerStyleHelper.shouldApplyPartnerResource(view)
        && (partnerMarginStartAvailable || partnerMarginEndAvailable)) {
      int paddingStart;
      int paddingEnd;
      if (partnerMarginStartAvailable) {
        paddingStart =
            (int)
                PartnerConfigHelper.get(context)
                    .getDimension(context, PartnerConfig.CONFIG_LAYOUT_MARGIN_START);
      } else {
        paddingStart = view.getPaddingStart();
      }
      if (partnerMarginEndAvailable) {
        paddingEnd =
            (int)
                PartnerConfigHelper.get(context)
                    .getDimension(context, PartnerConfig.CONFIG_LAYOUT_MARGIN_END);
      } else {
        paddingEnd = view.getPaddingEnd();
      }

      if (paddingStart != view.getPaddingStart() || paddingEnd != view.getPaddingEnd()) {
        view.setPadding(paddingStart, view.getPaddingTop(), paddingEnd, view.getPaddingBottom());
      }
    }
  }

  /**
   * Applies the extra padding style to the given view {@code view}. This method is used when {@code
   * view} already sets its margin, and like to extra padding make view.margin + view.pendding =
   * global page margin.
   *
   * @param view A view would be applied extra padding style based on the layout margin of partner
   *     config.
   */
  @TargetApi(VERSION_CODES.JELLY_BEAN_MR1)
  public static void applyPartnerCustomizationExtraPaddingStyle(@Nullable View view) {
    if (view == null) {
      return;
    }

    Context context = view.getContext();
    boolean partnerMarginStartAvailable =
        PartnerConfigHelper.get(context)
            .isPartnerConfigAvailable(PartnerConfig.CONFIG_LAYOUT_MARGIN_START);
    boolean partnerMarginEndAvailable =
        PartnerConfigHelper.get(context)
            .isPartnerConfigAvailable(PartnerConfig.CONFIG_LAYOUT_MARGIN_END);

    // TODO: After all users added the check before calling the API, this check can be
    // deleted.
    if (PartnerStyleHelper.shouldApplyPartnerResource(view)
        && (partnerMarginStartAvailable || partnerMarginEndAvailable)) {
      int extraPaddingStart;
      int extraPaddingEnd;

      TypedArray a =
          context.obtainStyledAttributes(new int[] {R.attr.sudMarginStart, R.attr.sudMarginEnd});
      int layoutMarginStart = a.getDimensionPixelSize(0, 0);
      int layoutMarginEnd = a.getDimensionPixelSize(1, 0);
      a.recycle();

      if (partnerMarginStartAvailable) {
        extraPaddingStart =
            ((int)
                    PartnerConfigHelper.get(context)
                        .getDimension(context, PartnerConfig.CONFIG_LAYOUT_MARGIN_START))
                - layoutMarginStart;
      } else {
        extraPaddingStart = view.getPaddingStart();
      }

      if (partnerMarginEndAvailable) {
        extraPaddingEnd =
            ((int)
                    PartnerConfigHelper.get(context)
                        .getDimension(context, PartnerConfig.CONFIG_LAYOUT_MARGIN_END))
                - layoutMarginEnd;
        // If the view is a content view, padding start and padding end will be the same.
        if (view.getId() == R.id.sud_layout_content) {
          extraPaddingEnd =
              ((int)
                      PartnerConfigHelper.get(context)
                          .getDimension(context, PartnerConfig.CONFIG_LAYOUT_MARGIN_START))
                  - layoutMarginEnd;
        }
      } else {
        extraPaddingEnd = view.getPaddingEnd();
        // If the view is a content view, padding start and padding end will be the same.
        if (view.getId() == R.id.sud_layout_content) {
          extraPaddingEnd = view.getPaddingStart();
        }
      }

      if (extraPaddingStart != view.getPaddingStart() || extraPaddingEnd != view.getPaddingEnd()) {
        if (view.getId() == R.id.sud_layout_content) {
          // The sud_layout_content is framelayout.
          // The framelayout background maybe infected by this change.
          // Currently the content background is same as the activity background, and there is no
          // partner config to customize it.
          ViewGroup.LayoutParams params = view.getLayoutParams();
          ViewGroup.MarginLayoutParams marginLayoutParams;
          if (params instanceof ViewGroup.MarginLayoutParams) {
            marginLayoutParams = (ViewGroup.MarginLayoutParams) params;
          } else {
            marginLayoutParams = new ViewGroup.MarginLayoutParams(params);
          }
          marginLayoutParams.setMargins(
              extraPaddingStart, view.getPaddingTop(), extraPaddingEnd, view.getPaddingBottom());
        } else {
          view.setPadding(
              extraPaddingStart, view.getPaddingTop(), extraPaddingEnd, view.getPaddingBottom());
        }
      }
    }
  }

  private LayoutStyler() {}
}
