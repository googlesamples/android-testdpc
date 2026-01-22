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

package com.google.android.setupcompat.template;

import android.view.MotionEvent;

/**
 * Interface for footer action buttons in Setup library to indicate Android Button or Material
 * button classes.
 *
 * <p>This interface defines common methods for footer action buttons, regardless of their specific
 * implementation. It provides a way to interact with footer buttons and determine their style
 * attributes.
 */
public interface IFooterActionButton {

  /**
   * Handles touch events for the footer action button, ensuring accessibility and proper behavior
   * even when the button is disabled.
   *
   * @param event The MotionEvent object representing the touch event.
   * @return true if the event was consumed by the button, false otherwise.
   */
  boolean onTouchEvent(MotionEvent event);

  /** Returns true when the footer button is primary button style. */
  boolean isPrimaryButtonStyle();
}
