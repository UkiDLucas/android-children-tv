package com.cyberwalkabout.common.util;

import android.content.Context;
import android.media.MediaScannerConnection;
import android.net.Uri;

import java.io.File;

/**
 * @author Andrii Kovalov
 */
public class SingleMediaScanner implements MediaScannerConnection.MediaScannerConnectionClient
{
	private MediaScannerConnection connection;
	private File file;

	public SingleMediaScanner(Context context, File f)
	{
		file = f;
		connection = new MediaScannerConnection(context, this);
		connection.connect();
	}

	@Override
	public void onMediaScannerConnected()
	{
		connection.scanFile(file.getAbsolutePath(), null);
	}

	@Override
	public void onScanCompleted(String path, Uri uri)
	{
		connection.disconnect();
	}
}
