package com.cyberwalkabout.common.util;

import java.io.File;

import android.os.Environment;
import android.os.StatFs;

public class MemoryStatus
{
	static final int UNKNOWN = -1;

	public static boolean externalMemoryAvailable()
	{
		return Environment.getExternalStorageState().equals(Environment.MEDIA_MOUNTED);
	}

	public static long getAvailableInternalMemorySize()
	{
		File path = Environment.getDataDirectory();
		StatFs stat = new StatFs(path.getPath());
		long blockSize = stat.getBlockSize();
		long availableBlocks = stat.getAvailableBlocks();
		return availableBlocks * blockSize;
	}

	public static long getTotalInternalMemorySize()
	{
		File path = Environment.getDataDirectory();
		StatFs stat = new StatFs(path.getPath());
		long blockSize = stat.getBlockSize();
		long totalBlocks = stat.getBlockCount();
		return totalBlocks * blockSize;
	}

	public static long getAvailableExternalMemorySize()
	{
		if (externalMemoryAvailable())
		{
			File path = Environment.getExternalStorageDirectory();
			StatFs stat = new StatFs(path.getPath());
			long blockSize = stat.getBlockSize();
			long availableBlocks = stat.getAvailableBlocks();
			return availableBlocks * blockSize;
		} else
		{
			return UNKNOWN;
		}
	}

	public static long getTotalExternalMemorySize()
	{
		if (externalMemoryAvailable())
		{
			File path = Environment.getExternalStorageDirectory();
			StatFs stat = new StatFs(path.getPath());
			long blockSize = stat.getBlockSize();
			long totalBlocks = stat.getBlockCount();
			return totalBlocks * blockSize;
		} else
		{
			return UNKNOWN;
		}
	}

	public static String formatSize(long size)
	{
		String suffix = null;

		if (size >= 1024)
		{
			suffix = "KiB";
			size /= 1024;
			if (size >= 1024)
			{
				suffix = "MiB";
				size /= 1024;
			}
		}

		StringBuilder resultBuffer = new StringBuilder(Long.toString(size));

		int commaOffset = resultBuffer.length() - 3;
		while (commaOffset > 0)
		{
			resultBuffer.insert(commaOffset, ',');
			commaOffset -= 3;
		}

		if (suffix != null)
			resultBuffer.append(suffix);
		return resultBuffer.toString();
	}
}
