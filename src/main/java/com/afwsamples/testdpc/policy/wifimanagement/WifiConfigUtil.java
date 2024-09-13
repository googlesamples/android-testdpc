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
  private static final int INVALID_NETWORK_ID = -1;

  /**
   * Save or replace the WiFi configuration.
   *
   * <p>Correctly 'saves' the network with the {@link WifiManager} pre-O, as required.
   *
   * @return whether it was successful.
   */
  public static boolean saveWifiConfiguration(
      Context context, WifiConfiguration wifiConfiguration) {
    WifiManager wifiManager = (WifiManager) context.getApplicationContext().getSystemService(
        Context.WIFI_SERVICE);
    final int networkId;
    if (wifiConfiguration.networkId == INVALID_NETWORK_ID) {
      networkId = addWifiNetwork(wifiManager, wifiConfiguration);
    } else {
      networkId = updateWifiNetwork(wifiManager, wifiConfiguration);
    }
    if (networkId == INVALID_NETWORK_ID) {
      return false;
    }
    wifiManager.enableNetwork(networkId, /* disableOthers= */ false);
    return true;
  }

  /**
   * Adds a new Wifi configuration, returning the configuration's networkId, or {@link
   * #INVALID_NETWORK_ID} if the operation fails.
   */
  private static int addWifiNetwork(WifiManager wifiManager, WifiConfiguration wifiConfiguration) {
    // WifiManager APIs are marked as deprecated but still explicitly supported for DPCs.
    int networkId = wifiManager.addNetwork(wifiConfiguration);
    if (networkId == INVALID_NETWORK_ID) {
      return INVALID_NETWORK_ID;
    }
    if (Build.VERSION.SDK_INT < Build.VERSION_CODES.O) {
      // Saving the configuration is required pre-O.
      if (!saveAddedWifiConfiguration(wifiManager, networkId)) {
        return INVALID_NETWORK_ID;
      }
    }
    return networkId;
  }

  private static boolean saveAddedWifiConfiguration(WifiManager wifiManager, int networkId) {
    boolean saveConfigurationSuccess = wifiManager.saveConfiguration();
    if (!saveConfigurationSuccess) {
      wifiManager.removeNetwork(networkId);
      return false;
    }
    return true;
  }

  /**
   * Saves an existing Wifi configuration, returning the configuration's networkId, or {@link
   * #INVALID_NETWORK_ID} if the operation fails.
   */
  private static int updateWifiNetwork(
      WifiManager wifiManager, WifiConfiguration wifiConfiguration) {
    // WifiManager APIs are marked as deprecated but still explicitly supported for DPCs.
    int networkId = wifiManager.updateNetwork(wifiConfiguration);
    if (networkId == INVALID_NETWORK_ID) {
      return INVALID_NETWORK_ID;
    }
    if (Build.VERSION.SDK_INT < Build.VERSION_CODES.O) {
      // Saving the configuration is required pre-O.
      if (!saveUpdatedWifiConfiguration(wifiManager)) {
        return INVALID_NETWORK_ID;
      }
    }
    return networkId;
  }

  private static boolean saveUpdatedWifiConfiguration(WifiManager wifiManager) {
    return wifiManager.saveConfiguration();
  }
}
