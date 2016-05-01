package com.cyberwalkabout.common.util;

import android.content.Context;
import android.location.Location;
import android.location.LocationManager;

import java.text.DecimalFormat;

public class DistanceUtils {

    // 1 meter = 0.000621371192 miles
    private static final double CONVERSION_UNIT = 0.00062137119;

    // calculates distance between two geo poins in miles
    public static double distance(double lat1, double lon1, double lat2, double lon2) {
        Location locationA = new Location(LocationManager.NETWORK_PROVIDER);
        locationA.setLatitude(lat1);
        locationA.setLongitude(lon1);

        Location locationB = new Location(LocationManager.NETWORK_PROVIDER);
        locationB.setLatitude(lat2);
        locationB.setLongitude(lon2);

        return locationA.distanceTo(locationB) * CONVERSION_UNIT;
    }

    public static double distance(Location locationA, Location locationB) {
        return locationA.distanceTo(locationB) * CONVERSION_UNIT;
    }

    public static String getDistanceToLocationStr(Context context, double destLatitude, double destLongitude) {
        return formatDistance(getDistanceToLocation(context, destLatitude, destLongitude), "#.##");
    }

    public static double getDistanceToLocation(Context context, double destLatitude, double destLongitude) {
        Location myLocation = Sys.getLatestKnownLocation(context);
        double result = 0.0;
        if (myLocation != null) {
            result = DistanceUtils.distance(myLocation.getLatitude(), myLocation.getLongitude(), destLatitude, destLongitude);
        }
        return result;
    }

    public static String getDistanceToLocationStr(Context context, String format, double destLatitude, double destLongitude) {
        return formatDistance(getDistanceToLocation(context, destLatitude, destLongitude), format);
    }

    public static String formatDistance(double distance, String format) {
        DecimalFormat decimalFormat = new DecimalFormat(format);
        decimalFormat.setMinimumFractionDigits(2);
        decimalFormat.setMaximumFractionDigits(2);
        return decimalFormat.format(distance);
    }

    public static double metersToMiles(int meters) {
        return meters * CONVERSION_UNIT;
    }
}