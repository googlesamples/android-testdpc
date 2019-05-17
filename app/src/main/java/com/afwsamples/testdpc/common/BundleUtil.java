/*
 * Copyright (C) 2016 The Android Open Source Project
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

package com.afwsamples.testdpc.common;

import android.annotation.TargetApi;
import android.os.Build.VERSION_CODES;
import android.os.Bundle;
import android.os.PersistableBundle;
import java.util.Set;

public class BundleUtil {

    @TargetApi(VERSION_CODES.LOLLIPOP_MR1)
    public static PersistableBundle bundleToPersistableBundle(Bundle bundle) {
        Set<String> keySet = bundle.keySet();
        PersistableBundle persistableBundle = new PersistableBundle();
        for (String key : keySet) {
            Object value = bundle.get(key);
            if (value instanceof Boolean) {
                persistableBundle.putBoolean(key, (boolean) value);
            } else if (value instanceof Integer) {
                persistableBundle.putInt(key, (int) value);
            } else if (value instanceof String) {
                persistableBundle.putString(key, (String) value);
            } else if (value instanceof String[]) {
                persistableBundle.putStringArray(key, (String[]) value);
            } else if (value instanceof Bundle) {
                PersistableBundle innerBundle = bundleToPersistableBundle((Bundle) value);
                persistableBundle.putPersistableBundle(key, innerBundle);
            }
        }
        return persistableBundle;
    }

    @TargetApi(VERSION_CODES.LOLLIPOP_MR1)
    public static Bundle persistableBundleToBundle(PersistableBundle persistableBundle) {
        Set<String> keySet = persistableBundle.keySet();
        Bundle bundle = new Bundle();
        for (String key : keySet) {
            Object value = persistableBundle.get(key);
            if (value instanceof Boolean) {
                bundle.putBoolean(key, (boolean) value);
            } else if (value instanceof Integer) {
                bundle.putInt(key, (int) value);
            } else if (value instanceof String) {
                bundle.putString(key, (String) value);
            } else if (value instanceof String[]) {
                bundle.putStringArray(key, (String[]) value);
            } else if (value instanceof PersistableBundle) {
                Bundle innerBundle = persistableBundleToBundle((PersistableBundle) value);
                bundle.putBundle(key, innerBundle);
            }
        }
        return bundle;
    }
}
