package com.cyberwalkabout.common.util;

import android.database.Cursor;
import android.util.Log;

public class CursorUtils
{
	private static final String TAG = CursorUtils.class.getSimpleName();

	public static void closeQuietly(Cursor cursor)
	{
		if (cursor != null)
		{
			try
			{
				cursor.close();
			} catch (Exception e)
			{
				Log.w(TAG, "Couldn't close cursor: " + e.getMessage());
//				Log.d(TAG, "Couldn't close cursor", e);
			}
		}
	}
}
