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

package com.afwsamples.testdpc.policy.networking;

import android.app.AlertDialog;
import android.app.DatePickerDialog;
import android.app.ListFragment;
import android.app.usage.NetworkStatsManager;
import android.app.usage.NetworkStats;
import android.content.Context;
import android.content.pm.ApplicationInfo;
import android.content.pm.PackageInfo;
import android.content.pm.PackageManager;
import android.content.res.Resources;
import android.graphics.drawable.Drawable;
import android.net.ConnectivityManager;
import android.os.Bundle;
import android.os.RemoteException;
import android.text.format.Formatter;
import android.util.SparseArray;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.AdapterView;
import android.widget.ArrayAdapter;
import android.widget.Button;
import android.widget.DatePicker;
import android.widget.ImageView;
import android.widget.ListView;
import android.widget.Spinner;
import android.widget.TextView;

import com.afwsamples.testdpc.R;

import java.text.DateFormat;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Calendar;
import java.util.Date;
import java.util.List;

/**
 * Fragment for checking network usage of apps using {@link android.app.usage.NetworkStatsManager}
 */
public class NetworkUsageStatsFragment extends ListFragment implements View.OnClickListener,
        AdapterView.OnItemSelectedListener {

    private static final String TAG = "TestDPC.NetworkUsageStatsFragment";

    // See @array/network_stats_queries_array
    private static final int QUERY_DEVICE_POS = 1;
    private static final int QUERY_PROFILE_POS = 2;
    private static final int QUERY_APPSUMMARY_POS = 3;
    private static final int QUERY_APPHISTORY_POS = 4;

    private NetworkStatsManager mNetstatsManager;
    private PackageManager mPackageManager;

    private Spinner mQuerySpinner;
    private TextView mExplanation;
    private Date mStartDate;
    private Date mEndDate;
    private Button mStartDateButton;
    private Button mEndDateButton;
    private TextView mDataUsageSummary;
    private ListView mDataUsageList;
    private List<List<NetworkStats.Bucket>> mListData;
    private ArrayAdapter<List<NetworkStats.Bucket>> mListAdapter;
    private ListView mAppHistoryList;
    private Button mBackToAppsListButton;

    @Override
    public View onCreateView(final LayoutInflater inflater, ViewGroup container,
            Bundle savedInstanceState) {
        super.onCreateView(inflater, container, savedInstanceState);
        View view = inflater.inflate(R.layout.network_usage_stats, container, false);

        getActivity().getActionBar().setTitle(R.string.data_usage);

        mPackageManager = getActivity().getPackageManager();
        mNetstatsManager = (NetworkStatsManager)getActivity().getSystemService(
                Context.NETWORK_STATS_SERVICE);

        mListData = new ArrayList<>();
        mListAdapter = new ArrayAdapter<List<NetworkStats.Bucket>>(getActivity(),
                R.layout.data_usage_item, android.R.id.title, mListData) {
            @Override
            public View getView(int position, View convertView, ViewGroup parent) {
                View appView = convertView;
                if (convertView == null) {
                    appView = inflater.inflate(R.layout.data_usage_item, parent, false);
                    final TextView title = (TextView) appView.findViewById(android.R.id.title);
                    final TextView summary = (TextView) appView.findViewById(android.R.id.summary);
                    final TextView state = (TextView) appView.findViewById(R.id.state);
                    ImageView imageView = (ImageView) appView.findViewById(android.R.id.icon);
                    appView.setTag(new ViewHolder(title, summary, state, imageView));
                }
                List<NetworkStats.Bucket> item = getItem(position);
                bindView(appView, item);
                return appView;
            }
        };
        mQuerySpinner = (Spinner) view.findViewById(R.id.query_type_spinner);
        mQuerySpinner.setOnItemSelectedListener(this);
        mExplanation = (TextView) view.findViewById(R.id.explanation);
        mStartDate = getTodayPlus(0, 0);
        mEndDate = getTodayPlus(Calendar.DAY_OF_MONTH, 1);
        mStartDateButton = (Button) view.findViewById(R.id.start_date_button);
        if (mStartDateButton != null) {
            mStartDateButton.setOnClickListener(this);
        }
        mEndDateButton = (Button) view.findViewById(R.id.end_date_button);
        if (mEndDateButton != null) {
            mEndDateButton.setOnClickListener(this);
        }
        updateButtonsText();
        mDataUsageSummary = (TextView) view.findViewById(R.id.data_usage_summary);
        mDataUsageList = (ListView) view.findViewById(android.R.id.list);
        mDataUsageList.setAdapter(mListAdapter);
        mAppHistoryList = (ListView) view.findViewById(R.id.app_history);
        mBackToAppsListButton = (Button) view.findViewById(R.id.back_to_apps_button);
        if (mBackToAppsListButton != null) {
            mBackToAppsListButton.setOnClickListener(this);
        }
        return view;
    }

    @Override
    public void onClick(View v) {
        switch (v.getId()) {
            case R.id.start_date_button: {
                pickDate(mStartDate);
            } break;
            case R.id.end_date_button: {
                pickDate(mEndDate);
            } break;
            case R.id.back_to_apps_button: {
                transitionAppHistoryView(View.GONE);
            }
        }
    }

    @Override
    public void onItemSelected(AdapterView<?> parent, View view, int pos, long id) {
        NetworkStats result = null;
        NetworkStats.Bucket bucket = null;
        mDataUsageSummary.setVisibility(View.GONE);
        mDataUsageList.setVisibility(View.GONE);
        mAppHistoryList.setVisibility(View.GONE);
        mBackToAppsListButton.setVisibility(View.GONE);
        mExplanation.setText("");
        try {
            switch (pos) {
                case QUERY_DEVICE_POS: {
                    bucket = mNetstatsManager.querySummaryForDevice(
                            ConnectivityManager.TYPE_WIFI, "", mStartDate.getTime(),
                            mEndDate.getTime());
                    mExplanation.setText(R.string.network_stats_device_summary_explanation);
                    mDataUsageSummary.setText(getString(R.string.network_stats_device_summary,
                            formatSize(bucket.getRxBytes()), bucket.getRxPackets(),
                            formatSize(bucket.getTxBytes()), bucket.getTxPackets()));
                    mDataUsageSummary.setVisibility(View.VISIBLE);
                } break;
                case QUERY_PROFILE_POS: {
                    bucket = mNetstatsManager.querySummaryForUser(
                            ConnectivityManager.TYPE_WIFI, "", mStartDate.getTime(),
                            mEndDate.getTime());
                    mExplanation.setText(R.string.network_stats_profile_summary_explanation);
                    mDataUsageSummary.setText(getString(R.string.network_stats_profile_summary,
                            formatSize(bucket.getRxBytes()), bucket.getRxPackets(),
                            formatSize(bucket.getTxBytes()), bucket.getTxPackets()));
                    mDataUsageSummary.setVisibility(View.VISIBLE);
                } break;
                case QUERY_APPSUMMARY_POS: {
                    result = mNetstatsManager.querySummary(
                            ConnectivityManager.TYPE_WIFI, "", mStartDate.getTime(),
                            mEndDate.getTime());
                    mListData.clear();
                    if (result != null) {
                        while (result.hasNextBucket()) {
                            bucket = new NetworkStats.Bucket();
                            result.getNextBucket(bucket);
                            mListData.add(Arrays.asList(new NetworkStats.Bucket[] { bucket }));
                        }
                    }
                    mListAdapter.notifyDataSetChanged();
                    mDataUsageList.setVisibility(View.VISIBLE);
                } break;
                case QUERY_APPHISTORY_POS: {
                    result = mNetstatsManager.queryDetails(
                            ConnectivityManager.TYPE_WIFI, "", mStartDate.getTime(),
                            mEndDate.getTime());
                    mListData.clear();
                    SparseArray<List<NetworkStats.Bucket>> uidMap = new SparseArray<>();
                    if (result != null) {
                        while (result.hasNextBucket()) {
                            bucket = new NetworkStats.Bucket();
                            result.getNextBucket(bucket);
                            final int uid = bucket.getUid();
                            List<NetworkStats.Bucket> list = uidMap.get(uid);
                            if (list == null) {
                                list = new ArrayList<NetworkStats.Bucket>();
                                mListData.add(list);
                                uidMap.put(uid, list);
                            }
                            list.add(bucket);
                        }
                    }
                    mListAdapter.notifyDataSetChanged();
                    mDataUsageList.setVisibility(View.VISIBLE);
                } break;
            }
        } catch (SecurityException e) {
            showErrorDialog(getString(R.string.network_stats_security_error_msg));
        } catch (RemoteException | NumberFormatException e) {
            showErrorDialog(e.toString());
        } finally {
            if (result != null) {
                result.close();
            }
        }
    }

    @Override
    public void onNothingSelected(AdapterView<?> parent) {
        // NOP
    }

    @Override
    public void onListItemClick (ListView l, View v, int position, long id) {
        List<NetworkStats.Bucket> item = mListAdapter.getItem(position);
        transitionAppHistoryView(View.GONE);
        if (item != null && item.size() > 1) {
            transitionAppHistoryView(View.VISIBLE);
            ArrayAdapter<NetworkStats.Bucket> adapter = new ArrayAdapter<NetworkStats.Bucket>(
                    getActivity(), android.R.layout.two_line_list_item, android.R.id.title, item) {
                        @Override
                        public View getView(int position, View convertView, ViewGroup parent) {
                            View view = convertView;
                            if (convertView == null) {
                                view = getActivity().getLayoutInflater().inflate(
                                        android.R.layout.two_line_list_item, parent, false);
                            }
                            DateFormat dateFormat = DateFormat.getDateInstance(DateFormat.SHORT);
                            NetworkStats.Bucket item = getItem(position);
                            Date startDate = new Date(item.getStartTimeStamp());
                            Date endDate = new Date(item.getEndTimeStamp());
                            TextView text1 = (TextView) view.findViewById(android.R.id.text1);
                            TextView text2 = (TextView) view.findViewById(android.R.id.text2);
                            text1.setText(dateFormat.format(startDate) + " - "
                                    + dateFormat.format(endDate));
                            text2.setText(getString(R.string.network_stats_bucket_usage,
                                    formatSize(item.getRxBytes()), item.getRxPackets(),
                                    formatSize(item.getTxBytes()), item.getTxPackets()));
                            return view;
                        }
                    };
            mAppHistoryList.setAdapter(adapter);
        }
    }

    private void showErrorDialog(CharSequence message) {
        new AlertDialog.Builder(getActivity())
                .setTitle(R.string.network_stats_error_title)
                .setIconAttribute(android.R.attr.alertDialogIcon)
                .setMessage(message)
                .setPositiveButton(android.R.string.ok, null)
                .show();
    }

    private void pickDate(final Date target) {
        final Calendar calendar = Calendar.getInstance();
        calendar.setTime(target);
        DatePickerDialog dialog = new DatePickerDialog(getActivity(),
                new DatePickerDialog.OnDateSetListener() {
                    @Override
                    public void onDateSet(DatePicker view, int year, int monthOfYear,
                            int dayOfMonth) {
                        calendar.set(Calendar.YEAR, year);
                        calendar.set(Calendar.MONTH, monthOfYear);
                        calendar.set(Calendar.DAY_OF_MONTH, dayOfMonth);
                        target.setTime(calendar.getTimeInMillis());
                        updateButtonsText();
                    }
                }, calendar.get(Calendar.YEAR), calendar.get(Calendar.MONTH),
                calendar.get(Calendar.DAY_OF_MONTH));
        dialog.show();
    }

    private void updateButtonsText() {
        DateFormat dateFormat = DateFormat.getDateInstance(DateFormat.MEDIUM);
        if (mStartDateButton != null) {
            mStartDateButton.setText(dateFormat.format(mStartDate));
        }
        if (mEndDateButton != null) {
            mEndDateButton.setText(dateFormat.format(mEndDate));
        }
    }

    private Date getTodayPlus(int calendarField, int valueToAdd) {
        final Calendar calendar = Calendar.getInstance();
        calendar.set(Calendar.HOUR, 0);
        calendar.set(Calendar.MINUTE, 0);
        calendar.set(Calendar.SECOND, 0);
        calendar.set(Calendar.MILLISECOND, 0);
        if (valueToAdd > 0) {
            calendar.add(calendarField, valueToAdd);
        }
        return calendar.getTime();
    }

    private void bindView(View appView, List<NetworkStats.Bucket> item) {
        final ViewHolder viewHolder = (ViewHolder) appView.getTag();
        final TextView title = viewHolder.title;
        final TextView summary = viewHolder.summary;
        final TextView state = viewHolder.state;
        final ImageView imageView = viewHolder.image;
        final Resources res = getResources();
        Drawable icon = res.getDrawable(android.R.drawable.ic_dialog_info, null);
        NetworkStats.Bucket bucket = item.get(0);
        final int uid = bucket.getUid();
        switch (uid) {
            case NetworkStats.Bucket.UID_REMOVED: {
                title.setText(R.string.network_stats_uid_removed);
            } break;
            case NetworkStats.Bucket.UID_TETHERING: {
                title.setText(R.string.network_stats_uid_tethering);
            } break;
            case android.os.Process.SYSTEM_UID: {
                title.setText(R.string.network_stats_uid_system);
            } break;
            default: {
                title.setText(getString(R.string.network_stats_uid, uid));
                icon = mPackageManager.getDefaultActivityIcon();
                final String[] packageNames = mPackageManager.getPackagesForUid(uid);
                final int length = packageNames != null ? packageNames.length : 0;
                try {
                    if (length == 1) {
                        final String pkgName = packageNames[0];
                        final ApplicationInfo info = mPackageManager.getApplicationInfo(pkgName,
                                0 /* no flags */);
                        if (info != null) {
                            title.setText(info.loadLabel(mPackageManager));
                            icon = info.loadIcon(mPackageManager);
                        }
                    } else {
                        for (int i = 0; i < length; i++) {
                            final String packageName = packageNames[i];
                            final PackageInfo packageInfo = mPackageManager.getPackageInfo(
                                    packageName, 0 /* no flags */);
                            final ApplicationInfo appInfo = mPackageManager.getApplicationInfo(
                                    packageName, 0 /* no flags */);

                            if (appInfo != null && packageInfo != null) {
                                if (packageInfo.sharedUserLabel != 0) {
                                    title.setText(mPackageManager.getText(packageName,
                                            packageInfo.sharedUserLabel,
                                            packageInfo.applicationInfo));
                                    icon = appInfo.loadIcon(mPackageManager);
                                }
                            }
                        }
                    }
                } catch (PackageManager.NameNotFoundException e) {
                    // keep the default activity icon
                }
            }
        }
        imageView.setImageDrawable(icon);
        final int bucketsCount = item.size();
        if (bucketsCount == 1) {
            summary.setText(formatSize(bucket.getRxBytes() + bucket.getTxBytes()));
            switch (bucket.getState()) {
                case NetworkStats.Bucket.STATE_FOREGROUND: {
                    state.setText(R.string.network_stats_foreground_state);
                } break;
                case NetworkStats.Bucket.STATE_DEFAULT: {
                    state.setText(R.string.network_stats_default_state);
                } break;
                case NetworkStats.Bucket.STATE_ALL: {
                    state.setText(R.string.network_stats_combined_state);
                } break;
            }
        } else {
            summary.setText(getString(R.string.network_stats_items, bucketsCount));
            state.setText(R.string.network_stats_combined_state);
        }
    }

    private void transitionAppHistoryView(int appHistoryVisibility) {
        mAppHistoryList.setVisibility(appHistoryVisibility);
        mBackToAppsListButton.setVisibility(appHistoryVisibility);
        mDataUsageList.setVisibility(
                appHistoryVisibility == View.VISIBLE ? View.GONE : View.VISIBLE);
    }

    private String formatSize(long sizeBytes) {
        return Formatter.formatFileSize(getActivity(), sizeBytes);
    }

    private static class ViewHolder {
        public final TextView title;
        public final TextView summary;
        public final TextView state;
        public final ImageView image;
        public ViewHolder(final TextView title, final TextView summary, final TextView state,
                final ImageView image) {
            this.title = title;
            this.summary = summary;
            this.state = state;
            this.image = image;
        }
    }
}
