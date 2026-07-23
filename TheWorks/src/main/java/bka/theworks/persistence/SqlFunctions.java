package bka.theworks.persistence;

import java.text.*;


/**
 */
public class SqlFunctions {

    public static String unaccent(String text) {
        if (text == null) {
            return null;
        }
        return Normalizer.normalize(text, Normalizer.Form.NFD).replaceAll("\\p{M}+", "");
    }

}
