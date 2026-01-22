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

import androidx.annotation.IntDef;
import androidx.annotation.StringDef;
import java.lang.annotation.Retention;
import java.lang.annotation.RetentionPolicy;

/** Constant values used for Portal */
public class PortalConstants {

  /** Enumeration of pending reasons, for {@link IPortalProgressCallback#setPendingReason}. */
  @Retention(RetentionPolicy.SOURCE)
  @IntDef({
    PendingReason.IN_PROGRESS,
    PendingReason.PROGRESS_REQUEST_ANY_NETWORK,
    PendingReason.PROGRESS_REQUEST_WIFI,
    PendingReason.PROGRESS_REQUEST_MOBILE,
    PendingReason.PROGRESS_RETRY,
    PendingReason.PROGRESS_REQUEST_REMOVED,
    PendingReason.MAX
  })
  public @interface PendingReason {
    /**
     * Don't used this, use {@link IPortalProgressCallback#setProgressCount} ot {@link
     * IPortalProgressCallback#setProgressPercentage} will reset pending reason to in progress.
     */
    int IN_PROGRESS = 0;

    /** Clients required network. */
    int PROGRESS_REQUEST_ANY_NETWORK = 1;

    /** Clients required a wifi network. */
    int PROGRESS_REQUEST_WIFI = 2;

    /** Client required a mobile data */
    int PROGRESS_REQUEST_MOBILE = 3;

    /** Client needs to wait for retry */
    int PROGRESS_RETRY = 4;

    /** Client required to remove added task */
    int PROGRESS_REQUEST_REMOVED = 5;

    int MAX = 6;
  }

  /** Bundle keys used in {@link IPortalProgressService#onGetRemainingValues}. */
  @Retention(RetentionPolicy.SOURCE)
  @StringDef({RemainingValues.REMAINING_SIZE_TO_BE_DOWNLOAD_IN_KB})
  public @interface RemainingValues {
    /** Remaining size to download in MB. */
    String REMAINING_SIZE_TO_BE_DOWNLOAD_IN_KB = "RemainingSizeInKB";
  }

  private PortalConstants() {}
}
