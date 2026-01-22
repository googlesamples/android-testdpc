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

package com.google.android.setupcompat.logging.internal;

import android.content.Context;
import androidx.annotation.IntDef;
import androidx.annotation.StringDef;
import com.google.android.setupcompat.logging.MetricKey;
import com.google.android.setupcompat.logging.ScreenKey;
import com.google.android.setupcompat.logging.SetupMetric;
import java.lang.annotation.Retention;
import java.lang.annotation.RetentionPolicy;

/** Constant values used by {@link com.google.android.setupcompat.logging.SetupMetricsLogger}. */
public interface SetupMetricsLoggingConstants {

  /** Enumeration of supported metric types logged to SetupWizard. */
  @Retention(RetentionPolicy.SOURCE)
  @IntDef({
      MetricType.CUSTOM_EVENT,
      MetricType.DURATION_EVENT,
      MetricType.COUNTER_EVENT,
      MetricType.SETUP_COLLECTION_EVENT,
      MetricType.INTERNAL})
  @interface MetricType {
    /**
     * MetricType constant used when logging {@link
     * com.google.android.setupcompat.logging.CustomEvent}.
     */
    int CUSTOM_EVENT = 1;
    /**
     * MetricType constant used when logging {@link com.google.android.setupcompat.logging.Timer}.
     */
    int DURATION_EVENT = 2;

    /**
     * MetricType constant used when logging counter value using {@link
     * com.google.android.setupcompat.logging.SetupMetricsLogger#logCounter(Context, MetricKey,
     * int)}.
     */
    int COUNTER_EVENT = 3;

    /**
     * MetricType constant used when logging setup metric using {@link
     * com.google.android.setupcompat.logging.SetupMetricsLogger#logMetrics(Context, ScreenKey,
     * SetupMetric...)}.
     */
    int SETUP_COLLECTION_EVENT = 4;

    /** MetricType constant used for internal logging purposes. */
    int INTERNAL = 100;
  }

  /**
   * Enumeration of supported EventType of {@link MetricType#SETUP_COLLECTION_EVENT} logged to
   * SetupWizard. (go/suw-metrics-collection-api)
   */
  @Retention(RetentionPolicy.SOURCE)
  @IntDef({
      EventType.UNKNOWN,
      EventType.IMPRESSION,
      EventType.OPT_IN,
      EventType.WAITING_START,
      EventType.WAITING_END,
      EventType.ERROR,
  })
  @interface EventType {
    int UNKNOWN = 1;
    int IMPRESSION = 2;
    int OPT_IN = 3;
    int WAITING_START = 4;
    int WAITING_END = 5;
    int ERROR = 6;
  }

  /** Keys of the bundle used while logging data to SetupWizard. */
  @Retention(RetentionPolicy.SOURCE)
  @StringDef({
    MetricBundleKeys.METRIC_KEY,
    MetricBundleKeys.METRIC_KEY_BUNDLE,
    MetricBundleKeys.CUSTOM_EVENT,
    MetricBundleKeys.CUSTOM_EVENT_BUNDLE,
    MetricBundleKeys.TIME_MILLIS_LONG,
    MetricBundleKeys.COUNTER_INT,
    MetricBundleKeys.SCREEN_KEY_BUNDLE,
    MetricBundleKeys.SETUP_METRIC_BUNDLE,
  })
  @interface MetricBundleKeys {
    /**
     * {@link MetricKey} of the data being logged. This will be set when {@code metricType} is
     * either {@link MetricType#COUNTER_EVENT} or {@link MetricType#DURATION_EVENT}.
     *
     * @deprecated Use {@link #METRIC_KEY_BUNDLE} instead.
     */
    @Deprecated String METRIC_KEY = "MetricKey";

    /**
     * This key will be used when {@code metricType} is {@link MetricType#CUSTOM_EVENT} with the
     * value being a parcelable of type {@link com.google.android.setupcompat.logging.CustomEvent}.
     *
     * @deprecated Use {@link #CUSTOM_EVENT_BUNDLE} instead.
     */
    @Deprecated String CUSTOM_EVENT = "CustomEvent";

    /**
     * This key will be set when {@code metricType} is {@link MetricType#DURATION_EVENT} with the
     * value of type {@code long} representing the {@code duration} in milliseconds for the given
     * {@link MetricKey}.
     */
    String TIME_MILLIS_LONG = "timeMillis";

    /**
     * This key will be set when {@code metricType} is {@link MetricType#COUNTER_EVENT} with the
     * value of type {@code int} representing the {@code counter} value logged for the given {@link
     * MetricKey}.
     */
    String COUNTER_INT = "counter";

    /**
     * {@link MetricKey} of the data being logged. This will be set when {@code metricType} is
     * either {@link MetricType#COUNTER_EVENT} or {@link MetricType#DURATION_EVENT}.
     */
    String METRIC_KEY_BUNDLE = "MetricKey_bundle";

    /**
     * This key will be used when {@code metricType} is {@link MetricType#CUSTOM_EVENT} with the
     * value being a Bundle which can be used to read {@link
     * com.google.android.setupcompat.logging.CustomEvent}.
     */
    String CUSTOM_EVENT_BUNDLE = "CustomEvent_bundle";

    /**
     * This key will be used when {@code metricType} is {@link MetricType#SETUP_COLLECTION_EVENT}
     * with the value being a Bundle which can be used to read {@link ScreenKey}
     */
    String SCREEN_KEY_BUNDLE = "ScreenKey_bundle";

    /**
     * This key will be used when {@code metricType} is {@link MetricType#SETUP_COLLECTION_EVENT}
     * with the value being a Bundle which can be used to read {@link SetupMetric}
     */
    String SETUP_METRIC_BUNDLE = "SetupMetric_bundle";
  }
}
