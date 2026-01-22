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

import android.annotation.TargetApi;
import android.os.BaseBundle;
import android.os.Build.VERSION_CODES;
import android.os.Bundle;
import android.os.PersistableBundle;
import android.util.ArrayMap;
import com.google.android.setupcompat.util.Logger;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collections;
import java.util.List;

/** Contains utility methods related to {@link PersistableBundle}. */
@TargetApi(VERSION_CODES.LOLLIPOP_MR1)
public final class PersistableBundles {

  private static final Logger LOG = new Logger("PersistableBundles");

  /**
   * Merges two or more {@link PersistableBundle}. Ensures no conflict of keys occurred during
   * merge.
   *
   * @return Returns a new {@link PersistableBundle} that contains all the data from {@code
   *     firstBundle}, {@code nextBundle} and {@code others}.
   */
  public static PersistableBundle mergeBundles(
      PersistableBundle firstBundle, PersistableBundle nextBundle, PersistableBundle... others) {
    List<PersistableBundle> allBundles = new ArrayList<>();
    allBundles.addAll(Arrays.asList(firstBundle, nextBundle));
    Collections.addAll(allBundles, others);

    PersistableBundle result = new PersistableBundle();
    for (PersistableBundle bundle : allBundles) {
      for (String key : bundle.keySet()) {
        Preconditions.checkArgument(
            !result.containsKey(key),
            String.format("Found duplicate key [%s] while attempting to merge bundles.", key));
      }
      result.putAll(bundle);
    }

    return result;
  }

  /** Returns a {@link Bundle} that contains all the values from {@code persistableBundle}. */
  public static Bundle toBundle(PersistableBundle persistableBundle) {
    Bundle bundle = new Bundle();
    bundle.putAll(persistableBundle);
    return bundle;
  }

  /**
   * Returns a {@link PersistableBundle} that contains values from {@code bundle} that are supported
   * by the logging API. Un-supported value types are dropped.
   */
  public static PersistableBundle fromBundle(Bundle bundle) {
    PersistableBundle to = new PersistableBundle();
    ArrayMap<String, Object> map = toMap(bundle);
    for (String key : map.keySet()) {
      Object value = map.get(key);
      if (value instanceof Long) {
        to.putLong(key, (Long) value);
      } else if (value instanceof Integer) {
        to.putInt(key, (Integer) value);
      } else if (value instanceof Double) {
        to.putDouble(key, (Double) value);
      } else if (value instanceof Boolean) {
        to.putBoolean(key, (Boolean) value);
      } else if (value instanceof String) {
        to.putString(key, (String) value);
      } else {
        throw new AssertionError(String.format("Missing put* for valid data type? = %s", value));
      }
    }
    return to;
  }

  /** Returns {@code true} if {@code left} contains same set of values as {@code right}. */
  public static boolean equals(PersistableBundle left, PersistableBundle right) {
    return (left == right) || toMap(left).equals(toMap(right));
  }

  /** Asserts that {@code persistableBundle} contains only supported data types. */
  public static PersistableBundle assertIsValid(PersistableBundle persistableBundle) {
    Preconditions.checkNotNull(persistableBundle, "PersistableBundle cannot be null!");
    for (String key : persistableBundle.keySet()) {
      Object value = persistableBundle.get(key);
      Preconditions.checkArgument(
          isSupportedDataType(value),
          String.format("Unknown/unsupported data type [%s] for key %s", value, key));
    }
    return persistableBundle;
  }

  /**
   * Returns a new {@link ArrayMap} that contains values from {@code bundle} that are supported by
   * the logging API.
   */
  private static ArrayMap<String, Object> toMap(BaseBundle baseBundle) {
    if (baseBundle == null || baseBundle.isEmpty()) {
      return new ArrayMap<>(0);
    }

    ArrayMap<String, Object> map = new ArrayMap<>(baseBundle.size());
    for (String key : baseBundle.keySet()) {
      Object value = baseBundle.get(key);
      if (!isSupportedDataType(value)) {
        LOG.w(String.format("Unknown/unsupported data type [%s] for key %s", value, key));
        continue;
      }
      map.put(key, baseBundle.get(key));
    }
    return map;
  }

  private static boolean isSupportedDataType(Object value) {
    return value instanceof Integer
        || value instanceof Long
        || value instanceof Double
        || value instanceof Float
        || value instanceof String
        || value instanceof Boolean;
  }

  private PersistableBundles() {
    throw new AssertionError("Should not be instantiated");
  }
}
