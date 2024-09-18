/*
** © Bart Kampers
*/

package bka.demo.calendar;

import java.awt.*;
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
        if (!calendar.isWeekDateSupported() || calendar.getWeekYear() == calendar.get(Calendar.YEAR)) {
            return "";
        }
        return " (" + calendar.getWeekYear() + ')';
    }

    public Optional<String> getDayOfYear() {
        return Optional.ofNullable(calendar.getDisplayName(Calendar.DAY_OF_YEAR, Calendar.LONG, Locale.getDefault()));
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

    public double getMinute() {
        return calendar.get(Calendar.MINUTE) + getSecond() / 60;
    }

    public double getSecond() {
        int second = calendar.get(Calendar.SECOND);
        if (calendar.get(Calendar.MILLISECOND) >= 500) {
            return second + 1;
        }
        return second;
    }

    public Optional<Color> getDayOfWeekColor() {
        if (calendar.get(Calendar.DAY_OF_WEEK) == Calendar.SUNDAY) {
            return Optional.of(Color.RED);
        }
        return Optional.empty();
    }

    public Optional<Color> getDayOfYearColor() {
        return Optional.empty();
    }

    protected Calendar getCalendar() {
        return calendar;
    }

    private final Calendar calendar;

}
