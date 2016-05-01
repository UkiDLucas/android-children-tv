package com.cyberwalkabout.youtube.lib.util;

import android.content.Context;
import android.content.SharedPreferences;
import android.provider.Settings;
import android.text.TextUtils;

import java.util.UUID;

/**
 * @author Andrii Kovalov
 */
public class Identity {

    public static String getId(Context context) {
        String androidId = Settings.Secure.getString(context.getContentResolver(), Settings.Secure.ANDROID_ID);

        String deviceId;

        if (TextUtils.isEmpty(androidId)) {
            SharedPreferences prefs = context.getSharedPreferences("device_id", Context.MODE_PRIVATE);

            if (prefs.contains("device_id")) {
                deviceId = prefs.getString("device_id", null);
            } else {
                deviceId = UUID.randomUUID().toString();
                prefs.edit().putString("device_id", deviceId).apply();
            }
        } else {
            deviceId = androidId;
        }
        return deviceId;
    }
}
