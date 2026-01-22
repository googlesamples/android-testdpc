/*
 * Copyright 2019 The Android Open Source Project
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

import android.annotation.SuppressLint;
import android.content.Context;
import android.util.AttributeSet;
import android.view.MotionEvent;
import android.view.View;
import android.widget.Button;
import androidx.annotation.VisibleForTesting;

/** Button that can react to touch when disabled. */
@SuppressWarnings("AppCompatCustomView")
public class FooterActionButton extends Button implements IFooterActionButton {

  @VisibleForTesting FooterButton footerButton;
  private boolean isPrimaryButtonStyle = false;

  public FooterActionButton(Context context, AttributeSet attrs) {
    super(context, attrs);
  }

  void setFooterButton(FooterButton footerButton) {
    this.footerButton = footerButton;
  }

  @SuppressLint("ClickableViewAccessibility")
  @Override
  public boolean onTouchEvent(MotionEvent event) {
    if (event.getAction() == MotionEvent.ACTION_DOWN) {
      if (footerButton != null
          && !footerButton.isEnabled()
          && footerButton.getVisibility() == View.VISIBLE) {
        // getOnClickListenerWhenDisabled is responsible for handling accessibility correctly,
        // calling performClick if necessary.
        OnClickListener listener = footerButton.getOnClickListenerWhenDisabled();
        if (listener != null) {
          listener.onClick(this);
        }
      }
    }
    return super.onTouchEvent(event);
  }

  /**
   * Sets this footer button is primary button style.
   *
   * @param isPrimaryButtonStyle True if this button is primary button style.
   */
  void setPrimaryButtonStyle(boolean isPrimaryButtonStyle) {
    this.isPrimaryButtonStyle = isPrimaryButtonStyle;
  }

  @Override
  public boolean isPrimaryButtonStyle() {
    return isPrimaryButtonStyle;
  }
}
