package com.afwsamples.testdpc.feedback;

import android.app.NotificationChannel;
import android.app.NotificationManager;
import android.os.Build.VERSION_CODES;
import androidx.core.app.NotificationCompat;
import androidx.core.app.NotificationManagerCompat;
import androidx.preference.PreferenceManager;
import android.util.Log;
import androidx.enterprise.feedback.KeyedAppState;
import androidx.enterprise.feedback.KeyedAppStatesService;
import androidx.enterprise.feedback.ReceivedKeyedAppState;
import com.afwsamples.testdpc.R;
import com.afwsamples.testdpc.common.Util;
import java.util.Collection;
import java.util.HashMap;
import java.util.Map;

/**
 * Receive {@link KeyedAppState} instances and show them as a notification.
 *
 * <p>Also logs received keyed app states using the tag "KeyedAppStates".
 */
public class AppStatesService extends KeyedAppStatesService {

  private static final String CHANNEL_ID = "KeyedAppStates";
  private static final String CHANNEL_NAME = "Keyed App States";

  private int nextNotificationId = 0;
  private Map<String, Integer> idMapping = new HashMap<>();

  @Override
  public void onReceive(Collection<ReceivedKeyedAppState> states, boolean requestSync) {
    boolean shouldNotify =
        PreferenceManager.getDefaultSharedPreferences(this)
            .getBoolean(getString(R.string.app_feedback_notifications), false);

    if (!shouldNotify) {
      return;
    }

    createNotificationChannel();

    for (ReceivedKeyedAppState state : states) {
      showNotification(state, requestSync);
    }
  }

  private void showNotification(ReceivedKeyedAppState state, boolean requestSync) {
    Log.i("KeyedAppStates",
        state.timestamp() + " " +
            state.packageName() + ":" +
            state.key() + "=" +
            state.data() + " (" +
            state.message() + ")" + (requestSync ? " - SYNC REQUESTED" : ""));

    NotificationCompat.Builder notificationBuilder =
        new NotificationCompat.Builder(this, CHANNEL_ID)
            .setSmallIcon(R.drawable.arrow_down)
            .setContentTitle(state.packageName() + ":" + state.key())
            .setContentText(state.timestamp() + " " +
                state.data() +
                " (" + state.message() +")" +
                (requestSync ? "\nSYNC REQUESTED" : ""));
    NotificationManagerCompat notificationManager = NotificationManagerCompat.from(this);
    notificationManager.notify(getIdForState(state), notificationBuilder.build());
  }

  private void createNotificationChannel() {
    if (Util.SDK_INT >= VERSION_CODES.O) {
      NotificationChannel channel =
          new NotificationChannel(CHANNEL_ID, CHANNEL_NAME, NotificationManager.IMPORTANCE_DEFAULT);
      NotificationManager notificationManager = getSystemService(NotificationManager.class);
      notificationManager.createNotificationChannel(channel);
    }
  }

  private int getIdForState(ReceivedKeyedAppState state) {
    String key = state.packageName() + ":" + state.key();

    if (!idMapping.containsKey(key)) {
      idMapping.put(key, nextNotificationId++);
    }
    return idMapping.get(key);
  }
}
