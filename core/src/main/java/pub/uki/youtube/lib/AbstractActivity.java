package com.cyberwalkabout.youtube.lib;

import android.os.Bundle;
import android.support.v7.app.AppCompatActivity;

import com.bugsense.trace.BugSenseHandler;
import com.cyberwalkabout.youtube.lib.analytics.FlurryAnalytics;
import com.cyberwalkabout.youtube.lib.app.ChildrenTVApp;
import com.flurry.android.FlurryAgent;

/**
 * @author Maria Dzyokh
 * @author Andrii Kovalov
 */
public class AbstractActivity extends AppCompatActivity {

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        BugSenseHandler.initAndStartSession(this, getString(R.string.bugsense_key));
    }

    @Override
    protected void onDestroy() {
        super.onDestroy();
        BugSenseHandler.closeSession(this);
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
    }

    @Override
    protected void onPause() {
        super.onPause();
        ChildrenTVApp.activityPaused();
    }

    protected ChildrenTVApp getApp() {
        return (ChildrenTVApp) getApplication();
    }
}
