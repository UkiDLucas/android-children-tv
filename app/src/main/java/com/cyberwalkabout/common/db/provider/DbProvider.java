package com.cyberwalkabout.common.db.provider;

import java.io.IOException;
import java.io.InputStream;

public interface DbProvider
{
	public enum ContentType
	{
		GZIP, ZIP, DEFAULT
	}

	InputStream getDb() throws IOException;
}
