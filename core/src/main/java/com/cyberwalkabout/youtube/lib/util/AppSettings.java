package com.cyberwalkabout.youtube.lib.util;

import android.app.AlarmManager;
import android.content.Context;
import android.content.SharedPreferences;

import com.cyberwalkabout.childrentv.shared.model.AgeGroupConst;
import com.cyberwalkabout.youtube.lib.R;
import com.cyberwalkabout.youtube.lib.data.DataLoadService;

import java.util.Locale;

/**
 * @author Maria Dzyokh
 * @author Andrii Kovalov
 */
public class AppSettings {

    private static final String KEY_FIRST_LAUNCH = "first_launch";
    private static final String KEY_FIRST_RUN_TIME = "first_launch_time";

    private static final String KEY_LAST_SYNC_TIME = "EXTRA_LAST_SYNC_TIME";
    private static final String KEY_LAST_SYNC_STATUS = "EXTRA_LAST_SYNC_STATUS";
    private static final String KEY_FIRST_SELECTED_LANGUAGE = "FIRST_SELECTED_LANGUAGE";

    private SharedPreferences mainPrefs;

    public static int getAgeGroupIconId(String ageGroup) {
        int ageGroupIconId = 0;
        if (AgeGroupConst.AGE_2.equals(ageGroup)) {
            ageGroupIconId = R.drawable.filter_age2;
        } else if (AgeGroupConst.AGE_4.equals(ageGroup)) {
            ageGroupIconId = R.drawable.filter_age4;
        } else if (AgeGroupConst.AGE_6.equals(ageGroup)) {
            ageGroupIconId = R.drawable.filter_age6;
        } else if (AgeGroupConst.AGE_8.equals(ageGroup)) {
            ageGroupIconId = R.drawable.filter_age8;
        }
        return ageGroupIconId;
    }

    public static int getLanguageIconResId(Context ctx, String language, boolean checked) {
        String uri = ctx.getPackageName() + ":drawable/" + language + "_" + (checked ? "ac" : "in");
        return ctx.getResources().getIdentifier(uri, null, null);
    }

    public AppSettings(Context context) {
        if (context == null) {
            throw new IllegalArgumentException("Context can't be null");
        }
        mainPrefs = context.getSharedPreferences("prefs", context.MODE_PRIVATE);
    }

    public void setFirstLaunchTimestamp(long timestamp) {
        mainPrefs.edit().putLong(KEY_FIRST_RUN_TIME, timestamp).apply();
    }

    public boolean isFirstLaunch() {
        return mainPrefs.getBoolean(KEY_FIRST_LAUNCH, true);
    }

    public void setFirstLaunch(boolean firstLaunch) {
        mainPrefs.edit().putBoolean(KEY_FIRST_LAUNCH, firstLaunch).apply();
    }

    public boolean isAgeGroupSelected(String ageGroup) {
        return mainPrefs.getBoolean(ageGroup, true);
    }

    public void updateAgeGroup(String ageGroup, boolean selected) {
        mainPrefs.edit().putBoolean(ageGroup, selected).apply();
    }

    public boolean isLanguageSelected(String language) {
        return mainPrefs.getBoolean(language, true);
    }

    public void updateLanguage(String language, boolean selected) {
        mainPrefs.edit().putBoolean(language, selected).apply();
    }

    public void setLastSyncTime(long lastSyncTime) {
        mainPrefs.edit().putLong(KEY_LAST_SYNC_TIME, lastSyncTime).apply();
    }

    public void setLastSyncStatus(String lastSyncStatus) {
        mainPrefs.edit().putString(KEY_LAST_SYNC_STATUS, lastSyncStatus).apply();
    }

    public void setLastSyncStatus(String lastSyncStatus, long lastSyncTime) {
        mainPrefs.edit().putString(KEY_LAST_SYNC_STATUS, lastSyncStatus).putLong(KEY_LAST_SYNC_TIME, lastSyncTime).apply();
    }

    public boolean isLastSyncFailed() {
        return DataLoadService.SyncStatus.FAILED.name().equals(mainPrefs.getString(KEY_LAST_SYNC_STATUS, DataLoadService.SyncStatus.FAILED.name()));
    }

    public boolean needToSyncVideos() {
        boolean dataExpired = System.currentTimeMillis() - mainPrefs.getLong(KEY_LAST_SYNC_TIME, 0) > AlarmManager.INTERVAL_DAY;
        boolean firstLaunch = mainPrefs.getBoolean(KEY_FIRST_LAUNCH, true);
        return firstLaunch || isLastSyncFailed() || dataExpired;
    }

    public String getFirstSelectedLanguage() {
        return mainPrefs.getString(KEY_FIRST_SELECTED_LANGUAGE, Locale.UK.getLanguage());
    }

    public void setFirstSelectedLanguage(String language) {
        mainPrefs.edit().putString(KEY_FIRST_SELECTED_LANGUAGE, language).apply();
    }
}
