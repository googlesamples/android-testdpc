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

import androidx.recyclerview.widget.RecyclerView;
import android.util.Log;
import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import com.google.android.setupdesign.template.RequireScrollMixin.ScrollHandlingDelegate;

/**
 * {@link ScrollHandlingDelegate} which analyzes scroll events from {@link RecyclerView} and
 * notifies {@link RequireScrollMixin} about scrollability changes.
 */
public class RecyclerViewScrollHandlingDelegate implements ScrollHandlingDelegate {

  private static final String TAG = "RVRequireScrollMixin";

  @Nullable private final RecyclerView recyclerView;

  @NonNull private final RequireScrollMixin requireScrollMixin;

  public RecyclerViewScrollHandlingDelegate(
      @NonNull RequireScrollMixin requireScrollMixin, @Nullable RecyclerView recyclerView) {
    this.requireScrollMixin = requireScrollMixin;
    this.recyclerView = recyclerView;
  }

  private boolean canScrollDown() {
    if (recyclerView != null) {
      // Compatibility implementation of View#canScrollVertically
      final int offset = recyclerView.computeVerticalScrollOffset();
      final int range =
          recyclerView.computeVerticalScrollRange() - recyclerView.computeVerticalScrollExtent();
      return range != 0 && offset < range - 1;
    }
    return false;
  }

  @Override
  public void startListening() {
    if (this.recyclerView != null) {
      this.recyclerView.addOnScrollListener(
          new RecyclerView.OnScrollListener() {
            @Override
            public void onScrolled(RecyclerView recyclerView, int dx, int dy) {
              requireScrollMixin.notifyScrollabilityChange(canScrollDown());
            }
          });

      if (canScrollDown()) {
        requireScrollMixin.notifyScrollabilityChange(true);
      }
    } else {
      Log.w(TAG, "Cannot require scroll. Recycler view is null.");
    }
  }

  @Override
  public void pageScrollDown() {
    if (recyclerView != null) {
      final int height = recyclerView.getHeight();
      recyclerView.smoothScrollBy(0, height);
    }
  }
}
