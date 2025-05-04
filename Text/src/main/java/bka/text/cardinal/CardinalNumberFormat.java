/*
** © Bart Kampers
** This code may not be used for any purpose that harms humans, humanity, the environment or the universe.
 */
package bka.text.cardinal;

import java.text.*;
import java.util.*;


public class CardinalNumberFormat extends NumberFormat {

    public CardinalNumberFormat() {
        this(Locale.getDefault());
    }

    public CardinalNumberFormat(Locale locale) {
        ResourceBundle bundle = ResourceBundle.getBundle(CardinalNumberFormat.class.getSimpleName(), locale);
        for (String key : KEYS) {
            directions.add(bundle.getString(key));
        }
    }

    @Override
    public StringBuffer format(long degrees, StringBuffer toAppendTo, FieldPosition position) {
        return format((double) degrees, toAppendTo, position);
    }

    @Override
    public StringBuffer format(double degrees, StringBuffer toAppendTo, FieldPosition position) {
        return toAppendTo.append(directions.get(index(degrees)));
    }

    private int index(double degrees) {
        if (!Double.isFinite(degrees)) {
            throw new IllegalArgumentException("Parameter degrees must be finite");
        }
        return ((int) Math.round(normalized(degrees) / INTERVAL)) % directions.size();
    }

    private static double normalized(double degrees) {
        return degrees - Math.floor(degrees / MAX_DEGREES) * MAX_DEGREES;
    }

    @Override
    public Number parse(String source, ParsePosition position) {
        if (source.length() <= position.getIndex()) {
            position.setErrorIndex(source.length());
            return null;
        }
        Optional<String> direction = directions.stream()
            .sorted(longestFirst())
            .filter(d -> source.startsWith(d, position.getIndex()))
            .findFirst();
        if (direction.isEmpty()) {
            position.setErrorIndex(position.getIndex());
            return null;
        }
        position.setIndex(position.getIndex() + direction.get().length());
        return directions.indexOf(direction.get()) * INTERVAL;
    }

    private static Comparator<String> longestFirst() {
        return (string1, string2) -> string2.length() - string1.length();
    }


    private final List<String> directions = new ArrayList<>();

    private static final String[] KEYS = { "N", "NNE", "NE", "ENE", "E", "ESE", "SE", "SSE", "S", "SSW", "SW", "WSW", "W", "WNW", "NW", "NNW" };
    private static final double MAX_DEGREES = 360;
    private static final double INTERVAL = MAX_DEGREES / KEYS.length;

}
