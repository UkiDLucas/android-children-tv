package com.cyberwalkabout.youtube.lib.model;

import android.provider.BaseColumns;

import com.j256.ormlite.field.DatabaseField;
import com.j256.ormlite.table.DatabaseTable;

/**
 * @author Andrii Kovalov
 */
@DatabaseTable(tableName = "local_rating")
public class LocalVideoRating {
    public static final String TABLE_NAME = "local_rating";

    public final static String ID = BaseColumns._ID;

    public static final String LOCAL_RATING = "local_rating";
    public static final String YOUTUBE_ID = "youtube_id";

    @DatabaseField(columnName = ID, generatedId = true)
    private Long id;
    @DatabaseField(columnName = YOUTUBE_ID)
    private String youtubeId;
    @DatabaseField(columnName = LOCAL_RATING)
    private Integer rating;

    public Long getId() {
        return id;
    }

    public void setId(Long id) {
        this.id = id;
    }

    public String getYoutubeId() {
        return youtubeId;
    }

    public void setYoutubeId(String youtubeId) {
        this.youtubeId = youtubeId;
    }

    public Integer getRating() {
        return rating;
    }

    public void setRating(Integer rating) {
        this.rating = rating;
    }
}
