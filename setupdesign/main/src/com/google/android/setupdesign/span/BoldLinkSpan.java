/*
 * Copyright (C) 2023 The Android Open Source Project
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
package com.google.android.setupdesign.span;

import android.content.Context;
import android.os.Build;
import android.text.TextPaint;
import androidx.annotation.VisibleForTesting;

/** A clickableSpan extends the {@link LinkSpan} with the configurable bold style. */
public class BoldLinkSpan extends LinkSpan {

  /* FontStyle.FONT_WEIGHT_BOLD - FontStyle.FONT_WEIGHT_NORMAL */
  @VisibleForTesting static final int BOLD_TEXT_ADJUSTMENT = 300;

  private final Context context;

  public BoldLinkSpan(Context context, String link) {
    super(link);
    this.context = context;
  }

  @Override
  public void updateDrawState(TextPaint drawState) {
    super.updateDrawState(drawState);
    if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.S) {
      int fontWeightAdjustment = context.getResources().getConfiguration().fontWeightAdjustment;
      boolean boldText = fontWeightAdjustment == BOLD_TEXT_ADJUSTMENT;
      drawState.setFakeBoldText(boldText);
    }
    drawState.setUnderlineText(true);
  }
}
