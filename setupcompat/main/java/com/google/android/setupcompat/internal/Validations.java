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

/** Commonly used validations and preconditions. */
public final class Validations {

  /**
   * Asserts that the {@code length} is in the expected range.
   *
   * @throws IllegalArgumentException if {@code input}'s length is than {@code minLength} or
   *     greather than {@code maxLength}.
   */
  public static void assertLengthInRange(int length, String name, int minLength, int maxLength) {
    Preconditions.checkArgument(
        length <= maxLength && length >= minLength,
        String.format("Length of %s should be in the range [%s-%s]", name, minLength, maxLength));
  }

  /**
   * Asserts that the {@code input}'s length is in the expected range.
   *
   * @throws NullPointerException if {@code input} is null.
   * @throws IllegalArgumentException if {@code input}'s length is than {@code minLength} or
   *     greather than {@code maxLength}.
   */
  public static void assertLengthInRange(String input, String name, int minLength, int maxLength) {
    Preconditions.checkNotNull(input, String.format("%s cannot be null.", name));
    assertLengthInRange(input.length(), name, minLength, maxLength);
  }

  private Validations() {
    throw new AssertionError("Should not be instantiated");
  }
}
