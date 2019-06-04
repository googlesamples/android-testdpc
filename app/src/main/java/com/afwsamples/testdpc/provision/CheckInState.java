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

package com.afwsamples.testdpc.provision;

import android.content.Context;
import android.content.Intent;
import android.content.SharedPreferences;
import android.preference.PreferenceManager;
import androidx.localbroadcastmanager.content.LocalBroadcastManager;

public class CheckInState {
    private SharedPreferences mSharedPreferences;
    private Context mContext;

    public static final int FIRST_ACCOUNT_STATE_PENDING = 0;
    public static final int FIRST_ACCOUNT_STATE_READY = 1;
    public static final int FIRST_ACCOUNT_STATE_TIMEOUT = 2;

    private static final String KEY_FIRST_ACCOUNT_STATE = "first_account_state";

    /**
     * Broadcast Action: FIRST_ACCOUNT_READY broadcast is processed.
     */
    public static final String FIRST_ACCOUNT_READY_PROCESSED_ACTION =
            "com.afwsamples.testdpc.FIRST_ACCOUNT_READY_PROCESSED";

    public CheckInState(Context context) {
        mSharedPreferences = PreferenceManager.getDefaultSharedPreferences(context);
        mContext = context.getApplicationContext();
    }

    public int getFirstAccountState() {
        return mSharedPreferences.getInt(KEY_FIRST_ACCOUNT_STATE, FIRST_ACCOUNT_STATE_PENDING);
    }

    public void setFirstAccountState(int state) {
        mSharedPreferences.edit().putInt(KEY_FIRST_ACCOUNT_STATE, state).apply();
        if (state != FIRST_ACCOUNT_STATE_PENDING) {
            LocalBroadcastManager.getInstance(mContext).sendBroadcast(
                    new Intent(FIRST_ACCOUNT_READY_PROCESSED_ACTION));
        }
    }
}
