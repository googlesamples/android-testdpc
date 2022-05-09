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
package com.afwsamples.testdpc.common;

import java.io.FileDescriptor;
import java.io.PrintWriter;

/** Base class for all components that implements a custom {@code dump()} method. */
public interface Dumpable {

  /**
   * Checks whether the arguments contains the option to just dump this app's state (and skipping
   * Android SDK state).
   */
  public static boolean isQuietMode(String[] args) {
    return args != null && args.length > 0 && (args[0].equals("-q") || args[0].equals("--quiet"));
  }

  /**
   * Custom dump that should only dump this app's state (and skip Android SDK state) when {@code
   * quietModeOnly} is {@code true}.
   */
  void dump(String prefix, PrintWriter pw, FileDescriptor fd, boolean quietModeOnly, String[] args);
}
