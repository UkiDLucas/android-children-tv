package com.cyberwalkabout.common.http;

import java.io.IOException;
import java.io.InputStream;

import org.apache.http.HttpEntity;
import org.apache.http.HttpResponse;
import org.apache.http.StatusLine;
import org.apache.http.client.ResponseHandler;

import android.util.Log;

public abstract class HttpHandler<T> implements ResponseHandler<T> {
	private static final String TAG = HttpUtils.class.getSimpleName();
	private HttpResponse response;
	private T result;

	@Override
	public T handleResponse(HttpResponse httpResponse) throws IOException {
		this.response = httpResponse;
		HttpEntity entity = httpResponse.getEntity();
		InputStream in = null;
		try {
			in = entity.getContent();
			handleInputStream(in);
		} catch (IOException e) {
			Log.e(TAG, e.getMessage(), e);
		} finally {
			if (in != null) {
				try {
					in.close();
				} catch (IOException e) {
					Log.w(TAG, "Problem closing InputStream", e);
				}
			}
		}
		return result;
	}

	public StatusLine getStatusLine() {
		return response != null ? response.getStatusLine() : null;
	}

	public void handleInputStream(InputStream in) {
		// TODO put dummy logging
	}

	public void onError(HttpException httpException) throws HttpException {
		throw httpException;
	}
}