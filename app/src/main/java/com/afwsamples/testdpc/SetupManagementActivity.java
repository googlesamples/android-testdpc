package com.afwsamples.testdpc;

import android.app.Activity;
import android.os.Bundle;

public class SetupManagementActivity extends Activity {

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);
        if (savedInstanceState == null) {
            getFragmentManager().beginTransaction().add(R.id.container,
                    new SetupManagementFragment(),
                    SetupManagementFragment.FRAGMENT_TAG).commit();
        }
    }
}
