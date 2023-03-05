/*
** © Bart Kampers
*/

package bka.demo.clock;

import java.text.*;


public class CardinalNumberFormat extends NumberFormat {

    @Override
    public StringBuffer format(double number, StringBuffer toAppendTo, FieldPosition position) {
        return format(Math.round(number), toAppendTo, position);
    }

    @Override
    public StringBuffer format(long number, StringBuffer toAppendTo, FieldPosition position) {
        String result = switch ((int) (number % 360)) {
            case 0:
                yield "N";
            case 45:
                yield "NO";
            case 90:
                yield "O";
            case 135:
                yield "ZO";
            case 180:
                yield "Z";
            case 225:
                yield "ZW";
            case 270:
                yield "W";
            case 315:
                yield "NW";
            default:
                throw new IllegalArgumentException("Cannot convert " + number + " to cardinal direction");
        };
        return toAppendTo.append(result);
    }

    @Override
    public Number parse(String string, ParsePosition position) {
        Double degrees = degrees(string.substring(position.getIndex()));
        if (degrees != null) {
            position.setIndex(position.getIndex() + string.length());
        }
        else {
            position.setErrorIndex(position.getIndex());
        }
        return degrees;
    }

    private static Double degrees(String cardinalDirection) {
        return switch (cardinalDirection) {
            case "N" ->
                0.0;
            case "NNO" ->
                22.5;
            case "NO" ->
                45.0;
            case "ONO" ->
                67.5;
            case "O" ->
                90.0;
            case "OZO" ->
                112.5;
            case "ZO" ->
                135.0;
            case "ZZO" ->
                157.5;
            case "Z" ->
                180.0;
            case "ZZW" ->
                202.5;
            case "ZW" ->
                225.0;
            case "WZW" ->
                247.5;
            case "W" ->
                270.0;
            case "WNW" ->
                292.5;
            case "NW" ->
                315.0;
            case "NNW" ->
                337.5;
            default ->
                null;
        };
    }
}
