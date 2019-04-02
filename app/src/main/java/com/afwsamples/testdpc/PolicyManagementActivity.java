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
import android.view.Menu;
import android.view.MenuItem;

import com.afwsamples.testdpc.common.OnBackPressedHandler;
import com.afwsamples.testdpc.policy.PolicyManagementFragment;
import com.afwsamples.testdpc.search.PolicySearchFragment;

/**
 * An entry activity that shows a profile setup fragment if the app is not a profile or device
 * owner. Otherwise, a policy management fragment is shown.
 */
public class PolicyManagementActivity extends Activity implements
        FragmentManager.OnBackStackChangedListener {

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

}
