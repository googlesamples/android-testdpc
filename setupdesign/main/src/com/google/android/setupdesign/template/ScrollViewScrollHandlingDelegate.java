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
import android.widget.ScrollView;
import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import com.google.android.setupdesign.template.RequireScrollMixin.ScrollHandlingDelegate;
import com.google.android.setupdesign.view.BottomScrollView;
import com.google.android.setupdesign.view.BottomScrollView.BottomScrollListener;

/**
 * {@link ScrollHandlingDelegate} which analyzes scroll events from {@link BottomScrollView} and
 * notifies {@link RequireScrollMixin} about scrollability changes.
 */
public class ScrollViewScrollHandlingDelegate
    implements ScrollHandlingDelegate, BottomScrollListener {

  private static final String TAG = "ScrollViewDelegate";

  @NonNull private final RequireScrollMixin requireScrollMixin;

  @Nullable private final BottomScrollView scrollView;

  public ScrollViewScrollHandlingDelegate(
      @NonNull RequireScrollMixin requireScrollMixin, @Nullable ScrollView scrollView) {
    this.requireScrollMixin = requireScrollMixin;
    if (scrollView instanceof BottomScrollView) {
      this.scrollView = (BottomScrollView) scrollView;
    } else {
      Log.w(TAG, "Cannot set non-BottomScrollView. Found=" + scrollView);
      this.scrollView = null;
    }
  }

  @Override
  public void onScrolledToBottom() {
    requireScrollMixin.notifyScrollabilityChange(false);
  }

  @Override
  public void onRequiresScroll() {
    requireScrollMixin.notifyScrollabilityChange(true);
  }

  @Override
  public void startListening() {
    if (scrollView != null) {
      scrollView.setBottomScrollListener(this);
    } else {
      Log.w(TAG, "Cannot require scroll. Scroll view is null.");
    }
  }

  @Override
  public void pageScrollDown() {
    if (scrollView != null) {
      scrollView.pageScroll(ScrollView.FOCUS_DOWN);
    }
  }
}
