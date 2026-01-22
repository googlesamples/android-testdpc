/*
 * Copyright (C) 2022 The Android Open Source Project
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

import com.google.android.setupcompat.bts.IBtsTaskServiceCallback;

/**
 * Declare the interface for BTS task service.
 *
 * The SetupWizard will bind BtsTaskService when specific event triggerred. The service callback
 * using {@link IBtsTaskServiceCallback#onTaskFinished} to notify SetupWizard the task is already
 *  completed and SetupWizard will unbind the service.
 *
 * If the service can't be complete before end of SetupWizard, the SetupWizard still unbind the
 * service since the background task is no longer helpful for SetupWizard.
 */
interface IBtsTaskService {

  /**
  * Set the callback for the client to notify the job already completed and can
  * be disconnected.
  */
  oneway void setCallback(IBtsTaskServiceCallback callback) = 1;
}