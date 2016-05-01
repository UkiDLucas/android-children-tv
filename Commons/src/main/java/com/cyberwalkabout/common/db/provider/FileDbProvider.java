package com.cyberwalkabout.common.db.provider;

import java.io.BufferedInputStream;
import java.io.File;
import java.io.FileInputStream;
import java.io.IOException;
import java.io.InputStream;
import java.util.zip.GZIPInputStream;
import java.util.zip.ZipEntry;
import java.util.zip.ZipFile;

public class FileDbProvider implements DbProvider
{
	private File dbFile;
	private ContentType type = ContentType.DEFAULT;

	public FileDbProvider(File dbFile)
	{
		this.dbFile = dbFile;
	}

	public FileDbProvider(File dbFile, ContentType type)
	{
		this.dbFile = dbFile;
		this.type = type;
	}

	@Override
	public InputStream getDb() throws IOException
	{
		InputStream in = null;
		switch (type)
		{
		case ZIP:
			ZipFile zipFile = new ZipFile(dbFile);
			if (zipFile.entries().hasMoreElements())
			{
				ZipEntry entry = zipFile.entries().nextElement();
				in = zipFile.getInputStream(entry);
			}
			break;
		case GZIP:
			in = new BufferedInputStream(new GZIPInputStream(new FileInputStream(dbFile)));
			break;
		case DEFAULT:
			in = new BufferedInputStream(new FileInputStream(dbFile));
			break;
		}
		return in;
	}

}
