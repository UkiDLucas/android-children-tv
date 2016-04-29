/*
   For step-by-step instructions on connecting your Android application to this backend module,
   see "App Engine Java Endpoints Module" template documentation at
   https://github.com/GoogleCloudPlatform/gradle-appengine-templates/tree/master/HelloEndpoints
*/

package com.cyberwalkabout.childrentv.backend.endpoint;

import com.cyberwalkabout.childrentv.backend.model.VideoEntity;
import com.google.api.server.spi.config.Api;
import com.google.api.server.spi.config.ApiMethod;
import com.google.api.server.spi.config.ApiNamespace;
import com.google.appengine.api.datastore.DatastoreService;
import com.google.appengine.api.datastore.DatastoreServiceFactory;
import com.google.appengine.api.datastore.Entity;
import com.google.appengine.api.datastore.FetchOptions;
import com.google.appengine.api.datastore.Query;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.lang.ref.SoftReference;
import java.util.ArrayList;
import java.util.List;
import java.util.concurrent.TimeUnit;

/**
 * @author Andrii Kovalov
 */
@Api(name = "videoApi", version = "v1", namespace = @ApiNamespace(ownerDomain = "backend.childrentv.cyberwalkabout.com", ownerName = "backend.childrentv.cyberwalkabout.com", packagePath = ""))
public class VideoEndpoint {
    public static final Logger LOG = LoggerFactory.getLogger(VideoEndpoint.class);

    public static final long CACHE_EXPIRATION_PERIOD = TimeUnit.HOURS.toMillis(6);

    // cache video entities in memory while
    private SoftReference<List<VideoEntity>> videoListRef;
    private long cacheTimestamp;

    @ApiMethod(name = "getVideos")
    public List<VideoEntity> getVideos() {
        LOG.info("Get videos");

        List<VideoEntity> videoList;
        if (videoListRef == null || videoListRef.get() == null) {
            videoList = queryAllVideos();
            videoListRef = new SoftReference<>(videoList);
            cacheTimestamp = System.currentTimeMillis();
        } else {
            if (System.currentTimeMillis() - cacheTimestamp >= CACHE_EXPIRATION_PERIOD) {
                LOG.info("Cached videos expired due to " + TimeUnit.MILLISECONDS.toHours(CACHE_EXPIRATION_PERIOD) + "h");
                videoList = queryAllVideos();
                videoListRef = new SoftReference<>(videoList);
                cacheTimestamp = System.currentTimeMillis();
            } else {
                LOG.info("Fetch cached videos");
                videoList = videoListRef.get();
            }
        }

        LOG.info("Returning " + (videoList == null ? 0 : videoList.size()) + " videos");

        return videoList;
    }

    private List<VideoEntity> queryAllVideos() {
        LOG.info("Query videos from datastore");
        DatastoreService datastoreService = DatastoreServiceFactory.getDatastoreService();
        Query query = new Query(VideoEntity.PARENT_KEY);
        List<Entity> results = datastoreService.prepare(query).asList(FetchOptions.Builder.withDefaults());

        List<VideoEntity> videoEntities = new ArrayList<>();
        for (Entity entity : results) {
            VideoEntity videoEntity = VideoEntity.toVideo(entity);
            videoEntities.add(videoEntity);
        }

        return videoEntities;
    }
}
