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

package com.afwsamples.testdpc;

import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.net.Uri;
import android.util.Log;

//TODO(b/122460462) Revert to android.app.admin.DelegatedAdminReceiver once we have a new SDK drop.
public class DelegatedAdminReceiver extends BroadcastReceiver {

    //@Override
    public String onChoosePrivateKeyAlias(Context context, Intent intent, int uid, Uri uri,
                                          String alias) {
        return CommonReceiverOperations.onChoosePrivateKeyAlias(context, uid);
    }

    //@Override
    public void onNetworkLogsAvailable(Context context, Intent intent, long batchToken,
                                       int networkLogsCount) {
        CommonReceiverOperations.onNetworkLogsAvailable(context, null, batchToken,
                networkLogsCount);
    }

    @Override
    public void onReceive(Context context, Intent intent) {
        String action = intent.getAction();

        if ("android.app.action.CHOOSE_PRIVATE_KEY_ALIAS".equals(action)) {
            int uid = intent.getIntExtra("android.app.extra.CHOOSE_PRIVATE_KEY_SENDER_UID", -1);
            Uri uri = intent.getParcelableExtra("android.app.extra.CHOOSE_PRIVATE_KEY_URI");
            String alias = intent.getStringExtra("android.app.extra.CHOOSE_PRIVATE_KEY_ALIAS");
            String chosenAlias = onChoosePrivateKeyAlias(context, intent, uid, uri, alias);
            setResultData(chosenAlias);
        } else if ("android.app.action.NETWORK_LOGS_AVAILABLE".equals(action)) {
            long batchToken = intent.getLongExtra("android.app.extra.EXTRA_NETWORK_LOGS_TOKEN", -1);
            int networkLogsCount = intent.getIntExtra("android.app.extra.EXTRA_NETWORK_LOGS_COUNT",
                    0);
            onNetworkLogsAvailable(context, intent, batchToken, networkLogsCount);
        } else {
            Log.w("DelegatedReceiver", "Unhandled broadcast: " + action);
        }

    }
}
