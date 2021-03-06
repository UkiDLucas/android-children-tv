package com.cyberwalkabout.childrentv;

import android.app.AlarmManager;
import android.app.Application;
import android.app.PendingIntent;
import android.content.Intent;
import android.os.SystemClock;
import android.util.Log;

import com.chicagoandroid.childrentv.R;
import com.cyberwalkabout.childrentv.analytics.FlurryAnalytics;
import com.cyberwalkabout.childrentv.data.DataLoadReceiver;
import com.cyberwalkabout.childrentv.data.db.DbHelper;
import com.flurry.android.FlurryAgent;
import com.nostra13.universalimageloader.core.DisplayImageOptions;
import com.nostra13.universalimageloader.core.ImageLoader;
import com.nostra13.universalimageloader.core.ImageLoaderConfiguration;


/**
 * @author Maria Dzyokh
 *         Andrii Kovalov
 *         UkiDLucas
 */
public class ChildrenTVApp extends Application {//MultiDexApplication {
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

//        AsyncTaskCompat.executeParallel(new AsyncTask<Void, Void, Void>() {
//            @Override
//            protected Void doInBackground(Void... params) {
//                // reset time watched every usage session
//                final SubscriptionHelper subscriptionHelper = new SubscriptionHelper(getApplicationContext());
//                subscriptionHelper.resetTimeWatched();
//
//                int totalWatchedCount = DbHelper.get(getApplicationContext()).getTotalWatchedCount();
//                boolean hasSubscription = subscriptionHelper.hasSubscription();
//                FlurryAnalytics.getInstance().appOpened(Identity.getId(getApplicationContext()), totalWatchedCount, hasSubscription);
//                return null;
//            }
//        });
    }

    private void initChromecast() {
        // initialize VideoCastManager; from this point on, access this singleton using VideoCastManager.getInstance()
        String googleCastAppId = getString(R.string.google_cast_app_id);
// TODO Uki: research this
//        VideoCastManager.
//                initialize(this, googleCastAppId, null, null).
//                setVolumeStep(VOLUME_INCREMENT).
//                enableFeatures(VideoCastManager.FEATURE_NOTIFICATION |
//                        VideoCastManager.FEATURE_LOCKSCREEN |
//                        VideoCastManager.FEATURE_WIFI_RECONNECT |
//                        VideoCastManager.FEATURE_CAPTIONS_PREFERENCE |
//                        VideoCastManager.FEATURE_DEBUGGING |
//                        VideoCastManager.FEATURE_AUTO_RECONNECT);
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
