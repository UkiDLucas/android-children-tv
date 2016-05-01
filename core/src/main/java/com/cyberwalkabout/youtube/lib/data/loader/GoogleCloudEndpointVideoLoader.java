package com.cyberwalkabout.youtube.lib.data.loader;

import android.content.ContentValues;
import android.content.Context;
import android.database.sqlite.SQLiteDatabase;
import android.util.Log;

import com.cyberwalkabout.childrentv.backend.model.VideoEntity;
import com.cyberwalkabout.childrentv.backend.videoApi.VideoApi;
import com.cyberwalkabout.childrentv.backend.videoApi.model.VideoEntityCollection;
import com.cyberwalkabout.youtube.lib.data.db.DbHelper;
import com.cyberwalkabout.youtube.lib.model.LocalVideo;
import com.google.api.client.extensions.android.http.AndroidHttp;
import com.google.api.client.extensions.android.json.AndroidJsonFactory;
import com.google.api.client.googleapis.services.AbstractGoogleClientRequest;
import com.google.api.client.googleapis.services.GoogleClientRequestInitializer;

import java.io.IOException;

/**
 * @author Andrii Kovalov
 */
public class GoogleCloudEndpointVideoLoader implements VideoDataLoader {
    private static final String TAG = GoogleCloudEndpointVideoLoader.class.getSimpleName();
    private static final boolean DEBUG = false;

    private static final String DEBUG_API_URL = "http://192.168.0.100:8080/_ah/api/";
    private static final String EMULATOR_API_URL = "http://10.0.2.2:8080/_ah/api/";

    private VideoApi videoApi;
    private DbHelper dbHelper;

    public GoogleCloudEndpointVideoLoader(Context context) {
        dbHelper = DbHelper.get(context);

        VideoApi.Builder builder = new VideoApi.Builder(AndroidHttp.newCompatibleTransport(), new AndroidJsonFactory(), null);

        if (DEBUG) {
            builder.setRootUrl(EMULATOR_API_URL)
                    .setGoogleClientRequestInitializer(new GoogleClientRequestInitializer() {
                        @Override
                        public void initialize(AbstractGoogleClientRequest<?> abstractGoogleClientRequest)
                                throws IOException {
                            abstractGoogleClientRequest.setDisableGZipContent(true);
                        }
                    });
        }

        videoApi = builder.build();
    }

    @Override
    public LoadResult loadVideos() {
        try {
            VideoEntityCollection videosCollection = videoApi.getVideos().execute();

            int newRecordsCount = 0;

            if (videosCollection != null && !videosCollection.isEmpty() && videosCollection.getItems() != null) {
                Log.d(TAG, "Received " + videosCollection.size() + " videos");

                long modifyTimestamp = System.currentTimeMillis();

                // TODO: consider to use ContentProvider instead
                SQLiteDatabase db = dbHelper.getWritableDatabase();

                try {
                    db.beginTransaction();

                    for (com.cyberwalkabout.childrentv.backend.videoApi.model.VideoEntity video : videosCollection.getItems()) {
                        LocalVideo localVideo = new LocalVideo();
                        localVideo.setTitle(video.getTitle());
                        localVideo.setDescription(video.getDescription());
                        localVideo.setDuration(video.getDuration());
                        localVideo.setLanguage(video.getLanguage());
                        localVideo.setRating(video.getRating());
                        localVideo.setYoutubeId(video.getYoutubeId());
                        localVideo.setYoutubeUrl(video.getYoutubeUrl());
                        localVideo.setAgeGroup(video.getAgeGroup());
                        localVideo.setSeriesId(video.getSeriesId());
                        localVideo.setNew(false);
                        localVideo.setModified(modifyTimestamp);
                        localVideo.setIsPlayable(true);

                        ContentValues contentValues = localVideo.toContentValues();
                        if (db.update(LocalVideo.TABLE_NAME, contentValues, LocalVideo.YOUTUBE_ID + "=?", new String[]{video.getYoutubeId()}) == 0) {
                            localVideo.setNew(true);
                            db.insert(LocalVideo.TABLE_NAME, null, contentValues);
                            newRecordsCount++;
                        }
                    }

                    // TODO: why do we consider number of deleted rows as number of new records?
                    newRecordsCount += db.delete(LocalVideo.TABLE_NAME, LocalVideo.MODIFIED + " !=?", new String[]{String.valueOf(modifyTimestamp)});

                    db.setTransactionSuccessful();
                } finally {
                    db.endTransaction();
                }
            }

            return new LoadResult(true, newRecordsCount);
        } catch (IOException e) {
            Log.e(TAG, e.getMessage(), e);
            return new LoadResult(false, 0, e.getLocalizedMessage());
        }
    }
}
