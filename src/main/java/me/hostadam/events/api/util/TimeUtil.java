package me.hostadam.events.api.util;

import java.util.Map;
import java.util.concurrent.TimeUnit;

public class TimeUtil {


    private static final Map<Character, Long> CHARACTERS = Map.of(
            'y', TimeUnit.DAYS.toMillis(365L),
            'M', TimeUnit.DAYS.toMillis(30L),
            'd', TimeUnit.DAYS.toMillis(1L),
            'h', TimeUnit.HOURS.toMillis(1L),
            'm', TimeUnit.MINUTES.toMillis(1L),
            's', TimeUnit.SECONDS.toMillis(1L)
    );

    public static long parse(String string) {
        if(string.equalsIgnoreCase("lifetime") || string.equalsIgnoreCase("permanent")
                || string.equalsIgnoreCase("forever") || string.equalsIgnoreCase("perm")) {
            return Long.MAX_VALUE;
        }


        long result = -1;
        int startIndex = 0;

        for(int index = 0; index < string.length(); index++) {
            char charAt = string.charAt(index);
            if(!CHARACTERS.containsKey(charAt)) continue;

            try {
                String substring = string.substring(startIndex, index);
                int parsedInt = Integer.parseInt(substring);
                result += (parsedInt * CHARACTERS.get(charAt)) + 1;

                startIndex = index + 1;
            } catch (Exception exception) {
                return -1;
            }
        }

        return result;
    }

    public static String formatSimple(long duration) {
        if(duration == Long.MAX_VALUE) {
            return "Permanent";
        }

        int sec = (int) (duration / 1000) % 60 ;
        int min = (int) ((duration / (1000*60)) % 60);
        int hour = (int) ((duration / (1000*60*60)) % 24);
        int days = (int) ((duration / (1000*60*60*24)) % 365);

        StringBuilder builder = new StringBuilder();

        if(days > 0) {
            builder.append(days + "d");
        }
        if(hour > 0) {
            builder.append(hour + "h");
        }
        if(min > 0) {
            builder.append(min + "m");
        }
        if(sec > 0) {
            builder.append(sec + "s");
        }

        String string = builder.toString();
        if(string.isEmpty()) {
            string = "0s";
        }

        return string;
    }
}
