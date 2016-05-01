package com.cyberwalkabout.common.db.meta;

public interface DbMetadata
{
	int getVersion();

	void setVersion(int version);

	String getMd5();

	void setMd5(String md5);

	int incrementDbVersion();

	boolean exists();
}
