/*
** © Bart Kampers
*/
package bka.calendar;

import java.util.*;
import org.junit.*;
import static org.junit.Assert.*;


public class EarthianCalendarTest {

    public EarthianCalendarTest() {
        GregorianCalendar gregorian = new GregorianCalendar();
        gregorian.setTimeZone(UTC);
        gregorian.setTimeInMillis(0);
        gregorian.set(Calendar.YEAR, 2007);
        gregorian.set(Calendar.MONTH, Calendar.MARCH);
        gregorian.set(Calendar.DAY_OF_MONTH, 21);
        epoch = gregorian.getTime();
    }

    @Before
    public void setUp() {
        earthian = new EarthianCalendar();
        earthian.setTimeZone(UTC);
    }

    @Test
    public void epochTest() {
        earthian.setTime(epoch);
        assertEquals(0, earthian.get(Calendar.YEAR));
        assertEquals(EarthianCalendar.ARIES, earthian.get(Calendar.MONTH));
        assertEquals(1, earthian.get(Calendar.DAY_OF_MONTH));
        assertEquals(EarthianCalendar.MERCURIUS, earthian.get(Calendar.DAY_OF_WEEK));
    }

    @Test
    public void afterEpochTest() {
        GregorianCalendar gregorian = new GregorianCalendar();
        gregorian.setTimeZone(UTC);
        gregorian.setTime(epoch);
        for (int year = 0; year < 200; ++year) {
            int increment = ((year % 33) % 4 == 2) ? 366 : 365;
            gregorian.add(Calendar.DATE, increment);
            earthian.setTime(gregorian.getTime());
            assertEquals(year + 1, earthian.get(Calendar.YEAR));
        }
    }


    @Test
    public void beforeEpochTest() {
        GregorianCalendar gregorian = new GregorianCalendar();
        gregorian.setTimeZone(UTC);
        gregorian.setTime(epoch);
        for (int i = 0; i > -200; --i) {
            int increment = (((i - 1) % 33) % 4 == -3) ? -366 : -365;
            gregorian.add(Calendar.DATE, increment);
            earthian.setTime(gregorian.getTime());
            assertEquals(i - 1, earthian.get(Calendar.YEAR));
        }
    }


    @Test
    public void longTermTest() {
        Calendar gregorian = GregorianCalendar.getInstance();
        gregorian.setTimeZone(UTC);
        gregorian.clear();
        gregorian.set(Calendar.YEAR, 1900);
        gregorian.set(Calendar.MONTH, Calendar.JANUARY);
        gregorian.set(Calendar.DATE, 1);
        long millis = gregorian.getTimeInMillis();
        int expectedYear = -108;
        int expectedMonth = EarthianCalendar.CAPRICORNUS;
        int expectedDate = 13;
        int expectedDayOfYear = 287;
        int expectedDayOfWeek = EarthianCalendar.LUNA;
        int expectedWeekOfMonth = 3;
        int expectedDayOfWeekInMonth = 2;
        int expectedWeekOfYear = 42;
        int count = 1;
        while (expectedYear < 108) {
            earthian.setTimeInMillis(millis);
            String message = "! [" + count + "] " + earthian.get(Calendar.YEAR) + "/" + (earthian.get(Calendar.MONTH) + 1) + "/" + earthian.get(Calendar.DATE);
            assertEquals("Year" + message, expectedYear, earthian.get(Calendar.YEAR));
            assertEquals("Month" + message, expectedMonth, earthian.get(Calendar.MONTH));
            assertEquals("Date" + message, expectedDate, earthian.get(Calendar.DATE));
            assertEquals("Day of Year" + message, expectedDayOfYear, earthian.get(Calendar.DAY_OF_YEAR));
            assertEquals("Day of Week" + message, expectedDayOfWeek, earthian.get(Calendar.DAY_OF_WEEK));
            assertEquals("Week of Month" + message, expectedWeekOfMonth, earthian.get(Calendar.WEEK_OF_MONTH));
            assertEquals("Day of Week in Month" + message, expectedDayOfWeekInMonth, earthian.get(Calendar.DAY_OF_WEEK_IN_MONTH));
            assertEquals("Week of Year" + message, expectedWeekOfYear, earthian.get(Calendar.WEEK_OF_YEAR));
            millis += 1000 * 60 * 60 * 24;
            expectedDayOfWeek = (expectedDayOfWeek % 7) + 1;
            if (expectedDayOfYear == dayCount(expectedYear)) {
                expectedYear++;
                expectedMonth = EarthianCalendar.ARIES;
                expectedDate = 1;
                expectedDayOfYear = 1;
                expectedWeekOfMonth = (1 < expectedDayOfWeek && expectedDayOfWeek < 5) ? 1 : 0;
                expectedDayOfWeekInMonth = 1;
            }
            else {
                if (expectedMonth % 2 == 0 && expectedDate == 30 || expectedDate == 31) {
                    expectedMonth++;
                    expectedDate = 1;
                    expectedWeekOfMonth = (1 < expectedDayOfWeek && expectedDayOfWeek < 5) ? 1 : 0;
                    expectedDayOfWeekInMonth = 1;
                }
                else {
                    if (expectedDate % 7 == 0) {
                        expectedDayOfWeekInMonth++;
                    }
                    expectedDate++;
                }
                expectedDayOfYear++;
            }
            if (expectedDayOfWeek == 1) {
                if (expectedDayOfYear <= 4 || dayCount(expectedYear) - expectedDayOfYear <= 2) {
                    expectedWeekOfYear = 1;
                }
                else {
                    expectedWeekOfYear++;
                }
                expectedWeekOfMonth++;
            }
            count++;
        }
    }


    @Test
    public void dateTest() {
        GregorianCalendar gregorian = new GregorianCalendar();
        gregorian.setTimeZone(UTC);
        gregorian.setTime(epoch);
        gregorian.add(Calendar.YEAR, -2);
        int date = 1;
        int month = EarthianCalendar.ARIES;
        int year = -2;
        int dayOfYear = 1;
        for (int i = 0; i < 4 * (365 * 33 + 8); ++i) {
            gregorian.add(Calendar.DATE, 1);
            boolean leap = (year % 33) % 4 == 2;
            boolean monthTurn = ((month % 2 == 0 || (month == 11 && !leap)) && date == 30) || date == 31;
            earthian.setTimeInMillis(gregorian.getTimeInMillis());
            date = (monthTurn) ? 1 : date + 1;
            dayOfYear++;
            if (monthTurn) {
                month = (month + 1) % 12;
                if (month == 0) {
                    year++;
                    dayOfYear = 1;
                }
            }
            assertEquals(date, earthian.get(Calendar.DATE));
            assertEquals(month, earthian.get(Calendar.MONTH));
            assertEquals(year, earthian.get(Calendar.YEAR));
            assertEquals(dayOfYear, earthian.get(Calendar.DAY_OF_YEAR));
            assertEquals(gregorian.get(Calendar.DAY_OF_WEEK), earthian.get(Calendar.DAY_OF_WEEK));
        }
    }


    @Test
    public void timeTest() {
        GregorianCalendar gregorian = new GregorianCalendar();
        gregorian.setTimeZone(TimeZone.getTimeZone("UTC"));
        for (long millis = -10000000000L; millis < 10000000000L; millis += 123456789L) {
            gregorian.setTimeInMillis(millis);
            earthian.setTimeInMillis(millis);
            for (int field = Calendar.AM_PM; field <= Calendar.MILLISECOND; ++field) {
                assertEquals(gregorian.get(field), earthian.get(field));
            }
        }
    }


    @Test
    public void timeZoneTest() {
        GregorianCalendar gregorian = new GregorianCalendar();
        gregorian.setTime(epoch);
        earthian.setTime(epoch);
        for (String zoneId : TimeZone.getAvailableIDs()) {
            TimeZone zone = TimeZone.getTimeZone(zoneId);
            earthian.setTimeZone(zone);
            gregorian.setTimeZone(zone);
            for (int field = Calendar.AM_PM; field <= Calendar.MILLISECOND; ++field) {
                assertEquals(gregorian.get(field), earthian.get(field));
            }
        }
    }


    @Test
    public void fieldSetTest() {
        earthian.set(Calendar.YEAR, 0);
        earthian.set(Calendar.MONTH, EarthianCalendar.ARIES);
        earthian.set(Calendar.DATE, 1);
        earthian.set(Calendar.HOUR_OF_DAY, 0);
        earthian.set(Calendar.MINUTE, 0);
        earthian.set(Calendar.SECOND, 0);
        earthian.set(Calendar.MILLISECOND, 0);
        assertEquals(epoch.getTime(), earthian.getTimeInMillis());
        earthian.set(Calendar.YEAR, -38);
        earthian.set(Calendar.MONTH, EarthianCalendar.CAPRICORNUS);
        earthian.set(Calendar.DATE, 13);
        assertEquals(0L, earthian.getTimeInMillis());
        earthian.setTimeZone(TimeZone.getTimeZone("CET"));
        earthian.set(Calendar.MILLISECOND, 0);
        assertEquals(-60 * 60 * 1000L, earthian.getTimeInMillis());
    }


    @Test
    public void fieldAddTest() {
        earthian.setTime(epoch);
        int milli = earthian.get(Calendar.MILLISECOND);
        earthian.add(Calendar.MILLISECOND, 1);
        assertEquals((milli + 1) % 1000, earthian.get(Calendar.MILLISECOND));
        int second = earthian.get(Calendar.SECOND);
        earthian.add(Calendar.MILLISECOND, 1000);
        assertEquals((second + 1) % 60, earthian.get(Calendar.SECOND));
        int minute = earthian.get(Calendar.MINUTE);
        earthian.add(Calendar.SECOND, 60);
        assertEquals((minute + 1) % 60, earthian.get(Calendar.MINUTE));
        int hour = earthian.get(Calendar.HOUR_OF_DAY);
        earthian.add(Calendar.MINUTE, 60);
        assertEquals((hour + 1) % 24, earthian.get(Calendar.HOUR));
        int month = earthian.get(Calendar.MONTH);
        earthian.add(Calendar.MONTH, 1);
        assertEquals(month + 1, earthian.get(Calendar.MONTH));
        int year = earthian.get(Calendar.YEAR);
        earthian.add(Calendar.YEAR, 1);
        assertEquals(year + 1, earthian.get(Calendar.YEAR));
    }


    @Ignore
    @Test
    public void monthAddTest() {
        earthian.setTime(epoch);
        earthian.add(Calendar.MONTH, 5);
        assertEquals(5, earthian.get(Calendar.MONTH));
        assertEquals(0, earthian.get(Calendar.YEAR));
        earthian.add(Calendar.MONTH, 12);
        assertEquals(5, earthian.get(Calendar.MONTH));
        assertEquals(1, earthian.get(Calendar.YEAR));
        earthian.setTime(epoch);
        earthian.add(Calendar.MONTH, -1);
        assertEquals(11, earthian.get(Calendar.MONTH));
        assertEquals(-1, earthian.get(Calendar.YEAR));
        earthian.add(Calendar.MONTH, -12);
        assertEquals(11, earthian.get(Calendar.MONTH));
        assertEquals(-2, earthian.get(Calendar.YEAR));
        earthian.set(Calendar.MONTH, 1);
        earthian.set(Calendar.DAY_OF_MONTH, 31);
        earthian.add(Calendar.MONTH, 1);
        assertEquals(2, earthian.get(Calendar.MONTH));
        assertEquals(30, earthian.get(Calendar.DAY_OF_MONTH));
    }

    @Ignore
    @Test
    public void fieldRollTest() {
        earthian.setTime(epoch);
        int month = earthian.get(Calendar.MONTH);
        for (int i = 1; i <= 12; ++i) {
            earthian.roll(Calendar.MONTH, 1);
            assertEquals((month + i) % 12, earthian.get(Calendar.MONTH));
        }
    }


    @Ignore
    @Test
    public void millisTest() {
        earthian.set(Calendar.YEAR, -2);
        earthian.set(Calendar.MONTH, 1);
        earthian.set(Calendar.DAY_OF_MONTH, 31);
        long millis = earthian.getTimeInMillis();
        earthian.setTimeInMillis(millis);
        assertEquals(-2, earthian.get(Calendar.YEAR));
        assertEquals(1, earthian.get(Calendar.MONTH));
        assertEquals(31, earthian.get(Calendar.DAY_OF_MONTH));
    }

    @Ignore
    @Test
    public void testSetWeekDate() {
        earthian.setWeekDate(0, 1, 1);
        System.out.printf("%04d-%02d-%02d (w) %04d.%02d.%d\n",
            earthian.get(Calendar.YEAR),
            earthian.get(Calendar.MONTH) + 1,
            earthian.get(Calendar.DATE),
            earthian.getWeekYear(),
            earthian.get(Calendar.WEEK_OF_YEAR),
            earthian.get(Calendar.DAY_OF_WEEK));
    }


    private int dayCount(int year) {
        return (isLeapYear(year)) ? 366 : 365;
    }


    private boolean isLeapYear(int year) {
        int generationIndex = (int) (year % 33);
        if (generationIndex < 0) {
            generationIndex += 33;
        }
        return generationIndex % 4 == 2;
    }


    private EarthianCalendar earthian;

    private final Date epoch;
    private static final TimeZone UTC = TimeZone.getTimeZone("UTC");


}