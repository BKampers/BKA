/*
** © Bart Kampers
** This code may not be used for any purpose that harms humans (including
** exploitation and discrimination), humanity, the environment or the
** universe.
*/
package gpx;

import java.util.*;


/**
 * Type of GPS fix ({@code fix} / fixType).
 */
public enum Fix {

    NONE("none"),
    TWO_D("2d"),
    THREE_D("3d"),
    DGPS("dgps"),
    PPS("pps");

    private Fix(String value) {
        this.value = value;
    }

    public String getValue() {
        return value;
    }

    public static Fix fromValue(String value) {
        return Arrays.stream(values())
            .filter(fix -> fix.value.equals(value))
            .findAny()
            .orElseThrow(() -> new IllegalArgumentException("Unknown fix type: " + value));
    }

    private final String value;

}
