package com.afwsamples.testdpc.policy;

import android.annotation.TargetApi;
import android.app.Fragment;
import android.app.admin.DevicePolicyManager;
import android.content.ComponentName;
import android.content.Context;
import android.content.Intent;
import android.os.Build;
import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Button;
import android.widget.EditText;

import com.afwsamples.testdpc.DeviceAdminReceiver;
import com.afwsamples.testdpc.R;
import com.afwsamples.testdpc.common.Util;

/**
 * Allows the user to set a test persistent device owner state.
 *
 * <p>For manual testing of forced re-enrollment.
 *
 * <p>If there is a non-empty peristent device owner state, it will survive the next factory reset,
 * TestDPC will be re-installed automatically as device owner and the state will be passed to it
 * during the initial device setup.
 */
public class PersistentDeviceOwnerFragment extends Fragment implements View.OnClickListener {

    private DevicePolicyManager mDpm;
    private ComponentName mAdminComponent;
    private EditText mStateEdit;

    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        getActivity().getActionBar().setTitle(R.string.persistent_device_owner);
        mDpm = (DevicePolicyManager) getActivity().getSystemService(
                Context.DEVICE_POLICY_SERVICE);
        mAdminComponent = DeviceAdminReceiver.getComponentName(getActivity());
    }

    @Override
    public View onCreateView(
            LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState) {
        View root = inflater.inflate(R.layout.persistent_device_owner_fragment, container, false);
        root.findViewById(R.id.clear_persistent_device_owner_button).setOnClickListener(this);
        root.findViewById(R.id.set_persistent_device_owner_button).setOnClickListener(this);
        mStateEdit = (EditText) root.findViewById(R.id.persistent_device_owner_state_edit);
        return root;
    }

    @Override
    public void onClick(View view) {
        String message = null;
        switch (view.getId()) {
            case R.id.clear_persistent_device_owner_button:
                mStateEdit.getText().clear();
                Util.setPersistentDoStateWithApplicationRestriction(
                        getActivity(), mDpm, mAdminComponent, null);
                break;
            case R.id.set_persistent_device_owner_button:
                Util.setPersistentDoStateWithApplicationRestriction(
                        getActivity(), mDpm, mAdminComponent, mStateEdit.getText().toString());
                break;
        }
    }

    @Override
    public void onResume() {
        super.onResume();
        String state = Util.getPersistentDoStateFromApplicationRestriction(mDpm, mAdminComponent);
        mStateEdit.setText(state == null ? "" : state);
    }
}
