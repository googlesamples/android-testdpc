/*
 * Copyright (C) 2016 The Android Open Source Project
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

package com.google.android.setupdesign.view;

import static java.lang.Math.min;

import android.annotation.TargetApi;
import android.content.Context;
import android.content.res.TypedArray;
import android.graphics.Rect;
import android.os.Build;
import android.os.Build.VERSION_CODES;
import android.util.AttributeSet;
import android.util.DisplayMetrics;
import android.view.Display;
import android.view.ViewGroup;
import android.view.WindowInsets;
import android.view.WindowManager;
import android.view.WindowMetrics;
import android.widget.FrameLayout;
import androidx.annotation.VisibleForTesting;
import com.google.android.setupcompat.partnerconfig.PartnerConfig;
import com.google.android.setupcompat.partnerconfig.PartnerConfigHelper;
import com.google.android.setupcompat.util.BuildCompatUtils;
import com.google.android.setupdesign.R;

/**
 * A FrameLayout subclass that has an "intrinsic size", which is the size it wants to be if that is
 * within the constraints given by the parent. The intrinsic size can be set with the {@code
 * android:width} and {@code android:height} attributes in XML.
 *
 * <p>Note that for the intrinsic size to be meaningful, {@code android:layout_width} and/or {@code
 * android:layout_height} will need to be {@code wrap_content}.
 */
public class IntrinsicSizeFrameLayout extends FrameLayout {

  private int intrinsicHeight = 0;
  private int intrinsicWidth = 0;
  private Object lastInsets; // Use generic Object type for compatibility

  // Define here to avoid allocating resource during layout/draw operation.
  private final Rect windowVisibleDisplayRect = new Rect();

  public IntrinsicSizeFrameLayout(Context context) {
    super(context);
    init(context, null, 0);
  }

  public IntrinsicSizeFrameLayout(Context context, AttributeSet attrs) {
    super(context, attrs);
    init(context, attrs, 0);
  }

  @TargetApi(VERSION_CODES.HONEYCOMB)
  public IntrinsicSizeFrameLayout(Context context, AttributeSet attrs, int defStyleAttr) {
    super(context, attrs, defStyleAttr);
    init(context, attrs, defStyleAttr);
  }

  private void init(Context context, AttributeSet attrs, int defStyleAttr) {
    if (isInEditMode()) {
      return;
    }

    final TypedArray a =
        context.obtainStyledAttributes(
            attrs, R.styleable.SudIntrinsicSizeFrameLayout, defStyleAttr, 0);
    intrinsicHeight =
        a.getDimensionPixelSize(R.styleable.SudIntrinsicSizeFrameLayout_android_height, 0);
    intrinsicWidth =
        a.getDimensionPixelSize(R.styleable.SudIntrinsicSizeFrameLayout_android_width, 0);
    a.recycle();

    if (BuildCompatUtils.isAtLeastS()) {
      if (PartnerConfigHelper.get(context)
          .isPartnerConfigAvailable(PartnerConfig.CONFIG_CARD_VIEW_INTRINSIC_HEIGHT)) {
        intrinsicHeight =
            (int)
                PartnerConfigHelper.get(context)
                    .getDimension(context, PartnerConfig.CONFIG_CARD_VIEW_INTRINSIC_HEIGHT);
      }
      if (PartnerConfigHelper.get(context)
          .isPartnerConfigAvailable(PartnerConfig.CONFIG_CARD_VIEW_INTRINSIC_WIDTH)) {
        intrinsicWidth =
            (int)
                PartnerConfigHelper.get(context)
                    .getDimension(context, PartnerConfig.CONFIG_CARD_VIEW_INTRINSIC_WIDTH);
      }
    }
  }

  @Override
  public void setLayoutParams(ViewGroup.LayoutParams params) {
    if (BuildCompatUtils.isAtLeastS()) {
      // When both intrinsic height and width are 0, the card view style would be removed from
      // foldable/tablet layout. It must set the layout width and height to MATCH_PARENT and then it
      // can ignore the IntrinsicSizeFrameLayout from the foldable/tablet layout.
      if (intrinsicHeight == 0 && intrinsicWidth == 0) {
        params.width = ViewGroup.LayoutParams.MATCH_PARENT;
        params.height = ViewGroup.LayoutParams.MATCH_PARENT;
        setElevation(0.0f);
      }
    }
    super.setLayoutParams(params);
  }

  @Override
  protected void onMeasure(int widthMeasureSpec, int heightMeasureSpec) {
    int measureWidth;

    // The the content may be truncated if the layout show in multi-window mode or two pane mode,
    // because the given width is fixed size which based on the display to compute. So the width
    // the content may be truncated. Make the layout width align window while window width smaller
    // than display size.
    if (isWindowSizeSmallerThanDisplaySize()) {
      getWindowVisibleDisplayFrame(windowVisibleDisplayRect);

      measureWidth =
          MeasureSpec.makeMeasureSpec(windowVisibleDisplayRect.width(), MeasureSpec.EXACTLY);
    } else {
      measureWidth = getIntrinsicMeasureSpec(widthMeasureSpec, intrinsicWidth);
    }

    super.onMeasure(measureWidth, getIntrinsicMeasureSpec(heightMeasureSpec, intrinsicHeight));
  }

  @VisibleForTesting
  boolean isWindowSizeSmallerThanDisplaySize() {
    boolean result = false;
    /// NOMUTANTS--reason(b/221151816): Support setCurrentWindowMetrics from ShadowWindowManagerImpl
    // to verify this case
    if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.S) {
      WindowManager windowManager = getContext().getSystemService(WindowManager.class);
      WindowMetrics windowMetrics = windowManager.getCurrentWindowMetrics();
      windowVisibleDisplayRect.set(windowMetrics.getBounds());

      Display display = getDisplay();
      if (display != null) {
        DisplayMetrics displayMetrics = new DisplayMetrics();
        display.getRealMetrics(displayMetrics);

        if (windowVisibleDisplayRect.width() > 0
            && windowVisibleDisplayRect.width() < displayMetrics.widthPixels) {
          result = true;
        }
      }
    }

    return result;
  }

  private int getIntrinsicMeasureSpec(int measureSpec, int intrinsicSize) {
    if (intrinsicSize <= 0) {
      // Intrinsic size is not set, just return the original spec
      return measureSpec;
    }
    final int mode = MeasureSpec.getMode(measureSpec);
    final int size = MeasureSpec.getSize(measureSpec);
    if (mode == MeasureSpec.UNSPECIFIED) {
      // Parent did not give any constraint, so we'll be the intrinsic size
      return MeasureSpec.makeMeasureSpec(intrinsicHeight, MeasureSpec.EXACTLY);
    } else if (mode == MeasureSpec.AT_MOST) {
      // If intrinsic size is within parents constraint, take the intrinsic size.
      // Otherwise take the parents size because that's closest to the intrinsic size.
      return MeasureSpec.makeMeasureSpec(min(size, intrinsicHeight), MeasureSpec.EXACTLY);
    }
    // Parent specified EXACTLY, or in all other cases, just return the original spec
    return measureSpec;
  }

  @Override
  protected void onAttachedToWindow() {
    super.onAttachedToWindow();
    if (Build.VERSION.SDK_INT >= VERSION_CODES.LOLLIPOP) {
      if (lastInsets == null) {
        requestApplyInsets();
      }
    }
  }

  @Override
  public WindowInsets onApplyWindowInsets(WindowInsets insets) {
    lastInsets = insets;
    return super.onApplyWindowInsets(insets);
  }
}
