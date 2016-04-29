package com.cyberwalkabout.youtube.lib.data.db;

import android.content.ContentValues;
import android.content.Context;
import android.database.Cursor;
import android.database.sqlite.SQLiteDatabase;
import android.database.sqlite.SQLiteQueryBuilder;
import android.text.TextUtils;
import android.util.Log;

import com.cyberwalkabout.common.util.CursorUtils;
import com.cyberwalkabout.common.util.StringUtils;
import com.cyberwalkabout.youtube.lib.app.ChildrenTVApp;
import com.cyberwalkabout.youtube.lib.model.LocalVideo;
import com.cyberwalkabout.youtube.lib.model.LocalVideoRating;
import com.j256.ormlite.android.apptools.OrmLiteSqliteOpenHelper;
import com.j256.ormlite.support.ConnectionSource;
import com.j256.ormlite.table.TableUtils;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.Iterator;
import java.util.List;

/**
 * @author Maria Dzyokh
 *         TODO: better create ContentProvider
 */
public class DbHelper extends OrmLiteSqliteOpenHelper {

    private static final String TAG = DbHelper.class.getSimpleName();

    public static final String NAME = "CHILDRENTV_DB_HELPER";

    private static final String DATABASE_NAME = "data";

    private static final int DB_49_INITIAL_RELEASE = 49; // 23 April 2014
    private static final int DB_50_V_37 = 50;
    private static final int DB_51_V_42 = 51;
    private static final int DB_52_V_43_ADDED_IS_PLAYABLE = 52;
    private static final int DB_53_V_44_FIX_IS_PLAYABLE_ISSUE = 53;

    private static final int CURRENT_DATABASE_VERSION = DB_53_V_44_FIX_IS_PLAYABLE_ISSUE;

    public static DbHelper get(Context context) {
        DbHelper dbHelper = (DbHelper) context.getSystemService(NAME);
        if (dbHelper == null) {
            synchronized (DbHelper.class) {
                if (dbHelper == null) {
                    context = context.getApplicationContext();
                    dbHelper = (DbHelper) context.getSystemService(NAME);
                    if (dbHelper == null) {
                        ChildrenTVApp app = (ChildrenTVApp) context;
                        app.initDbHelper();
                    }
                    dbHelper = (DbHelper) context.getSystemService(NAME);
                }
            }
        }
        if (dbHelper == null) {
            throw new IllegalStateException("DbHelper not available");
        }
        return dbHelper;
    }

    public DbHelper(Context context) {
        super(context, DATABASE_NAME, null, CURRENT_DATABASE_VERSION);
    }

    @Override
    public void onCreate(SQLiteDatabase db, ConnectionSource connectionSource) {
        try {
            TableUtils.createTable(connectionSource, LocalVideo.class);
            TableUtils.createTable(connectionSource, LocalVideoRating.class);
        } catch (java.sql.SQLException e) {
            Log.e(TAG, e.getMessage(), e);
        }
    }

    @Override
    public void onUpgrade(SQLiteDatabase db, ConnectionSource connectionSource, int oldVersion, int newVersion) {
        try {
            Log.d(TAG, "onUpgrade() from " + oldVersion + " to " + newVersion);

            int version = oldVersion;

            // if this is old database try to upgrade it to new
            if (version < DB_50_V_37) {
                if (upgradeFrom49To50(db)) {
                    version = DB_50_V_37;
                }
            }

            if (version >= DB_50_V_37 && version < DB_51_V_42) {
                if (upgradeFrom50to51(connectionSource)) {
                    version = DB_51_V_42;
                }
            }

            if (version >= DB_51_V_42 && version < DB_52_V_43_ADDED_IS_PLAYABLE) {
                if (upgradeFrom51To52(db)) {
                    version = DB_52_V_43_ADDED_IS_PLAYABLE;
                }
            }

            if (version >= DB_52_V_43_ADDED_IS_PLAYABLE && version < DB_53_V_44_FIX_IS_PLAYABLE_ISSUE) {
                if (upgradeFrom52To53(db)) {
                    version = DB_53_V_44_FIX_IS_PLAYABLE_ISSUE;
                }
            }

            // by now db should be fully upgraded, if no it means that unexpected error encountered and we must recreate database
            if (version != DB_52_V_43_ADDED_IS_PLAYABLE) {
                Log.w(TAG, "Upgrade unsuccessful -- destroying old data during upgrade");
                dropDb(connectionSource);
                onCreate(db, connectionSource);
            }
        } catch (java.sql.SQLException e) {
            Log.e(TAG, e.getMessage(), e);
            throw new RuntimeException(e);
        }
    }

    private void dropDb(ConnectionSource connectionSource) throws java.sql.SQLException {
        TableUtils.dropTable(connectionSource, LocalVideo.class, true);
        TableUtils.dropTable(connectionSource, LocalVideoRating.class, true);
    }

    private boolean upgradeFrom49To50(SQLiteDatabase db) {
        try {
            Log.d(TAG, "Upgrading to new database schema. Adding '" + LocalVideo.SORT_ORDER + "' column to '" + LocalVideo.TABLE_NAME + "' table which is required for 'randomize' feature");
            db.execSQL("ALTER TABLE " + LocalVideo.TABLE_NAME + " ADD COLUMN " + LocalVideo.SORT_ORDER + " INTEGER DEFAULT 0");
            return true;
        } catch (Exception e) {
            Log.w(TAG, "Failed to upgrade to version " + DB_50_V_37);
        }
        return false;
    }

    private boolean upgradeFrom50to51(ConnectionSource connectionSource) {
        try {
            try {
                TableUtils.createTable(connectionSource, LocalVideoRating.class);
                return true;
            } catch (java.sql.SQLException e) {
                Log.e(TAG, e.getMessage(), e);
            }
        } catch (Exception e) {
            Log.w(TAG, "Failed to upgrade to version " + DB_51_V_42);
        }
        return false;
    }

    private boolean upgradeFrom51To52(SQLiteDatabase db) {
        try {
            Log.d(TAG, "Upgrading to new database schema. Adding '" + LocalVideo.IS_PLAYABLE + "' column to '" + LocalVideo.TABLE_NAME + "' table which is required to hide not playable videos.");
            db.execSQL("alter table " + LocalVideo.TABLE_NAME + " add column " + LocalVideo.IS_PLAYABLE + " INTEGER DEFAULT 1");
            return true;
        } catch (Exception e) {
            Log.w(TAG, "Failed to upgrade to version " + DB_52_V_43_ADDED_IS_PLAYABLE);
        }
        return false;
    }

    private boolean upgradeFrom52To53(SQLiteDatabase db) {
        try {
            Log.d(TAG, "Reset all videos state to 'PLAYABLE' to fix issue which marks all videos to be 'NOT PLAYABLE' after update from server.");
            db.execSQL("update " + LocalVideo.TABLE_NAME + " set " + LocalVideo.IS_PLAYABLE + " = 1");
            return true;
        } catch (Exception e) {
            Log.w(TAG, "Failed to upgrade to version " + DB_53_V_44_FIX_IS_PLAYABLE_ISSUE);
        }
        return false;
    }

    public LocalVideo getVideoById(int id) {
        LocalVideo video = null;
        Cursor cursor = null;
        try {
            cursor = getReadableDatabase().rawQuery("select " + getColumnsStr() + " from videos where _id = '" + id + "'", null);
            if (cursor.moveToFirst()) {
                video = LocalVideo.create(cursor);
            }
        } finally {
            CursorUtils.closeQuietly(cursor);
        }
        return video;
    }

    public LocalVideo getVideoByYoutubeId(String youtubeId) {
        LocalVideo video = null;
        Cursor cursor = null;
        try {
            cursor = getReadableDatabase().rawQuery("select " + getColumnsStr() + " from videos where youtube_id = '" + youtubeId + "'", null);
            if (cursor.moveToFirst()) {
                video = LocalVideo.create(cursor);
            }
        } finally {
            CursorUtils.closeQuietly(cursor);
        }
        return video;
    }

    public List<String> getLanguages() {
        List<String> languages = new ArrayList<String>();
        Cursor cursor = null;
        try {
            cursor = getReadableDatabase().rawQuery("select distinct trim(" + LocalVideo.LANGUAGE + ") as " + LocalVideo.LANGUAGE + " from " + LocalVideo.TABLE_NAME + " order by " + LocalVideo.LANGUAGE + " asc", null);
            if (cursor.moveToFirst()) {
                do {
                    languages.add(cursor.getString(cursor.getColumnIndex(LocalVideo.LANGUAGE)).trim());
                } while (cursor.moveToNext());
            }
        } finally {
            CursorUtils.closeQuietly(cursor);
        }
        return languages;
    }

    public Cursor getSeriesCursor(int seriesId) {
        return getReadableDatabase().rawQuery("select " + StringUtils.toString(Arrays.asList(LocalVideo.ALL_COLUMNS)) + ", trim(" + LocalVideo.LANGUAGE + ") as " + LocalVideo.LANGUAGE + "  from videos where series_id = '" + seriesId + "' and youtube_id is not null order by _id", null);
    }

    public Cursor queryVideos(List<String> selectedAgeGroups, List<String> selectedLanguages, boolean favorite, Boolean isNew, String searchStr) {
        String sql = buildQueryVideosSql(selectedAgeGroups, selectedLanguages, favorite, isNew, searchStr);
        return getReadableDatabase().rawQuery(sql, null);
    }

    public void incrementWatchedCount(String youtubeId) {
        Cursor c = getReadableDatabase().rawQuery("select watched from videos where youtube_id = '" + youtubeId + "'", null);
        if (c.moveToFirst()) {
            int watchedCount = c.getInt(0);
            watchedCount++;
            c.close();

            try {
                SQLiteDatabase db = getWritableDatabase();
                ContentValues cv = new ContentValues();
                cv.put("watched", watchedCount);
                db.beginTransaction();
                db.update("videos", cv, "youtube_id = '" + youtubeId + "'", null);
                db.setTransactionSuccessful();
                db.endTransaction();
            } catch (Exception e) {
                Log.e(TAG, e.getMessage(), e);
            }
        }
    }

    public void markAllVideosOld() {
        try {
            SQLiteDatabase db = getWritableDatabase();
            ContentValues cv = new ContentValues();
            cv.put("is_new", 0);
            db.beginTransaction();
            db.update("videos", cv, null, null);
            db.setTransactionSuccessful();
            db.endTransaction();
        } catch (Exception e) {
            Log.e(TAG, e.getMessage(), e);
        }
    }

    public int getTotalWatchedCount() {
        Cursor cursor = null;
        try {
            cursor = getReadableDatabase().rawQuery("select sum(watched) from videos", null);
            if (cursor.moveToFirst()) {
                return cursor.getInt(0);
            }
        } finally {
            CursorUtils.closeQuietly(cursor);
        }
        return 0;
    }

    public int getWatchedCount(String youtubeId) {
        Cursor cursor = null;
        try {
            cursor = getReadableDatabase().rawQuery("select watched from videos where youtube_id = '" + youtubeId + "'", null);
            if (cursor.moveToFirst()) {
                return cursor.getInt(0);
            }
        } finally {
            CursorUtils.closeQuietly(cursor);
        }
        return 0;
    }

    public int getSeriesCount(int seriesId) {
        int count = 0;
        Cursor cursor = null;
        try {
            cursor = getReadableDatabase().rawQuery("select count(*) from videos where series_id = '" + seriesId + "' and youtube_id is not null", null);
            if (cursor.moveToFirst()) {
                count = cursor.getInt(0);
            }
        } finally {
            CursorUtils.closeQuietly(cursor);
        }
        return count;
    }

    public int getAllVideosCount() {
        int count = 0;
        Cursor cursor = null;
        try {
            cursor = getReadableDatabase().rawQuery("select count(*) from " + LocalVideo.TABLE_NAME, null);
            if (cursor.moveToFirst()) {
                count = cursor.getInt(0);
            }
        } catch (Exception e) {
            Log.e(TAG, e.getMessage(), e);
        } finally {
            CursorUtils.closeQuietly(cursor);
        }
        return count;
    }

    public int getNewVideosCount() {
        int count = 0;
        Cursor cursor = null;
        try {
            cursor = getReadableDatabase().rawQuery("select count(*) from " + LocalVideo.TABLE_NAME + " where is_new = 1", null);
            if (cursor.moveToFirst()) {
                count = cursor.getInt(0);
            }
        } catch (Exception e) {
            Log.e(TAG, e.getMessage(), e);
        } finally {
            CursorUtils.closeQuietly(cursor);
        }
        return count;
    }

    public ArrayList<String> getSeriesPlaylist(int seriesId) {
        ArrayList<String> result = new ArrayList<String>();
        Cursor cursor = null;
        try {
            cursor = getReadableDatabase().rawQuery("select _id, youtube_id, series_id from videos where series_id = '" + seriesId + "' and youtube_id is not null order by _id", null);
            if (cursor.moveToFirst()) {
                do {
                    result.add(cursor.getString(cursor.getColumnIndex(LocalVideo.YOUTUBE_ID)));
                } while (cursor.moveToNext());
            }
        } finally {
            CursorUtils.closeQuietly(cursor);
        }
        return result;
    }

    public ArrayList<String> getVideosPlaylist(List<String> selectedAgeGroups, List<String> selectedLanguages, boolean favorite, String searchStr) {
        ArrayList<String> result = new ArrayList<String>();
        Cursor cursor = null;
        try {
            cursor = getReadableDatabase().rawQuery(buildQueryVideosSql(selectedAgeGroups, selectedLanguages, favorite, null, searchStr), null);
            if (cursor.moveToFirst()) {
                do {
                    result.add(cursor.getString(cursor.getColumnIndex(LocalVideo.YOUTUBE_ID)));
                } while (cursor.moveToNext());
            }
        } finally {
            CursorUtils.closeQuietly(cursor);
        }
        return result;
    }

    public void adjustLocalRating(String youtubeId, int ratingDelta) {
        Integer currentRating = getVideoLocalRating(youtubeId);
        if (currentRating != null) {
            // rating exists
            int newRating = currentRating + ratingDelta;

            ContentValues values = new ContentValues();
            values.put(LocalVideoRating.LOCAL_RATING, newRating);
            getWritableDatabase().update(LocalVideoRating.TABLE_NAME, values, LocalVideoRating.YOUTUBE_ID + "=?", new String[]{youtubeId});

            Log.d(TAG, "Modify rating of the video '" + youtubeId + "' " + currentRating + " -> " + newRating);
        } else {
            // rating doesn't exist
            ContentValues values = new ContentValues();
            values.put(LocalVideoRating.LOCAL_RATING, ratingDelta);
            values.put(LocalVideoRating.YOUTUBE_ID, youtubeId);
            getWritableDatabase().insert(LocalVideoRating.TABLE_NAME, null, values);

            Log.d(TAG, "Set initial rating of the video '" + youtubeId + "' -> " + ratingDelta);
        }
    }

    public Integer getVideoLocalRating(String youtubeId) {
        if (!TextUtils.isEmpty(youtubeId)) {
            Cursor cursor = null;
            try {
                SQLiteQueryBuilder queryBuilder = new SQLiteQueryBuilder();
                queryBuilder.setTables(LocalVideoRating.TABLE_NAME);
                cursor = queryBuilder.query(getReadableDatabase(), new String[]{LocalVideoRating.LOCAL_RATING}, LocalVideoRating.YOUTUBE_ID + "=?", new String[]{youtubeId}, null, null, null);

                if (cursor.moveToFirst()) {
                    return cursor.getInt(0);
                }
            } finally {
                CursorUtils.closeQuietly(cursor);
            }
        }
        return null;
    }

    public void setVideoPlayable(String youtubeId, boolean isPlayable) {
        ContentValues values = new ContentValues();
        values.put(LocalVideo.IS_PLAYABLE, isPlayable ? 1 : 0);
        getWritableDatabase().update(LocalVideo.TABLE_NAME, values, LocalVideo.YOUTUBE_ID + "=?", new String[]{youtubeId});
    }

    // Utility methods

    private String getColumnsStr() {
        return StringUtils.toString(Arrays.asList(LocalVideo.ALL_COLUMNS)) + ", trim(" + LocalVideo.LANGUAGE + ") as " + LocalVideo.LANGUAGE;
    }

    private String buildQueryVideosSql(List<String> selectedAgeGroups, List<String> selectedLanguages, boolean favorite, Boolean isNew, String searchStr) {
        StringBuilder selection = new StringBuilder();
        selection.append(LocalVideo.AGE_GROUP).append(" in (");
        for (Iterator<String> iterator = selectedAgeGroups.iterator(); iterator.hasNext(); ) {
            String ageGroup = iterator.next();
            selection.append("'").append(ageGroup).append("'");
            if (iterator.hasNext())
                selection.append(",");
        }
        selection.append(") and ").append(LocalVideo.LANGUAGE).append(" in (");

        for (Iterator<String> iterator = selectedLanguages.iterator(); iterator.hasNext(); ) {
            String language = iterator.next();
            selection.append("'").append(language).append("'");
            if (iterator.hasNext())
                selection.append(",");
        }
        selection.append(")");

        selection.append(" and (").append(LocalVideo.IS_PLAYABLE).append(" = 1 or ").append(LocalVideo.IS_PLAYABLE).append(" is null)");

        if (favorite) {
            selection.append(" and ").append(LocalVideo.RATING).append("=5");
        }

        if (isNew != null) {
            selection.append(" and ").append(LocalVideo.IS_NEW).append("=").append(isNew ? "1" : "0");
        }

        if (!TextUtils.isEmpty(searchStr)) {
            selection.append(" and ").append(LocalVideo.TITLE).append(" LIKE '%").append(searchStr).append("%'");
        }

        String[] allColumns = new String[]
                {
                        LocalVideo.TABLE_NAME + "." + LocalVideo.ID + " as " + LocalVideo.ID,
                        LocalVideo.TITLE,
                        LocalVideo.TABLE_NAME + "." + LocalVideo.YOUTUBE_ID + " as " + LocalVideo.YOUTUBE_ID,
                        LocalVideo.AGE_GROUP,
                        LocalVideo.DESCRIPTION,
                        LocalVideo.DURATION,
                        LocalVideo.RATING,
                        LocalVideo.SERIES_ID,
                        LocalVideo.WATCHED,
                        LocalVideo.VIDEO_LINK,
                        LocalVideo.IS_NEW,
                        LocalVideo.SPREADSHEET_ID,
                        LocalVideo.SORT_ORDER,
                        LocalVideo.IS_PLAYABLE,
                        "ifnull(" + LocalVideoRating.TABLE_NAME + "." + LocalVideoRating.LOCAL_RATING + ",0) as " + LocalVideoRating.LOCAL_RATING
                };

        String allColumnsStr = StringUtils.toString(allColumns);

        StringBuilder sql = new StringBuilder();

        sql.append("select ").append(allColumnsStr).append(", trim(" + LocalVideo.LANGUAGE + ") as " + LocalVideo.LANGUAGE);
        appendJoinLocalRatingSql(sql);
        sql.append(" where ").append(selection.toString()).append(" and (series_id is null or series_id=0) and " + LocalVideo.TABLE_NAME + "." + LocalVideo.YOUTUBE_ID + " is not null");

        sql.append(" union ");

        sql.append("select ").append(allColumnsStr).append(", trim(" + LocalVideo.LANGUAGE + ") as " + LocalVideo.LANGUAGE);
        appendJoinLocalRatingSql(sql);
        sql.append(" where ").append(selection.toString()).append(" and series_id is not null and series_id !=0 and " + LocalVideo.TABLE_NAME + "." + LocalVideo.YOUTUBE_ID + " is not null")
                .append(" group by series_id");

        // new approach is to randomize search result
        if (!TextUtils.isEmpty(searchStr)) {
            // if search keyword provided sort by title to provide consistent results
            sql.append(" order by ").append(LocalVideo.TITLE).append(" desc");
            return sql.toString();
        /*} else if (randomize) {
            // randomize
            return "select * from (" + sql.toString() + ") order by random()";*/
        } else {
            // original sorting order (before we introduced randomization)
            sql.append(" order by local_rating desc, ").append(LocalVideo.WATCHED).append(" desc");
            return sql.toString();
        }
    }

    private void appendJoinLocalRatingSql(StringBuilder sqlBuilder) {
        sqlBuilder.append(" from ").append(LocalVideo.TABLE_NAME).append(" ").append(LocalVideo.TABLE_NAME)
                .append(" left join ").append(LocalVideoRating.TABLE_NAME).append(" ").append(LocalVideoRating.TABLE_NAME)
                .append(" on " + LocalVideoRating.TABLE_NAME + "." + LocalVideoRating.YOUTUBE_ID + "=" + LocalVideo.TABLE_NAME + "." + LocalVideo.YOUTUBE_ID);
    }
}
