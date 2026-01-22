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

import static java.util.concurrent.TimeUnit.NANOSECONDS;

import android.app.Activity;
import android.app.Fragment;
import android.app.FragmentManager;
import android.content.Context;
import android.os.Build.VERSION;
import android.os.Build.VERSION_CODES;
import android.os.PersistableBundle;
import android.util.Log;
import com.google.android.setupcompat.logging.CustomEvent;
import com.google.android.setupcompat.logging.MetricKey;
import com.google.android.setupcompat.logging.SetupMetricsLogger;
import com.google.android.setupcompat.util.WizardManagerHelper;

/** Fragment used to detect lifecycle of an activity for metrics logging. */
public class LifecycleFragment extends Fragment {
  private static final String LOG_TAG = LifecycleFragment.class.getSimpleName();
  private static final String FRAGMENT_ID = "lifecycle_monitor";

  private MetricKey metricKey;
  private long startInNanos;
  private long durationInNanos = 0;

  public LifecycleFragment() {
    setRetainInstance(true);
  }

  /**
   * Attaches the lifecycle fragment if it is not attached yet.
   *
   * @param activity the activity to detect lifecycle for.
   * @return fragment to monitor life cycle.
   */
  public static LifecycleFragment attachNow(Activity activity) {
    if (WizardManagerHelper.isAnySetupWizard(activity.getIntent())) {

      if (VERSION.SDK_INT > VERSION_CODES.M) {
        FragmentManager fragmentManager = activity.getFragmentManager();
        if (fragmentManager != null && !fragmentManager.isDestroyed()) {
          Fragment fragment = fragmentManager.findFragmentByTag(FRAGMENT_ID);
          if (fragment == null) {
            LifecycleFragment lifeCycleFragment = new LifecycleFragment();
            try {
              fragmentManager.beginTransaction().add(lifeCycleFragment, FRAGMENT_ID).commitNow();
              fragment = lifeCycleFragment;
            } catch (IllegalStateException e) {
              Log.e(
                  LOG_TAG,
                  "Error occurred when attach to Activity:" + activity.getComponentName(),
                  e);
            }
          } else if (!(fragment instanceof LifecycleFragment)) {
            Log.wtf(
                LOG_TAG,
                activity.getClass().getSimpleName() + " Incorrect instance on lifecycle fragment.");
            return null;
          }
          return (LifecycleFragment) fragment;
        }
      }
    }

    return null;
  }

  @Override
  public void onAttach(Context context) {
    super.onAttach(context);
    metricKey = MetricKey.get("ScreenDuration", getActivity());
  }

  @Override
  public void onDetach() {
    super.onDetach();
    SetupMetricsLogger.logDuration(getActivity(), metricKey, NANOSECONDS.toMillis(durationInNanos));
  }

  @Override
  public void onResume() {
    super.onResume();
    startInNanos = ClockProvider.timeInNanos();
    logScreenResume();
  }

  @Override
  public void onPause() {
    super.onPause();
    durationInNanos += (ClockProvider.timeInNanos() - startInNanos);
  }

  private void logScreenResume() {
    if (VERSION.SDK_INT >= VERSION_CODES.Q) {
      PersistableBundle bundle = new PersistableBundle();
      bundle.putLong("onScreenResume", System.nanoTime());
      SetupMetricsLogger.logCustomEvent(
          getActivity(),
          CustomEvent.create(MetricKey.get("ScreenActivity", getActivity()), bundle));
    }
  }
}
