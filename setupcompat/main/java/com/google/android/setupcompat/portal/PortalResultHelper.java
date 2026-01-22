/*
 * Copyright (C) 2021 The Android Open Source Project
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

package com.google.android.setupcompat.portal;

import android.os.Bundle;

public class PortalResultHelper {

  public static final String RESULT_BUNDLE_KEY_RESULT = "Result";
  public static final String RESULT_BUNDLE_KEY_ERROR = "Error";

  public static boolean isSuccess(Bundle bundle) {
    return bundle.getBoolean(RESULT_BUNDLE_KEY_RESULT, false);
  }

  public static String getErrorMessage(Bundle bundle) {
    return bundle.getString(RESULT_BUNDLE_KEY_ERROR, null);
  }

  public static Bundle createSuccessBundle() {
    Bundle resultBundle = new Bundle();
    resultBundle.putBoolean(RESULT_BUNDLE_KEY_RESULT, true);
    return resultBundle;
  }

  public static Bundle createFailureBundle(String errorMessage) {
    Bundle resultBundle = new Bundle();
    resultBundle.putBoolean(RESULT_BUNDLE_KEY_RESULT, false);
    resultBundle.putString(RESULT_BUNDLE_KEY_ERROR, errorMessage);
    return resultBundle;
  }

  private PortalResultHelper() {}
  ;
}
