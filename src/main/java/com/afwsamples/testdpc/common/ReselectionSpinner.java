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

package com.afwsamples.testdpc.common;

import android.content.Context;
import android.support.v7.widget.AppCompatSpinner;
import android.util.AttributeSet;

/**
 * A wrapper class of spinner which passes the onItemSelected event even when the same item is
 * selected again.
 */
public class ReselectionSpinner extends AppCompatSpinner {

  public ReselectionSpinner(Context context) {
    super(context);
  }

  public ReselectionSpinner(Context context, int mode) {
    super(context, mode);
  }

  public ReselectionSpinner(Context context, AttributeSet attrs) {
    super(context, attrs);
  }

  public ReselectionSpinner(Context context, AttributeSet attrs, int defStyleAttr) {
    super(context, attrs, defStyleAttr);
  }

  public ReselectionSpinner(Context context, AttributeSet attrs, int defStyleAttr, int mode) {
    super(context, attrs, defStyleAttr, mode);
  }

  @Override
  public void setSelection(int position) {
    int previousSelectedPosition = getSelectedItemPosition();
    super.setSelection(position);
    if (position == previousSelectedPosition) {
      OnItemSelectedListener onItemSelectedListener = getOnItemSelectedListener();
      if (onItemSelectedListener != null) {
        onItemSelectedListener.onItemSelected(
            this, getSelectedView(), position, getSelectedItemId());
      }
    }
  }
}
