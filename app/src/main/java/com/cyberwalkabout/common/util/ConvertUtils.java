package com.cyberwalkabout.common.util;

import android.content.res.Resources;
import android.graphics.*;
import android.location.Location;
import android.location.LocationManager;
import android.media.ExifInterface;
import android.util.Log;
import android.util.TypedValue;

import java.io.*;
import java.text.ParseException;
import java.text.SimpleDateFormat;
import java.util.Calendar;
import java.util.Date;
import java.util.TimeZone;

public class ConvertUtils {
    private static final String TAG = ConvertUtils.class.getSimpleName();

    public static float dipToPixels(Resources res, float value) {
        return TypedValue.applyDimension(TypedValue.COMPLEX_UNIT_DIP, value, res.getDisplayMetrics());
    }

    public static int toE6(double value) {
        return Double.valueOf(value * 1E6).intValue();
    }

    public static GeoPoint toGeoPoint(double lat, double lon) {
        return new GeoPoint(toE6(lat), toE6(lon));
    }

    public static GeoPoint toGeoPoint(Location location) {
        return new GeoPoint(toE6(location.getLatitude()), toE6(location.getLongitude()));
    }

    public static Location toLocation(GeoPoint point) {
        Location location = new Location(LocationManager.NETWORK_PROVIDER);
        location.setLatitude(point.getLatitudeE6() / 1E6);
        location.setLongitude(point.getLongitudeE6() / 1E6);
        return location;
    }

    public static Bitmap toBitmap(File f) {
        try {
            return toBitmap(new FileInputStream(f));
        } catch (FileNotFoundException e) {
            Log.e(TAG, e.getMessage(), e);
        }
        return null;
    }

    public static Bitmap toBitmap(InputStream in) throws FileNotFoundException {
        return BitmapFactory.decodeStream(in, null, null);
    }

    public static Bitmap toBitmap(File f, int requiredSize) throws FileNotFoundException {
        if (f.exists() && f.isFile() && f.canRead()) {
            try {
                // decode image size
                BitmapFactory.Options o = new BitmapFactory.Options();
                o.inJustDecodeBounds = true;
                try {
                    BitmapFactory.decodeStream(new FileInputStream(f), null, o);
                } catch (OutOfMemoryError e) {
                    e.printStackTrace();
                }

                // Find the correct scale value. It should be the power of 2.
                int width_tmp = o.outWidth, height_tmp = o.outHeight;
                int scale = 1;
                while (true) {
                    if (width_tmp / 2 < requiredSize || height_tmp / 2 < requiredSize) break;
                    width_tmp /= 2;
                    height_tmp /= 2;
                    scale *= 2;
                }

                // decode with inSampleSize
                BitmapFactory.Options o2 = new BitmapFactory.Options();
                o2.inSampleSize = scale;
                Bitmap bitmap = null;

                try {
                    bitmap = BitmapFactory.decodeStream(new FileInputStream(f), null, o2);
                } catch (OutOfMemoryError e) {
                    e.printStackTrace();
                }

                return bitmap;
            } catch (FileNotFoundException e) {
                e.printStackTrace();
            }
        }
        return null;
    }

    public static boolean saveBitmap(Bitmap bitmap, File file) {
        OutputStream fOut = null;
        try {
            if (!file.exists()) {
                file.createNewFile();
            }
            fOut = new FileOutputStream(file);
            bitmap.compress(Bitmap.CompressFormat.JPEG, 85, fOut);
            return true;
        } catch (Exception e) {
            Log.e(TAG, e.getMessage(), e);
            return false;
        } finally {
            try {
                fOut.flush();
            } catch (IOException e) {
                // ignore
            }
            try {
                fOut.close();
            } catch (IOException e) {
                //ignore
            }
        }
    }

    public static Bitmap getRoundedCornerBitmap(Bitmap bitmap, float roundPx) {
        Bitmap output = Bitmap.createBitmap(bitmap.getWidth(), bitmap.getHeight(), Bitmap.Config.ARGB_8888);
        Canvas canvas = new Canvas(output);

        final int color = 0xff424242;
        final Paint paint = new Paint();
        final Rect rect = new Rect(0, 0, bitmap.getWidth(), bitmap.getHeight());
        final RectF rectF = new RectF(rect);
        paint.setAntiAlias(true);
        canvas.drawARGB(0, 0, 0, 0);
        paint.setColor(color);
        canvas.drawRoundRect(rectF, roundPx, roundPx, paint);

        paint.setXfermode(new PorterDuffXfermode(PorterDuff.Mode.SRC_IN));
        canvas.drawBitmap(bitmap, rect, rect, paint);

        return output;
    }

    public static Date toDate(String input, String inputFormat) {
        try {
            return new SimpleDateFormat(inputFormat).parse(input);
        } catch (ParseException e) {
            Log.e("formatDateDisplay", "Date " + input + " " + e.getMessage());
        }
        return new Date();
    }

    public static Date incrementDate(String input, String inputFormat, int numberOfDays) {
        try {
            SimpleDateFormat sdf = new SimpleDateFormat(inputFormat);
            Calendar c = Calendar.getInstance();
            c.setTime(sdf.parse(input));
            c.add(Calendar.DATE, numberOfDays);
            return c.getTime();
        } catch (ParseException e) {
            Log.e("formatDateDisplay", "Date " + input + " " + e.getMessage());
        }
        return new Date();
    }

    public static String getCurrentDateTimeStrForTimezone(String timezone, String outputFormat) {
        Date now = new Date();
        TimeZone tz = TimeZone.getTimeZone(timezone);
        SimpleDateFormat destFormat = new SimpleDateFormat(outputFormat);
        destFormat.setTimeZone(tz);
        return destFormat.format(now);
    }

    public static int millisToHours(long millis) {
        return (int) (millis / (1000 * 60 * 60));
    }

    public static Matrix getClockwiseRotationMatrix(String imagePath) {
        Matrix matrix = new Matrix();
        try {
            ExifInterface exifInterface;
            exifInterface = new ExifInterface(imagePath);
            int orientation = exifInterface.getAttributeInt(ExifInterface.TAG_ORIENTATION, 1);
            switch (orientation) {
                case ExifInterface.ORIENTATION_UNDEFINED:
                    return null;
                case ExifInterface.ORIENTATION_NORMAL:
                    return null;
                case ExifInterface.ORIENTATION_ROTATE_90:
                    matrix.postRotate(90);
                    break;
                case ExifInterface.ORIENTATION_ROTATE_180:
                    matrix.postRotate(180);
                    break;
                case ExifInterface.ORIENTATION_ROTATE_270:
                    matrix.postRotate(270);
                    break;
            }

        } catch (IOException e) {
            e.printStackTrace();
        }
        return matrix;
    }
}