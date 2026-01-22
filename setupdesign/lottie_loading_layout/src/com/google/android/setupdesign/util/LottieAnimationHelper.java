/*
 * Copyright (C) 2022 The Android Open Source Project
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

package com.google.android.setupdesign.util;

import android.content.Context;
import android.content.res.Resources;
import android.graphics.Color;
import android.util.Log;
import androidx.annotation.NonNull;
import androidx.annotation.VisibleForTesting;
import com.airbnb.lottie.LottieAnimationView;
import com.airbnb.lottie.LottieProperty;
import com.airbnb.lottie.SimpleColorFilter;
import com.airbnb.lottie.model.KeyPath;
import com.airbnb.lottie.value.LottieValueCallback;
import com.google.android.setupcompat.partnerconfig.PartnerConfig;
import com.google.android.setupcompat.partnerconfig.PartnerConfigHelper;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

/** A helper to help apply color on lottie animation */
public class LottieAnimationHelper {

  private static final String TAG = "LottieAnimationHelper";

  private static LottieAnimationHelper instance = null;

  @VisibleForTesting public final Map<String, Integer> colorResourceMapping;

  public static LottieAnimationHelper get() {
    if (instance == null) {
      instance = new LottieAnimationHelper();
    }
    return instance;
  }

  private LottieAnimationHelper() {
    colorResourceMapping = new HashMap<>();
  }

  /**
   * The color resource is from PartnerConfig, which is a string array and each string will be
   * {key_path_name}:@{color_reference} or {key_path_name}:{color code}
   */
  public void applyColor(
      @NonNull Context context, LottieAnimationView lottieView, PartnerConfig partnerConfig) {
    applyColor(
        context,
        lottieView,
        PartnerConfigHelper.get(context).getStringArray(context, partnerConfig));
  }

  /**
   * The color resource is from list of string and each string will be
   * {key_path_name}:@{color_reference} or {key_path_name}:#{color code}
   */
  public void applyColor(
      @NonNull Context context, LottieAnimationView lottieView, List<String> colorMappings) {
    applyColor(context, lottieView, parseColorMapping(context, colorMappings));
  }

  /**
   * The color resource is from a color mapping table and the key is the keypath, and value is color
   * Integer.
   */
  public void applyColor(
      @NonNull Context context,
      LottieAnimationView lottieView,
      Map<KeyPath, Integer> colorMappings) {
    for (KeyPath keyPath : colorMappings.keySet()) {
      lottieView.addValueCallback(
          keyPath,
          LottieProperty.COLOR_FILTER,
          new LottieValueCallback<>(new SimpleColorFilter(colorMappings.get(keyPath))));
    }
  }

  private Map<KeyPath, Integer> parseColorMapping(
      @NonNull Context context, List<String> colorMappings) {
    Map<KeyPath, Integer> customizationMap = new HashMap<>();
    for (String colorMapping : colorMappings) {
      String[] splitItem = colorMapping.split(":");
      if (splitItem.length == 2) {
        if (splitItem[1].charAt(0) == '#') { // color code
          try {
            customizationMap.put(
                new KeyPath("**", splitItem[0], "**"), Color.parseColor(splitItem[1]));
          } catch (IllegalArgumentException exception) {
            Log.e(TAG, "Unknown color, value=" + colorMapping);
          }
        } else if (splitItem[1].charAt(0) == '@') { // color resource
          int colorResourceId;
          if (colorResourceMapping.containsKey(splitItem[1])) {
            colorResourceId = colorResourceMapping.get(splitItem[1]);
          } else {
            colorResourceId =
                context
                    .getResources()
                    .getIdentifier(splitItem[1].substring(1), "color", context.getPackageName());
            colorResourceMapping.put(splitItem[1], colorResourceId);
          }
          try {
            customizationMap.put(
                new KeyPath("**", splitItem[0], "**"),
                context.getResources().getColor(colorResourceId, null));
          } catch (Resources.NotFoundException exception) {
            Log.e(TAG, "Resource Not found, resource value=" + colorMapping);
          }
        } else {
          Log.w(TAG, "incorrect format customization, value=" + colorMapping);
        }
      } else {
        Log.w(TAG, "incorrect format customization, value=" + colorMapping);
      }
    }
    return customizationMap;
  }
}
