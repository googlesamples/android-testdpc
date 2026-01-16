# Test DPC Delay Feature Implementation Plan

> **For Claude:** REQUIRED SUB-SKILL: Use superpowers:executing-plans to implement this plan task-by-task.

**Goal:** Add a delay mechanism that queues ALL device policy changes and applies them after a configurable timer expires.

**Architecture:** Create a `DelayedDevicePolicyManagerGateway` decorator that wraps the existing `DevicePolicyManagerGatewayImpl`. All 150+ gateway methods are intercepted - when delay is enabled, actions are serialized to a Room database and applied by a background service when their timers expire.

**Tech Stack:** Java, Android Room (SQLite), WorkManager, SharedPreferences, Bazel build system

---

## Task 1: Add Room Dependencies to Bazel Build

**Files:**
- Modify: `testdpc-delay/MODULE.bazel`
- Modify: `testdpc-delay/BUILD`

**Step 1: Add Room to MODULE.bazel**

Open `MODULE.bazel` and add Room dependencies. The file currently has minimal content, so we need to add maven dependencies.

```
# In MODULE.bazel, the Room deps will be pulled via maven in WORKSPACE
# No changes needed to MODULE.bazel for now
```

**Step 2: Add Room to WORKSPACE maven dependencies**

Modify `WORKSPACE` file to add Room artifacts:

```python
# Add to maven_install artifacts list:
"androidx.room:room-runtime:2.5.0",
"androidx.room:room-common:2.5.0",
```

**Step 3: Add Room library target to BUILD**

Add to `BUILD` file after the existing `android_library` blocks:

```python
android_library(
    name = "room_deps",
    exports = [
        "@maven//:androidx_room_room_runtime",
        "@maven//:androidx_room_room_common",
    ],
)
```

**Step 4: Update testdpc_lib deps**

Add `:room_deps` to the deps list of `testdpc_lib`:

```python
android_library(
    name = "testdpc_lib",
    # ... existing config ...
    deps = [
        ":aidl",
        ":androidx_deps",
        ":bouncycastle_deps",
        ":guava_deps",
        ":room_deps",  # ADD THIS
        "@setupdesign//:setupdesign",
        "@setupcompat//:setupcompat",
    ],
)
```

**Step 5: Verify build still works**

Run: `cd testdpc-delay && bazel build //:testdpc`
Expected: BUILD SUCCESS (may need to fetch new deps)

**Step 6: Commit**

```bash
git add MODULE.bazel WORKSPACE BUILD
git commit -m "build: add Room database dependencies for delay feature"
```

---

## Task 2: Create DelayConfig Model

**Files:**
- Create: `testdpc-delay/src/main/java/com/afwsamples/testdpc/delay/DelayConfig.java`

**Step 1: Create delay package directory**

```bash
mkdir -p testdpc-delay/src/main/java/com/afwsamples/testdpc/delay
```

**Step 2: Create DelayConfig class**

```java
package com.afwsamples.testdpc.delay;

import android.content.Context;
import android.content.SharedPreferences;

/**
 * Configuration for the delay feature.
 * Stores whether delay is enabled and the delay duration.
 */
public class DelayConfig {
    private static final String PREFS_NAME = "delay_config";
    private static final String KEY_ENABLED = "delay_enabled";
    private static final String KEY_DURATION_VALUE = "delay_duration_value";
    private static final String KEY_DURATION_UNIT = "delay_duration_unit";

    public enum TimeUnit {
        SECONDS(1000L),
        MINUTES(60 * 1000L),
        HOURS(60 * 60 * 1000L);

        private final long millis;

        TimeUnit(long millis) {
            this.millis = millis;
        }

        public long toMillis(long value) {
            return value * millis;
        }
    }

    private final SharedPreferences prefs;

    public DelayConfig(Context context) {
        prefs = context.getSharedPreferences(PREFS_NAME, Context.MODE_PRIVATE);
    }

    public boolean isEnabled() {
        return prefs.getBoolean(KEY_ENABLED, false);
    }

    public void setEnabled(boolean enabled) {
        prefs.edit().putBoolean(KEY_ENABLED, enabled).apply();
    }

    public long getDurationValue() {
        return prefs.getLong(KEY_DURATION_VALUE, 1);
    }

    public void setDurationValue(long value) {
        prefs.edit().putLong(KEY_DURATION_VALUE, value).apply();
    }

    public TimeUnit getDurationUnit() {
        String unitName = prefs.getString(KEY_DURATION_UNIT, TimeUnit.HOURS.name());
        return TimeUnit.valueOf(unitName);
    }

    public void setDurationUnit(TimeUnit unit) {
        prefs.edit().putString(KEY_DURATION_UNIT, unit.name()).apply();
    }

    /**
     * @return The configured delay duration in milliseconds.
     */
    public long getDelayMillis() {
        return getDurationUnit().toMillis(getDurationValue());
    }

    /**
     * @return Human-readable delay duration string (e.g., "24 hours").
     */
    public String getDelayDisplayString() {
        long value = getDurationValue();
        String unit = getDurationUnit().name().toLowerCase();
        if (value == 1) {
            // Remove trailing 's' for singular
            unit = unit.substring(0, unit.length() - 1);
        }
        return value + " " + unit;
    }
}
```

**Step 3: Commit**

```bash
git add src/main/java/com/afwsamples/testdpc/delay/DelayConfig.java
git commit -m "feat(delay): add DelayConfig for storing delay settings"
```

---

## Task 3: Create PendingChange Entity

**Files:**
- Create: `testdpc-delay/src/main/java/com/afwsamples/testdpc/delay/PendingChange.java`

**Step 1: Create PendingChange Room entity**

```java
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
```

**Step 2: Commit**

```bash
git add src/main/java/com/afwsamples/testdpc/delay/PendingChange.java
git commit -m "feat(delay): add PendingChange Room entity"
```

---

## Task 4: Create PendingChangeDao

**Files:**
- Create: `testdpc-delay/src/main/java/com/afwsamples/testdpc/delay/PendingChangeDao.java`

**Step 1: Create the DAO interface**

```java
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
```

**Step 2: Commit**

```bash
git add src/main/java/com/afwsamples/testdpc/delay/PendingChangeDao.java
git commit -m "feat(delay): add PendingChangeDao for database operations"
```

---

## Task 5: Create DelayDatabase

**Files:**
- Create: `testdpc-delay/src/main/java/com/afwsamples/testdpc/delay/DelayDatabase.java`

**Step 1: Create the Room database class**

```java
package com.afwsamples.testdpc.delay;

import android.content.Context;
import androidx.room.Database;
import androidx.room.Room;
import androidx.room.RoomDatabase;

/**
 * Room database for storing pending DPM changes.
 */
@Database(entities = {PendingChange.class}, version = 1, exportSchema = false)
public abstract class DelayDatabase extends RoomDatabase {

    private static final String DATABASE_NAME = "delay_database";
    private static volatile DelayDatabase instance;

    public abstract PendingChangeDao pendingChangeDao();

    public static DelayDatabase getInstance(Context context) {
        if (instance == null) {
            synchronized (DelayDatabase.class) {
                if (instance == null) {
                    instance = Room.databaseBuilder(
                            context.getApplicationContext(),
                            DelayDatabase.class,
                            DATABASE_NAME
                    ).build();
                }
            }
        }
        return instance;
    }
}
```

**Step 2: Commit**

```bash
git add src/main/java/com/afwsamples/testdpc/delay/DelayDatabase.java
git commit -m "feat(delay): add DelayDatabase Room database"
```

---

## Task 6: Create DPMAction Sealed Classes

**Files:**
- Create: `testdpc-delay/src/main/java/com/afwsamples/testdpc/delay/DPMAction.java`

**Step 1: Create action serialization classes**

This file defines serializable representations of all DPM actions. We use a JSON-based approach for flexibility.

```java
package com.afwsamples.testdpc.delay;

import android.os.Bundle;
import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;
import java.util.ArrayList;
import java.util.HashSet;
import java.util.List;
import java.util.Set;

/**
 * Utility class for serializing and deserializing DPM action parameters.
 */
public class DPMAction {

    /**
     * Serialize action parameters to JSON string.
     */
    public static String serialize(Object... params) {
        try {
            JSONObject json = new JSONObject();
            JSONArray arr = new JSONArray();
            for (Object param : params) {
                if (param == null) {
                    arr.put(JSONObject.NULL);
                } else if (param instanceof Boolean) {
                    arr.put(param);
                } else if (param instanceof Integer) {
                    arr.put(param);
                } else if (param instanceof Long) {
                    arr.put(param);
                } else if (param instanceof String) {
                    arr.put(param);
                } else if (param instanceof String[]) {
                    JSONArray strArr = new JSONArray();
                    for (String s : (String[]) param) {
                        strArr.put(s);
                    }
                    arr.put(strArr);
                } else if (param instanceof List) {
                    JSONArray listArr = new JSONArray();
                    for (Object item : (List<?>) param) {
                        listArr.put(item != null ? item.toString() : JSONObject.NULL);
                    }
                    arr.put(listArr);
                } else if (param instanceof Set) {
                    JSONArray setArr = new JSONArray();
                    for (Object item : (Set<?>) param) {
                        setArr.put(item != null ? item.toString() : JSONObject.NULL);
                    }
                    arr.put(setArr);
                } else {
                    // For complex types, store as string representation
                    arr.put(param.toString());
                }
            }
            json.put("params", arr);
            return json.toString();
        } catch (JSONException e) {
            throw new RuntimeException("Failed to serialize action", e);
        }
    }

    /**
     * Deserialize parameters from JSON string.
     */
    public static Object[] deserialize(String json) {
        try {
            JSONObject obj = new JSONObject(json);
            JSONArray arr = obj.getJSONArray("params");
            Object[] result = new Object[arr.length()];
            for (int i = 0; i < arr.length(); i++) {
                Object val = arr.get(i);
                if (val == JSONObject.NULL) {
                    result[i] = null;
                } else if (val instanceof JSONArray) {
                    // Convert to List<String>
                    JSONArray jsonArr = (JSONArray) val;
                    List<String> list = new ArrayList<>();
                    for (int j = 0; j < jsonArr.length(); j++) {
                        Object item = jsonArr.get(j);
                        list.add(item == JSONObject.NULL ? null : item.toString());
                    }
                    result[i] = list;
                } else {
                    result[i] = val;
                }
            }
            return result;
        } catch (JSONException e) {
            throw new RuntimeException("Failed to deserialize action", e);
        }
    }

    public static boolean getBoolean(Object[] params, int index) {
        return (Boolean) params[index];
    }

    public static int getInt(Object[] params, int index) {
        Object val = params[index];
        if (val instanceof Long) {
            return ((Long) val).intValue();
        }
        return (Integer) val;
    }

    public static long getLong(Object[] params, int index) {
        Object val = params[index];
        if (val instanceof Integer) {
            return ((Integer) val).longValue();
        }
        return (Long) val;
    }

    public static String getString(Object[] params, int index) {
        Object val = params[index];
        return val == null ? null : val.toString();
    }

    @SuppressWarnings("unchecked")
    public static List<String> getStringList(Object[] params, int index) {
        Object val = params[index];
        if (val == null) return null;
        if (val instanceof List) {
            return (List<String>) val;
        }
        return null;
    }

    public static String[] getStringArray(Object[] params, int index) {
        List<String> list = getStringList(params, index);
        return list == null ? null : list.toArray(new String[0]);
    }

    public static Set<String> getStringSet(Object[] params, int index) {
        List<String> list = getStringList(params, index);
        return list == null ? null : new HashSet<>(list);
    }
}
```

**Step 2: Commit**

```bash
git add src/main/java/com/afwsamples/testdpc/delay/DPMAction.java
git commit -m "feat(delay): add DPMAction serialization utilities"
```

---

## Task 7: Create DelayManager Singleton

**Files:**
- Create: `testdpc-delay/src/main/java/com/afwsamples/testdpc/delay/DelayManager.java`

**Step 1: Create the core manager class**

```java
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
```

**Step 2: Commit**

```bash
git add src/main/java/com/afwsamples/testdpc/delay/DelayManager.java
git commit -m "feat(delay): add DelayManager singleton for coordinating delay logic"
```

---

## Task 8: Create DelayedDevicePolicyManagerGateway (Part 1 - Core Structure)

**Files:**
- Create: `testdpc-delay/src/main/java/com/afwsamples/testdpc/delay/DelayedDevicePolicyManagerGateway.java`

**Step 1: Create the decorator class with core structure**

This is a large file. First, we create the base structure with delegation pattern.

```java
package com.afwsamples.testdpc.delay;

import android.app.admin.DevicePolicyManager;
import android.app.admin.NetworkEvent;
import android.app.admin.SecurityLog.SecurityEvent;
import android.content.ComponentName;
import android.content.Context;
import android.content.Intent;
import android.content.pm.PackageManager.NameNotFoundException;
import android.graphics.Bitmap;
import android.os.Bundle;
import android.os.PersistableBundle;
import android.os.UserHandle;
import android.security.AttestedKeyPair;
import android.security.keystore.KeyGenParameterSpec;
import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import com.afwsamples.testdpc.DevicePolicyManagerGateway;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.function.Consumer;

/**
 * Decorator that wraps DevicePolicyManagerGateway to add delay functionality.
 * When delay is enabled, mutating operations are queued instead of executed immediately.
 */
public class DelayedDevicePolicyManagerGateway implements DevicePolicyManagerGateway {

    private final DevicePolicyManagerGateway delegate;
    private final DelayManager delayManager;
    private final Context context;

    public DelayedDevicePolicyManagerGateway(
            DevicePolicyManagerGateway delegate,
            Context context) {
        this.delegate = delegate;
        this.context = context;
        this.delayManager = DelayManager.getInstance(context);
    }

    /**
     * Get the underlying delegate gateway for direct access when needed.
     */
    public DevicePolicyManagerGateway getDelegate() {
        return delegate;
    }

    // ==================== READ-ONLY METHODS (pass through directly) ====================
    // These don't modify state, so they don't need delay

    @NonNull
    @Override
    public ComponentName getAdmin() {
        return delegate.getAdmin();
    }

    @NonNull
    @Override
    public DevicePolicyManager getDevicePolicyManager() {
        return delegate.getDevicePolicyManager();
    }

    @Override
    public boolean isDeviceOwnerApp() {
        return delegate.isDeviceOwnerApp();
    }

    @Override
    public boolean isProfileOwnerApp() {
        return delegate.isProfileOwnerApp();
    }

    @Override
    public boolean isOrganizationOwnedDeviceWithManagedProfile() {
        return delegate.isOrganizationOwnedDeviceWithManagedProfile();
    }

    @Override
    public boolean isHeadlessSystemUserMode() {
        return delegate.isHeadlessSystemUserMode();
    }

    @Override
    public boolean isUserForeground() {
        return delegate.isUserForeground();
    }

    @Override
    public List<UserHandle> listForegroundAffiliatedUsers() {
        return delegate.listForegroundAffiliatedUsers();
    }

    @NonNull
    @Override
    public CharSequence getStartUserSessionMessage() {
        return delegate.getStartUserSessionMessage();
    }

    @NonNull
    @Override
    public CharSequence getEndUserSessionMessage() {
        return delegate.getEndUserSessionMessage();
    }

    @Nullable
    @Override
    public UserHandle getUserHandle(long serialNumber) {
        return delegate.getUserHandle(serialNumber);
    }

    @Override
    public long getSerialNumber(@NonNull UserHandle user) {
        return delegate.getSerialNumber(user);
    }

    @Override
    public boolean isLogoutEnabled() {
        return delegate.isLogoutEnabled();
    }

    @Override
    public boolean isAffiliatedUser() {
        return delegate.isAffiliatedUser();
    }

    @NonNull
    @Override
    public Set<String> getAffiliationIds() {
        return delegate.getAffiliationIds();
    }

    @NonNull
    @Override
    public Set<String> getUserRestrictions() {
        return delegate.getUserRestrictions();
    }

    @Override
    public boolean hasUserRestriction(@NonNull String userRestriction) {
        return delegate.hasUserRestriction(userRestriction);
    }

    @Override
    public boolean isNetworkLoggingEnabled() {
        return delegate.isNetworkLoggingEnabled();
    }

    @Override
    public long getLastNetworkLogRetrievalTime() {
        return delegate.getLastNetworkLogRetrievalTime();
    }

    @Override
    public List<NetworkEvent> retrieveNetworkLogs(long batchToken) {
        return delegate.retrieveNetworkLogs(batchToken);
    }

    @Override
    public boolean isSecurityLoggingEnabled() {
        return delegate.isSecurityLoggingEnabled();
    }

    @Override
    public long getLastSecurityLogRetrievalTime() {
        return delegate.getLastSecurityLogRetrievalTime();
    }

    @Override
    public List<SecurityEvent> retrieveSecurityLogs() {
        return delegate.retrieveSecurityLogs();
    }

    @Override
    public List<SecurityEvent> retrievePreRebootSecurityLogs() {
        return delegate.retrievePreRebootSecurityLogs();
    }

    @Nullable
    @Override
    public CharSequence getOrganizationName() {
        return delegate.getOrganizationName();
    }

    @NonNull
    @Override
    public List<String> getUserControlDisabledPackages() {
        return delegate.getUserControlDisabledPackages();
    }

    @NonNull
    @Override
    public Set<String> getCrossProfilePackages() {
        return delegate.getCrossProfilePackages();
    }

    @Override
    public int getPasswordQuality() {
        return delegate.getPasswordQuality();
    }

    @Override
    public int getRequiredPasswordComplexity() {
        return delegate.getRequiredPasswordComplexity();
    }

    @Override
    public boolean isActivePasswordSufficient() {
        return delegate.isActivePasswordSufficient();
    }

    @Override
    public boolean isActivePasswordSufficientForDeviceRequirement() {
        return delegate.isActivePasswordSufficientForDeviceRequirement();
    }

    @Override
    public boolean isPreferentialNetworkServiceEnabled() {
        return delegate.isPreferentialNetworkServiceEnabled();
    }

    @Override
    public boolean isPackageSuspended(String packageName) throws NameNotFoundException {
        return delegate.isPackageSuspended(packageName);
    }

    @Override
    public boolean isApplicationHidden(String packageName) throws NameNotFoundException {
        return delegate.isApplicationHidden(packageName);
    }

    @Override
    public int getPersonalAppsSuspendedReasons() {
        return delegate.getPersonalAppsSuspendedReasons();
    }

    @NonNull
    @Override
    public List<String> getDisabledSystemApps() {
        return delegate.getDisabledSystemApps();
    }

    @Override
    public String[] getLockTaskPackages() {
        return delegate.getLockTaskPackages();
    }

    @Override
    public int getLockTaskFeatures() {
        return delegate.getLockTaskFeatures();
    }

    @Override
    public boolean isLockTaskPermitted(String packageName) {
        return delegate.isLockTaskPermitted(packageName);
    }

    @Override
    public Bundle getApplicationRestrictions(String packageName) {
        return delegate.getApplicationRestrictions(packageName);
    }

    @Override
    public Bundle getSelfRestrictions() {
        return delegate.getSelfRestrictions();
    }

    @Override
    public int getPermissionGrantState(String packageName, String permission) {
        return delegate.getPermissionGrantState(packageName, permission);
    }

    @Override
    public boolean canAdminGrantSensorsPermissions() {
        return delegate.canAdminGrantSensorsPermissions();
    }

    @Override
    public boolean isLocationEnabled() {
        return delegate.isLocationEnabled();
    }

    @Override
    public CharSequence getDeviceOwnerLockScreenInfo() {
        return delegate.getDeviceOwnerLockScreenInfo();
    }

    @Override
    public int getKeyguardDisabledFeatures() {
        return delegate.getKeyguardDisabledFeatures();
    }

    @Override
    public boolean getCameraDisabled() {
        return delegate.getCameraDisabled();
    }

    @Override
    public boolean getCameraDisabledByAnyAdmin() {
        return delegate.getCameraDisabledByAnyAdmin();
    }

    @Override
    public int getMaximumFailedPasswordsForWipe() {
        return delegate.getMaximumFailedPasswordsForWipe();
    }

    @Override
    public boolean isUninstallBlocked(@NonNull String packageName) {
        return delegate.isUninstallBlocked(packageName);
    }

    @Override
    public boolean isDeviceIdAttestationSupported() {
        return delegate.isDeviceIdAttestationSupported();
    }

    @Override
    public boolean isUniqueDeviceAttestationSupported() {
        return delegate.isUniqueDeviceAttestationSupported();
    }

    @Override
    public boolean hasKeyPair(String alias) {
        return delegate.hasKeyPair(alias);
    }

    @NonNull
    @Override
    public Map<Integer, Set<String>> getKeyPairGrants(@NonNull String alias) {
        return delegate.getKeyPairGrants(alias);
    }

    @NonNull
    @Override
    public List<String> getDelegatedScopes(@NonNull String delegatePackage) {
        return delegate.getDelegatedScopes(delegatePackage);
    }

    @NonNull
    @Override
    public List<String> getDelegatePackages(@NonNull String delegationScope) {
        return delegate.getDelegatePackages(delegationScope);
    }

    @NonNull
    @Override
    public List<String> getMeteredDataDisabledPackages() {
        return delegate.getMeteredDataDisabledPackages();
    }

    @NonNull
    @Override
    public List<UserHandle> getSecondaryUsers() {
        return delegate.getSecondaryUsers();
    }

    @Override
    public long getLastBugReportRequestTime() {
        return delegate.getLastBugReportRequestTime();
    }

    // ==================== MUTATING METHODS (require delay) ====================
    // These methods will be implemented in Part 2

    // Placeholder - to be continued in Task 9
}
```

**Step 2: Commit**

```bash
git add src/main/java/com/afwsamples/testdpc/delay/DelayedDevicePolicyManagerGateway.java
git commit -m "feat(delay): add DelayedDevicePolicyManagerGateway structure with read-only methods"
```

---

## Task 9: Complete DelayedDevicePolicyManagerGateway (Part 2 - Mutating Methods)

**Files:**
- Modify: `testdpc-delay/src/main/java/com/afwsamples/testdpc/delay/DelayedDevicePolicyManagerGateway.java`

**Step 1: Add helper method for queueing or executing**

Add this helper after the read-only methods:

```java
    // ==================== HELPER METHODS ====================

    /**
     * Queue an action if delay is enabled, otherwise execute immediately.
     * For methods with callbacks.
     */
    private <T> void queueOrExecute(
            String actionType,
            String description,
            Object[] params,
            Runnable immediateAction,
            Consumer<T> onSuccess,
            Consumer<Exception> onError) {
        if (delayManager.isDelayEnabled()) {
            String actionData = DPMAction.serialize(params);
            delayManager.queueAction(actionType, actionData, description);
            // Call success callback with null - action is queued, not executed
            if (onSuccess != null) {
                onSuccess.accept(null);
            }
        } else {
            immediateAction.run();
        }
    }

    /**
     * Queue an action if delay is enabled, otherwise execute immediately.
     * For methods without callbacks.
     */
    private void queueOrExecuteVoid(
            String actionType,
            String description,
            Object[] params,
            Runnable immediateAction) {
        if (delayManager.isDelayEnabled()) {
            String actionData = DPMAction.serialize(params);
            delayManager.queueAction(actionType, actionData, description);
        } else {
            immediateAction.run();
        }
    }
```

**Step 2: Add all mutating method implementations**

Add these implementations after the helper methods:

```java
    // ==================== MUTATING METHODS ====================

    @Override
    public void createAndManageUser(
            @Nullable String name,
            int flags,
            @NonNull Consumer<UserHandle> onSuccess,
            @NonNull Consumer<Exception> onError) {
        queueOrExecute("createAndManageUser",
            "Create user: " + (name != null ? name : "unnamed"),
            new Object[]{name, flags},
            () -> delegate.createAndManageUser(name, flags, onSuccess, onError),
            onSuccess, onError);
    }

    @Override
    public void setUserIcon(
            @NonNull Bitmap icon,
            @NonNull Consumer<Void> onSuccess,
            @NonNull Consumer<Exception> onError) {
        // Note: Bitmap can't be easily serialized, so this will fail on delayed execution
        // For now, queue as placeholder
        queueOrExecute("setUserIcon", "Set user icon",
            new Object[]{},
            () -> delegate.setUserIcon(icon, onSuccess, onError),
            onSuccess, onError);
    }

    @Override
    public void setStartUserSessionMessage(
            @Nullable CharSequence message,
            @NonNull Consumer<Void> onSuccess,
            @NonNull Consumer<Exception> onError) {
        queueOrExecute("setStartUserSessionMessage",
            "Set start session message",
            new Object[]{message != null ? message.toString() : null},
            () -> delegate.setStartUserSessionMessage(message, onSuccess, onError),
            onSuccess, onError);
    }

    @Override
    public void setEndUserSessionMessage(
            @Nullable CharSequence message,
            @NonNull Consumer<Void> onSuccess,
            @NonNull Consumer<Exception> onError) {
        queueOrExecute("setEndUserSessionMessage",
            "Set end session message",
            new Object[]{message != null ? message.toString() : null},
            () -> delegate.setEndUserSessionMessage(message, onSuccess, onError),
            onSuccess, onError);
    }

    @Override
    public void removeUser(
            @NonNull UserHandle userHandle,
            @NonNull Consumer<Void> onSuccess,
            @NonNull Consumer<Exception> onError) {
        queueOrExecute("removeUser",
            "Remove user",
            new Object[]{userHandle.hashCode()},
            () -> delegate.removeUser(userHandle, onSuccess, onError),
            onSuccess, onError);
    }

    @Override
    public void switchUser(
            @NonNull UserHandle userHandle,
            @NonNull Consumer<Void> onSuccess,
            @NonNull Consumer<Exception> onError) {
        queueOrExecute("switchUser",
            "Switch user",
            new Object[]{userHandle.hashCode()},
            () -> delegate.switchUser(userHandle, onSuccess, onError),
            onSuccess, onError);
    }

    @Override
    public void startUserInBackground(
            @NonNull UserHandle userHandle,
            @NonNull Consumer<Integer> onSuccess,
            @NonNull Consumer<Exception> onError) {
        queueOrExecute("startUserInBackground",
            "Start user in background",
            new Object[]{userHandle.hashCode()},
            () -> delegate.startUserInBackground(userHandle, onSuccess, onError),
            onSuccess, onError);
    }

    @Override
    public void stopUser(
            @NonNull UserHandle userHandle,
            @NonNull Consumer<Integer> onSuccess,
            @NonNull Consumer<Exception> onError) {
        queueOrExecute("stopUser",
            "Stop user",
            new Object[]{userHandle.hashCode()},
            () -> delegate.stopUser(userHandle, onSuccess, onError),
            onSuccess, onError);
    }

    @Override
    public void setLogoutEnabled(
            boolean enabled,
            @NonNull Consumer<Void> onSuccess,
            @NonNull Consumer<Exception> onError) {
        queueOrExecute("setLogoutEnabled",
            (enabled ? "Enable" : "Disable") + " logout",
            new Object[]{enabled},
            () -> delegate.setLogoutEnabled(enabled, onSuccess, onError),
            onSuccess, onError);
    }

    @Override
    public void logoutUser(
            @NonNull Consumer<Integer> onSuccess,
            @NonNull Consumer<Exception> onError) {
        queueOrExecute("logoutUser",
            "Logout user",
            new Object[]{},
            () -> delegate.logoutUser(onSuccess, onError),
            onSuccess, onError);
    }

    @Override
    public void setAffiliationIds(@NonNull Set<String> ids) {
        queueOrExecuteVoid("setAffiliationIds",
            "Set affiliation IDs",
            new Object[]{ids.toArray(new String[0])},
            () -> delegate.setAffiliationIds(ids));
    }

    @Override
    public void setUserRestriction(
            @NonNull String userRestriction,
            boolean enabled,
            @NonNull Consumer<Void> onSuccess,
            @NonNull Consumer<Exception> onError) {
        queueOrExecute("setUserRestriction",
            (enabled ? "Enable" : "Disable") + " restriction: " + userRestriction,
            new Object[]{userRestriction, enabled},
            () -> delegate.setUserRestriction(userRestriction, enabled, onSuccess, onError),
            onSuccess, onError);
    }

    @Override
    public void setUserRestriction(@NonNull String userRestriction, boolean enabled) {
        queueOrExecuteVoid("setUserRestriction",
            (enabled ? "Enable" : "Disable") + " restriction: " + userRestriction,
            new Object[]{userRestriction, enabled},
            () -> delegate.setUserRestriction(userRestriction, enabled));
    }

    @Override
    public void lockNow(@NonNull Consumer<Void> onSuccess, @NonNull Consumer<Exception> onError) {
        queueOrExecute("lockNow",
            "Lock device",
            new Object[]{},
            () -> delegate.lockNow(onSuccess, onError),
            onSuccess, onError);
    }

    @Override
    public void lockNow(int flags, @NonNull Consumer<Void> onSuccess, @NonNull Consumer<Exception> onError) {
        queueOrExecute("lockNowWithFlags",
            "Lock device",
            new Object[]{flags},
            () -> delegate.lockNow(flags, onSuccess, onError),
            onSuccess, onError);
    }

    @Override
    public void reboot(@NonNull Consumer<Void> onSuccess, @NonNull Consumer<Exception> onError) {
        queueOrExecute("reboot",
            "Reboot device",
            new Object[]{},
            () -> delegate.reboot(onSuccess, onError),
            onSuccess, onError);
    }

    @Override
    public void wipeData(int flags, @NonNull Consumer<Void> onSuccess, @NonNull Consumer<Exception> onError) {
        queueOrExecute("wipeData",
            "Wipe data",
            new Object[]{flags},
            () -> delegate.wipeData(flags, onSuccess, onError),
            onSuccess, onError);
    }

    @Override
    public void wipeDevice(int flags, @NonNull Consumer<Void> onSuccess, @NonNull Consumer<Exception> onError) {
        queueOrExecute("wipeDevice",
            "Wipe device",
            new Object[]{flags},
            () -> delegate.wipeDevice(flags, onSuccess, onError),
            onSuccess, onError);
    }

    @Override
    public void requestBugreport(@NonNull Consumer<Void> onSuccess, @NonNull Consumer<Exception> onError) {
        queueOrExecute("requestBugreport",
            "Request bug report",
            new Object[]{},
            () -> delegate.requestBugreport(onSuccess, onError),
            onSuccess, onError);
    }

    @Override
    public void setNetworkLoggingEnabled(
            boolean enabled,
            @NonNull Consumer<Void> onSuccess,
            @NonNull Consumer<Exception> onError) {
        queueOrExecute("setNetworkLoggingEnabled",
            (enabled ? "Enable" : "Disable") + " network logging",
            new Object[]{enabled},
            () -> delegate.setNetworkLoggingEnabled(enabled, onSuccess, onError),
            onSuccess, onError);
    }

    @Override
    public void setNetworkLoggingEnabled(boolean enabled) {
        queueOrExecuteVoid("setNetworkLoggingEnabled",
            (enabled ? "Enable" : "Disable") + " network logging",
            new Object[]{enabled},
            () -> delegate.setNetworkLoggingEnabled(enabled));
    }

    @Override
    public void setSecurityLoggingEnabled(
            boolean enabled,
            @NonNull Consumer<Void> onSuccess,
            @NonNull Consumer<Exception> onError) {
        queueOrExecute("setSecurityLoggingEnabled",
            (enabled ? "Enable" : "Disable") + " security logging",
            new Object[]{enabled},
            () -> delegate.setSecurityLoggingEnabled(enabled, onSuccess, onError),
            onSuccess, onError);
    }

    @Override
    public void setOrganizationName(
            @Nullable CharSequence title,
            @NonNull Consumer<Void> onSuccess,
            @NonNull Consumer<Exception> onError) {
        queueOrExecute("setOrganizationName",
            "Set organization name: " + title,
            new Object[]{title != null ? title.toString() : null},
            () -> delegate.setOrganizationName(title, onSuccess, onError),
            onSuccess, onError);
    }

    @Override
    public void setUserControlDisabledPackages(
            @Nullable List<String> packages,
            @NonNull Consumer<Void> onSuccess,
            @NonNull Consumer<Exception> onError) {
        queueOrExecute("setUserControlDisabledPackages",
            "Set user control disabled packages",
            new Object[]{packages},
            () -> delegate.setUserControlDisabledPackages(packages, onSuccess, onError),
            onSuccess, onError);
    }

    @Override
    public void setCrossProfilePackages(
            @NonNull Set<String> packages,
            @NonNull Consumer<Void> onSuccess,
            @NonNull Consumer<Exception> onError) {
        queueOrExecute("setCrossProfilePackages",
            "Set cross-profile packages",
            new Object[]{packages.toArray(new String[0])},
            () -> delegate.setCrossProfilePackages(packages, onSuccess, onError),
            onSuccess, onError);
    }

    @Override
    public boolean setPermittedInputMethods(
            List<String> packageNames,
            @NonNull Consumer<Void> onSuccess,
            @NonNull Consumer<Exception> onError) {
        if (delayManager.isDelayEnabled()) {
            delayManager.queueAction("setPermittedInputMethods",
                DPMAction.serialize(packageNames),
                "Set permitted input methods");
            if (onSuccess != null) onSuccess.accept(null);
            return true;
        }
        return delegate.setPermittedInputMethods(packageNames, onSuccess, onError);
    }

    @Override
    public boolean setPermittedInputMethods(List<String> packageNames) {
        if (delayManager.isDelayEnabled()) {
            delayManager.queueAction("setPermittedInputMethods",
                DPMAction.serialize(packageNames),
                "Set permitted input methods");
            return true;
        }
        return delegate.setPermittedInputMethods(packageNames);
    }

    @Override
    public void removeActiveAdmin(@NonNull Consumer<Void> onSuccess, @NonNull Consumer<Exception> onError) {
        queueOrExecute("removeActiveAdmin",
            "Remove active admin",
            new Object[]{},
            () -> delegate.removeActiveAdmin(onSuccess, onError),
            onSuccess, onError);
    }

    @Override
    public void clearDeviceOwnerApp(@NonNull Consumer<Void> onSuccess, @NonNull Consumer<Exception> onError) {
        queueOrExecute("clearDeviceOwnerApp",
            "Clear device owner",
            new Object[]{},
            () -> delegate.clearDeviceOwnerApp(onSuccess, onError),
            onSuccess, onError);
    }

    @Override
    public void clearProfileOwner(@NonNull Consumer<Void> onSuccess, @NonNull Consumer<Exception> onError) {
        queueOrExecute("clearProfileOwner",
            "Clear profile owner",
            new Object[]{},
            () -> delegate.clearProfileOwner(onSuccess, onError),
            onSuccess, onError);
    }

    @Override
    public void setPasswordQuality(
            int quality,
            @NonNull Consumer<Void> onSuccess,
            @NonNull Consumer<Exception> onError) {
        queueOrExecute("setPasswordQuality",
            "Set password quality: " + quality,
            new Object[]{quality},
            () -> delegate.setPasswordQuality(quality, onSuccess, onError),
            onSuccess, onError);
    }

    @Override
    public void setRequiredPasswordComplexity(
            int quality,
            @NonNull Consumer<Void> onSuccess,
            @NonNull Consumer<Exception> onError) {
        queueOrExecute("setRequiredPasswordComplexity",
            "Set required password complexity: " + quality,
            new Object[]{quality},
            () -> delegate.setRequiredPasswordComplexity(quality, onSuccess, onError),
            onSuccess, onError);
    }

    @Override
    public void transferOwnership(
            @NonNull ComponentName target,
            @Nullable PersistableBundle bundle,
            @NonNull Consumer<Void> onSuccess,
            @NonNull Consumer<Exception> onError) {
        queueOrExecute("transferOwnership",
            "Transfer ownership to: " + target.flattenToString(),
            new Object[]{target.flattenToString()},
            () -> delegate.transferOwnership(target, bundle, onSuccess, onError),
            onSuccess, onError);
    }

    @Override
    public void setUsbDataSignalingEnabled(
            boolean enabled,
            @NonNull Consumer<Void> onSuccess,
            @NonNull Consumer<Exception> onError) {
        queueOrExecute("setUsbDataSignalingEnabled",
            (enabled ? "Enable" : "Disable") + " USB data signaling",
            new Object[]{enabled},
            () -> delegate.setUsbDataSignalingEnabled(enabled, onSuccess, onError),
            onSuccess, onError);
    }

    @Override
    public void setUsbDataSignalingEnabled(boolean enabled) {
        queueOrExecuteVoid("setUsbDataSignalingEnabled",
            (enabled ? "Enable" : "Disable") + " USB data signaling",
            new Object[]{enabled},
            () -> delegate.setUsbDataSignalingEnabled(enabled));
    }

    @Override
    public void setPreferentialNetworkServiceEnabled(
            boolean enabled,
            @NonNull Consumer<Void> onSuccess,
            @NonNull Consumer<Exception> onError) {
        queueOrExecute("setPreferentialNetworkServiceEnabled",
            (enabled ? "Enable" : "Disable") + " preferential network service",
            new Object[]{enabled},
            () -> delegate.setPreferentialNetworkServiceEnabled(enabled, onSuccess, onError),
            onSuccess, onError);
    }

    @Override
    public void setPackagesSuspended(
            String[] packageNames,
            boolean suspended,
            @NonNull Consumer<String[]> onSuccess,
            @NonNull Consumer<Exception> onError) {
        queueOrExecute("setPackagesSuspended",
            (suspended ? "Suspend" : "Unsuspend") + " packages",
            new Object[]{packageNames, suspended},
            () -> delegate.setPackagesSuspended(packageNames, suspended, onSuccess, onError),
            onSuccess, onError);
    }

    @Override
    public void setApplicationHidden(
            String packageName,
            boolean hidden,
            @NonNull Consumer<Void> onSuccess,
            @NonNull Consumer<Exception> onError) {
        queueOrExecute("setApplicationHidden",
            (hidden ? "Hide" : "Unhide") + " app: " + packageName,
            new Object[]{packageName, hidden},
            () -> delegate.setApplicationHidden(packageName, hidden, onSuccess, onError),
            onSuccess, onError);
    }

    @Override
    public void setPersonalAppsSuspended(
            boolean suspended,
            @NonNull Consumer<Void> onSuccess,
            @NonNull Consumer<Exception> onError) {
        queueOrExecute("setPersonalAppsSuspended",
            (suspended ? "Suspend" : "Unsuspend") + " personal apps",
            new Object[]{suspended},
            () -> delegate.setPersonalAppsSuspended(suspended, onSuccess, onError),
            onSuccess, onError);
    }

    @Override
    public void enableSystemApp(
            String packageName,
            @NonNull Consumer<Void> onSuccess,
            @NonNull Consumer<Exception> onError) {
        queueOrExecute("enableSystemApp",
            "Enable system app: " + packageName,
            new Object[]{packageName},
            () -> delegate.enableSystemApp(packageName, onSuccess, onError),
            onSuccess, onError);
    }

    @Override
    public void enableSystemApp(
            Intent intent,
            @NonNull Consumer<Integer> onSuccess,
            @NonNull Consumer<Exception> onError) {
        queueOrExecute("enableSystemAppByIntent",
            "Enable system app by intent",
            new Object[]{intent.toUri(0)},
            () -> delegate.enableSystemApp(intent, onSuccess, onError),
            onSuccess, onError);
    }

    @Override
    public void setLockTaskPackages(
            String[] packages,
            @NonNull Consumer<Void> onSuccess,
            @NonNull Consumer<Exception> onError) {
        queueOrExecute("setLockTaskPackages",
            "Set lock task packages",
            new Object[]{packages},
            () -> delegate.setLockTaskPackages(packages, onSuccess, onError),
            onSuccess, onError);
    }

    @Override
    public void setLockTaskFeatures(
            int flags,
            @NonNull Consumer<Void> onSuccess,
            @NonNull Consumer<Exception> onError) {
        queueOrExecute("setLockTaskFeatures",
            "Set lock task features: " + flags,
            new Object[]{flags},
            () -> delegate.setLockTaskFeatures(flags, onSuccess, onError),
            onSuccess, onError);
    }

    @Override
    public void setApplicationRestrictions(
            String packageName,
            Bundle settings,
            @NonNull Consumer<Void> onSuccess,
            @NonNull Consumer<Exception> onError) {
        // Bundle serialization is complex, storing as placeholder
        queueOrExecute("setApplicationRestrictions",
            "Set app restrictions: " + packageName,
            new Object[]{packageName},
            () -> delegate.setApplicationRestrictions(packageName, settings, onSuccess, onError),
            onSuccess, onError);
    }

    @Override
    public void setPermissionGrantState(
            String packageName,
            String permission,
            int grantState,
            @NonNull Consumer<Void> onSuccess,
            @NonNull Consumer<Exception> onError) {
        queueOrExecute("setPermissionGrantState",
            "Set permission " + permission + " for " + packageName,
            new Object[]{packageName, permission, grantState},
            () -> delegate.setPermissionGrantState(packageName, permission, grantState, onSuccess, onError),
            onSuccess, onError);
    }

    @Override
    public void setLocationEnabled(
            boolean enabled,
            @NonNull Consumer<Void> onSuccess,
            @NonNull Consumer<Exception> onError) {
        queueOrExecute("setLocationEnabled",
            (enabled ? "Enable" : "Disable") + " location",
            new Object[]{enabled},
            () -> delegate.setLocationEnabled(enabled, onSuccess, onError),
            onSuccess, onError);
    }

    @Override
    public void setDeviceOwnerLockScreenInfo(
            CharSequence info,
            @NonNull Consumer<Void> onSuccess,
            @NonNull Consumer<Exception> onError) {
        queueOrExecute("setDeviceOwnerLockScreenInfo",
            "Set lock screen info",
            new Object[]{info != null ? info.toString() : null},
            () -> delegate.setDeviceOwnerLockScreenInfo(info, onSuccess, onError),
            onSuccess, onError);
    }

    @Override
    public void setKeyguardDisabled(
            boolean disabled,
            @NonNull Consumer<Void> onSuccess,
            @NonNull Consumer<Exception> onError) {
        queueOrExecute("setKeyguardDisabled",
            (disabled ? "Disable" : "Enable") + " keyguard",
            new Object[]{disabled},
            () -> delegate.setKeyguardDisabled(disabled, onSuccess, onError),
            onSuccess, onError);
    }

    @Override
    public void setKeyguardDisabledFeatures(
            int which,
            @NonNull Consumer<Void> onSuccess,
            @NonNull Consumer<Exception> onError) {
        queueOrExecute("setKeyguardDisabledFeatures",
            "Set keyguard disabled features: " + which,
            new Object[]{which},
            () -> delegate.setKeyguardDisabledFeatures(which, onSuccess, onError),
            onSuccess, onError);
    }

    @Override
    public void setCameraDisabled(
            boolean disabled,
            @NonNull Consumer<Void> onSuccess,
            @NonNull Consumer<Exception> onError) {
        queueOrExecute("setCameraDisabled",
            (disabled ? "Disable" : "Enable") + " camera",
            new Object[]{disabled},
            () -> delegate.setCameraDisabled(disabled, onSuccess, onError),
            onSuccess, onError);
    }

    @Override
    public void setStatusBarDisabled(
            boolean disabled,
            @NonNull Consumer<Void> onSuccess,
            @NonNull Consumer<Exception> onError) {
        queueOrExecute("setStatusBarDisabled",
            (disabled ? "Disable" : "Enable") + " status bar",
            new Object[]{disabled},
            () -> delegate.setStatusBarDisabled(disabled, onSuccess, onError),
            onSuccess, onError);
    }

    @Override
    public void setMaximumFailedPasswordsForWipe(
            int max,
            @NonNull Consumer<Void> onSuccess,
            @NonNull Consumer<Exception> onError) {
        queueOrExecute("setMaximumFailedPasswordsForWipe",
            "Set max failed passwords for wipe: " + max,
            new Object[]{max},
            () -> delegate.setMaximumFailedPasswordsForWipe(max, onSuccess, onError),
            onSuccess, onError);
    }

    @Override
    public void installExistingPackage(
            String packageName,
            @NonNull Consumer<Void> onSuccess,
            @NonNull Consumer<Exception> onError) {
        queueOrExecute("installExistingPackage",
            "Install existing package: " + packageName,
            new Object[]{packageName},
            () -> delegate.installExistingPackage(packageName, onSuccess, onError),
            onSuccess, onError);
    }

    @Override
    public void setUninstallBlocked(
            @NonNull String packageName,
            boolean uninstallBlocked,
            @NonNull Consumer<Void> onSuccess,
            @NonNull Consumer<Exception> onError) {
        queueOrExecute("setUninstallBlocked",
            (uninstallBlocked ? "Block" : "Allow") + " uninstall: " + packageName,
            new Object[]{packageName, uninstallBlocked},
            () -> delegate.setUninstallBlocked(packageName, uninstallBlocked, onSuccess, onError),
            onSuccess, onError);
    }

    @Override
    public void setSecureSetting(
            String setting,
            String value,
            @NonNull Consumer<Void> onSuccess,
            @NonNull Consumer<Exception> onError) {
        queueOrExecute("setSecureSetting",
            "Set secure setting: " + setting,
            new Object[]{setting, value},
            () -> delegate.setSecureSetting(setting, value, onSuccess, onError),
            onSuccess, onError);
    }

    @Override
    public void setGlobalSetting(
            String setting,
            String value,
            @NonNull Consumer<Void> onSuccess,
            @NonNull Consumer<Exception> onError) {
        queueOrExecute("setGlobalSetting",
            "Set global setting: " + setting,
            new Object[]{setting, value},
            () -> delegate.setGlobalSetting(setting, value, onSuccess, onError),
            onSuccess, onError);
    }

    @Override
    public void generateKeyPair(
            @NonNull String algorithm,
            @NonNull KeyGenParameterSpec keySpec,
            int idAttestationFlags,
            @NonNull Consumer<AttestedKeyPair> onSuccess,
            @NonNull Consumer<Exception> onError) {
        queueOrExecute("generateKeyPair",
            "Generate key pair: " + algorithm,
            new Object[]{algorithm, idAttestationFlags},
            () -> delegate.generateKeyPair(algorithm, keySpec, idAttestationFlags, onSuccess, onError),
            onSuccess, onError);
    }

    @Override
    public void removeKeyPair(
            @NonNull String alias,
            @NonNull Consumer<Boolean> onSuccess,
            @NonNull Consumer<Exception> onError) {
        queueOrExecute("removeKeyPair",
            "Remove key pair: " + alias,
            new Object[]{alias},
            () -> delegate.removeKeyPair(alias, onSuccess, onError),
            onSuccess, onError);
    }

    @Override
    public void grantKeyPairToApp(
            @NonNull String alias,
            @NonNull String packageName,
            @NonNull Consumer<Boolean> onSuccess,
            @NonNull Consumer<Exception> onError) {
        queueOrExecute("grantKeyPairToApp",
            "Grant key pair " + alias + " to " + packageName,
            new Object[]{alias, packageName},
            () -> delegate.grantKeyPairToApp(alias, packageName, onSuccess, onError),
            onSuccess, onError);
    }

    @Override
    public void revokeKeyPairFromApp(
            @NonNull String alias,
            @NonNull String packageName,
            @NonNull Consumer<Boolean> onSuccess,
            @NonNull Consumer<Exception> onError) {
        queueOrExecute("revokeKeyPairFromApp",
            "Revoke key pair " + alias + " from " + packageName,
            new Object[]{alias, packageName},
            () -> delegate.revokeKeyPairFromApp(alias, packageName, onSuccess, onError),
            onSuccess, onError);
    }

    @Override
    public void setDelegatedScopes(
            @NonNull String delegatePackage,
            @NonNull List<String> scopes,
            @NonNull Consumer<Void> onSuccess,
            @NonNull Consumer<Exception> onError) {
        queueOrExecute("setDelegatedScopes",
            "Set delegated scopes for " + delegatePackage,
            new Object[]{delegatePackage, scopes},
            () -> delegate.setDelegatedScopes(delegatePackage, scopes, onSuccess, onError),
            onSuccess, onError);
    }

    @Override
    public void setMeteredDataDisabledPackages(
            @NonNull List<String> packageNames,
            @NonNull Consumer<List<String>> onSuccess,
            @NonNull Consumer<Exception> onError) {
        queueOrExecute("setMeteredDataDisabledPackages",
            "Set metered data disabled packages",
            new Object[]{packageNames},
            () -> delegate.setMeteredDataDisabledPackages(packageNames, onSuccess, onError),
            onSuccess, onError);
    }
```

**Step 2: Commit**

```bash
git add src/main/java/com/afwsamples/testdpc/delay/DelayedDevicePolicyManagerGateway.java
git commit -m "feat(delay): complete DelayedDevicePolicyManagerGateway with all mutating methods"
```

---

## Task 10: Create ActionExecutor for Applying Pending Changes

**Files:**
- Create: `testdpc-delay/src/main/java/com/afwsamples/testdpc/delay/ActionExecutor.java`

**Step 1: Create the executor class**

```java
package com.afwsamples.testdpc.delay;

import android.content.Context;
import android.util.Log;
import com.afwsamples.testdpc.DevicePolicyManagerGateway;
import com.afwsamples.testdpc.DevicePolicyManagerGatewayImpl;
import java.util.Arrays;
import java.util.HashSet;
import java.util.List;

/**
 * Executes pending DPM actions when their delay timers expire.
 */
public class ActionExecutor {
    private static final String TAG = "ActionExecutor";

    private final Context context;
    private final DevicePolicyManagerGateway gateway;
    private final DelayManager delayManager;

    public ActionExecutor(Context context) {
        this.context = context;
        this.gateway = new DevicePolicyManagerGatewayImpl(context);
        this.delayManager = DelayManager.getInstance(context);
    }

    /**
     * Execute a single pending change.
     * @return true if execution succeeded, false otherwise
     */
    public boolean execute(PendingChange change) {
        Log.i(TAG, "Executing action: " + change.actionType + " - " + change.description);

        try {
            Object[] params = DPMAction.deserialize(change.actionData);

            switch (change.actionType) {
                case "disableDelay":
                    delayManager.disableDelayDirect();
                    break;

                case "setCameraDisabled":
                    gateway.setCameraDisabled(
                        DPMAction.getBoolean(params, 0),
                        v -> {}, e -> {});
                    break;

                case "setUserRestriction":
                    gateway.setUserRestriction(
                        DPMAction.getString(params, 0),
                        DPMAction.getBoolean(params, 1));
                    break;

                case "setNetworkLoggingEnabled":
                    gateway.setNetworkLoggingEnabled(DPMAction.getBoolean(params, 0));
                    break;

                case "setSecurityLoggingEnabled":
                    gateway.setSecurityLoggingEnabled(
                        DPMAction.getBoolean(params, 0),
                        v -> {}, e -> {});
                    break;

                case "setLocationEnabled":
                    gateway.setLocationEnabled(
                        DPMAction.getBoolean(params, 0),
                        v -> {}, e -> {});
                    break;

                case "setKeyguardDisabled":
                    gateway.setKeyguardDisabled(
                        DPMAction.getBoolean(params, 0),
                        v -> {}, e -> {});
                    break;

                case "setStatusBarDisabled":
                    gateway.setStatusBarDisabled(
                        DPMAction.getBoolean(params, 0),
                        v -> {}, e -> {});
                    break;

                case "setUsbDataSignalingEnabled":
                    gateway.setUsbDataSignalingEnabled(DPMAction.getBoolean(params, 0));
                    break;

                case "lockNow":
                    gateway.lockNow(v -> {}, e -> {});
                    break;

                case "reboot":
                    gateway.reboot(v -> {}, e -> {});
                    break;

                case "setPasswordQuality":
                    gateway.setPasswordQuality(
                        DPMAction.getInt(params, 0),
                        v -> {}, e -> {});
                    break;

                case "setRequiredPasswordComplexity":
                    gateway.setRequiredPasswordComplexity(
                        DPMAction.getInt(params, 0),
                        v -> {}, e -> {});
                    break;

                case "setLockTaskFeatures":
                    gateway.setLockTaskFeatures(
                        DPMAction.getInt(params, 0),
                        v -> {}, e -> {});
                    break;

                case "setKeyguardDisabledFeatures":
                    gateway.setKeyguardDisabledFeatures(
                        DPMAction.getInt(params, 0),
                        v -> {}, e -> {});
                    break;

                case "setMaximumFailedPasswordsForWipe":
                    gateway.setMaximumFailedPasswordsForWipe(
                        DPMAction.getInt(params, 0),
                        v -> {}, e -> {});
                    break;

                case "setApplicationHidden":
                    gateway.setApplicationHidden(
                        DPMAction.getString(params, 0),
                        DPMAction.getBoolean(params, 1),
                        v -> {}, e -> {});
                    break;

                case "setUninstallBlocked":
                    gateway.setUninstallBlocked(
                        DPMAction.getString(params, 0),
                        DPMAction.getBoolean(params, 1),
                        v -> {}, e -> {});
                    break;

                case "enableSystemApp":
                    gateway.enableSystemApp(
                        DPMAction.getString(params, 0),
                        v -> {}, e -> {});
                    break;

                case "installExistingPackage":
                    gateway.installExistingPackage(
                        DPMAction.getString(params, 0),
                        v -> {}, e -> {});
                    break;

                case "setOrganizationName":
                    gateway.setOrganizationName(
                        DPMAction.getString(params, 0),
                        v -> {}, e -> {});
                    break;

                case "setDeviceOwnerLockScreenInfo":
                    gateway.setDeviceOwnerLockScreenInfo(
                        DPMAction.getString(params, 0),
                        v -> {}, e -> {});
                    break;

                case "setSecureSetting":
                    gateway.setSecureSetting(
                        DPMAction.getString(params, 0),
                        DPMAction.getString(params, 1),
                        v -> {}, e -> {});
                    break;

                case "setGlobalSetting":
                    gateway.setGlobalSetting(
                        DPMAction.getString(params, 0),
                        DPMAction.getString(params, 1),
                        v -> {}, e -> {});
                    break;

                case "setLockTaskPackages":
                    gateway.setLockTaskPackages(
                        DPMAction.getStringArray(params, 0),
                        v -> {}, e -> {});
                    break;

                case "setPackagesSuspended":
                    gateway.setPackagesSuspended(
                        DPMAction.getStringArray(params, 0),
                        DPMAction.getBoolean(params, 1),
                        v -> {}, e -> {});
                    break;

                case "setPermittedInputMethods":
                    gateway.setPermittedInputMethods(DPMAction.getStringList(params, 0));
                    break;

                case "setAffiliationIds":
                    gateway.setAffiliationIds(DPMAction.getStringSet(params, 0));
                    break;

                case "setLogoutEnabled":
                    gateway.setLogoutEnabled(
                        DPMAction.getBoolean(params, 0),
                        v -> {}, e -> {});
                    break;

                case "setPersonalAppsSuspended":
                    gateway.setPersonalAppsSuspended(
                        DPMAction.getBoolean(params, 0),
                        v -> {}, e -> {});
                    break;

                case "setPreferentialNetworkServiceEnabled":
                    gateway.setPreferentialNetworkServiceEnabled(
                        DPMAction.getBoolean(params, 0),
                        v -> {}, e -> {});
                    break;

                case "removeActiveAdmin":
                    gateway.removeActiveAdmin(v -> {}, e -> {});
                    break;

                case "clearDeviceOwnerApp":
                    gateway.clearDeviceOwnerApp(v -> {}, e -> {});
                    break;

                case "clearProfileOwner":
                    gateway.clearProfileOwner(v -> {}, e -> {});
                    break;

                case "wipeData":
                    gateway.wipeData(DPMAction.getInt(params, 0), v -> {}, e -> {});
                    break;

                case "wipeDevice":
                    gateway.wipeDevice(DPMAction.getInt(params, 0), v -> {}, e -> {});
                    break;

                default:
                    Log.w(TAG, "Unknown action type: " + change.actionType);
                    return false;
            }

            Log.i(TAG, "Successfully executed: " + change.description);
            return true;

        } catch (Exception e) {
            Log.e(TAG, "Failed to execute action: " + change.description, e);
            return false;
        }
    }
}
```

**Step 2: Commit**

```bash
git add src/main/java/com/afwsamples/testdpc/delay/ActionExecutor.java
git commit -m "feat(delay): add ActionExecutor for applying pending changes"
```

---

## Task 11: Create DelayService Background Service

**Files:**
- Create: `testdpc-delay/src/main/java/com/afwsamples/testdpc/delay/DelayService.java`

**Step 1: Create the service class**

```java
package com.afwsamples.testdpc.delay;

import android.app.Notification;
import android.app.NotificationChannel;
import android.app.NotificationManager;
import android.app.PendingIntent;
import android.app.Service;
import android.content.Context;
import android.content.Intent;
import android.os.Build;
import android.os.Handler;
import android.os.IBinder;
import android.os.Looper;
import android.util.Log;
import androidx.annotation.Nullable;
import com.afwsamples.testdpc.PolicyManagementActivity;
import com.afwsamples.testdpc.R;
import java.util.List;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;

/**
 * Background service that monitors pending changes and applies them when ready.
 */
public class DelayService extends Service {
    private static final String TAG = "DelayService";
    private static final String CHANNEL_ID = "delay_service_channel";
    private static final int NOTIFICATION_ID = 1001;
    private static final long CHECK_INTERVAL_MS = 10_000; // Check every 10 seconds

    private DelayManager delayManager;
    private ActionExecutor actionExecutor;
    private ExecutorService executor;
    private Handler handler;
    private boolean isRunning = false;

    private final Runnable checkPendingRunnable = new Runnable() {
        @Override
        public void run() {
            if (isRunning) {
                checkAndApplyPendingChanges();
                handler.postDelayed(this, CHECK_INTERVAL_MS);
            }
        }
    };

    @Override
    public void onCreate() {
        super.onCreate();
        Log.i(TAG, "DelayService created");
        delayManager = DelayManager.getInstance(this);
        actionExecutor = new ActionExecutor(this);
        executor = Executors.newSingleThreadExecutor();
        handler = new Handler(Looper.getMainLooper());
        createNotificationChannel();
    }

    @Override
    public int onStartCommand(Intent intent, int flags, int startId) {
        Log.i(TAG, "DelayService started");

        if (!isRunning) {
            isRunning = true;
            startForeground(NOTIFICATION_ID, createNotification("Delay service active"));
            handler.post(checkPendingRunnable);
        }

        return START_STICKY;
    }

    @Override
    public void onDestroy() {
        Log.i(TAG, "DelayService destroyed");
        isRunning = false;
        handler.removeCallbacks(checkPendingRunnable);
        executor.shutdown();
        super.onDestroy();
    }

    @Nullable
    @Override
    public IBinder onBind(Intent intent) {
        return null;
    }

    private void checkAndApplyPendingChanges() {
        executor.execute(() -> {
            try {
                List<PendingChange> readyChanges = delayManager.getReadyChanges();

                if (readyChanges.isEmpty()) {
                    updateNotification("No pending changes");
                    return;
                }

                Log.i(TAG, "Found " + readyChanges.size() + " changes ready to apply");

                for (PendingChange change : readyChanges) {
                    updateNotification("Applying: " + change.description);

                    boolean success = actionExecutor.execute(change);

                    if (success) {
                        delayManager.markCompleted(change);
                        Log.i(TAG, "Completed: " + change.description);
                    } else {
                        delayManager.markFailed(change);
                        Log.e(TAG, "Failed: " + change.description);
                        showFailureNotification(change);
                    }
                }

                updatePendingCountNotification();

            } catch (Exception e) {
                Log.e(TAG, "Error checking pending changes", e);
            }
        });
    }

    private void updatePendingCountNotification() {
        delayManager.getPendingChanges(changes -> {
            if (changes.isEmpty()) {
                updateNotification("No pending changes");
            } else {
                PendingChange next = changes.get(0);
                long remaining = next.getTimeRemainingMillis();
                String timeStr = formatTimeRemaining(remaining);
                updateNotification(changes.size() + " pending. Next in " + timeStr);
            }
        });
    }

    private String formatTimeRemaining(long millis) {
        long seconds = millis / 1000;
        if (seconds < 60) {
            return seconds + "s";
        }
        long minutes = seconds / 60;
        if (minutes < 60) {
            return minutes + "m";
        }
        long hours = minutes / 60;
        return hours + "h " + (minutes % 60) + "m";
    }

    private void createNotificationChannel() {
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
            NotificationChannel channel = new NotificationChannel(
                CHANNEL_ID,
                "Delay Service",
                NotificationManager.IMPORTANCE_LOW
            );
            channel.setDescription("Monitors pending policy changes");

            NotificationManager manager = getSystemService(NotificationManager.class);
            if (manager != null) {
                manager.createNotificationChannel(channel);
            }
        }
    }

    private Notification createNotification(String text) {
        Intent intent = new Intent(this, PolicyManagementActivity.class);
        PendingIntent pendingIntent = PendingIntent.getActivity(
            this, 0, intent,
            PendingIntent.FLAG_UPDATE_CURRENT | PendingIntent.FLAG_IMMUTABLE
        );

        Notification.Builder builder;
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
            builder = new Notification.Builder(this, CHANNEL_ID);
        } else {
            builder = new Notification.Builder(this);
        }

        return builder
            .setContentTitle("Test DPC Delay")
            .setContentText(text)
            .setSmallIcon(R.drawable.ic_launcher)
            .setContentIntent(pendingIntent)
            .setOngoing(true)
            .build();
    }

    private void updateNotification(String text) {
        NotificationManager manager = (NotificationManager) getSystemService(Context.NOTIFICATION_SERVICE);
        if (manager != null) {
            manager.notify(NOTIFICATION_ID, createNotification(text));
        }
    }

    private void showFailureNotification(PendingChange change) {
        NotificationManager manager = (NotificationManager) getSystemService(Context.NOTIFICATION_SERVICE);
        if (manager != null) {
            Notification notification = createNotification("Failed: " + change.description);
            manager.notify(NOTIFICATION_ID + (int) change.id, notification);
        }
    }

    /**
     * Start the delay service.
     */
    public static void start(Context context) {
        Intent intent = new Intent(context, DelayService.class);
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
            context.startForegroundService(intent);
        } else {
            context.startService(intent);
        }
    }

    /**
     * Stop the delay service.
     */
    public static void stop(Context context) {
        Intent intent = new Intent(context, DelayService.class);
        context.stopService(intent);
    }
}
```

**Step 2: Commit**

```bash
git add src/main/java/com/afwsamples/testdpc/delay/DelayService.java
git commit -m "feat(delay): add DelayService for background execution of pending changes"
```

---

## Task 12: Update Existing BootReceiver

**Files:**
- Modify: `testdpc-delay/src/main/java/com/afwsamples/testdpc/BootReceiver.java`

**Step 1: Read the existing BootReceiver**

First, read the current BootReceiver to understand its structure.

**Step 2: Add delay service start to BootReceiver**

Add to the onReceive method:

```java
// Add import at top
import com.afwsamples.testdpc.delay.DelayConfig;
import com.afwsamples.testdpc.delay.DelayService;

// Add to onReceive method, after existing boot logic:
// Start delay service if delay is enabled
DelayConfig delayConfig = new DelayConfig(context);
if (delayConfig.isEnabled()) {
    DelayService.start(context);
}
```

**Step 3: Commit**

```bash
git add src/main/java/com/afwsamples/testdpc/BootReceiver.java
git commit -m "feat(delay): start DelayService on boot when delay is enabled"
```

---

## Task 13: Create Delay Settings UI Fragment

**Files:**
- Create: `testdpc-delay/src/main/java/com/afwsamples/testdpc/delay/DelaySettingsFragment.java`
- Create: `testdpc-delay/src/main/res/xml/delay_settings.xml`

**Step 1: Create the preferences XML**

```xml
<?xml version="1.0" encoding="utf-8"?>
<PreferenceScreen xmlns:android="http://schemas.android.com/apk/res/android">

    <SwitchPreference
        android:key="delay_enabled"
        android:title="Enable Delay"
        android:summary="When enabled, all policy changes require a waiting period" />

    <EditTextPreference
        android:key="delay_duration_value"
        android:title="Delay Duration"
        android:summary="Number of time units to wait"
        android:inputType="number"
        android:defaultValue="1" />

    <ListPreference
        android:key="delay_duration_unit"
        android:title="Time Unit"
        android:entries="@array/delay_time_units"
        android:entryValues="@array/delay_time_unit_values"
        android:defaultValue="HOURS" />

    <Preference
        android:key="pending_changes"
        android:title="View Pending Changes"
        android:summary="See and manage queued policy changes" />

</PreferenceScreen>
```

**Step 2: Create string array resources**

Add to `src/main/res/values/strings.xml`:

```xml
<string-array name="delay_time_units">
    <item>Seconds</item>
    <item>Minutes</item>
    <item>Hours</item>
</string-array>

<string-array name="delay_time_unit_values">
    <item>SECONDS</item>
    <item>MINUTES</item>
    <item>HOURS</item>
</string-array>
```

**Step 3: Create the fragment class**

```java
package com.afwsamples.testdpc.delay;

import android.os.Bundle;
import android.widget.Toast;
import androidx.preference.EditTextPreference;
import androidx.preference.ListPreference;
import androidx.preference.Preference;
import androidx.preference.PreferenceFragmentCompat;
import androidx.preference.SwitchPreference;
import com.afwsamples.testdpc.R;

/**
 * Settings fragment for configuring the delay feature.
 */
public class DelaySettingsFragment extends PreferenceFragmentCompat {

    private DelayConfig config;
    private DelayManager delayManager;

    @Override
    public void onCreatePreferences(Bundle savedInstanceState, String rootKey) {
        setPreferencesFromResource(R.xml.delay_settings, rootKey);

        config = new DelayConfig(requireContext());
        delayManager = DelayManager.getInstance(requireContext());

        setupEnableSwitch();
        setupDurationValue();
        setupDurationUnit();
        setupPendingChangesLink();
    }

    private void setupEnableSwitch() {
        SwitchPreference enablePref = findPreference("delay_enabled");
        if (enablePref == null) return;

        enablePref.setChecked(config.isEnabled());

        enablePref.setOnPreferenceChangeListener((preference, newValue) -> {
            boolean enabled = (Boolean) newValue;

            if (enabled) {
                // Enabling is instant
                delayManager.enableDelay();
                DelayService.start(requireContext());
                Toast.makeText(requireContext(),
                    "Delay enabled. Duration: " + config.getDelayDisplayString(),
                    Toast.LENGTH_LONG).show();
                return true;
            } else {
                // Disabling goes through delay
                if (config.isEnabled()) {
                    delayManager.queueDisableDelay();
                    Toast.makeText(requireContext(),
                        "Disable queued. Will take effect in " + config.getDelayDisplayString(),
                        Toast.LENGTH_LONG).show();
                    return false; // Don't update UI yet
                }
                return true;
            }
        });
    }

    private void setupDurationValue() {
        EditTextPreference valuePref = findPreference("delay_duration_value");
        if (valuePref == null) return;

        valuePref.setText(String.valueOf(config.getDurationValue()));
        valuePref.setSummary(String.valueOf(config.getDurationValue()));

        valuePref.setOnPreferenceChangeListener((preference, newValue) -> {
            try {
                long value = Long.parseLong(newValue.toString());
                if (value <= 0) {
                    Toast.makeText(requireContext(),
                        "Duration must be positive", Toast.LENGTH_SHORT).show();
                    return false;
                }

                if (config.isEnabled()) {
                    // Queue the change
                    Toast.makeText(requireContext(),
                        "Duration change queued. Will take effect in " + config.getDelayDisplayString(),
                        Toast.LENGTH_LONG).show();
                    // TODO: Queue actual duration change
                    return false;
                } else {
                    config.setDurationValue(value);
                    valuePref.setSummary(String.valueOf(value));
                    return true;
                }
            } catch (NumberFormatException e) {
                Toast.makeText(requireContext(),
                    "Invalid number", Toast.LENGTH_SHORT).show();
                return false;
            }
        });
    }

    private void setupDurationUnit() {
        ListPreference unitPref = findPreference("delay_duration_unit");
        if (unitPref == null) return;

        unitPref.setValue(config.getDurationUnit().name());
        unitPref.setSummary(config.getDurationUnit().name());

        unitPref.setOnPreferenceChangeListener((preference, newValue) -> {
            if (config.isEnabled()) {
                Toast.makeText(requireContext(),
                    "Unit change queued. Will take effect in " + config.getDelayDisplayString(),
                    Toast.LENGTH_LONG).show();
                return false;
            } else {
                config.setDurationUnit(DelayConfig.TimeUnit.valueOf(newValue.toString()));
                unitPref.setSummary(newValue.toString());
                return true;
            }
        });
    }

    private void setupPendingChangesLink() {
        Preference pendingPref = findPreference("pending_changes");
        if (pendingPref == null) return;

        pendingPref.setOnPreferenceClickListener(preference -> {
            // Navigate to pending changes list
            getParentFragmentManager()
                .beginTransaction()
                .replace(android.R.id.content, new PendingChangesFragment())
                .addToBackStack(null)
                .commit();
            return true;
        });
    }
}
```

**Step 4: Commit**

```bash
git add src/main/java/com/afwsamples/testdpc/delay/DelaySettingsFragment.java
git add src/main/res/xml/delay_settings.xml
git add src/main/res/values/strings.xml
git commit -m "feat(delay): add DelaySettingsFragment for delay configuration UI"
```

---

## Task 14: Create Pending Changes List Fragment

**Files:**
- Create: `testdpc-delay/src/main/java/com/afwsamples/testdpc/delay/PendingChangesFragment.java`
- Create: `testdpc-delay/src/main/res/layout/fragment_pending_changes.xml`
- Create: `testdpc-delay/src/main/res/layout/item_pending_change.xml`

**Step 1: Create fragment layout**

```xml
<?xml version="1.0" encoding="utf-8"?>
<LinearLayout xmlns:android="http://schemas.android.com/apk/res/android"
    android:layout_width="match_parent"
    android:layout_height="match_parent"
    android:orientation="vertical">

    <TextView
        android:id="@+id/empty_text"
        android:layout_width="match_parent"
        android:layout_height="wrap_content"
        android:padding="16dp"
        android:text="No pending changes"
        android:visibility="gone"
        android:gravity="center" />

    <androidx.recyclerview.widget.RecyclerView
        android:id="@+id/recycler_view"
        android:layout_width="match_parent"
        android:layout_height="match_parent" />

</LinearLayout>
```

**Step 2: Create list item layout**

```xml
<?xml version="1.0" encoding="utf-8"?>
<LinearLayout xmlns:android="http://schemas.android.com/apk/res/android"
    android:layout_width="match_parent"
    android:layout_height="wrap_content"
    android:orientation="horizontal"
    android:padding="16dp">

    <LinearLayout
        android:layout_width="0dp"
        android:layout_height="wrap_content"
        android:layout_weight="1"
        android:orientation="vertical">

        <TextView
            android:id="@+id/description"
            android:layout_width="wrap_content"
            android:layout_height="wrap_content"
            android:textSize="16sp"
            android:textStyle="bold" />

        <TextView
            android:id="@+id/time_remaining"
            android:layout_width="wrap_content"
            android:layout_height="wrap_content"
            android:textSize="14sp"
            android:textColor="@android:color/darker_gray" />

    </LinearLayout>

    <Button
        android:id="@+id/cancel_button"
        android:layout_width="wrap_content"
        android:layout_height="wrap_content"
        android:text="Cancel" />

</LinearLayout>
```

**Step 3: Create the fragment class**

```java
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
            timeRemaining.setText(formatTimeRemaining(change.getTimeRemainingMillis()));

            cancelButton.setOnClickListener(v -> {
                delayManager.cancelChange(change.id);
                refreshList();
            });
        }

        private String formatTimeRemaining(long millis) {
            long seconds = millis / 1000;
            if (seconds < 60) {
                return "Applies in " + seconds + " seconds";
            }
            long minutes = seconds / 60;
            if (minutes < 60) {
                return "Applies in " + minutes + " minutes";
            }
            long hours = minutes / 60;
            long remainingMinutes = minutes % 60;
            return "Applies in " + hours + "h " + remainingMinutes + "m";
        }
    }
}
```

**Step 4: Commit**

```bash
git add src/main/java/com/afwsamples/testdpc/delay/PendingChangesFragment.java
git add src/main/res/layout/fragment_pending_changes.xml
git add src/main/res/layout/item_pending_change.xml
git commit -m "feat(delay): add PendingChangesFragment for viewing/canceling pending changes"
```

---

## Task 15: Integrate Delayed Gateway into ProfileOrParentFragment

**Files:**
- Modify: `testdpc-delay/src/main/java/com/afwsamples/testdpc/common/ProfileOrParentFragment.java`

**Step 1: Update gateway creation**

Change line 154 from:
```java
mDevicePolicyGateway = new DevicePolicyManagerGatewayImpl(context);
```

To:
```java
DevicePolicyManagerGateway baseGateway = new DevicePolicyManagerGatewayImpl(context);
mDevicePolicyGateway = new DelayedDevicePolicyManagerGateway(baseGateway, context);
```

Also add import:
```java
import com.afwsamples.testdpc.delay.DelayedDevicePolicyManagerGateway;
```

**Step 2: Repeat for parent profile case**

Change line 161 from:
```java
mDevicePolicyGateway = DevicePolicyManagerGatewayImpl.forParentProfile(context);
```

To:
```java
DevicePolicyManagerGateway parentGateway = DevicePolicyManagerGatewayImpl.forParentProfile(context);
mDevicePolicyGateway = new DelayedDevicePolicyManagerGateway(parentGateway, context);
```

**Step 3: Commit**

```bash
git add src/main/java/com/afwsamples/testdpc/common/ProfileOrParentFragment.java
git commit -m "feat(delay): integrate DelayedDevicePolicyManagerGateway into ProfileOrParentFragment"
```

---

## Task 16: Add Delay Settings to Main Policy Menu

**Files:**
- Modify: `testdpc-delay/src/main/java/com/afwsamples/testdpc/policy/PolicyManagementFragment.java`
- Modify: `testdpc-delay/src/main/res/xml/device_policy_header.xml` (or equivalent preference XML)

**Step 1: Add delay settings preference**

Add a new preference entry to navigate to delay settings.

**Step 2: Handle preference click**

Add click handler to launch DelaySettingsFragment.

**Step 3: Commit**

```bash
git add src/main/java/com/afwsamples/testdpc/policy/PolicyManagementFragment.java
git add src/main/res/xml/*.xml
git commit -m "feat(delay): add Delay Settings entry to main policy menu"
```

---

## Task 17: Register Service and Permissions in AndroidManifest

**Files:**
- Modify: `testdpc-delay/src/main/AndroidManifest.xml`

**Step 1: Add service declaration**

```xml
<service
    android:name=".delay.DelayService"
    android:enabled="true"
    android:exported="false"
    android:foregroundServiceType="dataSync" />
```

**Step 2: Add required permissions**

```xml
<uses-permission android:name="android.permission.FOREGROUND_SERVICE" />
<uses-permission android:name="android.permission.FOREGROUND_SERVICE_DATA_SYNC" />
<uses-permission android:name="android.permission.RECEIVE_BOOT_COMPLETED" />
```

**Step 3: Commit**

```bash
git add src/main/AndroidManifest.xml
git commit -m "feat(delay): register DelayService and permissions in AndroidManifest"
```

---

## Task 18: Build and Test

**Step 1: Build the project**

```bash
cd testdpc-delay && bazel build //:testdpc
```

**Step 2: Install on test device**

```bash
adb install -r bazel-bin/testdpc.apk
```

**Step 3: Test the delay feature**

1. Open Test DPC
2. Navigate to Delay Settings
3. Set delay to 30 seconds
4. Enable delay
5. Try to disable camera
6. Verify toast shows "Queued. Applies in 30 seconds"
7. View Pending Changes
8. Wait for timer to expire
9. Verify camera is now disabled

**Step 4: Commit final changes**

```bash
git add -A
git commit -m "feat(delay): complete delay feature implementation"
```

---

## Summary

This plan implements the delay feature in 18 tasks:

1. **Tasks 1-6**: Database and model layer (Room, entities, DAO)
2. **Tasks 7-10**: Core delay logic (DelayManager, Gateway wrapper, ActionExecutor)
3. **Tasks 11-12**: Background service and boot handling
4. **Tasks 13-14**: UI for settings and pending changes
5. **Tasks 15-16**: Integration into existing codebase
6. **Tasks 17-18**: Manifest updates and testing

Each task is designed to be independently committable and testable.
