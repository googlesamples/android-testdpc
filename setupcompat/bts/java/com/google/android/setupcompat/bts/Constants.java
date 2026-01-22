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

package com.google.android.setupcompat.bts;

/** Constant values used by {@link com.google.android.setupcompat.bts.AbstractSetupBtsService}. */
public class Constants {

  /**
   * The extra key for {@link AbstractSetupBtsService} to send the task result to SUW for metric
   * collection.
   */
  public static final String EXTRA_KEY_TASK_SUCCEED = "succeed";

  /**
   * The extra key for {@link com.google.android.setupcompat.bts.AbstractSetupBtsService} to send
   * the failed reason to SUW for metric collection.
   */
  public static final String EXTRA_KEY_TASK_FAILED_REASON = "failed_reason";

  private Constants() {}
}
