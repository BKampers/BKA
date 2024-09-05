/*
** © Bart Kampers
*/

package bka.demo.calendar;

import java.util.*;

public class FrenchRepublicanCalendarModel extends CalendarModel {

    public FrenchRepublicanCalendarModel(Calendar calendar) {
        super(calendar);
    }

    @Override
    public String getYear() {
        return String.format("%04d", getCalendar().get(Calendar.YEAR));
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

}
