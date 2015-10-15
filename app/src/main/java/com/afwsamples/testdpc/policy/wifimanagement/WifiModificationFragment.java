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

package com.afwsamples.testdpc.policy.wifimanagement;

import android.app.AlertDialog;
import android.app.Fragment;
import android.content.Context;
import android.net.wifi.WifiConfiguration;
import android.net.wifi.WifiManager;
import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ArrayAdapter;
import android.widget.Button;
import android.widget.CheckedTextView;
import android.widget.ListView;
import android.widget.TextView;

import com.afwsamples.testdpc.R;

import java.util.ArrayList;
import java.util.List;

/**
 * Fragment for WiFi configuration editing.
 */
public class WifiModificationFragment extends Fragment
        implements WifiConfigCreationDialog.Listener {

    private static final String TAG_WIFI_CONFIG_MODIFICATION = "wifi_config_modification";
    private TextView mText;
    private ListView mConfigsList;
    private ConfigsAdapter mConfigsAdapter;
    private List<WifiConfiguration> mConfiguredNetworks = new ArrayList<>();
    private WifiManager mWifiManager;

    private void updateConfigsList() {
        mConfiguredNetworks.clear();
        List<WifiConfiguration> configuredNetworks = mWifiManager.getConfiguredNetworks();
        if (configuredNetworks != null) {
            mConfiguredNetworks.addAll(configuredNetworks);
        }
        mConfigsAdapter.notifyDataSetChanged();
    }

    private WifiConfiguration getClickedItem() {
        long clickedIds[] = mConfigsList.getCheckedItemIds();
        if (clickedIds.length == 0) {
            return null;
        }
        final int configuredNetworksSize = mConfiguredNetworks.size();
        for (int i = 0; i < configuredNetworksSize; ++i) {
            final WifiConfiguration config = mConfiguredNetworks.get(i);
            if (config.networkId == clickedIds[0]) {
                return config;
            }
        }
        return null;
    }

    private class ConfigsAdapter extends ArrayAdapter<WifiConfiguration> {
        public ConfigsAdapter() {
            super(getActivity(), android.R.layout.simple_list_item_checked, mConfiguredNetworks);
        }

        @Override
        public boolean hasStableIds() {
            return true;
        }

        @Override
        public long getItemId(int position) {
            return mConfiguredNetworks.get(position).networkId;
        }

        @Override
        public View getView (int position, View convertView, ViewGroup parent) {
            if (convertView == null) {
                convertView = getActivity().getLayoutInflater().inflate(
                        android.R.layout.simple_list_item_checked, parent, false);
            }
            WifiConfiguration config = mConfiguredNetworks.get(position);
            ((CheckedTextView)convertView).setText(config.SSID);

            return convertView;
        }
    }

    @Override
    public void onResume() {
        super.onResume();
        updateConfigsList();
    }

    @Override
    public View onCreateView(LayoutInflater inflater, final ViewGroup container,
            Bundle savedInstanceState) {
        super.onCreateView(inflater, container, savedInstanceState);
        mWifiManager = (WifiManager)getActivity().getSystemService(Context.WIFI_SERVICE);
        View view = inflater.inflate(R.layout.wifi_config_modification, container, false);

        mConfigsList = (ListView) view.findViewById(R.id.wifiConfigs);
        TextView configsHeader = (TextView) inflater.inflate(R.layout.wifi_config_list_header,
                mConfigsList, false);
        mConfigsList.addHeaderView(configsHeader);

        mConfigsAdapter = new ConfigsAdapter();
        mConfigsList.setAdapter(mConfigsAdapter);
        mConfigsList.setChoiceMode(ListView.CHOICE_MODE_SINGLE);
        updateConfigsList();

        Button updateConfigButton = (Button) view.findViewById(R.id.updateSelectedConfig);
        if (updateConfigButton != null) {
            updateConfigButton.setOnClickListener(new View.OnClickListener() {
                @Override
                public void onClick(View v) {
                    WifiConfiguration oldConf = getClickedItem();
                    if (oldConf != null) {
                        try {
                            WifiConfigCreationDialog dialog = WifiConfigCreationDialog.newInstance(
                                    oldConf, WifiModificationFragment.this);
                            dialog.show(getFragmentManager(), TAG_WIFI_CONFIG_MODIFICATION);
                        } catch (SecurityException e) {
                            showError(e.getMessage());
                        }
                    }
                }
            });
        }

        Button removeConfigButton = (Button) view.findViewById(R.id.removeSelectedConfig);
        if (removeConfigButton != null) {
            removeConfigButton.setOnClickListener(new View.OnClickListener() {
                @Override
                public void onClick(View v) {
                    WifiConfiguration oldConf = getClickedItem();
                    if (oldConf != null) {
                        try {
                            if (!mWifiManager.removeNetwork(oldConf.networkId)) {
                                showError(getString(R.string.wifi_remove_config_error, oldConf.SSID,
                                        oldConf.networkId));
                            } else {
                                updateConfigsList();
                            }
                        } catch (SecurityException e) {
                            showError(e.getMessage());
                        }
                    }
                }
            });
        }

        return view;
    }

    @Override
    public void onDismiss() {
        updateConfigsList();
    }

    @Override
    public void onCancel() {
    }

    private void showError(final String message) {
        new AlertDialog.Builder(getActivity()).setTitle(R.string.wifi_modification_error_title)
                .setIconAttribute(android.R.attr.alertDialogIcon)
                .setMessage(message).setPositiveButton(android.R.string.ok, null).show();
    }

}
