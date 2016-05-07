package com.cyberwalkabout.childrentv.subscription;

import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.util.Log;

import java.text.DateFormat;
import java.util.Date;

public class SubscriptionNotificationReceiver extends BroadcastReceiver {
    private static final String TAG = SubscriptionNotificationReceiver.class.getSimpleName();

    @Override
    public void onReceive(Context context, Intent intent) {
        Log.d(TAG, "onReceive: " + DateFormat.getDateTimeInstance().format(new Date()));
        showNotification(context);
    }

    private void showNotification(Context context) {
//        SubscriptionHelper subscriptionHelper = new SubscriptionHelper(context);
//        subscriptionHelper.displayNotification(context);
//        subscriptionHelper.setNotificationTimestamp(System.currentTimeMillis());
    }
}