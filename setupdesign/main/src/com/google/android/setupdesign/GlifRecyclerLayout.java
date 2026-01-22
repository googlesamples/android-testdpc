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
import androidx.recyclerview.widget.RecyclerView;
import androidx.recyclerview.widget.RecyclerView.Adapter;
import androidx.recyclerview.widget.RecyclerView.OnScrollListener;
import androidx.recyclerview.widget.RecyclerView.ViewHolder;
import android.util.AttributeSet;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import androidx.annotation.NonNull;
import com.google.android.setupcompat.partnerconfig.PartnerConfigHelper;
import com.google.android.setupcompat.util.ForceTwoPaneHelper;
import com.google.android.setupdesign.template.RecyclerMixin;
import com.google.android.setupdesign.template.RecyclerViewScrollHandlingDelegate;
import com.google.android.setupdesign.template.RequireScrollMixin;

/**
 * A GLIF themed layout with a RecyclerView. {@code android:entries} can also be used to specify an
 * {@link com.google.android.setupdesign.items.ItemHierarchy} to be used with this layout in XML.
 */
public class GlifRecyclerLayout extends GlifLayout {

  protected RecyclerMixin recyclerMixin;

  public GlifRecyclerLayout(Context context) {
    this(context, 0, 0);
  }

  public GlifRecyclerLayout(Context context, int template) {
    this(context, template, 0);
  }

  public GlifRecyclerLayout(Context context, int template, int containerId) {
    super(context, template, containerId);
    init(null, 0);
  }

  public GlifRecyclerLayout(Context context, AttributeSet attrs) {
    super(context, attrs);
    init(attrs, 0);
  }

  @TargetApi(VERSION_CODES.HONEYCOMB)
  public GlifRecyclerLayout(Context context, AttributeSet attrs, int defStyleAttr) {
    super(context, attrs, defStyleAttr);
    init(attrs, defStyleAttr);
  }

  private void init(AttributeSet attrs, int defStyleAttr) {
    if (isInEditMode()) {
      return;
    }

    recyclerMixin.parseAttributes(attrs, defStyleAttr);
    registerMixin(RecyclerMixin.class, recyclerMixin);

    final RequireScrollMixin requireScrollMixin = getMixin(RequireScrollMixin.class);
    requireScrollMixin.setScrollHandlingDelegate(
        new RecyclerViewScrollHandlingDelegate(requireScrollMixin, getRecyclerView()));

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
    recyclerMixin.onLayout();
  }

  @Override
  protected View onInflateTemplate(LayoutInflater inflater, int template) {
    if (template == 0) {
      template = R.layout.sud_glif_recycler_template;

      // if the activity is embedded should apply an embedded layout.
      if (isEmbeddedActivityOnePaneEnabled(getContext())) {
        if (isGlifExpressiveEnabled()) {
          template = R.layout.sud_glif_expressive_recycler_embedded_template;
        } else {
        template = R.layout.sud_glif_recycler_embedded_template;
        }
        // TODO add unit test for this case.
      } else if (isGlifExpressiveEnabled()) {
        template = R.layout.sud_glif_expressive_recycler_template;
      } else if (ForceTwoPaneHelper.isForceTwoPaneEnable(getContext())) {
        template = R.layout.sud_glif_recycler_template_two_pane;
      }
    }
    return super.onInflateTemplate(inflater, template);
  }

  @Override
  protected void onTemplateInflated() {
    final View recyclerView = findViewById(R.id.sud_recycler_view);
    if (recyclerView instanceof RecyclerView) {
      recyclerMixin = new RecyclerMixin(this, (RecyclerView) recyclerView);
    } else {
      throw new IllegalStateException(
          "GlifRecyclerLayout should use a template with recycler view");
    }
  }

  @Override
  protected ViewGroup findContainer(int containerId) {
    if (containerId == 0) {
      containerId = R.id.sud_recycler_view;
    }
    return super.findContainer(containerId);
  }

  @Override
  // Returning generic type is the common pattern used for findViewBy* methods
  @SuppressWarnings("TypeParameterUnusedInFormals")
  public <T extends View> T findManagedViewById(int id) {
    final View header = recyclerMixin.getHeader();
    if (header != null) {
      final T view = header.findViewById(id);
      if (view != null) {
        return view;
      }
    }
    return super.findViewById(id);
  }

  @Override
  protected void initScrollingListener() {
    RecyclerView recyclerView = getRecyclerView();
    if (recyclerView != null) {
      recyclerView.addOnScrollListener(
          new OnScrollListener() {
            @Override
            public void onScrolled(@NonNull RecyclerView recyclerView, int dx, int dy) {
              super.onScrolled(recyclerView, dx, dy);
              // direction > 0 means view can scroll down, direction < 0 means view can scroll up.
              // Here we use direction > 0 to detect whether the view can be scrolling down or not.
              boolean isAtBottom = !recyclerView.canScrollVertically(/* direction= */ 1);
              onScrolling(isAtBottom);
            }
          });
    }
  }

  /** @see RecyclerMixin#setDividerItemDecoration(DividerItemDecoration) */
  public void setDividerItemDecoration(DividerItemDecoration decoration) {
    recyclerMixin.setDividerItemDecoration(decoration);
  }

  /** @see RecyclerMixin#getRecyclerView() */
  public RecyclerView getRecyclerView() {
    return recyclerMixin.getRecyclerView();
  }

  /** @see RecyclerMixin#setAdapter(Adapter) */
  public void setAdapter(Adapter<? extends ViewHolder> adapter) {
    recyclerMixin.setAdapter(adapter);
  }

  /** @see RecyclerMixin#getAdapter() */
  public Adapter<? extends ViewHolder> getAdapter() {
    return recyclerMixin.getAdapter();
  }

  /** @deprecated Use {@link #setDividerInsets(int, int)} instead. */
  @Deprecated
  public void setDividerInset(int inset) {
    recyclerMixin.setDividerInset(inset);
  }

  /** @see RecyclerMixin#setDividerInset(int) */
  public void setDividerInsets(int start, int end) {
    recyclerMixin.setDividerInsets(start, end);
  }

  /** @deprecated Use {@link #getDividerInsetStart()} instead. */
  @Deprecated
  public int getDividerInset() {
    return recyclerMixin.getDividerInset();
  }

  /** @see RecyclerMixin#getDividerInsetStart() */
  public int getDividerInsetStart() {
    return recyclerMixin.getDividerInsetStart();
  }

  /** @see RecyclerMixin#getDividerInsetEnd() */
  public int getDividerInsetEnd() {
    return recyclerMixin.getDividerInsetEnd();
  }

  /** @see RecyclerMixin#getDivider() */
  public Drawable getDivider() {
    return recyclerMixin.getDivider();
  }
}
