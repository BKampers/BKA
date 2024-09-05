/*
** © Bart Kampers
*/

package bka.demo.calendar;

import bka.awt.clock.*;


public class EarthianCalendarPanelConfiguration extends CalendarPanelConfiguration {

    @Override
    public Scale getHourScale() {
        return new Scale(0, 23, 0, 23d / 24);
    }

    @Override
    public double getHourInterval() {
        return 2;
    }

}
