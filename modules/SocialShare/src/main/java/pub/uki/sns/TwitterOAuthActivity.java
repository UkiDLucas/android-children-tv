
package pub.uki.sns;

import android.app.Activity;
import android.content.SharedPreferences;
import android.graphics.Bitmap;
import android.net.Uri;
import android.os.Bundle;
import android.os.Handler;
import android.os.Message;
import android.util.Log;
import android.view.Window;
import android.webkit.WebChromeClient;
import android.webkit.WebView;
import android.webkit.WebViewClient;
import android.widget.Toast;
import com.chicagoandroid.sns.util.HttpHelper;
import oauth.signpost.basic.DefaultOAuthConsumer;
import oauth.signpost.basic.DefaultOAuthProvider;
import oauth.signpost.commonshttp.CommonsHttpOAuthConsumer;
import oauth.signpost.commonshttp.CommonsHttpOAuthProvider;
import oauth.signpost.exception.OAuthCommunicationException;

import java.net.SocketException;
import java.net.UnknownHostException;

public class TwitterOAuthActivity extends Activity {

    private WebView mOAuthWebView;

    private CommonsHttpOAuthConsumer mTwitterConsumer;

    private CommonsHttpOAuthProvider mTwitterProvider;

    private String mAuthUrl;

    private String mCallbackUrl;

    private String mConsumerKey;

    private String mConsumerSecret;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

        SharedPreferences prefs = getSharedPreferences(SNServices.PREFS_NAME, MODE_PRIVATE);
        mCallbackUrl = prefs.getString(SNServices.TWITTER_CALLBACK_URL, "");
        mConsumerKey = prefs.getString(SNServices.TWITTER_CONSUMER_KEY, "");
        mConsumerSecret = prefs.getString(SNServices.TWITTER_CONSUMER_SECRET, "");

        mOAuthWebView = new WebView(this);
        requestWindowFeature(Window.FEATURE_PROGRESS);
        getWindow().setFeatureInt(Window.FEATURE_PROGRESS, Window.PROGRESS_VISIBILITY_ON);
        mOAuthWebView.setWebChromeClient(new WebChromeClient() {
            public void onProgressChanged(WebView view, int progress) {
                // Make the bar disappear after URL is loaded, and changes
                // string to Loading...
                TwitterOAuthActivity.this.setTitle(R.string.message_loading);
                // TODO move string to resources
                TwitterOAuthActivity.this.setProgress(progress * 100);

                // Return the app name after finish loading
                if (progress == 100)
                    TwitterOAuthActivity.this.setTitle(SNServices.appName);
            }
        });
        mOAuthWebView.setWebViewClient(new WebViewClient() {

            @Override
            public void onPageStarted(WebView view, String url, Bitmap favicon) {
                Log.i("OnPageStarted", url);
                if (url.startsWith(mCallbackUrl)) {
                    Uri uri = Uri.parse(url);
                    String verifier = uri.getQueryParameter(oauth.signpost.OAuth.OAUTH_VERIFIER);
                    new RetriveAccessTokenInBackground(verifier).start();
                    finish();
                }
                ///else{
                // view.loadUrl(url);
                // }
            }

          /*  @Override
            public boolean shouldOverrideUrlLoading(WebView view, String url) {
            	Log.i("shouldOverrideUrlLoading", url);
                if (url.startsWith(mCallbackUrl)) {
                    Uri uri = Uri.parse(url);
                    String verifier = uri.getQueryParameter(oauth.signpost.OAuth.OAUTH_VERIFIER);
                    new RetriveAccessTokenInBackground(verifier).start();
                    finish();
                }
                view.loadUrl(url);
                return true;
            }*/
        });
        setContentView(mOAuthWebView);
        mTwitterConsumer = new CommonsHttpOAuthConsumer(mConsumerKey, mConsumerSecret);
        mTwitterProvider = new CommonsHttpOAuthProvider("https://api.twitter.com/oauth/request_token",
                "https://api.twitter.com/oauth/access_token",
                "https://api.twitter.com/oauth/authorize");

    }

    @Override
    protected void onPostCreate(Bundle savedInstanceState) {
        super.onPostCreate(savedInstanceState);

        final int error = 1;

        final Handler handler = new Handler() {
            @Override
            public void handleMessage(Message msg) {
                if (msg.what == error) {
                    finish();
                } else {
                    mOAuthWebView.loadUrl(mAuthUrl);
                    Toast.makeText(TwitterOAuthActivity.this, R.string.message_please_authorize_app,
                            Toast.LENGTH_LONG).show();
                }
            }
        };

        new Thread() {

            public void run() {
                try {
                    mAuthUrl = mTwitterProvider.retrieveRequestToken(mTwitterConsumer, mCallbackUrl);
                    handler.sendEmptyMessage(0);
                } catch (Exception e1) {
                    if (e1 instanceof SocketException || e1 instanceof UnknownHostException || e1 instanceof OAuthCommunicationException) {
                        HttpHelper.showInternetConnectionError(TwitterOAuthActivity.this);
                    }
                    handler.sendEmptyMessage(error);
                }
            };
        }.start();
    }

    private final class RetriveAccessTokenInBackground extends Thread {
        private final String verifier;

        private RetriveAccessTokenInBackground(String verifier) {
            this.verifier = verifier;
        }

        public void run() {
            try {
                mTwitterProvider.retrieveAccessToken(mTwitterConsumer, verifier);
            } catch (Exception e) {
                finish();
            }
            SNServices.twitterOAuthHandler.handle(mTwitterConsumer.getToken(),
                    mTwitterConsumer.getTokenSecret());
        }
    }

}
