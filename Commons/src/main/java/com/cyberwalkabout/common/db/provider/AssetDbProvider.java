package com.cyberwalkabout.common.db.provider;

import java.io.IOException;
import java.io.InputStream;
import java.lang.ref.WeakReference;
import java.util.zip.GZIPInputStream;
import java.util.zip.ZipEntry;
import java.util.zip.ZipInputStream;

import android.content.Context;

public class AssetDbProvider implements DbProvider
{
	private String dbAsset;
	private ContentType type;
	private WeakReference<Context> ctxRef;

	public AssetDbProvider(Context ctx, String dbAsset, ContentType type)
	{
		this.ctxRef = new WeakReference<Context>(ctx);
		this.dbAsset = dbAsset;
		this.type = type;
	}

	@Override
	public InputStream getDb() throws IOException
	{
		InputStream assetIn = null;
		Context ctx = ctxRef.get();
		if (ctx != null)
		{
			switch (type)
			{
			case ZIP:
				ZipInputStream zipIn = new ZipInputStream(ctx.getAssets().open(dbAsset));
				ZipEntry zipEntry = zipIn.getNextEntry();
				assetIn = zipIn;
				break;
			case GZIP:
				assetIn = new GZIPInputStream(ctx.getAssets().open(dbAsset));
				break;
			case DEFAULT:
				assetIn = ctx.getAssets().open(dbAsset);
				break;
			}
		}
		return assetIn;
	}

}
