/*
 * Copyright (C) 2019 The Android Open Source Project
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

import static android.os.Build.VERSION_CODES.LOLLIPOP;
import static android.os.Build.VERSION_CODES.N;
import static android.os.Build.VERSION_CODES.O;
import static com.google.common.truth.Truth.assertThat;
import static org.robolectric.Shadows.shadowOf;

import android.content.Context;
import android.net.wifi.WifiConfiguration;
import android.net.wifi.WifiManager;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.robolectric.RobolectricTestRunner;
import org.robolectric.RuntimeEnvironment;
import org.robolectric.annotation.Config;
import org.robolectric.shadows.ShadowWifiManager;

@RunWith(RobolectricTestRunner.class)
@Config(minSdk = LOLLIPOP)
public class WifiConfigUtilTest {
  private final Context mContext = RuntimeEnvironment.application;
  private final WifiManager mWifiManager =
      (WifiManager) mContext.getSystemService(Context.WIFI_SERVICE);

  @Test
  @Config(maxSdk = N)
  public void saveWifiConfiguration_preO_addNetwork_savesWifiConfiguration() {
    WifiConfigUtil.saveWifiConfiguration(mContext, buildWifiConfigurationToAddNetwork());
    assertThat(shadowOf(mWifiManager).wasConfigurationSaved()).isTrue();
  }

  @Test
  @Config(minSdk = O)
  public void saveWifiConfiguration_postO_addNetwork_doesNotSaveWifiConfiguration() {
    WifiConfigUtil.saveWifiConfiguration(mContext, buildWifiConfigurationToAddNetwork());
    assertThat(shadowOf(mWifiManager).wasConfigurationSaved()).isFalse();
  }

  @Test
  @Config(maxSdk = N)
  public void saveWifiConfiguration_preO_updateNetwork_savesWifiConfiguration() {
    WifiConfigUtil.saveWifiConfiguration(mContext, buildWifiConfigurationToUpdateNetwork());
    assertThat(shadowOf(mWifiManager).wasConfigurationSaved()).isTrue();
  }

  @Test
  @Config(minSdk = O)
  public void saveWifiConfiguration_postO_updateNetwork_doesNotSaveWifiConfiguration() {
    WifiConfigUtil.saveWifiConfiguration(mContext, buildWifiConfigurationToUpdateNetwork());
    assertThat(shadowOf(mWifiManager).wasConfigurationSaved()).isFalse();
  }

  private WifiConfiguration buildWifiConfigurationToAddNetwork() {
    return buildWifiConfigurationWithNetworkId(-1);
  }

  private WifiConfiguration buildWifiConfigurationToUpdateNetwork() {
    return buildWifiConfigurationWithNetworkId(1);
  }

  private WifiConfiguration buildWifiConfigurationWithNetworkId(int networkId) {
    WifiConfiguration wifiConfiguration = new WifiConfiguration();
    wifiConfiguration.networkId = networkId;
    return wifiConfiguration;
  }
}
