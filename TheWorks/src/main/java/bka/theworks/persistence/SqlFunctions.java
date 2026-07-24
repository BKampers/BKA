package bka.theworks.persistence;

import java.text.*;


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

}
