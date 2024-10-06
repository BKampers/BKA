/*
** © Bart Kampers
*/

package bka.demo.calendar;

import bka.calendar.*;
import java.awt.*;
import java.text.*;
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
    public Optional<String> getDayOfYear() {
        if (getCalendar().get(Calendar.DATE) == 8 && getCalendar().get(Calendar.MONTH) == EarthianCalendar.CAPRICORNUS) {
            return Optional.of("Kepler's birthday");
        }
        return Optional.empty();
    }

    @Override
    public double getHour() {
        return getCalendar().get(Calendar.HOUR_OF_DAY) + getMinute() / 60;
    }

    @Override
    public Optional<Color> getDayOfWeekColor() {
        return Optional.empty();
    }

    @Override
    public String getTimeToolTipText() {
        return new SimpleDateFormat("HH:mm").format(getCalendar().getTime());
    }

    @Override
    protected String getDateFormat() {
        return "%04d/%02d/%02d";
    }

}
