package com.cyberwalkabout.childrentv.youtube;

/**
 * TODO: JavaDoc
 */
public class VideoFileInfo {
    private String fallbackHost;
    private String quality;
    private String type;
    private String url;
    private String itag;

    public String getFallbackHost() {
        return fallbackHost;
    }

    public void setFallbackHost(String fallbackHost) {
        this.fallbackHost = fallbackHost;
    }

    public String getQuality() {
        return quality;
    }

    public void setQuality(String quality) {
        this.quality = quality;
    }

    public String getType() {
        return type;
    }

    public void setType(String type) {
        this.type = type;
    }

    public String getUrl() {
        return url;
    }

    public void setUrl(String url) {
        this.url = url;
    }

    public String getItag() {
        return itag;
    }

    public void setItag(String itag) {
        this.itag = itag;
    }

    @Override
    public String toString() {
        return "VideoFileInfo{" +
                "fallbackHost='" + fallbackHost + '\'' +
                ", quality='" + quality + '\'' +
                ", type='" + type + '\'' +
                ", url='" + url + '\'' +
                ", itag='" + itag + '\'' +
                '}';
    }
}
