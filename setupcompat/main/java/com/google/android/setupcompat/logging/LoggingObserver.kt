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
package com.google.android.setupcompat.logging

import android.view.View

/**
 * An abstract class which can be attached to a Setupcompat layout and provides methods for logging
 * impressions and interactions of its views and buttons.
 */
interface LoggingObserver {
  fun log(event: SetupCompatUiEvent)

  sealed class SetupCompatUiEvent {
    data class LayoutInflatedEvent(val view: View) : SetupCompatUiEvent()

    data class LayoutShownEvent(val view: View) : SetupCompatUiEvent()

    data class ButtonInflatedEvent(val view: View, val buttonType: ButtonType) :
      SetupCompatUiEvent()

    data class ButtonShownEvent(val view: View, val buttonType: ButtonType) : SetupCompatUiEvent()

    data class ButtonInteractionEvent(val view: View, val interactionType: InteractionType) :
      SetupCompatUiEvent()
  }

  enum class ButtonType {
    UNKNOWN,
    PRIMARY,
    SECONDARY
  }

  enum class InteractionType {
    UNKNOWN,
    TAP,
    LONG_PRESS,
    DOUBLE_TAP
  }
}
