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

import android.util.Log;
import android.widget.AbsListView;
import android.widget.ListAdapter;
import android.widget.ListView;
import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import com.google.android.setupdesign.template.RequireScrollMixin.ScrollHandlingDelegate;

/**
 * {@link ScrollHandlingDelegate} which analyzes scroll events from {@link ListView} and notifies
 * {@link RequireScrollMixin} about scrollability changes.
 */
public class ListViewScrollHandlingDelegate
    implements ScrollHandlingDelegate, AbsListView.OnScrollListener {

  private static final String TAG = "ListViewDelegate";

  private static final int SCROLL_DURATION = 500;

  @NonNull private final RequireScrollMixin requireScrollMixin;

  @Nullable private final ListView listView;

  public ListViewScrollHandlingDelegate(
      @NonNull RequireScrollMixin requireScrollMixin, @Nullable ListView listView) {
    this.requireScrollMixin = requireScrollMixin;
    this.listView = listView;
  }

  @Override
  public void startListening() {
    if (listView != null) {
      listView.setOnScrollListener(this);

      final ListAdapter adapter = listView.getAdapter();
      if (listView.getLastVisiblePosition() < adapter.getCount()) {
        requireScrollMixin.notifyScrollabilityChange(true);
      }
    } else {
      Log.w(TAG, "Cannot require scroll. List view is null");
    }
  }

  @Override
  public void pageScrollDown() {
    if (listView != null) {
      final int height = listView.getHeight();
      listView.smoothScrollBy(height, SCROLL_DURATION);
    }
  }

  @Override
  public void onScrollStateChanged(AbsListView view, int scrollState) {}

  @Override
  public void onScroll(
      AbsListView view, int firstVisibleItem, int visibleItemCount, int totalItemCount) {
    if (firstVisibleItem + visibleItemCount >= totalItemCount) {
      requireScrollMixin.notifyScrollabilityChange(false);
    } else {
      requireScrollMixin.notifyScrollabilityChange(true);
    }
  }
}
