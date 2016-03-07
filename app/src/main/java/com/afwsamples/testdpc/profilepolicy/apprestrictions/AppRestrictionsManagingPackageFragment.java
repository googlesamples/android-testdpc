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

package com.afwsamples.testdpc.profilepolicy.apprestrictions;

import com.afwsamples.testdpc.R;
import com.afwsamples.testdpc.common.SelectAppFragment;

/**
 * This fragment lets the user select an app that can manage application restrictions for the
 * current user. Related APIs:
 * 1) {@link AppRestrictionsProxyHandler#setApplicationRestrictionsManagingPackage}
 * 2) {@link AppRestrictionsProxyHandler#getApplicationRestrictionsManagingPackage}
 */
public class AppRestrictionsManagingPackageFragment extends SelectAppFragment {

    @Override
    public void onResume() {
        super.onResume();
        getActivity().getActionBar().setTitle(R.string.app_restrictions_managing_package);
    }

    @Override
    protected void setSelectedPackage(String pkgName) {
        AppRestrictionsProxyHandler.setApplicationRestrictionsManagingPackage(getContext(),
                pkgName);
    }

    @Override
    protected void clearSelectedPackage() {
        AppRestrictionsProxyHandler.setApplicationRestrictionsManagingPackage(getContext(), null);
    }

    @Override
    protected String getSelectedPackage() {
        return AppRestrictionsProxyHandler.getApplicationRestrictionsManagingPackage(getContext());
    }
}
