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

package com.afwsamples.testdpc.policy.keyguard;

import android.app.Fragment;
import android.app.admin.DevicePolicyManager;
import android.content.ComponentName;
import android.content.Context;
import android.os.Bundle;
import android.text.Editable;
import android.text.TextUtils;
import android.text.TextWatcher;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Button;
import android.widget.EditText;
import android.widget.RadioButton;
import android.widget.RadioGroup;
import android.widget.Toast;

import com.afwsamples.testdpc.DeviceAdminReceiver;
import com.afwsamples.testdpc.R;

import java.util.LinkedHashMap;
import java.util.Map;

/**
 * This fragment provides functionalities to set password constraint policies as a profile
 * or device owner.
 *
 * <p>These include:
 * <ul>
 * <li>{@link DevicePolicyManager#setPasswordMinimumLength(ComponentName, String)}</li>
 * <li>{@link DevicePolicyManager#setPasswordMinimumLetters(ComponentName, String)}</li>
 * <li>{@link DevicePolicyManager#setPasswordMinimumNumeric(ComponentName, String)}</li>
 * <li>{@link DevicePolicyManager#setPasswordMinimumLowerCase(ComponentName, String)}</li>
 * <li>{@link DevicePolicyManager#setPasswordMinimumUpperCase(ComponentName, String)}</li>
 * <li>{@link DevicePolicyManager#setPasswordMinimumSymbols(ComponentName, String)}</li>
 * <li>{@link DevicePolicyManager#setPasswordMinimumNonLetter(ComponentName, String)}</li>
 * </ul>
 */
public final class PasswordConstraintsFragment extends Fragment implements
        RadioGroup.OnCheckedChangeListener, TextWatcher {

    private static final Map<Integer, Integer> PASSWORD_QUALITIES = new LinkedHashMap<>(7);
    static {
        // IDs of settings for {@link DevicePolicyManager#setPasswordQuality(ComponentName, int)}.
        final int[] policyIds = new int[] {
            DevicePolicyManager.PASSWORD_QUALITY_UNSPECIFIED,
            DevicePolicyManager.PASSWORD_QUALITY_SOMETHING,
            DevicePolicyManager.PASSWORD_QUALITY_NUMERIC,
            DevicePolicyManager.PASSWORD_QUALITY_NUMERIC_COMPLEX,
            DevicePolicyManager.PASSWORD_QUALITY_ALPHABETIC,
            DevicePolicyManager.PASSWORD_QUALITY_ALPHANUMERIC,
            DevicePolicyManager.PASSWORD_QUALITY_COMPLEX
        };
        // Strings to show for each password quality setting.
        final int[] policyNames = new int[] {
            R.string.password_quality_unspecified,
            R.string.password_quality_something,
            R.string.password_quality_numeric,
            R.string.password_quality_numeric_complex,
            R.string.password_quality_alphabetic,
            R.string.password_quality_alphanumeric,
            R.string.password_quality_complex
        };
        if (policyIds.length != policyNames.length) {
            throw new AssertionError("Number of items in policyIds and policyNames do not match");
        }
        for (int i = 0; i < policyIds.length; i++) {
            PASSWORD_QUALITIES.put(policyIds[i], policyNames[i]);
        }
    };

    // Radio list of all complexity settings, as defined above.
    private RadioGroup mQualityGroup;

    // Individual minimum password attribute requirements.
    private EditText mMinLength;
    private EditText mMinLetters;
    private EditText mMinNumeric;
    private EditText mMinLowerCase;
    private EditText mMinUpperCase;
    private EditText mMinSymbols;
    private EditText mMinNonLetter;

    private DevicePolicyManager mDpm;
    private ComponentName mAdminComponent;

    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        getActivity().getActionBar().setTitle(R.string.password_constraints);

        mDpm = (DevicePolicyManager) getActivity().getSystemService(Context.DEVICE_POLICY_SERVICE);
        mAdminComponent = DeviceAdminReceiver.getComponentName(getActivity());
    }

    @Override
    public View onCreateView(LayoutInflater layoutInflater, ViewGroup container,
            Bundle savedInstanceState) {
        final View root = layoutInflater.inflate(R.layout.password_quality, null);

        // Create numeric text fields
        mMinLength = findAndPrepareField(root, R.id.password_min_length);
        mMinLetters = findAndPrepareField(root, R.id.password_min_letters);
        mMinNumeric = findAndPrepareField(root, R.id.password_min_numeric);
        mMinLowerCase = findAndPrepareField(root, R.id.password_min_lowercase);
        mMinUpperCase = findAndPrepareField(root, R.id.password_min_uppercase);
        mMinSymbols = findAndPrepareField(root, R.id.password_min_symbols);
        mMinNonLetter = findAndPrepareField(root, R.id.password_min_nonletter);

        // Create radio group for password quality
        mQualityGroup = (RadioGroup) root.findViewById(R.id.password_quality);
        for (Map.Entry<Integer, Integer> entry : PASSWORD_QUALITIES.entrySet()) {
            final RadioButton choice = new RadioButton(getContext());
            choice.setId(entry.getKey());
            choice.setText(entry.getValue());
            mQualityGroup.addView(choice);
        }
        mQualityGroup.setOnCheckedChangeListener(this);

        return root;
    }

    @Override
    public void onResume() {
        super.onResume();

        // Set the password quality radio group to show the requirement, if there is one.
        mQualityGroup.check(mDpm.getPasswordQuality(mAdminComponent));

        // Update all of our minimum requirement fields via getPasswordMinimum(.*)
        mMinLength.setText(Integer.toString(mDpm.getPasswordMinimumLength(mAdminComponent)));
        mMinLetters.setText(Integer.toString(mDpm.getPasswordMinimumLetters(mAdminComponent)));
        mMinNumeric.setText(Integer.toString(mDpm.getPasswordMinimumNumeric(mAdminComponent)));
        mMinLowerCase.setText(Integer.toString(mDpm.getPasswordMinimumLowerCase(mAdminComponent)));
        mMinUpperCase.setText(Integer.toString(mDpm.getPasswordMinimumUpperCase(mAdminComponent)));
        mMinSymbols.setText(Integer.toString(mDpm.getPasswordMinimumSymbols(mAdminComponent)));
        mMinNonLetter.setText(Integer.toString(mDpm.getPasswordMinimumNonLetter(mAdminComponent)));
    }

    @Override
    public void onCheckedChanged(RadioGroup view, int checkedId) {
        if (view == mQualityGroup) {
            mDpm.setPasswordQuality(mAdminComponent, checkedId);
        }
    }

    @Override
    public void afterTextChanged(Editable editable) {
        if (TextUtils.isEmpty(editable.toString())) {
            return;
        }

        final int value;
        try {
            value = Integer.parseInt(editable.toString());
        } catch (NumberFormatException e) {
            Toast.makeText(getActivity(), R.string.not_valid_input, Toast.LENGTH_SHORT).show();
            return;
        }

        if (editable == mMinLength.getEditableText()) {
            mDpm.setPasswordMinimumLength(mAdminComponent, value);
        } else if (editable == mMinLetters.getEditableText()) {
            mDpm.setPasswordMinimumLetters(mAdminComponent, value);
        } else if (editable == mMinNumeric.getEditableText()) {
            mDpm.setPasswordMinimumNumeric(mAdminComponent, value);
        } else if (editable == mMinLowerCase.getEditableText()) {
            mDpm.setPasswordMinimumLowerCase(mAdminComponent, value);
        } else if (editable == mMinUpperCase.getEditableText()) {
            mDpm.setPasswordMinimumUpperCase(mAdminComponent, value);
        } else if (editable == mMinSymbols.getEditableText()) {
            mDpm.setPasswordMinimumSymbols(mAdminComponent, value);
        } else if (editable == mMinNonLetter.getEditableText()) {
            mDpm.setPasswordMinimumNonLetter(mAdminComponent, value);
        }
    }

    @Override
    public void beforeTextChanged(CharSequence s, int start, int count, int after) {
    }

    @Override
    public void onTextChanged(CharSequence s, int start, int count, int after) {
    }

    private EditText findAndPrepareField(View root, final int id) {
        EditText field = (EditText) root.findViewById(id);
        field.addTextChangedListener(this);
        return field;
    }

}
