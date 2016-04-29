package com.cyberwalkabout.youtube.lib.data;

import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import com.cyberwalkabout.youtube.lib.app.ChildrenTVApp;

/**
 * @author Maria Dzyokh
 */
public class BootCompletedReceiver extends BroadcastReceiver {

    @Override
    public void onReceive(Context context, Intent intent) {
        // TODO: consider to use SyncAdapter
        ((ChildrenTVApp) context.getApplicationContext()).startPeriodicDataUpdate();
    }
}
