/*
 * Copyright (C) 2024 The Android Open Source Project
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
package com.afwsamples.testdpc.delay;

import android.os.Bundle;
import android.os.Handler;
import android.os.Looper;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Button;
import android.widget.TextView;
import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.fragment.app.Fragment;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;
import com.afwsamples.testdpc.R;
import java.util.ArrayList;
import java.util.List;

/**
 * Fragment displaying the list of pending policy changes.
 */
public class PendingChangesFragment extends Fragment {

    private RecyclerView recyclerView;
    private TextView emptyText;
    private PendingChangesAdapter adapter;
    private DelayManager delayManager;
    private Handler handler;

    private final Runnable refreshRunnable = new Runnable() {
        @Override
        public void run() {
            refreshList();
            handler.postDelayed(this, 1000); // Update every second
        }
    };

    @Override
    public void onCreate(@Nullable Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        delayManager = DelayManager.getInstance(requireContext());
        handler = new Handler(Looper.getMainLooper());
    }

    @Nullable
    @Override
    public View onCreateView(@NonNull LayoutInflater inflater,
            @Nullable ViewGroup container, @Nullable Bundle savedInstanceState) {
        return inflater.inflate(R.layout.fragment_pending_changes, container, false);
    }

    @Override
    public void onViewCreated(@NonNull View view, @Nullable Bundle savedInstanceState) {
        super.onViewCreated(view, savedInstanceState);

        recyclerView = view.findViewById(R.id.recycler_view);
        emptyText = view.findViewById(R.id.empty_text);

        adapter = new PendingChangesAdapter();
        recyclerView.setLayoutManager(new LinearLayoutManager(requireContext()));
        recyclerView.setAdapter(adapter);
    }

    @Override
    public void onResume() {
        super.onResume();
        handler.post(refreshRunnable);
    }

    @Override
    public void onPause() {
        super.onPause();
        handler.removeCallbacks(refreshRunnable);
    }

    private void refreshList() {
        delayManager.getPendingChanges(changes -> {
            if (getContext() == null) return;

            adapter.setChanges(changes);

            if (changes.isEmpty()) {
                emptyText.setVisibility(View.VISIBLE);
                recyclerView.setVisibility(View.GONE);
            } else {
                emptyText.setVisibility(View.GONE);
                recyclerView.setVisibility(View.VISIBLE);
            }
        });
    }

    private class PendingChangesAdapter extends RecyclerView.Adapter<PendingChangeViewHolder> {
        private List<PendingChange> changes = new ArrayList<>();

        void setChanges(List<PendingChange> changes) {
            this.changes = changes;
            notifyDataSetChanged();
        }

        @NonNull
        @Override
        public PendingChangeViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
            View view = LayoutInflater.from(parent.getContext())
                .inflate(R.layout.item_pending_change, parent, false);
            return new PendingChangeViewHolder(view);
        }

        @Override
        public void onBindViewHolder(@NonNull PendingChangeViewHolder holder, int position) {
            PendingChange change = changes.get(position);
            holder.bind(change);
        }

        @Override
        public int getItemCount() {
            return changes.size();
        }
    }

    private class PendingChangeViewHolder extends RecyclerView.ViewHolder {
        private final TextView description;
        private final TextView timeRemaining;
        private final Button cancelButton;

        PendingChangeViewHolder(@NonNull View itemView) {
            super(itemView);
            description = itemView.findViewById(R.id.description);
            timeRemaining = itemView.findViewById(R.id.time_remaining);
            cancelButton = itemView.findViewById(R.id.cancel_button);
        }

        void bind(PendingChange change) {
            description.setText(change.description);
            timeRemaining.setText(getString(R.string.applies_in, formatTimeRemaining(change.getTimeRemainingMillis())));

            cancelButton.setOnClickListener(v -> {
                delayManager.cancelChange(change.id);
                refreshList();
            });
        }

        private String formatTimeRemaining(long millis) {
            long seconds = millis / 1000;
            if (seconds < 60) {
                return seconds + " seconds";
            }
            long minutes = seconds / 60;
            if (minutes < 60) {
                return minutes + " minutes";
            }
            long hours = minutes / 60;
            long remainingMinutes = minutes % 60;
            return hours + "h " + remainingMinutes + "m";
        }
    }
}
