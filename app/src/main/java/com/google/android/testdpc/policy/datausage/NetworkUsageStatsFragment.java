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

package com.google.android.testdpc.policy.datausage;

import android.app.Fragment;
import android.app.usage.NetworkStatsManager;
import android.app.usage.NetworkUsageStats;
import android.content.Context;
import android.content.pm.PackageManager;
import android.net.ConnectivityManager;
import android.os.Bundle;
import android.os.Handler;
import android.os.RemoteException;
import android.text.method.ScrollingMovementMethod;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Button;
import android.widget.TextView;

import com.google.android.testdpc.R;

/**
 * Fragment for checking network usage of apps using {@link android.app.usage.NetworkStatsManager}
 */
public class NetworkUsageStatsFragment extends Fragment implements View.OnClickListener {

    private static final String TAG = "TestDPC.NetworkUsageStatsFragment";
    private TextView mText;
    private TextView mUidText;
    private NetworkStatsManager mNetstatsManager;
    private PackageManager mPackageManager;

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container,
            Bundle savedInstanceState) {
        super.onCreateView(inflater, container, savedInstanceState);
        View view = inflater.inflate(R.layout.network_usage_stats, container, false);

        mPackageManager = getActivity().getPackageManager();
        mNetstatsManager = (NetworkStatsManager)getActivity().getSystemService(
                Context.NETWORK_STATS_SERVICE);
        mText = (TextView) view.findViewById(R.id.textout);
        mText.setMovementMethod(new ScrollingMovementMethod());

        for (int id : new int[] {
                R.id.button_network_usage_summary_device,
                R.id.button_network_usage_summary_user,
                R.id.button_network_usage_summary_apps,
                R.id.button_network_usage_history_apps,
                R.id.button_network_usage_history_uid,
                R.id.button_network_usage_clear
                }) {
            Button button = (Button) view.findViewById(id);
            if (button != null) {
                button.setOnClickListener(this);
            }
        }
        return view;
    }

    @Override
    public void onClick(View v) {
        NetworkUsageStats result = null;
        NetworkUsageStats.Bucket bucket = null;
        try {
            switch (v.getId()) {
                case R.id.button_network_usage_summary_device: {
                    bucket = mNetstatsManager.querySummaryForDevice(
                            ConnectivityManager.TYPE_WIFI, "", Long.MIN_VALUE, Long.MAX_VALUE);
                } break;
                case R.id.button_network_usage_summary_user: {
                    bucket = mNetstatsManager.querySummaryForUser(
                            ConnectivityManager.TYPE_WIFI, "", Long.MIN_VALUE, Long.MAX_VALUE);
                } break;
                case R.id.button_network_usage_summary_apps: {
                    result = mNetstatsManager.querySummary(
                            ConnectivityManager.TYPE_WIFI, "", Long.MIN_VALUE, Long.MAX_VALUE);
                } break;
                case R.id.button_network_usage_history_apps: {
                    result = mNetstatsManager.queryDetails(
                            ConnectivityManager.TYPE_WIFI, "", Long.MIN_VALUE, Long.MAX_VALUE);
                } break;
                case R.id.button_network_usage_history_uid: {
                    result = mNetstatsManager.queryDetailsForUid(
                            ConnectivityManager.TYPE_WIFI, "", Long.MIN_VALUE, Long.MAX_VALUE,
                            Integer.parseInt(mUidText.getText().toString()));
                } break;
                case R.id.button_network_usage_clear: {
                    clearOutput();
                } break;
            }

            if (bucket != null) {
                show(bucket2String(bucket));
            }
            if (result != null) {
                showResult(result);
            }
        } catch (RemoteException | SecurityException | NumberFormatException e) {
            show(e.toString());
        } finally {
            if (result != null) {
                result.close();
            }
        }
    }

    private void showResult(NetworkUsageStats result) {
        if (result == null) {
            show("result == null");
            return;
        }
        NetworkUsageStats.Bucket bucket = new NetworkUsageStats.Bucket();
        while (result.getNextBucket(bucket)) {
            show(bucket2String(bucket));
        }
    }

    private void show(final String s) {
        if (s != null) {
            Handler h = new Handler(getActivity().getMainLooper());

            h.post(new Runnable() {
                @Override
                public void run() {
                    mText.append("\n");
                    mText.append(s);
                }
            });
        }
    }

    private String bucket2String(NetworkUsageStats.Bucket bucket) {
        if (bucket == null) {
            return "bucket == null";
        }
        return "App:"+(bucket.getUid() >= 0 ?
                mPackageManager.getNameForUid(bucket.getUid()) : "") +
                ", uid:" + bucket.getUid() +
                ", state:" + bucket.getState() +
                ", begin:" + bucket.getStartTimeStamp() +
                ", end:" + bucket.getEndTimeStamp() +
                ", rxBytes:" + bucket.getRxBytes() +
                ", rxPackets:" + bucket.getRxPackets() +
                ", txBytes:" + bucket.getTxBytes() +
                ", txPackets:" + bucket.getTxPackets();
    }

    private void clearOutput() {
        Handler h = new Handler(getActivity().getMainLooper());

        h.post(new Runnable() {
            @Override
            public void run() {
                mText.setText("");
            }
        });
    }
}
