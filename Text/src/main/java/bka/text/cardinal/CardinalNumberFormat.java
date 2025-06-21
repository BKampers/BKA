/*
** © Bart Kampers
** This code may not be used for any purpose that harms humans, humanity, the environment or the universe.
*/
package bka.text.cardinal;

import java.text.*;
import java.util.*;
import java.util.stream.*;

/**
 * Concrete subclass of java.text.NumberFormat that converts between degrees (absolute bearing) and cardinal (compass) directions like N, E, S, W, NE,
 * WSW, WbN, etc.<p/>
 * Supports translation of the direcitons to English, German and Dutch. Other locales default to English.<br/>
 * Supports formatting up to tertiary intercardinal direction. The default is secondary intercardinal direction.<br/>
 * See https://en.wikipedia.org/wiki/Cardinal_direction
 */
public class CardinalNumberFormat extends NumberFormat {

    /**
     * @return FieldPosition to format to cardinal directions.
     */
    public static FieldPosition cardinalFieldPostion() {
        return new FieldPosition(CARDINAL);
    }

    /**
     * @return FieldPosition to format to intercardinal directions.
     */
    public static FieldPosition intercardinalFieldPostion() {
        return new FieldPosition(INTERCARDINAL);
    }

    /**
     * @return FieldPosition to format to secondary intercardinal directions.
     */
    public static FieldPosition secondaryIntercardinalFieldPostion() {
        return new FieldPosition(SECONDARY_INTERCARDINAL);
    }

    /**
     * @return FieldPosition to format to tertiary intercardinal directions.
     */
    public static FieldPosition tertiaryIntercardinalFieldPostion() {
        return new FieldPosition(TERTIARY_INTERCARDINAL);
    }

    /**
     * Creates an instance for the default locale
     */
    public CardinalNumberFormat() {
        this(Locale.getDefault());
    }

    /**
     * Creates an instance for given locale.
     *
     * @param locale
     */
    public CardinalNumberFormat(Locale locale) {
        bundle = ResourceBundle.getBundle(CardinalNumberFormat.class.getSimpleName(), locale);
    }
    
    /**
     * Creates an instance with given translation bundle. The bundle may contain an incomplete 
     * set of keys, for example only N, E, S and W. In that case this instance will not be able 
     * to convert to or from any intercardinal format, but only to or from cardinal format.
     *
     * @param bundle
     */
    public CardinalNumberFormat(ResourceBundle bundle) {
        this.bundle = Objects.requireNonNull(bundle);
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
