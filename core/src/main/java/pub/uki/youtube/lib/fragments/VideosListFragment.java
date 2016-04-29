package com.cyberwalkabout.youtube.lib.fragments;

import android.app.Activity;
import android.app.ProgressDialog;
import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.content.IntentFilter;
import android.database.Cursor;
import android.net.Uri;
import android.os.AsyncTask;
import android.os.Bundle;
import android.os.Handler;
import android.support.v4.content.LocalBroadcastManager;
import android.support.v4.os.AsyncTaskCompat;
import android.support.v4.view.ViewCompat;
import android.support.v4.view.ViewPropertyAnimatorCompat;
import android.support.v4.widget.SwipeRefreshLayout;
import android.support.v7.media.MediaRouter;
import android.text.Editable;
import android.text.TextUtils;
import android.text.TextWatcher;
import android.util.Log;
import android.util.Pair;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.view.animation.AccelerateInterpolator;
import android.widget.AdapterView;
import android.widget.EditText;
import android.widget.FilterQueryProvider;
import android.widget.ImageButton;
import android.widget.ListView;
import android.widget.TextView;
import android.widget.Toast;

import com.cyberwalkabout.childrentv.shared.model.AgeGroupConst;
import com.cyberwalkabout.youtube.lib.AdaptiveSortHelper;
import com.cyberwalkabout.youtube.lib.AllVideosScreen;
import com.cyberwalkabout.youtube.lib.R;
import com.cyberwalkabout.youtube.lib.adapter.VideosAdapter;
import com.cyberwalkabout.youtube.lib.analytics.FlurryAnalytics;
import com.cyberwalkabout.youtube.lib.data.DataResultReceiver;
import com.cyberwalkabout.youtube.lib.model.LocalVideo;
import com.cyberwalkabout.youtube.lib.util.AppSettings;
import com.cyberwalkabout.youtube.lib.util.ListScrollDistanceCalculator;
import com.cyberwalkabout.youtube.lib.youtube.VideoFileInfo;
import com.cyberwalkabout.youtube.lib.youtube.YouTubeInfoLoader;
import com.cyberwalkabout.youtube.lib.youtube.YoutubeUtils;
import com.google.android.gms.cast.MediaInfo;
import com.google.android.gms.cast.MediaMetadata;
import com.google.android.gms.cast.MediaStatus;
import com.google.android.gms.common.ConnectionResult;
import com.google.android.gms.common.images.WebImage;
import com.google.android.libraries.cast.companionlibrary.cast.BaseCastManager;
import com.google.android.libraries.cast.companionlibrary.cast.VideoCastManager;
import com.google.android.libraries.cast.companionlibrary.cast.callbacks.VideoCastConsumerImpl;
import com.google.android.libraries.cast.companionlibrary.cast.exceptions.NoConnectionException;
import com.google.android.libraries.cast.companionlibrary.cast.exceptions.TransientNetworkDisconnectionException;
import com.google.android.libraries.cast.companionlibrary.widgets.MiniController;

import java.util.ArrayList;
import java.util.List;

/**
 * @author Maria Dzyokh
 * @author Andrii Kovalov
 */
public class VideosListFragment extends AbstractVideosListFragment implements TextWatcher, View.OnClickListener {
    private static final String TAG = VideosListFragment.class.getSimpleName();
    public static final int SEARCH_CONTAINER_ANIMATION_DURATION = 350;

    private VideosAdapter newVideosAdapter;
    private VideosAdapter allVideosAdapter;

    private ViewPropertyAnimatorCompat searchContainerAnimator;

    private AppSettings appSettings;

    private EditText txtSearch;
    private ImageButton btnClearSearch;
    private View searchContainer;

    private View newVideosHeader;
    private View newVideosFooter;

    private SwipeRefreshLayout swipeRefreshLayout;
    private ListView listView;

    private VideoCastManager mCastManager;
    private MiniController mMiniController;

    private ChromecastVideoConsumer chromecastConsumer = new ChromecastVideoConsumer();

    private ArrayList<String> currentPlaylist;
    private int currentPlaylistPosition = -1;

    private boolean hideAnimationInProgress = false;

    private BroadcastReceiver filterChangedReceiver = new BroadcastReceiver() {
        @Override
        public void onReceive(Context context, Intent intent) {
            if (ensureActivity() && isAdded()) {
                populateList(txtSearch.getText().toString().trim());
            }
        }
    };

    private DataResultReceiver resultReceiver = new DataResultReceiver(new Handler(), new DataResultReceiver.Receiver() {
        @Override
        public void onReceiveResult(int resultCode, Bundle resultData) {
            if (ensureActivity()) {
                swipeRefreshLayout.setRefreshing(false);
                populateList(txtSearch.getText().toString().trim());
            }
        }
    });

    private boolean firstCastVideoToPlay = true;

    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        mCastManager = VideoCastManager.getInstance();
        mCastManager.addVideoCastConsumer(chromecastConsumer);
    }

    @Override
    public void onDestroy() {
        if (null != mCastManager) {
            mMiniController.removeOnMiniControllerChangedListener(mCastManager);
            mCastManager.removeMiniController(mMiniController);
            mCastManager.removeVideoCastConsumer(chromecastConsumer);
        }
        super.onDestroy();
    }

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState) {
        return inflater.inflate(R.layout.videos_list, null);
    }

    @Override
    public void onActivityCreated(Bundle savedInstanceState) {
        super.onActivityCreated(savedInstanceState);

        appSettings = new AppSettings(getActivity());

        searchContainer = getView().findViewById(R.id.search_container);
        searchContainer.bringToFront();

        txtSearch = (EditText) getView().findViewById(R.id.txt_search);
        txtSearch.addTextChangedListener(this);
        btnClearSearch = (ImageButton) getView().findViewById(R.id.btn_clear_search);
        btnClearSearch.setOnClickListener(this);

        txtSearch.setVisibility(View.VISIBLE);
        btnClearSearch.setVisibility(View.VISIBLE);

        initList();
        setupMiniController();
    }

    @Override
    public void onResume() {
        super.onResume();
        populateList();
        LocalBroadcastManager.getInstance(getActivity()).registerReceiver(filterChangedReceiver, new IntentFilter(AllVideosScreen.FILTER_CHANGED_ACTION));
        mCastManager.incrementUiCounter();
    }

    @Override
    public void onPause() {
        LocalBroadcastManager.getInstance(getActivity()).unregisterReceiver(filterChangedReceiver);
        mCastManager.decrementUiCounter();
        super.onPause();
    }


    private void setupMiniController() {
        mMiniController = (MiniController) getView().findViewById(R.id.miniController);
        ((TextView) mMiniController.findViewById(R.id.title_view)).setTextColor(getResources().getColor(android.R.color.primary_text_light));
        ((TextView) mMiniController.findViewById(R.id.subtitle_view)).setTextColor(getResources().getColor(android.R.color.primary_text_light));

        mMiniController.bringToFront();
        mCastManager.addMiniController(mMiniController);
    }

    private void initList() {
        // TODO: temporarily disallow users to update content from server manually to reduce datastore read operations on google cloud servers
        /*swipeRefreshLayout = (SwipeRefreshLayout) getView().findViewById(R.id.swipe_to_refresh_layout);
        swipeRefreshLayout.setColorSchemeResources(R.color.refresh_progress_1, R.color.refresh_progress_2, R.color.refresh_progress_3, R.color.refresh_progress_4);
        swipeRefreshLayout.setOnRefreshListener(new SwipeRefreshLayout.OnRefreshListener() {
            @Override
            public void onRefresh() {
                *//*if (ensureActivity() && isAdded()) {
                    Intent refreshDataIntent = new Intent(getActivity(), DataLoadService.class);
                    refreshDataIntent.setAction(DataLoadService.ACTION_LOAD_DATA);
                    refreshDataIntent.putExtra(DataLoadService.KEY_RECEIVER, resultReceiver);
                    getActivity().startService(refreshDataIntent);
                }*//*
            }
        });*/

        listView = (ListView) getView().findViewById(R.id.videos_list);
        listView.setEmptyView(getView().findViewById(android.R.id.empty));
        listView.setOnItemClickListener(new AdapterView.OnItemClickListener() {
            @Override
            public void onItemClick(AdapterView<?> adapterView, View view, final int position, final long id) {
                new AsyncTask<Void, Void, Pair<LocalVideo, ArrayList<String>>>() {
                    @Override
                    protected Pair<LocalVideo, ArrayList<String>> doInBackground(Void... params) {
                        LocalVideo video = dbHelper.getVideoById((int) id);

                        Integer localRating = dbHelper.getVideoLocalRating(video.getYoutubeId());

                        ArrayList<String> playlist = dbHelper.getVideosPlaylist(getSelectedAgeGroups(), getSelectedLanguages(), isFavorite(), getSearchString());

                        dbHelper.adjustLocalRating(video.getYoutubeId(), AdaptiveSortHelper.RATING_DELTA_VIDEO_SELECTED);

                        Log.d(TAG, "Local rating '" + video.getYoutubeId() + "' = " + localRating);

                        return new Pair<>(video, playlist);
                    }

                    @Override
                    protected void onPostExecute(final Pair<LocalVideo, ArrayList<String>> pair) {
                        final LocalVideo video = pair.first;
                        currentPlaylistPosition = position;
                        currentPlaylist = pair.second;

                        if (video != null) {
                            if (mCastManager.isConnected()) {
                                AsyncTaskCompat.executeParallel(new LoadMediaTask(video, false));
                            } else {
                                if (currentPlaylist != null) {
                                    startInternalVideoPlayer(currentPlaylist, position);
                                } else {
                                    Toast.makeText(getActivity(), "YouTube video not found", Toast.LENGTH_SHORT).show();
                                }
                            }
                        } else {
                            Log.e(TAG, "Couldn't find video with id " + id);
                        }
                    }
                }.execute();
            }
        });

        mMiniController = new MiniController(getActivity());
        listView.addFooterView(mMiniController);

        listView.setOnScrollListener(new ListScrollDistanceCalculator(new ListScrollDistanceCalculator.ScrollDistanceListener() {
            @Override
            public void onScrollDistanceChanged(int delta, int total) {
                if (delta < -10) {
                    if (searchContainer.getTranslationY() == 0) {
                        searchContainerAnimator = ViewCompat.animate(searchContainer)
                                .setDuration(SEARCH_CONTAINER_ANIMATION_DURATION)
                                .setInterpolator(new AccelerateInterpolator())
                                .translationYBy(-searchContainer.getHeight());
                        searchContainerAnimator.start();
                    }
                }

                if (delta > 10) {
                    if (searchContainer.getTranslationY() == -searchContainer.getHeight()) {
                        if (searchContainerAnimator != null) {
                            searchContainerAnimator.cancel();

                            searchContainerAnimator = ViewCompat.animate(searchContainer)
                                    .setDuration(SEARCH_CONTAINER_ANIMATION_DURATION)
                                    .setInterpolator(new AccelerateInterpolator())
                                    .translationYBy(searchContainer.getHeight());

                            searchContainerAnimator.start();
                        }
                    }
                }
            }
        }));
    }

    private String getSearchString() {
        return txtSearch.getText().toString().trim();
    }

    private List<String> getSelectedAgeGroups() {
        List<String> selectedAgeGroups = new ArrayList<String>();
        for (int i = 1; i < AgeGroupConst.AGE_GROUPS.length; i++) {
            String ageGroup = AgeGroupConst.AGE_GROUPS[i];
            if (appSettings.isAgeGroupSelected(ageGroup))
                selectedAgeGroups.add(ageGroup);
        }
        return selectedAgeGroups;
    }

    private List<String> getSelectedLanguages() {
        List<String> languages = dbHelper.getLanguages();
        List<String> selectedLanguages = new ArrayList<String>();
        for (String language : languages) {
            if (appSettings.isLanguageSelected(language))
                selectedLanguages.add(language);
        }
        return selectedLanguages;
    }


    protected boolean isFavorite() {
        return false;
    }

    @Override
    public ListView getListView() {
        return listView;
    }

    @Override
    public void beforeTextChanged(CharSequence charSequence, int i, int i2, int i3) {
    }

    @Override
    public void onTextChanged(CharSequence charSequence, int i, int i2, int i3) {
        populateList(charSequence.toString());
    }

    @Override
    public void afterTextChanged(Editable editable) {
    }

    @Override
    public void onClick(View view) {
        if (view.getId() == R.id.btn_clear_search) {
            if (!TextUtils.isEmpty(txtSearch.getText().toString())) {
                txtSearch.setText("");
            }
        }
    }

    @Override
    public void onActivityResult(int requestCode, int resultCode, Intent intent) {
        if (requestCode == PLAYER_ACTIVITY_REQUEST_CODE) {
            populateList();
        } else {
            super.onActivityResult(requestCode, resultCode, intent);
        }
    }

    private void populateList() {
        populateList("");
    }

    private void populateList(final String searchStr) {
        /*MergeAdapter adapter = new MergeAdapter();

        // TODO: do not access database in UI thread
        // TODO: we don't have functionality to display new videos anymore, instead we have adaptive sort order
        if (dbHelper.getAllVideosCount() == dbHelper.getNewVideosCount()) {
            allVideosAdapter = new VideosAdapter(getActivity(), queryAllVideos(searchStr));
            allVideosAdapter.setFilterQueryProvider(new FilterQueryProvider() {
                @Override
                public Cursor runQuery(CharSequence constraint) {
                    return queryAllVideos(constraint.toString());
                }
            });
            adapter.addAdapter(allVideosAdapter);
        } else {
            Cursor newVideos = queryNewVideos(searchStr);

            if (newVideos.moveToFirst() && newVideos.getCount() > 0) {
                if (newVideosHeader == null) {
                    newVideosHeader = LayoutInflater.from(getActivity()).inflate(R.layout.new_videos_header, null);
                }
                adapter.addView(newVideosHeader, false);
                newVideosAdapter = new VideosAdapter(getActivity(), newVideos);
                newVideosAdapter.setFilterQueryProvider(new FilterQueryProvider() {
                    @Override
                    public Cursor runQuery(CharSequence constraint) {
                        return queryNewVideos(constraint.toString());
                    }
                });
                adapter.addAdapter(newVideosAdapter);
                if (newVideosFooter == null) {
                    newVideosFooter = LayoutInflater.from(getActivity()).inflate(R.layout.new_videos_footer, null);
                }
                adapter.addView(newVideosFooter, false);
            }

            allVideosAdapter = new VideosAdapter(getActivity(), queryOldVideos(searchStr));
            allVideosAdapter.setFilterQueryProvider(new FilterQueryProvider() {
                @Override
                public Cursor runQuery(CharSequence constraint) {
                    return queryAllVideos(constraint.toString());
                }
            });
            adapter.addAdapter(allVideosAdapter);
        }*/

        if (isAdded() && getActivity() != null) {
            final Activity activity = getActivity();

            AsyncTaskCompat.executeParallel(new AsyncTask<Void, Void, Cursor>() {
                @Override
                protected Cursor doInBackground(Void... params) {
                    return queryAllVideos(searchStr);
                }

                @Override
                protected void onPostExecute(Cursor cursor) {
                    if (allVideosAdapter == null) {
                        allVideosAdapter = new VideosAdapter(activity, cursor);
                        allVideosAdapter.setFilterQueryProvider(new FilterQueryProvider() {
                            @Override
                            public Cursor runQuery(CharSequence constraint) {
                                return queryAllVideos(constraint.toString());
                            }
                        });

                        getListView().setAdapter(allVideosAdapter);
                    } else {
                        allVideosAdapter.changeCursor(cursor);
                    }
                }
            });
        }
    }

    /*private Cursor queryNewVideos(String searchStr) {
        return dbHelper.queryVideos(getSelectedAgeGroups(), getSelectedLanguages(), isFavorite(), true, searchStr);
    }

    private Cursor queryOldVideos(String searchStr) {
        return dbHelper.queryVideos(getSelectedAgeGroups(), getSelectedLanguages(), isFavorite(), false, searchStr);
    }*/

    private Cursor queryAllVideos(String searchStr) {
        return dbHelper.queryVideos(getSelectedAgeGroups(), getSelectedLanguages(), isFavorite(), null, searchStr);
    }

    private class LoadMediaTask extends AsyncTask<Void, Void, VideoFileInfo> {

        private LocalVideo video;
        private ProgressDialog pd;
        private boolean autoPlay;

        public LoadMediaTask(LocalVideo video, boolean autoPlay) {
            this.video = video;
            this.autoPlay = autoPlay;
        }

        @Override
        protected void onPreExecute() {
            if (firstCastVideoToPlay) {
                firstCastVideoToPlay = false;
                pd = new ProgressDialog(getActivity());
                pd.setTitle("Processing...");
                pd.setMessage("Please wait.");
                pd.setCancelable(false);
                pd.setIndeterminate(true);
                pd.show();
            }
        }

        @Override
        protected VideoFileInfo doInBackground(Void... params) {
            YouTubeInfoLoader loader = new YouTubeInfoLoader();
            return loader.fetchBestQualityMetaData(video.getYoutubeId());
        }

        @Override
        protected void onPostExecute(VideoFileInfo videoFileInfo) {
            if (videoFileInfo != null) {
                MediaMetadata mediaMetadata = new MediaMetadata(MediaMetadata.MEDIA_TYPE_MOVIE);
                mediaMetadata.putString(MediaMetadata.KEY_TITLE, video.getTitle());
                mediaMetadata.putString(MediaMetadata.KEY_SUBTITLE, video.getDescription());
                mediaMetadata.addImage(new WebImage(Uri.parse(YoutubeUtils.getThumbnailUrl(video.getYoutubeId()))));
                mediaMetadata.addImage(new WebImage(Uri.parse(YoutubeUtils.getHighQualityImageUrl(video.getYoutubeId()))));

                MediaInfo mediaInfo = new MediaInfo.Builder(videoFileInfo.getUrl())
                        .setContentType(videoFileInfo.getType())
                        .setStreamType(MediaInfo.STREAM_TYPE_BUFFERED)
                        .setMetadata(mediaMetadata)
                        .build();

                try {
                    mCastManager.loadMedia(mediaInfo, true, 0);
                    FlurryAnalytics.getInstance().videoChromecastStarted(video, autoPlay);
                } catch (TransientNetworkDisconnectionException e) {
                    Log.e(TAG, "Disconnected from 'Chromecast' network");
                } catch (NoConnectionException e) {
                    Log.e(TAG, "No connection to 'Chromecast' network at the moment");
                }
            }

            if (pd != null) {
                pd.dismiss();
            }
        }
    }

    // TODO: it is very likely that it has to be a separate service which takes care about playlist and automatically play next videos
    private class ChromecastVideoConsumer extends VideoCastConsumerImpl {
        private boolean playing;

        @Override
        public void onDisconnected() {
            Log.d(TAG, "onDisconnected()");
        }

        @Override
        public void onDisconnectionReason(@BaseCastManager.DISCONNECT_REASON int reason) {
            Log.d(TAG, "onDisconnectionReason(" + reason + ")");
        }

        @Override
        public void onFailed(int resourceId, int statusCode) {
            Log.d(TAG, "onConnectionSuspended(" + resourceId + ", " + statusCode + ")");
        }

        @Override
        public void onConnectionSuspended(int cause) {
            Log.d(TAG, "onConnectionSuspended(" + cause + ")");
        }

        @Override
        public void onConnectivityRecovered() {
            Log.d(TAG, "onConnectivityRecovered()");
        }

        @Override
        public void onCastDeviceDetected(final MediaRouter.RouteInfo info) {
            Log.d(TAG, "onCastDeviceDetected(" + info + ")");
        }

        @Override
        public void onConnectionFailed(ConnectionResult result) {
            Log.d(TAG, "onConnectionFailed(" + result + ")");
        }

        @Override
        public void onApplicationConnectionFailed(int errorCode) {
            Log.d(TAG, "onApplicationConnectionFailed(" + errorCode + ")");
        }

        @Override
        public void onApplicationDisconnected(int errorCode) {
            Log.d(TAG, "onApplicationDisconnected(" + errorCode + ")");
        }

        @Override
        public void onApplicationStopFailed(int errorCode) {
            Log.d(TAG, "onApplicationStopFailed(" + errorCode + ")");
        }

        @Override
        public void onCastAvailabilityChanged(boolean castPresent) {
            Log.d(TAG, "onCastAvailabilityChanged(" + castPresent + ")");
        }

        @Override
        public void onRemoteMediaPlayerStatusUpdated() {
            int playbackStatus = mCastManager.getPlaybackStatus();

            Log.d(TAG, "onRemoteMediaPlayerStatusUpdated() Playback status: " + playbackStatus);

            if (!playing && playbackStatus == MediaStatus.PLAYER_STATE_PLAYING) {
                playing = true;
            }

            if (playing && playbackStatus == MediaStatus.PLAYER_STATE_IDLE && mCastManager.getIdleReason() == MediaStatus.IDLE_REASON_FINISHED) {
                // finished playing previous cartoon so auto-start next one if possible
                if (currentPlaylist != null && currentPlaylistPosition >= 0) {
                    try {
                        final String nextYoutubeVideoId = currentPlaylist.get(++currentPlaylistPosition);
                        new AsyncTask<Void, Void, LocalVideo>() {
                            @Override
                            protected LocalVideo doInBackground(Void... params) {
                                return dbHelper.getVideoByYoutubeId(nextYoutubeVideoId);
                            }

                            @Override
                            protected void onPostExecute(LocalVideo video) {
                                new LoadMediaTask(video, true).execute();
                            }
                        }.execute();
                    } catch (IndexOutOfBoundsException ignore) {
                    }
                }
            }
        }
    }

}
