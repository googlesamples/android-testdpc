/*
 * Copyright (C) 2019 The Android Open Source Project
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

import android.os.Looper;

/**
 * Static convenience methods that help a method or constructor check whether it was invoked
 * correctly (that is, whether its <i>preconditions</i> were met).
 *
 * <p>If the precondition is not met, the {@code Preconditions} method throws an unchecked exception
 * of a specified type, which helps the method in which the exception was thrown communicate that
 * its caller has made a mistake.
 */
public final class Preconditions {

  /** Ensures the truth of an expression involving one or more parameters to the calling method. */
  public static void checkArgument(boolean expression, String errorMessage) {
    if (!expression) {
      throw new IllegalArgumentException(errorMessage);
    }
  }

  /**
   * Ensures the truth of an expression involving the state of the calling instance, but not
   * involving any parameters to the calling method.
   */
  public static void checkState(boolean expression, String errorMessage) {
    if (!expression) {
      throw new IllegalStateException(errorMessage);
    }
  }

  /** Ensures that an object reference passed as a parameter to the calling method is not null. */
  public static <T> T checkNotNull(T reference, String errorMessage) {
    if (reference == null) {
      throw new NullPointerException(errorMessage);
    }
    return reference;
  }

  /**
   * Ensures that this method is called from the main thread, otherwise an exception will be thrown.
   */
  public static void ensureOnMainThread(String whichMethod) {
    if (Looper.myLooper() == Looper.getMainLooper()) {
      return;
    }
    throw new IllegalStateException(whichMethod + " must be called from the UI thread.");
  }
  /**
   * Ensure that this method is not called from the main thread, otherwise an exception will be
   * thrown.
   */
  public static void ensureNotOnMainThread(String whichMethod) {
    if (Looper.myLooper() != Looper.getMainLooper()) {
      return;
    }
    throw new IllegalThreadStateException(whichMethod + " cannot be called from the UI thread.");
  }
}
