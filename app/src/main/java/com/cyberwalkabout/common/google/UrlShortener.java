package com.cyberwalkabout.common.google;

import android.app.Activity;
import android.app.ProgressDialog;
import android.content.Context;
import android.os.AsyncTask;
import android.text.TextUtils;
import android.util.Log;

import com.cyberwalkabout.common.http.HttpUtils;
import com.cyberwalkabout.common.util.Cache;
import com.cyberwalkabout.common.util.Env;

import org.apache.http.client.HttpClient;
import org.apache.http.client.ResponseHandler;
import org.apache.http.client.methods.HttpPost;
import org.apache.http.entity.StringEntity;
import org.apache.http.impl.client.BasicResponseHandler;
import org.json.JSONObject;

import java.util.Map;

// TODO: consider to store shortened urls to db
public class UrlShortener {
    private static final String TAG = UrlShortener.class.getSimpleName();
    private static final String GOOGLE_URL_SHORTENER_API = "https://www.googleapis.com/urlshortener/v1/url";
    private static HttpClient httpClient;
    private static Map<String, String> cache = new Cache<String, String>(100, 14400000);

    private UrlShortener() {
        httpClient = HttpUtils.createHttpClient();
    }

    public static UrlShortener instance() {
        return SingletonHolder.instance;
    }

    public void tryShorten(Context ctx, final String longUrl, final OnUrlListener listener) {
        tryShorten(ctx, longUrl, listener, true);
    }

    public void tryShorten(final String longUrl, final OnUrlListener listener) {
        tryShorten(null, longUrl, listener, true);
    }

    public void tryShorten(final String longUrl, final OnUrlListener listener, final boolean showProgress) {
        tryShorten(null, longUrl, listener, showProgress);
    }

    @SuppressWarnings("unchecked")
    public void tryShorten(final Context ctx, final String longUrl, final OnUrlListener listener, final boolean showProgress) {
        if (cache.containsKey(longUrl)) {
            listener.onUrl(true, cache.get(longUrl));
        } else if (ctx != null && !Env.isConnected(ctx)) {
            listener.onUrl(false, longUrl);
        } else {
            new AsyncTask<Void, Void, String>() {
                private ProgressDialog dialog;

                @Override
                protected void onPreExecute() {
                    if (showProgress && ctx != null && ctx instanceof Activity) {
                        dialog = ProgressDialog.show(ctx, null, "Please wait...");
                    }
                }

                @Override
                protected String doInBackground(Void... voids) {
                    return shorten(longUrl);
                }

                @Override
                protected void onPostExecute(String shortUrl) {
                    if (dialog != null) {
                        try {
                            dialog.dismiss();
                        } catch (Exception e) {
//                            ignore
                        }
                    }
                    if (shortUrl != null) {
                        cache.put(longUrl, shortUrl);
                        listener.onUrl(true, shortUrl);
                    } else {
                        listener.onUrl(false, longUrl);
                    }
                }
            }.execute();
        }
    }

    protected String shorten(String longUrl) {
        String result = null;
        HttpPost post = new HttpPost(GOOGLE_URL_SHORTENER_API);
        post.setHeader("Accept", "application/json");
        post.setHeader("Content-type", "application/json; charset=utf-8");
        try {
            JSONObject request = new JSONObject();
            request.put("longUrl", longUrl);
            post.setEntity(new StringEntity(request.toString(), "UTF-8"));
            ResponseHandler<String> handler = new BasicResponseHandler();
            String response = httpClient.execute(post, handler);
            if (!TextUtils.isEmpty(response)) {
                JSONObject responseObj = new JSONObject(response);
                result = (String) responseObj.get("id");
            }
        } catch (Exception e) {
            Log.e(TAG, e.getMessage(), e);
        }
        return result;
    }

    public interface OnUrlListener {
        void onUrl(boolean success, String url);
    }

    private static class SingletonHolder {
        public static final UrlShortener instance = new UrlShortener();
    }
}
