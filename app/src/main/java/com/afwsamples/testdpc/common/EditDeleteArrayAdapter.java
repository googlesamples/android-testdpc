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

package com.afwsamples.testdpc.common;

import android.content.Context;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.view.ViewParent;
import android.widget.ArrayAdapter;
import android.widget.TextView;

import com.afwsamples.testdpc.R;

import java.util.Collections;
import java.util.Comparator;
import java.util.List;

/**
 * List of rows with edit and delete button.
 */
public abstract class EditDeleteArrayAdapter<T> extends ArrayAdapter<T>
        implements View.OnClickListener {

    private List<T> mEntries;
    private OnEditButtonClickListener mOnEditButtonClickListener;
    private OnDeleteButtonClickListener mOnDeleteButtonClickListener;

    public EditDeleteArrayAdapter(Context context, List<T> entries,
            OnEditButtonClickListener onEditButtonClickListener,
            OnDeleteButtonClickListener onDeleteButtonClickListener) {
        super(context, 0, entries);
        mEntries = entries;
        mOnEditButtonClickListener = onEditButtonClickListener;
        mOnDeleteButtonClickListener = onDeleteButtonClickListener;
    }

    @Override
    public View getView(final int position, View convertView, ViewGroup parent) {
        RowViewHolder<T> viewHolder;
        if (convertView == null) {
            convertView = LayoutInflater.from(getContext()).inflate(
                    R.layout.edit_delete_row, parent, false);
            convertView.findViewById(R.id.edit_row).setOnClickListener(this);
            convertView.findViewById(R.id.delete_row).setOnClickListener(this);

            viewHolder = new RowViewHolder<>();
            convertView.setTag(viewHolder);
            viewHolder.restrictionKeyText = (TextView) convertView.findViewById(
                    R.id.restriction_key);
        } else {
            viewHolder = (RowViewHolder) convertView.getTag();
        }
        viewHolder.entry = getItem(position);
        viewHolder.restrictionKeyText.setText(getDisplayName(viewHolder.entry));
        viewHolder.entryPosition = position;
        return convertView;
    }

    @Override
    public void onClick(View view) {
        ViewParent parentView = view.getParent();
        if (!(parentView instanceof View) || ((View) parentView).getTag() == null) {
            return;
        }
        final RowViewHolder<T> viewHolder =
                (RowViewHolder<T>) ((View) parentView).getTag();
        final T entry = viewHolder.entry;
        if (view.getId() == R.id.edit_row) {
            mOnEditButtonClickListener.onEditButtonClick(entry);
        } else if (view.getId() == R.id.delete_row) {
            remove(entry);
            if (mOnDeleteButtonClickListener != null) {
                mOnDeleteButtonClickListener.onDeleteButtonClick(entry);
            }
        }
    }

    public void set(int index, T item) {
        mEntries.set(index, item);
        notifyDataSetChanged();
    }

    protected abstract String getDisplayName(T entry);

    private static class RowViewHolder<T> {
        T entry;
        TextView restrictionKeyText;
        int entryPosition;
    }

    public interface OnEditButtonClickListener<T> {
        void onEditButtonClick(T entry);
    }

    public interface OnDeleteButtonClickListener<T> {
        void onDeleteButtonClick(T entry);
    }

    protected void sort() {
        if (mEntries != null) {
            Collections.sort(mEntries, new Comparator<T>() {
                @Override
                public int compare(T entry1, T entry2) {
                    return getDisplayName(entry1).compareTo(getDisplayName(entry2));
                }
            });
        }
    }
}
