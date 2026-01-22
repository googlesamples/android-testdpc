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

import androidx.annotation.Nullable;
import androidx.annotation.VisibleForTesting;
import java.util.concurrent.ArrayBlockingQueue;
import java.util.concurrent.Executor;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.ThreadPoolExecutor;
import java.util.concurrent.TimeUnit;

/**
 * Utility class to provide executors.
 *
 * <p>It allows the executors to be mocked in Robolectric, redirecting to Robolectric's schedulers
 * rather than using real threads.
 */
public final class ExecutorProvider<T extends Executor> {

  private static final int SETUP_METRICS_LOGGER_MAX_QUEUED = 50;
  /**
   * Creates a single threaded {@link ExecutorService} with a maximum pool size {@code maxSize}.
   * Jobs submitted when the pool is full causes {@link
   * java.util.concurrent.RejectedExecutionException} to be thrown.
   */
  public static final ExecutorProvider<ExecutorService> setupCompatServiceInvoker =
      new ExecutorProvider<>(
          createSizeBoundedExecutor("SetupCompatServiceInvoker", SETUP_METRICS_LOGGER_MAX_QUEUED));

  private final T executor;

  @Nullable private T injectedExecutor;

  private ExecutorProvider(T executor) {
    this.executor = executor;
  }

  public T get() {
    if (injectedExecutor != null) {
      return injectedExecutor;
    }
    return executor;
  }

  /**
   * Injects an executor for testing use for this provider. Subsequent calls to {@link #get} will
   * return this instance instead, until {@link #resetExecutors()} is called.
   */
  @VisibleForTesting
  public void injectExecutor(T executor) {
    this.injectedExecutor = executor;
  }

  @VisibleForTesting
  public static void resetExecutors() {
    setupCompatServiceInvoker.injectedExecutor = null;
  }

  @VisibleForTesting
  public static ExecutorService createSizeBoundedExecutor(String threadName, int maxSize) {
    return new ThreadPoolExecutor(
        /* corePoolSize= */ 1,
        /* maximumPoolSize= */ 1,
        /* keepAliveTime= */ 0,
        TimeUnit.SECONDS,
        new ArrayBlockingQueue<>(maxSize),
        runnable -> new Thread(runnable, threadName));
  }
}
