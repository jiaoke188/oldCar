package com.example.carrentalapp.util;

import java.text.SimpleDateFormat;
import java.util.Date;
import java.util.Locale;

public class FormatUtils {

    private static final SimpleDateFormat DATE_FORMAT = new SimpleDateFormat("yyyy-MM-dd", Locale.CHINA);
    private static final SimpleDateFormat DATE_TIME_FORMAT = new SimpleDateFormat("yyyy-MM-dd HH:mm", Locale.CHINA);

    public static String formatDate(long timestamp) {
        if (timestamp <= 0) {
            return "";
        }
        return DATE_FORMAT.format(new Date(timestamp));
    }

    public static String formatDateTime(long timestamp) {
        if (timestamp <= 0) {
            return "";
        }
        return DATE_TIME_FORMAT.format(new Date(timestamp));
    }

    public static String safe(String value) {
        return value == null ? "" : value;
    }
}
