package com.cyberwalkabout.youtube.lib.fragments;

import android.database.Cursor;
import android.os.AsyncTask;
import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.AdapterView;
import android.widget.ListView;
import android.widget.Toast;

import com.cyberwalkabout.youtube.lib.AbstractVideosActivity;
import com.cyberwalkabout.youtube.lib.R;
import com.cyberwalkabout.youtube.lib.adapter.SeriesAdapter;
import com.cyberwalkabout.youtube.lib.analytics.FlurryAnalytics;
import com.cyberwalkabout.youtube.lib.model.LocalVideo;
import com.jeremyfeinstein.slidingmenu.lib.SlidingMenu;

/**
 * @author Maria Dzyokh
 */
public class SeriesFragment extends AbstractVideosListFragment {

    private int seriesId;
    private Bundle savedInstanceState;

    public static SeriesFragment newInstance(int seriesId) {
        SeriesFragment fragment = new SeriesFragment();
        Bundle args = new Bundle();
        args.putInt("series_id", seriesId);
        fragment.setArguments(args);
        return fragment;
    }

    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        this.savedInstanceState = savedInstanceState;
        Bundle args = savedInstanceState != null ? savedInstanceState : getArguments();
        if (args != null) {
            seriesId = getArguments().getInt("series_id");
        }
    }

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState) {
        return inflater.inflate(R.layout.series_list, null);
    }

    @Override
    public void onActivityCreated(Bundle savedInstanceState) {
        super.onActivityCreated(savedInstanceState);
        ((AbstractVideosActivity) getActivity()).getSlidingMenu().setTouchModeAbove(SlidingMenu.TOUCHMODE_NONE);
        initList();
    }

    private void initList() {
        new AsyncTask<Void, Void, Cursor>() {

            @Override
            protected Cursor doInBackground(Void... params) {
                return dbHelper.getSeriesCursor(seriesId);
            }

            @Override
            protected void onPostExecute(Cursor cursor) {
                getListView().setAdapter(new SeriesAdapter(getActivity(), cursor));
                getListView().setOnItemClickListener(new AdapterView.OnItemClickListener() {

                    @Override
                    public void onItemClick(AdapterView<?> parent, View view, int position, long id) {
                        LocalVideo video = dbHelper.getVideoById((int) id);
                        if (video != null) {
                            FlurryAnalytics.getInstance().videoLocalStarted(video, position, false);
                            startInternalVideoPlayer(dbHelper.getSeriesPlaylist(video.getSeriesId()), position);
                        } else {
                            Toast.makeText(getActivity(), "YouTube video not found", Toast.LENGTH_SHORT).show();
                        }
                    }
                });
                resetListViewScrollState(savedInstanceState);
            }
        }.execute();

    }

    @Override
    public ListView getListView() {
        return (ListView) getView().findViewById(R.id.series_list);
    }
}
