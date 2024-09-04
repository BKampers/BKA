/*
** © Bart Kampers
*/

package bka.demo.clock.calendar;

import bka.awt.clock.*;


public class FrenchRepublicanCalendarPanelConfiguration extends CalendarPanelConfiguration {

    @Override
    public Scale getHourScale() {
        return new Scale(1, 10, 0.1, 1.0);
    }

    @Override
    public Scale getFractionScale() {
        return new Scale(0, 100);
    }

}
