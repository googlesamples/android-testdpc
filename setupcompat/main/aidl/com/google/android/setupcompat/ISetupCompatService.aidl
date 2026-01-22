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

package com.google.android.setupcompat;

import android.os.Bundle;

/**
 * Declares the interface for compat related service methods.
 */
interface ISetupCompatService {

  oneway void logMetric(int metricType, in Bundle arguments, in Bundle extras) = 1;

  oneway void onFocusStatusChanged(in Bundle bundle) = 2;
}
