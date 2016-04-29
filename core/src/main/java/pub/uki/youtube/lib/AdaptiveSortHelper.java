package com.cyberwalkabout.youtube.lib;

/**
 * @author Andrii Kovalov
 *         <p/>
 *         Sort order:
 *         The decision is to implement adaptive sort order which depends on local rating of the video.
 *         Rating rules:
 *         - user click on video +1
 *         - user fully watched video + 2 (more then 50% of the video length)
 *         - user skip video -3 (less then 50% of the video length)
 */
public class AdaptiveSortHelper {
    public static final int RATING_DELTA_SKIP_VIDEO = -3;
    public static final int RATING_DELTA_VIDEO_FULLY_WATCHED = +2;
    public static final int RATING_DELTA_VIDEO_SELECTED = +1;
}
