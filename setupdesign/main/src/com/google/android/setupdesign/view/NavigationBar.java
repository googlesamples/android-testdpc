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
import android.content.res.TypedArray;
import android.graphics.Color;
import android.os.Build.VERSION_CODES;
import android.util.AttributeSet;
import android.view.ContextThemeWrapper;
import android.view.View;
import android.widget.Button;
import android.widget.LinearLayout;
import androidx.annotation.StyleableRes;
import com.google.android.setupdesign.R;

/**
 * Custom navigation bar for use with setup wizard. This bar contains a back button, more button and
 * next button. By default, the more button is hidden, and typically the next button will be hidden
 * if the more button is shown.
 *
 * @see com.google.android.setupdesign.template.RequireScrollMixin
 */
public class NavigationBar extends LinearLayout implements View.OnClickListener {

  /**
   * An interface to listen to events of the navigation bar, namely when the user clicks on the back
   * or next button.
   */
  public interface NavigationBarListener {
    void onNavigateBack();

    void onNavigateNext();
  }

  private static int getNavbarTheme(Context context) {
    // Normally we can automatically guess the theme by comparing the foreground color against
    // the background color. But we also allow specifying explicitly using sudNavBarTheme.
    TypedArray attributes =
        context.obtainStyledAttributes(
            new int[] {
              R.attr.sudNavBarTheme, android.R.attr.colorForeground, android.R.attr.colorBackground
            });
    @StyleableRes int navBarTheme = 0;
    @StyleableRes int colorForeground = 1;
    @StyleableRes int colorBackground = 2;
    int theme = attributes.getResourceId(navBarTheme, 0);
    if (theme == 0) {
      // Compare the value of the foreground against the background color to see if current
      // theme is light-on-dark or dark-on-light.
      float[] foregroundHsv = new float[3];
      float[] backgroundHsv = new float[3];
      Color.colorToHSV(attributes.getColor(colorForeground, 0), foregroundHsv);
      Color.colorToHSV(attributes.getColor(colorBackground, 0), backgroundHsv);
      boolean isDarkBg = foregroundHsv[2] > backgroundHsv[2];
      theme = isDarkBg ? R.style.SudNavBarThemeDark : R.style.SudNavBarThemeLight;
    }
    attributes.recycle();
    return theme;
  }

  private static Context getThemedContext(Context context) {
    final int theme = getNavbarTheme(context);
    return new ContextThemeWrapper(context, theme);
  }

  private Button nextButton;
  private Button backButton;
  private Button moreButton;
  private NavigationBarListener listener;

  public NavigationBar(Context context) {
    super(getThemedContext(context));
    init();
  }

  public NavigationBar(Context context, AttributeSet attrs) {
    super(getThemedContext(context), attrs);
    init();
  }

  @TargetApi(VERSION_CODES.HONEYCOMB)
  public NavigationBar(Context context, AttributeSet attrs, int defStyleAttr) {
    super(getThemedContext(context), attrs, defStyleAttr);
    init();
  }

  // All the constructors delegate to this init method. The 3-argument constructor is not
  // available in LinearLayout before v11, so call super with the exact same arguments.
  private void init() {
    if (isInEditMode()) {
      return;
    }

    View.inflate(getContext(), R.layout.sud_navbar_view, this);
    nextButton = (Button) findViewById(R.id.sud_navbar_next);
    backButton = (Button) findViewById(R.id.sud_navbar_back);
    moreButton = (Button) findViewById(R.id.sud_navbar_more);
  }

  public Button getBackButton() {
    return backButton;
  }

  public Button getNextButton() {
    return nextButton;
  }

  public Button getMoreButton() {
    return moreButton;
  }

  public void setNavigationBarListener(NavigationBarListener listener) {
    this.listener = listener;
    if (this.listener != null) {
      getBackButton().setOnClickListener(this);
      getNextButton().setOnClickListener(this);
    }
  }

  @Override
  public void onClick(View view) {
    if (listener != null) {
      if (view == getBackButton()) {
        listener.onNavigateBack();
      } else if (view == getNextButton()) {
        listener.onNavigateNext();
      }
    }
  }
}
