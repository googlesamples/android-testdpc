package com.afwsamples.testdpc.policy;

import android.app.ListFragment;
import android.app.admin.DevicePolicyManager;
import android.app.admin.SecurityLog;
import android.app.admin.SecurityLog.SecurityEvent;
import android.content.ComponentName;
import android.content.Context;
import android.os.Bundle;
import android.util.Log;
import android.widget.ArrayAdapter;
import android.widget.ListView;

import com.afwsamples.testdpc.DeviceAdminReceiver;
import com.afwsamples.testdpc.R;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.Date;
import java.util.List;
import java.util.concurrent.TimeUnit;

public class ProcessLogsFragment extends ListFragment {

    private static final String TAG = "ProcessLogsFragment";

    private ArrayList<String> mLogs = new ArrayList<String>();
    private ArrayAdapter<String> mAdapter;

    private DevicePolicyManager mDevicePolicyManager;
    private ComponentName mAdminName;

    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        mAdminName = DeviceAdminReceiver.getComponentName(getActivity());
        mDevicePolicyManager = (DevicePolicyManager) getActivity().getSystemService(
                Context.DEVICE_POLICY_SERVICE);
        mAdapter = new ArrayAdapter<>(getActivity(), android.R.layout.simple_list_item_1,
                android.R.id.text1, mLogs);
        setListAdapter(mAdapter);
    }

    @Override
    public void onActivityCreated(Bundle savedInstanceState) {
        super.onActivityCreated(savedInstanceState);
        mAdapter.add(getString(R.string.process_logs_retrieved_message, new Date().toString()));
        processEvents(mDevicePolicyManager.retrieveSecurityLogs(mAdminName));
    }

    @Override
    public void onDestroy() {
        super.onDestroy();
        mAdapter = null;
    }

    private void processEvents(List<SecurityEvent> logs) {
        if (logs == null) {
            Log.w(TAG, "logs == null, are you polling too early?");
            mAdapter.add(getString(R.string.failed_to_retrieve_process_logs));
        } else {
            Log.d(TAG, "Incoming logs size: " + logs.size());
            for (SecurityEvent event : logs) {
                StringBuilder sb = new StringBuilder();
                sb.append(getStringEventTagFromId(event.getTag()));
                sb.append(" (").append(new Date(TimeUnit.NANOSECONDS.toMillis(
                        event.getTimeNanos()))).append("): ");
                printData(sb, event.getData());
                mAdapter.add(sb.toString());
            }
            ListView listView = ProcessLogsFragment.this.getListView();
            listView.setSelection(listView.getCount() - 1);
        }
    }

    private String getStringEventTagFromId(int eventId) {
        String eventTag;
        switch (eventId) {
            case SecurityLog.TAG_ADB_SHELL_INTERACTIVE:
                eventTag = "ADB_SHELL_INTERACTIVE";
                break;
            case SecurityLog.TAG_ADB_SHELL_CMD:
                eventTag = "ADB_SHELL_CMD";
                break;
            case SecurityLog.TAG_SYNC_RECV_FILE:
                eventTag = "SYNC_RECV_FILE";
                break;
            case SecurityLog.TAG_SYNC_SEND_FILE:
                eventTag = "SYNC_SEND_FILE";
                break;
            case SecurityLog.TAG_APP_PROCESS_START:
                eventTag = "APP_PROCESS_START";
                break;
            case SecurityLog.TAG_KEYGUARD_DISMISSED:
                eventTag = "KEYGUARD_DISMISSED";
                break;
            case SecurityLog.TAG_KEYGUARD_DISMISS_AUTH_ATTEMPT:
                eventTag = "KEYGUARD_DISMISS_AUTH_ATTEMPT";
                break;
            case SecurityLog.TAG_KEYGUARD_SECURED:
                eventTag = "KEYGUARD_SECURED";
                break;
            default:
                eventTag = "UNKNOWN";
        }
        return eventTag;
    }

    private void printData(StringBuilder sb, Object data) {
        if (data instanceof Integer || data instanceof Long || data instanceof Float
                || data instanceof String) {
            sb.append(data.toString()).append(" ");
        } else if (data instanceof Object[]) {
            for (Object item : (Object[]) data) {
                printData(sb, item);
            }
        }
    }
}
