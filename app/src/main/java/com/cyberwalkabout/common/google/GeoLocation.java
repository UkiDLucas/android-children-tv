package com.cyberwalkabout.common.google;

import android.location.Location;

import com.cyberwalkabout.common.util.Bounds;

import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

import java.io.Serializable;

/**
 * @author Andrii Kovalov
 */
public class GeoLocation implements Serializable
{
	private Name[] addressComponents;
	private String formattedAddress;
	private String[] types;
	private Bounds bounds;
	private Bounds viewport;
	private double latitude;
	private double longitude;

	public static GeoLocation parseJson(JSONObject json) throws JSONException
	{
		GeoLocation geoLocation = new GeoLocation();
		JSONArray addressComponentsJson = json.optJSONArray("address_components");
		if (addressComponentsJson != null && addressComponentsJson.length() > 0)
		{
			Name[] addressComponents = new Name[addressComponentsJson.length()];

			for (int i = 0; i < addressComponentsJson.length(); i++)
			{
				JSONObject component = addressComponentsJson.getJSONObject(i);

				Name name = new Name(component.getString("long_name"), component.getString("short_name"));

				JSONArray typesJson = component.optJSONArray("types");
				if (typesJson != null && typesJson.length() > 0)
				{
					String[] types = new String[typesJson.length()];
					for (int j = 0; j < typesJson.length(); j++)
					{
						types[j] = typesJson.getString(j);
					}
					name.setTypes(types);
				}
				addressComponents[i] = name;
			}
			geoLocation.setAddressComponents(addressComponents);
		}

		geoLocation.setFormattedAddress(json.getString("formatted_address"));

		JSONArray typesJson = json.optJSONArray("types");
		if (typesJson != null && typesJson.length() > 0)
		{
			String[] types = new String[typesJson.length()];
			for (int j = 0; j < typesJson.length(); j++)
			{
				types[j] = typesJson.getString(j);
			}
			geoLocation.setTypes(types);
		}

		JSONObject geometry = json.optJSONObject("geometry");
		if (geometry != null)
		{
			JSONObject locationJson = geometry.optJSONObject("location");
			if (locationJson != null)
			{
				geoLocation.setLatitude((float) locationJson.getDouble("lat"));
				geoLocation.setLongitude((float) locationJson.getDouble("lng"));
			}

			JSONObject boundsJson = geometry.optJSONObject("bounds");
			if (boundsJson != null)
			{
				Bounds bounds = new Bounds();
				JSONObject northeast = boundsJson.getJSONObject("northeast");
				bounds.setLatNorthEastern(northeast.getDouble("lat"));
				bounds.setLonNorthEastern(northeast.getDouble("lng"));
				JSONObject southwest = boundsJson.getJSONObject("southwest");
				bounds.setLatSouthWestern(southwest.getDouble("lat"));
				bounds.setLonSouthWestern(southwest.getDouble("lng"));
				geoLocation.setBounds(bounds);
			}

			JSONObject viewportJson = geometry.optJSONObject("viewport");
			if (viewportJson != null)
			{
				Bounds viewport = new Bounds();
				JSONObject northeast = viewportJson.getJSONObject("northeast");
				viewport.setLatNorthEastern(northeast.getDouble("lat"));
				viewport.setLonNorthEastern(northeast.getDouble("lng"));
				JSONObject southwest = viewportJson.getJSONObject("southwest");
				viewport.setLatSouthWestern(southwest.getDouble("lat"));
				viewport.setLonSouthWestern(southwest.getDouble("lng"));
				geoLocation.setViewport(viewport);
			}

		}
		return geoLocation;
	}

	public Name[] getAddressComponents()
	{
		return addressComponents;
	}

	public void setAddressComponents(Name[] addressComponents)
	{
		this.addressComponents = addressComponents;
	}

	public String getFormattedAddress()
	{
		return formattedAddress;
	}

	public void setFormattedAddress(String formattedAddress)
	{
		this.formattedAddress = formattedAddress;
	}

	public String[] getTypes()
	{
		return types;
	}

	public void setTypes(String[] types)
	{
		this.types = types;
	}

	public Bounds getBounds()
	{
		return bounds;
	}

	public void setBounds(Bounds bounds)
	{
		this.bounds = bounds;
	}

	public Bounds getViewport()
	{
		return viewport;
	}

	public void setViewport(Bounds viewport)
	{
		this.viewport = viewport;
	}

	public double getLatitude()
	{
		return latitude;
	}

	public void setLatitude(double latitude)
	{
		this.latitude = latitude;
	}

	public double getLongitude()
	{
		return longitude;
	}

	public void setLongitude(double longitude)
	{
		this.longitude = longitude;
	}

	public Location getLocation()
	{
		Location location = new Location("google");
		location.setLatitude(latitude);
		location.setLongitude(longitude);
		return location;
	}

	public static class Name implements Serializable
	{
		private String longName;
		private String shortName;
		private String[] types;

		public Name(String longName, String shortName)
		{
			this.longName = longName;
			this.shortName = shortName;
		}

		public String getLongName()
		{
			return longName;
		}

		public void setLongName(String longName)
		{
			this.longName = longName;
		}

		public String getShortName()
		{
			return shortName;
		}

		public void setShortName(String shortName)
		{
			this.shortName = shortName;
		}

		public String[] getTypes()
		{
			return types;
		}

		public void setTypes(String[] types)
		{
			this.types = types;
		}
	}
}
