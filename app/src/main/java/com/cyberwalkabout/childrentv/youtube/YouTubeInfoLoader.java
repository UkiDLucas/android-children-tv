package com.cyberwalkabout.childrentv.youtube;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.io.ByteArrayOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.io.UnsupportedEncodingException;
import java.net.HttpURLConnection;
import java.net.URL;
import java.net.URLDecoder;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

/**
 * TODO: JavaDoc
 */
public class YouTubeInfoLoader {
    private static final Logger LOG = LoggerFactory.getLogger(YouTubeInfoLoader.class);

    private static final int READ_TIMEOUT = 10000;
    private static final int CONNECT_TIMEOUT = 15000;

    private static final String URL_YOUTUBE_VIDEO_INFO = "http://www.youtube.com/get_video_info?video_id=%s";
    private static final String PARAM_URL_ENCODED_FMT_STREAM_MAP = "url_encoded_fmt_stream_map";
    private static final String PARAM_FALLBACK_HOST = "fallback_host";
    private static final String PARAM_QUALITY = "quality";
    private static final String PARAM_TYPE = "type";
    private static final String PARAM_URL = "url";
    private static final String PARAM_ITAG = "itag";

    private VideoQualityComparator videoQualityComparator = new VideoQualityComparator();

    public VideoFileInfo fetchBestQualityMetaData(String videoId) {
        List<VideoFileInfo> videoFileInfoList = fetchMetaData(videoId);
        return selectBestQualityVideo(videoFileInfoList);
    }

    public List<VideoFileInfo> fetchMetaData(String videoId) {
        List<VideoFileInfo> metaDataList = new ArrayList<VideoFileInfo>();

        Map<String, String> videoInfoMap = getVideoInfo(videoId);

        if (videoInfoMap.containsKey(PARAM_URL_ENCODED_FMT_STREAM_MAP)) {

            String fmtUrlMap = videoInfoMap.get(PARAM_URL_ENCODED_FMT_STREAM_MAP);

            for (String videoInfoStr : fmtUrlMap.split(",")) {
                Map<String, String> params = paramsStrToMap(videoInfoStr);

                VideoFileInfo metaData = new VideoFileInfo();
                metaData.setFallbackHost(params.get(PARAM_FALLBACK_HOST));
                metaData.setQuality(params.get(PARAM_QUALITY));
                metaData.setType(params.get(PARAM_TYPE));

                metaData.setUrl(params.get(PARAM_URL));
                metaData.setItag(params.get(PARAM_ITAG));

                metaDataList.add(metaData);
            }
        }

        return metaDataList;
    }

    public VideoFileInfo selectBestQualityVideo(List<VideoFileInfo> videoFileInfoList) {
        VideoFileInfo bestVideoFile = null;

        for (VideoFileInfo videoFileInfo : videoFileInfoList) {
            if (bestVideoFile != null) {
                bestVideoFile = videoQualityComparator.selectBetterQuality(bestVideoFile, videoFileInfo);
            } else {
                bestVideoFile = videoFileInfo;
            }
        }

        return bestVideoFile;
    }

    public Map<String, String> getVideoInfo(String videoId) {
        LOG.info("Get youtube video info for id: " + videoId);

        try {
            URL url = new URL(String.format(URL_YOUTUBE_VIDEO_INFO, videoId));

            HttpURLConnection urlConnection = (HttpURLConnection) url.openConnection();
            urlConnection.setReadTimeout(READ_TIMEOUT);
            urlConnection.setConnectTimeout(CONNECT_TIMEOUT);
            urlConnection.setRequestMethod("GET");
            urlConnection.setDoInput(true);

            urlConnection.connect();

            int response = urlConnection.getResponseCode();

            LOG.info("HTTP response code " + response);

            if (response == HttpURLConnection.HTTP_OK) {
                InputStream in = null;
                try {
                    in = urlConnection.getInputStream();

                    ByteArrayOutputStream out = new ByteArrayOutputStream();

                    int length;
                    byte[] buffer = new byte[1024];
                    while ((length = in.read(buffer)) != -1) {
                        out.write(buffer, 0, length);
                    }

                    String videoInfo = new String(out.toByteArray());

                    LOG.debug("HTTP response content: " + videoInfo);

                    return paramsStrToMap(videoInfo);
                } catch (IOException e) {
                    LOG.error(e.getMessage(), e);
                } finally {
                    if (in != null) {
                        in.close();
                    }
                }
            }
        } catch (Exception e) {
            LOG.error(e.getMessage(), e);
        }
        return null;
    }

    private Map<String, String> paramsStrToMap(String paramsStr) {
        Map<String, String> params = new HashMap<String, String>();
        for (String param : paramsStr.split("&")) {
            String[] pair = param.split("=");
            String key = null;
            try {
                key = URLDecoder.decode(pair[0], "UTF-8");
            } catch (UnsupportedEncodingException e) {
                LOG.warn("Couldn't decode key", e);
            }
            String value = "";
            if (pair.length > 1) {
                try {
                    value = URLDecoder.decode(pair[1], "UTF-8");
                } catch (UnsupportedEncodingException e) {
                    LOG.warn("Couldn't decode value", e);
                }
            }

            params.put(key, value);
        }
        return params;
    }

    public static void main(String[] args) {
        String videoId = "wtLJPvx7-ys";
        System.out.println("Video: " + videoId);

        YouTubeInfoLoader youtube = new YouTubeInfoLoader();
        VideoFileInfo videoFileInfo = youtube.fetchBestQualityMetaData(videoId);
        System.out.println(videoFileInfo);
    }
}
