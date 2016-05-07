package com.cyberwalkabout.childrentv.activities;

import android.os.AsyncTask;
import android.os.Build;
import android.os.Bundle;
import android.os.Handler;
import android.support.v4.os.AsyncTaskCompat;
import android.support.v7.app.AppCompatActivity;
import android.util.Log;
import android.view.View;
import android.view.ViewGroup;
import android.view.WindowManager;
import android.widget.TextView;
import android.widget.Toast;

import com.bugsense.trace.BugSenseHandler;
import com.chicagoandroid.childrentv.R;
import com.cyberwalkabout.childrentv.ChildrenTVApp;
import com.cyberwalkabout.childrentv.analytics.FlurryAnalytics;
import com.cyberwalkabout.childrentv.data.db.DbHelper;
import com.cyberwalkabout.childrentv.model.LocalVideo;
import com.flurry.android.FlurryAgent;
import com.google.android.libraries.cast.companionlibrary.cast.VideoCastManager;
import com.google.android.youtube.player.YouTubeInitializationResult;
import com.google.android.youtube.player.YouTubePlayer;
import com.google.android.youtube.player.YouTubePlayerSupportFragment;

import java.util.ArrayList;

public class PlayerViewActivity extends AppCompatActivity implements YouTubePlayer.OnInitializedListener {
    private static final String TAG = PlayerViewActivity.class.getSimpleName();

    private YouTubePlayer player;

    private ArrayList<String> playlist;
    private int currentIndex = 0;

    private DbHelper dbHelper;


    private boolean videoStarted = false;
    private boolean autoplay = false;

    private enum PlaybackStatus {
        BUFFERING, PLAYING, STOPPED, PAUSED, SEEK_TO, UNINITIALIZED;
    }

    private PlaybackStatus currentStatus = PlaybackStatus.BUFFERING;

    private Handler customHandler = new Handler();

    private long timeWatchedCurrentVideo;

    private Runnable updateWatchedTimeTask = new Runnable() {
        public void run() {
            int second = 1000;
            if (videoStarted && currentStatus == PlaybackStatus.PLAYING) {
                timeWatchedCurrentVideo += second;

            }
            customHandler.postDelayed(this, second);
        }
    };

    private VideoCastManager mCastManager;

    private String currentYoutubeId;

    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

        setContentView(R.layout.playerview);

        mCastManager = VideoCastManager.getInstance();

        BugSenseHandler.initAndStartSession(this, getString(R.string.bugsense_key));

        makeFullscreen();


        dbHelper = DbHelper.get(PlayerViewActivity.this);

        YouTubePlayerSupportFragment youtubeFragment = getYoutubeFragment();
        youtubeFragment.initialize(getString(R.string.google_api_key), this);

        getDataFromIntent();
    }

    private void hideNavigationBar() {
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.JELLY_BEAN) {
            View decorView = getWindow().getDecorView();
            // Hide both the navigation bar and the status bar.
            // SYSTEM_UI_FLAG_FULLSCREEN is only available on Android 4.1 and higher, but as
            // a general rule, you should design your app to hide the status bar whenever you
            // hide the navigation bar.
            int uiOptions = View.SYSTEM_UI_FLAG_HIDE_NAVIGATION | View.SYSTEM_UI_FLAG_FULLSCREEN;
            decorView.setSystemUiVisibility(uiOptions);
        }
    }

    private void makeFullscreen() {
        WindowManager.LayoutParams attrs = getWindow().getAttributes();
        attrs.flags |= WindowManager.LayoutParams.FLAG_LAYOUT_IN_SCREEN | WindowManager.LayoutParams.FLAG_LAYOUT_NO_LIMITS | WindowManager.LayoutParams.FLAG_FULLSCREEN;
        getWindow().setAttributes(attrs);
    }

    @Override
    public void onStart() {
        super.onStart();
        FlurryAgent.setReportLocation(false);
        FlurryAgent.onStartSession(this, FlurryAnalytics.FLURRY_APP_KEY);
    }

    @Override
    public void onStop() {
        super.onStop();
        FlurryAgent.onEndSession(this);
    }

    @Override
    protected void onResume() {
        super.onResume();

        ChildrenTVApp.activityResumed();
        setFullscreen();
        customHandler.postDelayed(updateWatchedTimeTask, 0);
        mCastManager.incrementUiCounter();
    }

    @Override
    protected void onPause() {
        ChildrenTVApp.activityPaused();
        customHandler.removeCallbacks(updateWatchedTimeTask);
        mCastManager.decrementUiCounter();

        super.onPause();
    }

    @Override
    protected void onDestroy() {
        super.onDestroy();
        BugSenseHandler.closeSession(this);
    }

    @Override
    protected void onUserLeaveHint() {
        super.onUserLeaveHint();
        Log.d(TAG, "User leaving current activity. It might be 'home button' press or simply transition to another activity.");

    }

    private void getDataFromIntent() {
        playlist = (ArrayList<String>) getIntent().getSerializableExtra("youtubeIds");
        currentIndex = getIntent().getIntExtra("current", 0);
    }

    @Override
    public void onInitializationFailure(YouTubePlayer.Provider provider,
                                        YouTubeInitializationResult errorReason) {
        if (errorReason.isUserRecoverableError()) {
            getYoutubeFragment().initialize(getString(R.string.google_api_key), this);
        } else {
            String errorMessage = String.format(getString(R.string.error_player), errorReason.toString());
            Toast.makeText(this, errorMessage, Toast.LENGTH_LONG).show();
        }
    }

    @Override
    public void onInitializationSuccess(YouTubePlayer.Provider provider, final YouTubePlayer player,
                                        boolean wasRestored) {

        ViewGroup apiMobileControllerOverlay = (ViewGroup) findView((ViewGroup) getYoutubeFragment().getView(), "YouTubePlayerView");
        System.out.println(apiMobileControllerOverlay);

        if (!wasRestored) {
            setFullscreen();
            this.player = player;
            this.player.setShowFullscreenButton(false);
            this.player.setPlaylistEventListener(new YouTubePlayer.PlaylistEventListener() {
                @Override
                public void onPrevious() {
                    Log.d(TAG, "onPrevious");
                    videoStarted = true;
                }

                @Override
                public void onNext() {
                    videoStarted = true;
                    Log.d(TAG, "onNext: " + player.getCurrentTimeMillis() + ", " + player.getDurationMillis());
                    // if less then half of the video watched then it is considered as video skipping
                    if (timeWatchedCurrentVideo < player.getDurationMillis() / 2) {
                        Log.d(TAG, "Skip video: " + currentYoutubeId);

                        AsyncTaskCompat.executeParallel(new AsyncTask<Void, Void, Void>() {
                            @Override
                            protected Void doInBackground(Void... params) {
                                dbHelper.adjustLocalRating(currentYoutubeId, AdaptiveSortHelper.RATING_DELTA_SKIP_VIDEO);
                                FlurryAnalytics.getInstance().skipYoutubeVideo(dbHelper.getVideoByYoutubeId(currentYoutubeId));
                                return null;
                            }
                        });
                    } else if (timeWatchedCurrentVideo > player.getDurationMillis() / 2) {
                        AsyncTaskCompat.executeParallel(new AsyncTask<Void, Void, Void>() {
                            @Override
                            protected Void doInBackground(Void... params) {
                                dbHelper.adjustLocalRating(currentYoutubeId, AdaptiveSortHelper.RATING_DELTA_VIDEO_FULLY_WATCHED);
                                return null;
                            }
                        });
                    }
                }

                @Override
                public void onPlaylistEnded() {
                    Log.d(TAG, "onPlaylistEnded");
                }
            });
            this.player.setPlayerStateChangeListener(new YouTubePlayer.PlayerStateChangeListener() {
                @Override
                public void onLoading() {
                    setFullscreen();
                }

                @Override
                public void onLoaded(final String youtubeId) {
                    timeWatchedCurrentVideo = 0;
                    currentYoutubeId = youtubeId;
                    setFullscreen();
                    new AsyncTask<Void, Void, Void>() {
                        @Override
                        protected Void doInBackground(Void... params) {
                            dbHelper.incrementWatchedCount(youtubeId);
                            LocalVideo localVideo = dbHelper.getVideoByYoutubeId(youtubeId);
                            FlurryAnalytics.getInstance().videoLocalStarted(localVideo, currentIndex, autoplay);
                            return null;
                        }
                    }.execute();
                }

                @Override
                public void onAdStarted() {
                    setFullscreen();
                }

                @Override
                public void onVideoStarted() {
                    setFullscreen();
                    videoStarted = true;
                }

                @Override
                public void onVideoEnded() {
                    Log.d(TAG, "onVideoEnded");

                    if (timeWatchedCurrentVideo > player.getDurationMillis() / 2) {
                        AsyncTaskCompat.executeParallel(new AsyncTask<Void, Void, Void>() {
                            @Override
                            protected Void doInBackground(Void... params) {
                                dbHelper.adjustLocalRating(currentYoutubeId, AdaptiveSortHelper.RATING_DELTA_VIDEO_FULLY_WATCHED);
                                return null;
                            }
                        });
                    }

                    videoStarted = false;
                    autoplay = true;
                    if (player.hasNext()) {
                        currentIndex++;
                        player.next();
                    } else {
                        currentIndex = 0;
                        player.loadVideos(playlist, currentIndex, 0);
                    }
                }

                @Override
                public void onError(final YouTubePlayer.ErrorReason errorReason) {
                    Log.d(TAG, "onError");
                    if (errorReason == YouTubePlayer.ErrorReason.UNEXPECTED_SERVICE_DISCONNECTION) {
                        // When this error occurs the player is released and can no longer be used.
                        PlayerViewActivity.this.player = null;
                        currentStatus = PlaybackStatus.UNINITIALIZED;
                    }
                    setFullscreen();
                    videoStarted = false;

                    new AsyncTask<Void, Void, Void>() {
                        @Override
                        protected Void doInBackground(Void... params) {
                            String youtubeId = playlist.get(currentIndex);
                            LocalVideo video = dbHelper.getVideoByYoutubeId(youtubeId);

                            if (errorReason == YouTubePlayer.ErrorReason.NOT_PLAYABLE) {
                                dbHelper.setVideoPlayable(youtubeId, false);
                            }

                            FlurryAnalytics.getInstance().videoPlaybackError(video, getErrorMessage(errorReason));
                            return null;
                        }
                    }.execute();
                }
            });
            this.player.setPlaybackEventListener(new YouTubePlayer.PlaybackEventListener() {

                @Override
                public void onPlaying() {
                    currentStatus = PlaybackStatus.PLAYING;
                }

                @Override
                public void onPaused() {
                    currentStatus = PlaybackStatus.PAUSED;
                }

                @Override
                public void onStopped() {
                    currentStatus = PlaybackStatus.STOPPED;
                }

                @Override
                public void onBuffering(boolean b) {
                    currentStatus = PlaybackStatus.BUFFERING;
                }

                @Override
                public void onSeekTo(int millisAfterSeek) {
                    currentStatus = PlaybackStatus.SEEK_TO;
                }
            });
            this.player.loadVideos(playlist, currentIndex, 0);
        }
    }

    private YouTubePlayerSupportFragment getYoutubeFragment() {
        return (YouTubePlayerSupportFragment) getSupportFragmentManager().findFragmentById(R.id.youtube_fragment);
    }

    private void setFullscreen() {
        WindowManager.LayoutParams attrs = getWindow().getAttributes();
        attrs.flags |= WindowManager.LayoutParams.FLAG_FULLSCREEN;
        getWindow().setAttributes(attrs);
        hideNavigationBar();
    }

    private String getErrorMessage(YouTubePlayer.ErrorReason errorReason) {
        StringBuilder message = new StringBuilder(errorReason.name());
        try {
            ViewGroup playerOverlaysLayout = (ViewGroup) findView((ViewGroup) getWindow().getDecorView(), "PlayerOverlaysLayout");
            ViewGroup frameLayout = (ViewGroup) findView(playerOverlaysLayout, "FrameLayout");
            ViewGroup apiMobileControllerOverlay = (ViewGroup) findView(frameLayout, "ApiMobileControllerOverlay");
            TextView textView = (TextView) findView(apiMobileControllerOverlay, "TextView");
            if (textView != null) {
                message.append(" ").append(textView.getText().toString());
            }
        } catch (Exception e) {
            message.append(" ").append("Unable to retrieve error message from youtube player");
        }
        return message.toString();
    }

    private View findView(ViewGroup root, String className) {
        if (root != null) {
            for (int i = 0; i < root.getChildCount(); i++) {
                View view = root.getChildAt(i);
                if (view.getClass().getSimpleName().equals(className)) {
                    return view;
                }

                if (view instanceof ViewGroup) {
                    return findView((ViewGroup) view, className);
                }
            }
        }
        return null;
    }

}
