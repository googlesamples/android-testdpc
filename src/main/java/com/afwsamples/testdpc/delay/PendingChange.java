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

import androidx.room.Entity;
import androidx.room.PrimaryKey;

/**
 * Represents a queued DPM action waiting to be applied.
 */
@Entity(tableName = "pending_changes")
public class PendingChange {

    @PrimaryKey(autoGenerate = true)
    public long id;

    /** The type of DPM action (e.g., "setCameraDisabled"). */
    public String actionType;

    /** JSON-serialized parameters for the action. */
    public String actionData;

    /** Human-readable description for UI display. */
    public String description;

    /** Epoch millis when this change was queued. */
    public long queuedAt;

    /** Epoch millis when this change should be applied. */
    public long appliesAt;

    /** Status: "pending", "applying", "completed", "failed". */
    public String status;

    public PendingChange() {
        this.status = "pending";
    }

    public static PendingChange create(
            String actionType,
            String actionData,
            String description,
            long delayMillis) {
        PendingChange change = new PendingChange();
        change.actionType = actionType;
        change.actionData = actionData;
        change.description = description;
        change.queuedAt = System.currentTimeMillis();
        change.appliesAt = change.queuedAt + delayMillis;
        return change;
    }

    /**
     * @return Time remaining in millis until this change applies.
     */
    public long getTimeRemainingMillis() {
        return Math.max(0, appliesAt - System.currentTimeMillis());
    }

    /**
     * @return Whether this change is ready to be applied.
     */
    public boolean isReady() {
        return System.currentTimeMillis() >= appliesAt && "pending".equals(status);
    }
}
