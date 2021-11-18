package com.codewizards.meshify_chat.util;

import android.content.res.Resources;

import org.apache.commons.lang3.StringUtils;
import org.joda.time.DateTime;
import org.joda.time.LocalDateTime;
import com.codewizards.meshify_chat.R;

import java.util.Random;

public class MeshifyUtils {

    public static final String ZERO_LEADING_NUMBER_FORMAT = "%02d";
    public static final String NUMBER_FORMAT = "%d";

    public static String generateInitials(String str) {
        try {
            if (str.length() > 1) {
                String[] split = str.replace("(", "").replace(")", "").replace("+", "").split(StringUtils.SPACE);
                if (split.length == 1) {
                    String str2 = split[0];
                    return ("" + str2.charAt(0) + str2.charAt(str2.length() - 1)).toUpperCase();
                } else if (split.length < 2) {
                    return "--";
                } else {
                    return ("" + split[0].charAt(0) + split[1].charAt(0)).toUpperCase();
                }
            } else {
                return str.charAt(0) + "";
            }
        } catch (Exception unused) {
            return "--";
        }
    }

    public static String getRandomColor() {
        Random r = new Random();
        StringBuilder sb = new StringBuilder("#");
        while (sb.length() < 7) {
            sb.append(Integer.toHexString(r.nextInt()));
        }
        return sb.toString().substring(0, 7);
    }


    public static String getMessageDate(String str) {
        LocalDateTime localDateTime = new DateTime().toLocalDateTime();
        Resources resources = Resources.getSystem();
        try {
            LocalDateTime localDateTime2 = new DateTime(Long.parseLong(str)).toLocalDateTime();
            if (localDateTime.getDayOfMonth() == localDateTime2.getDayOfMonth()) {
                return String.format(MeshifyUtils.ZERO_LEADING_NUMBER_FORMAT, Integer.valueOf(localDateTime2.getHourOfDay())) + ":" + String.format(MeshifyUtils.ZERO_LEADING_NUMBER_FORMAT, Integer.valueOf(localDateTime2.getMinuteOfHour()));
            } else if (localDateTime2.getDayOfMonth() == localDateTime.minusDays(1).getDayOfMonth()) {
                return resources.getString(R.string.message_date_yesterday);
            } else {
                if (localDateTime.minusDays(6).isBefore(localDateTime2)) {
                    return resources.getStringArray(R.array.days_of_the_week)[localDateTime2.getDayOfWeek() - 1];
                }
                return String.format(MeshifyUtils.ZERO_LEADING_NUMBER_FORMAT, Integer.valueOf(localDateTime2.getDayOfMonth())) + "/" + String.format(MeshifyUtils.ZERO_LEADING_NUMBER_FORMAT, Integer.valueOf(localDateTime2.getMonthOfYear())) + "/" + String.format(MeshifyUtils.ZERO_LEADING_NUMBER_FORMAT, Integer.valueOf(localDateTime2.getYear()));
            }
        } catch (IllegalArgumentException unused) {
            return "";
        }
    }

}