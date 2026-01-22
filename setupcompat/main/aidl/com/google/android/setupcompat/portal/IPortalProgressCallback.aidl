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

/**
 * Interface for progress service to update progress to SUW. Clients should
 * update progress at least once a minute, or set a pending reason to stop
 * update progress. Without progress update and pending reason. We considering
 * the progress service is no response will suspend it and unbinde service.
 */
interface IPortalProgressCallback {
  /**
   * Sets completed amount.
   */
  Bundle setProgressCount(int completed, int failed, int total) = 0;

  /**
   * Sets completed percentage.
   */
  Bundle setProgressPercentage(int percentage) = 1;

  /**
   * Sets the summary shows on portal activity.
   */
  Bundle setSummary(String summary) = 2;

  /**
   * Let SUW knows the progress is pending now, and stop update progress.
   * @param reasonResId The resource identifier.
   * @param quantity The number used to get the correct string for the current language's
   *           plural rules
   * @param formatArgs The format arguments that will be used for substitution.
   */
  Bundle setPendingReason(int reasonResId, int quantity, in int[] formatArgs, int reason) = 3;

  /**
   * Once the progress completed, and service can be destroy. Call this function.
   * SUW will unbind the service.
   * @param resId The resource identifier.
   * @param quantity The number used to get the correct string for the current language's
   *           plural rules
   * @param formatArgs The format arguments that will be used for substitution.
   */
  Bundle setComplete(int resId, int quantity, in int[] formatArgs) = 4;

  /**
   * Once the progress failed, and not able to finish the progress. Should call
   * this function. SUW will unbind service after receive setFailure. Client can
   * registerProgressService again once the service is ready to execute.
   * @param resId The resource identifier.
   * @param quantity The number used to get the correct string for the current language's
   *           plural rules
   * @param formatArgs The format arguments that will be used for substitution.
   */
  Bundle setFailure(int resId, int quantity, in int[] formatArgs) = 5;
}