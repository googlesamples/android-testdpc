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

package com.afwsamples.testdpc.common.preference;

import android.support.annotation.Nullable;

/**
 * Common base class for the DpcPreference family of classes.
 */
public interface DpcPreferenceBase {
    void setMinSdkVersion(int version);
    void setAdminConstraint(@DpcPreferenceHelper.AdminKind int adminConstraint);
    void clearAdminConstraint();
    void setUserConstraint(@DpcPreferenceHelper.UserKind int userConstraints);
    void clearUserConstraint();
    void clearNonCustomConstraints();
    void setCustomConstraint(@Nullable CustomConstraint customConstraint);
    /**
     * To re-check is the constraint met and enable/disable the preference accordingly.
     */
    void refreshEnabledState();
}
