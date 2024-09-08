/*
** © Bart Kampers
** This code may not be used for any purpose that harms humans (including
** exploitation and discrimination), humanity, the environment or the
** universe.
*/
package bka.calendar;

import static java.util.Calendar.DAY_OF_WEEK;
import static java.util.Calendar.MONTH;
import static java.util.Calendar.WEEK_OF_YEAR;
import static java.util.Calendar.YEAR;
import java.util.*;


public class FrenchRepublicanCalendar extends Calendar {

    
    public static final int VENDEMIAIRE = 0;
    public static final int BRUMAIRE = 1;
    public static final int FRIMAIRE = 2;
    public static final int NIVOSE = 3;
    public static final int PLUVIOSE =  4;
    public static final int VENTOSE = 5;
    public static final int GERMINAL = 6;
    public static final int FLOREAL = 7;
    public static final int PRAIRIAL = 8;
    public static final int MESSIDOR = 9;
    public static final int THERMIDOR = 10;
    public static final int FRUCTIDOR = 11;
    public static final int JOURS_COMPLEMENTAIRES = 12;
    
    public static final int PRIMIDI = 1;
    public static final int DUODI = 2;
    public static final int TRIDI = 3;
    public static final int QUARTIDI = 4;
    public static final int QUINTIDI = 5;
    public static final int SEXTIDI = 6;
    public static final int SEPTIDI = 7;
    public static final int OCTIDI = 8;
    public static final int NONIDI = 9;
    public static final int DECADI = 10;
    
    public static final int JOUR_DE_LA_VERTU = 0;
    public static final int JOUR_DU_GENIE = 1;
    public static final int JOUR_DU_TRAVAIL = 2;
    public static final int JOUR_DE_L_OPINION = 3;
    public static final int JOUR_DES_RECOMPENSES = 4;
    public static final int JOUR_DE_LA_REVOLUTION = 5;
    
    public FrenchRepublicanCalendar() {
        setFirstDayOfWeek(PRIMIDI);
        setMinimalDaysInFirstWeek(DAYS_PER_WEEK);
    }

    public static FrenchRepublicanCalendar getInstance() {
        return new FrenchRepublicanCalendar();
    }

    @Override
    protected void computeTime() {
        throw new UnsupportedOperationException("Not supported yet.");
    }

    @Override
    protected void computeFields() {
        fields[ERA] = 1;
        isSet[ERA] = true;
        long remainder = time - LEAP_ORIGIN;
        int periodCount = (int) (remainder / FOUR_MILLENNIA);
        fields[YEAR] = periodCount * 4000;
        remainder %= FOUR_MILLENNIA;
        long yearStart = LEAP_ORIGIN + periodCount * FOUR_MILLENNIA;
        periodCount = (int) (remainder / FOUR_CENTURIES);
        yearStart += periodCount * FOUR_CENTURIES;
        fields[YEAR] += periodCount * 400;
        remainder %= FOUR_CENTURIES;
        periodCount = (int) (remainder / ONE_CENTURY);
        if (periodCount > 3) {
            periodCount = 3;
            remainder -= 3 * ONE_CENTURY;
        }
        else {
            remainder %= ONE_CENTURY;        
        }
        yearStart += periodCount * ONE_CENTURY;
        fields[YEAR] += periodCount * 100;
        periodCount = (int) (remainder / FOUR_YEARS);
        yearStart += periodCount * FOUR_YEARS;
        fields[YEAR] += periodCount * 4;
        remainder %= FOUR_YEARS;
        periodCount = Math.min((int) (remainder / ONE_YEAR), 3);
        if (remainder < 0) {
            periodCount--;
        }
        yearStart += periodCount * ONE_YEAR;
        fields[YEAR] += periodCount;
        isSet[YEAR] = true;
        int dayIndex = (int) ((time - yearStart) / ONE_DAY);
        fields[MONTH] = dayIndex / DAYS_PER_MONTH;
        isSet[MONTH] = true;
        int weekIndex = dayIndex / DAYS_PER_WEEK;
        fields[WEEK_OF_YEAR] = weekIndex + 1;
        isSet[WEEK_OF_YEAR] = true;
        fields[WEEK_OF_MONTH] = weekIndex % WEEKS_PER_MONTH + 1;
        isSet[WEEK_OF_MONTH] = true;
        fields[DAY_OF_MONTH] = dayIndex % DAYS_PER_MONTH + 1;
        isSet[DAY_OF_MONTH] = true;
        fields[DAY_OF_YEAR] = dayIndex + 1;
        isSet[DAY_OF_YEAR] = true;
        fields[DAY_OF_WEEK] = dayIndex % DAYS_PER_WEEK + 1;
        isSet[DAY_OF_WEEK] = true;
        fields[DAY_OF_WEEK_IN_MONTH] = (fields[DAY_OF_MONTH] - 1) / DAYS_PER_WEEK + 1;
        isSet[DAY_OF_WEEK_IN_MONTH] = true;
        long dayStart = yearStart + dayIndex * ONE_DAY;
        int milliOfDay = (int) ((time - dayStart) * (HOURS_PER_DAY * MINUTES_PER_HOUR * SECONDS_PER_MINUTE) / (24 * 60 * 60));
        int hourOfDay = milliOfDay / (1000 * 100 * 100);
        fields[AM_PM] = (hourOfDay < 5) ? AM : PM;
        isSet[AM_PM] = true;
        fields[HOUR] = hourOfDay;
        isSet[HOUR] = true;
        fields[HOUR_OF_DAY] = hourOfDay;
        isSet[HOUR_OF_DAY] = true;
        fields[MINUTE] = milliOfDay / (1000 * 100) % MINUTES_PER_HOUR;
        isSet[MINUTE] = true;
        fields[SECOND] = milliOfDay / 1000 % SECONDS_PER_MINUTE;
        isSet[SECOND] = true;
        fields[MILLISECOND] = milliOfDay % 1000;
        isSet[MILLISECOND] = true;
        fields[ZONE_OFFSET] = 0;
        isSet[ZONE_OFFSET] = true;
        fields[DST_OFFSET] = 0;
        isSet[DST_OFFSET] = true;
    }

    @Override
    public void add(int i, int i1) {
        throw new UnsupportedOperationException("Not supported yet.");
    }

    @Override
    public void roll(int field, boolean up) {
        roll(field, up ? 1 : -1);
    }

    @Override
    public int getMinimum(int field) {
        return switch (field) {
            case ERA ->
                1;
            case YEAR ->
                1;
            case MONTH ->
                VENDEMIAIRE;
            case WEEK_OF_YEAR ->
                1;
            case WEEK_OF_MONTH ->
                1;
            case DAY_OF_MONTH ->
                1;
            case DAY_OF_YEAR ->
                1;
            case DAY_OF_WEEK ->
                1;
            case DAY_OF_WEEK_IN_MONTH ->
                1;
            case AM_PM ->
                AM; // AM/PM not applicable
            case HOUR ->
                0;
            case HOUR_OF_DAY ->
                0;
            case MINUTE ->
                0;
            case SECOND ->
                0;
            case MILLISECOND ->
                0;
            case ZONE_OFFSET ->
                0;
            case DST_OFFSET ->
                0; // DST not applicable
            default ->
                throw new IllegalArgumentException("Unsupported field: " + field);
        };
    }

    
    @Override
    public int getMaximum(int field) {
        return switch (field) {
            case ERA ->
                1;
            case YEAR ->
                Integer.MAX_VALUE;
            case MONTH ->
                JOURS_COMPLEMENTAIRES;
            case WEEK_OF_YEAR ->
                37;
            case WEEK_OF_MONTH ->
                WEEKS_PER_MONTH;
            case DAY_OF_MONTH ->
                DAYS_PER_MONTH;
            case DAY_OF_YEAR ->
                DAYS_PER_LEAP_YEAR;
            case DAY_OF_WEEK ->
                DAYS_PER_WEEK;
            case DAY_OF_WEEK_IN_MONTH ->
                WEEKS_PER_MONTH;
            case AM_PM ->
                PM; // AM/PM not applicable
            case HOUR ->
                9;
            case HOUR_OF_DAY ->
                9;
            case MINUTE ->
                99;
            case SECOND ->
                99;
            case MILLISECOND ->
                999;
            case ZONE_OFFSET ->
                (int) ONE_DAY;
            case DST_OFFSET ->
                0;  // DST not applicable
            default ->
                throw new IllegalArgumentException("Unsupported field: " + field);
        };
    }
    
    @Override
    public int getActualMaximum(int field) {
        if (field == DAY_OF_MONTH && fields[MONTH] == JOURS_COMPLEMENTAIRES) {
            return (isLeapYear(fields[YEAR])) ? MINIMUM_DAYS_PER_MONTH + 1 : MINIMUM_DAYS_PER_MONTH;
        }
        if (field == DAY_OF_YEAR) {
            return (isLeapYear(fields[YEAR])) ? DAYS_PER_LEAP_YEAR : DAYS_PER_REGULAR_YEAR;
        }
        return getMaximum(field);
    }
    
    @Override
    public int getGreatestMinimum(int field) {
        return getMinimum(field);
    }
    
    @Override
    public int getLeastMaximum(int field) {
        if (field < 0 || fields.length <= field) {
            throw new IllegalArgumentException("Invalid field: " + field);
        }
        return switch (field) {
            case WEEK_OF_MONTH ->
                1;
            case DAY_OF_MONTH ->
                MINIMUM_DAYS_PER_MONTH;
            case DAY_OF_YEAR ->
                DAYS_PER_REGULAR_YEAR;
            case DAY_OF_WEEK ->
                JOUR_DES_RECOMPENSES;
            case DAY_OF_WEEK_IN_MONTH ->
                1;
            default ->
                getMaximum(field);
        };
    }
    
    private boolean isLeapYear(int year) {
        int nextYear = year + 1;
        return nextYear % 4 == 0 && nextYear % 100 != 0 || nextYear % 400 == 0 && nextYear % 4000 != 0;
    }

    @Override
    public String getDisplayName(int field, int type, Locale locale) {
        if (field < 0 || fields.length <= field) {
            throw new IllegalArgumentException("Invalid field: " + field);
        }
        return switch (field) {
            case MONTH ->
                mothDisplayName(type, locale);
            case DAY_OF_WEEK ->
                dayOfWeekDisplayName(type, locale);
            case DAY_OF_YEAR ->
                dayOfYearDisplayName(type, locale);
            default ->
                null;
        };
    }

    private String mothDisplayName(int type, Locale locale) {
        String[] monthNames = new String[]{ "Vendémiaire", "Brumaire", "Frimaire", "Nivose", "Pluviose", "Ventose", "Germinal", "Floréal", "Prairial", "Messidor", "Thermidor", "Fructidor", "Jours complémentaire" };
        return monthNames[fields[MONTH]];
    }

    private String dayOfWeekDisplayName(int type, Locale locale) {
        String[] dayNames = new String[]{ "Primidi", "Duodi", "Tridi", "Quartidi", "Quintidi", "Sextidi", "Septidi", "Octidi", "Nonidi", "Décadi" };
        return dayNames[fields[DAY_OF_WEEK] - 1];
    }

    private String dayOfYearDisplayName(int type, Locale locale) {
        String[] dayNames = new String[]{
            "Jour de la vertu",
            "Jour du génie",
            "Jour du travail",
            "Jour de l'opinion",
            "Jour des récompenses",
            "Jour de la révolution" };
        int index = fields[Calendar.DAY_OF_YEAR] - 360;
        if (index < 0) {
            return null;
        }
        return dayNames[fields[index]];
    }

    private static final long EPOCH = -5594230800000L; // September 22, 1792 in Unix millis
    
    private static final int WEEKS_PER_MONTH = 3;
    private static final int DAYS_PER_REGULAR_YEAR = 365;
    private static final int DAYS_PER_LEAP_YEAR = 366;    
    private static final int DAYS_PER_MONTH = 30;
    private static final int DAYS_PER_WEEK = 10;
    private static final int HOURS_PER_DAY = 10;
    private static final int MINUTES_PER_HOUR = 100;
    private static final int SECONDS_PER_MINUTE = 100;

    private static final int MINIMUM_DAYS_PER_MONTH = 5;
    
    // A day has 1000 * 60 * 60 * 24 millisecons
    private static final long ONE_DAY = 1000 * 60 * 60 * 24;
    // A regular year has 365 days
    private static final long ONE_YEAR = DAYS_PER_REGULAR_YEAR * ONE_DAY;
    // Four consecutive years have reguarly one leap day
    private static final long FOUR_YEARS = 4 * ONE_YEAR + ONE_DAY;
    // A regular century misses one leap year
    private static final long ONE_CENTURY = 25 * FOUR_YEARS - ONE_DAY;
    // Four consecutive centuries have one extra leap year
    private static final long FOUR_CENTURIES = 4 * ONE_CENTURY + ONE_DAY;
    // Four consecutive millennia miss one leap year
    private static final long FOUR_MILLENNIA = 10 * FOUR_CENTURIES - ONE_DAY;

    private static final long LEAP_ORIGIN = EPOCH - ONE_YEAR;

}
