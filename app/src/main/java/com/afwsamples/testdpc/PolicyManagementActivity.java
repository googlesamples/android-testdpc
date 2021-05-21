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

package com.afwsamples.testdpc;

import android.app.Activity;
import android.app.Fragment;
import android.app.FragmentManager;
import android.os.Bundle;
import android.util.Log;
import android.view.Menu;
import android.view.MenuItem;

import java.io.FileDescriptor;
import java.io.PrintWriter;

import com.afwsamples.testdpc.common.DumpableActivity;
import com.afwsamples.testdpc.common.OnBackPressedHandler;
import com.afwsamples.testdpc.policy.PolicyManagementFragment;
import com.afwsamples.testdpc.search.PolicySearchFragment;

/**
 * An entry activity that shows a profile setup fragment if the app is not a profile or device
 * owner. Otherwise, a policy management fragment is shown.
 */
public class PolicyManagementActivity extends DumpableActivity implements
        FragmentManager.OnBackStackChangedListener {

    private static final String TAG = PolicyManagementActivity.class.getSimpleName();

    private static final String CMD_LOCK_TASK_MODE = "lock-task-mode";
    private static final String LOCK_MODE_ACTION_START = "start";
    private static final String LOCK_MODE_ACTION_STATUS = "status";
    private static final String LOCK_MODE_ACTION_STOP = "stop";

    private boolean mLockTaskMode;

    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);
        if (savedInstanceState == null) {
            getFragmentManager().beginTransaction().add(R.id.container,
                    new PolicyManagementFragment(),
                    PolicyManagementFragment.FRAGMENT_TAG).commit();
        }
        getFragmentManager().addOnBackStackChangedListener(this);
    }

    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        getMenuInflater().inflate(R.menu.policy_management_menu, menu);
        return true;
    }

    @Override
    public boolean onMenuItemSelected(int featureId, MenuItem item) {
        switch (item.getItemId()) {
            case R.id.action_show_search:
                getFragmentManager()
                        .beginTransaction()
                        .replace(R.id.container, PolicySearchFragment.newInstance())
                        .addToBackStack("search")
                        .commit();
                break;
            case android.R.id.home:
                getFragmentManager().popBackStack();
                break;
        }
        return false;
    }

    @Override
    protected void onResume() {
        super.onResume();

        String lockModeCommand = getIntent().getStringExtra(CMD_LOCK_TASK_MODE);
        if (lockModeCommand != null) {
            setLockTaskMode(lockModeCommand);
        }
    }

    @Override
    public void onBackStackChanged() {
        // Show the up button in actionbar if back stack has any entry.
        getActionBar().setDisplayHomeAsUpEnabled(
                getFragmentManager().getBackStackEntryCount() > 0);
    }

    @Override
    public void onBackPressed() {
        Fragment currFragment = getFragmentManager().findFragmentById(R.id.container);
        boolean onBackPressHandled = false;
        if (currFragment != null && currFragment instanceof OnBackPressedHandler) {
            onBackPressHandled = ((OnBackPressedHandler) currFragment).onBackPressed();
        }
        if (!onBackPressHandled) {
            super.onBackPressed();
        }
    }

    @Override
    public void onDestroy() {
        super.onDestroy();
        getFragmentManager().removeOnBackStackChangedListener(this);
    }

    @Override
    public void dump(String prefix, FileDescriptor fd, PrintWriter pw, String[] args) {
        if (args != null && args.length > 0 && args[0].equals(CMD_LOCK_TASK_MODE)) {
            String action = args.length == 1 ? LOCK_MODE_ACTION_STATUS : args[1];
            switch (action) {
                case LOCK_MODE_ACTION_START:
                    pw.println("Starting lock-task mode");
                    startLockTaskMode();
                    break;
                case LOCK_MODE_ACTION_STOP:
                    pw.println("Stopping lock-task mode");
                    stopLockTaskMode();
                    break;
                case LOCK_MODE_ACTION_STATUS:
                    dumpLockModeStatus(pw);
                    break;
                default:
                    pw.printf("Invalid lock-task mode action: %s\n", action);
            }
            return;
        }
        pw.print(prefix); dumpLockModeStatus(pw);

        super.dump(prefix, fd, pw, args);
    }

    private void startLockTaskMode() {
        if (mLockTaskMode) Log.w(TAG, "startLockTaskMode(): mLockTaskMode already true");
        mLockTaskMode = true;

        Log.i(TAG, "startLockTaskMode(): calling Activity.startLockTask()");
        startLockTask();
    }

    private void stopLockTaskMode() {
        if (!mLockTaskMode) Log.w(TAG, "startLockTaskMode(): mLockTaskMode already false");
        mLockTaskMode = false;

        Log.i(TAG, "stopLockTaskMode(): calling Activity.stopLockTask()");
        stopLockTask();
    }

    private void dumpLockModeStatus(PrintWriter pw) {
        pw.printf("lock-task mode: %b\n", mLockTaskMode);
    }

    private void setLockTaskMode(String action) {
        switch (action) {
            case LOCK_MODE_ACTION_START:
                startLockTaskMode();
                break;
            case LOCK_MODE_ACTION_STOP:
                stopLockTaskMode();
                break;
            case LOCK_MODE_ACTION_STATUS:
                Log.d(TAG, "lock-task mode status: " + mLockTaskMode);
                break;
            default:
                Log.e(TAG, "invalid lock-task action: " + action);
        }
    }
}
