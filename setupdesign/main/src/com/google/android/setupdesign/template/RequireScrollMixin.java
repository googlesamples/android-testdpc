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
import android.graphics.Color;
import android.graphics.drawable.Drawable;
import android.os.Handler;
import android.os.Looper;
import android.util.Log;
import android.view.View;
import android.view.View.OnClickListener;
import android.widget.Button;
import android.widget.LinearLayout;
import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.annotation.StringRes;
import com.google.android.material.button.MaterialButton;
import com.google.android.setupcompat.internal.TemplateLayout;
import com.google.android.setupcompat.partnerconfig.PartnerConfigHelper;
import com.google.android.setupcompat.template.FooterBarMixin;
import com.google.android.setupcompat.template.FooterButton;
import com.google.android.setupcompat.template.Mixin;
import com.google.android.setupdesign.GlifLayout;
import com.google.android.setupdesign.R;
import com.google.android.setupdesign.view.NavigationBar;

/**
 * A mixin to require the a scrollable container (BottomScrollView, RecyclerView or ListView) to be
 * scrolled to bottom, making sure that the user sees all content above and below the fold.
 */
public class RequireScrollMixin implements Mixin {

  private static final String LOG_TAG = "RequireScrollMixin";

  /* static section */

  /**
   * Listener for when the require-scroll state changes. Note that this only requires the user to
   * scroll to the bottom once - if the user scrolled to the bottom and back-up, scrolling to bottom
   * is not required again.
   */
  public interface OnRequireScrollStateChangedListener {

    /**
     * Called when require-scroll state changed.
     *
     * @param scrollNeeded True if the user should be required to scroll to bottom.
     */
    void onRequireScrollStateChanged(boolean scrollNeeded);
  }

  /**
   * A delegate to detect scrollability changes and to scroll the page. This provides a layer of
   * abstraction for BottomScrollView, RecyclerView and ListView. The delegate should call {@link
   * #notifyScrollabilityChange(boolean)} when the view scrollability is changed.
   */
  interface ScrollHandlingDelegate {

    /** Starts listening to scrollability changes at the target scrollable container. */
    void startListening();

    /** Scroll the page content down by one page. */
    void pageScrollDown();
  }

  /* non-static section */

  private final Handler handler = new Handler(Looper.getMainLooper());

  private boolean requiringScrollToBottom = false;

  // Whether the user have seen the more button yet.
  private boolean everScrolledToBottom = false;

  private ScrollHandlingDelegate delegate;

  private final TemplateLayout templateLayout;

  @Nullable private OnRequireScrollStateChangedListener listener;

  /**
   * @param templateLayout The template containing this mixin
   */
  public RequireScrollMixin(@NonNull TemplateLayout templateLayout) {
    this.templateLayout = templateLayout;
  }

  /**
   * Sets the delegate to handle scrolling. The type of delegate should depend on whether the
   * scrolling view is a BottomScrollView, RecyclerView or ListView.
   */
  public void setScrollHandlingDelegate(@NonNull ScrollHandlingDelegate delegate) {
    this.delegate = delegate;
  }

  /**
   * Listen to require scroll state changes. When scroll is required, {@link
   * OnRequireScrollStateChangedListener#onRequireScrollStateChanged(boolean)} is called with {@code
   * true}, and vice versa.
   */
  public void setOnRequireScrollStateChangedListener(
      @Nullable OnRequireScrollStateChangedListener listener) {
    this.listener = listener;
  }

  /** Returns the scroll state listener previously set, or {@code null} if none is registered. */
  public OnRequireScrollStateChangedListener getOnRequireScrollStateChangedListener() {
    return listener;
  }

  /**
   * Creates an {@link OnClickListener} which if scrolling is required, will scroll the page down,
   * and if scrolling is not required, delegates to the wrapped {@code listener}. Note that you
   * should call {@link #requireScroll()} as well in order to start requiring scrolling.
   *
   * @param listener The listener to be invoked when scrolling is not needed and the user taps on
   *     the button. If {@code null}, the click listener will be a no-op when scroll is not
   *     required.
   * @return A new {@link OnClickListener} which will scroll the page down or delegate to the given
   *     listener depending on the current require-scroll state.
   */
  public OnClickListener createOnClickListener(@Nullable final OnClickListener listener) {
    return new OnClickListener() {
      @Override
      public void onClick(View view) {
        if (requiringScrollToBottom) {
          delegate.pageScrollDown();
        } else if (listener != null) {
          listener.onClick(view);
        }
      }
    };
  }

  /**
   * Coordinate with the given navigation bar to require scrolling on the page. The more button will
   * be shown instead of the next button while scrolling is required.
   */
  public void requireScrollWithNavigationBar(@NonNull final NavigationBar navigationBar) {
    setOnRequireScrollStateChangedListener(
        new OnRequireScrollStateChangedListener() {
          @Override
          public void onRequireScrollStateChanged(boolean scrollNeeded) {
            navigationBar.getMoreButton().setVisibility(scrollNeeded ? View.VISIBLE : View.GONE);
            navigationBar.getNextButton().setVisibility(scrollNeeded ? View.GONE : View.VISIBLE);
          }
        });
    navigationBar.getMoreButton().setOnClickListener(createOnClickListener(null));
    requireScroll();
  }

  /** See {@link #requireScrollWithButton(Button, CharSequence, OnClickListener)}. */
  public void requireScrollWithButton(
      @NonNull Button button, @StringRes int moreText, @Nullable OnClickListener onClickListener) {
    requireScrollWithButton(button, button.getContext().getText(moreText), onClickListener);
  }

  /**
   * Use the given {@code button} to require scrolling. When scrolling is required, the button label
   * will change to {@code moreText}, and tapping the button will cause the page to scroll down.
   *
   * <p>Note: Calling {@link View#setOnClickListener} on the button after this method will remove
   * its link to the require-scroll mechanism. If you need to do that, obtain the click listener
   * from {@link #createOnClickListener(OnClickListener)}.
   *
   * <p>Note: The normal button label is taken from the button's text at the time of calling this
   * method. Calling {@link android.widget.TextView#setText} after calling this method causes
   * undefined behavior.
   *
   * @param button The button to use for require scroll. The button's "normal" label is taken from
   *     the text at the time of calling this method, and the click listener of it will be replaced.
   * @param moreText The button label when scroll is required.
   * @param onClickListener The listener for clicks when scrolling is not required.
   */
  public void requireScrollWithButton(
      @NonNull final Button button,
      final CharSequence moreText,
      @Nullable OnClickListener onClickListener) {
    final CharSequence nextText = button.getText();
    button.setOnClickListener(createOnClickListener(onClickListener));
    setOnRequireScrollStateChangedListener(
        new OnRequireScrollStateChangedListener() {
          @Override
          public void onRequireScrollStateChanged(boolean scrollNeeded) {
            button.setText(scrollNeeded ? moreText : nextText);
          }
        });
    requireScroll();
  }

  /**
   * Use the given {@code button} to require scrolling. When scrolling is required, the button label
   * will change to {@code moreText}, and tapping the button will cause the page to scroll down.
   *
   * <p>Note: Calling {@link View#setOnClickListener} on the button after this method will remove
   * its link to the require-scroll mechanism. If you need to do that, obtain the click listener
   * from {@link #createOnClickListener(OnClickListener)}.
   *
   * <p>Note: The normal button label is taken from the button's text at the time of calling this
   * method. Calling {@link android.widget.TextView#setText} after calling this method causes
   * undefined behavior.
   *
   * @param button The button to use for require scroll. The button's "normal" label is taken from
   *     the text at the time of calling this method, and the click listener of it will be replaced.
   * @param moreText The button label when scroll is required.
   * @param onClickListener The listener for clicks when scrolling is not required.
   */
  public void requireScrollWithButton(
      @NonNull Context context,
      @NonNull FooterButton button,
      @StringRes int moreText,
      @Nullable OnClickListener onClickListener) {
    requireScrollWithButton(button, context.getText(moreText), onClickListener);
  }

  /**
   * Use the given {@code button} to require scrolling. When scrolling is required, the button label
   * will change to {@code moreText}, and tapping the button will cause the page to scroll down.
   *
   * <p>Note: Calling {@link View#setOnClickListener} on the button after this method will remove
   * its link to the require-scroll mechanism. If you need to do that, obtain the click listener
   * from {@link #createOnClickListener(OnClickListener)}.
   *
   * <p>Note: The normal button label is taken from the button's text at the time of calling this
   * method. Calling {@link android.widget.TextView#setText} after calling this method causes
   * undefined behavior.
   *
   * @param button The button to use for require scroll. The button's "normal" label is taken from
   *     the text at the time of calling this method, and the click listener of it will be replaced.
   * @param moreText The button label when scroll is required.
   * @param onClickListener The listener for clicks when scrolling is not required.
   */
  public void requireScrollWithButton(
      @NonNull final FooterButton button,
      final CharSequence moreText,
      @Nullable OnClickListener onClickListener) {
    Context context = templateLayout.getContext();
    if (PartnerConfigHelper.isGlifExpressiveEnabled(context)) {
      requireScrollWithDownButton(context, onClickListener);
    } else {
      final CharSequence nextText = button.getText();
      button.setOnClickListener(createOnClickListener(onClickListener));
      setOnRequireScrollStateChangedListener(
          new OnRequireScrollStateChangedListener() {
            @Override
            public void onRequireScrollStateChanged(boolean scrollNeeded) {
              button.setText(scrollNeeded ? moreText : nextText);
            }
          });
      requireScroll();
    }
  }

  /**
   * Use the given {@code primaryButton} to require scrolling. When scrolling is required, the
   * primary button label will change to {@code moreText}, and the secondary button will be hidden.
   * Tapping the primary button will cause the page to scroll down and reveal both the primary and
   * secondary buttons.
   *
   * <p>Note: Calling {@link View#setOnClickListener} on the primary button after this method will
   * remove its link to the require-scroll mechanism. If you need to do that, obtain the click
   * listener from {@link #createOnClickListener(OnClickListener)}.
   *
   * <p>Note: The normal button label is taken from the primary button's text at the time of calling
   * this method. Calling {@link android.widget.TextView#setText} after calling this method causes
   * undefined behavior.
   *
   * @param context The context used to resolve resource IDs.
   * @param primaryButton The button to use for require scroll. The button's "normal" label is taken
   *     from the text at the time of calling this method, and the click listener of it will be
   *     replaced.
   * @param secondaryButton The secondary button. This button will be hidden when scrolling is
   *     required.
   * @param moreText The primary button label when scroll is required.
   * @param onClickListener The listener for primary button clicks when scrolling is not required.
   */
  public void requireScrollWithButton(
      @NonNull Context context,
      @NonNull FooterButton primaryButton,
      @NonNull FooterButton secondaryButton,
      @StringRes int moreText,
      @Nullable OnClickListener onClickListener) {
    requireScrollWithButton(
        primaryButton, secondaryButton, context.getText(moreText), onClickListener);
  }

  /**
   * Use the given {@code primaryButton} to require scrolling. When scrolling is required, the
   * primary button label will change to {@code moreText}, and the secondary button will be hidden.
   * Tapping the primary button will cause the page to scroll down and reveal both the primary and
   * secondary buttons.
   *
   * <p>Note: Calling {@link View#setOnClickListener} on the primary button after this method will
   * remove its link to the require-scroll mechanism. If you need to do that, obtain the click
   * listener from {@link #createOnClickListener(OnClickListener)}.
   *
   * <p>Note: The normal button label is taken from the primary button's text at the time of calling
   * this method. Calling {@link android.widget.TextView#setText} after calling this method causes
   * undefined behavior.
   *
   * @param primaryButton The button to use for require scroll. The button's "normal" label is taken
   *     from the text at the time of calling this method, and the click listener of it will be
   *     replaced.
   * @param secondaryButton The secondary button. This button will be hidden when scrolling is
   *     required.
   * @param moreText The primary button label when scroll is required.
   * @param onClickListener The listener for primary button clicks when scrolling is not required.
   */
  public void requireScrollWithButton(
      @NonNull final FooterButton primaryButton,
      @NonNull final FooterButton secondaryButton,
      final CharSequence moreText,
      @Nullable OnClickListener onClickListener) {
    Context context = templateLayout.getContext();
    if (PartnerConfigHelper.isGlifExpressiveEnabled(context)) {
      requireScrollWithDownButton(context, onClickListener);
    } else {
      final CharSequence nextText = primaryButton.getText();
      primaryButton.setOnClickListener(createOnClickListener(onClickListener));
      setOnRequireScrollStateChangedListener(
          new OnRequireScrollStateChangedListener() {
            @Override
            public void onRequireScrollStateChanged(boolean scrollNeeded) {
              primaryButton.setText(scrollNeeded ? moreText : nextText);
              secondaryButton.setVisibility(scrollNeeded ? View.GONE : View.VISIBLE);
            }
          });
      requireScroll();
    }
  }

  public void requireScrollWithDownButton(
      @NonNull Context context, @Nullable OnClickListener onClickListener) {
    FooterBarMixin footerBarMixin = templateLayout.getMixin(FooterBarMixin.class);
    Button primaryButtonView = footerBarMixin.getPrimaryButtonView();
    Button secondaryButtonView = footerBarMixin.getSecondaryButtonView();
    CharSequence nextText = primaryButtonView.getText();
    primaryButtonView.setVisibility(View.INVISIBLE);
    primaryButtonView.setOnClickListener(createOnClickListener(onClickListener));
    footerBarMixin.setButtonWidthForExpressiveStyle(/* isDownButton= */ false);
    LinearLayout footerContainer = footerBarMixin.getButtonContainer();

    setOnRequireScrollStateChangedListener(
        scrollNeeded -> {
          if (scrollNeeded) {
            generateGlifExpressiveDownButton(context, primaryButtonView, footerBarMixin);
            footerContainer.setBackgroundColor(
                ((GlifLayout) templateLayout).getFooterBackgroundColorFromStyle());
          } else {
            // Switch style to glif expressive common button.
            if (primaryButtonView instanceof MaterialButton) {
              ((MaterialButton) primaryButtonView).setIcon(null);
              primaryButtonView.setText(nextText);
              footerBarMixin.setButtonWidthForExpressiveStyle(/* isDownButton= */ false);
              // Screen no need to scroll, sets the secondary button as visible if it exists.
              if (secondaryButtonView != null) {
                secondaryButtonView.setVisibility(View.VISIBLE);
              }
              footerContainer.setBackgroundColor(Color.TRANSPARENT);
            } else {
              Log.i(LOG_TAG, "Cannot clean up icon for the button. Skipping set text.");
            }
          }
        });
    primaryButtonView.setVisibility(View.VISIBLE);
    requireScroll();
  }

  private void generateGlifExpressiveDownButton(
      Context context, Button button, FooterBarMixin footerBarMixin) {
    Drawable icon = context.getResources().getDrawable(R.drawable.sud_ic_down_arrow);
    if (button instanceof MaterialButton) {
      // Remove the text and set down arrow icon to the button.
      button.setText("");
      ((MaterialButton) button).setIcon(icon);
      ((MaterialButton) button).setIconGravity(MaterialButton.ICON_GRAVITY_TEXT_START);
      ((MaterialButton) button).setIconPadding(0);
      footerBarMixin.setButtonWidthForExpressiveStyle(/* isDownButton= */ true);
    } else {
      Log.i(LOG_TAG, "Cannot set icon for the button. Skipping clean up text.");
    }
  }

  /**
   * @return True if scrolling is required. Note that this mixin only requires the user to scroll to
   *     the bottom once - if the user scrolled to the bottom and back-up, scrolling to bottom is
   *     not required again.
   */
  public boolean isScrollingRequired() {
    return requiringScrollToBottom;
  }

  /**
   * Start requiring scrolling on the layout. After calling this method, this mixin will start
   * listening to scroll events from the scrolling container, and call {@link
   * OnRequireScrollStateChangedListener} when the scroll state changes.
   */
  public void requireScroll() {
    delegate.startListening();
  }

  /**
   * {@link ScrollHandlingDelegate} should call this method when the scrollability of the scrolling
   * container changed, so this mixin can recompute whether scrolling should be required.
   *
   * @param canScrollDown True if the view can scroll down further.
   */
  void notifyScrollabilityChange(boolean canScrollDown) {
    if (canScrollDown == requiringScrollToBottom) {
      // Already at the desired require-scroll state
      return;
    }
    if (canScrollDown) {
      if (!everScrolledToBottom) {
        postScrollStateChange(true);
        requiringScrollToBottom = true;
      }
    } else {
      postScrollStateChange(false);
      requiringScrollToBottom = false;
      everScrolledToBottom = true;
    }
  }

  private void postScrollStateChange(final boolean scrollNeeded) {
    handler.post(
        new Runnable() {
          @Override
          public void run() {
            if (listener != null) {
              listener.onRequireScrollStateChanged(scrollNeeded);
            }
          }
        });
  }
}
