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

import android.text.Editable;
import android.text.TextWatcher;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.EditText;
import android.widget.ImageView;
import androidx.recyclerview.widget.RecyclerView;
import com.afwsamples.testdpc.R;
import java.util.ArrayList;
import java.util.List;

public class StringArrayTypeInputAdapter
    extends RecyclerView.Adapter<StringArrayTypeInputAdapter.ViewHolder> {
  private List<String> mStringList;

  public StringArrayTypeInputAdapter() {
    mStringList = new ArrayList<>();
  }

  @Override
  public ViewHolder onCreateViewHolder(ViewGroup parent, int viewType) {
    View view =
        LayoutInflater.from(parent.getContext()).inflate(R.layout.string_array_row, parent, false);
    return new ViewHolder(view);
  }

  @Override
  public void onBindViewHolder(final ViewHolder holder, final int position) {
    holder.stringValue.setText(mStringList.get(position));
    if (holder.textWatcher != null) {
      holder.stringValue.removeTextChangedListener(holder.textWatcher);
    }
    holder.textWatcher = createEditTextTextWatcher(holder);
    holder.stringValue.addTextChangedListener(holder.textWatcher);
    holder.delete.setOnClickListener(
        new View.OnClickListener() {
          @Override
          public void onClick(View view) {
            int adapterPosition = holder.getAdapterPosition();
            // make sure the view is not removed yet.
            if (adapterPosition != -1) {
              mStringList.remove(adapterPosition);
              notifyItemRemoved(adapterPosition);
            }
          }
        });
  }

  @Override
  public int getItemCount() {
    return mStringList.size();
  }

  public List<String> getStringList() {
    return mStringList;
  }

  public void setStringList(List<String> stringList) {
    mStringList = stringList;
    notifyDataSetChanged();
  }

  public class ViewHolder extends RecyclerView.ViewHolder {
    public EditText stringValue;
    public ImageView delete;
    public TextWatcher textWatcher;

    public ViewHolder(View view) {
      super(view);
      stringValue = (EditText) view.findViewById(R.id.string_input);
      delete = (ImageView) view.findViewById(R.id.delete_row);
    }
  }

  private TextWatcher createEditTextTextWatcher(final ViewHolder viewHolder) {
    return new TextWatcher() {
      @Override
      public void beforeTextChanged(CharSequence charSequence, int i, int i1, int i2) {}

      @Override
      public void onTextChanged(CharSequence charSequence, int i, int i1, int i2) {}

      @Override
      public void afterTextChanged(Editable editable) {
        mStringList.set(viewHolder.getAdapterPosition(), editable.toString());
      }
    };
  }
}
