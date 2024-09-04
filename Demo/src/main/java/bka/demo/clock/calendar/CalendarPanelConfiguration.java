/*
** © Bart Kampers
*/

package bka.demo.clock.calendar;

import bka.awt.clock.*;


public class CalendarPanelConfiguration {

    public Scale getHourScale() {
        return new Scale(1, 12, 1d / 12, 1);
    }

    public double getHourInterval() {
        return 1;
    }

    public Scale getFractionScale() {
        return new Scale(0, 60);
    }

}
