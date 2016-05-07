package com.cyberwalkabout.childrentv.youtube;

import java.util.Comparator;

/**
 * TODO: JavaDoc
 */
public class VideoQualityComparator implements Comparator<VideoFileInfo> {
    private static final boolean PREFER_MP4 = true;

    private static final String QUALITY_SMALL = "small";
    private static final String QUALITY_MEDIUM = "medium";
    private static final String QUALITY_HD = "hd";

    private static final int QUALITY_SMALL_WEIGHT = 1;
    private static final int QUALITY_MEDIUM_WEIGHT = 2;
    private static final int QUALITY_HD_WEIGHT = 3;
    public static final String MP4 = "mp4";

    public VideoFileInfo selectBetterQuality(VideoFileInfo videoFileInfo1, VideoFileInfo videoFileInfo2) {
        int compare = compare(videoFileInfo1, videoFileInfo2);

        if (compare > 0) {
            return videoFileInfo1;
        } else {
            return videoFileInfo2;
        }
    }

    @Override
    public int compare(VideoFileInfo videoFileInfo1, VideoFileInfo videoFileInfo2) {
        if (videoFileInfo1.getQuality().equals(videoFileInfo2.getQuality())) {
            if (PREFER_MP4) {
                if (videoFileInfo2.getType().toLowerCase().contains(MP4)) {
                    return -1;
                } else {
                    return 1;
                }
            } else {
                return 0;
            }
        } else {
            int weight1 = getWeight(videoFileInfo1.getQuality());
            int weight2 = getWeight(videoFileInfo2.getQuality());

            return Integer.valueOf(weight1).compareTo(weight2);
        }
    }

    private int getWeight(String quality) {
        if (quality != null) {
            if (QUALITY_SMALL.equals(quality)) {
                return QUALITY_SMALL_WEIGHT;
            } else if (QUALITY_MEDIUM.equals(quality)) {
                return QUALITY_MEDIUM_WEIGHT;
            } else if (quality.contains(QUALITY_HD)) {
                String videoModeStr = quality.replace("hd", "");

                int videoMode = 0;
                try {
                    videoMode = Integer.parseInt(videoModeStr);
                } catch (NumberFormatException e) {
                }

                return QUALITY_HD_WEIGHT + videoMode;
            }
        }
        return -1;
    }

}
