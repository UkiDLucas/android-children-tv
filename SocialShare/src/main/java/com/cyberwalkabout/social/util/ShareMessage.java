package com.cyberwalkabout.social.util;

import java.io.Serializable;

public class ShareMessage implements Serializable {
    private CharSequence message;
    private String link;
    private String linkCaption;
    private String linkName;
    private String linkDescription;
    private String imageUrl;
    private String mimeType = "text/plain";

    public ShareMessage() {
    }

    public ShareMessage(CharSequence message) {
        this.message = message;
    }

    public ShareMessage(CharSequence message, String mimeType) {
        this.message = message;
        this.mimeType = mimeType;
    }

    public CharSequence getMessage() {
        return message;
    }

    public void setMessage(CharSequence message) {
        this.message = message;
    }

    public String getLink() {
        return link;
    }

    public void setLink(String link) {
        this.link = link;
    }

    public String getLinkCaption() {
        return linkCaption;
    }

    public void setLinkCaption(String linkCaption) {
        this.linkCaption = linkCaption;
    }

    public String getLinkName() {
        return linkName;
    }

    public void setLinkName(String linkName) {
        this.linkName = linkName;
    }

    public String getLinkDescription() {
        return linkDescription;
    }

    public void setLinkDescription(String linkDescription) {
        this.linkDescription = linkDescription;
    }

    public String getImageUrl() {
        return imageUrl;
    }

    public void setImageUrl(String imageUrl) {
        this.imageUrl = imageUrl;
    }

    public String getMimeType() {
        return mimeType;
    }

    public void setMimeType(String mimeType) {
        this.mimeType = mimeType;
    }
}
