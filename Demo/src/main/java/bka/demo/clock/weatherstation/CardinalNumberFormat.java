package bka.demo.clock.weatherstation;

/*
** © Bart Kampers
*/

import java.text.*;
import java.util.*;


public class CardinalNumberFormat extends NumberFormat {

    @Override
    public StringBuffer format(long number, StringBuffer toAppendTo, FieldPosition position) {
        return format((double) number, toAppendTo, position);
    }

    @Override
    public StringBuffer format(double number, StringBuffer toAppendTo, FieldPosition position) {
        return toAppendTo.append(toCardinalString(number));
    }

    private static String toCardinalString(double number) {
        return DIRECTIONS.get(index(number));
    }

    private static int index(double number) {
        return (int) Math.round(degrees(number) / INTERVAL);
    }

    private static double degrees(double number) {
        return number - Math.floor(number / MAX_DEGREES) * MAX_DEGREES;
    }

    @Override
    public Number parse(String string, ParsePosition position) {
        Double degrees = degrees(string.substring(position.getIndex()));
        if (degrees == null) {
            position.setErrorIndex(position.getIndex());
            return null;
        }
        position.setIndex(position.getIndex() + string.length());
        return degrees;
    }

    private static Double degrees(String cardinalDirection) {
        int index = DIRECTIONS.indexOf(cardinalDirection);
        return (index < 0) ? null : index * INTERVAL;
    }


    private static final List<String> DIRECTIONS = List.of("N", "NNO", "NO", "ONO", "O", "OZO", "ZO", "ZZO", "Z", "ZZW", "ZW", "WZW", "W", "WNW", "NW", "NNW");
    private static final double MAX_DEGREES = 360;
    private static final double INTERVAL = MAX_DEGREES / DIRECTIONS.size();

}
