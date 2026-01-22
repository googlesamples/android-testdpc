/*
 * Copyright (C) 2021 The Android Open Source Project
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

package com.google.android.setupdesign.widget;

import android.graphics.Canvas;
import android.graphics.ColorFilter;
import android.graphics.Paint;
import android.graphics.Path;
import android.graphics.Path.Direction;
import android.graphics.Path.FillType;
import android.graphics.PixelFormat;
import android.graphics.Rect;
import android.graphics.RectF;
import android.graphics.drawable.Drawable;
import androidx.annotation.ColorInt;
import androidx.annotation.Nullable;

/** A rounded rectangle drawable. */
public class CardBackgroundDrawable extends Drawable {
  private final float inset;

  private final Paint paint;
  private final RectF cardBounds = new RectF();
  private final Path clipPath = new Path();

  private float cornerRadius;
  private boolean dirty = false;

  /**
   * @param color Background color of the card to be rendered
   * @param radius Corner rounding radius
   * @param inset Inset from the edge of the canvas to the card
   */
  public CardBackgroundDrawable(@ColorInt int color, float radius, float inset) {
    cornerRadius = radius;
    paint = new Paint(Paint.ANTI_ALIAS_FLAG | Paint.DITHER_FLAG);
    paint.setColor(color);
    this.inset = inset;
  }

  @Override
  public void onBoundsChange(Rect bounds) {
    super.onBoundsChange(bounds);
    dirty = true;
  }

  @Override
  public void setColorFilter(@Nullable ColorFilter cf) {
    paint.setColorFilter(cf);
  }

  @Override
  public int getOpacity() {
    return PixelFormat.OPAQUE;
  }

  public void setCornerRadius(float radius) {
    if (cornerRadius == radius) {
      return;
    }

    cornerRadius = radius;
    dirty = true;
    invalidateSelf();
  }

  @Override
  public void draw(Canvas canvas) {
    if (dirty) {
      buildComponents(getBounds());
      dirty = false;
    }

    if (cornerRadius > 0) {
      canvas.clipPath(clipPath);
    }
  }

  @Override
  public void setAlpha(int alpha) {}

  private void buildComponents(Rect bounds) {
    cardBounds.set(bounds);
    cardBounds.inset(inset, inset);

    clipPath.reset();
    clipPath.setFillType(FillType.EVEN_ODD);
    clipPath.addRoundRect(cardBounds, cornerRadius, cornerRadius, Direction.CW);
  }
}
