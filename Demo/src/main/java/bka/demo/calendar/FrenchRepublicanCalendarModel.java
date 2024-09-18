/*
** © Bart Kampers
*/

package bka.demo.calendar;

import bka.calendar.*;
import bka.text.roman.*;
import java.awt.*;
import java.util.*;

public class FrenchRepublicanCalendarModel extends CalendarModel {

    public FrenchRepublicanCalendarModel(Calendar calendar) {
        super(calendar);
    }

    @Override
    public String getYear() {
        return roman.format(getCalendar().get(Calendar.YEAR));
    }

    @Override
    public String getWeek() {
        return "décade " + getCalendar().get(Calendar.WEEK_OF_YEAR);
    }

    @Override
    public double getHour() {
        return getCalendar().get(Calendar.HOUR_OF_DAY) + getMinute() / 100;
    }

    @Override
    public double getMinute() {
        return getCalendar().get(Calendar.MINUTE) + getSecond() / 100;
    }

    @Override
    public double getSecond() {
        return getCalendar().get(Calendar.SECOND) + getCalendar().get(Calendar.MILLISECOND) / 1000d;
    }

    @Override
    public String getMonth() {
        if (getCalendar().get(Calendar.MONTH) == FrenchRepublicanCalendar.JOURS_COMPLEMENTAIRES) {
            return "";
        }
        return super.getMonth();
    }

    @Override
    public String getDayOfWeek() {
        if (getCalendar().get(Calendar.MONTH) == FrenchRepublicanCalendar.JOURS_COMPLEMENTAIRES) {
            return "";
        }
        return super.getDayOfWeek();
    }

    @Override
    public Optional<Color> getDayOfWeekColor() {
        if (getCalendar().get(Calendar.DAY_OF_WEEK) == FrenchRepublicanCalendar.DECADI) {
            return Optional.of(Color.RED);
        }
        return Optional.empty();
    }

    public Optional<Color> getDayOfYearColor() {
        if (getCalendar().get(Calendar.MONTH) == FrenchRepublicanCalendar.JOURS_COMPLEMENTAIRES) {
            return Optional.of(Color.RED);
        }
        return Optional.empty();
    }

    private final RomanNumberFormat roman = new RomanNumberFormat();

}
