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

package com.afwsamples.testdpc.policy.wifimanagement;

import android.content.Context;
import android.net.wifi.WifiConfiguration;
import android.net.wifi.WifiManager;
import android.os.Build;

public class WifiConfigUtil {

  /**
   * Save or replace the WiFi configuration.
   *
   * <p>Correctly 'saves' the network with the {@link WifiManager} pre-O, as required.
   *
   * @return whether it was successful.
   */
  public static boolean saveWifiConfiguration(
      Context context, WifiConfiguration wifiConfiguration) {
    WifiManager wifiManager = (WifiManager) context.getSystemService(Context.WIFI_SERVICE);
    return wifiConfiguration.networkId == -1
        ? addWifiNetwork(wifiManager, wifiConfiguration)
        : updateWifiNetwork(wifiManager, wifiConfiguration);
  }

  private static boolean addWifiNetwork(
      WifiManager wifiManager, WifiConfiguration wifiConfiguration) {
    // WifiManager APIs are marked as deprecated but still explicitly supported for DPCs.
    int networkId = wifiManager.addNetwork(wifiConfiguration);
    if (networkId == -1) {
      return false;
    }
    if (Build.VERSION.SDK_INT < Build.VERSION_CODES.O) {
      // Saving the configuration is required pre-O.
      return saveAddedWifiConfiguration(wifiManager, networkId);
    }
    return true;
  }

  private static boolean saveAddedWifiConfiguration(WifiManager wifiManager, int networkId) {
    boolean saveConfigurationSuccess = wifiManager.saveConfiguration();
    if (!saveConfigurationSuccess) {
      wifiManager.removeNetwork(networkId);
      return false;
    }
    return true;
  }

  private static boolean updateWifiNetwork(
      WifiManager wifiManager, WifiConfiguration wifiConfiguration) {
    // WifiManager APIs are marked as deprecated but still explicitly supported for DPCs.
    int networkId = wifiManager.updateNetwork(wifiConfiguration);
    if (networkId == -1) {
      return false;
    }
    if (Build.VERSION.SDK_INT < Build.VERSION_CODES.O) {
      // Saving the configuration is required pre-O.
      return saveUpdatedWifiConfiguration(wifiManager);
    }
    return true;
  }

  private static boolean saveUpdatedWifiConfiguration(WifiManager wifiManager) {
    return wifiManager.saveConfiguration();
  }
}
