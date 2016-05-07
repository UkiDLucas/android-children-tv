package com.cyberwalkabout.common.util;

import android.util.Log;

import java.io.*;

public class IoUtils
{
	public static final int DEFAULT_BUFFER_SIZE = 1024;
	private static final String TAG = IoUtils.class.getName();

	public static void close(InputStream in)
	{
		if (in != null)
		{
			try
			{
				in.close();
			} catch (IOException e)
			{
				Log.e(TAG, e.getMessage() != null ? e.getMessage() : "", e);
			}
		}
	}

	public static void close(OutputStream out)
	{
		if (out != null)
		{
			try
			{
				out.close();
			} catch (IOException e)
			{
				Log.e(TAG, e.getMessage() != null ? e.getMessage() : "", e);
			}
		}
	}

	public static void close(OutputStream out, boolean flush)
	{
		if (out != null)
		{
			if (flush)
			{
				try
				{
					out.flush();
				} catch (IOException e)
				{
					Log.e(TAG, e.getMessage() != null ? e.getMessage() : "", e);
				}
			}
			close(out);
		}
	}

	public static boolean copy(File from, File to)
	{
		if (from != null && to != null && from.exists())
		{
			try
			{
				return copy(new FileInputStream(from), new FileOutputStream(to));
			} catch (FileNotFoundException e)
			{
				Log.e(TAG, e.getMessage() != null ? e.getMessage() : "", e);
			}
		}
		return false;
	}

	public static boolean copy(InputStream in, OutputStream out)
	{
		boolean success = false;
		try
		{
			byte[] buffer = new byte[DEFAULT_BUFFER_SIZE];
			int total = 0;
			int length;
			while ((length = in.read(buffer)) > 0)
			{
				out.write(buffer, 0, length);
				total += length;
			}
			success = true;
		} catch (Exception e)
		{
			Log.e(TAG, e.getMessage(), e);
		} finally
		{
			IoUtils.close(in);
			IoUtils.close(out, true);
		}
		return success;
	}

	public static boolean copy(InputStream in, OutputStream out, CopyListener listener)
	{
		try
		{
			return copy(in, out, in.available(), listener);
		} catch (IOException e)
		{
			Log.e(TAG, e.getMessage() != null ? e.getMessage() : "", e);
		}
		return false;
	}

	public static boolean copy(InputStream in, OutputStream out, long contentLength, CopyListener listener)
	{
		boolean success = false;
		try
		{
			int total = 0;
			int length;
			byte[] buffer = new byte[DEFAULT_BUFFER_SIZE];
			while ((length = in.read(buffer)) > 0)
			{
				out.write(buffer, 0, length);
				total += length;
				if (listener != null)
					listener.update(contentLength, total, (int) (total * 100 / contentLength));
			}
			if (listener != null)
				listener.onFinish(total);
			success = true;
		} catch (Exception e)
		{
			success = false;
			Log.e(TAG, e.getMessage() != null ? e.getMessage() : "", e);
		} finally
		{
			IoUtils.close(in);
			IoUtils.close(out, true);
		}
		return success;
	}

	public static boolean deleteDir(File dir)
	{
		if (dir.isDirectory())
		{
			String[] children = dir.list();
			for (int i = 0; i < children.length; i++)
			{
				boolean success = deleteDir(new File(dir, children[i]));
				if (!success)
				{
					return false;
				}
			}
		}
		return dir.delete();
	}

	public static abstract class CopyListener
	{
		private int delay = 100;
		private long lastUpdate = System.currentTimeMillis();

		protected CopyListener()
		{
		}

		protected CopyListener(int delay)
		{
			this.delay = delay;
		}

		void update(long contentLength, int bytesRead, int progress)
		{
			long timestamp = System.currentTimeMillis();
			if (progress == 100 || timestamp - lastUpdate >= delay)
			{
				lastUpdate = timestamp;
				onUpdate(contentLength, bytesRead, progress);
			}
		}

		public abstract void onUpdate(long contentLength, int bytesRead, int progress);

		public abstract void onFinish(int bytesRead);
	}

}
