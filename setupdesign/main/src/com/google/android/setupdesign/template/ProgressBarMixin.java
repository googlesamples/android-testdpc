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
import android.content.res.ColorStateList;
import android.content.res.TypedArray;
import android.os.Build;
import android.os.Build.VERSION_CODES;
import android.util.AttributeSet;
import android.util.Log;
import android.view.View;
import android.view.ViewGroup;
import android.view.ViewStub;
import android.widget.ProgressBar;
import androidx.annotation.AttrRes;
import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.annotation.VisibleForTesting;
import com.google.android.material.progressindicator.LinearProgressIndicator;
import com.google.android.setupcompat.internal.TemplateLayout;
import com.google.android.setupcompat.partnerconfig.PartnerConfigHelper;
import com.google.android.setupcompat.template.Mixin;
import com.google.android.setupdesign.R;
import com.google.android.setupdesign.util.HeaderAreaStyler;
import com.google.android.setupdesign.util.PartnerStyleHelper;

/** A {@link Mixin} for showing a progress bar. */
public class ProgressBarMixin implements Mixin {

  private static final String TAG = "ProgressBarMixin";
  private final TemplateLayout templateLayout;
  private final boolean useBottomProgressBar;
  private final boolean isGlifExpressiveEnabled;
  @Nullable private ColorStateList color;

  /** @param layout The layout this mixin belongs to. */
  public ProgressBarMixin(@NonNull TemplateLayout layout) {
    this(layout, null, 0);
  }

  /**
   * Constructor that allow using the bottom progress bar.
   *
   * @param layout The layout this mixin belongs to.
   * @param useBottomProgressBar Whether to use bottom progress
   */
  public ProgressBarMixin(@NonNull TemplateLayout layout, boolean useBottomProgressBar) {
    templateLayout = layout;
    this.useBottomProgressBar = useBottomProgressBar;
    isGlifExpressiveEnabled = PartnerConfigHelper.isGlifExpressiveEnabled(layout.getContext());
  }

  /**
   * Constructor that provide styled attribute information in this Context's theme.
   *
   * @param layout The {@link TemplateLayout} containing this mixin.
   * @param attrs XML attributes given to the layout.
   * @param defStyleAttr The default style attribute as given to the constructor of the layout.
   */
  public ProgressBarMixin(
      @NonNull TemplateLayout layout, AttributeSet attrs, @AttrRes int defStyleAttr) {
    templateLayout = layout;

    boolean useBottomProgressBar = false;
    if (attrs != null) {
      final TypedArray a =
          layout
              .getContext()
              .obtainStyledAttributes(attrs, R.styleable.SudProgressBarMixin, defStyleAttr, 0);

      if (a.hasValue(R.styleable.SudProgressBarMixin_sudUseBottomProgressBar)) {
        // Set whether we use bottom progress bar or not
        useBottomProgressBar =
            a.getBoolean(R.styleable.SudProgressBarMixin_sudUseBottomProgressBar, false);
      }

      a.recycle();

      // To avoid bottom progressbar bouncing, change the view state from GONE to INVISIBLE
      setShown(false);
    }

    this.useBottomProgressBar = useBottomProgressBar;
    isGlifExpressiveEnabled = PartnerConfigHelper.isGlifExpressiveEnabled(layout.getContext());
  }

  /** @return True if the progress bar is currently shown. */
  public boolean isShown() {
    final View progressBar;
    if (isGlifExpressiveEnabled) {
      progressBar = templateLayout.findManagedViewById(R.id.sud_layout_progress_indicator);
    } else {
      progressBar =
          templateLayout.findManagedViewById(
              useBottomProgressBar ? R.id.sud_glif_progress_bar : R.id.sud_layout_progress);
    }
    return progressBar != null && progressBar.getVisibility() == View.VISIBLE;
  }

  /**
   * Sets whether the progress bar is shown. If the progress bar has not been inflated from the
   * stub, this method will inflate the progress bar.
   *
   * @param shown True to show the progress bar, false to hide it.
   */
  public void setShown(boolean shown) {
    if (shown) {
      View progressBar = getProgressBar();
      if (progressBar != null) {
        progressBar.setVisibility(View.VISIBLE);
      }
    } else {
      View progressBar = peekProgressBar();
      if (progressBar != null) {
        progressBar.setVisibility(useBottomProgressBar ? View.INVISIBLE : View.GONE);
      }
    }
  }

  /**
   * Gets the progress bar in the layout. If the progress bar has not been used before, it will be
   * installed (i.e. inflated from its view stub).
   *
   * @return The progress bar of this layout. May be null only if the template used doesn't have a
   *     progress bar built-in.
   */
  @VisibleForTesting
  protected View getProgressBar() {
    final View progressBarView = peekProgressBar();
    if (progressBarView == null) {
      if (isGlifExpressiveEnabled) {
        final ViewStub progressIndicatorStub =
            (ViewStub) templateLayout.findManagedViewById(R.id.sud_glif_progress_indicator_stub);
        if (progressIndicatorStub != null) {
          progressIndicatorStub.inflate();
        }
      } else if (!useBottomProgressBar) {
        final ViewStub progressBarStub =
            (ViewStub) templateLayout.findManagedViewById(R.id.sud_layout_progress_stub);
        if (progressBarStub != null) {
          progressBarStub.inflate();
        }
        setColor(color);
      }
    }
    return peekProgressBar();
  }

  /**
   * Gets the progress bar in the layout only if it has been installed. {@link #setShown(boolean)}
   * should be called before this to ensure the progress bar is set up correctly.
   *
   * @return The progress bar of this layout, or null if the progress bar is not installed. The null
   *     case can happen either if {@link #setShown(boolean)} with true was not called before this,
   *     or if the template does not contain a progress bar.
   */
  public ProgressBar peekProgressBar() {
    if (isGlifExpressiveEnabled) {
      LinearProgressIndicator progressIndicator =
          templateLayout.findManagedViewById(R.id.sud_layout_progress_indicator);
      return (ProgressBar) progressIndicator;
    } else {
      return (ProgressBar)
          templateLayout.findManagedViewById(
              useBottomProgressBar ? R.id.sud_glif_progress_bar : R.id.sud_layout_progress);
    }
  }

  /** Sets the color of the indeterminate progress bar. This method is a no-op on SDK < 21. */
  /**
   * @deprecated Use {@link ProgressBar#setProgressBackgroundTintList(int)} or {@link
   *     LinearProgressIndicator#setIndeterminateTintList(int)} and {@link
   *     LinearProgressIndicator#setTrackColor(int)} instead.
   */
  @Deprecated
  public void setColor(@Nullable ColorStateList color) {
    this.color = color;
    if (Build.VERSION.SDK_INT >= VERSION_CODES.LOLLIPOP) {
      final View view = peekProgressBar();
      if (view != null) {
        if (view instanceof ProgressBar) {
          ProgressBar bar = (ProgressBar) view;
          bar.setIndeterminateTintList(color);
          if (Build.VERSION.SDK_INT >= VERSION_CODES.M || color != null) {
            // There is a bug in Lollipop where setting the progress tint color to null
            // will crash with "java.lang.NullPointerException: Attempt to invoke virtual
            // method 'int android.graphics.Paint.getAlpha()' on a null object reference"
            // at android.graphics.drawable.NinePatchDrawable.draw(:250)
            // The bug doesn't affect ProgressBar on M because it uses ShapeDrawable instead
            // of NinePatchDrawable. (commit 6a8253fdc9f4574c28b4beeeed90580ffc93734a)
            bar.setProgressBackgroundTintList(color);
          }
        } else if (view instanceof LinearProgressIndicator) {
          // TODO: b/377241556 - Set color from the view LinearProgressIndicator.
        }
      }
    }
  }

  /**
   * @return The color previously set in {@link #setColor(ColorStateList)}, or null if the color is
   *     not set. In case of null, the color of the progress bar will be inherited from the theme.
   */
  @Nullable
  public ColorStateList getColor() {
    return color;
  }

  /**
   * Tries to apply the partner customizations to the progress bar. Use the default values if
   * partner config isn't enable.
   */
  public void tryApplyPartnerCustomizationStyle() {
    View progressBar = peekProgressBar();
    if (!useBottomProgressBar || progressBar == null) {
      return;
    }

    boolean partnerHeavyThemeLayout = PartnerStyleHelper.isPartnerHeavyThemeLayout(templateLayout);

    if (partnerHeavyThemeLayout) {
      if (progressBar instanceof ProgressBar) {
        HeaderAreaStyler.applyPartnerCustomizationProgressBarStyle((ProgressBar) progressBar);
      } else {
        Log.w(TAG, "The view is not a ProgressBar");
      }
    } else {
      Context context = progressBar.getContext();
      final ViewGroup.LayoutParams lp = progressBar.getLayoutParams();

      if (lp instanceof ViewGroup.MarginLayoutParams) {
        int marginTop =
            (int) context.getResources().getDimension(R.dimen.sud_progress_bar_margin_top);
        int marginBottom =
            (int) context.getResources().getDimension(R.dimen.sud_progress_bar_margin_bottom);

        final ViewGroup.MarginLayoutParams mlp = (ViewGroup.MarginLayoutParams) lp;
        mlp.setMargins(mlp.leftMargin, marginTop, mlp.rightMargin, marginBottom);
      }
    }
  }
}
