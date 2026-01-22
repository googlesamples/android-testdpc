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

import android.widget.TextView;
import com.google.android.setupcompat.partnerconfig.PartnerConfig;
import com.google.android.setupdesign.util.TextViewPartnerStyler.TextPartnerConfigs;

/**
 * Applies the partner style of description to the given TextView {@code description}. The user
 * needs to check if the {@code description} should apply partner heavy theme or light theme before
 * calling this method, only heavy theme can apply for all configs.
 */
public final class DescriptionStyler {

  /**
   * Applies the partner heavy style of description to the given text view. Must check the current
   * text view applies partner customized configurations to heavy theme before applying.
   *
   * @param description A text view description resource
   */
  public static void applyPartnerCustomizationHeavyStyle(TextView description) {
    TextViewPartnerStyler.applyPartnerCustomizationStyle(
        description,
        new TextPartnerConfigs(
            PartnerConfig.CONFIG_DESCRIPTION_TEXT_COLOR,
            PartnerConfig.CONFIG_DESCRIPTION_LINK_TEXT_COLOR,
            PartnerConfig.CONFIG_DESCRIPTION_TEXT_SIZE,
            PartnerConfig.CONFIG_DESCRIPTION_FONT_FAMILY,
            PartnerConfig.CONFIG_DESCRIPTION_FONT_WEIGHT,
            PartnerConfig.CONFIG_DESCRIPTION_LINK_FONT_FAMILY,
            /* textMarginTopConfig= */ null,
            /* textMarginBottomConfig= */ null,
            PartnerStyleHelper.getLayoutGravity(description.getContext())));
  }

  /**
   * Applies the partner light style of description to the given text view. Must check the current
   * text view applies partner customized configurations to light theme before applying.
   *
   * @param description A text view description resource
   */
  public static void applyPartnerCustomizationLightStyle(TextView description) {
    TextViewPartnerStyler.applyPartnerCustomizationLightStyle(
        description,
        new TextPartnerConfigs(
            /* textColorConfig= */ null,
            /* textLinkedColorConfig= */ null,
            /* textSizeConfig= */ null,
            /* textFontFamilyConfig= */ null,
            /* textFontWeightConfig= */ null,
            /* textLinkFontFamilyConfig= */ null,
            /* textMarginTopConfig= */ null,
            /* textMarginBottomConfig= */ null,
            PartnerStyleHelper.getLayoutGravity(description.getContext())));
  }

  private DescriptionStyler() {}
}
