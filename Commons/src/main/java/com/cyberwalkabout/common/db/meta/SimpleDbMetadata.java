package com.cyberwalkabout.common.db.meta;

import java.lang.ref.WeakReference;

import android.content.Context;
import android.content.SharedPreferences;
import android.content.SharedPreferences.Editor;

public class SimpleDbMetadata implements DbMetadata
{
	public static final int UNKNOWN = -1;

	public static final String DB_CCBN = "DB_CCBN";
	public static final String DB_SEARCH = "DB_SEARCH";
	private static final String PREF_VERSION = "version";
	private static final String PREF_MD5 = "md5";

	private String name = DB_CCBN;
	private WeakReference<Context> ctxRef;

	public SimpleDbMetadata(Context ctx, String name)
	{
		this.ctxRef = new WeakReference<Context>(ctx);
		this.name = name;
	}

	@Override
	public int getVersion()
	{
		int version = UNKNOWN;
		SharedPreferences prefs = getReadablePreferences();
		if (prefs != null)
		{
			version = prefs.getInt(PREF_VERSION, UNKNOWN);
		}
		return version;
	}

	@Override
	public void setVersion(int version)
	{
		SharedPreferences prefs = getWritablePreferences();
		if (prefs != null)
		{
			Editor edit = prefs.edit();
			edit.putInt(PREF_VERSION, version);
			edit.commit();
		}
	}

	@Override
	public String getMd5()
	{

		String md5 = null;
		SharedPreferences prefs = getReadablePreferences();
		if (prefs != null)
		{
			md5 = prefs.getString(PREF_MD5, null);
		}
		return md5;
	}

	@Override
	public void setMd5(String md5)
	{
		SharedPreferences prefs = getWritablePreferences();
		if (prefs != null)
		{
			Editor edit = prefs.edit();
			edit.putString(PREF_MD5, md5);
			edit.commit();
		}
	}

	@Override
	public int incrementDbVersion()
	{
		int version = getVersion();

		if (version == UNKNOWN)
		{
			version = 0;
		}

		SharedPreferences prefs = getWritablePreferences();
		if (prefs != null)
		{
			Editor edit = prefs.edit();
			edit.putInt(PREF_VERSION, ++version);
			edit.commit();
		}
		return version;
	}

	@Override
	public boolean exists()
	{
		return getVersion() != UNKNOWN;
	}

	private SharedPreferences getWritablePreferences()
	{
		Context ctx = ctxRef.get();
		if (ctx != null)
		{
			return ctx.getSharedPreferences(name, Context.MODE_WORLD_WRITEABLE);
		}
		return null;
	}

	private SharedPreferences getReadablePreferences()
	{
		Context ctx = ctxRef.get();
		if (ctx != null)
		{
			return ctx.getSharedPreferences(name, Context.MODE_WORLD_READABLE);
		}
		return null;
	}
}
