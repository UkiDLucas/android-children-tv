package com.cyberwalkabout.common.util;

import android.text.TextUtils;
import android.util.Log;
import android.webkit.MimeTypeMap;

import java.io.*;
import java.text.NumberFormat;
import java.text.ParseException;
import java.text.SimpleDateFormat;
import java.util.*;

public class StringUtils {

    public static String usDollarsPretty(float amount) {
        long rounded = Math.round(amount);

        if ((long) amount == rounded) {
            // whole number e.g. $20
            return "$" + rounded;
        } else {
            // has cents e.g. $19.99
            return convertToUSDollars(amount);
        }
    }

    public static String percentageRounded(float number) {
        return Math.round(number) + "%";
    }

    public static String humanReadableByteCount(long bytes, boolean si) {
        int unit = si ? 1000 : 1024;
        if (bytes < unit)
            return bytes + " B";
        int exp = (int) (Math.log(bytes) / Math.log(unit));
        String pre = (si ? "kMGTPE" : "KMGTPE").charAt(exp - 1) + (si ? "" : "i");
        return String.format("%.1f %sB", bytes / Math.pow(unit, exp), pre);
    }

    public static String collapseSpaces(String str) {
        if (TextUtils.isEmpty(str)) {
            return "";
        }
        char last = str.charAt(0);
        StringBuffer buf = new StringBuffer();
        for (int cIdx = 0; cIdx < str.length(); cIdx++) {
            char ch = str.charAt(cIdx);
            if (ch != ' ' || last != ' ') {
                buf.append(ch);
                last = ch;
            }
        }
        return buf.toString();
    }

    public static String convertToUSDollars(double amount) {
        Locale usLocale = new Locale("en", "US");
        NumberFormat usdFormat = NumberFormat.getCurrencyInstance(usLocale);
        Currency usDollar = Currency.getInstance("USD");
        usdFormat.setCurrency(usDollar);

        return usdFormat.format(amount);
    }

    public static String convertToUSPercentage(double amount) {
        Locale usLocale = new Locale("en", "US");
        NumberFormat percentageFormat = NumberFormat.getPercentInstance(usLocale);
        return percentageFormat.format(amount);
    }

    public static String extractAfterIgnoreCase(String str, String after) {
        String result = "";
        if (!TextUtils.isEmpty(after) && !TextUtils.isEmpty(str) && str.length() > after.length()) {
            String strLower = str.toLowerCase();
            String afterLower = after.toLowerCase();

            int startIndex = strLower.indexOf(afterLower);
            result = str.substring(startIndex + after.length(), str.length()).trim();
        }
        return result;
    }

    public static String formatDateDisplay(String input, String inputFormat, String displayFormat) {
        if (TextUtils.isEmpty(displayFormat)) {
            displayFormat = "MM/dd/yyyy";
        }
        try {
            Date date = new SimpleDateFormat(inputFormat, Locale.US).parse(input);
            return new SimpleDateFormat(displayFormat).format(date);
        } catch (ParseException e) {
            Log.e("formatDateDisplay", "Date " + input + " " + e.getMessage());
        }
        return "";
    }

    public static String formatDateDisplay(Date date, String displayFormat) {
        return formatDateDisplay(date.getTime(), displayFormat);
    }

    public static String formatDateDisplay(long input, String displayFormat) {
        if (TextUtils.isEmpty(displayFormat)) {
            displayFormat = "MM/dd/yyyy";
        }
        Date date = new Date(input);
        return new SimpleDateFormat(displayFormat).format(date);
    }

    public static String toString(InputStream in) {
        BufferedReader reader = new BufferedReader(new InputStreamReader(in));
        StringBuilder sb = new StringBuilder();

        if (in != null) {
            String line = null;
            try {
                while ((line = reader.readLine()) != null) {
                    sb.append(line + "\n");
                }
            } catch (IOException e) {
                e.printStackTrace();
            } finally {
                try {
                    in.close();
                } catch (IOException e) {
                    e.printStackTrace();
                }
            }
        }
        return sb.toString();
    }

    public static String toString(Collection collection) {
        return toString(collection, null);
    }

    public static String toString(Collection collection, String separator) {
        if (separator == null)
            separator = ", ";
        StringBuilder result = new StringBuilder();

        if (collection != null && collection.size() > 0) {
            for (Object obj : collection) {
                result.append(obj.toString());
                result.append(separator);
            }
            result.delete(result.length() - separator.length(), result.length());
        }
        return result.toString();
    }

    public static String toString(Object[] array) {
        return toString(array, null);
    }

    public static String toString(Object[] array, String separator) {
        if (separator == null)
            separator = ", ";
        StringBuilder result = new StringBuilder();

        if (array != null && array.length > 0) {
            for (int i = 0; i < array.length; i++) {
                result.append(array[i].toString());
                if (i != array.length - 1) {
                    result.append(separator);
                }
            }
        }

        return result.toString();
    }

    public static String toStringWithQuotes(List list) {
        StringBuilder result = new StringBuilder();

        if (list != null && list.size() > 0) {
            for (int i = 0; i < list.size(); i++) {
                result.append("'").append(list.get(i).toString()).append("'");
                if (i != list.size() - 1) {
                    result.append(", ");
                }
            }
        }
        return result.toString();
    }

    public static String getFileExtension(File file) {
        return getFileExtension(file.getAbsolutePath());
    }

    public static String getFileExtension(String filename) {
        return MimeTypeMap.getFileExtensionFromUrl(filename);
    }

    public static String capitalizeFirstLetters(String s) {

        for (int i = 0; i < s.length(); i++) {

            if (i == 0) {
                // Capitalize the first letter of the string.
                s = String.format("%s%s", Character.toUpperCase(s.charAt(0)), s.substring(1));
            }

            // Is this character a non-letter or non-digit? If so
            // then this is probably a word boundary so let's capitalize
            // the next character in the sequence.
            if (!Character.isLetterOrDigit(s.charAt(i))) {
                if (i + 1 < s.length()) {
                    s = String.format("%s%s%s", s.subSequence(0, i + 1), Character.toUpperCase(s.charAt(i + 1)), s.substring(i + 2));
                }
            }

        }
        return s;
    }

    /**
     * Will take a url such as http://www.stackoverflow.com and return www.stackoverflow.com
     *
     * @param url
     * @return
     */
    public static String getHost(String url) {
        if (url == null || url.length() == 0)
            return "";

        int doubleslash = url.indexOf("//");
        if (doubleslash == -1)
            doubleslash = 0;
        else
            doubleslash += 2;

        int end = url.indexOf('/', doubleslash);
        end = end >= 0 ? end : url.length();

        return url.substring(doubleslash, end);
    }


    /**
     * Based on : http://grepcode.com/file/repository.grepcode.com/java/ext/com.google.android/android/2.3.3_r1/android/webkit/CookieManager.java#CookieManager.getBaseDomain%28java.lang.String%29
     * Get the base domain for a given host or url. E.g. mail.google.com will return google.com
     *
     * @param host
     * @return
     */
    public static String getBaseDomain(String url) {
        String host = getHost(url);

        int startIndex = 0;
        int nextIndex = host.indexOf('.');
        int lastIndex = host.lastIndexOf('.');
        while (nextIndex < lastIndex) {
            startIndex = nextIndex + 1;
            nextIndex = host.indexOf('.', startIndex);
        }
        if (startIndex > 0) {
            return host.substring(startIndex);
        } else {
            return host;
        }
    }
}
