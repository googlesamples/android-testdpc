/*
 * Copyright 2015 Google Inc. All Rights Reserved.
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

package com.google.android.testdpc.profilepolicy.apprestrictions;

import android.content.Context;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.view.ViewParent;
import android.widget.ArrayAdapter;
import android.widget.EditText;

import com.google.android.testdpc.R;

import java.util.ArrayList;

/**
 * A dynamic view for viewing and editing String[] app restriction.
 */
public class StringArrayInputArrayAdapter extends ArrayAdapter<String> {

    public static final String TAG = "StringArrayInputArrayAdapter";

    private ArrayList<String> mInputList = null;

    private int mLastFocusedEditTextPos = -1;

    private EditText mLastFocusedEditText = null;

    private View.OnClickListener mAddNewRowOnClickListener = new View.OnClickListener() {
        @Override
        public void onClick(View v) {
            ViewParent viewParent = v.getParent();
            if (viewParent != null && viewParent instanceof View
                    && ((View) viewParent).getTag() != null) {
                StringArrayViewHolder viewHolder = (StringArrayViewHolder) ((View) viewParent)
                        .getTag();
                // Save the last row content immediately. Otherwise, it will be
                // overridden by the new row added below.
                mInputList.set(viewHolder.position, viewHolder.strVal.getText().toString());
                // Add a new row at the end of the list.
                mInputList.add("");
                notifyDataSetChanged();
            } else {
                Log.d(TAG, "Fail to find the view holder from the the add new row button.");
            }
        }
    };

    private View.OnFocusChangeListener mStrValOnFocusChangeListener
            = new View.OnFocusChangeListener() {
        @Override
        public void onFocusChange(View v, boolean hasFocus) {
            ViewParent viewParent = v.getParent();
            if (viewParent != null && viewParent instanceof View
                    && ((View) viewParent).getTag() != null) {
                StringArrayViewHolder viewHolder = (StringArrayViewHolder) ((View) viewParent)
                        .getTag();
                if (hasFocus) {
                    mLastFocusedEditTextPos = viewHolder.position;
                    mLastFocusedEditText = viewHolder.strVal;
                } else {
                    // Ignore the case where the delete
                    if (viewHolder.position < getCount()) {
                        mInputList.set(viewHolder.position, viewHolder.strVal.getText().toString());
                    }
                    if (mLastFocusedEditTextPos == viewHolder.position) {
                        mLastFocusedEditTextPos = -1;
                        mLastFocusedEditText = null;
                    }
                }
            } else {
                Log.d(TAG, "Fail to find the view holder from the string input EditText.");
            }
        }
    };

    private View.OnClickListener mDeleteRowOnClickListener = new View.OnClickListener() {
        @Override
        public void onClick(View v) {
            ViewParent viewParent = v.getParent();
            if (viewParent != null && viewParent instanceof View
                    && ((View) viewParent).getTag() != null) {
                StringArrayViewHolder viewHolder = (StringArrayViewHolder) ((View) viewParent)
                        .getTag();
                if (mLastFocusedEditTextPos == viewHolder.position) {
                    mLastFocusedEditTextPos = -1;
                    mLastFocusedEditText = null;
                }
                mInputList.remove(viewHolder.position);
                notifyDataSetChanged();
            } else {
                Log.d(TAG, "Fail to find the view holder from the delete row button.");
            }
        }
    };

    public StringArrayInputArrayAdapter(Context context, int resource,
            ArrayList<String> objects) {
        super(context, resource, objects);
        mInputList = objects;
    }

    @Override
    public View getView(int position, View convertView, ViewGroup parent) {
        if (convertView == null) {
            convertView = LayoutInflater.from(getContext())
                    .inflate(R.layout.app_restrictions_string_array_row, parent, false);
        }
        final StringArrayViewHolder viewHolder = new StringArrayViewHolder();
        viewHolder.position = position;
        viewHolder.strVal = (EditText) convertView.findViewById(R.id.string_input);
        convertView.setTag(viewHolder);
        viewHolder.strVal.setOnFocusChangeListener(mStrValOnFocusChangeListener);
        if (viewHolder.position == getCount() - 1) {
            convertView.findViewById(R.id.add_new_row).setVisibility(View.VISIBLE);
            convertView.findViewById(R.id.add_new_row)
                    .setOnClickListener(mAddNewRowOnClickListener);
        } else {
            convertView.findViewById(R.id.add_new_row).setVisibility(View.GONE);
        }
        viewHolder.strVal.setText(getItem(viewHolder.position));
        convertView.findViewById(R.id.delete_row).setOnClickListener(mDeleteRowOnClickListener);
        return convertView;
    }

    public void onSave() {
        if (mLastFocusedEditText != null && mLastFocusedEditTextPos != -1) {
            mInputList.set(mLastFocusedEditTextPos, mLastFocusedEditText.getText().toString());
        }
    }

    private class StringArrayViewHolder {
        int position = -1;
        EditText strVal = null;
    }
}
