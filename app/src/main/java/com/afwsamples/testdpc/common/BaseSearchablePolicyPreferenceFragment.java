package com.afwsamples.testdpc.common;

import android.content.Context;
import android.os.Bundle;
import androidx.preference.PreferenceFragment;
import androidx.preference.Preference;
import androidx.preference.PreferenceGroup;
import androidx.preference.PreferenceGroupAdapter;
import androidx.preference.PreferenceScreen;
import androidx.preference.PreferenceViewHolder;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;
import android.text.TextUtils;
import android.view.Menu;
import android.view.MenuInflater;
import android.view.MenuItem;
import android.view.View;

import com.afwsamples.testdpc.common.Dumpable;

import java.io.FileDescriptor;
import java.io.PrintWriter;

import com.afwsamples.testdpc.R;

/**
 * Base class of searchable policy preference fragment. With specified preference key,
 * it will scroll to the corresponding preference and highlight it.
 */
public abstract class BaseSearchablePolicyPreferenceFragment extends PreferenceFragment
        implements Dumpable {
    protected LinearLayoutManager mLayoutManager;
    private HighlightablePreferenceGroupAdapter mAdapter;
    private String mPreferenceKey;
    private boolean mPreferenceHighlighted = false;
    public static final String EXTRA_PREFERENCE_KEY = "preference_key";
    private static final String SAVE_HIGHLIGHTED_KEY = "preference_highlighted";
    private static final int DELAY_HIGHLIGHT_DURATION_MILLIS = 500;

    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        if (savedInstanceState != null) {
            mPreferenceHighlighted = savedInstanceState.getBoolean(SAVE_HIGHLIGHTED_KEY);
        }
        final Bundle args = getArguments();
        if (args != null) {
            mPreferenceKey = args.getString(EXTRA_PREFERENCE_KEY);
        }
        setHasOptionsMenu(true);
    }

    @Override
    public void onCreateOptionsMenu(Menu menu, MenuInflater inflater) {
        super.onCreateOptionsMenu(menu, inflater);
        // Hide the search icon when we are showing search result.
        MenuItem showSearchItem = menu.findItem(R.id.action_show_search);
        if (showSearchItem != null) {
            boolean isShowingSearchResult = !TextUtils.isEmpty(mPreferenceKey);
            showSearchItem.setVisible(!isShowingSearchResult);
        }
    }

    @Override
    public void onResume() {
        super.onResume();
        highlightPreferenceIfNeeded();
    }

    @Override
    public void onSaveInstanceState(Bundle outState) {
        super.onSaveInstanceState(outState);
        outState.putBoolean(SAVE_HIGHLIGHTED_KEY, mPreferenceHighlighted);
    }

    @Override
    public RecyclerView.LayoutManager onCreateLayoutManager() {
        mLayoutManager = new LinearLayoutManager(getActivity());
        return mLayoutManager;
    }

    @Override
    protected RecyclerView.Adapter onCreateAdapter(PreferenceScreen preferenceScreen) {
        mAdapter = new HighlightablePreferenceGroupAdapter(preferenceScreen);
        return mAdapter;
    }

    @Override // from Fragment
    public final void dump(String prefix, FileDescriptor fd, PrintWriter pw, String[] args) {
        dump(prefix, pw, fd, Dumpable.isQuietMode(args), args);
    }

    @Override // from Dumpable
    public final void dump(String prefix, PrintWriter pw, FileDescriptor fd, boolean quietModeOnly,
            String[] args) {
        // Dump its own state
        if (mAdapter == null) {
            pw.printf("%sno adapter\n", prefix);
        } else {
            pw.printf("%smHighlightPosition: %d\n", prefix, mAdapter.mHighlightPosition);
        }
        pw.printf("%smPreferenceKey: %s\n", prefix, mPreferenceKey);
        pw.printf("%smPreferenceHighlighted: %b\n", prefix, mPreferenceHighlighted);

        // Dump subclass state
        dump(prefix, pw, args);

        if (quietModeOnly) return;

        // Dump superclass state
        super.dump(prefix, fd, pw, args);

    }

    /**
     * Dumps just the subclass state.
     */
    protected void dump(String prefix, PrintWriter writer, String[] args) {

    }

    private void highlightPreferenceIfNeeded() {
        if (isAdded() && !mPreferenceHighlighted && !TextUtils.isEmpty(mPreferenceKey)) {
            highlightPreference(mPreferenceKey);
        }
    }

    private void highlightPreference(String key) {
        final int position = canUseListViewForHighLighting(key);
        if (position >= 0) {
            mPreferenceHighlighted = true;
            mLayoutManager.scrollToPosition(position);
            getView().postDelayed(new Runnable() {
                @Override
                public void run() {
                    mAdapter.highlight(position);
                }
            }, DELAY_HIGHLIGHT_DURATION_MILLIS);
        }
    }

    /**
     * Return a valid ListView position or -1 if none is found
     */
    private int canUseListViewForHighLighting(String key) {
        if (getListView() == null) {
            return -1;
        }

        RecyclerView listView = getListView();
        RecyclerView.Adapter adapter = listView.getAdapter();

        if (adapter != null && adapter instanceof PreferenceGroupAdapter) {
            return findListPositionFromKey((PreferenceGroupAdapter) adapter, key);
        }

        return -1;
    }

    private int findListPositionFromKey(PreferenceGroupAdapter adapter, String key) {
        final int count = adapter.getItemCount();
        for (int n = 0; n < count; n++) {
            final Preference preference = adapter.getItem(n);
            final String preferenceKey = preference.getKey();
            if (preferenceKey != null && preferenceKey.equals(key)) {
                return n;
            }
        }
        return -1;
    }

    /**
     * Highlight a specific preference by showing a ripple.
     */
    public static class HighlightablePreferenceGroupAdapter extends PreferenceGroupAdapter {
        private int mHighlightPosition = -1;

        public HighlightablePreferenceGroupAdapter(PreferenceGroup preferenceGroup) {
            super(preferenceGroup);
        }

        public void highlight(int position) {
            mHighlightPosition = position;
            notifyDataSetChanged();
        }

        @Override
        public void onBindViewHolder(PreferenceViewHolder holder, int position) {
            super.onBindViewHolder(holder, position);
            if (position == mHighlightPosition) {
                View v = holder.itemView;
                if (v.getBackground() != null) {
                    final int centerX = v.getWidth() / 2;
                    final int centerY = v.getHeight() / 2;
                    v.getBackground().setHotspot(centerX, centerY);
                }
                v.setPressed(true);
                v.setPressed(false);
                mHighlightPosition = -1;
            }
        }
    }

    /**
     * The implementation must not use any variable that only initialzied in life-cycle callback.
     * @return whether the preference fragment is available.
     */
    public abstract boolean isAvailable(Context context);
}
