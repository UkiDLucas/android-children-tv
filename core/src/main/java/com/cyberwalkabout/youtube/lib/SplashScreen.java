package com.cyberwalkabout.youtube.lib;

import android.content.Intent;
import android.os.AsyncTask;
import android.os.Bundle;
import android.os.Handler;
import android.support.v4.os.AsyncTaskCompat;
import android.view.View;
import android.view.WindowManager;

import com.bugsense.trace.BugSenseHandler;
import com.cyberwalkabout.youtube.lib.data.DataResultReceiver;
import com.cyberwalkabout.youtube.lib.data.db.DbHelper;
import com.cyberwalkabout.youtube.lib.util.AppSettings;

import com.cyberwalkabout.youtube.lib.data.DataLoadService;

public class SplashScreen extends AbstractActivity {

    private static final long SPLASH_TIME = 3000;

    private View progressBar;
    private AppSettings appSettings;

    private DataResultReceiver resultReceiver = new DataResultReceiver(new Handler(), new DataResultReceiver.Receiver() {
        @Override
        public void onReceiveResult(int resultCode, Bundle resultData) {
            progressBar.setVisibility(View.GONE);
            if (appSettings.isFirstLaunch()) {
                appSettings.setFirstLaunch(false);
                appSettings.setFirstLaunchTimestamp(System.currentTimeMillis());
                startActivity(new Intent(SplashScreen.this, TutorialScreen.class).putExtra(TutorialScreen.EXTRA_CLASS_NAME, SplashScreen.class.getSimpleName()));
            } else {
                startActivity(new Intent(SplashScreen.this, AllVideosScreen.class));
            }
            SplashScreen.this.finish();
        }
    });

    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        BugSenseHandler.initAndStartSession(this, getString(R.string.bugsense_key));
        setContentView(com.cyberwalkabout.youtube.lib.R.layout.splash_screen);
        makeFullscreen();

        progressBar = findViewById(com.cyberwalkabout.youtube.lib.R.id.progress_bar);
        appSettings = new AppSettings(this);

        tryToSyncData();
    }

    private void tryToSyncData() {
        AsyncTaskCompat.executeParallel(new AsyncTask<Void, Void, Boolean>() {
            @Override
            protected Boolean doInBackground(Void... params) {
                boolean syncRequired = appSettings.needToSyncVideos();

                if (!syncRequired) {
                    DbHelper dbHelper = DbHelper.get(getApplicationContext());
                    syncRequired = dbHelper.getAllVideosCount() == 0;
                }

                return syncRequired;
            }

            @Override
            protected void onPostExecute(Boolean syncRequired) {
                if (syncRequired) {
                    progressBar.setVisibility(View.VISIBLE);
                    Intent intent = new Intent(SplashScreen.this, DataLoadService.class);
                    intent.setAction(DataLoadService.ACTION_LOAD_DATA);
                    intent.putExtra(DataLoadService.KEY_RECEIVER, resultReceiver);
                    startService(intent);
                } else {
                    new Handler().postDelayed(new Runnable() {
                        @Override
                        public void run() {
                            startActivity(new Intent(SplashScreen.this, AllVideosScreen.class));
                            SplashScreen.this.finish();
                        }
                    }, SPLASH_TIME);
                }
            }
        });
    }

    @Override
    protected void onDestroy() {
        super.onDestroy();
        resultReceiver.setReceiver(null);
        BugSenseHandler.closeSession(this);
    }

    private void makeFullscreen() {
        WindowManager.LayoutParams attrs = getWindow().getAttributes();
        attrs.flags |= WindowManager.LayoutParams.FLAG_LAYOUT_IN_SCREEN | WindowManager.LayoutParams.FLAG_LAYOUT_NO_LIMITS | WindowManager.LayoutParams.FLAG_FULLSCREEN;
        getWindow().setAttributes(attrs);
    }
}

