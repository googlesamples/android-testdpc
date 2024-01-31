package com.afwsamples.testdpc;

import android.app.Activity;
import android.os.Bundle;
import com.afwsamples.testdpc.common.ThemeUtil;
import com.google.android.setupcompat.util.WizardManagerHelper;

public class SetupManagementActivity extends Activity {

  @Override
  protected void onCreate(Bundle savedInstanceState) {
    super.onCreate(savedInstanceState);

    // get default theme string from suw intent extra and set the Theme.
    ThemeUtil.setTheme(this, getIntent().getStringExtra(WizardManagerHelper.EXTRA_THEME));

    setContentView(R.layout.activity_main);
    if (savedInstanceState == null) {
      getFragmentManager()
          .beginTransaction()
          .add(R.id.container, new SetupManagementFragment(), SetupManagementFragment.FRAGMENT_TAG)
          .commit();
    }
  }
}
