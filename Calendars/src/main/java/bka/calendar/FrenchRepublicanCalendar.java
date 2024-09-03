package bka.calendar;

import java.util.Calendar;


public class FrenchRepublicanCalendar extends Calendar {

    
    public static final int VENDÉMIAIRE = 0;
    public static final int BRUMAIRE = 1;
    public static final int FRIMAIRE = 2;
    public static final int NIVOSE = 3;
    public static final int PLUVIOSE =  4;
    public static final int VENTOSE = 5;
    public static final int GERMINAL = 6;
    public static final int FLORÉAL = 7;
    public static final int PRAIRIAL = 8;
    public static final int MESSIDOR = 9;
    public static final int THERMIDOR = 10;
    public static final int FRUCTIDOR = 11;
    
    public static final int JOURS_COMPLÉMENTAIRES = 12;
    
    
    public static final int PRIMIDI = 1;
    public static final int DUODI = 2;
    public static final int TRIDI = 3;
    public static final int QUARTIDI = 4;
    public static final int QUINTIDI = 5;
    public static final int SEXTIDI = 6;
    public static final int SEPTIDI = 7;
    public static final int OCTIDI = 8;
    public static final int NONIDI = 9;
    public static final int DÉCADI  = 10;
    
    public static final int JOUR_DE_LA_VERTU = 0;
    public static final int JOUR_DU_GÉNIE = 1;
    public static final int JOUR_DU_TRAVAIL = 2;
    public static final int JOUR_DE_L_OPINION = 3;
    public static final int JOUR_DES_RÉCOMPENSES = 4;
    public static final int JOUR_DE_LA_RÉVOLUTION = 5;

    
    public static void main(String[] arguments) {
        clock();
    }
    
    
    public FrenchRepublicanCalendar() {
        setFirstDayOfWeek(PRIMIDI);
        setMinimalDaysInFirstWeek(DAYS_PER_WEEK);
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
//        if (EPOCH > time) {
//            //fields[YEAR]--;
//            yearStart -= ONE_YEAR;
//            if (isLeapYear(fields[YEAR])) {
//                yearStart -= ONE_DAY;
//            }
//        }
//        else {
//            //fields[YEAR]--;
//        }
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
        switch (field) {
            case ERA                  : return 1;
            case YEAR                 : return 1;
            case MONTH                : return VENDÉMIAIRE;
            case WEEK_OF_YEAR         : return 1;
            case WEEK_OF_MONTH        : return 1; 
            case DAY_OF_MONTH         : return 1;
            case DAY_OF_YEAR          : return 1;
            case DAY_OF_WEEK	      : return 1;
            case DAY_OF_WEEK_IN_MONTH : return 1;
            case AM_PM                : return AM;
            case HOUR                 : return 0;
            case HOUR_OF_DAY          : return 0;
            case MINUTE               : return 0;
            case SECOND               : return 0;
            case MILLISECOND          : return 0;
            case ZONE_OFFSET          : return 0;
            case DST_OFFSET           : return 0;  // DST not applicable 
        }
        return 0; // Should not occur
    }

    
    @Override
    public int getMaximum(int field) {
        switch (field) {
            case ERA                  : return 1;
            case YEAR                 : return Integer.MAX_VALUE;
            case MONTH                : return JOURS_COMPLÉMENTAIRES;
            case WEEK_OF_YEAR         : return 37;
            case WEEK_OF_MONTH        : return WEEKS_PER_MONTH; 
            case DAY_OF_MONTH         : return DAYS_PER_MONTH;
            case DAY_OF_YEAR          : return DAYS_PER_LEAP_YEAR;
            case DAY_OF_WEEK	      : return DAYS_PER_WEEK;
            case DAY_OF_WEEK_IN_MONTH : return WEEKS_PER_MONTH;
            case AM_PM                : return PM;
            case HOUR                 : return 9;
            case HOUR_OF_DAY          : return 9;
            case MINUTE               : return 99;
            case SECOND               : return 99;
            case MILLISECOND          : return 999;
            case ZONE_OFFSET          : return (int) ONE_DAY;
            case DST_OFFSET           : return 0;  // DST not applicable 
        }
        return 0; // Should not occur
    }
    
    
    @Override
    public int getActualMaximum(int field) {
        if (field == DAY_OF_MONTH && fields[MONTH] == JOURS_COMPLÉMENTAIRES) {
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
        switch (field) {
            case WEEK_OF_MONTH        : return 1; 
            case DAY_OF_MONTH         : return MINIMUM_DAYS_PER_MONTH;
            case DAY_OF_YEAR          : return DAYS_PER_REGULAR_YEAR;
            case DAY_OF_WEEK	      : return JOUR_DES_RÉCOMPENSES;
            case DAY_OF_WEEK_IN_MONTH : return 1;
            default                   : return getMaximum(field);
        }
    }
    
    
    private boolean isLeapYear(int year) {
        year++;
        return year % 4 == 0 && year % 100 != 0 || year % 400 == 0 && year % 4000 != 0;
    }
    
    
    private static void clock() {
        java.util.GregorianCalendar gregorian = new java.util.GregorianCalendar();
        int dstOffset = gregorian.get(DST_OFFSET);
        int previous = -1;
        FrenchRepublicanCalendar calendar = new FrenchRepublicanCalendar();
        for (;;) {
            long timestamp = System.currentTimeMillis() + dstOffset;
            calendar.setTimeInMillis(timestamp);
            int second = calendar.get(Calendar.SECOND);
            if (second != previous) {
                print(calendar);
                previous = second;
            }
        }
    }
    
    
    private static void timeTest() {
        long timestamp = EPOCH;
        while (timestamp <= EPOCH + ONE_DAY) {
            print(timestamp);
            timestamp += ONE_DAY / 24;
        }
    }
    
    
    private static final void weekTest() {
        FrenchRepublicanCalendar calendar = new FrenchRepublicanCalendar(); 
        long timestamp = EPOCH;
        while (timestamp < EPOCH + FOUR_YEARS) {
            calendar.setTimeInMillis(timestamp);
            System.out.printf(
                "%3d %2d %d %d %d\n",
                calendar.get(DAY_OF_YEAR),
                calendar.get(WEEK_OF_YEAR),
                calendar.get(WEEK_OF_MONTH),
                calendar.get(DAY_OF_WEEK),
                calendar.get(DAY_OF_WEEK_IN_MONTH));
            timestamp += ONE_DAY;
        }
    }
    
    
    private static void dateTest() {
        long timestamp = EPOCH;
        FrenchRepublicanCalendar calendar = new FrenchRepublicanCalendar();
        calendar.setTimeInMillis(timestamp);
        int year = calendar.get(YEAR);
        int month = calendar.get(MONTH);
        int date = calendar.get(DATE);
        while (timestamp < Long.MAX_VALUE - ONE_DAY) {
            timestamp += ONE_DAY;
            calendar.setTimeInMillis(timestamp);
            int nextYear = calendar.get(YEAR);
            int nextMonth = calendar.get(MONTH);
            int nextDate = calendar.get(DATE);
            if (
                    nextYear == year && nextMonth == month && nextDate == date + 1 ||
                    nextYear == year && nextMonth == month + 1 && nextDate == 1 && date == 30 ||
                    nextYear == year + 1 && nextMonth == 0 && nextDate == 1 && month == 12 && (date == 5 || date == 6)
               ) {
                if (nextMonth == 0 && nextDate == 1) {
                    boolean leap = calendar.isLeapYear(year);
                    if (leap && date != 6 || ! leap && date != 5) {
                        System.out.print("Leap error: ");
                        print(calendar);
                    }
                }
//                if (nextMonth == 12 && nextDate == 6) {
//                    int leap = nextYear + 1;
//                    if (leap % 4 != 0 || leap % 100 == 0 && leap % 400 != 0) {
//                        System.out.print("Leap error: ");
//                        print(calendar);
//                    }
//                }
            }
            else {
                System.out.println("Error: ");
                print(timestamp - ONE_DAY);
                print(calendar);
            }
            if (nextYear != year && nextYear % 100000 == 0) {
                System.out.print("OK: ");
                print(calendar);
            }
            year = nextYear;
            month = nextMonth;
            date = nextDate;
        }
    }
    
    
    private static void negativeDateTest() {
        long timestamp = EPOCH;
        FrenchRepublicanCalendar calendar = new FrenchRepublicanCalendar();
        calendar.setTimeInMillis(timestamp);
        int year = calendar.get(YEAR);
        int month = calendar.get(MONTH);
        int date = calendar.get(DATE);
        while (timestamp > Long.MIN_VALUE + ONE_DAY) {
            timestamp -= ONE_DAY;
            calendar.setTimeInMillis(timestamp);
            int nextYear = calendar.get(YEAR);
            int nextMonth = calendar.get(MONTH);
            int nextDate = calendar.get(DATE);
            if (
                    nextYear == year && nextMonth == month && nextDate == date - 1 ||
                    nextYear == year && nextMonth == month - 1 && nextDate == 30 && date == 1 ||
                    nextYear == year - 1 && nextMonth == 12 && month == 0 && (nextDate == 5  || nextDate ==6) && date == 1
               ) {
//                if (nextMonth == 0 && nextDate == 1) {
//                    boolean leap = calendar.isLeapYear(year);
//                    if (leap && date != 6 || ! leap && date != 5) {
//                        System.out.print("Leap error: ");
//                        print(calendar);
//                    }
//                }
//                if (nextMonth == 12 && nextDate == 6) {
//                    int leap = nextYear + 1;
//                    if (leap % 4 != 0 || leap % 100 == 0 && leap % 400 != 0) {
//                        System.out.print("Leap error: ");
//                        print(calendar);
//                    }
//                }
            }
            else {
                System.out.println("Error: ");
                print(timestamp + ONE_DAY);
                print(timestamp);
            }
//            if (nextYear != year && nextYear % 100000 == 0) {
                System.out.print("OK: ");
                print(calendar);
//            }
            year = nextYear;
            month = nextMonth;
            date = nextDate;
        }
    }
    
    
    private static void print(long timestamp) {
        FrenchRepublicanCalendar calendar = new FrenchRepublicanCalendar();
        calendar.setTimeInMillis(timestamp);
        print(calendar);
    }
    
    
    private static void print(Calendar calendar) {
        System.out.printf(
            "%10d/%02d/%02d %d:%02d:%02d,%03d\n", 
            calendar.get(YEAR), 
            calendar.get(MONTH) + 1, 
            calendar.get(DATE),
            calendar.get(HOUR),
            calendar.get(MINUTE),
            calendar.get(SECOND),
            calendar.get(MILLISECOND));
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
