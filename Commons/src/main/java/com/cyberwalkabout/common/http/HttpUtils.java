package com.cyberwalkabout.common.http;

import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.text.TextUtils;
import android.util.Log;
import com.cyberwalkabout.common.util.Crypto;
import org.apache.http.Header;
import org.apache.http.HttpStatus;
import org.apache.http.HttpVersion;
import org.apache.http.client.HttpClient;
import org.apache.http.client.methods.HttpGet;
import org.apache.http.client.methods.HttpRequestBase;
import org.apache.http.conn.params.ConnManagerParams;
import org.apache.http.conn.params.ConnRouteParams;
import org.apache.http.conn.scheme.PlainSocketFactory;
import org.apache.http.conn.scheme.Scheme;
import org.apache.http.conn.scheme.SchemeRegistry;
import org.apache.http.conn.ssl.SSLSocketFactory;
import org.apache.http.impl.client.DefaultHttpClient;
import org.apache.http.impl.client.DefaultHttpRequestRetryHandler;
import org.apache.http.impl.conn.tsccm.ThreadSafeClientConnManager;
import org.apache.http.message.BasicHeader;
import org.apache.http.params.BasicHttpParams;
import org.apache.http.params.HttpConnectionParams;
import org.apache.http.params.HttpParams;
import org.apache.http.params.HttpProtocolParams;
import org.apache.http.protocol.HTTP;

import java.io.IOException;
import java.lang.reflect.Field;
import java.net.HttpURLConnection;
import java.net.MalformedURLException;
import java.net.URI;
import java.net.URL;
import java.security.KeyStore;
import java.util.ArrayList;
import java.util.List;

public class HttpUtils {
    private static final String TAG = HttpUtils.class.getSimpleName();

    public static DefaultHttpClient createHttpClient() {
        return createHttpClient(HttpClientConfig.getDefaultConfig());
    }

    public static DefaultHttpClient createHttpClient(HttpClientConfig config) {
        DefaultHttpClient httpClient = null;

        try {
            HttpParams params = new BasicHttpParams();
            int timeout = config.getTimeout();
            HttpConnectionParams.setConnectionTimeout(params, timeout);
            HttpConnectionParams.setSoTimeout(params, timeout);
            ConnManagerParams.setTimeout(params, timeout);
            HttpProtocolParams.setVersion(params, HttpVersion.HTTP_1_1);
            HttpProtocolParams.setContentCharset(params, HTTP.UTF_8);
            HttpProtocolParams.setContentCharset(params, config.getCharset());

            KeyStore trustStore = KeyStore.getInstance(KeyStore.getDefaultType());
            trustStore.load(null, null);

            SchemeRegistry registry = new SchemeRegistry();
            registry.register(new Scheme("http", PlainSocketFactory.getSocketFactory(), config.getHttpPort()));
            SSLSocketFactory sslSocketFactory = new TrustAllSslSocketFactory(trustStore);
            sslSocketFactory.setHostnameVerifier(SSLSocketFactory.ALLOW_ALL_HOSTNAME_VERIFIER);
            registry.register(new Scheme("https", sslSocketFactory, config.getHttpsPort()));

            ThreadSafeClientConnManager manager = new ThreadSafeClientConnManager(params, registry);

            httpClient = new DefaultHttpClient(manager, params);
            httpClient.setHttpRequestRetryHandler(new DefaultHttpRequestRetryHandler(3, false));

            if (config.getProxy() != null) {
                ConnRouteParams.setDefaultProxy(params, config.getProxy());
            }
        } catch (Exception e) {
            Log.e(TAG, e.getMessage(), e);
        }
        return httpClient;
    }

    public static<T> T doGet(String url, HttpHandler<T> handler, HttpClient httpClient) {
        if (handler == null) {
            throw new NullPointerException("HttpHandler is null");
        }
        HttpGet httpGet = new HttpGet(url);
        T result = null;
        try {
            Log.d(TAG, "Get data: " + url);
            httpClient.execute(httpGet, handler);
            int statusCode = handler.getStatusLine().getStatusCode();
            Log.d(TAG, "Received http response, status: " + statusCode);
            if (statusCode == HttpStatus.SC_SERVICE_UNAVAILABLE || statusCode == HttpStatus.SC_BAD_REQUEST || statusCode == HttpStatus.SC_INTERNAL_SERVER_ERROR) {
                Log.e(TAG, "Request failed: HttpStatus - " + statusCode);
                handler.onError(new HttpException(handler.getStatusLine().getReasonPhrase(), url, statusCode));
            }
        } catch (IOException e) {
            Log.e(TAG, e.getMessage(), e);
            handler.onError(new HttpException(e.getMessage(), url));
        } finally {
            try {
                httpClient.getConnectionManager().shutdown();
            } catch (Exception e) {
                Log.e(TAG, e.getMessage(), e);
            }
        }
        return result;
    }

    public static <T> T doGet(String url, HttpHandler<T> handler) {
       return doGet(url, handler, createHttpClient());
    }

    public static void addBasicAuthHeaders(HttpRequestBase request, String user, String password) {
        List<Header> headers = createBasicAuthHeaders(user, password);
        for (Header header : headers) {
            request.addHeader(header);
        }
    }

    public static Bitmap getImage(URL url) {
        HttpURLConnection connection = null;
        try {
            connection = (HttpURLConnection) url.openConnection();
            connection.connect();
            int responseCode = connection.getResponseCode();
            if (responseCode == 200) {
                return BitmapFactory.decodeStream(connection.getInputStream());
            } else return null;
        } catch (Exception e) {
            return null;
        } finally {
            if (connection != null) {
                connection.disconnect();
            }
        }
    }

    public static Bitmap getImage(String urlString) {
        try {
            URL url = new URL(urlString);
            return getImage(url);
        } catch (MalformedURLException e) {
            return null;
        }
    }

    public static boolean fixUriWithUnderscores(URI uri) {
        if (TextUtils.isEmpty(uri.getHost())) {
            try {
                Field field = URI.class.getDeclaredField("host");
                field.setAccessible(true);
                field.set(uri, uri.getAuthority());
            } catch (Exception e) {
                return false;
            }
        }
        return true;
    }

    public static List<Header> createBasicAuthHeaders(String user, String password) {
        List<Header> headers = new ArrayList<Header>(2);
        String basicAuthString = constructBasicAuthString(user, password);
        headers.add(new BasicHeader("Authorization", basicAuthString));
        headers.add(new BasicHeader("Proxy-Authorization", basicAuthString));
        return headers;
    }

    private static String constructBasicAuthString(String authUser, String authPass) {
        String credentials = authUser + ":" + authPass;
        return "Basic " + Crypto.Base64.encodeBytes(credentials.getBytes());
    }
}
