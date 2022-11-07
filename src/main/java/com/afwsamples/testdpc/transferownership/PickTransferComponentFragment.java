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

package com.afwsamples.testdpc.transferownership;

import android.annotation.TargetApi;
import android.app.Fragment;
import android.app.admin.DeviceAdminReceiver;
import android.app.admin.DevicePolicyManager;
import android.content.ComponentName;
import android.content.Context;
import android.content.Intent;
import android.content.pm.ActivityInfo;
import android.content.pm.PackageManager;
import android.content.pm.ResolveInfo;
import android.os.Build.VERSION_CODES;
import android.os.Bundle;
import android.os.PersistableBundle;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ArrayAdapter;
import android.widget.Button;
import android.widget.EditText;
import android.widget.ListAdapter;
import android.widget.ListView;
import androidx.annotation.Nullable;
import com.afwsamples.testdpc.R;
import java.io.PrintWriter;
import java.io.StringWriter;
import java.lang.reflect.InvocationTargetException;
import java.util.ArrayList;
import java.util.List;

@TargetApi(VERSION_CODES.P)
public class PickTransferComponentFragment extends Fragment {

  private DevicePolicyManager mDevicePolicyManager;

  @Override
  public void onCreate(@Nullable Bundle savedInstanceState) {
    super.onCreate(savedInstanceState);
    mDevicePolicyManager =
        (DevicePolicyManager) getActivity().getSystemService(Context.DEVICE_POLICY_SERVICE);
  }

  @Override
  public View onCreateView(
      LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState) {
    final ViewGroup rootView =
        (ViewGroup) inflater.inflate(R.layout.transfer_ownership_dialog, container, false);
    final ListView listView = rootView.findViewById(R.id.list);
    final EditText componentName = rootView.findViewById(R.id.component_name);
    final EditText result = rootView.findViewById(R.id.result);
    final Button transferButton = rootView.findViewById(R.id.transfer_btn);

    final Intent intent = new Intent(DeviceAdminReceiver.ACTION_DEVICE_ADMIN_ENABLED);
    final List<String> items = new ArrayList<>();
    final PackageManager packageManager = getActivity().getPackageManager();
    final List<ResolveInfo> resolveInfos = packageManager.queryBroadcastReceivers(intent, 0);
    for (ResolveInfo resolveInfo : resolveInfos) {
      ActivityInfo activityInfo = resolveInfo.activityInfo;
      if (activityInfo == null) {
        continue;
      }
      items.add(activityInfo.packageName + "/" + activityInfo.name);
    }

    final ListAdapter adapter =
        new ArrayAdapter<>(getActivity(), android.R.layout.simple_list_item_1, items);
    listView.setAdapter(adapter);
    listView.setOnItemClickListener(
        (adapterView, view1, i, __) ->
            componentName.setText(adapterView.getItemAtPosition(i).toString()));

    transferButton.setOnClickListener(
        view -> {
          ComponentName target =
              ComponentName.unflattenFromString(componentName.getText().toString());
          if (target != null) {
            result.setText(performTransfer(target));
          } else {
            result.setText(R.string.transfer_ownership_invalid_target_format);
          }
        });

    return rootView;
  }

  private String performTransfer(ComponentName target) {
    ComponentName source =
        com.afwsamples.testdpc.DeviceAdminReceiver.getComponentName(getActivity());
    Log.i(getClass().getName(), "Transferring ownership from " + source + " to target " + target);
    try {
      PersistableBundle persistableBundle = new PersistableBundle();
      persistableBundle.putString("random_key", "random_value");
      // TODO: use DevicePolicyManagerGateway instead
      mDevicePolicyManager.transferOwnership(source, target, persistableBundle);
      return "Success!";
    } catch (Exception e) {
      Throwable cause = e.getCause();
      if (cause instanceof InvocationTargetException) {
        return getStackTrace(((InvocationTargetException) cause).getTargetException());
      }
      return getStackTrace(cause);
    }
  }

  private String getStackTrace(Throwable throwable) {
    StringWriter stringWriter = new StringWriter();
    PrintWriter printWriter = new PrintWriter(stringWriter);
    throwable.printStackTrace(printWriter);
    return stringWriter.toString();
  }
}
