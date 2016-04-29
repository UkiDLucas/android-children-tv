package pub.uki.sns;

import java.io.Serializable;

public class ShareSettings implements Serializable {

    public ShareSettings(String twitterCallbackUrl, String twitterConsumerKey, String twitterConsumerSecret,
                         String facebookCallbackUrl, String facebookAppId, String facebookAppSecret) {
        this.twitterCallbackUrl = twitterCallbackUrl;
        this.twitterConsumerKey = twitterConsumerKey;
        this.twitterConsumerSecret = twitterConsumerSecret;
        this.facebookCallbackUrl = facebookCallbackUrl;
        this.facebookAppId = facebookAppId;
        this.facebookAppSecret = facebookAppSecret;
    }

    private String applicationName = "";

    private String twitterCallbackUrl;
    private String twitterConsumerKey;
    private String twitterConsumerSecret;

    private String facebookCallbackUrl;
    private String facebookAppId;
    private String facebookAppSecret;

    public String getTwitterCallbackUrl() {
        return twitterCallbackUrl;
    }

    public void setTwitterCallbackUrl(String twitterCallbackUrl) {
        this.twitterCallbackUrl = twitterCallbackUrl;
    }

    public String getTwitterConsumerKey() {
        return twitterConsumerKey;
    }

    public void setTwitterConsumerKey(String twitterConsumerKey) {
        this.twitterConsumerKey = twitterConsumerKey;
    }

    public String getTwitterConsumerSecret() {
        return twitterConsumerSecret;
    }

    public void setTwitterConsumerSecret(String twitterConsumerSecret) {
        this.twitterConsumerSecret = twitterConsumerSecret;
    }

    public String getFacebookCallbackUrl() {
        return facebookCallbackUrl;
    }

    public void setFacebookCallbackUrl(String facebookCallbackUrl) {
        this.facebookCallbackUrl = facebookCallbackUrl;
    }

    public String getFacebookAppId() {
        return facebookAppId;
    }

    public void setFacebookAppId(String facebookAppId) {
        this.facebookAppId = facebookAppId;
    }

    public String getFacebookAppSecret() {
        return facebookAppSecret;
    }

    public void setFacebookAppSecret(String facebookAppSecret) {
        this.facebookAppSecret = facebookAppSecret;
    }

    public void setApplicationName(String applicationName) {
        this.applicationName = applicationName;
    }

    public String getApplicationName() {
        return applicationName;
    }

    public ShareSettings updateAppName(String applicationName) {
        this.applicationName = applicationName;
        return this;
    }
}
