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

package com.google.android.setupcompat.template;

import static com.google.android.setupcompat.internal.Preconditions.ensureOnMainThread;
import static java.lang.Math.max;

import android.annotation.SuppressLint;
import android.annotation.TargetApi;
import android.content.Context;
import android.content.res.Configuration;
import android.content.res.TypedArray;
import android.graphics.Color;
import android.graphics.drawable.GradientDrawable;
import android.os.Build.VERSION_CODES;
import android.os.PersistableBundle;
import android.text.Layout.Alignment;
import android.text.StaticLayout;
import android.text.TextPaint;
import android.util.AttributeSet;
import android.util.DisplayMetrics;
import android.view.ContextThemeWrapper;
import android.view.Gravity;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.view.ViewStub;
import android.view.ViewTreeObserver;
import android.view.ViewTreeObserver.OnGlobalLayoutListener;
import android.widget.Button;
import android.widget.LinearLayout;
import android.widget.LinearLayout.LayoutParams;
import androidx.annotation.AttrRes;
import androidx.annotation.CallSuper;
import androidx.annotation.ColorInt;
import androidx.annotation.IdRes;
import androidx.annotation.LayoutRes;
import androidx.annotation.MainThread;
import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.annotation.StyleRes;
import androidx.annotation.VisibleForTesting;
import com.google.android.material.button.MaterialButton;
import com.google.android.setupcompat.PartnerCustomizationLayout;
import com.google.android.setupcompat.R;
import com.google.android.setupcompat.internal.FooterButtonPartnerConfig;
import com.google.android.setupcompat.internal.TemplateLayout;
import com.google.android.setupcompat.logging.LoggingObserver;
import com.google.android.setupcompat.logging.LoggingObserver.SetupCompatUiEvent.ButtonInflatedEvent;
import com.google.android.setupcompat.logging.internal.FooterBarMixinMetrics;
import com.google.android.setupcompat.partnerconfig.PartnerConfig;
import com.google.android.setupcompat.partnerconfig.PartnerConfigHelper;
import com.google.android.setupcompat.template.FooterButton.ButtonType;
import com.google.android.setupcompat.util.KeyboardHelper;
import com.google.android.setupcompat.util.Logger;
import com.google.android.setupcompat.view.ButtonBarLayout;
import java.util.Locale;

/**
 * A {@link Mixin} for managing buttons. By default, the button bar expects that buttons on the
 * start (left for LTR) are "secondary" borderless buttons, while buttons on the end (right for LTR)
 * are "primary" accent-colored buttons.
 */
public class FooterBarMixin implements Mixin {

  private static final Logger LOG = new Logger("FooterBarMixin");

  private final Context context;

  @Nullable private final ViewStub footerStub;

  @VisibleForTesting final boolean applyPartnerResources;
  @VisibleForTesting final boolean applyDynamicColor;
  @VisibleForTesting final boolean useFullDynamicColor;
  @VisibleForTesting final boolean footerButtonAlignEnd;

  @VisibleForTesting public LinearLayout buttonContainer;
  private FooterButton primaryButton;
  private FooterButton secondaryButton;
  private LoggingObserver loggingObserver;
  @IdRes private int primaryButtonId;
  @IdRes private int secondaryButtonId;
  @VisibleForTesting public FooterButtonPartnerConfig primaryButtonPartnerConfigForTesting;
  @VisibleForTesting public FooterButtonPartnerConfig secondaryButtonPartnerConfigForTesting;

  private int footerBarPaddingTop;
  private int footerBarPaddingBottom;
  @VisibleForTesting int footerBarPaddingStart;
  @VisibleForTesting int footerBarPaddingEnd;
  @VisibleForTesting int defaultPadding;
  @ColorInt private final int footerBarPrimaryBackgroundColor;
  @ColorInt private final int footerBarSecondaryBackgroundColor;
  private boolean removeFooterBarWhenEmpty = true;
  private boolean isSecondaryButtonInPrimaryStyle = false;
  private final int footerBarPrimaryButtonEnabledTextColor;
  private final int footerBarSecondaryButtonEnabledTextColor;
  private final int footerBarPrimaryButtonDisabledTextColor;
  private final int footerBarSecondaryButtonDisabledTextColor;
  @VisibleForTesting final int footerBarButtonMiddleSpacing;

  @VisibleForTesting public final FooterBarMixinMetrics metrics = new FooterBarMixinMetrics();

  private FooterButton.OnButtonEventListener createButtonEventListener(@IdRes int id) {

    return new FooterButton.OnButtonEventListener() {

      @Override
      public void onEnabledChanged(boolean enabled) {
        if (buttonContainer != null) {
          Button button = buttonContainer.findViewById(id);
          if (button != null) {
            button.setEnabled(enabled);

            // TODO: b/364981299 - Use partner config to allow user to customize text color.
            if (PartnerConfigHelper.isGlifExpressiveEnabled(context)) {
              if (id == primaryButtonId) {
                updateTextColorForButton(
                    button,
                    enabled,
                    enabled
                        ? footerBarPrimaryButtonEnabledTextColor
                        : footerBarPrimaryButtonDisabledTextColor);
              } else if (id == secondaryButtonId) {
                updateTextColorForButton(
                    button,
                    enabled,
                    enabled
                        ? footerBarSecondaryButtonEnabledTextColor
                        : footerBarSecondaryButtonDisabledTextColor);
              }
            } else {
              if (applyPartnerResources && !applyDynamicColor) {
                updateButtonTextColorWithStates(
                    button,
                    (id == primaryButtonId || isSecondaryButtonInPrimaryStyle)
                        ? PartnerConfig.CONFIG_FOOTER_PRIMARY_BUTTON_TEXT_COLOR
                        : PartnerConfig.CONFIG_FOOTER_SECONDARY_BUTTON_TEXT_COLOR,
                    (id == primaryButtonId || isSecondaryButtonInPrimaryStyle)
                        ? PartnerConfig.CONFIG_FOOTER_PRIMARY_BUTTON_DISABLED_TEXT_COLOR
                        : PartnerConfig.CONFIG_FOOTER_SECONDARY_BUTTON_DISABLED_TEXT_COLOR);
              }
            }
          }
        }
      }

      @Override
      public void onVisibilityChanged(int visibility) {
        if (buttonContainer != null) {
          Button button = buttonContainer.findViewById(id);
          if (button != null) {
            button.setVisibility(visibility);
            autoSetButtonBarVisibility();

            if (PartnerConfigHelper.isGlifExpressiveEnabled(context)) {
              setButtonWidthForExpressiveStyle(/* isDownButton= */ false);
            }
          }
        }
      }

      @Override
      public void onTextChanged(CharSequence text) {
        if (buttonContainer != null) {
          Button button = buttonContainer.findViewById(id);
          if (button != null) {
            button.setText(text);
          }
        }
      }

      @Override
      public void onLocaleChanged(Locale locale) {
        if (buttonContainer != null) {
          Button button = buttonContainer.findViewById(id);
          if (button != null && locale != null) {
            button.setTextLocale(locale);
          }
        }
      }

      @Override
      public void onDirectionChanged(int direction) {
        if (buttonContainer != null && direction != -1) {
          buttonContainer.setLayoutDirection(direction);
        }
      }
    };
  }

  /**
   * Creates a mixin for managing buttons on the footer.
   *
   * @param layout The {@link TemplateLayout} containing this mixin.
   * @param attrs XML attributes given to the layout.
   * @param defStyleAttr The default style attribute as given to the constructor of the layout.
   */
  public FooterBarMixin(
      TemplateLayout layout, @Nullable AttributeSet attrs, @AttrRes int defStyleAttr) {
    context = layout.getContext();
    footerStub = layout.findManagedViewById(R.id.suc_layout_footer);
    FooterButtonStyleUtils.clearSavedDefaultTextColor();

    this.applyPartnerResources =
        layout instanceof PartnerCustomizationLayout
            && ((PartnerCustomizationLayout) layout).shouldApplyPartnerResource();

    applyDynamicColor =
        layout instanceof PartnerCustomizationLayout
            && ((PartnerCustomizationLayout) layout).shouldApplyDynamicColor();

    useFullDynamicColor =
        layout instanceof PartnerCustomizationLayout
            && ((PartnerCustomizationLayout) layout).useFullDynamicColor();

    TypedArray a =
        context.obtainStyledAttributes(attrs, R.styleable.SucFooterBarMixin, defStyleAttr, 0);
    defaultPadding =
        a.getDimensionPixelSize(R.styleable.SucFooterBarMixin_sucFooterBarPaddingVertical, 0);
    footerBarPaddingTop =
        a.getDimensionPixelSize(
            R.styleable.SucFooterBarMixin_sucFooterBarPaddingTop, defaultPadding);
    footerBarPaddingBottom =
        a.getDimensionPixelSize(
            R.styleable.SucFooterBarMixin_sucFooterBarPaddingBottom, defaultPadding);
    footerBarPaddingStart =
        a.getDimensionPixelSize(R.styleable.SucFooterBarMixin_sucFooterBarPaddingStart, 0);
    footerBarPaddingEnd =
        a.getDimensionPixelSize(R.styleable.SucFooterBarMixin_sucFooterBarPaddingEnd, 0);
    footerBarPrimaryBackgroundColor =
        a.getColor(R.styleable.SucFooterBarMixin_sucFooterBarPrimaryFooterBackground, 0);
    footerBarSecondaryBackgroundColor =
        a.getColor(R.styleable.SucFooterBarMixin_sucFooterBarSecondaryFooterBackground, 0);
    footerButtonAlignEnd =
        a.getBoolean(R.styleable.SucFooterBarMixin_sucFooterBarButtonAlignEnd, false);
    footerBarPrimaryButtonEnabledTextColor =
        a.getColor(
            R.styleable.SucFooterBarMixin_sucFooterBarPrimaryFooterButtonEnabledTextColor, 0);
    footerBarSecondaryButtonEnabledTextColor =
        a.getColor(
            R.styleable.SucFooterBarMixin_sucFooterBarSecondaryFooterButtonEnabledTextColor, 0);
    footerBarPrimaryButtonDisabledTextColor =
        a.getColor(
            R.styleable.SucFooterBarMixin_sucFooterBarPrimaryFooterButtonDisabledTextColor, 0);
    footerBarSecondaryButtonDisabledTextColor =
        a.getColor(
            R.styleable.SucFooterBarMixin_sucFooterBarSecondaryFooterButtonDisabledTextColor, 0);
    footerBarButtonMiddleSpacing =
        a.getDimensionPixelSize(R.styleable.SucFooterBarMixin_sucFooterBarButtonMiddleSpacing, 0);

    int primaryBtn =
        a.getResourceId(R.styleable.SucFooterBarMixin_sucFooterBarPrimaryFooterButton, 0);
    int secondaryBtn =
        a.getResourceId(R.styleable.SucFooterBarMixin_sucFooterBarSecondaryFooterButton, 0);
    a.recycle();

    FooterButtonInflater inflater = new FooterButtonInflater(context);

    if (secondaryBtn != 0) {
      setSecondaryButton(inflater.inflate(secondaryBtn));
      metrics.logPrimaryButtonInitialStateVisibility(/* isVisible= */ true, /* isUsingXml= */ true);
    }

    if (primaryBtn != 0) {
      setPrimaryButton(inflater.inflate(primaryBtn));
      metrics.logSecondaryButtonInitialStateVisibility(
          /* isVisible= */ true, /* isUsingXml= */ true);
    }
  }

  public void setLoggingObserver(LoggingObserver observer) {
    loggingObserver = observer;

    // If primary button is already created, it's likely that {@code setPrimaryButton()} was called
    // before an {@link LoggingObserver} is set, we need to set an observer and call the right
    // logging method here.
    if (primaryButtonId != 0) {
      loggingObserver.log(
          new ButtonInflatedEvent(getPrimaryButtonView(), LoggingObserver.ButtonType.PRIMARY));
      getPrimaryButton().setLoggingObserver(observer);
    }
    // Same for secondary button.
    if (secondaryButtonId != 0) {
      loggingObserver.log(
          new ButtonInflatedEvent(getSecondaryButtonView(), LoggingObserver.ButtonType.SECONDARY));
      getSecondaryButton().setLoggingObserver(observer);
    }
  }

  protected boolean isFooterButtonAlignedEnd() {
    if (PartnerConfigHelper.get(context)
        .isPartnerConfigAvailable(PartnerConfig.CONFIG_FOOTER_BUTTON_ALIGNED_END)) {
      return PartnerConfigHelper.get(context)
          .getBoolean(context, PartnerConfig.CONFIG_FOOTER_BUTTON_ALIGNED_END, false);
    } else {
      return footerButtonAlignEnd;
    }
  }

  protected boolean isFooterButtonsEvenlyWeighted() {
    if (!isSecondaryButtonInPrimaryStyle) {
      return false;
    }
    PartnerConfigHelper.get(context);
    return PartnerConfigHelper.isNeutralButtonStyleEnabled(context);
  }

  private View addSpace() {
    LinearLayout buttonContainerLayout = ensureFooterInflated();
    View space = new View(context);
    space.setLayoutParams(new LayoutParams(0, 0, 1.0f));
    space.setVisibility(View.INVISIBLE);
    buttonContainerLayout.addView(space);
    return space;
  }

  @NonNull
  private LinearLayout ensureFooterInflated() {
    if (buttonContainer == null) {
      if (footerStub == null) {
        throw new IllegalStateException("Footer stub is not found in this template");
      }
      buttonContainer = (LinearLayout) inflateFooter(R.layout.suc_footer_button_bar);
      onFooterBarInflated(buttonContainer);
      onFooterBarApplyPartnerResource(buttonContainer);
    }
    return buttonContainer;
  }

  /**
   * Notifies that the footer bar has been inflated to the view hierarchy. Calling super is
   * necessary while subclass implement it.
   */
  @CallSuper
  protected void onFooterBarInflated(LinearLayout buttonContainer) {
    if (buttonContainer == null) {
      // Ignore action since buttonContainer is null
      return;
    }
    buttonContainer.setId(View.generateViewId());
    updateFooterBarPadding(
        buttonContainer,
        footerBarPaddingStart,
        footerBarPaddingTop,
        footerBarPaddingEnd,
        footerBarPaddingBottom);
    if (isFooterButtonAlignedEnd()) {
      buttonContainer.setGravity(Gravity.END | Gravity.CENTER_VERTICAL);
    }
  }

  /**
   * Notifies while the footer bar apply Partner Resource. Calling super is necessary while subclass
   * implement it.
   */
  @CallSuper
  protected void onFooterBarApplyPartnerResource(LinearLayout buttonContainer) {
    if (buttonContainer == null) {
      // Ignore action since buttonContainer is null
      return;
    }
    if (!applyPartnerResources) {
      return;
    }

    // skip apply partner resources on footerbar background if dynamic color enabled
    if (!useFullDynamicColor) {
      @ColorInt
      int color =
          PartnerConfigHelper.get(context)
              .getColor(context, PartnerConfig.CONFIG_FOOTER_BAR_BG_COLOR);
      buttonContainer.setBackgroundColor(color);
    }

    if (PartnerConfigHelper.get(context)
        .isPartnerConfigAvailable(PartnerConfig.CONFIG_FOOTER_BUTTON_PADDING_TOP)) {
      footerBarPaddingTop =
          (int)
              PartnerConfigHelper.get(context)
                  .getDimension(context, PartnerConfig.CONFIG_FOOTER_BUTTON_PADDING_TOP);
    }
    if (PartnerConfigHelper.get(context)
        .isPartnerConfigAvailable(PartnerConfig.CONFIG_FOOTER_BUTTON_PADDING_BOTTOM)) {
      footerBarPaddingBottom =
          (int)
              PartnerConfigHelper.get(context)
                  .getDimension(context, PartnerConfig.CONFIG_FOOTER_BUTTON_PADDING_BOTTOM);
    }
    if (PartnerConfigHelper.get(context)
        .isPartnerConfigAvailable(PartnerConfig.CONFIG_FOOTER_BAR_PADDING_START)) {
      footerBarPaddingStart =
          (int)
              PartnerConfigHelper.get(context)
                  .getDimension(context, PartnerConfig.CONFIG_FOOTER_BAR_PADDING_START);
    }
    if (PartnerConfigHelper.get(context)
        .isPartnerConfigAvailable(PartnerConfig.CONFIG_FOOTER_BAR_PADDING_END)) {
      footerBarPaddingEnd =
          (int)
              PartnerConfigHelper.get(context)
                  .getDimension(context, PartnerConfig.CONFIG_FOOTER_BAR_PADDING_END);
    }
    updateFooterBarPadding(
        buttonContainer,
        footerBarPaddingStart,
        footerBarPaddingTop,
        footerBarPaddingEnd,
        footerBarPaddingBottom);

    if (PartnerConfigHelper.get(context)
        .isPartnerConfigAvailable(PartnerConfig.CONFIG_FOOTER_BAR_MIN_HEIGHT)) {
      int minHeight =
          (int)
              PartnerConfigHelper.get(context)
                  .getDimension(context, PartnerConfig.CONFIG_FOOTER_BAR_MIN_HEIGHT);
      if (minHeight > 0) {
        buttonContainer.setMinimumHeight(minHeight);
      }
    }
  }

  /**
   * Inflate IFooterActionButton with layout "suc_button". Subclasses can implement this method to
   * modify the footer button layout as necessary.
   */
  @SuppressLint("InflateParams")
  protected IFooterActionButton createThemedButton(Context context, @StyleRes int theme) {
    if (PartnerConfigHelper.isGlifExpressiveEnabled(context)) {
      try {
        if (theme == R.style.SucGlifMaterialButton_Primary) {
          return new MaterialFooterActionButton(
              new ContextThemeWrapper(context, theme), null, R.attr.sucMaterialButtonStyle);
        } else {
          return new MaterialFooterActionButton(
              new ContextThemeWrapper(context, theme), null, R.attr.sucMaterialOutlinedButtonStyle);
        }
      } catch (IllegalArgumentException e) {
        LOG.e("Applyed invalid material theme: " + e);
        // fallback theme style to glif theme
        if (theme == R.style.SucGlifMaterialButton_Primary) {
          theme = R.style.SucPartnerCustomizationButton_Primary;
        } else {
          theme = R.style.SucPartnerCustomizationButton_Secondary;
        }
      }
    }
    // Inflate a single button from XML, which when using support lib, will take advantage of
    // the injected layout inflater and give us AppCompatButton instead.
    LayoutInflater inflater = LayoutInflater.from(new ContextThemeWrapper(context, theme));
    return (IFooterActionButton) inflater.inflate(R.layout.suc_button, null, false);
  }

  /** Sets primary button for footer. */
  @MainThread
  public void setPrimaryButton(FooterButton footerButton) {
    ensureOnMainThread("setPrimaryButton");
    ensureFooterInflated();

    int defaultPartnerTheme;
    if (PartnerConfigHelper.isGlifExpressiveEnabled(context)) {
      defaultPartnerTheme = R.style.SucGlifMaterialButton_Primary;
    } else {
      defaultPartnerTheme = R.style.SucPartnerCustomizationButton_Primary;
    }

    // TODO: b/364980746 - Use partner config to allow user to customize primary bg color.
    // Setup button partner config
    FooterButtonPartnerConfig footerButtonPartnerConfig =
        new FooterButtonPartnerConfig.Builder(footerButton)
            .setPartnerTheme(
                getPartnerTheme(
                    footerButton,
                    /* defaultPartnerTheme= */ defaultPartnerTheme,
                    /* buttonBackgroundColorConfig= */ PartnerConfig
                        .CONFIG_FOOTER_PRIMARY_BUTTON_BG_COLOR))
            .setButtonBackgroundConfig(PartnerConfig.CONFIG_FOOTER_PRIMARY_BUTTON_BG_COLOR)
            .setButtonDisableAlphaConfig(PartnerConfig.CONFIG_FOOTER_BUTTON_DISABLED_ALPHA)
            .setButtonDisableBackgroundConfig(PartnerConfig.CONFIG_FOOTER_BUTTON_DISABLED_BG_COLOR)
            .setButtonDisableTextColorConfig(
                PartnerConfig.CONFIG_FOOTER_PRIMARY_BUTTON_DISABLED_TEXT_COLOR)
            .setButtonIconConfig(getDrawablePartnerConfig(footerButton.getButtonType()))
            .setButtonRadiusConfig(PartnerConfig.CONFIG_FOOTER_BUTTON_RADIUS)
            .setButtonRippleColorAlphaConfig(PartnerConfig.CONFIG_FOOTER_BUTTON_RIPPLE_COLOR_ALPHA)
            .setTextColorConfig(PartnerConfig.CONFIG_FOOTER_PRIMARY_BUTTON_TEXT_COLOR)
            .setMarginStartConfig(PartnerConfig.CONFIG_FOOTER_PRIMARY_BUTTON_MARGIN_START)
            .setTextSizeConfig(PartnerConfig.CONFIG_FOOTER_BUTTON_TEXT_SIZE)
            .setButtonMinHeight(PartnerConfig.CONFIG_FOOTER_BUTTON_MIN_HEIGHT)
            .setTextTypeFaceConfig(PartnerConfig.CONFIG_FOOTER_BUTTON_FONT_FAMILY)
            .setTextWeightConfig(PartnerConfig.CONFIG_FOOTER_BUTTON_FONT_WEIGHT)
            .setTextStyleConfig(PartnerConfig.CONFIG_FOOTER_BUTTON_TEXT_STYLE)
            .build();

    IFooterActionButton buttonImpl = inflateButton(footerButton, footerButtonPartnerConfig);
    // update information for primary button. Need to update as long as the button inflated.
    Button button = (Button) buttonImpl;
    primaryButtonId = button.getId();
    if (buttonImpl instanceof MaterialFooterActionButton) {
      ((MaterialFooterActionButton) buttonImpl)
          .setPrimaryButtonStyle(/* isPrimaryButtonStyle= */ true);
    } else if (button instanceof FooterActionButton) {
      ((FooterActionButton) buttonImpl).setPrimaryButtonStyle(/* isPrimaryButtonStyle= */ true);
    } else {
      LOG.e("Set the primary button style error when setting primary button.");
    }
    primaryButton = footerButton;
    primaryButtonPartnerConfigForTesting = footerButtonPartnerConfig;
    onFooterButtonInflated(button, footerBarPrimaryBackgroundColor);
    onFooterButtonApplyPartnerResource(button, footerButtonPartnerConfig);
    // TODO: b/364981299 - Use partner config to allow user to customize text color.
    if (PartnerConfigHelper.isGlifExpressiveEnabled(context)) {
      boolean enabled = primaryButton.isEnabled();
      updateTextColorForButton(
          button,
          enabled,
          enabled
              ? footerBarPrimaryButtonEnabledTextColor
              : footerBarPrimaryButtonDisabledTextColor);
    }
    if (loggingObserver != null) {
      loggingObserver.log(
          new ButtonInflatedEvent(getPrimaryButtonView(), LoggingObserver.ButtonType.PRIMARY));
      footerButton.setLoggingObserver(loggingObserver);
    }

    // Make sure the position of buttons are correctly and prevent primary button create twice or
    // more.
    repopulateButtons();

    // The requestFocus() is only working after activity onResume.
    button.post(
        () -> {
          if (KeyboardHelper.isKeyboardFocusEnhancementEnabled(context)
              && KeyboardHelper.hasHardwareKeyboard(context)) {
            button.requestFocus();
          }
        });
  }

  /** Returns the {@link FooterButton} of primary button. */
  public FooterButton getPrimaryButton() {
    return primaryButton;
  }

  /**
   * Returns the {@link Button} of primary button.
   *
   * @apiNote It is not recommended to apply style to the view directly. The setup library will
   *     handle the button style. There is no guarantee that changes made directly to the button
   *     style will not cause unexpected behavior.
   */
  public Button getPrimaryButtonView() {
    return buttonContainer == null ? null : buttonContainer.findViewById(primaryButtonId);
  }

  @VisibleForTesting
  boolean isPrimaryButtonVisible() {
    return getPrimaryButtonView() != null && getPrimaryButtonView().getVisibility() == View.VISIBLE;
  }

  /** Sets secondary button for footer. */
  @MainThread
  public void setSecondaryButton(FooterButton footerButton) {
    setSecondaryButton(footerButton, /* usePrimaryStyle= */ false);
  }

  /** Sets secondary button for footer. Allow to use the primary button style. */
  @MainThread
  public void setSecondaryButton(FooterButton footerButton, boolean usePrimaryStyle) {
    ensureOnMainThread("setSecondaryButton");
    isSecondaryButtonInPrimaryStyle = usePrimaryStyle;
    ensureFooterInflated();

    int defaultPartnerTheme;
    if (PartnerConfigHelper.isGlifExpressiveEnabled(context)) {
      defaultPartnerTheme =
          usePrimaryStyle
              ? R.style.SucGlifMaterialButton_Primary
              : R.style.SucGlifMaterialButton_Secondary;
    } else {
      defaultPartnerTheme =
          usePrimaryStyle
              ? R.style.SucPartnerCustomizationButton_Primary
              : R.style.SucPartnerCustomizationButton_Secondary;
    }

    // Setup button partner config
    FooterButtonPartnerConfig footerButtonPartnerConfig =
        new FooterButtonPartnerConfig.Builder(footerButton)
            .setPartnerTheme(
                getPartnerTheme(
                    footerButton,
                    /* defaultPartnerTheme= */ defaultPartnerTheme,
                    /* buttonBackgroundColorConfig= */ usePrimaryStyle
                        ? PartnerConfig.CONFIG_FOOTER_PRIMARY_BUTTON_BG_COLOR
                        : PartnerConfig.CONFIG_FOOTER_SECONDARY_BUTTON_BG_COLOR))
            .setButtonBackgroundConfig(
                usePrimaryStyle
                    ? PartnerConfig.CONFIG_FOOTER_PRIMARY_BUTTON_BG_COLOR
                    : PartnerConfig.CONFIG_FOOTER_SECONDARY_BUTTON_BG_COLOR)
            .setButtonDisableAlphaConfig(PartnerConfig.CONFIG_FOOTER_BUTTON_DISABLED_ALPHA)
            .setButtonDisableBackgroundConfig(PartnerConfig.CONFIG_FOOTER_BUTTON_DISABLED_BG_COLOR)
            .setButtonDisableTextColorConfig(
                usePrimaryStyle
                    ? PartnerConfig.CONFIG_FOOTER_PRIMARY_BUTTON_DISABLED_TEXT_COLOR
                    : PartnerConfig.CONFIG_FOOTER_SECONDARY_BUTTON_DISABLED_TEXT_COLOR)
            .setButtonIconConfig(getDrawablePartnerConfig(footerButton.getButtonType()))
            .setButtonRadiusConfig(PartnerConfig.CONFIG_FOOTER_BUTTON_RADIUS)
            .setButtonRippleColorAlphaConfig(PartnerConfig.CONFIG_FOOTER_BUTTON_RIPPLE_COLOR_ALPHA)
            .setTextColorConfig(
                usePrimaryStyle
                    ? PartnerConfig.CONFIG_FOOTER_PRIMARY_BUTTON_TEXT_COLOR
                    : PartnerConfig.CONFIG_FOOTER_SECONDARY_BUTTON_TEXT_COLOR)
            .setMarginStartConfig(PartnerConfig.CONFIG_FOOTER_SECONDARY_BUTTON_MARGIN_START)
            .setTextSizeConfig(PartnerConfig.CONFIG_FOOTER_BUTTON_TEXT_SIZE)
            .setButtonMinHeight(PartnerConfig.CONFIG_FOOTER_BUTTON_MIN_HEIGHT)
            .setTextTypeFaceConfig(PartnerConfig.CONFIG_FOOTER_BUTTON_FONT_FAMILY)
            .setTextWeightConfig(PartnerConfig.CONFIG_FOOTER_BUTTON_FONT_WEIGHT)
            .setTextStyleConfig(PartnerConfig.CONFIG_FOOTER_BUTTON_TEXT_STYLE)
            .build();

    IFooterActionButton buttonImpl = inflateButton(footerButton, footerButtonPartnerConfig);
    // update information for secondary button. Need to update as long as the button inflated.
    Button button = (Button) buttonImpl;
    secondaryButtonId = button.getId();
    if (buttonImpl instanceof MaterialFooterActionButton) {
      ((MaterialFooterActionButton) buttonImpl).setPrimaryButtonStyle(usePrimaryStyle);
    } else if (button instanceof FooterActionButton) {
      ((FooterActionButton) buttonImpl).setPrimaryButtonStyle(usePrimaryStyle);
    } else {
      LOG.e("Set the primary button style error when setting secondary button.");
    }
    secondaryButton = footerButton;
    secondaryButtonPartnerConfigForTesting = footerButtonPartnerConfig;

    onFooterButtonInflated(button, footerBarSecondaryBackgroundColor);
    onFooterButtonApplyPartnerResource(button, footerButtonPartnerConfig);
    // TODO: b/364981299 - Use partner config to allow user to customize text color.
    if (PartnerConfigHelper.isGlifExpressiveEnabled(context)) {
      boolean enabled = secondaryButton.isEnabled();
      updateTextColorForButton(
          button,
          enabled,
          enabled
              ? footerBarSecondaryButtonEnabledTextColor
              : footerBarSecondaryButtonDisabledTextColor);
    }
    if (loggingObserver != null) {
      loggingObserver.log(new ButtonInflatedEvent(button, LoggingObserver.ButtonType.SECONDARY));
      footerButton.setLoggingObserver(loggingObserver);
    }

    // Make sure the position of buttons are correctly and prevent secondary button create twice or
    // more.
    repopulateButtons();

    // The requestFocus() is only working after activity onResume.
    button.post(
        () -> {
          if (KeyboardHelper.isKeyboardFocusEnhancementEnabled(context)
              && KeyboardHelper.hasHardwareKeyboard(context)
              && (primaryButtonId == 0
                  // primary button may not be visible but it has been created
                  || getPrimaryButtonView().getVisibility() != View.VISIBLE)) {
            button.requestFocus();
          }
        });
  }

  /**
   * Corrects the order of footer buttons after the button has been inflated to the view hierarchy.
   * Subclasses can implement this method to modify the order of footer buttons as necessary.
   */
  protected void repopulateButtons() {
    LinearLayout buttonContainer = ensureFooterInflated();
    Button tempPrimaryButton = getPrimaryButtonView();
    Button tempSecondaryButton = getSecondaryButtonView();
    buttonContainer.removeAllViews();

    boolean isEvenlyWeightedButtons = isFooterButtonsEvenlyWeighted();
    boolean isLandscape =
        context.getResources().getConfiguration().orientation
            == Configuration.ORIENTATION_LANDSCAPE;
    if (isLandscape
        && isEvenlyWeightedButtons
        && isFooterButtonAlignedEnd()
        && !PartnerConfigHelper.isGlifExpressiveEnabled(context)) {
      addSpace();
    }

    if (tempSecondaryButton != null) {
      if (isSecondaryButtonInPrimaryStyle) {
        // Since the secondary button has the same style (with background) as the primary button,
        // we need to have the left padding equal to the right padding.
        updateFooterBarPadding(
            buttonContainer,
            buttonContainer.getPaddingRight(),
            buttonContainer.getPaddingTop(),
            buttonContainer.getPaddingRight(),
            buttonContainer.getPaddingBottom());
      }
      buttonContainer.addView(tempSecondaryButton);
    }
    if (!isFooterButtonAlignedEnd() && !PartnerConfigHelper.isGlifExpressiveEnabled(context)) {
      addSpace();
    }
    if (tempPrimaryButton != null) {
      buttonContainer.addView(tempPrimaryButton);
    }

    setEvenlyWeightedButtons(tempPrimaryButton, tempSecondaryButton, isEvenlyWeightedButtons);

    if (PartnerConfigHelper.isGlifExpressiveEnabled(context)) {
      setButtonWidthForExpressiveStyle(/* isDownButton= */ false);
    }
  }

  private void setEvenlyWeightedButtons(
      Button primaryButton, Button secondaryButton, boolean isEvenlyWeighted) {
    if (primaryButton != null && secondaryButton != null && isEvenlyWeighted) {
      primaryButton.measure(View.MeasureSpec.UNSPECIFIED, View.MeasureSpec.UNSPECIFIED);
      int primaryButtonMeasuredWidth = primaryButton.getMeasuredWidth();
      secondaryButton.measure(View.MeasureSpec.UNSPECIFIED, View.MeasureSpec.UNSPECIFIED);

      int secondaryButtonMeasuredWidth = secondaryButton.getMeasuredWidth();
      int maxButtonMeasureWidth = max(primaryButtonMeasuredWidth, secondaryButtonMeasuredWidth);

      primaryButton.getLayoutParams().width = maxButtonMeasureWidth;
      secondaryButton.getLayoutParams().width = maxButtonMeasureWidth;
    } else {
      if (primaryButton != null) {
        LinearLayout.LayoutParams primaryLayoutParams =
            (LinearLayout.LayoutParams) primaryButton.getLayoutParams();
        if (primaryLayoutParams != null) {
          primaryLayoutParams.width = ViewGroup.LayoutParams.WRAP_CONTENT;
          primaryLayoutParams.weight = 0;
          primaryButton.setLayoutParams(primaryLayoutParams);
        }
      }
      if (secondaryButton != null) {
        LinearLayout.LayoutParams secondaryLayoutParams =
            (LinearLayout.LayoutParams) secondaryButton.getLayoutParams();
        if (secondaryLayoutParams != null) {
          secondaryLayoutParams.width = ViewGroup.LayoutParams.WRAP_CONTENT;
          secondaryLayoutParams.weight = 0;
          secondaryButton.setLayoutParams(secondaryLayoutParams);
        }
      }
    }
  }

  // TODO: b/369285240 - Migrate setButtonWidthForExpressiveStyle of FooterBarMixin to
  /** Sets button width for expressive style. */
  public void setButtonWidthForExpressiveStyle(boolean isDownButton) {
    final ViewTreeObserver.OnGlobalLayoutListener onGlobalLayoutListener =
        new OnGlobalLayoutListener() {
          @Override
          public void onGlobalLayout() {
            int initialLeftMargin = 0;
            if (!isDownButton) {
              Button primaryButton = getPrimaryButtonView();
              Button secondaryButton = getSecondaryButtonView();
              DisplayMetrics displayMetrics = context.getResources().getDisplayMetrics();
              int screenWidth = displayMetrics.widthPixels;
              if (isTwoPaneLayout()) {
                screenWidth = screenWidth / 2;
                if (primaryButton != null) {
                  // Set back the margin once down button scrolling to the bottom.
                  LinearLayout.LayoutParams primaryLayoutParams =
                      ((LayoutParams) primaryButton.getLayoutParams());
                  if (primaryLayoutParams.leftMargin != initialLeftMargin) {
                    primaryLayoutParams.leftMargin = initialLeftMargin;
                    primaryButton.setLayoutParams(primaryLayoutParams);
                  }
                }
                buttonContainer.setGravity(Gravity.END);
              }

              // TODO: b/364981820 - Use partner config to allow user to customize button width.
              int availableFooterBarWidth =
                  screenWidth
                      - footerBarPaddingStart
                      - footerBarPaddingEnd
                      - footerBarButtonMiddleSpacing;
              int maxButtonWidth = availableFooterBarWidth / 2;
              if (isBothButtons(primaryButton, secondaryButton)) {
                LayoutParams primaryLayoutParams = (LayoutParams) primaryButton.getLayoutParams();
                LayoutParams secondaryLayoutParams =
                    (LayoutParams) secondaryButton.getLayoutParams();

                boolean isPrimaryTextTooLong = isTextTooLong(primaryButton, maxButtonWidth);
                boolean isSecondaryTextTooLong = isTextTooLong(secondaryButton, maxButtonWidth);

                if (isPrimaryTextTooLong || isSecondaryTextTooLong) {
                  if (buttonContainer instanceof ButtonBarLayout) {
                    ((ButtonBarLayout) buttonContainer).setStackedButtonForExpressiveStyle(true);
                  }
                  int stackButtonMiddleSpacing = footerBarButtonMiddleSpacing / 2;
                  primaryLayoutParams.width = availableFooterBarWidth;
                  primaryLayoutParams.bottomMargin = stackButtonMiddleSpacing;
                  primaryButton.setLayoutParams(primaryLayoutParams);

                  secondaryLayoutParams.width = availableFooterBarWidth;
                  secondaryLayoutParams.topMargin = stackButtonMiddleSpacing;
                  secondaryButton.setLayoutParams(secondaryLayoutParams);
                } else {
                  if (primaryLayoutParams != null) {
                    primaryLayoutParams.width = maxButtonWidth;
                    primaryLayoutParams.setMarginStart(footerBarButtonMiddleSpacing / 2);
                    primaryButton.setLayoutParams(primaryLayoutParams);
                  }
                  if (secondaryLayoutParams != null) {
                    secondaryLayoutParams.width = maxButtonWidth;
                    secondaryLayoutParams.setMarginEnd(footerBarButtonMiddleSpacing / 2);
                    secondaryButton.setLayoutParams(secondaryLayoutParams);
                  }
                }
              } else if (isPrimaryButtonOnly(primaryButton, secondaryButton)) {
                LayoutParams primaryLayoutParams = (LayoutParams) primaryButton.getLayoutParams();
                if (primaryLayoutParams != null) {
                  primaryLayoutParams.width = availableFooterBarWidth;
                  primaryButton.setLayoutParams(primaryLayoutParams);
                }
              } else if (isSecondaryOnly(primaryButton, secondaryButton)) {
                LayoutParams secondaryLayoutParams =
                    (LayoutParams) secondaryButton.getLayoutParams();
                if (secondaryLayoutParams != null) {
                  secondaryLayoutParams.width = availableFooterBarWidth;
                  secondaryButton.setLayoutParams(secondaryLayoutParams);
                }
              } else {
                LOG.atInfo("There are no button visible in the footer bar.");
              }
            } else {
              // Only allow primary button been shown on the screen if in the down button style.
              if (getSecondaryButtonView() != null) {
                getSecondaryButtonView().setVisibility(View.GONE);
              }
              setDownButtonStyle(getPrimaryButtonView());
              if (!isTwoPaneLayout()) {
                buttonContainer.setGravity(Gravity.CENTER_HORIZONTAL | Gravity.CENTER_VERTICAL);
              } else {
                buttonContainer.setGravity(Gravity.CENTER_VERTICAL);
                int containerWidth = buttonContainer.getWidth();
                Button downButtonView = getPrimaryButtonView();
                LayoutParams primaryLayoutParams = (LayoutParams) downButtonView.getLayoutParams();
                int halfContainerWidth = containerWidth / 2;
                // Put down button to the center of the one side in two pane mode.
                primaryLayoutParams.setMarginStart(
                    (halfContainerWidth
                        + (halfContainerWidth / 2 - downButtonView.getWidth() / 2)));
                downButtonView.setLayoutParams(primaryLayoutParams);
              }
            }
            buttonContainer.getViewTreeObserver().removeOnGlobalLayoutListener(this);
          }
        };

    buttonContainer.getViewTreeObserver().addOnGlobalLayoutListener(onGlobalLayoutListener);
  }

  // TODO: b/376153500 - Add a test case for button stack mechanism.
  private boolean isTextTooLong(Button button, float maxButtonWidth) {
    String text = button.getText().toString();
    TextPaint textPaint = button.getPaint();

    int buttonWidth = (int) maxButtonWidth - button.getPaddingLeft() - button.getPaddingRight();

    // Generate a static layout to see if text requires switching lines.
    StaticLayout staticLayout =
        new StaticLayout(
            text,
            textPaint,
            buttonWidth,
            Alignment.ALIGN_CENTER,
            /* spacingMult= */ 1.0f,
            /* spacingAdd= */ 0.0f,
            /* includePad= */ false);
    return staticLayout.getLineCount() > 1;
  }

  private boolean isTwoPaneLayout() {
    return context.getResources().getBoolean(R.bool.sucTwoPaneLayoutStyle);
  }

  private boolean isBothButtons(Button primaryButton, Button secondaryButton) {
    boolean isPrimaryVisible =
        primaryButton != null && primaryButton.getVisibility() == View.VISIBLE;
    boolean isSecondaryVisible =
        secondaryButton != null && secondaryButton.getVisibility() == View.VISIBLE;
    LOG.atDebug(
        "isPrimaryVisible=" + isPrimaryVisible + ", isSecondaryVisible=" + isSecondaryVisible);
    return isPrimaryVisible && isSecondaryVisible;
  }

  private boolean isPrimaryButtonOnly(Button primaryButton, Button secondaryButton) {
    boolean isPrimaryOnly = primaryButton != null && secondaryButton == null;
    boolean isPrimaryOnlyButSecondaryInvisible =
        (primaryButton != null)
            && (secondaryButton != null && secondaryButton.getVisibility() != View.VISIBLE);
    LOG.atDebug(
        "isPrimaryOnly="
            + isPrimaryOnly
            + ", isPrimaryOnlyButSecondaryInvisible="
            + isPrimaryOnlyButSecondaryInvisible);
    return isPrimaryOnly || isPrimaryOnlyButSecondaryInvisible;
  }

  private boolean isSecondaryOnly(Button primaryButton, Button secondaryButton) {
    boolean isSecondaryOnly = secondaryButton != null && primaryButton == null;
    boolean isSecondaryOnlyButPrimaryInvisible =
        (secondaryButton != null)
            && (primaryButton != null && primaryButton.getVisibility() != View.VISIBLE);
    LOG.atDebug(
        "isSecondaryOnly="
            + isSecondaryOnly
            + ", isSecondaryOnlyButPrimaryInvisible="
            + isSecondaryOnlyButPrimaryInvisible);
    return isSecondaryOnly || isSecondaryOnlyButPrimaryInvisible;
  }

  private void setDownButtonStyle(Button button) {
    // TODO: b/364121308 - Extract values as attributes.
    int width =
        context.getResources().getDimensionPixelSize(R.dimen.suc_glif_expressive_down_button_width);
    int height =
        context
            .getResources()
            .getDimensionPixelSize(R.dimen.suc_glif_expressive_down_button_height);

    if (button != null) {
      LinearLayout.LayoutParams layoutParams = (LinearLayout.LayoutParams) button.getLayoutParams();
      layoutParams.width = width;
      layoutParams.height = height;
      button.setLayoutParams(layoutParams);
    }
    setDownButtonRadius(button);
  }

  private void setDownButtonRadius(Button button) {
    float radius =
        context.getResources().getDimension(R.dimen.suc_glif_expressive_down_button_radius);
    if (button != null) {
      if (button instanceof MaterialButton) {
        ((MaterialButton) button).setCornerRadius((int) radius);
      } else {
        GradientDrawable gradientDrawable = FooterButtonStyleUtils.getGradientDrawable(button);
        if (gradientDrawable != null) {
          gradientDrawable.setCornerRadius(radius);
        }
      }
    }
  }

  /**
   * Notifies that the footer button has been inInflated and add to the view hierarchy. Calling
   * super is necessary while subclass implement it.
   */
  @CallSuper
  protected void onFooterButtonInflated(Button button, @ColorInt int defaultButtonBackgroundColor) {
    // Try to set default background
    if (!applyDynamicColor) {
      if (defaultButtonBackgroundColor != 0) {
        FooterButtonStyleUtils.updateButtonBackground(button, defaultButtonBackgroundColor);
      } else {
        // TODO: get button background color from activity theme
      }
    }
    buttonContainer.addView(button);
    autoSetButtonBarVisibility();
  }

  private int getPartnerTheme(
      FooterButton footerButton,
      int defaultPartnerTheme,
      PartnerConfig buttonBackgroundColorConfig) {
    int overrideTheme = footerButton.getTheme();

    // Set the default theme if theme is not set, or when running in setup flow.
    if (footerButton.getTheme() == 0
        || applyPartnerResources
        || PartnerConfigHelper.isGlifExpressiveEnabled(context)) {
      overrideTheme = defaultPartnerTheme;
    }
    // TODO: Make sure customize attributes in theme can be applied during setup flow.
    // If sets background color to full transparent, the button changes to colored borderless ink
    // button style.
    if (applyPartnerResources) {
      int color = PartnerConfigHelper.get(context).getColor(context, buttonBackgroundColorConfig);
      if (color == Color.TRANSPARENT) {
        overrideTheme =
            PartnerConfigHelper.isGlifExpressiveEnabled(context)
                ? R.style.SucGlifMaterialButton_Secondary
                : R.style.SucPartnerCustomizationButton_Secondary;
      } else {
        overrideTheme =
            PartnerConfigHelper.isGlifExpressiveEnabled(context)
                ? R.style.SucGlifMaterialButton_Primary
                : R.style.SucPartnerCustomizationButton_Primary;
      }
    }
    return overrideTheme;
  }

  /** Returns the {@link LinearLayout} of button container. */
  public LinearLayout getButtonContainer() {
    return buttonContainer;
  }

  /** Returns the {@link FooterButton} of secondary button. */
  public FooterButton getSecondaryButton() {
    return secondaryButton;
  }

  /**
   * Sets whether the footer bar should be removed when there are no footer buttons in the bar.
   *
   * @param value True if footer bar is gone, false otherwise.
   */
  public void setRemoveFooterBarWhenEmpty(boolean value) {
    removeFooterBarWhenEmpty = value;
    autoSetButtonBarVisibility();
  }

  /**
   * Checks the visibility state of footer buttons to set the visibility state of this footer bar
   * automatically.
   */
  private void autoSetButtonBarVisibility() {
    Button primaryButton = getPrimaryButtonView();
    Button secondaryButton = getSecondaryButtonView();
    boolean primaryVisible = primaryButton != null && primaryButton.getVisibility() == View.VISIBLE;
    boolean secondaryVisible =
        secondaryButton != null && secondaryButton.getVisibility() == View.VISIBLE;

    if (buttonContainer != null) {
      buttonContainer.setVisibility(
          primaryVisible || secondaryVisible
              ? View.VISIBLE
              : removeFooterBarWhenEmpty ? View.GONE : View.INVISIBLE);
    }
  }

  /** Returns the visibility status for this footer bar. */
  @VisibleForTesting
  public int getVisibility() {
    return buttonContainer.getVisibility();
  }

  /**
   * Returns the {@link Button} of secondary button.
   *
   * @apiNote It is not recommended to apply style to the view directly. The setup library will
   *     handle the button style. There is no guarantee that changes made directly to the button
   *     style will not cause unexpected behavior.
   */
  public Button getSecondaryButtonView() {
    return buttonContainer == null ? null : buttonContainer.findViewById(secondaryButtonId);
  }

  @VisibleForTesting
  boolean isSecondaryButtonVisible() {
    return getSecondaryButtonView() != null
        && getSecondaryButtonView().getVisibility() == View.VISIBLE;
  }

  private IFooterActionButton inflateButton(
      FooterButton footerButton, FooterButtonPartnerConfig footerButtonPartnerConfig) {
    IFooterActionButton buttonImpl =
        createThemedButton(context, footerButtonPartnerConfig.getPartnerTheme());
    Button button = (Button) buttonImpl;
    button.setId(View.generateViewId());

    // apply initial configuration into button view.
    button.setText(footerButton.getText());
    button.setOnClickListener(footerButton);
    button.setVisibility(footerButton.getVisibility());
    button.setEnabled(footerButton.isEnabled());
    if (buttonImpl instanceof MaterialFooterActionButton) {
      ((MaterialFooterActionButton) buttonImpl).setFooterButton(footerButton);
    } else if (button instanceof FooterActionButton) {
      ((FooterActionButton) buttonImpl).setFooterButton(footerButton);
    } else {
      LOG.e("Set the footer button error!");
    }
    footerButton.setOnButtonEventListener(createButtonEventListener(button.getId()));
    return buttonImpl;
  }

  // TODO: Make sure customize attributes in theme can be applied during setup flow.
  @TargetApi(VERSION_CODES.Q)
  private void onFooterButtonApplyPartnerResource(
      Button button, FooterButtonPartnerConfig footerButtonPartnerConfig) {
    if (!applyPartnerResources) {
      return;
    }
    FooterButtonStyleUtils.applyButtonPartnerResources(
        context,
        button,
        applyDynamicColor,
        /* isButtonIconAtEnd= */ (button.getId() == primaryButtonId),
        footerButtonPartnerConfig);
    if (!applyDynamicColor) {
      // adjust text color based on enabled state
      updateButtonTextColorWithStates(
          button,
          footerButtonPartnerConfig.getButtonTextColorConfig(),
          footerButtonPartnerConfig.getButtonDisableTextColorConfig());
    }
  }

  private void updateButtonTextColorWithStates(
      Button button,
      PartnerConfig buttonTextColorConfig,
      PartnerConfig buttonTextDisabledColorConfig) {
    if (button.isEnabled()) {
      FooterButtonStyleUtils.updateButtonTextEnabledColorWithPartnerConfig(
          context, button, buttonTextColorConfig);
    } else {
      FooterButtonStyleUtils.updateButtonTextDisabledColorWithPartnerConfig(
          context, button, buttonTextDisabledColorConfig);
    }
  }

  private static PartnerConfig getDrawablePartnerConfig(@ButtonType int buttonType) {
    PartnerConfig result;
    switch (buttonType) {
      case ButtonType.ADD_ANOTHER:
        result = PartnerConfig.CONFIG_FOOTER_BUTTON_ICON_ADD_ANOTHER;
        break;
      case ButtonType.CANCEL:
        result = PartnerConfig.CONFIG_FOOTER_BUTTON_ICON_CANCEL;
        break;
      case ButtonType.CLEAR:
        result = PartnerConfig.CONFIG_FOOTER_BUTTON_ICON_CLEAR;
        break;
      case ButtonType.DONE:
        result = PartnerConfig.CONFIG_FOOTER_BUTTON_ICON_DONE;
        break;
      case ButtonType.NEXT:
        result = PartnerConfig.CONFIG_FOOTER_BUTTON_ICON_NEXT;
        break;
      case ButtonType.OPT_IN:
        result = PartnerConfig.CONFIG_FOOTER_BUTTON_ICON_OPT_IN;
        break;
      case ButtonType.SKIP:
        result = PartnerConfig.CONFIG_FOOTER_BUTTON_ICON_SKIP;
        break;
      case ButtonType.STOP:
        result = PartnerConfig.CONFIG_FOOTER_BUTTON_ICON_STOP;
        break;
      case ButtonType.OTHER:
      default:
        result = null;
        break;
    }
    return result;
  }

  protected View inflateFooter(@LayoutRes int footer) {
    LayoutInflater inflater =
        LayoutInflater.from(
            new ContextThemeWrapper(context, R.style.SucPartnerCustomizationButtonBar_Stackable));
    footerStub.setLayoutInflater(inflater);

    footerStub.setLayoutResource(footer);
    return footerStub.inflate();
  }

  private void updateFooterBarPadding(
      LinearLayout buttonContainer, int left, int top, int right, int bottom) {
    if (buttonContainer == null) {
      // Ignore action since buttonContainer is null
      return;
    }
    buttonContainer.setPadding(left, top, right, bottom);
  }

  /** Returns the paddingTop of footer bar. */
  @VisibleForTesting
  int getPaddingTop() {
    return (buttonContainer != null) ? buttonContainer.getPaddingTop() : footerStub.getPaddingTop();
  }

  /** Returns the paddingBottom of footer bar. */
  @VisibleForTesting
  int getPaddingBottom() {
    return (buttonContainer != null)
        ? buttonContainer.getPaddingBottom()
        : footerStub.getPaddingBottom();
  }

  /** Uses for notify mixin the view already attached to window. */
  public void onAttachedToWindow() {
    metrics.logPrimaryButtonInitialStateVisibility(
        /* isVisible= */ isPrimaryButtonVisible(), /* isUsingXml= */ false);
    metrics.logSecondaryButtonInitialStateVisibility(
        /* isVisible= */ isSecondaryButtonVisible(), /* isUsingXml= */ false);
  }

  /** Uses for notify mixin the view already detached from window. */
  public void onDetachedFromWindow() {
    metrics.updateButtonVisibility(isPrimaryButtonVisible(), isSecondaryButtonVisible());
  }

  /**
   * Assigns logging metrics to bundle for PartnerCustomizationLayout to log metrics to SetupWizard.
   */
  @TargetApi(VERSION_CODES.Q)
  public PersistableBundle getLoggingMetrics() {
    return metrics.getMetrics();
  }

  private void updateTextColorForButton(Button button, boolean enable, int color) {
    if (enable) {
      FooterButtonStyleUtils.updateButtonTextEnabledColor(button, color);
    } else {
      FooterButtonStyleUtils.updateButtonTextDisabledColor(button, color);
    }
  }
}
