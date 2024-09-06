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
    public Optional<Color> getDateColor() {
        if (getCalendar().get(Calendar.DAY_OF_WEEK) == FrenchRepublicanCalendar.DECADI || getCalendar().get(Calendar.MONTH) == FrenchRepublicanCalendar.JOURS_COMPLEMENTAIRES) {
            return Optional.of(Color.RED);
        }
        return Optional.empty();
    }

    final RomanNumberFormat roman = new RomanNumberFormat();

}
