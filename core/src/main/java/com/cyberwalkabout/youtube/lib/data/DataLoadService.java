package com.cyberwalkabout.youtube.lib.data;

import android.app.IntentService;
import android.app.NotificationManager;
import android.app.PendingIntent;
import android.content.Context;
import android.content.Intent;
import android.graphics.BitmapFactory;
import android.os.ResultReceiver;
import android.support.v4.app.NotificationCompat;
import android.text.TextUtils;

import com.cyberwalkabout.youtube.lib.data.loader.GoogleCloudEndpointVideoLoader;
import com.cyberwalkabout.youtube.lib.data.loader.VideoDataLoader;
import com.cyberwalkabout.youtube.lib.util.AppSettings;
import com.cyberwalkabout.youtube.lib.AllVideosScreen;
import com.cyberwalkabout.youtube.lib.R;
import com.cyberwalkabout.youtube.lib.data.loader.LoadResult;

/**
 * @author Maria Dzyokh
 * @author Andrii Kovalov
 *         <p/>
 *         // TODO: consider to implement SyncAdapter
 */
public class DataLoadService extends IntentService {

    public static final String ACTION_LOAD_DATA = "com.cyberwalkabout.youtube.lib.data.ACTION_LOAD_DATA";
    // cyberfit ???
    public static final String KEY_RECEIVER = "com.cyberwalkabout.cyberfit.RunningService.KEY_RECEIVER";
    public static final String KEY_SHOW_NOTIFICATION = "com.cyberwalkabout.cyberfit.RunningService.KEY_SHOW_NOTIFICATION";

    public static final int RESULT_DONE = 0;
    public static final int RESULT_ERROR = 1;

    public static final int NEW_EVENTS_NOTIFICATION_ID = 909;

    public enum SyncStatus {
        OK, FAILED
    }

    public DataLoadService() {
        super(DataLoadService.class.getName());
    }

    @Override
    protected void onHandleIntent(Intent intent) {
        if (intent != null && !TextUtils.isEmpty(intent.getAction()) && intent.getAction().equals(ACTION_LOAD_DATA)) {
            ResultReceiver resultReceiver = intent.getParcelableExtra(KEY_RECEIVER);
            boolean showNotification = intent.hasExtra(KEY_SHOW_NOTIFICATION) && intent.getBooleanExtra(KEY_SHOW_NOTIFICATION, false);

            VideoDataLoader videoDataLoader = new GoogleCloudEndpointVideoLoader(this);

            LoadResult result = null;
            for (int i = 0; i < 3; i++) {
                result = videoDataLoader.loadVideos();
                if (result.isSuccess()) {
                    break;
                }
            }

            AppSettings appSettings = new AppSettings(this);
            appSettings.setLastSyncStatus(result.isSuccess() ? SyncStatus.OK.name() : SyncStatus.FAILED.name(), System.currentTimeMillis());

            if (resultReceiver != null) {
                resultReceiver.send(result.isSuccess() ? RESULT_DONE : RESULT_ERROR, null);
            }

            if (showNotification && result.getNewRecordsCount() > 0) {
                showNotification(result.getNewRecordsCount());
            }
        }
    }

    private void showNotification(int newRecords) {
        NotificationCompat.Builder mBuilder =
                new NotificationCompat.Builder(DataLoadService.this)
                        .setSmallIcon(R.drawable.ic_action_video)
                        .setLargeIcon(BitmapFactory.decodeResource(getResources(), R.drawable.ic_action_video))
                        // TODO: move strings to xml
                        .setContentTitle(getString(R.string.app_name) + ": new videos")
                        .setContentText(newRecords + " new videos has been reviewed.")
                        .setAutoCancel(true);

        Intent intent = new Intent(DataLoadService.this, AllVideosScreen.class);
        PendingIntent resultPendingIntent = PendingIntent.getActivity(this, 0, intent, PendingIntent.FLAG_UPDATE_CURRENT);
        mBuilder.setContentIntent(resultPendingIntent);
        NotificationManager mNotificationManager =
                (NotificationManager) getSystemService(Context.NOTIFICATION_SERVICE);
        mNotificationManager.notify(NEW_EVENTS_NOTIFICATION_ID, mBuilder.getNotification());
    }
}
