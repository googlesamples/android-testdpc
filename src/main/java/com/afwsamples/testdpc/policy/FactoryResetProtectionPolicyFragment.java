/*
 * Copyright (C) 2020 The Android Open Source Project
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

package com.afwsamples.testdpc.policy;

import android.app.AlertDialog;
import android.app.Fragment;
import android.app.admin.DevicePolicyManager;
import android.app.admin.FactoryResetProtectionPolicy;
import android.content.ComponentName;
import android.content.Context;
import android.os.Build.VERSION_CODES;
import android.os.Bundle;
import android.text.TextUtils;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.AdapterView;
import android.widget.ArrayAdapter;
import android.widget.Button;
import android.widget.EditText;
import android.widget.ListView;
import android.widget.Spinner;
import android.widget.TextView;
import android.widget.Toast;
import androidx.annotation.RequiresApi;
import androidx.annotation.StringRes;
import com.afwsamples.testdpc.DeviceAdminReceiver;
import com.afwsamples.testdpc.R;
import java.util.ArrayList;
import java.util.List;

@RequiresApi(api = VERSION_CODES.R)
public class FactoryResetProtectionPolicyFragment extends Fragment
    implements AdapterView.OnItemSelectedListener, View.OnClickListener {

  private static final int DISABLED = 0;
  private static final int ENABLED = 1;

  /**
   * The number of digits in a Google account ID, which includes {@link
   * #GOOGLE_ACCOUNT_ID_PREFIX_LENGTH}
   */
  private static final int GOOGLE_ACCOUNT_ID_LENGTH = 21;

  private static final int GOOGLE_ACCOUNT_ID_PREFIX_LENGTH = 1;

  private DevicePolicyManager mDevicePolicyManager;
  private ComponentName mAdminComponentName;

  private List<String> mAccounts = new ArrayList<>();
  private boolean mEnabled;

  private FrpAccountsAdapter mAccountsAdapter;
  private Spinner mFrpEnabledSpinner;

  @Override
  public void onCreate(Bundle savedInstanceState) {
    mDevicePolicyManager =
        (DevicePolicyManager) getActivity().getSystemService(Context.DEVICE_POLICY_SERVICE);
    mAdminComponentName = DeviceAdminReceiver.getComponentName(getActivity());
    super.onCreate(savedInstanceState);
    getActivity().getActionBar().setTitle(R.string.factory_reset_protection_policy);
  }

  @Override
  public View onCreateView(
      LayoutInflater inflater, final ViewGroup container, Bundle savedInstanceState) {
    super.onCreateView(inflater, container, savedInstanceState);
    View view = inflater.inflate(R.layout.factory_reset_protection_policy, container, false);

    ListView frpAccounts = view.findViewById(R.id.frp_accounts);
    mAccountsAdapter = new FrpAccountsAdapter();
    frpAccounts.setAdapter(mAccountsAdapter);

    Button addAccountButton = view.findViewById(R.id.add_account_button);
    addAccountButton.setOnClickListener(this);
    Button clearButton = view.findViewById(R.id.clear_frp_button);
    clearButton.setOnClickListener(this);
    Button saveButton = view.findViewById(R.id.save_frp_button);
    saveButton.setOnClickListener(this);

    mFrpEnabledSpinner = view.findViewById(R.id.frp_enabled);
    mFrpEnabledSpinner.setOnItemSelectedListener(this);
    ArrayAdapter<CharSequence> enabledAdapter =
        ArrayAdapter.createFromResource(
            getActivity(),
            R.array.factory_reset_protection_policy_enabled_choices,
            android.R.layout.simple_spinner_item);
    enabledAdapter.setDropDownViewResource(android.R.layout.simple_spinner_dropdown_item);
    mFrpEnabledSpinner.setAdapter(enabledAdapter);

    refreshUi();
    return view;
  }

  private void refreshUi() {
    mAccountsAdapter.clear();
    mFrpEnabledSpinner.setSelection(DISABLED);

    FactoryResetProtectionPolicy mFrpPolicy =
        mDevicePolicyManager.getFactoryResetProtectionPolicy(mAdminComponentName);
    if (mFrpPolicy != null) {
      mAccountsAdapter.addAll(mFrpPolicy.getFactoryResetProtectionAccounts());
      mFrpEnabledSpinner.setSelection(
          mFrpPolicy.isFactoryResetProtectionEnabled() ? ENABLED : DISABLED);
    }
  }

  private class FrpAccountsAdapter extends ArrayAdapter<String> {

    public FrpAccountsAdapter() {
      super(getActivity(), R.layout.factory_reset_protection_policy_account, mAccounts);
    }

    @Override
    public View getView(int position, View view, ViewGroup parent) {
      if (view == null) {
        view =
            getActivity()
                .getLayoutInflater()
                .inflate(R.layout.factory_reset_protection_policy_account, parent, false);
      }
      TextView listItemText = view.findViewById(R.id.frp_account);
      listItemText.setText(mAccounts.get(position));

      Button removeAccountButton = view.findViewById(R.id.remove_account_button);
      removeAccountButton.setOnClickListener(
          v -> {
            mAccounts.remove(position);
            notifyDataSetChanged();
          });

      return view;
    }
  }

  @Override
  public void onClick(View view) {
    int id = view.getId();
    if (id == R.id.add_account_button) {
      createAddAccountDialog();
    } else if (id == R.id.clear_frp_button) {
      mDevicePolicyManager.setFactoryResetProtectionPolicy(mAdminComponentName, null);
      refreshUi();
      showToast(R.string.cleared_factory_reset_protection_policy);
    } else if (id == R.id.save_frp_button) {
      mDevicePolicyManager.setFactoryResetProtectionPolicy(
          mAdminComponentName,
          new FactoryResetProtectionPolicy.Builder()
              .setFactoryResetProtectionAccounts(mAccounts)
              .setFactoryResetProtectionEnabled(mEnabled)
              .build());
      showToast(R.string.saved_factory_reset_protection_policy);
    }
  }

  public void createAddAccountDialog() {
    View view = LayoutInflater.from(getActivity()).inflate(R.layout.simple_edittext, null);
    final EditText input = view.findViewById(R.id.input);

    final AlertDialog dialog =
        new AlertDialog.Builder(getActivity())
            .setTitle(R.string.add_account)
            .setMessage(R.string.factory_reset_protection_policy_account_id_msg)
            .setView(view)
            .setPositiveButton(android.R.string.ok, null)
            .setNegativeButton(android.R.string.cancel, null)
            .create();
    dialog.setOnShowListener(
        dialogInterface ->
            dialog
                .getButton(AlertDialog.BUTTON_POSITIVE)
                .setOnClickListener(
                    okButtonView -> {
                      String item = input.getText().toString();
                      if (!isValidAccountId(item)) {
                        showToast(R.string.fail_to_add_account);
                        return;
                      }
                      mAccountsAdapter.add(item);
                      dialog.dismiss();
                    }));
    dialog.show();
  }

  @Override
  public void onItemSelected(AdapterView<?> parent, View view, int pos, long id) {
    if (parent.getId() == R.id.frp_enabled) {
      switch (pos) {
        case DISABLED:
          mEnabled = false;
          break;
        case ENABLED:
          mEnabled = true;
          break;
      }
    }
  }

  @Override
  public void onNothingSelected(AdapterView<?> adapterView) {
    // do nothing
  }

  private void showToast(@StringRes int stringResId) {
    Toast.makeText(getActivity(), stringResId, Toast.LENGTH_LONG).show();
  }

  /**
   * Returns whether the given string is a valid Google account ID, which are numeric strings
   * that are exactly {@value #GOOGLE_ACCOUNT_ID_LENGTH} digits in length.
   */
  private boolean isValidAccountId(String accountId) {
    if (TextUtils.isEmpty(accountId)) {
      return false;
    }

    if (accountId.length() != GOOGLE_ACCOUNT_ID_LENGTH) {
      return false;
    }

    try {
      // Strip the prefix and verify that the rest of the ID can be parsed as a long
      Long.parseUnsignedLong(accountId.substring(GOOGLE_ACCOUNT_ID_PREFIX_LENGTH));
      return true;
    } catch (NumberFormatException ex) {
      return false;
    }
  }
}
