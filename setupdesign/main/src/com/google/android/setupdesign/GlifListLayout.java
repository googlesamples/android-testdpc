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

import android.annotation.TargetApi;
import android.content.Context;
import android.graphics.drawable.Drawable;
import android.os.Build.VERSION_CODES;
import android.util.AttributeSet;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.AbsListView;
import android.widget.AbsListView.OnScrollListener;
import android.widget.ListAdapter;
import android.widget.ListView;
import com.google.android.setupcompat.partnerconfig.PartnerConfigHelper;
import com.google.android.setupcompat.util.ForceTwoPaneHelper;
import com.google.android.setupdesign.template.ListMixin;
import com.google.android.setupdesign.template.ListViewScrollHandlingDelegate;
import com.google.android.setupdesign.template.RequireScrollMixin;

/**
 * A GLIF themed layout with a ListView. {@code android:entries} can also be used to specify an
 * {@link com.google.android.setupdesign.items.ItemHierarchy} to be used with this layout in XML.
 */
public class GlifListLayout extends GlifLayout {

  private ListMixin listMixin;

  public GlifListLayout(Context context) {
    this(context, 0, 0);
  }

  public GlifListLayout(Context context, int template) {
    this(context, template, 0);
  }

  public GlifListLayout(Context context, int template, int containerId) {
    super(context, template, containerId);
    init(null, 0);
  }

  public GlifListLayout(Context context, AttributeSet attrs) {
    super(context, attrs);
    init(attrs, 0);
  }

  @TargetApi(VERSION_CODES.HONEYCOMB)
  public GlifListLayout(Context context, AttributeSet attrs, int defStyleAttr) {
    super(context, attrs, defStyleAttr);
    init(attrs, defStyleAttr);
  }

  private void init(AttributeSet attrs, int defStyleAttr) {
    if (isInEditMode()) {
      return;
    }

    listMixin = new ListMixin(this, attrs, defStyleAttr);
    registerMixin(ListMixin.class, listMixin);

    final RequireScrollMixin requireScrollMixin = getMixin(RequireScrollMixin.class);
    requireScrollMixin.setScrollHandlingDelegate(
        new ListViewScrollHandlingDelegate(requireScrollMixin, getListView()));

    View view = this.findManagedViewById(R.id.sud_landscape_content_area);
    if (view != null) {
      tryApplyPartnerCustomizationContentPaddingTopStyle(view);
    }
    updateLandscapeMiddleHorizontalSpacing();

    if (PartnerConfigHelper.isGlifExpressiveEnabled(getContext())) {
      initScrollingListener();
    }

    initBackButton();
  }

  @Override
  protected void onLayout(boolean changed, int left, int top, int right, int bottom) {
    super.onLayout(changed, left, top, right, bottom);
    listMixin.onLayout();
  }

  @Override
  protected View onInflateTemplate(LayoutInflater inflater, int template) {
    if (template == 0) {
      template = R.layout.sud_glif_list_template;

      // if the activity is embedded should apply an embedded layout.
      if (isEmbeddedActivityOnePaneEnabled(getContext())) {
        if (isGlifExpressiveEnabled()) {
          template = R.layout.sud_glif_expressive_list_embedded_template;
        } else {
          template = R.layout.sud_glif_list_embedded_template;
        }
        // TODO add unit test for this case.
      } else if (isGlifExpressiveEnabled()) {
        template = R.layout.sud_glif_expressive_list_template;
      } else if (ForceTwoPaneHelper.isForceTwoPaneEnable(getContext())) {
        template = R.layout.sud_glif_list_template_two_pane;
      }
    }
    return super.onInflateTemplate(inflater, template);
  }

  @Override
  protected ViewGroup findContainer(int containerId) {
    if (containerId == 0) {
      containerId = android.R.id.list;
    }
    return super.findContainer(containerId);
  }

  @Override
  protected void initScrollingListener() {
    ListView listView = null;
    if (listMixin != null) {
      listView = listMixin.getListView();
    }

    if (listView != null) {
      listView.setOnScrollListener(
          new OnScrollListener() {
            @Override
            public void onScrollStateChanged(AbsListView absListView, int i) {}

            @Override
            public void onScroll(
                AbsListView view, int firstVisibleItem, int visibleItemCount, int totalItemCount) {
              onScrolling(
                  firstVisibleItem + visibleItemCount >= totalItemCount && totalItemCount > 0);
            }
          });
    }
  }

  public ListView getListView() {
    return listMixin.getListView();
  }

  public void setAdapter(ListAdapter adapter) {
    listMixin.setAdapter(adapter);
  }

  public ListAdapter getAdapter() {
    return listMixin.getAdapter();
  }

  /** @deprecated Use {@link #setDividerInsets(int, int)} instead. */
  @Deprecated
  public void setDividerInset(int inset) {
    listMixin.setDividerInset(inset);
  }

  /**
   * Sets the start inset of the divider. This will use the default divider drawable set in the
   * theme and apply insets to it.
   *
   * @param start The number of pixels to inset on the "start" side of the list divider. Typically
   *     this will be either {@code @dimen/sud_items_glif_icon_divider_inset} or
   *     {@code @dimen/sud_items_glif_text_divider_inset}.
   * @param end The number of pixels to inset on the "end" side of the list divider.
   * @see ListMixin#setDividerInsets(int, int)
   */
  public void setDividerInsets(int start, int end) {
    listMixin.setDividerInsets(start, end);
  }

  /** @deprecated Use {@link #getDividerInsetStart()} instead. */
  @Deprecated
  public int getDividerInset() {
    return listMixin.getDividerInset();
  }

  /** @see ListMixin#getDividerInsetStart() */
  public int getDividerInsetStart() {
    return listMixin.getDividerInsetStart();
  }

  /** @see ListMixin#getDividerInsetEnd() */
  public int getDividerInsetEnd() {
    return listMixin.getDividerInsetEnd();
  }

  /** @see ListMixin#getDivider() */
  public Drawable getDivider() {
    return listMixin.getDivider();
  }
}
