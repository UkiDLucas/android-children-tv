package pub.uki.sns;

import android.app.Activity;
import android.content.Context;
import android.content.Intent;
import android.content.SharedPreferences;
import android.net.Uri;
import android.util.Log;
import android.widget.Toast;
import com.chicagoandroid.sns.util.Extras;
import com.chicagoandroid.sns.util.HttpHelper;
import com.chicagoandroid.sns.util.ShareMessage;
import com.chicagoandroid.sns.util.ShareMessageBundle;
import oauth.signpost.commonshttp.CommonsHttpOAuthConsumer;
import org.apache.http.NameValuePair;
import org.apache.http.client.entity.UrlEncodedFormEntity;
import org.apache.http.client.methods.HttpPost;
import org.apache.http.message.BasicNameValuePair;
import org.apache.http.params.BasicHttpParams;
import org.apache.http.params.HttpParams;
import org.apache.http.params.HttpProtocolParams;
import org.apache.http.protocol.HTTP;
import org.json.JSONException;
import org.json.JSONObject;

import java.util.ArrayList;
import java.util.LinkedList;
import java.util.List;

/**
 * share method is enter point
 *
 * @author sgolub
 */
public final class SNServices {

    private static final String TAG = SNServices.class.getSimpleName();

    public static final String PREFS_NAME = "sns_prefs";

    private static final String TWITTER_FILE = "twitter_settings";

    private static final String TOKEN_SECRET = "token_secret";

    private static final String TOKEN = "token";

    // Prefs settings
    public static final String TWITTER_CALLBACK_URL = "tw_callback_url";

    public static final String TWITTER_CONSUMER_KEY = "tw_consumer_key";

    public static final String TWITTER_CONSUMER_SECRET = "tw_consumer_secret";

    public static final String FACEBOOK_CALLBACK_URL = "fb_callback_url";

    public static final String FACEBOOK_APP_ID = "fb_app_id";

    public static final String FACEBOOK_APP_SECRET = "fb_app_secret";

    private static final String FACEBOOK_ACCESS_TOKEN = "fb_access_token";

    private static final String FACEBOOK_ACCESS_TOKEN_EXPIRES = "fb_access_token_expire";

    private Activity mActivity;

    public static String appName;

    public static FacebookOAuthHandler facebookOAuthHandler;

    private static String mMsg;

    private String mImgUrl;

    private String mLink;

    private String mCaption;

    private String mLinkName;

    private String mLinkDescription;

    public static TwitterOAuthHandler twitterOAuthHandler;

    public SNServices(String appName) {
        SNServices.appName = appName;
    }

    public void postToWallTwitter(String msg, final Activity activity) {
        mActivity = activity;
        mMsg = msg;
        twitterOAuthHandler = new TwitterOAuthHandler();

        SharedPreferences prefs = activity.getSharedPreferences(TWITTER_FILE, Activity.MODE_PRIVATE);
        final String token = prefs.getString(TOKEN, null);
        final String tokenSecret = prefs.getString(TOKEN_SECRET, null);
        if (token != null && tokenSecret != null) {
            new Thread() {
                public void run() {
                    try {
                        postTweet(token, tokenSecret);
                    } catch (Exception e) {
                        // something wrong try to reauthorize
                        mActivity.startActivity(new Intent(activity, TwitterOAuthActivity.class));
                    }
                }

                ;
            }.start();
        } else {
            activity.startActivity(new Intent(activity, TwitterOAuthActivity.class));
        }
    }

    private void postTweet(String token, String tokenSecret) throws Exception {

        HttpPost post = new HttpPost("https://api.twitter.com/1.1/statuses/update.json");
        LinkedList<BasicNameValuePair> out = new LinkedList<BasicNameValuePair>();
        out.add(new BasicNameValuePair("status", mMsg));
        post.setEntity(new UrlEncodedFormEntity(out, HTTP.UTF_8));
        // Tweak further as needed for your app
        HttpParams params = new BasicHttpParams();
        // set this to false, or else you'll get an Expectation Failed: error
        HttpProtocolParams.setUseExpectContinue(params, false);
        post.setParams(params);
        // sign the request to authenticate
        SharedPreferences prefs = mActivity.getSharedPreferences(PREFS_NAME, Context.MODE_PRIVATE);
        String consumerKey = prefs.getString(TWITTER_CONSUMER_KEY, "");
        String consumerSecret = prefs.getString(TWITTER_CONSUMER_SECRET, "");
        CommonsHttpOAuthConsumer consumer = new CommonsHttpOAuthConsumer(consumerKey, consumerSecret);
        consumer.setTokenWithSecret(token, tokenSecret);
        consumer.sign(post);
        new HttpHelper().doPost(post, mActivity);

        mActivity.runOnUiThread(new Runnable() {
            @Override
            public void run() {
                Toast.makeText(mActivity, "Twitter: " + mMsg, Toast.LENGTH_LONG).show();
            }
        });
    }

    protected class TwitterOAuthHandler {
        public void handle(String token, String tokenSecret) {
            SharedPreferences prefs = mActivity.getSharedPreferences(TWITTER_FILE, Activity.MODE_PRIVATE);
            prefs.edit().putString(TOKEN, token).putString(TOKEN_SECRET, tokenSecret).commit();
            try {
                postTweet(token, tokenSecret);
            } catch (Exception e) {
                // TODO handle
            }
        }
    }

    protected void updateFacebookStatus(ShareMessage msg, String message, Activity activity) {
        mActivity = activity;
        mMsg = message;
        mImgUrl = msg.getImageUrl();
        mLink = msg.getLink();
        mCaption = msg.getLinkCaption();
        mLinkName = msg.getLinkName();
        mLinkDescription = msg.getLinkDescription();

        updateFacebookStatus();
    }

    protected void updateFacebookStatus(String msg, Activity activity) {
        mActivity = activity;
        mMsg = msg;

        updateFacebookStatus();
    }

    private void updateFacebookStatus() {
        facebookOAuthHandler = new FacebookOAuthHandler();

        SharedPreferences prefs = mActivity.getSharedPreferences(PREFS_NAME, Activity.MODE_PRIVATE);
        final String accessToken = prefs.getString(FACEBOOK_ACCESS_TOKEN, null);
        final long expires = prefs.getLong(FACEBOOK_ACCESS_TOKEN_EXPIRES, 0);
        if (accessToken != null) {
            new Thread() {
                public void run() {
                    try {
                        if (isSessionExpired(expires)) {
                            Log.d(TAG, " facebook session expired");
                            refreshFacebookToken();
                        }
                        updateFacebookStatus(accessToken);
                    } catch (Exception e) {
                        // something wrong try to reauthorize
                        mActivity.startActivity(new Intent(mActivity, FacebookOAuthActivity.class));
                    }
                }
            }.start();
        } else {
            mActivity.startActivity(new Intent(mActivity, FacebookOAuthActivity.class));
        }
    }

    private boolean isSessionExpired(long expires) {
        return System.currentTimeMillis() < expires;
    }

    private String refreshFacebookToken() {
        SharedPreferences prefs = mActivity.getSharedPreferences(PREFS_NAME, Activity.MODE_PRIVATE);
        String app_ID = prefs.getString(FACEBOOK_APP_ID, "");
        String clientSecret = prefs.getString(FACEBOOK_APP_SECRET, "");
        String token = prefs.getString(FACEBOOK_ACCESS_TOKEN, "");
        String url = "https://graph.facebook.com/oauth/access_token?client_id=" + app_ID + "&client_secret=" + clientSecret + "&grant_type=fb_exchange_token&fb_exchange_token=" + token;

        String response = new HttpHelper().getPage(url, mActivity);
        String newToken = response.substring("access_token=".length(), response.indexOf("&expires="));
        prefs.edit().putString(FACEBOOK_ACCESS_TOKEN, newToken);
        return newToken;
    }

    private void updateFacebookStatus(String accessToken) throws Exception {
        HttpPost httpost = new HttpPost("https://graph.facebook.com/me/feed");

        List<NameValuePair> nvps = new ArrayList<NameValuePair>();
        nvps.add(new BasicNameValuePair("access_token", accessToken));
        nvps.add(new BasicNameValuePair("message", mMsg));
        if (mLink != null) {
            nvps.add(new BasicNameValuePair("link", mLink));
        }
        if (mCaption != null) {
            nvps.add(new BasicNameValuePair("caption", mCaption));
        }
        if (mImgUrl != null) {
            nvps.add(new BasicNameValuePair("picture", mImgUrl));
        }
        if (mLinkName != null) {
            nvps.add(new BasicNameValuePair("name", mLinkName));
        }
        if (mLinkDescription != null) {
            nvps.add(new BasicNameValuePair("description", mLinkDescription));
        }
        httpost.setEntity(new UrlEncodedFormEntity(nvps));

        String response = new HttpHelper().doPost(httpost, mActivity);
        Log.d("FacebookUpdateResult", response);
        if (isFacebookResponseSuccessful(response)) {
            mActivity.runOnUiThread(new Runnable() {
                @Override
                public void run() {
                    Toast.makeText(mActivity, "Facebook: " + mMsg, Toast.LENGTH_LONG).show();
                }
            });
        } else {
            FacebookError facebookError = parseFacebookError(response);
            if (facebookError.getErrorType().equals("OAuthException")) {
                Toast.makeText(mActivity, "Your access token seems to be corrupted, relogin please.", Toast.LENGTH_SHORT).show();
                throw new Exception();
            }
        }

    }

    private boolean isFacebookResponseSuccessful(String response) {
        return !(response.indexOf("error") > -1);
    }

    private FacebookError parseFacebookError(String response) {
        JSONObject responseJson = new JSONObject();
        FacebookError facebookError;
        try {
            facebookError = new FacebookError(responseJson.getJSONObject("error"));
        } catch (JSONException e) {
            facebookError = new FacebookError(e.getMessage());
        }
        return facebookError;
    }

    protected class FacebookOAuthHandler {

        public void handle(String accessToken, long accessExpires) {
            SharedPreferences prefs = mActivity.getSharedPreferences(PREFS_NAME, Activity.MODE_PRIVATE);
            prefs.edit().putString(FACEBOOK_ACCESS_TOKEN, accessToken).commit();
            prefs.edit().putLong(FACEBOOK_ACCESS_TOKEN_EXPIRES, accessExpires);
            Log.d("AccessToken", accessToken);
            try {
                updateFacebookStatus(accessToken);
            } catch (Exception e) {
                // TODO handle
            }
        }

    }

    public static void sendEmail(Context ctx, String subject, String message, String to) {
        Intent emailIntent = new Intent(Intent.ACTION_SENDTO);
        emailIntent.setFlags(Intent.FLAG_ACTIVITY_CLEAR_TOP);
        if (to == null) {
            to = "";
        }
        emailIntent.setData(Uri.parse("mailto:" + to));
        emailIntent.putExtra(Intent.EXTRA_SUBJECT, subject);
        emailIntent.putExtra(Intent.EXTRA_TEXT, message);

        ctx.startActivity(Intent.createChooser(emailIntent, "Send mail..."));
    }

    public static void sendEmail(Context ctx, String subject, String message, String to, ArrayList<Uri> attachments) {
        final Intent emailIntent = new Intent(Intent.ACTION_SEND_MULTIPLE);
        emailIntent.setFlags(Intent.FLAG_ACTIVITY_CLEAR_TOP);
        if (to == null) {
            to = "";
        }
        emailIntent.setData(Uri.parse("mailto:" + to));
        emailIntent.putExtra(Intent.EXTRA_SUBJECT, subject);
        emailIntent.putExtra(Intent.EXTRA_TEXT, message);
        emailIntent.putParcelableArrayListExtra(Intent.EXTRA_STREAM, attachments);
        ctx.startActivity(Intent.createChooser(emailIntent, "Send mail..."));
    }

    public static void sendSMS(String message, Activity activity) {
        Intent sendSms = new Intent(Intent.ACTION_VIEW);
        sendSms.setType("vnd.android-dir/mms-sms");
        sendSms.putExtra("sms_body", message);
        activity.startActivity(sendSms);
    }

    public static void shareToFacebook(Context ctx, String message) {
        Intent facebookIntent = new Intent();
        facebookIntent.setClass(ctx, ShareActivity.class);
        facebookIntent.putExtra(ShareActivity.EXTRA_SHARE_TYPE, ShareActivity.EXTRA_SHARE_FACEBOOK);
        facebookIntent.putExtra(Intent.EXTRA_TEXT, message);
        ctx.startActivity(facebookIntent);
    }

    public static void shareToTwitter(Context ctx, String message) {
        Intent twitterIntent = new Intent();
        twitterIntent.setClass(ctx, ShareActivity.class);
        twitterIntent.putExtra(ShareActivity.EXTRA_SHARE_TYPE, ShareActivity.EXTRA_SHARE_TWITTER);
        twitterIntent.putExtra(Intent.EXTRA_TEXT, message);
        ctx.startActivity(twitterIntent);
    }

    public static void share(String subject, String text, Uri uri, Context context, ShareSettings settings) {
        Intent intent = new Intent(context, ShareListActivity.class);

        intent.setType("text/plain");
        intent.putExtra(Intent.EXTRA_SUBJECT, subject);
        intent.putExtra(Intent.EXTRA_TEXT, text);
        intent.putExtra(Intent.EXTRA_STREAM, uri);

        saveSettings(settings, context);

        context.startActivity(intent);
    }

    public static void share(Context context, String subject, ShareMessageBundle messages, ShareSettings settings) {
        Intent intent = new Intent(context, ShareListActivity.class);

        intent.setType("text/plain");
        intent.putExtra(Intent.EXTRA_SUBJECT, subject);
        intent.putExtra(Extras.EXTRA_MESSAGE_BUNDLE, messages);
        intent.setFlags(intent.getFlags() | Intent.FLAG_ACTIVITY_NEW_TASK);

        saveSettings(settings, context);

        context.startActivity(intent);
    }

    public static void saveSettings(ShareSettings settings, Context context) {
        SNServices.appName = settings.getApplicationName();

        SharedPreferences prefs = context.getSharedPreferences(PREFS_NAME, Context.MODE_PRIVATE);
        prefs.edit().putString(TWITTER_CALLBACK_URL, settings.getTwitterCallbackUrl()).putString(TWITTER_CONSUMER_KEY, settings.getTwitterConsumerKey()).putString(TWITTER_CONSUMER_SECRET, settings.getTwitterConsumerSecret()).putString(FACEBOOK_CALLBACK_URL, settings.getFacebookCallbackUrl()).putString(FACEBOOK_APP_ID, settings.getFacebookAppId()).putString(FACEBOOK_APP_SECRET, settings.getFacebookAppSecret()).commit();
    }
}
