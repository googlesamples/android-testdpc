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

package com.google.android.setupdesign.template;

import android.content.Context;
import android.content.res.TypedArray;
import android.graphics.drawable.Drawable;
import android.os.Build;
import android.os.Build.VERSION_CODES;
import android.util.AttributeSet;
import android.view.View;
import android.widget.HeaderViewListAdapter;
import android.widget.ListAdapter;
import android.widget.ListView;
import androidx.annotation.AttrRes;
import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import com.google.android.setupcompat.internal.TemplateLayout;
import com.google.android.setupcompat.partnerconfig.PartnerConfig;
import com.google.android.setupcompat.partnerconfig.PartnerConfigHelper;
import com.google.android.setupcompat.template.Mixin;
import com.google.android.setupdesign.R;
import com.google.android.setupdesign.items.ItemAdapter;
import com.google.android.setupdesign.items.ItemGroup;
import com.google.android.setupdesign.items.ItemInflater;
import com.google.android.setupdesign.util.DrawableLayoutDirectionHelper;
import com.google.android.setupdesign.util.PartnerStyleHelper;

/** A {@link Mixin} for interacting with ListViews. */
public class ListMixin implements Mixin {

  private final TemplateLayout templateLayout;

  @Nullable private ListView listView;

  private Drawable divider;
  private Drawable defaultDivider;

  private int dividerInsetStart;
  private int dividerInsetEnd;
  /** @param layout The layout this mixin belongs to. */
  public ListMixin(
      @NonNull TemplateLayout layout, @Nullable AttributeSet attrs, @AttrRes int defStyleAttr) {
    templateLayout = layout;

    final Context context = layout.getContext();
    final TypedArray a =
        context.obtainStyledAttributes(attrs, R.styleable.SudListMixin, defStyleAttr, 0);

    final int entries = a.getResourceId(R.styleable.SudListMixin_android_entries, 0);
    if (entries != 0) {
      final ItemGroup inflated = (ItemGroup) new ItemInflater(context).inflate(entries);
      setAdapter(new ItemAdapter(inflated));
    }

    boolean isDividerDisplay = a.getBoolean(R.styleable.SudListMixin_sudDividerShown, true);
    if (isDividerShown(context, isDividerDisplay)) {
      int dividerInset = a.getDimensionPixelSize(R.styleable.SudListMixin_sudDividerInset, -1);
      if (dividerInset != -1) {
        setDividerInset(dividerInset);
      } else {
        int dividerInsetStart =
            a.getDimensionPixelSize(R.styleable.SudListMixin_sudDividerInsetStart, 0);
        int dividerInsetEnd =
            a.getDimensionPixelSize(R.styleable.SudListMixin_sudDividerInsetEnd, 0);

        if (PartnerStyleHelper.shouldApplyPartnerResource(templateLayout)) {
          if (PartnerConfigHelper.get(context)
              .isPartnerConfigAvailable(PartnerConfig.CONFIG_LAYOUT_MARGIN_START)) {
            dividerInsetStart =
                (int)
                    PartnerConfigHelper.get(context)
                        .getDimension(context, PartnerConfig.CONFIG_LAYOUT_MARGIN_START);
          }
          if (PartnerConfigHelper.get(context)
              .isPartnerConfigAvailable(PartnerConfig.CONFIG_LAYOUT_MARGIN_END)) {
            dividerInsetEnd =
                (int)
                    PartnerConfigHelper.get(context)
                        .getDimension(context, PartnerConfig.CONFIG_LAYOUT_MARGIN_END);
          }
        }
        setDividerInsets(dividerInsetStart, dividerInsetEnd);
      }
    }
    else{
      getListView().setDivider(null);
    }
    a.recycle();
  }

  private boolean isDividerShown(Context context, boolean isDividerDisplay) {
    if (PartnerStyleHelper.shouldApplyPartnerResource(templateLayout)) {
      if (PartnerConfigHelper.get(context)
          .isPartnerConfigAvailable(PartnerConfig.CONFIG_ITEMS_DIVIDER_SHOWN)) {
        isDividerDisplay =
            PartnerConfigHelper.get(context)
                .getBoolean(context, PartnerConfig.CONFIG_ITEMS_DIVIDER_SHOWN, true);
      }
    }
    return isDividerDisplay;
  }

  /**
   * @return The list view contained in the layout, as marked by {@code @android:id/list}. This will
   *     return {@code null} if the list doesn't exist in the layout.
   */
  public ListView getListView() {
    return getListViewInternal();
  }

  // Client code can assume getListView() will not be null if they know their template contains
  // the list, but this mixin cannot. Any usages of getListView in this mixin needs null checks.
  @Nullable
  private ListView getListViewInternal() {
    if (listView == null) {
      final View list = templateLayout.findManagedViewById(android.R.id.list);
      if (list instanceof ListView) {
        listView = (ListView) list;
      }
    }
    return listView;
  }

  /**
   * List mixin needs to update the dividers if the layout direction has changed. This method should
   * be called when {@link View#onLayout(boolean, int, int, int, int)} of the template is called.
   */
  public void onLayout() {
    if (divider == null) {
      // Update divider in case layout direction has just been resolved
      updateDivider();
    }
  }

  /**
   * Gets the adapter of the list view in this layout. If the adapter is a HeaderViewListAdapter,
   * this method will unwrap it and return the underlying adapter.
   *
   * @return The adapter, or {@code null} if there is no list, or if the list has no adapter.
   */
  public ListAdapter getAdapter() {
    final ListView listView = getListViewInternal();
    if (listView != null) {
      final ListAdapter adapter = listView.getAdapter();
      if (adapter instanceof HeaderViewListAdapter) {
        return ((HeaderViewListAdapter) adapter).getWrappedAdapter();
      }
      return adapter;
    }
    return null;
  }

  /** Sets the adapter on the list view in this layout. */
  public void setAdapter(ListAdapter adapter) {
    final ListView listView = getListViewInternal();
    if (listView != null) {
      listView.setAdapter(adapter);
    }
  }

  /** @deprecated Use {@link #setDividerInsets(int, int)} instead. */
  @Deprecated
  public void setDividerInset(int inset) {
    setDividerInsets(inset, 0);
  }

  /**
   * Sets the start inset of the divider. This will use the default divider drawable set in the
   * theme and apply insets to it.
   *
   * @param start The number of pixels to inset on the "start" side of the list divider. Typically
   *     this will be either {@code @dimen/sud_items_glif_icon_divider_inset} or
   *     {@code @dimen/sud_items_glif_text_divider_inset}.
   * @param end The number of pixels to inset on the "end" side of the list divider.
   */
  public void setDividerInsets(int start, int end) {
    dividerInsetStart = start;
    dividerInsetEnd = end;
    updateDivider();
  }

  /**
   * @return The number of pixels inset on the start side of the divider.
   * @deprecated This is the same as {@link #getDividerInsetStart()}. Use that instead.
   */
  @Deprecated
  public int getDividerInset() {
    return getDividerInsetStart();
  }

  /** @return The number of pixels inset on the start side of the divider. */
  public int getDividerInsetStart() {
    return dividerInsetStart;
  }

  /** @return The number of pixels inset on the end side of the divider. */
  public int getDividerInsetEnd() {
    return dividerInsetEnd;
  }

  private void updateDivider() {
    final ListView listView = getListViewInternal();
    if (listView == null) {
      return;
    }
    boolean shouldUpdate = true;
    if (Build.VERSION.SDK_INT >= VERSION_CODES.KITKAT) {
      shouldUpdate = templateLayout.isLayoutDirectionResolved();
    }
    if (shouldUpdate) {
      if (defaultDivider == null) {
        defaultDivider = listView.getDivider();
      }
      if (defaultDivider != null) {
        divider =
            DrawableLayoutDirectionHelper.createRelativeInsetDrawable(
                defaultDivider,
                dividerInsetStart /* start */,
                0 /* top */,
                dividerInsetEnd /* end */,
                0 /* bottom */,
                templateLayout);
        listView.setDivider(divider);
      }
    }
  }

  /** @return The drawable used as the divider. */
  public Drawable getDivider() {
    return divider;
  }
}
