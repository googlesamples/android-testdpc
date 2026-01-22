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

package com.google.android.setupdesign;

import android.content.Context;
import android.content.res.TypedArray;
import android.graphics.Canvas;
import android.graphics.Rect;
import android.graphics.drawable.Drawable;
import androidx.recyclerview.widget.RecyclerView;
import android.view.View;
import androidx.annotation.IntDef;
import androidx.core.view.ViewCompat;
import java.lang.annotation.Retention;
import java.lang.annotation.RetentionPolicy;

/**
 * An {@link androidx.recyclerview.widget.RecyclerView.ItemDecoration} for RecyclerView to draw
 * dividers between items. This ItemDecoration will draw the drawable specified by {@link
 * #setDivider(android.graphics.drawable.Drawable)} as the divider in between each item by default,
 * and the behavior of whether the divider is shown can be customized by subclassing {@link
 * com.google.android.setupdesign.DividerItemDecoration.DividedViewHolder}.
 *
 * <p>Modified from v14 PreferenceFragment.DividerDecoration.
 */
public class DividerItemDecoration extends RecyclerView.ItemDecoration {

  /* static section */

  /**
   * An interface to be implemented by a {@link RecyclerView.ViewHolder} which controls whether
   * dividers should be shown above and below that item.
   */
  public interface DividedViewHolder {

    /**
     * Returns whether divider is allowed above this item. A divider will be shown only if both
     * items immediately above and below it allows this divider.
     */
    boolean isDividerAllowedAbove();

    /**
     * Returns whether divider is allowed below this item. A divider will be shown only if both
     * items immediately above and below it allows this divider.
     */
    boolean isDividerAllowedBelow();
  }

  @Retention(RetentionPolicy.SOURCE)
  @IntDef({DIVIDER_CONDITION_EITHER, DIVIDER_CONDITION_BOTH})
  public @interface DividerCondition {}

  public static final int DIVIDER_CONDITION_EITHER = 0;
  public static final int DIVIDER_CONDITION_BOTH = 1;

  /** @deprecated Use {@link #DividerItemDecoration(android.content.Context)} */
  @Deprecated
  public static DividerItemDecoration getDefault(Context context) {
    return new DividerItemDecoration(context);
  }

  /* non-static section */

  private Drawable divider;
  private int dividerHeight;
  private int dividerIntrinsicHeight;
  @DividerCondition private int dividerCondition;

  public DividerItemDecoration() {}

  public DividerItemDecoration(Context context) {
    final TypedArray a = context.obtainStyledAttributes(R.styleable.SudDividerItemDecoration);
    final Drawable divider =
        a.getDrawable(R.styleable.SudDividerItemDecoration_android_listDivider);
    final int dividerHeight =
        a.getDimensionPixelSize(R.styleable.SudDividerItemDecoration_android_dividerHeight, 0);
    @DividerCondition
    final int dividerCondition =
        a.getInt(
            R.styleable.SudDividerItemDecoration_sudDividerCondition, DIVIDER_CONDITION_EITHER);
    a.recycle();

    setDivider(divider);
    setDividerHeight(dividerHeight);
    setDividerCondition(dividerCondition);
  }

  @Override
  public void onDraw(Canvas c, RecyclerView parent, RecyclerView.State state) {
    if (divider == null) {
      return;
    }
    final int childCount = parent.getChildCount();
    final int width = parent.getWidth();
    final int dividerHeight = this.dividerHeight != 0 ? this.dividerHeight : dividerIntrinsicHeight;
    for (int childViewIndex = 0; childViewIndex < childCount; childViewIndex++) {
      final View view = parent.getChildAt(childViewIndex);
      if (shouldDrawDividerBelow(view, parent)) {
        final int top = (int) ViewCompat.getY(view) + view.getHeight();
        divider.setBounds(0, top, width, top + dividerHeight);
        divider.draw(c);
      }
    }
  }

  @Override
  public void getItemOffsets(
      Rect outRect, View view, RecyclerView parent, RecyclerView.State state) {
    if (shouldDrawDividerBelow(view, parent)) {
      outRect.bottom = dividerHeight != 0 ? dividerHeight : dividerIntrinsicHeight;
    }
  }

  protected boolean shouldDrawDividerBelow(View view, RecyclerView parent) {
    final RecyclerView.ViewHolder holder = parent.getChildViewHolder(view);
    final int index = holder.getLayoutPosition();
    final int lastItemIndex = parent.getAdapter().getItemCount() - 1;
    if (isDividerAllowedBelow(holder)) {
      if (dividerCondition == DIVIDER_CONDITION_EITHER) {
        // Draw the divider without consulting the next item if we only
        // need permission for either above or below.
        return true;
      }
    } else if (dividerCondition == DIVIDER_CONDITION_BOTH || index == lastItemIndex) {
      // Don't draw if the current view holder doesn't allow drawing below
      // and the current theme requires permission for both the item below and above.
      // Also, if this is the last item, there is no item below to ask permission
      // for whether to draw a divider above, so don't draw it.
      return false;
    }
    // Require permission from index below to draw the divider.
    if (index < lastItemIndex) {
      final RecyclerView.ViewHolder nextHolder = parent.findViewHolderForLayoutPosition(index + 1);
      if (!isDividerAllowedAbove(nextHolder)) {
        // Don't draw if the next view holder doesn't allow drawing above
        return false;
      }
    }
    return true;
  }

  /**
   * Whether a divider is allowed above the view holder. The allowed values will be combined
   * according to {@link #getDividerCondition()}. The default implementation delegates to {@link
   * com.google.android.setupdesign.DividerItemDecoration.DividedViewHolder}, or simply allows the
   * divider if the view holder doesn't implement {@code DividedViewHolder}. Subclasses can override
   * this to give more information to decide whether a divider should be drawn.
   *
   * @return True if divider is allowed above this view holder.
   */
  protected boolean isDividerAllowedAbove(RecyclerView.ViewHolder viewHolder) {
    return !(viewHolder instanceof DividedViewHolder)
        || ((DividedViewHolder) viewHolder).isDividerAllowedAbove();
  }

  /**
   * Whether a divider is allowed below the view holder. The allowed values will be combined
   * according to {@link #getDividerCondition()}. The default implementation delegates to {@link
   * com.google.android.setupdesign.DividerItemDecoration.DividedViewHolder}, or simply allows the
   * divider if the view holder doesn't implement {@code DividedViewHolder}. Subclasses can override
   * this to give more information to decide whether a divider should be drawn.
   *
   * @return True if divider is allowed below this view holder.
   */
  protected boolean isDividerAllowedBelow(RecyclerView.ViewHolder viewHolder) {
    return !(viewHolder instanceof DividedViewHolder)
        || ((DividedViewHolder) viewHolder).isDividerAllowedBelow();
  }

  /** Sets the drawable to be used as the divider. */
  public void setDivider(Drawable divider) {
    if (divider != null) {
      dividerIntrinsicHeight = divider.getIntrinsicHeight();
    } else {
      dividerIntrinsicHeight = 0;
    }
    this.divider = divider;
  }

  /** Gets the drawable currently used as the divider. */
  public Drawable getDivider() {
    return divider;
  }

  /** Sets the divider height, in pixels. */
  public void setDividerHeight(int dividerHeight) {
    this.dividerHeight = dividerHeight;
  }

  /** Gets the divider height, in pixels. */
  public int getDividerHeight() {
    return dividerHeight;
  }

  /**
   * Sets whether the divider needs permission from both the item view holder below and above from
   * where the divider would draw itself or just needs permission from one or the other before
   * drawing itself.
   */
  public void setDividerCondition(@DividerCondition int dividerCondition) {
    this.dividerCondition = dividerCondition;
  }

  /**
   * Gets whether the divider needs permission from both the item view holder below and above from
   * where the divider would draw itself or just needs permission from one or the other before
   * drawing itself.
   */
  @DividerCondition
  public int getDividerCondition() {
    return dividerCondition;
  }
}
