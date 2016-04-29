package com.cyberwalkabout.youtube.lib.subscription;

import android.app.AlarmManager;
import android.app.NotificationManager;
import android.app.PendingIntent;
import android.content.Context;
import android.content.Intent;
import android.content.SharedPreferences;
import android.graphics.BitmapFactory;
import android.os.AsyncTask;
import android.os.Handler;
import android.os.Looper;
import android.os.SystemClock;
import android.preference.PreferenceManager;
import android.support.v4.app.NotificationCompat;
import android.support.v4.content.LocalBroadcastManager;
import android.util.Log;

import com.cyberwalkabout.youtube.lib.AbstractVideosActivity;
import com.cyberwalkabout.youtube.lib.AllVideosScreen;
import com.cyberwalkabout.youtube.lib.R;
import com.cyberwalkabout.youtube.lib.analytics.FlurryAnalytics;
import com.cyberwalkabout.youtube.lib.app.ChildrenTVApp;

import java.text.DateFormat;
import java.util.Date;

/**
 * This is class created to help to handle donate feature (notifications, popups, purchase etc...)
 * <p/>
 * Donate feature intended to request user to purchase subscription in a smart way.
 * It is designed to be not annoying and appear only when user session is done.
 * <p/>
 * Following rules define workflow of the donate related functionality:
 * - Display 'donate' notification only if user didn't purchase subscription already
 * - Last notification displayed more then specified limit (default not more then 1 notification per 3 days)
 * - User watched some valuable amount of video during session, which means user really using application (default 5 minutes of video to watch)
 * - Display 'donate' notification to user with 30 sec delay after user left application (for instance pressed home button)
 *
 * @author Andrii Kovalov
 */
public class SubscriptionHelper {
    private static final String TAG = SubscriptionHelper.class.getSimpleName();

    public static final String REQUEST_DONATE = "donate_requested";

    private static final int DONATE_NOTIFICATION_ID = 1001;

    private static String KEY_IS_SUBSCRIPTION_VALID = "is_subscripion_valid";
    private static String KEY_TIME_WATCHED = "time_watched";
    private static String KEY_LAST_POPUP_DELAY = "last_popup_delay";

    private SharedPreferences prefs;

    public SubscriptionHelper(Context context) {
        if (context == null) {
            throw new IllegalArgumentException("Context can't be null");
        }
        prefs = PreferenceManager.getDefaultSharedPreferences(context);
    }

    /**
     * Check if user has valid subscription
     *
     * @return
     */
    public boolean hasSubscription() {
        return prefs.getBoolean(KEY_IS_SUBSCRIPTION_VALID, false);
    }

    /**
     * Set user subscription state (valid/invalid)
     *
     * @param isValid
     */
    public void setSubscriptionValid(boolean isValid) {
        prefs.edit().putBoolean(KEY_IS_SUBSCRIPTION_VALID, isValid).apply();
    }

    /**
     * Reset to zero watched video time.
     * We reset this value each usage session.
     */
    public void resetTimeWatched() {
        prefs.edit().putLong(KEY_TIME_WATCHED, 0).apply();
    }

    /**
     * Get watched time video recorded by now.
     *
     * @return
     */
    public long getTimeWatched() {
        return prefs.getLong(KEY_TIME_WATCHED, 0L);
    }

    /**
     * Add watched video time in milliseconds.
     *
     * @param time
     */
    public void addTimeWatched(long time) {
        if (!hasSubscription()) {
            long timeWatched = getTimeWatched();
            timeWatched += time;
            prefs.edit().putLong(KEY_TIME_WATCHED, timeWatched).apply();
        }
    }

    /**
     * Set donate notification display timestamp
     *
     * @param timestamp
     */
    public void setNotificationTimestamp(long timestamp) {
        prefs.edit().putLong(KEY_LAST_POPUP_DELAY, timestamp).putLong(KEY_TIME_WATCHED, 0).apply();
    }

    /**
     * Get donate notification display timestamp
     *
     * @return
     */
    public long getNotificationTimestamp() {
        return prefs.getLong(KEY_LAST_POPUP_DELAY, 0);
    }

    /**
     * Verify that application has to display donate notification.
     * Current rules:
     * - User didn't purchase subscription
     * - Last notification displayed more then specified limit (3 days)
     * - User watched enough video during usage session (5 minutes)
     *
     * @param context
     * @return
     */
    public boolean hasToDisplayNotification(Context context) {
        boolean showNotification = false;
        if (!hasSubscription()) {
            boolean exceededTimeSinceLastNotification = System.currentTimeMillis() - getNotificationTimestamp() >= Long.valueOf(context.getString(R.string.donate_notification_display_every_ms));
            boolean enoughVideoWatchedToTriggerNotification = getTimeWatched() >= Long.valueOf(context.getString(R.string.time_of_video_to_watch_before_donate_notification_ms));

            showNotification = exceededTimeSinceLastNotification && enoughVideoWatchedToTriggerNotification;
        }
        return showNotification;
    }

    /**
     * Schedule donate notification to be displayed with delay configured in config.xml (1 min)
     *
     * @param context
     */
    public void scheduleNotification(final Context context) {
        long delay = Long.valueOf(context.getString(R.string.donate_notification_delay_ms));
        Log.d(TAG, "Application isn't in foreground, probably user pressed 'home button' recently. Schedule notification to be displayed in " + (delay / 1000) + "s");

        Intent intent = new Intent(context.getApplicationContext(), SubscriptionNotificationReceiver.class);
        PendingIntent pendingIntent = PendingIntent.getBroadcast(context.getApplicationContext(), 0, intent, 0);

        AlarmManager alarmManager = (AlarmManager) context.getSystemService(Context.ALARM_SERVICE);
        alarmManager.set(AlarmManager.ELAPSED_REALTIME, SystemClock.elapsedRealtime() + delay, pendingIntent);
    }

    public void tryToScheduleNotification(final Context context) {
        new Handler(Looper.getMainLooper()).postDelayed(new Runnable() {
            @Override
            public void run() {
                if (!ChildrenTVApp.inForeground()) {
                    new AsyncTask<Void, Void, Boolean>() {

                        @Override
                        protected Boolean doInBackground(Void... params) {
                            return hasToDisplayNotification(context);
                        }

                        @Override
                        protected void onPostExecute(Boolean displayNotification) {
                            if (displayNotification) {
                                scheduleNotification(context);
                            } else {
                                Log.d(TAG, "Not sufficient conditions to display donate notification at the moment.");
                            }

                        }
                    }.execute();
                }
            }
        }, 1000); // wait 1 sec to make sure that user left application
    }

    public void displayNotification(Context context) {
        // The Intent to be used when the user clicks on the Notification View
        Intent intent = new Intent(context, AllVideosScreen.class);
        intent.putExtra(REQUEST_DONATE, true);

        // The PendingIntent that wraps the underlying Intent
        PendingIntent contentIntent = PendingIntent.getActivity(context, 0, intent, PendingIntent.FLAG_CANCEL_CURRENT);

        // Build the Notification
        NotificationCompat.Builder notificationBuilder = new NotificationCompat.Builder(context)
                .setAutoCancel(true)
                .setLargeIcon(BitmapFactory.decodeResource(context.getResources(), R.drawable.launcher_icon))
                .setSmallIcon(R.drawable.ic_donate_notification_small)
                .setTicker(context.getString(R.string.donate_notification_text))
                .setContentTitle(context.getString(R.string.donate_notification_title))
                .setContentText(context.getString(R.string.donate_notification_text))
                .setContentIntent(contentIntent)
                .setStyle(new NotificationCompat.BigTextStyle().bigText(context.getString(R.string.donate_notification_text)));

        // Get the NotificationManager
        NotificationManager notificationManager = (NotificationManager) context.getSystemService(Context.NOTIFICATION_SERVICE);

        // Pass the Notification to the NotificationManager:
        notificationManager.notify(DONATE_NOTIFICATION_ID, notificationBuilder.build());

        FlurryAnalytics.getInstance().subscriptionNotification();

        // Log occurrence of notify() call
        Log.d(TAG, "Display donate notification at:" + DateFormat.getDateTimeInstance().format(new Date()));
    }

    public void initiatePurchaseProcess(Context context) {
        FlurryAnalytics.getInstance().subscriptionShowPurchaseDialog();
        LocalBroadcastManager.getInstance(context).sendBroadcast(new Intent(AbstractVideosActivity.PURCHASE_SUBSCRIPTION_ACTION));
    }

    public boolean isSubscriptionPurchaseRequested(Intent intent) {
        return intent != null && intent.hasExtra(REQUEST_DONATE);
    }
}
