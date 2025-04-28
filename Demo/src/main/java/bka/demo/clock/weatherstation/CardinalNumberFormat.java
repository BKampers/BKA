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
        return DIRECTIONS.entrySet().stream()
            .skip(index(number))
            .findFirst()
            .get().getKey();
    }

    private static long index(double number) {
        return Math.round(degrees(number) / 22.5);
    }

    private static double degrees(double number) {
        return number - Math.floor(number / 360) * 360;
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
        return DIRECTIONS.get(cardinalDirection);
    }


    private static final Map<String, Double> DIRECTIONS = new LinkedHashMap<>();

    static {
        DIRECTIONS.put("N", 0.0);
        DIRECTIONS.put("NNO", 22.5);
        DIRECTIONS.put("NO", 45.0);
        DIRECTIONS.put("ONO", 67.5);
        DIRECTIONS.put("O", 90.0);
        DIRECTIONS.put("OZO", 112.5);
        DIRECTIONS.put("ZO", 135.0);
        DIRECTIONS.put("ZZO", 157.5);
        DIRECTIONS.put("Z", 180.0);
        DIRECTIONS.put("ZZW", 202.5);
        DIRECTIONS.put("ZW", 225.0);
        DIRECTIONS.put("WZW", 247.5);
        DIRECTIONS.put("W", 270.0);
        DIRECTIONS.put("WNW", 292.5);
        DIRECTIONS.put("NW", 315.0);
        DIRECTIONS.put("NNW", 337.5);
    }

}
