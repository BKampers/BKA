/*
** © Bart Kampers
*/

package bka.demo.calendar;

import bka.awt.clock.*;
import bka.calendar.events.*;
import java.util.*;


public class EarthianCalendarPanelConfiguration extends CalendarPanelConfiguration {

    @Override
    public Scale getHourScale() {
        return new Scale(0, 23, 0, 23d / 24);
    }

    @Override
    public double getHourInterval() {
        return 2;
    }

    @Override
    public Optional<SolarDecorator> getDecorator() {
        return decorator;
    }

    private final Optional<SolarDecorator> decorator = Optional.of(new SolarDecorator(new SolarEventCalculator(new Location(51.48080, 5.64965), TimeZone.getTimeZone("Europe/Amsterdam"))));

}
