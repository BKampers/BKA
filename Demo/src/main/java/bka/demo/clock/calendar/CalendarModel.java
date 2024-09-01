/*
** © Bart Kampers
*/

package bka.demo.clock.calendar;

import java.util.*;

public class CalendarModel {

    public CalendarModel(Calendar calendar) {
        this.calendar = Objects.requireNonNull(calendar);
    }

    public String getYear() {
        return Integer.toString(calendar.get(Calendar.YEAR));
    }

    public String getWeek() {
        return "week " + calendar.get(Calendar.WEEK_OF_YEAR) + getOptionalWeekYear();
    }

    private String getOptionalWeekYear() {
        if (calendar.getWeekYear() == calendar.get(Calendar.YEAR)) {
            return "";
        }
        return " (" + calendar.getWeekYear() + ')';
    }

    public String getDayOfWeek() {
        return calendar.getDisplayName(Calendar.DAY_OF_WEEK, Calendar.LONG, Locale.getDefault());
    }

    public String getDate() {
        return Integer.toString(calendar.get(Calendar.DATE));
    }

    public String getMonth() {
        return calendar.getDisplayName(Calendar.MONTH, Calendar.LONG, Locale.getDefault());
    }

    public double getHour() {
        return calendar.get(Calendar.HOUR) + getMinute() / 60;
    }

    double getMinute() {
        return calendar.get(Calendar.MINUTE) + getSecond() / 60;
    }

    double getSecond() {
        return calendar.get(Calendar.SECOND) + calendar.get(Calendar.MILLISECOND) / 1000d;
    }

    private final Calendar calendar;

}
