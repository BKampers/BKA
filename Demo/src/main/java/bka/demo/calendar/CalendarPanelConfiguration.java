/*
** © Bart Kampers
*/

package bka.demo.calendar;

import bka.awt.clock.*;
import java.util.*;


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

    public Optional<SolarDecorator> getDecorator() {
        return Optional.empty();
    }

}
