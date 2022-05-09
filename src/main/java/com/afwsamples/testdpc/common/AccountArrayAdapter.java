/*
 * Copyright (C) 2018 The Android Open Source Project
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

package com.afwsamples.testdpc.common;

import android.accounts.Account;
import android.accounts.AccountManager;
import android.accounts.AuthenticatorDescription;
import android.content.Context;
import android.content.pm.PackageManager;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ArrayAdapter;
import android.widget.ImageView;
import android.widget.TextView;
import com.afwsamples.testdpc.R;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

/** A simple adapter which takes a list of accounts in a listview. */
public class AccountArrayAdapter extends ArrayAdapter<Account> {
  private static final String TAG = "AccountArrayAdapter";

  private PackageManager mPackageManager;
  private Map<String, AuthenticatorDescription> mAuthenticatorMap;

  public AccountArrayAdapter(Context context, int resource, List<Account> accountList) {
    super(context, resource, accountList);
    mPackageManager = context.getPackageManager();
    mAuthenticatorMap = new HashMap<>();
    AccountManager accountManager = AccountManager.get(context);
    for (AuthenticatorDescription authenticator : accountManager.getAuthenticatorTypes()) {
      mAuthenticatorMap.put(authenticator.type, authenticator);
    }
  }

  @Override
  public View getView(int position, View convertView, ViewGroup parent) {
    if (convertView == null) {
      convertView = LayoutInflater.from(getContext()).inflate(R.layout.account_row, parent, false);
    }

    Account account = getItem(position);
    AuthenticatorDescription authenticator = mAuthenticatorMap.get(account.type);
    final ImageView iconImageView = convertView.findViewById(R.id.account_type_icon);
    final TextView accountNameTextView = convertView.findViewById(R.id.account_name);
    iconImageView.setImageDrawable(
        mPackageManager.getDrawable(authenticator.packageName, authenticator.iconId, null));
    accountNameTextView.setText(account.name);
    return convertView;
  }
}
