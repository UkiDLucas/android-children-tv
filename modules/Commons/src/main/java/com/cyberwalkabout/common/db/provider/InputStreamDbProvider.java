package com.cyberwalkabout.common.db.provider;

import java.io.IOException;
import java.io.InputStream;

public class InputStreamDbProvider implements DbProvider
{
	private InputStream in;

	public InputStreamDbProvider(InputStream in)
	{
		this.in = in;
	}

	@Override
	public InputStream getDb() throws IOException
	{
		return in;
	}
}
