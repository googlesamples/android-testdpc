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

import android.os.UserHandle;
import com.google.android.setupcompat.portal.IPortalRegisterResultListener;
import com.google.android.setupcompat.portal.NotificationComponent;
import com.google.android.setupcompat.portal.ProgressServiceComponent;

/**
 * Declares the interface for notification related service methods.
 */
interface ISetupNotificationService {
  /** Register a notification without progress service */
  boolean registerNotification(in NotificationComponent component) = 0;
  void unregisterNotification(in NotificationComponent component) = 1;

  /** Register a progress service */
  void registerProgressService(in ProgressServiceComponent component, in UserHandle userHandle, IPortalRegisterResultListener listener) = 2;

  /** Checks the progress connection still alive or not.  */
  boolean isProgressServiceAlive(in ProgressServiceComponent component, in UserHandle userHandle) = 3;

  /** Checks portal avaailable or not. */
  boolean isPortalAvailable() = 4;
}