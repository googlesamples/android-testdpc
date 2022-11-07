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

package com.afwsamples.testdpc.policy.networking;

import android.annotation.TargetApi;
import android.app.Fragment;
import android.app.admin.DevicePolicyManager;
import android.content.ComponentName;
import android.content.Context;
import android.os.Build.VERSION_CODES;
import android.os.Bundle;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Button;
import android.widget.EditText;
import android.widget.RadioGroup;
import android.widget.Toast;
import com.afwsamples.testdpc.DeviceAdminReceiver;
import com.afwsamples.testdpc.R;

@TargetApi(VERSION_CODES.Q)
public class PrivateDnsModeFragment extends Fragment
    implements View.OnClickListener, RadioGroup.OnCheckedChangeListener {
  private static final String TAG = "PDNS_FRAG";

  // Copied from DevicePolicyManager, should be removed when the code is migrated to Q.
  static final int PRIVATE_DNS_MODE_UNKNOWN = 0;
  static final int PRIVATE_DNS_MODE_OFF = 1;
  static final int PRIVATE_DNS_MODE_OPPORTUNISTIC = 2;
  static final int PRIVATE_DNS_MODE_PROVIDER_HOSTNAME = 3;

  private DevicePolicyManager mDpm;
  private RadioGroup mPrivateDnsModeSelection;
  private Button mSetButton;
  private EditText mCurrentResolver;
  // The mode that is currently selected in the radio group.
  private int mSelectedMode;

  @Override
  public void onCreate(Bundle savedInstanceState) {
    super.onCreate(savedInstanceState);
    mDpm = (DevicePolicyManager) getActivity().getSystemService(Context.DEVICE_POLICY_SERVICE);
    mSelectedMode = PRIVATE_DNS_MODE_UNKNOWN;
  }

  @Override
  public void onClick(View view) {
    String resolver = mCurrentResolver.getText().toString();
    setPrivateDnsMode(mSelectedMode, resolver);
  }

  @Override
  public void onCheckedChanged(RadioGroup group, int checkedId) {
    updateSelectedMode(checkedId);
  }

  @Override
  public View onCreateView(
      LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState) {
    View view = inflater.inflate(R.layout.private_dns_mode, container, false);
    mSetButton = view.findViewById(R.id.private_dns_mode_apply);
    mSetButton.setOnClickListener(this);

    mPrivateDnsModeSelection = view.findViewById(R.id.private_dns_mode_selection);
    int currentMode = getPrivateDnsMode();
    switch (currentMode) {
      case PRIVATE_DNS_MODE_OFF:
        mPrivateDnsModeSelection.check(R.id.private_dns_mode_off);
        break;
      case PRIVATE_DNS_MODE_OPPORTUNISTIC:
        mPrivateDnsModeSelection.check(R.id.private_dns_mode_automatic);
        break;
      case PRIVATE_DNS_MODE_PROVIDER_HOSTNAME:
        mPrivateDnsModeSelection.check(R.id.private_dns_mode_specific_host);
        break;
      default:
        mPrivateDnsModeSelection.check(R.id.private_dns_mode_unknown);
        break;
    }
    mPrivateDnsModeSelection.setOnCheckedChangeListener(this);
    updateSelectedMode(mPrivateDnsModeSelection.getCheckedRadioButtonId());

    mCurrentResolver = view.findViewById(R.id.private_dns_resolver);
    mCurrentResolver.setText(getPrivateDnsHost());
    return view;
  }

  private void updateSelectedMode(int checkedId) {
    if (checkedId == R.id.private_dns_mode_off) {
      mSelectedMode = PRIVATE_DNS_MODE_OFF;
      mSetButton.setEnabled(false);
    } else if (checkedId == R.id.private_dns_mode_automatic) {
      mSelectedMode = PRIVATE_DNS_MODE_OPPORTUNISTIC;
      mSetButton.setEnabled(true);
    } else if (checkedId == R.id.private_dns_mode_specific_host) {
      mSelectedMode = PRIVATE_DNS_MODE_PROVIDER_HOSTNAME;
      mSetButton.setEnabled(true);
    } else {
      mSelectedMode = PRIVATE_DNS_MODE_UNKNOWN;
      mSetButton.setEnabled(false);
    }
  }

  private int getPrivateDnsMode() {
    try {
      return mDpm.getGlobalPrivateDnsMode(DeviceAdminReceiver.getComponentName(getActivity()));
    } catch (SecurityException e) {
      Log.w(TAG, "Failure getting current mode", e);
    }

    return PRIVATE_DNS_MODE_UNKNOWN;
  }

  private String getPrivateDnsHost() {
    try {
      return mDpm.getGlobalPrivateDnsHost(DeviceAdminReceiver.getComponentName(getActivity()));
    } catch (SecurityException e) {
      Log.w(TAG, "Failure getting host", e);
    }

    return "<error getting resolver>";
  }

  private void setPrivateDnsMode(int mode, String resolver) {
    Log.w(TAG, String.format("Setting mode %d host %s", mSelectedMode, resolver));

    final ComponentName component = DeviceAdminReceiver.getComponentName(getActivity());
    new SetPrivateDnsTask(
            mDpm,
            component,
            mode,
            resolver,
            (int msgId, Object... args) -> {
              Toast.makeText(getActivity(), getString(msgId, args), Toast.LENGTH_LONG).show();
            })
        .execute(new Void[0]);
  }
}
