package com.cyberwalkabout.common.util;

import java.text.*;
import java.util.Date;

/**
 * @author Andrii Kovalov
 */
public class DateUtils extends android.text.format.DateUtils {
    public static Date parse(String date, String format) {
        try {
            return new SimpleDateFormat(format).parse(date);
        }  catch (java.text.ParseException e) {
            return null;
        }
    }
}
