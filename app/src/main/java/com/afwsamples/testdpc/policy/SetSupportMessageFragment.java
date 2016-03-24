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

package com.afwsamples.testdpc.policy;

import android.app.admin.DevicePolicyManager;
import android.app.Fragment;
import android.content.ComponentName;
import android.content.Context;
import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.EditText;

import com.afwsamples.testdpc.DeviceAdminReceiver;
import com.afwsamples.testdpc.R;

public class SetSupportMessageFragment extends Fragment
        implements View.OnClickListener {
    private static final String ARG_MESSAGE_TYPE = "message_type";

    public static final int TYPE_SHORT = 0;
    public static final int TYPE_LONG = 1;

    private int mMessageType = TYPE_SHORT;
    private EditText mSupportMessage;
    private DevicePolicyManager mDpm;
    private ComponentName mAdmin;

    public static SetSupportMessageFragment newInstance(int messageType) {
        SetSupportMessageFragment fragment = new SetSupportMessageFragment();
        Bundle args = new Bundle();
        args.putInt(ARG_MESSAGE_TYPE, messageType);
        fragment.setArguments(args);
        return fragment;
    }

    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        Bundle args = getArguments();
        if (args != null) {
            mMessageType = args.getInt(ARG_MESSAGE_TYPE);
        }
        getActivity().getActionBar().setTitle(mMessageType == TYPE_SHORT ?
                R.string.set_short_support_message : R.string.set_long_support_message);
        mDpm = (DevicePolicyManager) getActivity().getSystemService(
                Context.DEVICE_POLICY_SERVICE);
        mAdmin = DeviceAdminReceiver.getComponentName(getActivity());
    }

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container,
            Bundle savedInstanceState) {
        View root = inflater.inflate(R.layout.set_support_message, container, false);
        root.findViewById(R.id.set_default_message).setOnClickListener(this);
        root.findViewById(R.id.set_message).setOnClickListener(this);
        root.findViewById(R.id.clear_message).setOnClickListener(this);
        mSupportMessage = (EditText) root.findViewById(R.id.message_view);
        if (mMessageType == TYPE_SHORT) {
            mSupportMessage.setText(mDpm.getShortSupportMessage(mAdmin));
        } else {
            mSupportMessage.setText(mDpm.getLongSupportMessage(mAdmin));
        }
        return root;
    }

    @Override
    public void onClick(View view) {
        String message = null;
        switch (view.getId()) {
            case R.id.set_default_message:
                message = getString(mMessageType == TYPE_SHORT ?
                        R.string.default_short_message : R.string.default_long_message);
                break;
            case R.id.set_message:
                message = mSupportMessage.getText().toString();
                break;
            case R.id.clear_message:
                message = null;
                break;
        }
        if (mMessageType == TYPE_SHORT) {
            mDpm.setShortSupportMessage(mAdmin, message);
        } else {
            mDpm.setLongSupportMessage(mAdmin, message);
        }
        mSupportMessage.setText(message);
    }
}