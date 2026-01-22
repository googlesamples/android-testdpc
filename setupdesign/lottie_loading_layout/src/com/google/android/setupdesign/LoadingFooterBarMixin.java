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

package com.google.android.setupdesign;

import android.util.AttributeSet;
import android.widget.LinearLayout;
import androidx.annotation.Nullable;
import com.google.android.setupcompat.internal.TemplateLayout;
import com.google.android.setupcompat.template.FooterBarMixin;

/** A {@link Mixin} to get the container of footer bar for usage. */
public class LoadingFooterBarMixin extends FooterBarMixin {

  /**
   * Creates a mixin for managing buttons on the footer.
   *
   * @param layout The {@link TemplateLayout} containing this mixin.
   * @param attrs XML attributes given to the layout.
   * @param defStyleAttr The default style attribute as given to the constructor of the layout.
   */
  public LoadingFooterBarMixin(
      TemplateLayout layout, @Nullable AttributeSet attrs, int defStyleAttr) {
    super(layout, attrs, defStyleAttr);
  }

  @Override
  public LinearLayout getButtonContainer() {
    return super.getButtonContainer();
  }
}
