package pub.uki.sns;

import android.app.Activity;
import android.content.SharedPreferences;
import android.graphics.Bitmap;
import android.net.Uri;
import android.os.Bundle;
import android.text.TextUtils;
import android.util.Log;
import android.view.Window;
import android.webkit.*;
import com.chicagoandroid.sns.util.HttpHelper;

import java.net.URLEncoder;

public class FacebookOAuthActivity extends Activity {

    private WebView mOAuthWebView;

    private String mCallbackUrl;

    private String mAppId;

    private String mAppSecret;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

        SharedPreferences prefs = getSharedPreferences(SNServices.PREFS_NAME, MODE_PRIVATE);
        mCallbackUrl = prefs.getString(SNServices.FACEBOOK_CALLBACK_URL, "");
        mAppId = prefs.getString(SNServices.FACEBOOK_APP_ID, "");
        mAppSecret = prefs.getString(SNServices.FACEBOOK_APP_SECRET, "");

        mOAuthWebView = new WebView(this);
        mOAuthWebView.getSettings().setJavaScriptEnabled(true);
        CookieSyncManager.createInstance(FacebookOAuthActivity.this);
        CookieManager.getInstance().removeAllCookie();
        requestWindowFeature(Window.FEATURE_PROGRESS);
        getWindow().setFeatureInt(Window.FEATURE_PROGRESS, Window.PROGRESS_VISIBILITY_ON);

        mOAuthWebView.setWebChromeClient(new WebChromeClient() {
            public void onProgressChanged(WebView view, int progress) {
                // Make the bar disappear after URL is loaded, and changes
                // string to Loading...
                FacebookOAuthActivity.this.setTitle("Loading...");
                // TODO move string to resources
                FacebookOAuthActivity.this.setProgress(progress * 100);

                // Return the app name after finish loading
                if (progress == 100) FacebookOAuthActivity.this.setTitle(SNServices.appName);
            }
        });

        mOAuthWebView.setWebViewClient(new WebViewClient() {
            @Override
            public boolean shouldOverrideUrlLoading(WebView view, String url) {
                view.loadUrl(url);
                return true;
            }

            @Override
            public void onPageStarted(final WebView webView, String url, Bitmap favicon) {
                Log.d("PageStarted", url);
                if (url.startsWith(mCallbackUrl)) {
                    Uri uri = Uri.parse(url);
                    String code = uri.getQueryParameter("code");
                    Log.d("facebook code", code == null ? "no code" : code);

                    final StringBuilder accessTokenRequestUrl = new StringBuilder("https://graph.facebook.com/oauth/access_token?client_id=");
                    accessTokenRequestUrl.append(mAppId + "&" + "redirect_uri=" + mCallbackUrl + "&" + "client_secret=" + mAppSecret);
                    if (!TextUtils.isEmpty(code)) {
                        accessTokenRequestUrl.append("&code=" + URLEncoder.encode(code));
                    }
                    Log.d("Access token request url", accessTokenRequestUrl.toString());
                    new Thread() {
                        public void run() {
                            String accessTokenPage = new HttpHelper().getPage(accessTokenRequestUrl.toString(), FacebookOAuthActivity.this);
                            Log.d("access token page", accessTokenPage);
                            if (accessTokenPage.length() > 0) {
                                // TODO: review, what if something different will be here instead of &expires
                                String accessToken = accessTokenPage.substring("access_token=".length(), accessTokenPage.indexOf("&expires="));
                                String expiresIn = accessTokenPage.substring(accessTokenPage.indexOf("&expires=") + "&expires=".length(), accessTokenPage.length());

                                long expires = System.currentTimeMillis() + Integer.parseInt(expiresIn) * 1000;
                                if (accessToken != null) {
                                    SNServices.facebookOAuthHandler.handle(accessToken, expires);
                                    webView.stopLoading();
                                    finish();
                                } else {
                                    // TODO handle error error_reason in uri??
                                    finish();
                                    Log.d("AccessError", accessTokenPage);
                                }
                            }
                        }
                    }.start();
                }
                super.onPageStarted(webView, url, favicon);

            }

            @Override
            public void onPageFinished(WebView view, String url) {
                Log.d("PageFinished", url);
                super.onPageFinished(view, url);
            }
        });
        setContentView(mOAuthWebView);
    }

    @Override
    protected void onPostCreate(Bundle savedInstanceState) {
        super.onPostCreate(savedInstanceState);

        String codeRequestUrl = "http://graph.facebook.com/oauth/authorize?" + "client_id=" + mAppId + "&" + "display=touch&redirect_uri=" + mCallbackUrl + "&" + "scope=offline_access,publish_stream,read_stream";
        mOAuthWebView.loadUrl(codeRequestUrl);
    }
}
