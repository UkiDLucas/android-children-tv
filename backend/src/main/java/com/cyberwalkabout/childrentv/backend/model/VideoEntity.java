package com.cyberwalkabout.childrentv.backend.model;

import com.google.appengine.api.datastore.Entity;
import com.google.appengine.api.datastore.Key;
import com.google.appengine.api.datastore.KeyFactory;

/**
 * @author Andrii Kovalov
 */
public class VideoEntity {
    public static final String PARENT_TYPE = "YoutubeVideo";
    public static final String TYPE = "VideoEntity";

    public static final Key PARENT_KEY = KeyFactory.createKey(PARENT_TYPE, "all");

    public final static String TITLE = "title";
    public final static String DESCRIPTION = "description";
    public final static String AGE_GROUP = "age_group";
    public final static String SERIES_ID = "series_id";

    public final static String YOUTUBE_ID = "youtube_id";
    public final static String YOUTUBE_URL = "youtube_url";
    public final static String DURATION = "duration";

    public final static String RATING = "rating";
    public final static String LANGUAGE = "language";

    public final static String MODIFIED_TIMESTAMP = "modified";
    public static final String IS_ACTIVE = "is_active";

    private Long id;

    private String title;
    private String description;
    private String ageGroup;
    private int seriesId;

    private String youtubeId;
    private String youtubeUrl;

    private String duration;
    private int rating;
    private String language;
    private long modifiedTimestamp;

    public static VideoEntity toVideo(Entity entity) {
        VideoEntity videoEntity = new VideoEntity();

        if (entity.getKey() != null) {
            videoEntity.setId(entity.getKey().getId());
        }

        if (entity.hasProperty(TITLE)) {
            videoEntity.setTitle((String) entity.getProperty(TITLE));
        }

        if (entity.hasProperty(DURATION)) {
            videoEntity.setDescription((String) entity.getProperty(DESCRIPTION));
        }

        if (entity.hasProperty(AGE_GROUP)) {
            videoEntity.setAgeGroup((String) entity.getProperty(AGE_GROUP));
        }

        if (entity.hasProperty(SERIES_ID)) {
            videoEntity.setSeriesId(((Long) entity.getProperty(SERIES_ID)).intValue());
        }

        if (entity.hasProperty(YOUTUBE_ID)) {
            videoEntity.setYoutubeId((String) entity.getProperty(YOUTUBE_ID));
        }

        if (entity.hasProperty(YOUTUBE_URL)) {
            videoEntity.setYoutubeUrl((String) entity.getProperty(YOUTUBE_URL));
        }

        if (entity.hasProperty(DURATION)) {
            // TODO: consider to convert it to milliseconds
            videoEntity.setDuration((String) entity.getProperty(DURATION));
        }

        if (entity.hasProperty(RATING)) {
            videoEntity.setRating(((Long) entity.getProperty(RATING)).intValue());
        }

        if (entity.hasProperty(LANGUAGE)) {
            videoEntity.setLanguage((String) entity.getProperty(LANGUAGE));
        }

        if (entity.hasProperty(MODIFIED_TIMESTAMP)) {
            videoEntity.setModifiedTimestamp((Long) entity.getProperty(MODIFIED_TIMESTAMP));
        }
        return videoEntity;
    }

    public Entity toEntity() {
        Entity entity;

        if (getId() == null) {
            entity = new Entity(TYPE, PARENT_KEY);
        } else {
            entity = new Entity(TYPE, getId(), PARENT_KEY);
        }

        entity.setProperty(TITLE, getTitle());
        entity.setProperty(DESCRIPTION, getDescription());
        entity.setProperty(AGE_GROUP, getAgeGroup());
        entity.setProperty(SERIES_ID, getSeriesId());
        entity.setProperty(YOUTUBE_ID, getYoutubeId());
        entity.setProperty(YOUTUBE_URL, getYoutubeUrl());
        entity.setProperty(DURATION, getDuration());
        entity.setProperty(RATING, getRating());
        entity.setProperty(LANGUAGE, getLanguage());
        entity.setProperty(MODIFIED_TIMESTAMP, getModifiedTimestamp());

        return entity;
    }

    public String getYoutubeUrl() {
        return youtubeUrl;
    }

    // TODO: we probably don't need it because we have youtubeId
    public void setYoutubeUrl(String youtobeUrl) {
        this.youtubeUrl = youtobeUrl;
    }

    public long getModifiedTimestamp() {
        return modifiedTimestamp;
    }

    public void setModifiedTimestamp(long modifiedTimestamp) {
        this.modifiedTimestamp = modifiedTimestamp;
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
