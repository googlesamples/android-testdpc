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

import android.app.Fragment;
import android.content.ComponentName;
import android.content.pm.ApplicationInfo;
import android.content.pm.PackageManager;
import android.content.pm.ResolveInfo;
import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.AdapterView;
import android.widget.ListView;
import android.widget.Spinner;
import android.widget.SpinnerAdapter;

import com.afwsamples.testdpc.R;

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;

/**
 * This fragment shows a spinner of all allowed apps and a list of properties associated with the
 * currently selected application.
 */
public abstract class ManageResolveInfoFragment extends BaseManageComponentFragment<ResolveInfo> {

    protected List<ResolveInfo> mResolveInfos;

    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
    }

    @Override
    protected SpinnerAdapter createSpinnerAdapter() {
        mResolveInfos = loadResolveInfoList();
        return new ResolveInfoSpinnerAdapter(
                getActivity(), R.layout.app_row, R.id.pkg_name, mResolveInfos);
    }

    protected abstract List<ResolveInfo> loadResolveInfoList();
}
