/*
 * Copyright (C) 2020 The Android Open Source Project
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

package com.google.android.setupcompat.portal;

/** Constant values used for PortalExtension */
public class PortalExtensionConstants {
  /**
   * Intent action to bind Portal Service.
   * @deprecated, use {@code BIND_SERVICE_V_1_1_INTENT_ACTION}.
   * */
  @Deprecated
  public static final String BIND_SERVICE_INTENT_ACTION =
      "com.google.android.setupcompat.portal.SetupNotificationService.BIND_EXTENSION";

  /**
   * Intent action to bind Portal Service.
   */
  public static final String BIND_SERVICE_V_1_1_INTENT_ACTION =
      "com.google.android.setupcompat.portal.SetupNotificationService.BIND_EXTENSION_V_1_1";

  private PortalExtensionConstants() {}
}
