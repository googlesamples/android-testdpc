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
import android.graphics.drawable.Drawable;
import androidx.recyclerview.widget.RecyclerView;
import androidx.recyclerview.widget.RecyclerView.Adapter;
import androidx.recyclerview.widget.RecyclerView.ViewHolder;
import android.util.AttributeSet;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import com.google.android.setupdesign.template.RecyclerMixin;
import com.google.android.setupdesign.template.RecyclerViewScrollHandlingDelegate;
import com.google.android.setupdesign.template.RequireScrollMixin;

/**
 * A setup wizard layout for use with {@link androidx.recyclerview.widget.RecyclerView}. {@code
 * android:entries} can also be used to specify an {@link
 * com.google.android.setupdesign.items.ItemHierarchy} to be used with this layout in XML.
 *
 * @see SetupWizardListLayout
 */
public class SetupWizardRecyclerLayout extends SetupWizardLayout {

  protected RecyclerMixin recyclerMixin;

  public SetupWizardRecyclerLayout(Context context) {
    this(context, 0, 0);
  }

  public SetupWizardRecyclerLayout(Context context, int template, int containerId) {
    super(context, template, containerId);
    init(null, 0);
  }

  public SetupWizardRecyclerLayout(Context context, AttributeSet attrs) {
    super(context, attrs);
    init(attrs, 0);
  }

  public SetupWizardRecyclerLayout(Context context, AttributeSet attrs, int defStyleAttr) {
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
  }

  @Override
  protected void onLayout(boolean changed, int left, int top, int right, int bottom) {
    super.onLayout(changed, left, top, right, bottom);
    recyclerMixin.onLayout();
  }

  /** @see RecyclerMixin#getAdapter() */
  public Adapter<? extends ViewHolder> getAdapter() {
    return recyclerMixin.getAdapter();
  }

  /** @see RecyclerMixin#setAdapter(Adapter) */
  public void setAdapter(Adapter<? extends ViewHolder> adapter) {
    recyclerMixin.setAdapter(adapter);
  }

  /** @see RecyclerMixin#getRecyclerView() */
  public RecyclerView getRecyclerView() {
    return recyclerMixin.getRecyclerView();
  }

  @Override
  protected ViewGroup findContainer(int containerId) {
    if (containerId == 0) {
      containerId = R.id.sud_recycler_view;
    }
    return super.findContainer(containerId);
  }

  @Override
  protected View onInflateTemplate(LayoutInflater inflater, int template) {
    if (template == 0) {
      template = R.layout.sud_recycler_template;
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
          "SetupWizardRecyclerLayout should use a template with recycler view");
    }
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

  /** @deprecated Use {@link #setDividerInsets(int, int)} instead. */
  @Deprecated
  public void setDividerInset(int inset) {
    recyclerMixin.setDividerInset(inset);
  }

  /**
   * Sets the start inset of the divider. This will use the default divider drawable set in the
   * theme and apply insets to it.
   *
   * @param start The number of pixels to inset on the "start" side of the list divider. Typically
   *     this will be either {@code @dimen/sud_items_icon_divider_inset} or
   *     {@code @dimen/sud_items_text_divider_inset}.
   * @param end The number of pixels to inset on the "end" side of the list divider.
   * @see RecyclerMixin#setDividerInsets(int, int)
   */
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
