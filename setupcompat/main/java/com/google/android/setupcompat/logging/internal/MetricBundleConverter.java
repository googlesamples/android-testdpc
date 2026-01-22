package com.google.android.setupcompat.logging.internal;

import android.os.Bundle;
import com.google.android.setupcompat.logging.CustomEvent;
import com.google.android.setupcompat.logging.MetricKey;
import com.google.android.setupcompat.logging.ScreenKey;
import com.google.android.setupcompat.logging.SetupMetric;
import com.google.android.setupcompat.logging.internal.SetupMetricsLoggingConstants.MetricBundleKeys;

/** Collection of helper methods for reading and writing {@link CustomEvent}, {@link MetricKey}. */
public final class MetricBundleConverter {

  public static Bundle createBundleForLogging(CustomEvent customEvent) {
    Bundle bundle = new Bundle();
    bundle.putParcelable(MetricBundleKeys.CUSTOM_EVENT_BUNDLE, CustomEvent.toBundle(customEvent));
    return bundle;
  }

  public static Bundle createBundleForLoggingCounter(MetricKey counterName, int times) {
    Bundle bundle = new Bundle();
    bundle.putParcelable(MetricBundleKeys.METRIC_KEY_BUNDLE, MetricKey.fromMetricKey(counterName));
    bundle.putInt(MetricBundleKeys.COUNTER_INT, times);
    return bundle;
  }

  public static Bundle createBundleForLoggingTimer(MetricKey timerName, long timeInMillis) {
    Bundle bundle = new Bundle();
    bundle.putParcelable(MetricBundleKeys.METRIC_KEY_BUNDLE, MetricKey.fromMetricKey(timerName));
    bundle.putLong(MetricBundleKeys.TIME_MILLIS_LONG, timeInMillis);
    return bundle;
  }

  public static Bundle createBundleForLoggingSetupMetric(ScreenKey screenKey, SetupMetric metric) {
    Bundle bundle = new Bundle();
    bundle.putParcelable(MetricBundleKeys.SCREEN_KEY_BUNDLE, ScreenKey.toBundle(screenKey));
    bundle.putParcelable(MetricBundleKeys.SETUP_METRIC_BUNDLE, SetupMetric.toBundle(metric));
    return bundle;
  }

  private MetricBundleConverter() {
    throw new AssertionError("Cannot instantiate MetricBundleConverter");
  }
}
