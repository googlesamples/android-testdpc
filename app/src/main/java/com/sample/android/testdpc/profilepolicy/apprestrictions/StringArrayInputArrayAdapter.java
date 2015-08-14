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

package com.sample.android.testdpc.profilepolicy.apprestrictions;

import android.content.Context;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.view.ViewParent;
import android.widget.ArrayAdapter;
import android.widget.EditText;

import com.sample.android.testdpc.R;

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
        StringArrayViewHolder viewHolder;
        if (convertView == null) {
            convertView = LayoutInflater.from(getContext())
                    .inflate(R.layout.app_restrictions_string_array_row, parent, false);
            viewHolder = new StringArrayViewHolder();
            convertView.setTag(viewHolder);
            viewHolder.position = position;
            viewHolder.strVal = (EditText) convertView.findViewById(R.id.string_input);
            viewHolder.strVal.setOnFocusChangeListener(mStrValOnFocusChangeListener);
            viewHolder.addNewRowView = convertView.findViewById(R.id.add_new_row);
            convertView.findViewById(R.id.delete_row).setOnClickListener(mDeleteRowOnClickListener);
        } else {
            viewHolder = (StringArrayViewHolder) convertView.getTag();
        }

        viewHolder.position = position;
        // Visibility of add new row option will be "gone" by default and we make it "visible"
        // only for the last row.
        if (viewHolder.position == getCount() - 1) {
            // TODO: Update the layout to avoid having a add_new_row button
            // in every row.
            viewHolder.addNewRowView.setVisibility(View.VISIBLE);
            viewHolder.addNewRowView.setOnClickListener(mAddNewRowOnClickListener);
        } else {
            viewHolder.addNewRowView.setVisibility(View.GONE);
        }

        viewHolder.strVal.setText(getItem(viewHolder.position));
        return convertView;
    }

    public void onSave() {
        if (mLastFocusedEditText != null && mLastFocusedEditTextPos != -1) {
            mInputList.set(mLastFocusedEditTextPos, mLastFocusedEditText.getText().toString());
        }
    }

    private static class StringArrayViewHolder {
        int position = -1;
        EditText strVal = null;
        View addNewRowView = null;
    }
}
