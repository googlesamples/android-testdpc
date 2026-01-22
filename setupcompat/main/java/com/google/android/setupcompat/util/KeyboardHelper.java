/*
 * Copyright (C) 2024 The Android Open Source Project
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
package com.google.android.setupcompat.util;

import android.content.Context;
import android.content.res.Configuration;
import androidx.annotation.NonNull;
import com.google.android.setupcompat.partnerconfig.PartnerConfigHelper;

/** Helper class to handle keyboard related operations. */
public final class KeyboardHelper {

  /** Returns whether the keyboard focus changed is enabled. */
  public static boolean isKeyboardFocusEnhancementEnabled(@NonNull Context context) {
    return PartnerConfigHelper.isKeyboardFocusEnhancementEnabled(context);
  }

  /** Returns whether a physical keyboard is available. */
  public static boolean hasHardwareKeyboard(Context context) {
    Configuration configuration = context.getResources().getConfiguration();
    return configuration.keyboard != Configuration.KEYBOARD_NOKEYS
        && configuration.hardKeyboardHidden != Configuration.HARDKEYBOARDHIDDEN_YES;
  }

  private KeyboardHelper() {}
}
