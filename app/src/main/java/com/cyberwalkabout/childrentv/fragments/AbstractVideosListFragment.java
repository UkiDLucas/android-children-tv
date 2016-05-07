package com.cyberwalkabout.childrentv.fragments;

import android.content.Intent;
import android.os.Bundle;
import android.support.v4.app.Fragment;
import android.view.View;
import android.widget.ListView;

import com.cyberwalkabout.childrentv.activities.PlayerViewActivity;
import com.cyberwalkabout.childrentv.data.db.DbHelper;

import java.util.ArrayList;

/**
 * @author Maria Dzyokh, Andrii Kovalov
 */
public abstract class AbstractVideosListFragment extends Fragment {

    public static final int PLAYER_ACTIVITY_REQUEST_CODE = 3;

    protected static final String LIST_INDEX = "list_index";
    protected static final String LIST_TOP = "list_top";

    protected DbHelper dbHelper;


    public abstract ListView getListView();

    @Override
    public void onActivityCreated(Bundle savedInstanceState) {
        super.onActivityCreated(savedInstanceState);
        dbHelper = DbHelper.get(getActivity());
    }

    @Override
    public void onSaveInstanceState(Bundle outState) {
        outState.putInt(LIST_INDEX, getListView().getFirstVisiblePosition());
        View v = getListView().getChildAt(0);
        int top = (v == null) ? 0 : v.getTop();
        outState.putInt(LIST_TOP, top);
        super.onSaveInstanceState(outState);
    }

    protected void resetListViewScrollState(Bundle savedInstanceState) {
        if (savedInstanceState != null && savedInstanceState.containsKey(LIST_INDEX)) {
            getListView().setSelectionFromTop(savedInstanceState.getInt(LIST_INDEX), savedInstanceState.getInt(LIST_TOP));
        }
    }

    protected void startInternalVideoPlayer(ArrayList<String> playlist, int startIndex) {
        Intent i = new Intent(getActivity(), PlayerViewActivity.class);
        i.putExtra("youtubeIds", playlist);
        i.putExtra("current", startIndex);
        startActivityForResult(i, PLAYER_ACTIVITY_REQUEST_CODE);
    }

    /**
     * Ensure that getActivity() method doesn't return null
     *
     * @return
     */
    protected boolean ensureActivity() {
        return getActivity() != null;
    }
}
