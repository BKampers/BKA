package bka.theworks.persistence;

import java.text.*;
import java.time.*;


/**
 * Provides special functions for database queries
 */
public final class SqlFunctions {

    private SqlFunctions() {
        // Utility class should not be instantiated
    }

    /**
     * @param text
     * @return given text without diacritical signs
     */
    public static String unaccent(String text) {
        if (text == null) {
            return null;
        }
        return Normalizer.normalize(text, Normalizer.Form.NFD).replaceAll("\\p{M}+", "");
    }

    public static long hours(long count) {
        return Duration.ofHours(count).toMillis();
    }

    public static long minutes(long count) {
        return Duration.ofMinutes(count).toMillis();
    }

    public static long seconds(long count) {
        return Duration.ofSeconds(count).toMillis();
    }

}
