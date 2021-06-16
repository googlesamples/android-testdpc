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

package com.afwsamples.testdpc.provision;

import static android.app.Activity.RESULT_CANCELED;
import static android.app.Activity.RESULT_OK;
import static android.app.admin.DevicePolicyManager.EXTRA_PROVISIONING_ALLOWED_PROVISIONING_MODES;
import static android.app.admin.DevicePolicyManager.EXTRA_PROVISIONING_MODE;
import static android.app.admin.DevicePolicyManager.PROVISIONING_MODE_FULLY_MANAGED_DEVICE;
import static android.app.admin.DevicePolicyManager.PROVISIONING_MODE_MANAGED_PROFILE;
import static android.app.admin.DevicePolicyManager.PROVISIONING_MODE_MANAGED_PROFILE_ON_PERSONAL_DEVICE;
import static com.google.common.truth.Truth.assertThat;
import static org.robolectric.Shadows.shadowOf;

import android.content.Intent;
import android.os.Build.VERSION_CODES;
import android.view.View;
import androidx.test.core.app.ActivityScenario;
import com.afwsamples.testdpc.R;
import java.util.ArrayList;
import java.util.Arrays;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.robolectric.Robolectric;
import org.robolectric.RobolectricTestRunner;
import org.robolectric.annotation.Config;

@RunWith(RobolectricTestRunner.class)
@Config(minSdk = VERSION_CODES.S)
public class GetProvisioningModeActivityTest {

  @Test
  public void onPoButtonClick_shouldFinishWithCorrectIntent() {
    ActivityScenario.launch(GetProvisioningModeActivity.class).onActivity(activity -> {
      activity.findViewById(R.id.po_selection_button).performClick();

      assertThat(shadowOf(activity).getResultCode()).isEqualTo(RESULT_OK);
      assertThat(shadowOf(activity).getResultIntent().getIntExtra(EXTRA_PROVISIONING_MODE, -1))
          .isEqualTo(PROVISIONING_MODE_MANAGED_PROFILE);
    });
  }

  @Test
  public void onDoButtonClick_shouldFinishWithCorrectIntent() {
    ActivityScenario.launch(GetProvisioningModeActivity.class).onActivity(activity -> {
      activity.findViewById(R.id.do_selection_button).performClick();

      assertThat(shadowOf(activity).getResultCode()).isEqualTo(RESULT_OK);
      assertThat(shadowOf(activity).getResultIntent().getIntExtra(EXTRA_PROVISIONING_MODE, -1))
          .isEqualTo(PROVISIONING_MODE_FULLY_MANAGED_DEVICE);
    });
  }

  @Test
  public void onBackPressed_shouldSetResultToCancelled() {
    ActivityScenario.launch(GetProvisioningModeActivity.class).onActivity(activity -> {

      activity.onBackPressed();

      assertThat(shadowOf(activity).getResultCode()).isEqualTo(RESULT_CANCELED);
    });
  }

  @Test
  public void onCreate_withDoAllowedProvisioningMode_showsOnlyDoOption() {
    GetProvisioningModeActivity activity =
        launchActivityWithAllowedProvisioningModes(PROVISIONING_MODE_FULLY_MANAGED_DEVICE);

    assertThat(activity.findViewById(R.id.do_option).getVisibility()).isEqualTo(View.VISIBLE);
    assertThat(activity.findViewById(R.id.po_option).getVisibility()).isEqualTo(View.GONE);
    assertThat(activity.findViewById(R.id.divider).getVisibility()).isEqualTo(View.GONE);
  }

  @Test
  public void onCreate_withPoAllowedProvisioningMode_showsOnlyPoOption() {
    GetProvisioningModeActivity activity =
        launchActivityWithAllowedProvisioningModes(PROVISIONING_MODE_MANAGED_PROFILE);

    assertThat(activity.findViewById(R.id.do_option).getVisibility()).isEqualTo(View.GONE);
    assertThat(activity.findViewById(R.id.po_option).getVisibility()).isEqualTo(View.VISIBLE);
    assertThat(activity.findViewById(R.id.divider).getVisibility()).isEqualTo(View.GONE);
  }

  @Test
  public void onCreate_withByodAllowedProvisioningMode_showsOnlyPoOption() {
    GetProvisioningModeActivity activity = launchActivityWithAllowedProvisioningModes(
            PROVISIONING_MODE_MANAGED_PROFILE_ON_PERSONAL_DEVICE);

    assertThat(activity.findViewById(R.id.do_option).getVisibility()).isEqualTo(View.GONE);
    assertThat(activity.findViewById(R.id.po_option).getVisibility()).isEqualTo(View.VISIBLE);
    assertThat(activity.findViewById(R.id.divider).getVisibility()).isEqualTo(View.GONE);
  }

  @Test
  public void onCreate_withPoAndDoAllowedProvisioningModes_showsPoAndDoOptions() {
    GetProvisioningModeActivity activity =
        launchActivityWithAllowedProvisioningModes(
            PROVISIONING_MODE_FULLY_MANAGED_DEVICE,
            PROVISIONING_MODE_MANAGED_PROFILE);

    assertThat(activity.findViewById(R.id.do_option).getVisibility()).isEqualTo(View.VISIBLE);
    assertThat(activity.findViewById(R.id.po_option).getVisibility()).isEqualTo(View.VISIBLE);
    assertThat(activity.findViewById(R.id.divider).getVisibility()).isEqualTo(View.VISIBLE);
  }

  @Test
  public void onCreate_withByodAndDoAllowedProvisioningMode_showsPoAndDoOptions() {
    GetProvisioningModeActivity activity = launchActivityWithAllowedProvisioningModes(
        PROVISIONING_MODE_FULLY_MANAGED_DEVICE,
        PROVISIONING_MODE_MANAGED_PROFILE_ON_PERSONAL_DEVICE);

    assertThat(activity.findViewById(R.id.do_option).getVisibility()).isEqualTo(View.VISIBLE);
    assertThat(activity.findViewById(R.id.po_option).getVisibility()).isEqualTo(View.VISIBLE);
    assertThat(activity.findViewById(R.id.divider).getVisibility()).isEqualTo(View.VISIBLE);
  }

  @Test
  public void onCreate_withNoAllowedProvisioningModes_showsPoAndDoOptions() {
    GetProvisioningModeActivity activity = launchActivityWithAllowedProvisioningModes();

    assertThat(activity.findViewById(R.id.do_option).getVisibility()).isEqualTo(View.VISIBLE);
    assertThat(activity.findViewById(R.id.po_option).getVisibility()).isEqualTo(View.VISIBLE);
    assertThat(activity.findViewById(R.id.divider).getVisibility()).isEqualTo(View.VISIBLE);
  }

  private GetProvisioningModeActivity launchActivityWithAllowedProvisioningModes(
      Integer... allowedProvisioningModes) {
    Intent intent = new Intent();
    ArrayList<Integer> arr = new ArrayList<>(Arrays.asList(allowedProvisioningModes));
    intent.putExtra(EXTRA_PROVISIONING_ALLOWED_PROVISIONING_MODES, arr);
    return Robolectric
        .buildActivity(GetProvisioningModeActivity.class, intent).create().get();
  }
}