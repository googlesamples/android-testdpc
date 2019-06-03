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

import static com.afwsamples.testdpc.common.Util.Q_VERSION_CODE;

import android.annotation.TargetApi;
import android.content.Context;
import android.content.Intent;
import android.net.Uri;
import android.os.Build.VERSION_CODES;

@TargetApi(Q_VERSION_CODE)
public class DelegatedAdminReceiver extends android.app.admin.DelegatedAdminReceiver {

    @Override
    public String onChoosePrivateKeyAlias(Context context, Intent intent, int uid, Uri uri,
                                          String alias) {
        return CommonReceiverOperations.onChoosePrivateKeyAlias(context, uid);
    }

    @Override
    public void onNetworkLogsAvailable(Context context, Intent intent, long batchToken,
                                       int networkLogsCount) {
        CommonReceiverOperations.onNetworkLogsAvailable(context, null, batchToken,
                networkLogsCount);
    }
}
