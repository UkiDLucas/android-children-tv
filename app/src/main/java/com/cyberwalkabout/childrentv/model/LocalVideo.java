package com.cyberwalkabout.childrentv.model;

import android.content.ContentValues;
import android.database.Cursor;
import android.provider.BaseColumns;

import com.j256.ormlite.field.DatabaseField;
import com.j256.ormlite.table.DatabaseTable;

import java.io.Serializable;

// TODO: consider to get rid of this model and use only VideoEntity

/**
 * @author Andrii Kovalov
 */
@DatabaseTable(tableName = "videos")
public class LocalVideo implements Serializable {
    /**
     *
     */
    private static final long serialVersionUID = 7083970146981977895L;

    public static final String TABLE_NAME = "videos";

    public final static String ID = BaseColumns._ID;
    public final static String TITLE = "title";
    public final static String YOUTUBE_ID = "youtube_id";
    public final static String DESCRIPTION = "description";
    public final static String AGE_GROUP = "age_group";
    public final static String DURATION = "duration";
    public final static String RATING = "rating";
    public final static String WATCHED = "watched";
    public final static String LANGUAGE = "language";
    public final static String SERIES_ID = "series_id";
    public final static String MODIFIED = "modified";
    public final static String VIDEO_LINK = "youtube_video_link";
    public final static String IS_NEW = "is_new";
    // TODO: do not forget to remove in future
    public final static String SPREADSHEET_ID = "spreadsheet_id";
    public final static String SORT_ORDER = "sort_order";
    public final static String IS_PLAYABLE = "is_playable";

    public static String[] ALL_COLUMNS = new String[]
            {ID, TITLE, YOUTUBE_ID, AGE_GROUP, DESCRIPTION, DURATION, RATING, SERIES_ID, WATCHED, VIDEO_LINK, IS_NEW, SPREADSHEET_ID, SORT_ORDER, IS_PLAYABLE};

    @DatabaseField(columnName = ID, generatedId = true)
    private Long id;
    @DatabaseField(columnName = YOUTUBE_ID)
    private String youtubeId;
    @DatabaseField(columnName = TITLE)
    private String title;
    @DatabaseField(columnName = DESCRIPTION)
    private String description;
    @DatabaseField(columnName = AGE_GROUP)
    private String ageGroup;
    @DatabaseField(columnName = DURATION)
    private String duration;
    @DatabaseField(columnName = RATING)
    private int rating;
    @DatabaseField(columnName = LANGUAGE)
    private String language;
    @DatabaseField(columnName = SERIES_ID)
    private int seriesId;
    @DatabaseField(columnName = WATCHED)
    private int watchedCount;
    @DatabaseField(columnName = MODIFIED)
    private long modified;
    @DatabaseField(columnName = VIDEO_LINK)
    private String youtubeVideoLink;
    @DatabaseField(columnName = IS_NEW)
    private boolean isNew;
    @DatabaseField(columnName = SPREADSHEET_ID)
    private int spreadsheetId;
    @DatabaseField(columnName = SORT_ORDER)
    private long sortOrder;
    @DatabaseField(columnName = IS_PLAYABLE)
    private Boolean isPlayable;

    public static LocalVideo create(Cursor cursor) {
        LocalVideo video = new LocalVideo();

        video.setId(cursor.getLong(cursor.getColumnIndex(ID)));
        video.setTitle(cursor.getString(cursor.getColumnIndex(LocalVideo.TITLE)));
        video.setYoutubeId(cursor.getString(cursor.getColumnIndex(LocalVideo.YOUTUBE_ID)));
        video.setAgeGroup(cursor.getString(cursor.getColumnIndex(LocalVideo.AGE_GROUP)));
        video.setDescription(cursor.getString(cursor.getColumnIndex(LocalVideo.DESCRIPTION)));
        video.setDuration(cursor.getString(cursor.getColumnIndex(LocalVideo.DURATION)));
        video.setRating(cursor.getInt(cursor.getColumnIndex(LocalVideo.RATING)));
        video.setSeriesId(cursor.getInt(cursor.getColumnIndex(LocalVideo.SERIES_ID)));
        video.setLanguage(cursor.getString(cursor.getColumnIndex(LocalVideo.LANGUAGE)));
        video.setYoutubeUrl(cursor.getString(cursor.getColumnIndex(VIDEO_LINK)));
        video.setNew(cursor.getInt(cursor.getColumnIndex(IS_NEW)) == 1);
        video.setSpreadsheetId(cursor.getInt(cursor.getColumnIndex(SPREADSHEET_ID)));
        video.setSortOrder(cursor.getLong(cursor.getColumnIndex(SORT_ORDER)));
        video.setIsPlayable(cursor.isNull(cursor.getColumnIndex(IS_PLAYABLE)) || cursor.getInt(cursor.getColumnIndex(IS_PLAYABLE)) == 1);
        return video;
    }

    public ContentValues toContentValues() {
        ContentValues cv = new ContentValues();
        cv.put(AGE_GROUP, getAgeGroup());
        cv.put(DESCRIPTION, getDescription());
        cv.put(DURATION, getDuration());
        cv.put(LANGUAGE, getLanguage());
        cv.put(RATING, getRating());
        cv.put(SERIES_ID, getSeriesId());
        cv.put(TITLE, getTitle());
        cv.put(YOUTUBE_ID, getYoutubeId());
        cv.put(MODIFIED, getModified());
        cv.put(VIDEO_LINK, getYoutubeVideoLink());
        cv.put(IS_NEW, isNew() ? 1 : 0);
        cv.put(SPREADSHEET_ID, getSpreadsheetId());
        cv.put(SORT_ORDER, getSortOrder());
        cv.put(IS_PLAYABLE, getIsPlayable() ? 1 : 0);
        return cv;
    }

    public Boolean getIsPlayable() {
        return isPlayable == null || isPlayable;
    }

    public void setIsPlayable(Boolean isPlayable) {
        this.isPlayable = isPlayable;
    }

    public long getSortOrder() {
        return sortOrder;
    }

    public void setSortOrder(long sortOrder) {
        this.sortOrder = sortOrder;
    }

    public int getSpreadsheetId() {
        return spreadsheetId;
    }

    public void setSpreadsheetId(int spreadsheetId) {
        this.spreadsheetId = spreadsheetId;
    }

    public String getYoutubeVideoLink() {
        return youtubeVideoLink;
    }

    public void setYoutubeUrl(String youtobeVideoLink) {
        this.youtubeVideoLink = youtobeVideoLink;
    }

    public boolean isNew() {
        return isNew;
    }

    public void setNew(boolean isNew) {
        this.isNew = isNew;
    }

    public long getModified() {
        return modified;
    }

    public void setModified(long modified) {
        this.modified = modified;
    }

    public String getYoutubeId() {
        return youtubeId;
    }

    public void setYoutubeId(String youtubeId) {
        this.youtubeId = youtubeId;
    }

    public String getTitle() {
        return title;
    }

    public void setTitle(String title) {
        this.title = title;
    }

    public void setDescription(String description) {
        this.description = description;
    }

    public String getDescription() {
        return description;
    }

    public void setAgeGroup(String ageGroup) {
        this.ageGroup = ageGroup;
    }

    public String getAgeGroup() {
        return ageGroup;
    }

    public void setDuration(String duration) {
        this.duration = duration;
    }

    public String getDuration() {
        return duration;
    }

    public void setId(Long id) {
        this.id = id;
    }

    public Long getId() {
        return id;
    }

    public int getWatchedCount() {
        return watchedCount;
    }

    public void setWatchedCount(int watchedCount) {
        this.watchedCount = watchedCount;
    }

    public void setRating(int rating) {
        this.rating = rating;
    }

    public int getRating() {
        return rating;
    }

    public void setLanguage(String language) {
        this.language = language;
    }

    public String getLanguage() {
        return language;
    }

    public void setSeriesId(int seriesId) {
        this.seriesId = seriesId;
    }

    public int getSeriesId() {
        return seriesId;
    }
}
