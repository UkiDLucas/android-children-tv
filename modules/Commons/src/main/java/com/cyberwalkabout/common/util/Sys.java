package com.cyberwalkabout.common.util;

import android.app.Activity;
import android.app.AlertDialog;
import android.content.Context;
import android.content.DialogInterface;
import android.content.Intent;
import android.location.Criteria;
import android.location.Location;
import android.location.LocationManager;
import android.net.Uri;
import android.text.TextUtils;
import android.util.Log;
import android.view.View;
import android.view.inputmethod.InputMethodManager;
import android.webkit.MimeTypeMap;
import android.widget.EditText;

import java.io.File;
import java.lang.reflect.Method;

/**
 * @author Andrii Kovalov
 */
public class Sys {
	private static final String TAG = Sys.class.getSimpleName();

	public static String getMimeType(File file) {
		String mimeType = "*/*";

		String extension = StringUtils.getFileExtension(file);
		if (!TextUtils.isEmpty(extension)) {
			String detectedMimeType = MimeTypeMap.getSingleton().getMimeTypeFromExtension(extension);
			if (!TextUtils.isEmpty(detectedMimeType)) {
				mimeType = detectedMimeType;
			}
		}
		return mimeType;
	}

	public static void openImage(Context ctx, File file) {
		Intent intent = new Intent();
		intent.setAction(Intent.ACTION_VIEW);
		intent.setDataAndType(Uri.fromFile(file), "image/*");
		ctx.startActivity(intent);
	}

	public static void openFile(Context ctx, File file) {
		String mimeType = Sys.getMimeType(file);

		Intent intent = new Intent();
		intent.setAction(Intent.ACTION_VIEW);
		intent.setDataAndType(Uri.fromFile(file), mimeType);

		Intent intentChooser = Intent.createChooser(intent, "Open file");
		intentChooser.setFlags(Intent.FLAG_ACTIVITY_NEW_TASK);

		ctx.startActivity(intentChooser);
	}

	public static void copyToClipboard(Context context, String text) {
		Object clipboard = context.getSystemService(Context.CLIPBOARD_SERVICE);

		Method[] allMethods = clipboard.getClass().getDeclaredMethods();

		for (Method m : allMethods) {
			if (!m.getName().equals("setText")) {
				continue;
			}
			try {
				m.invoke(clipboard, new Object[]{text});
			} catch (Exception e) {
				Log.e(TAG, e.getMessage(), e);
			}
			break;
		}
	}

	public static boolean isNetworkLocationProviderEnabled(Context ctx) {
		final LocationManager manager = (LocationManager) ctx.getSystemService(Context.LOCATION_SERVICE);
		return manager.isProviderEnabled(LocationManager.NETWORK_PROVIDER);
	}

	public static boolean isGPSLocationProviderEnabled(Context ctx) {
		final LocationManager manager = (LocationManager) ctx.getSystemService(Context.LOCATION_SERVICE);
		return manager.isProviderEnabled(LocationManager.GPS_PROVIDER);
	}

	public static boolean ifAnyLocationProviderEnabled(Context ctx) {
		final LocationManager manager = (LocationManager) ctx.getSystemService(Context.LOCATION_SERVICE);
		return manager.isProviderEnabled(LocationManager.NETWORK_PROVIDER) || manager.isProviderEnabled(LocationManager.GPS_PROVIDER);
	}

	// TODO: exclude to DialogFragment
	public static void createLocationProviderDisabledAlert(final Context ctx) {
		AlertDialog.Builder builder = new AlertDialog.Builder(ctx);
		builder.setMessage("This option requires location provider be enabled. Would you like to go to the settings screen?").setCancelable(false).setPositiveButton("Yes", new DialogInterface.OnClickListener() {
			public void onClick(DialogInterface dialog, int id) {
				Intent gpsOptionsIntent = new Intent(android.provider.Settings.ACTION_LOCATION_SOURCE_SETTINGS);
				ctx.startActivity(gpsOptionsIntent);
			}
		});
		builder.setNegativeButton("No", new DialogInterface.OnClickListener() {
			public void onClick(DialogInterface dialog, int id) {
				dialog.cancel();
			}
		});
		AlertDialog alert = builder.create();
		alert.show();
	}

	public static Location getLatestKnownLocation(Context context) {
		if (ensureContext(context)) {
			LocationManager lm = (LocationManager) context.getSystemService(Context.LOCATION_SERVICE);
			Criteria criteria = new Criteria();
			criteria.setAccuracy(Criteria.ACCURACY_COARSE);
			String provider = lm.getBestProvider(criteria, true);
			if (provider != null) {
				Location loc = lm.getLastKnownLocation(provider);
				return loc;
			} else {
				return null;
			}
		}
		return null;
	}

	public static void getDirections(Context context, double startLatitude, double startLongitude, double destLatitude, double destLongitude) {
		Intent intent = new Intent(Intent.ACTION_VIEW, Uri.parse("http://maps.google.com/maps?saddr=" + startLatitude + "," + startLongitude + "&daddr=" + destLatitude + "," + destLongitude));
		context.startActivity(intent);
	}

	public static void showKeyboard(View view) {
		showKeyboard(view.getContext());
	}

	public static void showKeyboard(Context context) {
		InputMethodManager imm = (InputMethodManager) context.getSystemService(Context.INPUT_METHOD_SERVICE);
		imm.toggleSoftInput(InputMethodManager.SHOW_FORCED, InputMethodManager.HIDE_IMPLICIT_ONLY);
	}

	public static void hideKeyboard(View view) {
		Context context = view.getContext();
		InputMethodManager imm = (InputMethodManager) context.getSystemService(Context.INPUT_METHOD_SERVICE);
		imm.hideSoftInputFromWindow(view.getWindowToken(), 0);
	}

	public static boolean hideKeyboard(Activity activity) {
		if (activity.getCurrentFocus() != null && activity.getCurrentFocus() instanceof EditText) {
			InputMethodManager imm = (InputMethodManager) activity.getSystemService(Context.INPUT_METHOD_SERVICE);
			imm.hideSoftInputFromWindow(activity.getCurrentFocus().getWindowToken(), 0);
			return true;
		}
		return false;
	}

	private static boolean ensureContext(Context ctx) {
		return ctx != null;
	}
}
