package com.cyberwalkabout.common.http;

import org.apache.http.HttpHost;

public class HttpClientConfig {
	public static final String DEFAULT_CHARSET = "UTF8";
	public static final int DEFAULT_HTTP_PORT = 80;
	public static final int DEFAULT_HTTPS_PORT = 443;
	public static final int DEFAULT_TIMEOUT = 3 * 1000; // ms

	// cached copy of default config
	private static HttpClientConfig defaultConfig;

	// common configuration
	private String charset = DEFAULT_CHARSET;
	private int timeout = DEFAULT_TIMEOUT;

	// advanced configuration
	private HttpHost proxy;
	private int httpPort = DEFAULT_HTTP_PORT;
	private int httpsPort = DEFAULT_HTTPS_PORT;

	public static HttpClientConfig getDefaultConfig() {
		if (defaultConfig == null) {
			defaultConfig = new HttpClientConfig();
		}
		return defaultConfig;
	}

	public static HttpClientConfig createConfig(int timeout) {
		HttpClientConfig config = new HttpClientConfig();
		config.timeout = timeout;
		return config;
	}

	public static HttpClientConfig createConfig(HttpHost proxy) {
		HttpClientConfig config = new HttpClientConfig();
		config.proxy = proxy;
		return config;
	}

	public static HttpClientConfig createConfig(int timeout, HttpHost proxy) {
		HttpClientConfig config = createConfig(timeout);
		config.proxy = proxy;
		return config;
	}

	private HttpClientConfig() {
	}

	public String getCharset() {
		return charset;
	}

	public void setCharset(String charset) {
		this.charset = charset;
	}

	public int getTimeout() {
		return timeout;
	}

	public void setTimeout(int timeout) {
		this.timeout = timeout;
	}

	public HttpHost getProxy() {
		return proxy;
	}

	public void setProxy(HttpHost proxy) {
		this.proxy = proxy;
	}

	public int getHttpPort() {
		return httpPort;
	}

	public void setHttpPort(int httpPort) {
		this.httpPort = httpPort;
	}

	public int getHttpsPort() {
		return httpsPort;
	}

	public void setHttpsPort(int httpsPort) {
		this.httpsPort = httpsPort;
	}
}
