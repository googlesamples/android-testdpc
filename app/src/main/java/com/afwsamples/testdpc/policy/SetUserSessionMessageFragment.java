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

import android.annotation.TargetApi;
import android.app.Fragment;
import android.app.admin.DevicePolicyManager;
import android.content.ComponentName;
import android.content.Context;
import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.EditText;
import com.afwsamples.testdpc.DeviceAdminReceiver;
import com.afwsamples.testdpc.R;

@TargetApi(28)
public class SetUserSessionMessageFragment extends Fragment
        implements View.OnClickListener {
    private static final String TAG = "SetUserSessionMessage";

    private DevicePolicyManager mDevicePolicyManager;
    private ComponentName mComponentName;

    private EditText mStartSessionMessage;
    private EditText mEndSessionMessage;

    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        getActivity().getActionBar().setTitle(R.string.set_user_session_message);
        mDevicePolicyManager = (DevicePolicyManager) getActivity().getSystemService(
                Context.DEVICE_POLICY_SERVICE);
        mComponentName = DeviceAdminReceiver.getComponentName(getActivity());
    }

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container,
            Bundle savedInstanceState) {
        View root = inflater.inflate(R.layout.set_user_session_message, container, false);
        root.findViewById(R.id.set_message).setOnClickListener(this);
        root.findViewById(R.id.clear_message).setOnClickListener(this);
        mStartSessionMessage = root.findViewById(R.id.start_session_message_view);
        mEndSessionMessage = root.findViewById(R.id.end_session_message_view);
        mStartSessionMessage.setText(mDevicePolicyManager.getStartUserSessionMessage(mComponentName));
        mEndSessionMessage.setText(mDevicePolicyManager.getEndUserSessionMessage(mComponentName));
        return root;
    }

    @Override
    public void onClick(View view) {
        CharSequence startSessionMessage = null;
        CharSequence endSessionMessage = null;
        switch (view.getId()) {
            case R.id.set_message:
                startSessionMessage = mStartSessionMessage.getText();
                endSessionMessage = mEndSessionMessage.getText();
                break;
            case R.id.clear_message:
                mStartSessionMessage.setText(null);
                mEndSessionMessage.setText(null);
                break;
        }

        mDevicePolicyManager.setStartUserSessionMessage(mComponentName, startSessionMessage);
        mDevicePolicyManager.setEndUserSessionMessage(mComponentName, endSessionMessage);
    }
}