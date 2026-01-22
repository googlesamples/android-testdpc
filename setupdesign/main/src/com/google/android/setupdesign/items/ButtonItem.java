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

package com.google.android.setupdesign.items;

import android.annotation.SuppressLint;
import android.content.Context;
import android.content.res.TypedArray;
import android.util.AttributeSet;
import android.view.ContextThemeWrapper;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Button;
import com.google.android.setupdesign.R;

/**
 * Description of a button inside {@link com.google.android.setupdesign.items.ButtonBarItem}. This
 * item will not be bound by the adapter, and must be a child of {@code ButtonBarItem}.
 */
public class ButtonItem extends AbstractItem implements View.OnClickListener {

  public interface OnClickListener {
    void onClick(ButtonItem item);
  }

  private boolean enabled = true;
  private CharSequence text;
  private int theme = R.style.SudButtonItem;
  private OnClickListener listener;

  private Button button;

  public ButtonItem() {
    super();
  }

  public ButtonItem(Context context, AttributeSet attrs) {
    super(context, attrs);
    TypedArray a = context.obtainStyledAttributes(attrs, R.styleable.SudButtonItem);
    enabled = a.getBoolean(R.styleable.SudButtonItem_android_enabled, true);
    text = a.getText(R.styleable.SudButtonItem_android_text);
    theme = a.getResourceId(R.styleable.SudButtonItem_android_theme, R.style.SudButtonItem);
    a.recycle();
  }

  public void setOnClickListener(OnClickListener listener) {
    this.listener = listener;
  }

  public void setText(CharSequence text) {
    this.text = text;
  }

  public CharSequence getText() {
    return text;
  }

  /**
   * The theme to use for this button. This can be used to create button of a particular style (e.g.
   * a colored or borderless button). Typically {@code android:buttonStyle} will be set in the theme
   * to change the style applied by the button.
   *
   * @param theme Resource ID of the theme
   */
  public void setTheme(int theme) {
    this.theme = theme;
    button = null;
  }

  /** @return Resource ID of the theme used by this button. */
  public int getTheme() {
    return theme;
  }

  public void setEnabled(boolean enabled) {
    this.enabled = enabled;
  }

  @Override
  public int getCount() {
    return 0;
  }

  @Override
  public boolean isEnabled() {
    return enabled;
  }

  @Override
  public int getLayoutResource() {
    return 0;
  }

  /** Do not use this since ButtonItem is not directly part of a list. */
  @Override
  public final void onBindView(View view) {
    throw new UnsupportedOperationException("Cannot bind to ButtonItem's view");
  }

  /**
   * Create a button according to this button item.
   *
   * @param parent The parent of the button, used to retrieve the theme and context for this button.
   * @return A button that can be added to the parent.
   */
  protected Button createButton(ViewGroup parent) {
    if (button == null) {
      Context context = parent.getContext();
      if (theme != 0) {
        context = new ContextThemeWrapper(context, theme);
      }
      button = createButton(context);
      button.setOnClickListener(this);
    } else {
      if (button.getParent() instanceof ViewGroup) {
        // A view cannot be added to a different parent if one already exists. Remove this
        // button from its parent before returning.
        ((ViewGroup) button.getParent()).removeView(button);
      }
    }
    button.setEnabled(enabled);
    button.setText(text);
    button.setId(getViewId());
    return button;
  }

  @SuppressLint("InflateParams") // This is used similar to Button(Context), so it's OK to not
  // specify the parent.
  private Button createButton(Context context) {
    // Inflate a single button from XML, so that when using support lib, it will take advantage
    // of the injected layout inflater and give us AppCompatButton instead.
    return (Button) LayoutInflater.from(context).inflate(R.layout.sud_button, null, false);
  }

  @Override
  public void onClick(View v) {
    if (listener != null) {
      listener.onClick(this);
    }
  }
}
