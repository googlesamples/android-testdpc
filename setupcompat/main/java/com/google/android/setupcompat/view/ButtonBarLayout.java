/*
 * Copyright (C) 2017 The Android Open Source Project
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

package com.google.android.setupcompat.view;

import android.content.Context;
import android.util.AttributeSet;
import android.view.Gravity;
import android.view.View;
import android.widget.LinearLayout;
import com.google.android.setupcompat.R;
import com.google.android.setupcompat.partnerconfig.PartnerConfigHelper;
import com.google.android.setupcompat.template.FooterActionButton;
import com.google.android.setupcompat.util.Logger;
import java.util.ArrayList;
import java.util.Collections;

/**
 * An extension of LinearLayout that automatically switches to vertical orientation when it can't
 * fit its child views horizontally.
 *
 * <p>Modified from {@code com.android.internal.widget.ButtonBarLayout}
 */
public class ButtonBarLayout extends LinearLayout {

  private static final Logger LOG = new Logger(ButtonBarLayout.class);

  private boolean stacked = false;
  private int originalPaddingLeft;
  private int originalPaddingRight;

  private boolean stackedButtonForExpressiveStyle;

  public ButtonBarLayout(Context context) {
    super(context);
  }

  public ButtonBarLayout(Context context, AttributeSet attrs) {
    super(context, attrs);
  }

  @Override
  protected void onMeasure(int widthMeasureSpec, int heightMeasureSpec) {
    final int widthSize = MeasureSpec.getSize(widthMeasureSpec);

    setStacked(false);

    boolean needsRemeasure = false;

    int initialWidthMeasureSpec = widthMeasureSpec;
    if (MeasureSpec.getMode(widthMeasureSpec) == MeasureSpec.EXACTLY) {
      // Measure with WRAP_CONTENT, so that we can compare the measured size with the
      // available size to see if we need to stack.
      initialWidthMeasureSpec = MeasureSpec.makeMeasureSpec(0, MeasureSpec.UNSPECIFIED);

      // We'll need to remeasure again to fill excess space.
      needsRemeasure = true;
    }

    super.onMeasure(initialWidthMeasureSpec, heightMeasureSpec);

    final boolean childrenLargerThanContainer =
        ((widthSize > 0) && (getMeasuredWidth() > widthSize)) || stackedButtonForExpressiveStyle;
    if (!isFooterButtonsEvenlyWeighted(getContext()) && childrenLargerThanContainer) {
      setStacked(true);

      // Measure again in the new orientation.
      needsRemeasure = true;
    }

    if (needsRemeasure) {
      super.onMeasure(widthMeasureSpec, heightMeasureSpec);
    }
  }

  private void setStacked(boolean stacked) {
    if (this.stacked == stacked) {
      return;
    }
    this.stacked = stacked;
    boolean isUnstack = false;
    int primaryStyleButtonCount = 0;
    int childCount = getChildCount();
    for (int i = 0; i < childCount; i++) {
      View child = getChildAt(i);
      LayoutParams childParams = (LayoutParams) child.getLayoutParams();
      if (stacked) {
        child.setTag(R.id.suc_customization_original_weight, childParams.weight);
        childParams.weight = 0;
        childParams.leftMargin = 0;
      } else {
        Float weight = (Float) child.getTag(R.id.suc_customization_original_weight);
        if (weight != null) {
          childParams.weight = weight;
        } else {
          // If the tag in the child is gone, it will be unstack and the child in the container will
          // be disorder.
          isUnstack = true;
        }
        if (isPrimaryButtonStyle(child)) {
          primaryStyleButtonCount++;
        }
      }
      child.setLayoutParams(childParams);
    }

    setOrientation(stacked ? LinearLayout.VERTICAL : LinearLayout.HORIZONTAL);
    if (isUnstack) {
      LOG.w("Reorder the FooterActionButtons in the container");
      ArrayList<View> childViewsInContainerInOrder =
          getChildViewsInContainerInOrder(
              childCount, /* isOnePrimaryButton= */ (primaryStyleButtonCount <= 1));
      for (int i = 0; i < childCount; i++) {
        View view = childViewsInContainerInOrder.get(i);
        if (view != null) {
          bringChildToFront(view);
        }
      }
    } else {
      for (int i = childCount - 1; i >= 0; i--) {
        bringChildToFront(getChildAt(i));
      }
    }

    if (stacked) {
      if (getContext().getResources().getBoolean(R.bool.sucTwoPaneLayoutStyle)
          && PartnerConfigHelper.isGlifExpressiveEnabled(getContext())) {
        // When device in the two pane mode and glif expressive flag enabled, the button should
        // aligned to the end.
        setHorizontalGravity(Gravity.END);
      } else {
        // When stacked, the buttons need to be kept in the center of the button bar.
        setHorizontalGravity(Gravity.CENTER);
      }
      // HACK: In the default button bar style, the left and right paddings are not
      // balanced to compensate for different alignment for borderless (left) button and
      // the raised (right) button. When it's stacked, we want the buttons to be centered,
      // so we balance out the paddings here.
      originalPaddingLeft = getPaddingLeft();
      originalPaddingRight = getPaddingRight();
      int paddingHorizontal = Math.max(originalPaddingLeft, originalPaddingRight);
      setPadding(paddingHorizontal, getPaddingTop(), paddingHorizontal, getPaddingBottom());
    } else {
      setPadding(originalPaddingLeft, getPaddingTop(), originalPaddingRight, getPaddingBottom());
    }
  }

  private boolean isPrimaryButtonStyle(View child) {
    return child instanceof FooterActionButton
        && ((FooterActionButton) child).isPrimaryButtonStyle();
  }

  /**
   * Return a array which store child views in the container and in the order (secondary button,
   * space view, primary button), if only one primary button, the child views will replace null
   * value in specific proper position, if there are two primary buttons, expected get the original
   * child by the order (space view, secondary button, primary button), so insert the space view to
   * the middle in the array.
   */
  private ArrayList<View> getChildViewsInContainerInOrder(
      int childCount, boolean isOnePrimaryButton) {
    int childViewsInContainerCount = 3;
    int secondaryButtonIndex = 0;
    int spaceViewIndex = 1;
    int primaryButtonIndex = 2;

    ArrayList<View> childFooterButtons = new ArrayList<>();

    if (isOnePrimaryButton) {
      childFooterButtons.addAll(Collections.nCopies(childViewsInContainerCount, null));
    }

    for (int i = 0; i < childCount; i++) {
      View childAt = getChildAt(i);
      if (isOnePrimaryButton) {
        if (isPrimaryButtonStyle(childAt)) {
          childFooterButtons.set(primaryButtonIndex, childAt);
        } else if (!(childAt instanceof FooterActionButton)) {
          childFooterButtons.set(spaceViewIndex, childAt);
        } else {
          childFooterButtons.set(secondaryButtonIndex, childAt);
        }
      } else {
        if (!(childAt instanceof FooterActionButton)) {
          childFooterButtons.add(spaceViewIndex, childAt);
        } else {
          childFooterButtons.add(getChildAt(i));
        }
      }
    }
    return childFooterButtons;
  }

  private boolean isFooterButtonsEvenlyWeighted(Context context) {
    int childCount = getChildCount();
    int primaryButtonCount = 0;
    for (int i = 0; i < childCount; i++) {
      View child = getChildAt(i);
      if (child instanceof FooterActionButton) {
        if (((FooterActionButton) child).isPrimaryButtonStyle()) {
          primaryButtonCount += 1;
        }
      }
    }
    if (primaryButtonCount != 2) {
      return false;
    }

    // TODO: Support neutral button style in glif layout for phone and tablet
    if (context.getResources().getConfiguration().smallestScreenWidthDp >= 600
        && PartnerConfigHelper.shouldApplyExtendedPartnerConfig(context)) {
      return true;
    } else {
      return false;
    }
  }

  public void setStackedButtonForExpressiveStyle(boolean isStacked) {
    if (PartnerConfigHelper.isGlifExpressiveEnabled(getContext())) {
      stackedButtonForExpressiveStyle = isStacked;
    } else {
      stackedButtonForExpressiveStyle = false;
    }
  }
}
