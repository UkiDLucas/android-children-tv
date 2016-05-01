package com.cyberwalkabout.common.google;

import android.location.Location;
import android.text.TextUtils;
import android.util.Log;
import com.cyberwalkabout.common.http.HttpUtils;
import org.apache.http.client.HttpClient;
import org.apache.http.client.ResponseHandler;
import org.apache.http.client.methods.HttpGet;
import org.apache.http.impl.client.BasicResponseHandler;
import org.json.JSONArray;
import org.json.JSONObject;

import java.io.UnsupportedEncodingException;
import java.net.URLEncoder;
import java.util.ArrayList;

// TODO: drop it and use android.location.Geocoder
@Deprecated
public class GeocodeUtils
{
	private static final String TAG = GeocodeUtils.class.getSimpleName();
	private static final String GOOGLE_GEOCODE_JSON_API = "https://maps.googleapis.com/maps/api/geocode/json";
	private HttpClient httpClient;

	public GeocodeUtils(HttpClient httpClient)
	{
		this.httpClient = httpClient;
	}

	public GeocodeUtils()
	{
		this(HttpUtils.createHttpClient());
	}

	public ArrayList<GeoLocation> getLocations(String input)
	{
		ArrayList<GeoLocation> geoLocations = new ArrayList<GeoLocation>();
		try
		{
			input = URLEncoder.encode(input, "UTF-8");
			HttpGet get = new HttpGet(GOOGLE_GEOCODE_JSON_API + "?sensor=true&address=" + input);
			ResponseHandler<String> handler = new BasicResponseHandler();
			String response = httpClient.execute(get, handler);
			if (!TextUtils.isEmpty(response))
			{
				JSONObject json = new JSONObject(response);
				if ("OK".equals(json.optString("status")))
				{
					JSONArray results = json.getJSONArray("results");
					for (int i = 0; i < results.length(); i++)
					{
						geoLocations.add(GeoLocation.parseJson(results.getJSONObject(i)));
					}
				}
			}
		} catch (Exception e)
		{
			Log.e(TAG, e.getMessage(), e);
		}
		return geoLocations;
	}

	public Location getFirstLocation(String input)
	{
		Location location = null;

		try
		{
			input = URLEncoder.encode(input, "UTF-8");
			HttpGet get = new HttpGet(GOOGLE_GEOCODE_JSON_API + "?sensor=true&address=" + input);
			try
			{
				ResponseHandler<String> handler = new BasicResponseHandler();
				String response = httpClient.execute(get, handler);
				if (!TextUtils.isEmpty(response))
				{
					JSONObject json = new JSONObject(response);
					if ("OK".equals(json.getString("status")))
					{
						JSONArray results = json.getJSONArray("results");
						// for now just take first lat lon
						for (int i = 0; i < results.length(); i++)
						{
							JSONObject item = results.getJSONObject(i);
							JSONObject geometry = item.optJSONObject("geometry");
							if (geometry != null)
							{
								JSONObject geoLocation = geometry.optJSONObject("location");
								if (geoLocation != null)
								{
									location = new Location("google");
									location.setLatitude(geoLocation.getDouble("lat"));
									location.setLongitude(geoLocation.getDouble("lng"));
									break;
								}
							}

						}
					}
					Log.d(TAG, json.toString());
				}
			} catch (Exception e)
			{
				Log.e(TAG, e.getMessage(), e);
			}
		} catch (UnsupportedEncodingException e)
		{
			Log.e(TAG, e.getMessage(), e);
		}
		return location;
	}
}
