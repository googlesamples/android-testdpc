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

package com.google.android.setupdesign.transition.support;

import static com.google.android.setupdesign.transition.TransitionHelper.CONFIG_TRANSITION_SHARED_X_AXIS;
import static com.google.android.setupdesign.transition.TransitionHelper.getConfigTransitionType;

import android.annotation.TargetApi;
import android.app.Activity;
import android.os.Build;
import android.os.Build.VERSION_CODES;
import androidx.fragment.app.Fragment;
import android.util.Log;
import android.view.Window;
import androidx.annotation.Nullable;
import androidx.core.app.ActivityOptionsCompat;
import com.google.android.material.transition.platform.MaterialSharedAxis;
import com.google.android.setupcompat.partnerconfig.PartnerConfig;

/** Helper class for apply the transition to the pages which uses support library. */
public class TransitionHelper {

  private static final String TAG = "TransitionHelper";

  private TransitionHelper() {}

  /**
   * Apply the transition for going forward which is decided by partner resource {@link
   * PartnerConfig#CONFIG_TRANSITION_TYPE} and system property {@code setupwizard.transition_type}.
   * The default transition that will be applied is {@link
   * com.google.android.setupdesign.transition.TransitionHelper#CONFIG_TRANSITION_NONE}. The timing
   * to apply the transition is going forward from the previous {@link Fragment} to this, or going
   * forward from this {@link Fragment} to the next.
   */
  @TargetApi(VERSION_CODES.M)
  public static void applyForwardTransition(Fragment fragment) {
    if (Build.VERSION.SDK_INT >= VERSION_CODES.M) {
      if (CONFIG_TRANSITION_SHARED_X_AXIS == getConfigTransitionType(fragment.getContext())) {
        MaterialSharedAxis exitTransition =
            new MaterialSharedAxis(MaterialSharedAxis.X, /* forward= */ true);
        fragment.setExitTransition(exitTransition);

        MaterialSharedAxis enterTransition =
            new MaterialSharedAxis(MaterialSharedAxis.X, /* forward= */ true);
        fragment.setEnterTransition(enterTransition);
      } else {
        Log.w(TAG, "Not apply the forward transition for support lib's fragment.");
      }
    } else {
      Log.w(
          TAG,
          "Not apply the forward transition for support lib's fragment. The API is supported from"
              + " Android Sdk "
              + VERSION_CODES.M);
    }
  }

  /**
   * Apply the transition for going backward which is decided by partner resource {@link
   * PartnerConfig#CONFIG_TRANSITION_TYPE} and system property {@code setupwizard.transition_type}.
   * The default transition that will be applied is {@link
   * com.google.android.setupdesign.transition.TransitionHelper#CONFIG_TRANSITION_NONE}. The timing
   * to apply the transition is going backward from the next {@link Fragment} to this, or going
   * backward from this {@link Fragment} to the previous.
   */
  @TargetApi(VERSION_CODES.M)
  public static void applyBackwardTransition(Fragment fragment) {
    if (Build.VERSION.SDK_INT >= VERSION_CODES.M) {
      if (CONFIG_TRANSITION_SHARED_X_AXIS == getConfigTransitionType(fragment.getContext())) {
        MaterialSharedAxis returnTransition =
            new MaterialSharedAxis(MaterialSharedAxis.X, /* forward= */ false);
        fragment.setReturnTransition(returnTransition);

        MaterialSharedAxis reenterTransition =
            new MaterialSharedAxis(MaterialSharedAxis.X, /* forward= */ false);
        fragment.setReenterTransition(reenterTransition);
      } else {
        Log.w(TAG, "Not apply the backward transition for support lib's fragment.");
      }
    } else {
      Log.w(
          TAG,
          "Not apply the backward transition for support lib's fragment. The API is supported from"
              + " Android Sdk "
              + VERSION_CODES.M);
    }
  }

  /**
   * A wrapper method, create an {@link ActivityOptionsCompat} to transition between activities as
   * the {@link ActivityOptionsCompat} parameter of {@link
   * androidx.activity.result.ActivityResultLauncher#launch(I, ActivityOptionsCompat)} method.
   */
  @Nullable
  public static ActivityOptionsCompat makeActivityOptionsCompat(Activity activity) {
    ActivityOptionsCompat activityOptionsCompat = null;
    if (activity == null) {
      return activityOptionsCompat;
    }

    if (getConfigTransitionType(activity) == CONFIG_TRANSITION_SHARED_X_AXIS) {
      if (Build.VERSION.SDK_INT >= VERSION_CODES.LOLLIPOP) {
        if (activity.getWindow() != null
            && !activity.getWindow().hasFeature(Window.FEATURE_ACTIVITY_TRANSITIONS)) {
          Log.w(
              TAG,
              "The transition won't take effect due to NO FEATURE_ACTIVITY_TRANSITIONS feature");
        }

        activityOptionsCompat = ActivityOptionsCompat.makeSceneTransitionAnimation(activity);
      }
    }

    return activityOptionsCompat;
  }
}
