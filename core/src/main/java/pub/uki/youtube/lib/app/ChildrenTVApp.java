package pub.uki.youtube.lib.app;

import android.app.AlarmManager;
import android.app.PendingIntent;
import android.content.Intent;
import android.os.AsyncTask;
import android.os.SystemClock;
import android.support.multidex.MultiDexApplication;
import android.support.v4.os.AsyncTaskCompat;
import android.util.Log;

import pub.uki.youtube.lib.R;
import pub.uki.youtube.lib.analytics.FlurryAnalytics;
import pub.uki.youtube.lib.data.DataLoadReceiver;
import pub.uki.youtube.lib.data.db.DbHelper;
import pub.uki.youtube.lib.subscription.SubscriptionHelper;
import pub.uki.youtube.lib.util.Identity;
import com.flurry.android.FlurryAgent;
import com.google.android.libraries.cast.companionlibrary.cast.VideoCastManager;
import com.nostra13.universalimageloader.core.DisplayImageOptions;
import com.nostra13.universalimageloader.core.ImageLoader;
import com.nostra13.universalimageloader.core.ImageLoaderConfiguration;

/**
 * @author Maria Dzyokh
 * @author Andrii Kovalov
 */
public class ChildrenTVApp extends MultiDexApplication {
    public static final double VOLUME_INCREMENT = 0.05;

    private static boolean ACTIVITY_VISIBLE;

    private volatile DbHelper dbHelper;

    public static boolean inForeground() {
        return ACTIVITY_VISIBLE;
    }

    public static void activityResumed() {
        ACTIVITY_VISIBLE = true;
    }

    public static void activityPaused() {
        ACTIVITY_VISIBLE = false;
    }

    @Override
    public void onCreate() {
        super.onCreate();

        initDbHelper();
        startPeriodicDataUpdate();
        initImageLoader();
        initChromecast();

        FlurryAgent.init(this, FlurryAnalytics.FLURRY_APP_KEY);

        AsyncTaskCompat.executeParallel(new AsyncTask<Void, Void, Void>() {
            @Override
            protected Void doInBackground(Void... params) {
                // reset time watched every usage session
                final SubscriptionHelper subscriptionHelper = new SubscriptionHelper(getApplicationContext());
                subscriptionHelper.resetTimeWatched();

                int totalWatchedCount = DbHelper.get(getApplicationContext()).getTotalWatchedCount();
                boolean hasSubscription = subscriptionHelper.hasSubscription();
                FlurryAnalytics.getInstance().appOpened(Identity.getId(getApplicationContext()), totalWatchedCount, hasSubscription);
                return null;
            }
        });
    }

    private void initChromecast() {
        // initialize VideoCastManager; from this point on, access this singleton using VideoCastManager.getInstance()
        String googleCastAppId = getString(R.string.google_cast_app_id);

        VideoCastManager.
                initialize(this, googleCastAppId, null, null).
                setVolumeStep(VOLUME_INCREMENT).
                enableFeatures(VideoCastManager.FEATURE_NOTIFICATION |
                        VideoCastManager.FEATURE_LOCKSCREEN |
                        VideoCastManager.FEATURE_WIFI_RECONNECT |
                        VideoCastManager.FEATURE_CAPTIONS_PREFERENCE |
                        VideoCastManager.FEATURE_DEBUGGING |
                        VideoCastManager.FEATURE_AUTO_RECONNECT);
    }

    private void initImageLoader() {
        ImageLoaderConfiguration config = new ImageLoaderConfiguration.Builder(getApplicationContext())
                .defaultDisplayImageOptions(getDisplayImageOptions())
                .build();
        ImageLoader.getInstance().init(config);
    }

    @Override
    public Object getSystemService(String name) {
        if (DbHelper.NAME.equals(name)) {
            return dbHelper;
        } else {
            return super.getSystemService(name);
        }
    }

    public void initDbHelper() {
        dbHelper = new DbHelper(ChildrenTVApp.this);
    }

    private DisplayImageOptions getDisplayImageOptions() {
        return new DisplayImageOptions.Builder()
                .showImageOnLoading(R.drawable.video_background_layer_3_tv)
                .showImageForEmptyUri(R.drawable.video_background_layer_3_tv)
                .cacheInMemory(true)
                .cacheOnDisk(true)
                .showImageOnFail(R.drawable.video_background_layer_3_tv).build();
    }

    public void startPeriodicDataUpdate() {
        Intent intent = new Intent(this, DataLoadReceiver.class);
        intent.setAction("com.cyberwalkabout.youtube.lib.app.UPDATE_DATA");
        PendingIntent pendingIntent = PendingIntent.getBroadcast(this, 0, intent, PendingIntent.FLAG_UPDATE_CURRENT);
        AlarmManager am = (AlarmManager) getSystemService(ALARM_SERVICE);
        am.setRepeating(AlarmManager.ELAPSED_REALTIME, SystemClock.elapsedRealtime() + AlarmManager.INTERVAL_DAY * 2, AlarmManager.INTERVAL_DAY * 2, pendingIntent);
        Log.d(this.getClass().getName(), "Data updater service started at " + new java.sql.Timestamp(System.currentTimeMillis()).toString());
    }

}
