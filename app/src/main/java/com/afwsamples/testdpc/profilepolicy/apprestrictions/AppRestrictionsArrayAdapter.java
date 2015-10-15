/*
 * Copyright (C) 2015 The Android Open Source Project
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

package com.afwsamples.testdpc.profilepolicy.apprestrictions;

import android.app.AlertDialog;
import android.content.Context;
import android.content.DialogInterface;
import android.content.RestrictionEntry;
import android.os.Build;
import android.text.InputType;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.view.ViewParent;
import android.view.WindowManager;
import android.view.inputmethod.EditorInfo;
import android.widget.AdapterView;
import android.widget.ArrayAdapter;
import android.widget.EditText;
import android.widget.ListView;
import android.widget.Spinner;
import android.widget.Switch;
import android.widget.TextView;
import android.widget.Toast;

import com.afwsamples.testdpc.R;

import java.util.ArrayList;
import java.util.Collections;
import java.util.Comparator;
import java.util.List;

/**
 * Renders a list of app restrictions with key, value and value-type input fields.
 */
public class AppRestrictionsArrayAdapter extends ArrayAdapter<RestrictionEntry>
        implements View.OnClickListener {
    private static final String[] APP_RESTRICTION_TYPES = {
            "Boolean",
            "Integer",
            "String",
            "String[]",
            "Bundle",
            "Bundle[]"};

    private static final String[] PRE_M_APP_RESTRICTION_TYPES = {
            "Boolean",
            "Integer",
            "String",
            "String[]"};

    // Order comes from @string-array/restriction_types.
    private static final int TYPE_BOOLEAN_INDEX = 0;
    private static final int TYPE_INTEGER_INDEX = 1;
    private static final int TYPE_STRING_INDEX = 2;
    private static final int TYPE_MULTI_SELECT_INDEX = 3;
    private static final int TYPE_BUNDLE_INDEX = 4;
    private static final int TYPE_BUNDLE_ARRAY_INDEX = 5;

    private BaseAppRestrictionsFragment mFragment;
    private List<RestrictionEntry> mLastSavedRestrictions;
    private String mAppName;
    private List<RestrictionEntry> mEntries;

    public AppRestrictionsArrayAdapter(Context context, List<RestrictionEntry> entries,
            BaseAppRestrictionsFragment fragment, String appName) {
        super(context, 0, entries);
        mFragment = fragment;
        mLastSavedRestrictions = new ArrayList<RestrictionEntry>(entries);
        mEntries = entries;
        mAppName = appName;
    }

    @Override
    public View getView(final int position, View convertView, ViewGroup parent) {
        AppRestrictionsViewHolder viewHolder;
        if (convertView == null) {
            convertView = LayoutInflater.from(getContext()).inflate(
                    R.layout.app_restrictions_row, parent, false);
            convertView.findViewById(R.id.edit_row).setOnClickListener(this);
            convertView.findViewById(R.id.delete_row).setOnClickListener(this);

            viewHolder = new AppRestrictionsViewHolder();
            convertView.setTag(viewHolder);
            viewHolder.restrictionKeyText = (TextView) convertView.findViewById(
                    R.id.restriction_key);
        } else {
            viewHolder = (AppRestrictionsViewHolder) convertView.getTag();
        }
        viewHolder.restrictionEntry = getItem(position);
        viewHolder.restrictionKeyText.setText(viewHolder.restrictionEntry.getKey());
        viewHolder.entryPosition = position;
        return convertView;
    }

    @Override
    public void onClick(View view) {
        ViewParent parentView = view.getParent();
        if (!(parentView instanceof View) || ((View) parentView).getTag() == null) {
            return;
        }

        final AppRestrictionsViewHolder viewHolder =
                (AppRestrictionsViewHolder) ((View) parentView).getTag();
        final RestrictionEntry restrictionEntry = viewHolder.restrictionEntry;
        if (view.getId() == R.id.edit_row) {
            switch (restrictionEntry.getType()) {
                case RestrictionEntry.TYPE_BOOLEAN:
                    showEditDialog(restrictionEntry);
                    break;
                case RestrictionEntry.TYPE_INTEGER:
                    showEditDialog(restrictionEntry);
                    break;
                case RestrictionEntry.TYPE_STRING:
                    showEditDialog(restrictionEntry);
                    break;
                case RestrictionEntry.TYPE_MULTI_SELECT:
                    showEditDialog(restrictionEntry);
                    break;
                case RestrictionEntry.TYPE_BUNDLE:
                case RestrictionEntry.TYPE_BUNDLE_ARRAY:
                    BundleTypeRestrictionsFragment fragment =
                            BundleTypeRestrictionsFragment.newInstance(restrictionEntry,
                            viewHolder.entryPosition, restrictionEntry.getKey(), mAppName);
                    fragment.setTargetFragment(mFragment, 0);
                    mFragment.getFragmentManager().beginTransaction()
                            .addToBackStack(BundleTypeRestrictionsFragment.class.getName())
                            .hide(mFragment)
                            .add(R.id.container, fragment)
                            .commit();
                    break;
            }
        } else if (view.getId() == R.id.delete_row) {
            remove(restrictionEntry);
        }
    }

    private void showEditDialog(final RestrictionEntry restrictionEntry) {
        final View dialogView = LayoutInflater.from(getContext()).inflate(
                R.layout.edit_restriction_layout, null);

        final StringArrayInputArrayAdapter stringArrayInputArrayAdapter =
                new StringArrayInputArrayAdapter(getContext(), 0, new ArrayList<String>());
        stringArrayInputArrayAdapter.add("");
        ListView listView = (ListView) dialogView.findViewById(R.id.value_str_array);
        listView.setAdapter(stringArrayInputArrayAdapter);
        listView.setDescendantFocusability(ViewGroup.FOCUS_AFTER_DESCENDANTS);

        final EditText keyText = (EditText) dialogView.findViewById(R.id.restriction_key);
        final Spinner typeSpinner = (Spinner) dialogView.findViewById(R.id.type_spinner);
        final ArrayAdapter<String> typeArrayAdapter;
        // Bundle and Bundle[] are added in M, so we disable these options in the spinner
        // for pre-M.
        if (Build.VERSION.SDK_INT < Build.VERSION_CODES.M) {
            typeArrayAdapter = new ArrayAdapter<String>(getContext(),
                    android.R.layout.simple_spinner_dropdown_item, PRE_M_APP_RESTRICTION_TYPES);
        } else {
            typeArrayAdapter = new ArrayAdapter<String>(getContext(),
                    android.R.layout.simple_spinner_dropdown_item, APP_RESTRICTION_TYPES);
        }
        typeSpinner.setAdapter(typeArrayAdapter);

        if (restrictionEntry != null) {
            keyText.setText(restrictionEntry.getKey());
            updateDialogValueFromRestriction(dialogView, restrictionEntry,
                    stringArrayInputArrayAdapter);
            typeSpinner.setSelection(getTypeIndexFromRestrictionType(restrictionEntry.getType()));
        }

        typeSpinner.setOnItemSelectedListener(new AdapterView.OnItemSelectedListener() {
            @Override
            public void onItemSelected(AdapterView<?> parent, View view, int pos, long id) {
                updateValueVisibilities(dialogView, pos);
            }

            @Override
            public void onNothingSelected(AdapterView<?> parent) {
                // Do nothing.
            }
        });

        final AlertDialog alertDialog = new AlertDialog.Builder(getContext())
                .setView(dialogView)
                // Listener for save button will be set after calling show().
                .setPositiveButton(R.string.restriction_save_label, null)
                .setNegativeButton(R.string.restriction_cancel_label, null)
                .create();
        alertDialog.show();

        alertDialog.getButton(DialogInterface.BUTTON_POSITIVE).setOnClickListener(
                new View.OnClickListener() {
                    @Override
                    public void onClick(View view) {
                        if (keyText.length() == 0) {
                            showToast(R.string.key_empty_error);
                            return;
                        }

                        final int selectedRestrictionType = getRestrictionTypeFromTypeIndex(
                                typeSpinner.getSelectedItemPosition());
                        RestrictionEntry newRestrictionEntry = new RestrictionEntry(
                                selectedRestrictionType, keyText.getText().toString());
                        if (!updateRestrictionValueFromDialog(dialogView, newRestrictionEntry,
                                stringArrayInputArrayAdapter)) {
                            showToast(R.string.value_not_valid);
                            return;
                        }
                        if (restrictionEntry != null) {
                            remove(restrictionEntry);
                        }
                        add(newRestrictionEntry);
                        alertDialog.dismiss();
                    }
                }
        );
        alertDialog.getWindow().clearFlags(WindowManager.LayoutParams.FLAG_NOT_FOCUSABLE |
                WindowManager.LayoutParams.FLAG_ALT_FOCUSABLE_IM);
        alertDialog.getWindow().setSoftInputMode(
                WindowManager.LayoutParams.SOFT_INPUT_ADJUST_RESIZE);
    }

    public void addNewEntry() {
        showEditDialog(null);
    }

    private void updateDialogValueFromRestriction(View dialogView,
            RestrictionEntry restrictionEntry, StringArrayInputArrayAdapter arrayAdapter) {
        switch (restrictionEntry.getType()) {
            case RestrictionEntry.TYPE_BOOLEAN:
                Switch valueSwitch = (Switch) dialogView.findViewById(R.id.value_bool);
                valueSwitch.setChecked(restrictionEntry.getSelectedState());
                break;
            case RestrictionEntry.TYPE_INTEGER:
                EditText intValueText = (EditText) dialogView.findViewById(R.id.value_int_str);
                intValueText.setText(String.valueOf(restrictionEntry.getIntValue()));
                break;
            case RestrictionEntry.TYPE_STRING:
                EditText strValueText = (EditText) dialogView.findViewById(R.id.value_int_str);
                strValueText.setText(restrictionEntry.getSelectedString());
                break;
            case RestrictionEntry.TYPE_MULTI_SELECT:
                ListView listView = (ListView) dialogView.findViewById(R.id.value_str_array);
                listView.setAdapter(arrayAdapter);
                arrayAdapter.clear();
                String[] strArray = restrictionEntry.getAllSelectedStrings();
                if (strArray != null && strArray.length != 0) {
                    arrayAdapter.addAll(strArray);
                } else {
                    // TODO (b/23718355): We display add and delete buttons with the edit boxes. If
                    // the strArray is null or empty, then no edit boxes will be displayed and user
                    // has no way of adding new strings. So, we add an empty string to the list.
                    arrayAdapter.add("");
                }
                break;
        }
    }

    private boolean updateRestrictionValueFromDialog(View dialogView,
            RestrictionEntry restrictionEntry, StringArrayInputArrayAdapter arrayAdapter) {
        switch (restrictionEntry.getType()) {
            case RestrictionEntry.TYPE_BOOLEAN:
                Switch valueSwitch = (Switch) dialogView.findViewById(R.id.value_bool);
                restrictionEntry.setSelectedState(valueSwitch.isChecked());
                return true;
            case RestrictionEntry.TYPE_INTEGER:
                EditText intValueText = (EditText) dialogView.findViewById(R.id.value_int_str);
                try {
                    restrictionEntry.setIntValue(
                            Integer.parseInt(intValueText.getText().toString()));
                    return true;
                } catch (NumberFormatException e) {
                    return false;
                }
            case RestrictionEntry.TYPE_STRING:
                EditText strValueText = (EditText) dialogView.findViewById(R.id.value_int_str);
                restrictionEntry.setSelectedString(strValueText.getText().toString());
                return true;
            case RestrictionEntry.TYPE_MULTI_SELECT:
                arrayAdapter.onSave();
                int size = arrayAdapter.getCount();
                ArrayList<String> strList = new ArrayList<String>();
                for (int i = 0; i < size; ++i) {
                    String adapterItem = arrayAdapter.getItem(i);
                    if (adapterItem != null && !adapterItem.isEmpty()) {
                        strList.add(adapterItem);
                    }
                }
                restrictionEntry.setAllSelectedStrings(strList.toArray(new String[0]));
                return true;
            case RestrictionEntry.TYPE_BUNDLE:
            case RestrictionEntry.TYPE_BUNDLE_ARRAY:
                if (restrictionEntry.getRestrictions() == null) {
                    restrictionEntry.setRestrictions(new RestrictionEntry[0]);
                }
                return true;
        }
        return false;
    }

    private void updateValueVisibilities(View dialogView, int typeIndex) {
        // First reset to the initial state.
        final Switch boolValue = (Switch) dialogView.findViewById(R.id.value_bool);
        final EditText intStrValue = (EditText) dialogView.findViewById(R.id.value_int_str);
        final ListView strArrayValue = (ListView) dialogView.findViewById(R.id.value_str_array);
        final TextView bundleValue = (TextView) dialogView.findViewById(R.id.value_bundle);
        final TextView valueView = (TextView) dialogView.findViewById(R.id.value_text);

        boolValue.setVisibility(View.GONE);
        intStrValue.setVisibility(View.GONE);
        strArrayValue.setVisibility(View.GONE);
        bundleValue.setVisibility(View.GONE);
        valueView.setVisibility(View.VISIBLE);

        // Now make the required type visible.
        switch (typeIndex) {
            case TYPE_BOOLEAN_INDEX:
                boolValue.setVisibility(View.VISIBLE);
                break;
            case TYPE_INTEGER_INDEX:
                intStrValue.setInputType(InputType.TYPE_CLASS_NUMBER);
                intStrValue.setVisibility(View.VISIBLE);
            case TYPE_STRING_INDEX:
                intStrValue.setInputType(EditorInfo.TYPE_TEXT_FLAG_NO_SUGGESTIONS);
                intStrValue.setVisibility(View.VISIBLE);
                break;
            case TYPE_MULTI_SELECT_INDEX:
                strArrayValue.setVisibility(View.VISIBLE);
                valueView.setVisibility(View.GONE);
                break;
            case TYPE_BUNDLE_INDEX:
            case TYPE_BUNDLE_ARRAY_INDEX:
                bundleValue.setVisibility(View.VISIBLE);
                valueView.setVisibility(View.GONE);
                break;
        }
    }

    private int getTypeIndexFromRestrictionType(int restrictionType) {
        switch (restrictionType) {
            case RestrictionEntry.TYPE_BOOLEAN:
                return TYPE_BOOLEAN_INDEX;
            case RestrictionEntry.TYPE_INTEGER:
                return TYPE_INTEGER_INDEX;
            case RestrictionEntry.TYPE_STRING:
                return TYPE_STRING_INDEX;
            case RestrictionEntry.TYPE_MULTI_SELECT:
                return TYPE_MULTI_SELECT_INDEX;
            case RestrictionEntry.TYPE_BUNDLE:
                return TYPE_BUNDLE_INDEX;
            case RestrictionEntry.TYPE_BUNDLE_ARRAY:
                return TYPE_BUNDLE_ARRAY_INDEX;
            default:
                throw new AssertionError("Unknown restriction type");
        }
    }

    private int getRestrictionTypeFromTypeIndex(int typeIndex) {
        switch (typeIndex) {
            case TYPE_BOOLEAN_INDEX:
                return RestrictionEntry.TYPE_BOOLEAN;
            case TYPE_INTEGER_INDEX:
                return RestrictionEntry.TYPE_INTEGER;
            case TYPE_STRING_INDEX:
                return RestrictionEntry.TYPE_STRING;
            case TYPE_MULTI_SELECT_INDEX:
                return RestrictionEntry.TYPE_MULTI_SELECT;
            case TYPE_BUNDLE_INDEX:
                return RestrictionEntry.TYPE_BUNDLE;
            case TYPE_BUNDLE_ARRAY_INDEX:
                return RestrictionEntry.TYPE_BUNDLE_ARRAY;
            default:
                throw new AssertionError("Unknown type index");
        }
    }

    @Override
    public void notifyDataSetChanged() {
        if (mEntries != null) {
            Collections.sort(mEntries, new Comparator<RestrictionEntry>() {
                @Override
                public int compare(RestrictionEntry entry1, RestrictionEntry entry2) {
                    return entry1.getKey().compareTo(entry2.getKey());
                }
            });
        }
        super.notifyDataSetChanged();
    }

    public void setSavedRestrictions(List<RestrictionEntry> entries) {
        mLastSavedRestrictions = new ArrayList<>(entries);
    }

    public void resetAppRestrictions() {
        clear();
        addAll(mLastSavedRestrictions);
    }

    private static class AppRestrictionsViewHolder {
        RestrictionEntry restrictionEntry;
        TextView restrictionKeyText;
        int entryPosition;
    }

    private void showToast(int strResId) {
        Toast.makeText(getContext(), strResId, Toast.LENGTH_SHORT).show();
    }
}
