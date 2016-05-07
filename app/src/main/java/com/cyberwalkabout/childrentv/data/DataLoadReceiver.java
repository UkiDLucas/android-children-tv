package com.cyberwalkabout.childrentv.data;

import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;

public class DataLoadReceiver extends BroadcastReceiver {

    @Override
    public void onReceive(final Context context, Intent intent) {
        Intent syncDataIntent = new Intent(context, DataLoadService.class);
        syncDataIntent.setAction(DataLoadService.ACTION_LOAD_DATA);
        syncDataIntent.putExtra(DataLoadService.KEY_SHOW_NOTIFICATION, true);
        context.startService(syncDataIntent);
    }
}
