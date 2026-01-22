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

import static java.lang.annotation.RetentionPolicy.SOURCE;

import android.annotation.TargetApi;
import android.os.Build.VERSION_CODES;
import android.os.PersistableBundle;
import androidx.annotation.StringDef;
import androidx.annotation.VisibleForTesting;
import java.lang.annotation.Retention;

/** Uses to log internal event footer button metric */
public class FooterBarMixinMetrics {
  @VisibleForTesting
  public static final String EXTRA_PRIMARY_BUTTON_VISIBILITY = "PrimaryButtonVisibility";

  @VisibleForTesting
  public static final String EXTRA_SECONDARY_BUTTON_VISIBILITY = "SecondaryButtonVisibility";

  @Retention(SOURCE)
  @StringDef({
    FooterButtonVisibility.UNKNOWN,
    FooterButtonVisibility.VISIBLE_USING_XML,
    FooterButtonVisibility.VISIBLE,
    FooterButtonVisibility.VISIBLE_USING_XML_TO_INVISIBLE,
    FooterButtonVisibility.VISIBLE_TO_INVISIBLE,
    FooterButtonVisibility.INVISIBLE_TO_VISIBLE,
    FooterButtonVisibility.INVISIBLE,
  })
  @VisibleForTesting
  public @interface FooterButtonVisibility {
    String UNKNOWN = "Unknown";
    String VISIBLE_USING_XML = "VisibleUsingXml";
    String VISIBLE = "Visible";
    String VISIBLE_USING_XML_TO_INVISIBLE = "VisibleUsingXml_to_Invisible";
    String VISIBLE_TO_INVISIBLE = "Visible_to_Invisible";
    String INVISIBLE_TO_VISIBLE = "Invisible_to_Visible";
    String INVISIBLE = "Invisible";
  }

  @FooterButtonVisibility String primaryButtonVisibility = FooterButtonVisibility.UNKNOWN;

  @FooterButtonVisibility String secondaryButtonVisibility = FooterButtonVisibility.UNKNOWN;

  /** Creates a metric object for metric logging */
  public FooterBarMixinMetrics() {}

  /** Gets initial state visibility */
  @FooterButtonVisibility
  public String getInitialStateVisibility(boolean isVisible, boolean isUsingXml) {
    @FooterButtonVisibility String visibility;

    if (isVisible) {
      visibility =
          isUsingXml ? FooterButtonVisibility.VISIBLE_USING_XML : FooterButtonVisibility.VISIBLE;
    } else {
      visibility = FooterButtonVisibility.INVISIBLE;
    }

    return visibility;
  }

  /** Saves primary footer button visibility when initial state */
  public void logPrimaryButtonInitialStateVisibility(boolean isVisible, boolean isUsingXml) {
    primaryButtonVisibility =
        primaryButtonVisibility.equals(FooterButtonVisibility.UNKNOWN)
            ? getInitialStateVisibility(isVisible, isUsingXml)
            : primaryButtonVisibility;
  }

  /** Saves secondary footer button visibility when initial state */
  public void logSecondaryButtonInitialStateVisibility(boolean isVisible, boolean isUsingXml) {
    secondaryButtonVisibility =
        secondaryButtonVisibility.equals(FooterButtonVisibility.UNKNOWN)
            ? getInitialStateVisibility(isVisible, isUsingXml)
            : secondaryButtonVisibility;
  }

  /** Saves footer button visibility when finish state */
  public void updateButtonVisibility(
      boolean isPrimaryButtonVisible, boolean isSecondaryButtonVisible) {
    primaryButtonVisibility =
        updateButtonVisibilityState(primaryButtonVisibility, isPrimaryButtonVisible);
    secondaryButtonVisibility =
        updateButtonVisibilityState(secondaryButtonVisibility, isSecondaryButtonVisible);
  }

  @FooterButtonVisibility
  static String updateButtonVisibilityState(
      @FooterButtonVisibility String originalVisibility, boolean isVisible) {
    if (!FooterButtonVisibility.VISIBLE_USING_XML.equals(originalVisibility)
        && !FooterButtonVisibility.VISIBLE.equals(originalVisibility)
        && !FooterButtonVisibility.INVISIBLE.equals(originalVisibility)) {
      throw new IllegalStateException("Illegal visibility state: " + originalVisibility);
    }

    if (isVisible && FooterButtonVisibility.INVISIBLE.equals(originalVisibility)) {
      return FooterButtonVisibility.INVISIBLE_TO_VISIBLE;
    } else if (!isVisible) {
      if (FooterButtonVisibility.VISIBLE_USING_XML.equals(originalVisibility)) {
        return FooterButtonVisibility.VISIBLE_USING_XML_TO_INVISIBLE;
      } else if (FooterButtonVisibility.VISIBLE.equals(originalVisibility)) {
        return FooterButtonVisibility.VISIBLE_TO_INVISIBLE;
      }
    }
    return originalVisibility;
  }

  /** Returns metrics data for logging */
  @TargetApi(VERSION_CODES.Q)
  public PersistableBundle getMetrics() {
    PersistableBundle persistableBundle = new PersistableBundle();
    persistableBundle.putString(EXTRA_PRIMARY_BUTTON_VISIBILITY, primaryButtonVisibility);
    persistableBundle.putString(EXTRA_SECONDARY_BUTTON_VISIBILITY, secondaryButtonVisibility);
    return persistableBundle;
  }
}
