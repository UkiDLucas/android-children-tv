package com.cyberwalkabout.common.util;

import android.content.Context;
import android.content.res.Configuration;
import android.net.ConnectivityManager;
import android.net.NetworkInfo;
import android.os.Build;
import android.os.Environment;
import android.provider.Settings;
import android.telephony.TelephonyManager;
import android.util.DisplayMetrics;
import android.util.Log;
import android.view.Display;
import android.view.WindowManager;

import java.io.File;
import java.net.InetAddress;
import java.net.NetworkInterface;
import java.net.SocketException;
import java.util.Enumeration;

public class Env {
    private static final String TAG = Env.class.getSimpleName();

    public static String DIRECTORY_DOWNLOADS = "Download";
    public static String DIRECTORY_ALARMS = "Alarms";
    public static String DIRECTORY_DCIM = "DCIM";
    public static String DIRECTORY_MOVIES = "Movies";
    public static String DIRECTORY_MUSIC = "Music";
    public static String DIRECTORY_NOTIFICATIONS = "Notifications";
    public static String DIRECTORY_PICTURES = "Pictures";
    public static String DIRECTORY_PODCASTS = "Podcasts";
    public static String DIRECTORY_RINGTONES = "Ringtones";

    private static final int NETWORK_TYPE_EHRPD = 14;
    private static final int NETWORK_TYPE_EVDO_B = 12;
    private static final int NETWORK_TYPE_HSPAP = 15;
    private static final int NETWORK_TYPE_IDEN = 11;
    private static final int NETWORK_TYPE_LTE = 13;
    private static final int NETWORK_TYPE_HSDPA = 8;
    private static final int NETWORK_TYPE_HSPA = 10;
    private static final int NETWORK_TYPE_HSUPA = 9;

    private static final double SCREEN_SIZE_SMALL = 3.5;

    private static volatile boolean externalStorageAvailable = false;
    private static volatile boolean externalStorageWritable = false;

    static {
        checkExternalStorageState();
        if (Env.atLeastFroyo()) {
            initDirectoryValues();
        }
    }

    public static void checkExternalStorageState() {
        String state = Environment.getExternalStorageState();
        if (Environment.MEDIA_MOUNTED.equals(state)) {
            externalStorageAvailable = externalStorageWritable = true;
        } else if (Environment.MEDIA_MOUNTED_READ_ONLY.equals(state)) {
            externalStorageAvailable = true;
            externalStorageWritable = false;
        } else {
            externalStorageAvailable = externalStorageWritable = false;
        }
    }

    private static void initDirectoryValues() {
        try {
            Class environmentClass = Class.forName("android.os.Environment");
            DIRECTORY_DOWNLOADS = (String) environmentClass.getDeclaredField("DIRECTORY_DOWNLOADS").get(null);
            DIRECTORY_ALARMS = (String) environmentClass.getDeclaredField("DIRECTORY_ALARMS").get(null);
            DIRECTORY_DCIM = (String) environmentClass.getDeclaredField("DIRECTORY_DCIM").get(null);
            DIRECTORY_MOVIES = (String) environmentClass.getDeclaredField("DIRECTORY_MOVIES").get(null);
            DIRECTORY_MUSIC = (String) environmentClass.getDeclaredField("DIRECTORY_MUSIC").get(null);
            DIRECTORY_NOTIFICATIONS = (String) environmentClass.getDeclaredField("DIRECTORY_NOTIFICATIONS").get(null);
            DIRECTORY_PICTURES = (String) environmentClass.getDeclaredField("DIRECTORY_PICTURES").get(null);
            DIRECTORY_PODCASTS = (String) environmentClass.getDeclaredField("DIRECTORY_PODCASTS").get(null);
            DIRECTORY_RINGTONES = (String) environmentClass.getDeclaredField("DIRECTORY_RINGTONES").get(null);
        } catch (Exception e) {
            Log.d("Env", "Failed to initialize system directory values");
        }
    }

    public static boolean atLeastEclair() {
        return Build.VERSION.SDK_INT >= 7;
    }

    public static boolean atLeastFroyo() {
        return Build.VERSION.SDK_INT >= 8;
    }

    public static boolean atLeastHoneycomb() {
        return Build.VERSION.SDK_INT >= 11;
    }

    public static boolean atLeastICS() {
        return Build.VERSION.SDK_INT >= 14;
    }

    public static boolean isTablet(Context context) {
        return (context.getResources().getConfiguration().screenLayout & Configuration.SCREENLAYOUT_SIZE_MASK) >= Configuration.SCREENLAYOUT_SIZE_LARGE;
    }

    public static float getScreenInches(Context ctx) {
        WindowManager windowManager = (WindowManager) ctx.getSystemService(Context.WINDOW_SERVICE);
        Display display = windowManager.getDefaultDisplay();
        DisplayMetrics dm = new DisplayMetrics();
        display.getMetrics(dm);
        float x = (float) Math.pow(display.getWidth() / dm.xdpi, 2);
        float y = (float) Math.pow(display.getHeight() / dm.ydpi, 2);
        float screenInches = (float) Math.sqrt(x + y);
        return screenInches;
    }

    public static boolean isSmallScreen(Context ctx) {
        return getScreenInches(ctx) <= SCREEN_SIZE_SMALL;
    }

    public static File getExternalDir(String dir) {
        File subDir = new File(Environment.getExternalStorageDirectory() + File.separator + dir);
        if (!subDir.exists()) {
            subDir.mkdirs();
        }
        return subDir;
    }

    public static File getAppDataSubDir(Context ctx, String subDirName) {
        File dir = getAppDataDir(ctx);
        File subDir = new File(dir.getAbsolutePath() + File.separatorChar + subDirName);
        if (!subDir.exists()) {
            subDir.mkdirs();
        }
        return subDir;
    }

    public static File getAppDataDir(Context ctx) {
        if (isExternalStorageAvailable()) {
            return new File(Environment.getExternalStorageDirectory().getAbsolutePath() + File.separator + "Android" + File.separator + "data" + File.separator + ctx.getPackageName());
        } else {
            return new File(Environment.getDataDirectory().getAbsolutePath() + File.separator + "data" + File.separator + ctx.getPackageName());
        }
    }

    public static boolean isExternalStorageAvailable() {
        return externalStorageAvailable && externalStorageWritable;
    }

    public static boolean isHoneycombTablet(Context context) {
        return atLeastHoneycomb() && isTablet(context);
    }

    public static boolean isAirplaneModeOn(Context ctx) {
        return Settings.System.getInt(ctx.getContentResolver(), Settings.System.AIRPLANE_MODE_ON, 0) != 0;
    }

    public static boolean isWifiConnected(Context ctx) {
        ConnectivityManager connManager = (ConnectivityManager) ctx.getSystemService(Context.CONNECTIVITY_SERVICE);
        NetworkInfo activeNetworkInfo = connManager.getActiveNetworkInfo();

        if (activeNetworkInfo != null) {
            return true;
        }

        //wifi
        NetworkInfo.State wifi = connManager.getNetworkInfo(1) != null ? connManager.getNetworkInfo(1).getState() : null;

        return wifi == NetworkInfo.State.CONNECTED || wifi == NetworkInfo.State.CONNECTING;
    }

    public static boolean isConnected(Context ctx) {
        ConnectivityManager connManager = (ConnectivityManager) ctx.getSystemService(Context.CONNECTIVITY_SERVICE);
        NetworkInfo activeNetworkInfo = connManager.getActiveNetworkInfo();

        if (activeNetworkInfo != null) {
            return true;
        }

        //mobile
        NetworkInfo.State mobile = connManager.getNetworkInfo(0) != null ? connManager.getNetworkInfo(0).getState() : null;
        //wifi
        NetworkInfo.State wifi = connManager.getNetworkInfo(1) != null ? connManager.getNetworkInfo(1).getState() : null;

        return mobile == NetworkInfo.State.CONNECTED || mobile == NetworkInfo.State.CONNECTING || wifi == NetworkInfo.State.CONNECTED || wifi == NetworkInfo.State.CONNECTING;
    }

    public static boolean isConnectedFast(Context context) {
        ConnectivityManager cm = (ConnectivityManager) context.getSystemService(Context.CONNECTIVITY_SERVICE);
        NetworkInfo info = cm.getActiveNetworkInfo();
        return (info != null && info.isConnected() && Env.isConnectionFast(info.getType(), info.getSubtype()));
    }

    public static boolean isConnectionFast(int type, int subType) {
        if (type == ConnectivityManager.TYPE_WIFI) {
            return true;
        } else if (type == ConnectivityManager.TYPE_MOBILE) {
            switch (subType) {
                case TelephonyManager.NETWORK_TYPE_1xRTT:
                    return false; // ~ 50-100 kbps
                case TelephonyManager.NETWORK_TYPE_CDMA:
                    return false; // ~ 14-64 kbps
                case TelephonyManager.NETWORK_TYPE_EDGE:
                    return false; // ~ 50-100 kbps
                case TelephonyManager.NETWORK_TYPE_EVDO_0:
                    return true; // ~ 400-1000 kbps
                case TelephonyManager.NETWORK_TYPE_EVDO_A:
                    return true; // ~ 600-1400 kbps
                case TelephonyManager.NETWORK_TYPE_GPRS:
                    return false; // ~ 100 kbps
                case Env.NETWORK_TYPE_HSDPA:
                    return true; // ~ 2-14 Mbps
                case Env.NETWORK_TYPE_HSPA:
                    return true; // ~ 700-1700 kbps
                case Env.NETWORK_TYPE_HSUPA:
                    return true; // ~ 1-23 Mbps
                case TelephonyManager.NETWORK_TYPE_UMTS:
                    return true; // ~ 400-7000 kbps
                // NOT AVAILABLE YET IN API LEVEL 7
                case Env.NETWORK_TYPE_EHRPD:
                    return true; // ~ 1-2 Mbps
                case Env.NETWORK_TYPE_EVDO_B:
                    return true; // ~ 5 Mbps
                case Env.NETWORK_TYPE_HSPAP:
                    return true; // ~ 10-20 Mbps
                case Env.NETWORK_TYPE_IDEN:
                    return false; // ~25 kbps
                case Env.NETWORK_TYPE_LTE:
                    return true; // ~ 10+ Mbps
                // Unknown
                case TelephonyManager.NETWORK_TYPE_UNKNOWN:
                    return false;
                default:
                    return false;
            }
        } else {
            return false;
        }
    }

    public static String getIp() {
        try {
            for (Enumeration<NetworkInterface> en = NetworkInterface.getNetworkInterfaces(); en.hasMoreElements(); ) {
                NetworkInterface intf = en.nextElement();
                for (Enumeration<InetAddress> enumIpAddr = intf.getInetAddresses(); enumIpAddr.hasMoreElements(); ) {
                    InetAddress inetAddress = enumIpAddr.nextElement();
                    if (!inetAddress.isLoopbackAddress()) {
                        return inetAddress.getHostAddress().toString();
                    }
                }
            }
        } catch (SocketException ex) {
            Log.e(TAG, ex.toString());
        }
        return null;
    }

    public static boolean checkClass(String className) {
        boolean classExists = true;
        try {
            Class.forName(className);
        } catch (ClassNotFoundException e) {
            classExists = false;
        }
        return classExists;
    }
}