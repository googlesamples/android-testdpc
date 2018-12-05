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

package com.afwsamples.testdpc.policy;

import static com.afwsamples.testdpc.CommonReceiverOperations.NETWORK_LOGS_FILE_PREFIX;

import android.annotation.TargetApi;
import android.app.ListFragment;
import android.os.Build.VERSION_CODES;
import android.os.Bundle;
import android.util.Log;
import android.widget.ArrayAdapter;
import android.widget.ListView;
import com.afwsamples.testdpc.R;
import java.io.BufferedReader;
import java.io.File;
import java.io.FileReader;
import java.io.IOException;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collections;
import java.util.List;
import java.util.regex.PatternSyntaxException;

/**
 * Display the last retrieved batch of NetworkEvents.
 */
@TargetApi(VERSION_CODES.O)
public class NetworkLogsFragment extends ListFragment {

    private static final String TAG = "NetworkLogsFragment";

    private List<String> mLogs = new ArrayList<>();
    private ArrayAdapter<String> mAdapter;

    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        mAdapter =
            new ArrayAdapter<>(
                getActivity(),
                android.R.layout.simple_list_item_1,
                android.R.id.text1,
                mLogs);
        setListAdapter(mAdapter);
    }

    @Override
    public void onActivityCreated(Bundle savedInstanceState) {
        super.onActivityCreated(savedInstanceState);
        mLogs = fetchEvents();
        showEvents(mLogs);
    }

    private List<String> fetchEvents() {
        File logsFile = findLastBatch();
        if (logsFile == null) {
            return Collections.emptyList();
        }
        BufferedReader reader = null;
        try {
            long batchToken = determineBatchToken(logsFile.getName());
            mAdapter.add(
                getContext().getString(R.string.on_network_logs_available_success, batchToken));
            reader = new BufferedReader(new FileReader(logsFile));
            ArrayList<String> events = new ArrayList<>();
            String line;
            while ((line = reader.readLine()) != null) {
                Log.v(TAG, "found line: " + line);
                events.add(line);
            }
            return events;
        } catch (NumberFormatException | IOException e) {
            mAdapter.add(getString(R.string.on_network_logs_available_failure));
        } finally {
            try {
                if (reader != null) {
                    reader.close();
                }
            } catch (IOException e) {
                // no-op
            }
        }
        return Collections.emptyList();
    }

    private void showEvents(List<String> logs) {
        if (logs == null) {
            Log.w(TAG, "logs == null, are you polling too early?");
            mAdapter.add(getString(R.string.on_network_logs_available_failure));
        } else {
            Log.d(TAG, "Incoming logs size: " + logs.size());
            mAdapter.addAll(mLogs);
            ListView listView = NetworkLogsFragment.this.getListView();
            listView.setSelection(listView.getCount() - 1);
        }
    }

    private File findLastBatch() {
        File path = getContext().getExternalFilesDir(null);
        if (path == null) {
            return null;
        }
        File[] networkLogsFiles =
            path.listFiles((File file, String s) -> s.startsWith(NETWORK_LOGS_FILE_PREFIX));
        if (networkLogsFiles == null || networkLogsFiles.length == 0) {
            return null;
        }
        // Get the most recent batch. Batches are sorted by the timestamp they were last modified
        // in, also corresponding to the last value in their file names.
        return Collections.max(
            // Can't use Comparator.comparing(): requires default interface methods (min_sdk >= 24).
            Arrays.asList(networkLogsFiles),
            (f1, f2) -> Long.signum (f1.lastModified() - f2.lastModified()));
    }

    private long determineBatchToken(String fileName) throws NumberFormatException {
        // Name should be "network_logs_X_Y.txt", where X is the batch token
        String[] fileNameArr = fileName.split("_");
        if (fileNameArr.length <= 2) {
            throw new NumberFormatException("Failed parsing the batch from file: " + fileName);
        }
        long batchToken;
        try {
            batchToken = Long.parseLong(fileNameArr[2]);
        } catch (PatternSyntaxException e) {
            throw new NumberFormatException("Failed parsing the batch from file: " + fileName);
        }
        return batchToken;
    }
}
