/*
 * Copyright (C) 2018 The Android Open Source Project
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

package com.google.android.setupcompat.logging;

import android.util.Log;
import com.google.android.setupcompat.internal.ClockProvider;
import com.google.android.setupcompat.internal.Preconditions;

/** Convenience utility to log duration events. Please note that this class is not thread-safe. */
public final class Timer {
  /** Creates a new instance of timer for the given {@code metricKey}. */
  public Timer(MetricKey metricKey) {
    this.metricKey = metricKey;
  }

  /**
   * Starts the timer and notes the current clock time.
   *
   * @throws IllegalStateException if the timer was stopped.
   */
  public void start() {
    Preconditions.checkState(!isStopped(), "Timer cannot be started once stopped.");
    if (isStarted()) {
      Log.wtf(
          TAG,
          String.format(
              "Timer instance was already started for: %s at [%s].", metricKey, startInNanos));
      return;
    }
    startInNanos = ClockProvider.timeInNanos();
  }

  /**
   * Stops the watch and the current clock time is noted.
   *
   * @throws IllegalStateException if the watch was not started.
   */
  public void stop() {
    Preconditions.checkState(isStarted(), "Timer must be started before it can be stopped.");
    if (isStopped()) {
      Log.wtf(
          TAG,
          String.format(
              "Timer instance was already stopped for: %s at [%s]", metricKey, stopInNanos));
      return;
    }
    stopInNanos = ClockProvider.timeInNanos();
  }

  boolean isStopped() {
    return stopInNanos != 0;
  }

  private boolean isStarted() {
    return startInNanos != 0;
  }

  long getDurationInNanos() {
    return stopInNanos - startInNanos;
  }

  MetricKey getMetricKey() {
    return metricKey;
  }

  private long startInNanos;
  private long stopInNanos;
  private final MetricKey metricKey;

  private static final String TAG = "SetupCompat.Timer";
}
