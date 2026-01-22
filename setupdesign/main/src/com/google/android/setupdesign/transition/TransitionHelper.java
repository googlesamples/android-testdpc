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

package com.google.android.setupdesign.transition;

import android.annotation.TargetApi;
import android.app.Activity;
import android.app.ActivityOptions;
import android.app.Fragment;
import android.content.ActivityNotFoundException;
import android.content.Context;
import android.content.Intent;
import android.content.res.TypedArray;
import android.os.Build.VERSION_CODES;
import android.os.Bundle;
import android.util.Log;
import androidx.annotation.IntDef;
import androidx.annotation.Nullable;
import androidx.annotation.VisibleForTesting;
import com.google.android.setupcompat.partnerconfig.PartnerConfig;
import com.google.android.setupcompat.partnerconfig.PartnerConfigHelper;
import com.google.android.setupcompat.util.BuildCompatUtils;
import com.google.android.setupdesign.R;
import com.google.android.setupdesign.util.ThemeHelper;
import com.google.errorprone.annotations.InlineMe;
import java.lang.annotation.Retention;
import java.lang.annotation.RetentionPolicy;

/** Helper class for apply the transition to the pages which uses platform version. */
public class TransitionHelper {

  private static final String TAG = "TransitionHelper";

  /*
   * In Setup Wizard, all Just-a-sec style screens (i.e. screens that has an indeterminate
   * progress bar and automatically finishes itself), should do a cross-fade when entering or
   * exiting the screen. For all other screens, the transition should be a slide-in-from-right
   * or customized.
   *
   * We use two different ways to override the transitions. The first is calling
   * overridePendingTransition in code, and the second is using windowAnimationStyle in the theme.
   * They have the following priority when framework is figuring out what transition to use:
   * 1. overridePendingTransition, entering activity (highest priority)
   * 2. overridePendingTransition, exiting activity
   * 3. windowAnimationStyle, entering activity
   * 4. windowAnimationStyle, exiting activity
   *
   * This is why, in general, overridePendingTransition is used to specify the fade animation,
   * while windowAnimationStyle is used to specify the slide transition. This way fade animation
   * will take priority over the slide animation.
   *
   * Below are types of animation when switching activities. These are return values for
   * {@link #getTransition()}. Each of these values represents 4 animations: (backward exit,
   * backward enter, forward exit, forward enter).
   *
   * We override the transition in the following flow
   * +--------------+-------------------------+--------------------------+
   * |              | going forward           | going backward           |
   * +--------------+-------------------------+--------------------------+
   * | old activity | startActivity(OnResult) | onActivityResult         |
   * +--------------+-------------------------+--------------------------+
   * | new activity | onStart                 | finish (RESULT_CANCELED) |
   * +--------------+-------------------------+--------------------------+
   */

  /** The constant of transition type. */
  @Retention(RetentionPolicy.SOURCE)
  @IntDef({
    TRANSITION_NONE,
    TRANSITION_NO_OVERRIDE,
    TRANSITION_FRAMEWORK_DEFAULT,
    TRANSITION_SLIDE,
    TRANSITION_FADE,
    TRANSITION_FRAMEWORK_DEFAULT_PRE_P,
    TRANSITION_CAPTIVE,
    TRANSITION_FADE_THROUGH,
  })
  public @interface TransitionType {}

  /** No transition, as in overridePendingTransition(0, 0). */
  public static final int TRANSITION_NONE = -1;

  /**
   * No override. If this is specified as the transition, overridePendingTransition will not be
   * called.
   */
  public static final int TRANSITION_NO_OVERRIDE = 0;

  /**
   * Override the transition to the framework default. This values are read from {@link
   * android.R.style#Animation_Activity}.
   */
  public static final int TRANSITION_FRAMEWORK_DEFAULT = 1;

  /** Override the transition to a slide-in-from-right (or from-left for RTL locales). */
  public static final int TRANSITION_SLIDE = 2;

  /**
   * Override the transition to fade in the new activity, while keeping the old activity. Setup
   * wizard does not use cross fade to avoid the bright-dim-bright effect when transitioning between
   * two screens that look similar.
   */
  public static final int TRANSITION_FADE = 3;

  /** Override the transition to the old framework default pre P. */
  public static final int TRANSITION_FRAMEWORK_DEFAULT_PRE_P = 4;

  /**
   * Override the transition to the specific transition and the transition type will depends on the
   * partner resource.
   */
  // TODO: Add new partner resource to determine which transition type would be apply.
  public static final int TRANSITION_CAPTIVE = 5;

  /** Override the transition to a fade-through-from-right (or from-left for RTL locales). */
  public static final int TRANSITION_FADE_THROUGH = 6;

  /**
   * No override. If this is specified as the transition, the enter/exit transition of the window
   * will not be set and keep original behavior.
   */
  public static final int CONFIG_TRANSITION_NONE = 0;

  /** Override the transition to the specific type that will depend on the partner resource. */
  public static final int CONFIG_TRANSITION_SHARED_X_AXIS = 1;

  /**
   * Passed in an intent as EXTRA_ACTIVITY_OPTIONS. This is the {@link ActivityOptions} of the
   * transition used in {@link Activity#startActivity} or {@link Activity#startActivityForResult}.
   *
   * @deprecated Deprecated to use CONFIG_TRANSITION_SHARED_X_AXIS transition, so it never have
   *     activity options input.
   */
  @Deprecated public static final String EXTRA_ACTIVITY_OPTIONS = "sud:activity_options";

  /** A flag to avoid the {@link Activity#finish} been called more than once. */
  @VisibleForTesting static boolean isFinishCalled = false;

  /** A flag to avoid the {@link Activity#startActivity} called more than once. */
  @VisibleForTesting static boolean isStartActivity = false;

  /** A flag to avoid the {@link Activity#startActivityForResult} called more than once. */
  @VisibleForTesting static boolean isStartActivityForResult = false;

  private TransitionHelper() {}

  /**
   * Apply the transition for going forward which is decided by {@code Animation.SudWindowAnimation}
   * theme if the API level is equal or higher than {@link android.os.Build.VERSION_CODES#U}.
   *
   * <p>Otherwise, apply the transition for going forward which is decided by partner resource
   * {@link PartnerConfig#CONFIG_TRANSITION_TYPE} and system property {@code
   * setupwizard.transition_type} if the API level is equal or lower than {@link
   * android.os.Build.VERSION_CODES#T}. The default transition that will be applied is {@link
   * #TRANSITION_SLIDE}.
   *
   * <p>The timing to apply the transition is going forward from the previous activity to this, or
   * going forward from this activity to the next.
   *
   * <p>For example, in the flow below, the forward transitions will be applied to all arrows
   * pointing to the right. Previous screen --> This screen --> Next screen
   */
  @TargetApi(VERSION_CODES.LOLLIPOP)
  public static void applyForwardTransition(Activity activity) {
    applyForwardTransition(activity, TRANSITION_CAPTIVE);
  }

  /**
   * Apply the transition for going forward which is decided by partner resource {@link
   * PartnerConfig#CONFIG_TRANSITION_TYPE} and system property {@code setupwizard.transition_type}.
   * The default transition that will be applied is {@link #CONFIG_TRANSITION_NONE}. The timing to
   * apply the transition is going forward from the previous {@link Fragment} to this, or going
   * forward from this {@link Fragment} to the next.
   *
   * @deprecated Deprecated to use CONFIG_TRANSITION_SHARED_X_AXIS transition, so it never have
   *     activity options input, should start the activity directly.
   */
  @TargetApi(VERSION_CODES.M)
  @Deprecated
  public static void applyForwardTransition(Fragment fragment) {
    // Do nothing
  }

  /**
   * Apply the transition for going forward which is decided by {@code Animation.SudWindowAnimation}
   * theme if the API level is equal or higher than {@link android.os.Build.VERSION_CODES#U}.
   *
   * <p>Otherwise, apply the transition for going forward which is decided by the argument {@code
   * transitionId} if the API level is equal or lower than {@link android.os.Build.VERSION_CODES#T}.
   *
   * <p>The timing to apply the transition is going forward from the previous activity to this, or
   * going forward from this activity to the next.
   */
  @TargetApi(VERSION_CODES.LOLLIPOP)
  public static void applyForwardTransition(Activity activity, @TransitionType int transitionId) {
    applyForwardTransition(activity, transitionId, /* useClientTransitionSettings= */ false);
  }

  /**
   * Apply the transition for going forward which is decided by {@code Animation.SudWindowAnimation}
   * theme if the API level is equal or higher than {@link android.os.Build.VERSION_CODES#U}, and
   * argument {@code useClientTransitionSettings} is false, and System property {@code
   * suw_apply_glif_theme_controlled_transition} is true, and {@code TRANSITION_FADE_THOUGH}
   * transition is not specified.
   *
   * <p>Otherwise, apply the transition for going forward which is decided by the argument {@code
   * transitionId}, {@code shared_x_axis_activity} transition is used only when {@code
   * TRANSITION_FADE_TROUGH} transition is specified, and System property {@code *
   * suw_apply_glif_theme_controlled_transition} is true, and the API level is equal or more than
   * {@link android.os.Build.VERSION_CODES#U}, other {@code transitionId} can be specified if the
   * API level is equal or lower than {@link android.os.Build.VERSION_CODES#T}, or argument {@code
   * useClientTransitionSettings} is true, or System property {@code
   * suw_apply_glif_theme_controlled_transition} is false. The default transition that will be
   * applied is {@link #TRANSITION_SLIDE}.
   *
   * <p>The timing to apply the transition is going forward from the previous activity to this, or
   * going forward from this activity to the next.
   *
   * <p>For example, in the flow below, the forward transitions will be applied to all arrows
   * pointing to the right. Previous screen --> This screen --> Next screen
   */
  @TargetApi(VERSION_CODES.LOLLIPOP)
  public static void applyForwardTransition(
      Activity activity, @TransitionType int transitionId, boolean useClientTransitionSettings) {
    if (BuildCompatUtils.isAtLeastU()
        && !useClientTransitionSettings
        && PartnerConfigHelper.isGlifThemeControlledTransitionApplied(activity)
        && transitionId != TRANSITION_FADE_THROUGH) {
      // Do nothing
    } else if (BuildCompatUtils.isAtLeastU() && transitionId == TRANSITION_FADE_THROUGH) {
      if (PartnerConfigHelper.isGlifThemeControlledTransitionApplied(activity)) {
        int openEnterTransition =
            ThemeHelper.shouldApplyDynamicColor(activity)
                ? R.anim.shared_x_axis_activity_open_enter_dynamic_color
                : R.anim.shared_x_axis_activity_open_enter;
        activity.overridePendingTransition(
            openEnterTransition, R.anim.shared_x_axis_activity_open_exit);
      } else {
        activity.overridePendingTransition(R.anim.sud_slide_next_in, R.anim.sud_slide_next_out);
      }
    } else if (transitionId == TRANSITION_SLIDE) {
      activity.overridePendingTransition(R.anim.sud_slide_next_in, R.anim.sud_slide_next_out);
    } else if (transitionId == TRANSITION_FADE) {
      activity.overridePendingTransition(android.R.anim.fade_in, R.anim.sud_stay);
    } else if (transitionId == TRANSITION_FRAMEWORK_DEFAULT) {
      TypedArray typedArray =
          activity.obtainStyledAttributes(
              android.R.style.Animation_Activity,
              new int[] {
                android.R.attr.activityOpenEnterAnimation, android.R.attr.activityOpenExitAnimation
              });
      activity.overridePendingTransition(
          typedArray.getResourceId(/* index= */ 0, /* defValue= */ 0),
          typedArray.getResourceId(/* index= */ 1, /* defValue= */ 0));
      typedArray.recycle();
    } else if (transitionId == TRANSITION_FRAMEWORK_DEFAULT_PRE_P) {
      activity.overridePendingTransition(
          R.anim.sud_pre_p_activity_open_enter, R.anim.sud_pre_p_activity_open_exit);
    } else if (transitionId == TRANSITION_NONE) {
      // For TRANSITION_NONE, turn off the transition
      activity.overridePendingTransition(/* enterAnim= */ 0, /* exitAnim= */ 0);
    }
    // For TRANSITION_NO_OVERRIDE or other values, do not override the transition
  }

  /**
   * Apply the transition for going backward which is decided by {@code
   * Animation.SudWindowAnimation} theme if the API level is equal or higher than {@link
   * android.os.Build.VERSION_CODES#U}.
   *
   * <p>Otherwise, apply the transition for going backward which is decided by partner resource
   * {@link PartnerConfig#CONFIG_TRANSITION_TYPE} and system property {@code
   * setupwizard.transition_type} if the API level is equal or lower than {@link
   * android.os.Build.VERSION_CODES#T}. The default transition that will be applied is {@link
   * #TRANSITION_SLIDE}.
   *
   * <p>The timing to apply the transition is going backward from the next activity to this, or
   * going backward from this activity to the previous.
   *
   * <p>For example, in the flow below, the backward transitions will be applied to all arrows
   * pointing to the left. Previous screen <-- This screen <-- Next screen.
   */
  @TargetApi(VERSION_CODES.LOLLIPOP)
  public static void applyBackwardTransition(Activity activity) {
    applyBackwardTransition(activity, TRANSITION_CAPTIVE);
  }

  /**
   * Apply the transition for going backward which is decided by partner resource {@link
   * PartnerConfig#CONFIG_TRANSITION_TYPE} and system property {@code setupwizard.transition_type}.
   * The default transition that will be applied is {@link #CONFIG_TRANSITION_NONE}. The timing to
   * apply the transition is going backward from the next {@link Fragment} to this, or going
   * backward from this {@link Fragment} to the previous.
   *
   * @deprecated Deprecated to use CONFIG_TRANSITION_SHARED_X_AXIS transition, so it never have
   *     activity options input, should start the activity directly.
   */
  @TargetApi(VERSION_CODES.M)
  @Deprecated
  public static void applyBackwardTransition(Fragment fragment) {
    // Do nothing
  }

  /**
   * Apply the transition for going backward which is decided by {@code
   * Animation.SudWindowAnimation} theme if the API level is equal or higher than {@link
   * android.os.Build.VERSION_CODES#U}.
   *
   * <p>Otherwise, apply the transition for going backward which is decided by the argument {@code
   * transitionId} if the API level is equal or lower than {@link android.os.Build.VERSION_CODES#T}.
   *
   * <p>The timing to apply the transition is going backward from the next activity to this, or
   * going backward from this activity to the previous.
   */
  @TargetApi(VERSION_CODES.LOLLIPOP)
  public static void applyBackwardTransition(Activity activity, @TransitionType int transitionId) {
    applyBackwardTransition(activity, transitionId, /* useClientTransitionSettings= */ false);
  }

  /**
   * Apply the transition for going backward which is decided by {@code
   * Animation.SudWindowAnimation} theme if the API level is equal or higher than {@link
   * android.os.Build.VERSION_CODES#U}, and argument {@code useClientTransitionSettings} is false,
   * and System property {@code suw_apply_glif_theme_controlled_transition} is true, and {@code
   * TRANSITION_FADE_THOUGH} transition is not specified.
   *
   * <p>Otherwise, apply the transition for going backward which is decided by the argument {@code
   * transitionId}, {@code shared_x_axis_activity} transition is used only when {@code
   * TRANSITION_FADE_TROUGH} transition is specified, and System property {@code *
   * suw_apply_glif_theme_controlled_transition} is true, and the API level is equal or more than
   * {@link android.os.Build.VERSION_CODES#U}, other {@code transitionId} can be specified if the
   * API level is equal or lower than {@link android.os.Build.VERSION_CODES#T}, or argument {@code
   * useClientTransitionSettings} is true, or System property {@code
   * suw_apply_glif_theme_controlled_transition} is false. The default transition that will be
   * applied is {@link #TRANSITION_SLIDE}.
   *
   * <p>The timing to apply the transition is going backward from the next activity to this, or
   * going backward from this activity to the previous.
   *
   * <p>For example, in the flow below, the backward transitions will be applied to all arrows
   * pointing to the left. Previous screen <-- This screen <-- Next screen
   */
  @TargetApi(VERSION_CODES.LOLLIPOP)
  public static void applyBackwardTransition(
      Activity activity, @TransitionType int transitionId, boolean useClientTransitionSettings) {
    if (BuildCompatUtils.isAtLeastU()
        && !useClientTransitionSettings
        && PartnerConfigHelper.isGlifThemeControlledTransitionApplied(activity)
        && transitionId != TRANSITION_FADE_THROUGH) {
      // Do nothing
    } else if (BuildCompatUtils.isAtLeastU() && transitionId == TRANSITION_FADE_THROUGH) {
      if (PartnerConfigHelper.isGlifThemeControlledTransitionApplied(activity)) {
        int closeEnterTransition =
            ThemeHelper.shouldApplyDynamicColor(activity)
                ? R.anim.shared_x_axis_activity_close_enter_dynamic_color
                : R.anim.shared_x_axis_activity_close_enter;
        activity.overridePendingTransition(
            closeEnterTransition, R.anim.shared_x_axis_activity_close_exit);
      } else {
        activity.overridePendingTransition(R.anim.sud_slide_back_in, R.anim.sud_slide_back_out);
      }
    } else if (transitionId == TRANSITION_SLIDE) {
      activity.overridePendingTransition(R.anim.sud_slide_back_in, R.anim.sud_slide_back_out);
    } else if (transitionId == TRANSITION_FADE) {
      activity.overridePendingTransition(R.anim.sud_stay, android.R.anim.fade_out);
    } else if (transitionId == TRANSITION_FRAMEWORK_DEFAULT) {
      TypedArray typedArray =
          activity.obtainStyledAttributes(
              android.R.style.Animation_Activity,
              new int[] {
                android.R.attr.activityCloseEnterAnimation,
                android.R.attr.activityCloseExitAnimation
              });
      activity.overridePendingTransition(
          typedArray.getResourceId(/* index= */ 0, /* defValue= */ 0),
          typedArray.getResourceId(/* index= */ 1, /* defValue= */ 0));
      typedArray.recycle();
    } else if (transitionId == TRANSITION_FRAMEWORK_DEFAULT_PRE_P) {
      activity.overridePendingTransition(
          R.anim.sud_pre_p_activity_close_enter, R.anim.sud_pre_p_activity_close_exit);
    } else if (transitionId == TRANSITION_NONE) {
      // For TRANSITION_NONE, turn off the transition
      activity.overridePendingTransition(/* enterAnim= */ 0, /* exitAnim= */ 0);
    }
  }

  /**
   * A wrapper method, create an {@link android.app.ActivityOptions} to transition between
   * activities as the {@link ActivityOptions} parameter of {@link Activity#startActivity}.
   *
   * @throws IllegalArgumentException is thrown when {@code activity} or {@code intent} is null.
   * @throws android.content.ActivityNotFoundException if there was no {@link Activity} found to run
   *     the given Intent.
   * @deprecated Deprecated to use CONFIG_TRANSITION_SHARED_X_AXIS transition, so it never have
   *     activity options input, should start the activity directly.
   */
  @InlineMe(replacement = "activity.startActivity(intent)")
  @Deprecated
  public static void startActivityWithTransition(Activity activity, Intent intent) {
    activity.startActivity(intent);
  }

  /**
   * A wrapper method, create an {@link android.app.ActivityOptions} to transition between
   * activities as the {@link ActivityOptions} parameter of {@link Activity#startActivity}.
   *
   * @throws IllegalArgumentException is thrown when {@code activity} or {@code intent} is null.
   * @throws android.content.ActivityNotFoundException if there was no {@link Activity} found to run
   *     the given Intent.
   * @deprecated Deprecated to use CONFIG_TRANSITION_SHARED_X_AXIS transition, so it never have
   *     activity options input, should start the activity directly.
   */
  @Deprecated
  public static void startActivityWithTransition(
      Activity activity, Intent intent, Bundle overrideActivityOptions) {
    if (activity == null) {
      throw new IllegalArgumentException("Invalid activity=" + activity);
    }

    if (intent == null) {
      throw new IllegalArgumentException("Invalid intent=" + intent);
    }

    if ((intent.getFlags() & Intent.FLAG_ACTIVITY_NEW_TASK) == Intent.FLAG_ACTIVITY_NEW_TASK) {
      Log.e(
          TAG,
          "The transition won't take effect since the WindowManager does not allow override new"
              + " task transitions");
    }

    if (!isStartActivity) {
      isStartActivity = true;
      activity.startActivity(intent);
    }
    isStartActivity = false;
  }

  /**
   * A wrapper method, create an {@link android.app.ActivityOptions} to transition between
   * activities as the {@code activityOptions} parameter of {@link Activity#startActivityForResult}.
   *
   * @throws IllegalArgumentException is thrown when {@code activity} or {@code intent} is null.
   * @throws android.content.ActivityNotFoundException if there was no {@link Activity} found to run
   *     the given Intent.
   * @deprecated Deprecated to use CONFIG_TRANSITION_SHARED_X_AXIS transition, so it never have
   *     activity options input, should start the activity directly.
   */
  @InlineMe(replacement = "activity.startActivityForResult(intent, requestCode)")
  @Deprecated
  public static void startActivityForResultWithTransition(
      Activity activity, Intent intent, int requestCode) {
    activity.startActivityForResult(intent, requestCode);
  }

  /**
   * A wrapper method, create an {@link android.app.ActivityOptions} to transition between
   * activities as the {@code activityOptions} parameter of {@link Activity#startActivityForResult}.
   *
   * @throws IllegalArgumentException is thrown when {@code activity} or {@code intent} is null.
   * @throws android.content.ActivityNotFoundException if there was no {@link Activity} found to run
   *     the given Intent.
   * @deprecated Deprecated to use CONFIG_TRANSITION_SHARED_X_AXIS transition, so it never have
   *     activity options input, should start the activity directly.
   */
  @Deprecated
  public static void startActivityForResultWithTransition(
      Activity activity, Intent intent, int requestCode, Bundle overrideActivityOptions) {
    if (activity == null) {
      throw new IllegalArgumentException("Invalid activity=" + activity);
    }

    if (intent == null) {
      throw new IllegalArgumentException("Invalid intent=" + intent);
    }

    if ((intent.getFlags() & Intent.FLAG_ACTIVITY_NEW_TASK) == Intent.FLAG_ACTIVITY_NEW_TASK) {
      Log.e(
          TAG,
          "The transition won't take effect since the WindowManager does not allow override new"
              + " task transitions");
    }

    if (!isStartActivityForResult) {
      try {
        isStartActivityForResult = true;
        activity.startActivityForResult(intent, requestCode);
      } catch (ActivityNotFoundException e) {
        Log.w(TAG, "Activity not found when startActivityForResult with transition.");
        throw e;
      } finally {
        // Allow to start next activity.
        isStartActivityForResult = false;
      }
    }
  }

  /**
   * A wrapper method, calling {@link Activity#finishAfterTransition()} to trigger exit transition
   * when running in Android S and the transition type {link #CONFIG_TRANSITION_SHARED_X_AXIS}.
   *
   * @throws IllegalArgumentException is thrown when {@code activity} is null.
   */
  public static void finishActivity(Activity activity) {
    if (activity == null) {
      throw new IllegalArgumentException("Invalid activity=" + activity);
    }

    // Avoids finish been called more than once.
    if (!isFinishCalled) {
      isFinishCalled = true;
      Log.w(
          TAG,
          "Fallback to using Activity#finish() due to the"
              + " Activity#finishAfterTransition() is supported from Android Sdk "
              + VERSION_CODES.LOLLIPOP);
      activity.finish();
    }
      isFinishCalled = false;
  }

  /**
   * Returns the transition type from the {@link PartnerConfig#CONFIG_TRANSITION_TYPE} partner
   * resource on Android S, otherwise returns {@link #CONFIG_TRANSITION_NONE}.
   */
  public static int getConfigTransitionType(Context context) {
    return BuildCompatUtils.isAtLeastS() && ThemeHelper.shouldApplyExtendedPartnerConfig(context)
        ? PartnerConfigHelper.get(context)
            .getInteger(context, PartnerConfig.CONFIG_TRANSITION_TYPE, CONFIG_TRANSITION_NONE)
        : CONFIG_TRANSITION_NONE;
  }

  /**
   * A wrapper method, create a {@link Bundle} from {@link ActivityOptions} to transition between
   * Activities using cross-Activity scene animations. This {@link Bundle} that can be used with
   * {@link Context#startActivity(Intent, Bundle)} and related methods.
   *
   * <p>Example usage:
   *
   * <pre>{@code
   * Intent intent = new Intent("com.example.NEXT_ACTIVITY");
   * activity.startActivity(intent, TransitionHelper.makeActivityOptions(activity, intent, null);
   * }</pre>
   *
   * <p>Unexpected usage:
   *
   * <pre>{@code
   * Intent intent = new Intent("com.example.NEXT_ACTIVITY");
   * Intent intent2 = new Intent("com.example.NEXT_ACTIVITY");
   * activity.startActivity(intent, TransitionHelper.makeActivityOptions(activity, intent2, null);
   * }</pre>
   *
   * @deprecated Deprecated to use CONFIG_TRANSITION_SHARED_X_AXIS transition, so it never have
   *     activity options input, should start the activity directly.
   */
  @InlineMe(replacement = "null")
  @Nullable
  @Deprecated
  public static Bundle makeActivityOptions(Activity activity, Intent intent) {
    return null;
  }

  /**
   * A wrapper method, create a {@link Bundle} from {@link ActivityOptions} to transition between
   * Activities using cross-Activity scene animations. This {@link Bundle} that can be used with
   * {@link Context#startActivity(Intent, Bundle)} and related methods. When this {@code activity}
   * is a no UI activity(the activity doesn't inflate any layouts), you will need to pass the bundle
   * coming from previous UI activity as the {@link ActivityOptions}, otherwise, the transition
   * won't be take effect. The {@code overrideActivityOptionsFromIntent} is supporting this purpose
   * to return the {@link ActivityOptions} instead of creating from this no UI activity while the
   * transition is apply {@link #CONFIG_TRANSITION_SHARED_X_AXIS} config. Moreover, the
   * startActivity*WithTransition relative methods and {@link #makeActivityOptions} will put {@link
   * ActivityOptions} to the {@code intent} by default, you can get the {@link ActivityOptions}
   * which makes from previous activity by accessing {@link #EXTRA_ACTIVITY_OPTIONS} extra from
   * {@link Activity#getIntent()}.
   *
   * <p>Example usage of a no UI activity:
   *
   * <pre>{@code
   * Intent intent = new Intent("com.example.NEXT_ACTIVITY");
   * activity.startActivity(intent, TransitionHelper.makeActivityOptions(activity, intent, true);
   * }</pre>
   *
   * @deprecated Deprecated to use CONFIG_TRANSITION_SHARED_X_AXIS transition, so it never have
   *     activity options input, should start the activity directly.
   */
  @InlineMe(replacement = "null")
  @Nullable
  @Deprecated
  public static Bundle makeActivityOptions(
      Activity activity, Intent intent, boolean overrideActivityOptionsFromIntent) {
    return null;
  }
}
