package org.comp3607.analytics;

// Turns milliseconds into formatted minutes and seconds

public class TimeFormatter {

// Formats a duration in milliseconds as minutes and seconds

    public static String TimeFormat(long millis) {
        long totalSeconds = millis / 1000;
        long minutes = totalSeconds / 60;
        long seconds = totalSeconds % 60;

        if (seconds < 10) {
            return minutes + "m 0" + seconds + "s";
        }
        return minutes + "m " + seconds + "s";
    }
}