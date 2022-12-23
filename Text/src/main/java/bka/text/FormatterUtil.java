/*
** © Bart Kampers
*/

package bka.text;

import java.math.*;
import java.util.*;
import java.util.regex.*;

public class FormatterUtil {


    public static String format(String format, Number number) {
        return format(format, Locale.getDefault(), number);
    }


    public static String format(String format, Locale locale, Number number) {
        Objects.requireNonNull(locale);
        if (format == null || format.isEmpty()) {
            return Objects.toString(number);
        }
        if (number == null) {
            return formatNumber(format, locale, number);
        }
        Matcher matcher = CONVERSION_PATTERN.matcher(format);
        if (matcher.find()) {
            return formatNumber(format, locale, convertIfRequired(matcher, number));
        }
        return formatDate(format, locale, number);
    }


    private static Number convertIfRequired(Matcher matcher, Number number) {
        if (number == null) {
            return null;
        }
        switch (matcher.group(CONVERSION).charAt(0)) {
            case 'd':
            case 'x':
            case 'X':
            case 'o':
                return convertToInteger(number);
            case 'f':
            case 'e':
            case 'E':
            case 'g':
            case 'G':
            case 'a':
            case 'A':
                return convertToFloatingPoint(number);
            case 'c':
            case 'C':
                return number.intValue();
        }
        return number;
    }


    private static Number convertToInteger(Number number) {
        if (number instanceof Float) {
            return number.intValue();
        }
        if (number instanceof BigDecimal) {
            return ((BigDecimal) number).toBigInteger();
        }
        return number.longValue();
    }


    private static Number convertToFloatingPoint(Number number) {
        if (number instanceof Double || number instanceof Float || number instanceof BigDecimal) {
            return number;
        }
        if (number instanceof Integer || number instanceof Short || number instanceof Byte) {
            return number.floatValue();
        }
        if (number instanceof BigInteger) {
            return new BigDecimal(number.toString());
        }
        return number.doubleValue();
    }


    private static String formatDate(String format, Locale locale, Number number) {
        java.text.SimpleDateFormat formatter = new java.text.SimpleDateFormat(format, locale);
        return formatter.format(new Date(number.longValue()));
    }


    private static String formatNumber(String format, Locale locale, Number number) {
        java.util.Formatter formatter = new java.util.Formatter(locale);
        return formatter.format(format, number).toString();
    }


    private static final String CONVERSION = "conversion";
    private static final Pattern CONVERSION_PATTERN = Pattern.compile("%\\d*(\\.\\d+)?" + RegexUtil.namedCharacter(CONVERSION, "dxXhHofeEgGaAsScCbB"));
}
