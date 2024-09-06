/*
** © Bart Kampers
*/

package bka.demo.calendar;

import java.awt.*;
import java.util.*;

public class EarthianCalendarModel extends CalendarModel {

    public EarthianCalendarModel(Calendar calendar) {
        super(calendar);
    }

    @Override
    public String getYear() {
        return String.format("%04d", getCalendar().get(Calendar.YEAR));
    }

    @Override
    public double getHour() {
        return getCalendar().get(Calendar.HOUR_OF_DAY) + getMinute() / 60;
    }

    @Override
    public Optional<Color> getDateColor() {
        return Optional.empty();
    }

}
