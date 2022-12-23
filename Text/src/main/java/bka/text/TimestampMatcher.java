/*
** © Bart Kampers
*/

package bka.text;

import java.util.*;
import java.util.regex.*;


public class TimestampMatcher {


    public Date getDate(String string) {
        Matcher matcher = datePattern().matcher(string);
        if (!matcher.matches()) {
            return null;
        }
        Calendar calendar = Calendar.getInstance();
        calendar.clear();
        calendar.set(Calendar.ERA, GregorianCalendar.AD);
        applyIfSet(calendar, Calendar.YEAR, matcher.group(YEAR));
        applyIfSet(calendar, Calendar.MONTH, month(matcher.group(MONTH)));
        applyIfSet(calendar, Calendar.DATE, matcher.group(DATE));
        applyIfSet(calendar, Calendar.HOUR, matcher.group(HOUR));
        applyIfSet(calendar, Calendar.MINUTE, matcher.group(MINUTE));
        applyIfSet(calendar, Calendar.SECOND, matcher.group(SECOND));
        applyIfSet(calendar, Calendar.MILLISECOND, thousandths(matcher.group(MILLISECOND)));
        return calendar.getTime();
    }


    private static Pattern datePattern() {
        return Pattern.compile(RegexUtil.namedDecimalGroup(YEAR) + RegexUtil.namedDecimalGroup("\\-", MONTH) + RegexUtil.optionalNamedDecimalGroup("\\-", DATE, timeRegex()));
    }


    private static String timeRegex() {
        return RegexUtil.optionalNamedDecimalGroup("T", HOUR, RegexUtil.optionalNamedDecimalGroup("\\:", MINUTE, RegexUtil.optionalNamedDecimalGroup("\\:", SECOND, RegexUtil.optionalNamedDecimalGroup("\\.", MILLISECOND))));
    }


    private static Integer month(String monthIndex) {
        if (monthIndex == null) {
            return null;
        }
        return Integer.parseInt(monthIndex) - 1;
    }


    private static Integer thousandths(String units) {
        if (units == null) {
            return null;
        }
        return Math.round(Float.parseFloat("." + units) * 1000.0f);
    }


    private static void applyIfSet(Calendar calendar, int field, String value) {
        if (value != null) {
            calendar.set(field, Integer.parseInt(value));
        }
    }


    private static void applyIfSet(Calendar calendar, int field, Integer value) {
        if (value != null) {
            calendar.set(field, value);
        }
    }


    private static final String YEAR = "year";
    private static final String MONTH = "month";
    private static final String DATE = "date";
    private static final String HOUR = "hour";
    private static final String MINUTE = "minute";
    private static final String SECOND = "second";
    private static final String MILLISECOND = "milli";

}
