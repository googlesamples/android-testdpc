/*
 * Copyright (C) 2015 The Android Open Source Project
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

package com.sample.android.testdpc.common;

import android.accounts.Account;
import android.content.Intent;
import android.os.PersistableBundle;

/**
 * Common utility functions used for retrieving information from the intent that launched TestDPC.
 */
public class LaunchIntentUtil {

    public static final String EXTRA_ACCOUNT_NAME = "account_name";

    private static final String EXTRA_IS_SETUP_WIZARD = "is_setup_wizard";
    private static final String EXTRA_ACCOUNT = "account";

    private LaunchIntentUtil() {}

    /**
     * @returns true if TestDPC was launched as part of synchronous authentication flow in setup
     * wizard or settings->add account
     */
    public static boolean isSynchronousAuthLaunch(Intent launchIntent) {
        return launchIntent != null
                && (launchIntent.getExtras() != null)
                && (launchIntent.getExtras().get(EXTRA_IS_SETUP_WIZARD) != null);
    }

    /**
     * @returns true if TestDPC was launched as part of synchronous authentication flow in setup
     * wizard or settings->add account, based upon the extras of the given bundle which was
     * populated by {@link #prepareDeviceAdminExtras(Intent, PersistableBundle)}
     */
    public static boolean isSynchronousAuthLaunch(PersistableBundle extras) {
        // NOTE: Value here is irrelevant, only presence of extra matters - true indicates sync-
        // auth from Setup Wizard, false indicates another path (e.g. Settings->Add Account).
        return extras != null && (extras.get(EXTRA_IS_SETUP_WIZARD) != null);
    }

    /**
     * @returns an account, if TestDPC was informed of which account it was invoked as a result of
     * adding in synchronous auth cases
     */
    public static Account getAddedAccount(Intent intent) {
        return intent != null ? intent.<Account>getParcelableExtra(EXTRA_ACCOUNT) : null;
    }

    /**
     * @returns the account name in the given bundle, as populated by
     * {@link #prepareDeviceAdminExtras(Intent, PersistableBundle)}
     */
    public static String getAddedAccountName(PersistableBundle persistableBundle) {
        return persistableBundle != null
                ? persistableBundle.getString(EXTRA_ACCOUNT_NAME, null) : null;
    }

    /**
     * Copy important intent extras from the launching intent launchIntent into newBundle.
     */
    public static void prepareDeviceAdminExtras(Intent launchIntent, PersistableBundle newBundle) {
        if (isSynchronousAuthLaunch(launchIntent)) {
            boolean isSetupWizard = launchIntent.getBooleanExtra(EXTRA_IS_SETUP_WIZARD, false);

            // Store as String in new bundle, as API 21 doesn't support putBoolean.
            newBundle.putString(EXTRA_IS_SETUP_WIZARD, Boolean.toString(isSetupWizard));

            Account addedAccount = getAddedAccount(launchIntent);
            if (addedAccount != null) {
                newBundle.putString(EXTRA_ACCOUNT_NAME, addedAccount.name);
            }
        }
    }
}
