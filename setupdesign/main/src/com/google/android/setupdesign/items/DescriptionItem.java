/*
 * Copyright (C) 2015 The Android Open Source Project
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

package com.google.android.setupdesign.items;

import android.content.Context;
import android.util.AttributeSet;
import android.view.View;
import android.widget.FrameLayout;
import android.widget.TextView;
import com.google.android.setupcompat.PartnerCustomizationLayout;
import com.google.android.setupdesign.GlifLayout;
import com.google.android.setupdesign.R;
import com.google.android.setupdesign.util.DescriptionStyler;

/**
 * Definition of an item in an {@link ItemHierarchy}. An item is usually defined in XML and inflated
 * using {@link ItemInflater}.
 *
 * @deprecated Use {@link com.google.android.setupdesign.template.DescriptionMixin} instead.
 */
@Deprecated
public class DescriptionItem extends Item {

  private boolean partnerDescriptionHeavyStyle = false;
  private boolean partnerDescriptionLightStyle = false;

  public DescriptionItem() {
    super();
  }

  public DescriptionItem(Context context, AttributeSet attrs) {
    super(context, attrs);
  }

  /**
   * Returns true if the description of partner's layout should apply heavy style, this depends on
   * if the layout fulfill conditions in {@code applyPartnerHeavyThemeResource} of {@link
   * com.google.android.setupdesign.GlifLayout}
   */
  public boolean shouldApplyPartnerDescriptionHeavyStyle() {
    return partnerDescriptionHeavyStyle;
  }

  /**
   * Returns true if the description of partner's layout should apply light style, this depends on
   * if the layout fulfill conditions in {@code shouldApplyPartnerResource} of {@link
   * com.google.android.setupcompat.PartnerCustomizationLayout}
   */
  public boolean shouldApplyPartnerDescriptionLightStyle() {
    return partnerDescriptionLightStyle;
  }

  /**
   * Applies partner description style on the title of the item, i.e. the TextView with {@code
   * R.id.sud_items_title}.
   *
   * @param layout A layout indicates if the description of partner's layout should apply heavy or
   *     light style
   */
  public void setPartnerDescriptionStyle(FrameLayout layout) {
    if (layout instanceof GlifLayout) {
      this.partnerDescriptionHeavyStyle =
          ((GlifLayout) layout).shouldApplyPartnerHeavyThemeResource();
    }
    if (layout instanceof PartnerCustomizationLayout) {
      this.partnerDescriptionLightStyle =
          ((PartnerCustomizationLayout) layout).shouldApplyPartnerResource();
    }
    notifyItemChanged();
  }

  @Override
  public void onBindView(View view) {
    super.onBindView(view);
    TextView label = (TextView) view.findViewById(R.id.sud_items_title);
    if (shouldApplyPartnerDescriptionHeavyStyle()) {
      DescriptionStyler.applyPartnerCustomizationHeavyStyle(label);
    } else if (shouldApplyPartnerDescriptionLightStyle()) {
      DescriptionStyler.applyPartnerCustomizationLightStyle(label);
    }
  }
}
