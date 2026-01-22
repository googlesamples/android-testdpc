/*
 * Copyright 2018 The Android Open Source Project
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *     http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */

package com.google.android.setupcompat.template;

import static java.lang.annotation.RetentionPolicy.SOURCE;

import android.annotation.TargetApi;
import android.content.Context;
import android.content.res.TypedArray;
import android.os.Build.VERSION_CODES;
import android.os.PersistableBundle;
import android.util.AttributeSet;
import android.view.View;
import android.view.View.OnClickListener;
import androidx.annotation.IntDef;
import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.annotation.StringRes;
import androidx.annotation.StyleRes;
import com.google.android.setupcompat.R;
import com.google.android.setupcompat.logging.CustomEvent;
import com.google.android.setupcompat.logging.LoggingObserver;
import com.google.android.setupcompat.logging.LoggingObserver.InteractionType;
import com.google.android.setupcompat.logging.LoggingObserver.SetupCompatUiEvent.ButtonInteractionEvent;
import java.lang.annotation.Retention;
import java.util.Locale;

/**
 * Definition of a footer button. Clients can use this class to customize attributes like text,
 * button type and click listener, and FooterBarMixin will inflate a corresponding Button view.
 */
public final class FooterButton implements OnClickListener {
  private static final String KEY_BUTTON_ON_CLICK_COUNT = "_onClickCount";
  private static final String KEY_BUTTON_TEXT = "_text";
  private static final String KEY_BUTTON_TYPE = "_type";

  @ButtonType private final int buttonType;
  private CharSequence text;
  private boolean enabled = true;
  private int visibility = View.VISIBLE;
  private int theme;
  private OnClickListener onClickListener;
  private OnClickListener onClickListenerWhenDisabled;
  private OnButtonEventListener buttonListener;
  private LoggingObserver loggingObserver;
  private int clickCount = 0;
  private Locale locale;
  private int direction;

  public FooterButton(Context context, AttributeSet attrs) {
    TypedArray a = context.obtainStyledAttributes(attrs, R.styleable.SucFooterButton);
    text = a.getString(R.styleable.SucFooterButton_android_text);
    onClickListener = null;
    buttonType =
        getButtonTypeValue(
            a.getInt(R.styleable.SucFooterButton_sucButtonType, /* defValue= */ ButtonType.OTHER));
    theme = a.getResourceId(R.styleable.SucFooterButton_android_theme, /* defValue= */ 0);
    a.recycle();
  }

  /**
   * Allows client customize text, click listener and theme for footer button before Button has been
   * created. The {@link FooterBarMixin} will inflate a corresponding Button view.
   *
   * @param text The text for button.
   * @param listener The listener for button.
   * @param buttonType The type of button.
   * @param theme The theme for button.
   * @param visibility the visibility for button.
   */
  private FooterButton(
      CharSequence text,
      @Nullable OnClickListener listener,
      @ButtonType int buttonType,
      @StyleRes int theme,
      Locale locale,
      int direction,
      int visibility) {
    this.text = text;
    onClickListener = listener;
    this.buttonType = buttonType;
    this.theme = theme;
    this.locale = locale;
    this.direction = direction;
    this.visibility = visibility;
  }

  /** Returns the text that this footer button is displaying. */
  public CharSequence getText() {
    return text;
  }

  /**
   * Registers a callback to be invoked when this view of footer button is clicked.
   *
   * @param listener The callback that will run
   */
  public void setOnClickListener(@Nullable OnClickListener listener) {
    onClickListener = listener;
  }

  /** Returns an {@link OnClickListener} of this footer button. */
  public OnClickListener getOnClickListenerWhenDisabled() {
    return onClickListenerWhenDisabled;
  }

  /**
   * Registers a callback to be invoked when footer button disabled and touch event has reacted.
   *
   * @param listener The callback that will run
   */
  public void setOnClickListenerWhenDisabled(@Nullable OnClickListener listener) {
    onClickListenerWhenDisabled = listener;
  }

  /** Returns the type of this footer button icon. */
  @ButtonType
  public int getButtonType() {
    return buttonType;
  }

  /** Returns the theme of this footer button. */
  @StyleRes
  public int getTheme() {
    return theme;
  }

  /**
   * Sets the enabled state of this footer button.
   *
   * @param enabled True if this view is enabled, false otherwise.
   */
  public void setEnabled(boolean enabled) {
    this.enabled = enabled;
    if (buttonListener != null) {
      buttonListener.onEnabledChanged(enabled);
    }
  }

  /** Returns the enabled status for this footer button. */
  public boolean isEnabled() {
    return enabled;
  }

  /** Returns the layout direction for this footer button. */
  public int getLayoutDirection() {
    return direction;
  }

  /** Returns the text locale for this footer button. */
  public Locale getTextLocale() {
    return locale;
  }

  /**
   * Sets the visibility state of this footer button.
   *
   * @param visibility one of {@link #VISIBLE}, {@link #INVISIBLE}, or {@link #GONE}.
   */
  public void setVisibility(int visibility) {
    this.visibility = visibility;
    if (buttonListener != null) {
      buttonListener.onVisibilityChanged(visibility);
    }
  }

  /** Returns the visibility status for this footer button. */
  public int getVisibility() {
    return visibility;
  }

  /** Sets the text to be displayed using a string resource identifier. */
  public void setText(Context context, @StringRes int resId) {
    setText(context.getText(resId));
  }

  /** Sets the text to be displayed on footer button. */
  public void setText(CharSequence text) {
    this.text = text;
    if (buttonListener != null) {
      buttonListener.onTextChanged(text);
    }
  }

  /** Sets the text locale to be displayed on footer button. */
  public void setTextLocale(Locale locale) {
    this.locale = locale;
    if (buttonListener != null) {
      buttonListener.onLocaleChanged(locale);
    }
  }

  /** Sets the layout direction to be displayed on footer button. */
  public void setLayoutDirection(int direction) {
    this.direction = direction;
    if (buttonListener != null) {
      buttonListener.onDirectionChanged(direction);
    }
  }

  /**
   * Registers a callback to be invoked when footer button API has set.
   *
   * @param listener The callback that will run
   */
  void setOnButtonEventListener(@Nullable OnButtonEventListener listener) {
    if (listener != null) {
      buttonListener = listener;
    } else {
      throw new NullPointerException("Event listener of footer button may not be null.");
    }
  }

  @Override
  public void onClick(View v) {
    if (onClickListener != null) {
      clickCount++;
      onClickListener.onClick(v);
      if (loggingObserver != null) {
        loggingObserver.log(new ButtonInteractionEvent(v, InteractionType.TAP));
      }
    }
  }

  void setLoggingObserver(LoggingObserver loggingObserver) {
    this.loggingObserver = loggingObserver;
  }

  /** Interface definition for a callback to be invoked when footer button API has set. */
  interface OnButtonEventListener {

    void onEnabledChanged(boolean enabled);

    void onVisibilityChanged(int visibility);

    void onTextChanged(CharSequence text);

    void onLocaleChanged(Locale locale);

    void onDirectionChanged(int direction);
  }

  /** Maximum valid value of ButtonType */
  private static final int MAX_BUTTON_TYPE = 8;

  @Retention(SOURCE)
  @IntDef({
    ButtonType.OTHER,
    ButtonType.ADD_ANOTHER,
    ButtonType.CANCEL,
    ButtonType.CLEAR,
    ButtonType.DONE,
    ButtonType.NEXT,
    ButtonType.OPT_IN,
    ButtonType.SKIP,
    ButtonType.STOP
  })
  /**
   * Types for footer button. The button appearance and behavior may change based on its type. In
   * order to be backward compatible with application built with old version of setupcompat; each
   * ButtonType should not be changed.
   */
  public @interface ButtonType {
    /** A type of button that doesn't fit into any other categories. */
    int OTHER = 0;

    /**
     * A type of button that will set up additional elements of the ongoing setup step(s) when
     * clicked.
     */
    int ADD_ANOTHER = 1;

    /** A type of button that will cancel the ongoing setup step(s) and exit setup when clicked. */
    int CANCEL = 2;

    /** A type of button that will clear the progress when clicked. (eg: clear PIN code) */
    int CLEAR = 3;

    /** A type of button that will exit the setup flow when clicked. */
    int DONE = 4;

    /** A type of button that will go to the next screen, or next step in the flow when clicked. */
    int NEXT = 5;

    /** A type of button to opt-in or agree to the features described in the current screen. */
    int OPT_IN = 6;

    /** A type of button that will skip the current step when clicked. */
    int SKIP = 7;

    /** A type of button that will stop the ongoing setup step(s) and skip forward when clicked. */
    int STOP = 8;
  }

  private int getButtonTypeValue(int value) {
    if (value >= 0 && value <= MAX_BUTTON_TYPE) {
      return value;
    } else {
      throw new IllegalArgumentException("Not a ButtonType");
    }
  }

  private String getButtonTypeName() {
    switch (buttonType) {
      case ButtonType.ADD_ANOTHER:
        return "ADD_ANOTHER";
      case ButtonType.CANCEL:
        return "CANCEL";
      case ButtonType.CLEAR:
        return "CLEAR";
      case ButtonType.DONE:
        return "DONE";
      case ButtonType.NEXT:
        return "NEXT";
      case ButtonType.OPT_IN:
        return "OPT_IN";
      case ButtonType.SKIP:
        return "SKIP";
      case ButtonType.STOP:
        return "STOP";
      case ButtonType.OTHER:
      default:
        return "OTHER";
    }
  }

  /**
   * Returns footer button related metrics bundle for PartnerCustomizationLayout to log to
   * SetupWizard.
   */
  @TargetApi(VERSION_CODES.Q)
  public PersistableBundle getMetrics(String buttonName) {
    PersistableBundle bundle = new PersistableBundle();
    bundle.putString(
        buttonName + KEY_BUTTON_TEXT, CustomEvent.trimsStringOverMaxLength(getText().toString()));
    bundle.putString(buttonName + KEY_BUTTON_TYPE, getButtonTypeName());
    bundle.putInt(buttonName + KEY_BUTTON_ON_CLICK_COUNT, clickCount);
    return bundle;
  }

  /**
   * Builder class for constructing {@code FooterButton} objects.
   *
   * <p>Allows client customize text, click listener and theme for footer button before Button has
   * been created. The {@link FooterBarMixin} will inflate a corresponding Button view.
   *
   * <p>Example:
   *
   * <pre class="prettyprint">
   * FooterButton primaryButton =
   *     new FooterButton.Builder(mContext)
   *         .setText(R.string.primary_button_label)
   *         .setListener(primaryButton)
   *         .setButtonType(ButtonType.NEXT)
   *         .setTheme(R.style.SuwGlifButton_Primary)
   *         .setTextLocale(Locale.CANADA)
   *         .setLayoutDirection(View.LAYOUT_DIRECTION_LTR)
   *         .setVisibility(View.VISIBLE)
   *         .build();
   * </pre>
   */
  public static class Builder {
    private final Context context;
    private String text = "";
    private Locale locale = null;
    private int direction = -1;
    private OnClickListener onClickListener = null;
    @ButtonType private int buttonType = ButtonType.OTHER;
    private int theme = 0;

    private int visibility = View.VISIBLE;

    public Builder(@NonNull Context context) {
      this.context = context;
    }

    /** Sets the {@code text} of FooterButton. */
    public Builder setText(String text) {
      this.text = text;
      return this;
    }

    /** Sets the {@code text} of FooterButton by resource. */
    public Builder setText(@StringRes int text) {
      this.text = context.getString(text);
      return this;
    }

    /** Sets the {@code locale} of FooterButton. */
    public Builder setTextLocale(Locale locale) {
      this.locale = locale;
      return this;
    }

    /** Sets the {@code direction} of FooterButton. */
    public Builder setLayoutDirection(int direction) {
      this.direction = direction;
      return this;
    }

    /** Sets the {@code listener} of FooterButton. */
    public Builder setListener(@Nullable OnClickListener listener) {
      onClickListener = listener;
      return this;
    }

    /** Sets the {@code buttonType} of FooterButton. */
    public Builder setButtonType(@ButtonType int buttonType) {
      this.buttonType = buttonType;
      return this;
    }

    /** Sets the {@code theme} for applying FooterButton. */
    public Builder setTheme(@StyleRes int theme) {
      this.theme = theme;
      return this;
    }

    /** Sets the {@code visibility} of FooterButton. */
    public Builder setVisibility(int visibility) {
      this.visibility = visibility;
      return this;
    }

    public FooterButton build() {
      return new FooterButton(
          text, onClickListener, buttonType, theme, locale, direction, visibility);
    }
  }
}
