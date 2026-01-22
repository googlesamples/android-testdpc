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

import android.os.Bundle;
import com.google.android.setupcompat.portal.IPortalProgressCallback;

/**
 * Interface of progress service, all servics needs to run during onboarding, and would like
 * to consolidate notifications with SetupNotificationService, should implement this interface.
 * So that SetupNotificationService can bind the progress service and run below
 * SetupNotificationService.
 */
interface IPortalProgressService {
  /**
  * Called when the Portal notification is started.
  */
  oneway void onPortalSessionStart() = 0;

  /**
   * Provides a non-null callback after service connected.
   */
  oneway void onSetCallback(IPortalProgressCallback callback) = 1;

  /**
   * Called when progress timed out.
   */
  oneway void onSuspend() = 2;

  /**
   * User clicks "User mobile data" on portal activity.
   * All running progress should agree to use mobile data.
   */
  oneway void onAllowMobileData(boolean allowed) = 3;

  /**
   * Portal service calls to get remaining downloading size(MB) from progress service.
   */
  @nullable
  Bundle onGetRemainingValues() = 4;
}