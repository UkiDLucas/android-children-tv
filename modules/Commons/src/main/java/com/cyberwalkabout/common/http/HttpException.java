package com.cyberwalkabout.common.http;

public class HttpException extends RuntimeException {
	private static final long serialVersionUID = -9096965126636054901L;

	private String requestUrl;
	private int statusCode;

	public HttpException(String message) {
		super(message);
	}

	public HttpException(String message, String requestUrl) {
		super(message);
		this.requestUrl = requestUrl;
	}

	public HttpException(String message, String requestUrl, int statusCode) {
		this(message, requestUrl);
		this.statusCode = statusCode;
	}

	public HttpException(Throwable t) {
		super(t);
	}

	public HttpException(String message, Throwable t, String requestUrl) {
		super(message, t);
		this.requestUrl = requestUrl;
	}

	public String getRequestUrl() {
		return requestUrl;
	}

	public void setRequestUrl(String requestUrl) {
		this.requestUrl = requestUrl;
	}
}
