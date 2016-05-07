package com.cyberwalkabout.common.http;

import android.R;
import android.app.Notification;
import android.app.NotificationManager;
import android.app.PendingIntent;
import android.content.Context;
import android.content.Intent;
import android.net.Uri;
import android.os.AsyncTask;
import android.util.Log;

import com.cyberwalkabout.common.util.Sys;

import java.io.File;
import java.io.FileOutputStream;
import java.io.InputStream;
import java.net.HttpURLConnection;
import java.net.URL;

import com.cyberwalkabout.common.util.IoUtils;

/**
 * @author Andrii Kovalov
 */
public class DownloadFile extends AsyncTask<String, Integer, String>
{
	private static final String TAG = DownloadFile.class.getSimpleName();
	private static final String DEFAULT_TICKER_TEXT = "Downloading...";
	private static final int NOTIFICATION_ID = 100500;

	private Notification notification;
	private NotificationManager notificationManager;
	private Context context;

	private int icon = R.drawable.stat_sys_download;
	private CharSequence contentText = "0% complete";
	private CharSequence contentTitle = "Your download is in progress";
	private CharSequence tickerText = DEFAULT_TICKER_TEXT;

	private PendingIntent contentIntent;
	private File outFile;
	private Callback callback;
	private boolean hideNotificationOnFinish;


	private long lastProgressPublish;

	public DownloadFile(Context context, File outFile)
	{
		this.notificationManager = (NotificationManager) context.getSystemService(Context.NOTIFICATION_SERVICE);
		this.context = context;
		this.outFile = outFile;
	}

	public DownloadFile(Context context, File outFile, Callback callback, boolean hideNotificationOnFinish)
	{
		this(context, outFile);
		this.callback = callback;
		this.hideNotificationOnFinish = hideNotificationOnFinish;
	}

	@Override
	protected void onPreExecute()
	{
		super.onPreExecute();

		notification = new Notification(icon, tickerText, System.currentTimeMillis());
		notification.flags = notification.flags | Notification.FLAG_AUTO_CANCEL;

		String mimeType = Sys.getMimeType(outFile);

		Intent intent = new Intent();
		intent.setAction(Intent.ACTION_VIEW);
		intent.setDataAndType(Uri.fromFile(outFile), mimeType);

		contentIntent = PendingIntent.getActivity(context, 0, intent, 0);

		updateNotification();
	}

	@Override
	protected String doInBackground(String... urlStr)
	{
		int count;
		FileOutputStream out = null;
		InputStream in = null;
		try
		{
			URL url = new URL(urlStr[0]);
			HttpURLConnection connection = (HttpURLConnection) url.openConnection();
			connection.setRequestMethod("GET");
			connection.setDoOutput(true);
			connection.connect();

			int contentLength = connection.getContentLength();

			out = new FileOutputStream(outFile);
			in = connection.getInputStream();

			byte[] data = new byte[1024];
			long total = 0;
			while ((count = in.read(data)) != -1)
			{
				total += count;
				out.write(data, 0, count);
				limitedProgressUpdate((int) (total * 100 / contentLength));
			}
		} catch (Exception e)
		{
			Log.e(TAG, e.getMessage(), e);
			contentTitle = "Failed to download";
			contentText = "";
			notification.icon = R.drawable.stat_sys_download_done;
			updateNotification();
			if (this.callback != null)
			{
				callback.onFail(e);
			}
		} finally
		{
			IoUtils.close(in);
			IoUtils.close(out, true);
		}
		return null;
	}

	private void limitedProgressUpdate(int progress)
	{
		long timestamp = System.currentTimeMillis();
		if (progress == 100 || timestamp - lastProgressPublish >= 100)
		{
			publishProgress(progress);
			lastProgressPublish = timestamp;
		}
	}

	@Override
	public void onProgressUpdate(Integer... progress)
	{
		contentText = progress[0] + "% complete";

		if (progress[0] == 100)
		{
			notification.icon = R.drawable.stat_sys_download_done;
			contentTitle = "Your download done";
			contentText = "Click to open file";
			if (hideNotificationOnFinish)
			{
				notificationManager.cancel(NOTIFICATION_ID);
			} else
			{
				updateNotification();
			}
			if (callback != null)
			{
				callback.onSuccess(outFile);
			}
		} else
		{
			updateNotification();
		}

		super.onProgressUpdate(progress);
	}

	private void updateNotification()
	{
//		notification.setLatestEventInfo(context, contentTitle, contentText, contentIntent);
//		notificationManager.notify(NOTIFICATION_ID, notification);
	}

	public interface Callback
	{
		void onSuccess(File file);

		void onFail(Exception e);
	}
}