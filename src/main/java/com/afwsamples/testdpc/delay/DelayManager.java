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

import android.content.Context;
import android.os.Handler;
import android.os.Looper;
import android.util.Log;
import android.widget.Toast;
import java.util.List;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;

/**
 * Central manager for the delay feature.
 * Handles queueing actions, managing the database, and coordinating with the UI.
 */
public class DelayManager {
    private static final String TAG = "DelayManager";
    private static volatile DelayManager instance;

    private final Context context;
    private final DelayConfig config;
    private final DelayDatabase database;
    private final ExecutorService executor;
    private final Handler mainHandler;

    private DelayManager(Context context) {
        this.context = context.getApplicationContext();
        this.config = new DelayConfig(this.context);
        this.database = DelayDatabase.getInstance(this.context);
        this.executor = Executors.newSingleThreadExecutor();
        this.mainHandler = new Handler(Looper.getMainLooper());
    }

    public static DelayManager getInstance(Context context) {
        if (instance == null) {
            synchronized (DelayManager.class) {
                if (instance == null) {
                    instance = new DelayManager(context);
                }
            }
        }
        return instance;
    }

    public DelayConfig getConfig() {
        return config;
    }

    /**
     * Check if delay is currently enabled.
     */
    public boolean isDelayEnabled() {
        return config.isEnabled();
    }

    /**
     * Enable the delay feature (takes effect immediately).
     */
    public void enableDelay() {
        config.setEnabled(true);
        Log.i(TAG, "Delay feature enabled");
    }

    /**
     * Queue a request to disable the delay feature (goes through delay).
     */
    public void queueDisableDelay() {
        queueAction(
            "disableDelay",
            DPMAction.serialize(),
            "Disable delay feature"
        );
    }

    /**
     * Queue an action to be applied after the delay.
     *
     * @param actionType The method name/type of action
     * @param actionData Serialized parameters
     * @param description Human-readable description
     * @return The ID of the queued change
     */
    public long queueAction(String actionType, String actionData, String description) {
        PendingChange change = PendingChange.create(
            actionType,
            actionData,
            description,
            config.getDelayMillis()
        );

        // Insert on background thread, but we need the ID
        final long[] resultId = new long[1];
        try {
            executor.submit(() -> {
                resultId[0] = database.pendingChangeDao().insert(change);
                Log.i(TAG, "Queued action: " + description + " (ID: " + resultId[0] + ")");
            }).get();
        } catch (Exception e) {
            Log.e(TAG, "Failed to queue action", e);
            return -1;
        }

        // Show toast on main thread
        mainHandler.post(() -> {
            String msg = "Queued: " + description + ". Applies in " + config.getDelayDisplayString();
            Toast.makeText(context, msg, Toast.LENGTH_LONG).show();
        });

        return resultId[0];
    }

    /**
     * Cancel a pending change immediately.
     */
    public void cancelChange(long id) {
        executor.execute(() -> {
            database.pendingChangeDao().deleteById(id);
            Log.i(TAG, "Cancelled pending change: " + id);
        });
    }

    /**
     * Get all pending changes.
     */
    public void getPendingChanges(PendingChangesCallback callback) {
        executor.execute(() -> {
            List<PendingChange> changes = database.pendingChangeDao().getAllPending();
            mainHandler.post(() -> callback.onResult(changes));
        });
    }

    /**
     * Get changes that are ready to apply.
     */
    public List<PendingChange> getReadyChanges() {
        return database.pendingChangeDao().getReadyToApply(System.currentTimeMillis());
    }

    /**
     * Mark a change as completed and delete it.
     */
    public void markCompleted(PendingChange change) {
        change.status = "completed";
        database.pendingChangeDao().delete(change);
    }

    /**
     * Mark a change as failed.
     */
    public void markFailed(PendingChange change) {
        change.status = "failed";
        database.pendingChangeDao().update(change);
    }

    /**
     * Directly disable delay (called by executor when delay-disable action applies).
     */
    public void disableDelayDirect() {
        config.setEnabled(false);
        Log.i(TAG, "Delay feature disabled");
    }

    public interface PendingChangesCallback {
        void onResult(List<PendingChange> changes);
    }
}
