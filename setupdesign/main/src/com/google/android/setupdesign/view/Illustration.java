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

package com.google.android.setupdesign.view;

import android.annotation.TargetApi;
import android.content.Context;
import android.content.pm.ApplicationInfo;
import android.content.res.TypedArray;
import android.graphics.Canvas;
import android.graphics.Rect;
import android.graphics.drawable.Drawable;
import android.os.Build.VERSION;
import android.os.Build.VERSION_CODES;
import android.util.AttributeSet;
import android.util.LayoutDirection;
import android.view.Gravity;
import android.view.ViewOutlineProvider;
import android.widget.FrameLayout;
import com.google.android.setupdesign.R;

/**
 * Class to draw the illustration of setup wizard. The {@code aspectRatio} attribute determines the
 * aspect ratio of the top padding, which leaves space for the illustration. Draws the illustration
 * drawable to fit the width of the view and fills the rest with the background.
 *
 * <p>If an aspect ratio is set, then the aspect ratio of the source drawable is maintained.
 * Otherwise the aspect ratio will be ignored, only increasing the width of the illustration.
 */
public class Illustration extends FrameLayout {

  // Size of the baseline grid in pixels
  private float baselineGridSize;
  private Drawable background;
  private Drawable illustration;
  private final Rect viewBounds = new Rect();
  private final Rect illustrationBounds = new Rect();
  private float scale = 1.0f;
  private float aspectRatio = 0.0f;

  public Illustration(Context context) {
    super(context);
    init(null, 0);
  }

  public Illustration(Context context, AttributeSet attrs) {
    super(context, attrs);
    init(attrs, 0);
  }

  @TargetApi(VERSION_CODES.HONEYCOMB)
  public Illustration(Context context, AttributeSet attrs, int defStyleAttr) {
    super(context, attrs, defStyleAttr);
    init(attrs, defStyleAttr);
  }

  // All the constructors delegate to this init method. The 3-argument constructor is not
  // available in FrameLayout before v11, so call super with the exact same arguments.
  private void init(AttributeSet attrs, int defStyleAttr) {
    if (isInEditMode()) {
      return;
    }

    if (attrs != null) {
      TypedArray a =
          getContext().obtainStyledAttributes(attrs, R.styleable.SudIllustration, defStyleAttr, 0);
      aspectRatio = a.getFloat(R.styleable.SudIllustration_sudAspectRatio, 0.0f);
      a.recycle();
    }
    // Number of pixels of the 8dp baseline grid as defined in material design specs
    baselineGridSize = getResources().getDisplayMetrics().density * 8;
    setWillNotDraw(false);
  }

  /**
   * The background will be drawn to fill up the rest of the view. It will also be scaled by the
   * same amount as the foreground so their textures look the same.
   */
  // Override the deprecated setBackgroundDrawable method to support API < 16. View.setBackground
  // forwards to setBackgroundDrawable in the framework implementation.
  @SuppressWarnings("deprecation")
  @Override
  public void setBackgroundDrawable(Drawable background) {
    if (background == this.background) {
      return;
    }
    this.background = background;
    invalidate();
    requestLayout();
  }

  /**
   * Sets the drawable used as the illustration. The drawable is expected to have intrinsic width
   * and height defined and will be scaled to fit the width of the view.
   */
  public void setIllustration(Drawable illustration) {
    if (illustration == this.illustration) {
      return;
    }
    this.illustration = illustration;
    invalidate();
    requestLayout();
  }

  /**
   * Set the aspect ratio reserved for the illustration. This overrides the top padding of the view
   * according to the width of this view and the aspect ratio. Children views will start being laid
   * out below this aspect ratio.
   *
   * @param aspectRatio A float value specifying the aspect ratio (= width / height). 0 to not
   *     override the top padding.
   */
  public void setAspectRatio(float aspectRatio) {
    this.aspectRatio = aspectRatio;
    invalidate();
    requestLayout();
  }

  @Override
  @Deprecated
  public void setForeground(Drawable d) {
    setIllustration(d);
  }

  @Override
  protected void onMeasure(int widthMeasureSpec, int heightMeasureSpec) {
    if (aspectRatio != 0.0f) {
      int parentWidth = MeasureSpec.getSize(widthMeasureSpec);
      int illustrationHeight = (int) (parentWidth / aspectRatio);
      illustrationHeight = (int) (illustrationHeight - (illustrationHeight % baselineGridSize));
      setPadding(0, illustrationHeight, 0, 0);
    }
    if (VERSION.SDK_INT >= VERSION_CODES.LOLLIPOP) {
      //noinspection AndroidLintInlinedApi
      setOutlineProvider(ViewOutlineProvider.BOUNDS);
    }
    super.onMeasure(widthMeasureSpec, heightMeasureSpec);
  }

  @Override
  protected void onLayout(boolean changed, int left, int top, int right, int bottom) {
    final int layoutWidth = right - left;
    final int layoutHeight = bottom - top;
    if (illustration != null) {
      int intrinsicWidth = illustration.getIntrinsicWidth();
      int intrinsicHeight = illustration.getIntrinsicHeight();

      viewBounds.set(0, 0, layoutWidth, layoutHeight);
      if (aspectRatio != 0f) {
        scale = layoutWidth / (float) intrinsicWidth;
        intrinsicWidth = layoutWidth;
        intrinsicHeight = (int) (intrinsicHeight * scale);
      }
      Gravity.apply(
          Gravity.FILL_HORIZONTAL | Gravity.TOP,
          intrinsicWidth,
          intrinsicHeight,
          viewBounds,
          illustrationBounds);
      illustration.setBounds(illustrationBounds);
    }
    if (background != null) {
      // Scale the background bounds by the same scale to compensate for the scale done to the
      // canvas in onDraw.
      background.setBounds(
          0,
          0,
          (int) Math.ceil(layoutWidth / scale),
          (int) Math.ceil((layoutHeight - illustrationBounds.height()) / scale));
    }
    super.onLayout(changed, left, top, right, bottom);
  }

  @Override
  public void onDraw(Canvas canvas) {
    if (background != null) {
      // Draw the background filling parts not covered by the illustration
      canvas.save();
      canvas.translate(0, illustrationBounds.height());
      // Scale the background so its size matches the foreground
      canvas.scale(scale, scale, 0, 0);
      if (VERSION.SDK_INT > VERSION_CODES.JELLY_BEAN_MR1
          && shouldMirrorDrawable(background, getLayoutDirection())) {
        // Flip the illustration for RTL layouts
        canvas.scale(-1, 1);
        canvas.translate(-background.getBounds().width(), 0);
      }
      background.draw(canvas);
      canvas.restore();
    }
    if (illustration != null) {
      canvas.save();
      if (VERSION.SDK_INT > VERSION_CODES.JELLY_BEAN_MR1
          && shouldMirrorDrawable(illustration, getLayoutDirection())) {
        // Flip the illustration for RTL layouts
        canvas.scale(-1, 1);
        canvas.translate(-illustrationBounds.width(), 0);
      }
      // Draw the illustration
      illustration.draw(canvas);
      canvas.restore();
    }
    super.onDraw(canvas);
  }

  private boolean shouldMirrorDrawable(Drawable drawable, int layoutDirection) {
    if (layoutDirection == LayoutDirection.RTL) {
      if (VERSION.SDK_INT >= VERSION_CODES.KITKAT) {
        return drawable.isAutoMirrored();
      } else if (VERSION.SDK_INT >= VERSION_CODES.JELLY_BEAN_MR1) {
        final int flags = getContext().getApplicationInfo().flags;
        //noinspection AndroidLintInlinedApi
        return (flags & ApplicationInfo.FLAG_SUPPORTS_RTL) != 0;
      }
    }
    return false;
  }
}
