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

import androidx.room.Dao;
import androidx.room.Delete;
import androidx.room.Insert;
import androidx.room.Query;
import androidx.room.Update;
import java.util.List;

/**
 * Data Access Object for PendingChange entities.
 */
@Dao
public interface PendingChangeDao {

    @Query("SELECT * FROM pending_changes WHERE status = 'pending' ORDER BY appliesAt ASC")
    List<PendingChange> getAllPending();

    @Query("SELECT * FROM pending_changes WHERE appliesAt <= :now AND status = 'pending'")
    List<PendingChange> getReadyToApply(long now);

    @Query("SELECT * FROM pending_changes WHERE id = :id")
    PendingChange getById(long id);

    @Insert
    long insert(PendingChange change);

    @Update
    void update(PendingChange change);

    @Delete
    void delete(PendingChange change);

    @Query("DELETE FROM pending_changes WHERE id = :id")
    void deleteById(long id);

    @Query("SELECT COUNT(*) FROM pending_changes WHERE status = 'pending'")
    int getPendingCount();

    @Query("SELECT * FROM pending_changes WHERE status = 'pending' ORDER BY appliesAt ASC LIMIT 1")
    PendingChange getNextPending();
}
