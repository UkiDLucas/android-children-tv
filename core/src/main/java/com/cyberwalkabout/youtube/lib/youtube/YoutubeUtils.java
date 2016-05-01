package com.cyberwalkabout.youtube.lib.youtube;

/**
 * @author Andrii Kovalov
 */
public class YoutubeUtils {

    public static String getThumbnailUrl(String youtubeId) {
        return "http://i.ytimg.com/vi/" + youtubeId + "/default.jpg";
    }

    public static String getHighQualityImageUrl(String youtubeId) {
        return "http://i1.ytimg.com/vi/" + youtubeId + "/hqdefault.jpg";
    }

    public static String getMaxQualityImageUrl(String youtubeId) {
        return "http://i1.ytimg.com/vi/" + youtubeId + "/maxresdefault.jpg";
    }
}
