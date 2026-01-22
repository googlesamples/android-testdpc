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

package com.google.android.setupcompat.internal;

import androidx.annotation.VisibleForTesting;
import java.util.concurrent.TimeUnit;

/** Provider for time in nanos and millis. Allows overriding time during tests. */
public class ClockProvider {

  public static long timeInNanos() {
    return ticker.read();
  }

  public static long timeInMillis() {
    return TimeUnit.NANOSECONDS.toMillis(timeInNanos());
  }

  @VisibleForTesting
  public static void resetInstance() {
    ticker = SYSTEM_TICKER;
  }

  @VisibleForTesting
  public static void setInstance(Supplier<Long> nanoSecondSupplier) {
    ticker = () -> nanoSecondSupplier.get();
  }

  public long read() {
    return ticker.read();
  }

  private static final Ticker SYSTEM_TICKER = () -> System.nanoTime();
  private static Ticker ticker = SYSTEM_TICKER;

  @VisibleForTesting
  public interface Supplier<T> {
    T get();
  }
}
