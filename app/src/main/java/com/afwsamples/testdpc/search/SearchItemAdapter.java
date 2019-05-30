package com.afwsamples.testdpc.search;

import androidx.recyclerview.widget.RecyclerView;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.TextView;

import com.afwsamples.testdpc.R;
import com.afwsamples.testdpc.search.SearchItemAdapter.SearchItemViewHolder;

import java.util.ArrayList;
import java.util.List;

/**
 * Represent rows of search result in {@link PolicySearchFragment}.
 */
public class SearchItemAdapter extends RecyclerView.Adapter<SearchItemViewHolder> {
    private List<PreferenceIndex> mPreferenceIndexList = new ArrayList<>();
    private OnItemClickListener mOnItemClickListener;

    public SearchItemAdapter(OnItemClickListener onItemClickListener) {
        mOnItemClickListener = onItemClickListener;
    }

    @Override
    public SearchItemViewHolder onCreateViewHolder(ViewGroup parent, int viewType) {
        View itemView = LayoutInflater.from(parent.getContext()).
                inflate(R.layout.search_result_item, parent, false);
        return new SearchItemViewHolder(itemView);
    }

    @Override
    public void onBindViewHolder(final SearchItemViewHolder holder, int position) {
        final PreferenceIndex preferenceIndex = mPreferenceIndexList.get(position);
        holder.textView.setText(preferenceIndex.title);
        holder.textView.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                final int adapterPosition = holder.getAdapterPosition();
                PreferenceIndex clickedItem = mPreferenceIndexList.get(adapterPosition);
                mOnItemClickListener.onItemClick(clickedItem);
            }
        });
    }

    @Override
    public int getItemCount() {
        return mPreferenceIndexList.size();
    }

    public void setSearchResult(List<PreferenceIndex> list) {
        mPreferenceIndexList = list;
    }

    public interface OnItemClickListener {
        void onItemClick(PreferenceIndex preferenceIndex);
    }

    public static class SearchItemViewHolder extends RecyclerView.ViewHolder {
        public TextView textView;

        public SearchItemViewHolder(View itemView) {
            super(itemView);
            textView = (TextView) itemView;
        }
    }
}

