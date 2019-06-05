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
import static android.app.admin.DevicePolicyManager.EXTRA_PROVISIONING_MODE;
import static android.app.admin.DevicePolicyManager.PROVISIONING_MODE_FULLY_MANAGED_DEVICE;
import static android.app.admin.DevicePolicyManager.PROVISIONING_MODE_MANAGED_PROFILE;
import static com.google.common.truth.Truth.assertThat;
import static org.robolectric.Shadows.shadowOf;

import android.os.Build.VERSION_CODES;
import android.widget.RadioButton;
import androidx.test.core.app.ActivityScenario;
import com.afwsamples.testdpc.R;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.robolectric.RobolectricTestRunner;
import org.robolectric.annotation.Config;

@RunWith(RobolectricTestRunner.class)
// TODO: update to Q when Q is testable
@Config(minSdk = VERSION_CODES.P)
public class DpcLoginActivityTest {

  @Test
  public void onNavigateNext_poRadioBoxSelected_shouldFinishWithCorrectIntent() {
    ActivityScenario.launch(DpcLoginActivity.class).onActivity(activity -> {
      RadioButton dpcLoginPo = activity.findViewById(R.id.dpc_login_po);
      dpcLoginPo.setChecked(true);

      activity.findViewById(R.id.suw_navbar_next).performClick();

      assertThat(shadowOf(activity).getResultCode()).isEqualTo(RESULT_OK);
      assertThat(shadowOf(activity).getResultIntent().getIntExtra(EXTRA_PROVISIONING_MODE, -1))
          .isEqualTo(PROVISIONING_MODE_MANAGED_PROFILE);
    });
  }

  @Test
  public void onNavigateNext_doRadioBoxSelected_shouldFinishWithCorrectIntent() {
    ActivityScenario.launch(DpcLoginActivity.class).onActivity(activity -> {
      RadioButton dpcLoginDo = activity.findViewById(R.id.dpc_login_do);
      dpcLoginDo.setChecked(true);

      activity.findViewById(R.id.suw_navbar_next).performClick();

      assertThat(shadowOf(activity).getResultCode()).isEqualTo(RESULT_OK);
      assertThat(shadowOf(activity).getResultIntent().getIntExtra(EXTRA_PROVISIONING_MODE, -1))
          .isEqualTo(PROVISIONING_MODE_FULLY_MANAGED_DEVICE);
    });
  }

  @Test
  public void onBackPressed_shouldSetResultToCancelled() {
    ActivityScenario.launch(DpcLoginActivity.class).onActivity(activity -> {

      activity.onBackPressed();

      assertThat(shadowOf(activity).getResultCode()).isEqualTo(RESULT_CANCELED);
    });
  }
}