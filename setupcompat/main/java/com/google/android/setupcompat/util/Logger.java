/*
 * Copyright (C) 2021 The Android Open Source Project
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

import android.util.Log;

/**
 * Helper class that wraps {@link Log} to log messages to logcat. This class consolidate the log
 * {@link #TAG} in both SetupCompat and SetupDesign library.
 *
 * <p>When logging verbose and debug logs, the logs should either be guarded by {@code if
 * (logger.isV())}, or a constant if (DEBUG). That DEBUG constant should be false on any submitted
 * code.
 */
public final class Logger {

  public static final String TAG = "SetupLibrary";

  private final String prefix;

  public Logger(Class<?> cls) {
    this(cls.getSimpleName());
  }

  public Logger(String prefix) {
    this.prefix = "[" + prefix + "] ";
  }

  public boolean isV() {
    return Log.isLoggable(TAG, Log.VERBOSE);
  }

  public boolean isD() {
    return Log.isLoggable(TAG, Log.DEBUG);
  }

  public boolean isI() {
    return Log.isLoggable(TAG, Log.INFO);
  }

  public void atVerbose(String message) {
    if (isV()) {
      Log.v(TAG, prefix.concat(message));
    }
  }

  public void atDebug(String message) {
    if (isD()) {
      Log.d(TAG, prefix.concat(message));
    }
  }

  public void atInfo(String message) {
    if (isI()) {
      Log.i(TAG, prefix.concat(message));
    }
  }

  public void w(String message) {
    Log.w(TAG, prefix.concat(message));
  }

  public void e(String message) {
    Log.e(TAG, prefix.concat(message));
  }

  public void e(String message, Throwable throwable) {
    Log.e(TAG, prefix.concat(message), throwable);
  }
}
