/*
** © Bart Kampers
** This code may not be used for any purpose that harms humans, humanity, the environment or the universe.
 */
package bka.text.cardinal;

import java.text.*;
import java.util.*;
import java.util.stream.*;


public class CardinalNumberFormat extends NumberFormat {

    public static FieldPosition cardinalFieldPostion() {
        return new FieldPosition(CARDINAL);
    }

    public static FieldPosition interCardinalFieldPostion() {
        return new FieldPosition(INTERCARDINAL);
    }

    public static FieldPosition secondaryInterCardinalFieldPostion() {
        return new FieldPosition(SECONDARY_INTERCARDINAL);
    }

    public static FieldPosition tertiaryInterCardinalFieldPostion() {
        return new FieldPosition(TERTIARY_INTERCARDINAL);
    }

    public CardinalNumberFormat() {
        this(Locale.getDefault());
    }

    public CardinalNumberFormat(Locale locale) {
        bundle = ResourceBundle.getBundle(CardinalNumberFormat.class.getSimpleName(), locale);
    }

    @Override
    public StringBuffer format(long degrees, StringBuffer toAppendTo, FieldPosition fieldPosition) {
        return toAppendTo.append(getDirection(degrees, fieldPosition));
    }

    @Override
    public StringBuffer format(double degrees, StringBuffer toAppendTo, FieldPosition fieldPosition) {
        if (!Double.isFinite(degrees)) {
            throw new IllegalArgumentException("degrees must be finite");
        }
        return toAppendTo.append(getDirection(degrees, fieldPosition));
    }

    private String getDirection(double degrees, FieldPosition fieldPosition) {
        List<String> directions = directions(field(fieldPosition));
        return directions.get(index(degrees, directions));
    }

    private int field(FieldPosition position) {
        int field = position.getField();
        return (field < CARDINAL || TERTIARY_INTERCARDINAL < field) ? SECONDARY_INTERCARDINAL : field;
    }

    private int index(double degrees, List<String> directions) {
        int index = (int) (Math.round(degrees / (MAX_DEGREES / directions.size())) % directions.size());
        return (index < 0) ? index + directions.size() : index;
    }

    @Override
    public Number parse(String source, ParsePosition position) {
        if (source.length() <= position.getIndex()) {
            position.setErrorIndex(source.length());
            return null;
        }
        List<String> directions = directions(TERTIARY_INTERCARDINAL);
        Optional<String> direction = directions.stream()
            .sorted(longestFirst())
            .filter(d -> source.startsWith(d, position.getIndex()))
            .findFirst();
        if (direction.isEmpty()) {
            position.setErrorIndex(position.getIndex());
            return null;
        }
        position.setIndex(position.getIndex() + direction.get().length());
        return directions.indexOf(direction.get()) * (MAX_DEGREES / directions.size());
    }

    private static Comparator<String> longestFirst() {
        return (string1, string2) -> string2.length() - string1.length();
    }

    private List<String> directions(int field) {
        return directionsCache.computeIfAbsent(field, f -> switch (field) {
            case TERTIARY_INTERCARDINAL ->
                Stream.of(KEYS).map(bundle::getString).collect(Collectors.toList());
            case SECONDARY_INTERCARDINAL ->
                Stream.of(KEYS).filter(key -> !key.contains("b")).map(bundle::getString).collect(Collectors.toList());
            default ->
                Stream.of(KEYS).filter(key -> key.length() <= field).map(bundle::getString).collect(Collectors.toList());
        });
    }

    private final ResourceBundle bundle;
    private final Map<Integer, List<String>> directionsCache = new HashMap<>();

    private static final String[] KEYS = {
        "N", "NbE", "NNE", "NEbN", "NE", "NEbE", "ENE", "EbN",
        "E", "EbS", "ESE", "SEbE", "SE", "SEbS", "SSE", "SbE",
        "S", "SbW", "SSW", "SWbS", "SW", "SWbW", "WSW", "WbS",
        "W", "WbN", "WNW", "NWbW", "NW", "NWbN", "NNW", "NbW" };

    private static final double MAX_DEGREES = 360;

    private static final int CARDINAL = 1;
    private static final int INTERCARDINAL = 2;
    private static final int SECONDARY_INTERCARDINAL = 3;
    private static final int TERTIARY_INTERCARDINAL = 4;

}
