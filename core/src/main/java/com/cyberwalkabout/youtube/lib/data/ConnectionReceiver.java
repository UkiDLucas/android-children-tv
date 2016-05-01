package com.cyberwalkabout.youtube.lib.data;

import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;

import com.cyberwalkabout.youtube.lib.util.AppSettings;

import com.cyberwalkabout.common.util.Env;

/**
 * @author Maria Dzyokh
 */
public class ConnectionReceiver extends BroadcastReceiver {
    @Override
    public void onReceive(Context context, Intent intent) {
        if (Env.isConnected(context)) {
            if (new AppSettings(context).isLastSyncFailed()) {
                Intent syncIntent = new Intent(context, DataLoadService.class);
                syncIntent.setAction(DataLoadService.ACTION_LOAD_DATA);
                context.startService(syncIntent);
            }
        }
    }
}
