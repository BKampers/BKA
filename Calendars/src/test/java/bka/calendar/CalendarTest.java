/*
 * © Bart Kampers
 */
package bka.calendar;

import java.util.*;
import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertFalse;
import static org.junit.Assert.assertTrue;
import org.junit.*;

/**
 */
public class CalendarTest {

    @Test
    public void testClear() {
        Calendar calendar = Calendar.getInstance(TimeZone.getTimeZone("UTC"));
        calendar.clear();
        assertEquals(0, calendar.getTimeInMillis());
        for (int field = 0; field < Calendar.FIELD_COUNT; ++field) {
            assertFalse(calendar.isSet(field));
        }
        assertFields(calendar);
    }

    @Test
    public void testSetMillisecond() {
        Calendar calendar = Calendar.getInstance(TimeZone.getTimeZone("UTC"));
        calendar.clear();
        calendar.set(Calendar.MILLISECOND, 1);
        assertEquals(1, calendar.getTimeInMillis());
        assertFields(calendar, Calendar.MILLISECOND);
        calendar.set(Calendar.MILLISECOND, 2);
        assertEquals(2, calendar.getTimeInMillis());
        assertAllFields(calendar);
        calendar.set(Calendar.SECOND, 1);
        assertEquals(1002, calendar.getTimeInMillis());
        assertAllFields(calendar);
    }

    @Test
    public void testSetSecond() {
        Calendar calendar = Calendar.getInstance(TimeZone.getTimeZone("UTC"));
        calendar.clear();
        calendar.set(Calendar.SECOND, 1);
        assertEquals(1000, calendar.getTimeInMillis());
        assertFields(calendar, Calendar.SECOND);
        calendar.set(Calendar.MINUTE, 1);
        assertEquals(61000, calendar.getTimeInMillis());
        assertAllFields(calendar);
    }

    @Test
    public void testSetMinute() {
        Calendar calendar = Calendar.getInstance(TimeZone.getTimeZone("UTC"));
        calendar.clear();
        calendar.set(Calendar.MINUTE, 1);
        assertEquals(60000, calendar.getTimeInMillis());
        assertFields(calendar, Calendar.MINUTE);
        calendar.set(Calendar.HOUR, 1);
        assertEquals(3660000, calendar.getTimeInMillis());
        assertAllFields(calendar);
        calendar.set(Calendar.HOUR_OF_DAY, 10);
        assertEquals(36060000, calendar.getTimeInMillis());
        assertAllFields(calendar);
    }

    @Test
    public void testSetHour() {
        Calendar calendar = Calendar.getInstance(TimeZone.getTimeZone("UTC"));
        calendar.clear();
        calendar.set(Calendar.HOUR, 1);
        assertEquals(3600000, calendar.getTimeInMillis());
        assertFields(calendar, Calendar.HOUR);
        calendar.set(Calendar.HOUR_OF_DAY, 10);
        assertEquals(36000000, calendar.getTimeInMillis());
        assertAllFields(calendar);
    }

    @Test
    public void testSetHourOfDay() {
        Calendar calendar = Calendar.getInstance(TimeZone.getTimeZone("UTC"));
        calendar.clear();
        calendar.set(Calendar.HOUR_OF_DAY, 1);
        assertEquals(3600000, calendar.getTimeInMillis());
        assertFields(calendar, Calendar.HOUR_OF_DAY);
        calendar.set(Calendar.DAY_OF_MONTH, 1);
        assertEquals(3600000, calendar.getTimeInMillis());
        assertAllFields(calendar);
    }

    @Test
    public void testSetDayOfMonth() {
        Calendar calendar = Calendar.getInstance(TimeZone.getTimeZone("UTC"));
        calendar.clear();
        calendar.set(Calendar.DAY_OF_MONTH, 1);
        assertEquals(0, calendar.getTimeInMillis());
        assertFields(calendar, Calendar.DAY_OF_MONTH);
        calendar.set(Calendar.MONTH, Calendar.JANUARY);
        assertEquals(0, calendar.getTimeInMillis());
        assertAllFields(calendar);
    }

    @Test
    public void testSetMonth() {
        Calendar calendar = Calendar.getInstance(TimeZone.getTimeZone("UTC"));
        calendar.clear();
        calendar.set(Calendar.MONTH, Calendar.JANUARY);
        assertEquals(0, calendar.getTimeInMillis());
        assertFields(calendar, Calendar.MONTH);
        calendar.set(Calendar.MONTH, Calendar.FEBRUARY);
        assertEquals(3600000L * 24L * 31L, calendar.getTimeInMillis());
        assertAllFields(calendar);
    }

    @Test
    public void testSetYear() {
        Calendar calendar = Calendar.getInstance(TimeZone.getTimeZone("UTC"));
        calendar.clear();
        calendar.set(Calendar.YEAR, 1970);
        assertEquals(0, calendar.getTimeInMillis());
        assertFields(calendar, Calendar.YEAR);
        calendar.set(Calendar.YEAR, 1971);
        assertEquals(3600000L * 24L * 365L, calendar.getTimeInMillis());
        assertAllFields(calendar);
    }

    private static void assertAllFields(Calendar calendar) {
        assertFields(calendar, Calendar.ERA, Calendar.YEAR, Calendar.MONTH, Calendar.WEEK_OF_YEAR, Calendar.WEEK_OF_MONTH, Calendar.DAY_OF_MONTH, Calendar.DAY_OF_YEAR, Calendar.DAY_OF_WEEK, Calendar.DAY_OF_WEEK_IN_MONTH, Calendar.AM_PM, Calendar.HOUR, Calendar.HOUR_OF_DAY, Calendar.MINUTE, Calendar.SECOND, Calendar.MILLISECOND, Calendar.ZONE_OFFSET, Calendar.DST_OFFSET);
    }

    private static void assertFields(Calendar calendar, int... fields) {
        BitSet fieldSet = new BitSet();
        for (int i = 0; i < fields.length; ++i) {
            fieldSet.set(fields[i]);
        }
        StringBuilder builder = new StringBuilder();
        for (int field = 0; field < Calendar.FIELD_COUNT; ++field) {
            if (fieldSet.get(field) && !calendar.isSet(field)) {
                builder.append(fieldName(field)).append(" expected but not set\n");
            }
            else if (!fieldSet.get(field) && calendar.isSet(field)) {
                builder.append(fieldName(field)).append(" not expected but set\n");
            }
        }
        assertTrue(builder.toString(), builder.length() == 0);
    }


    private static String fieldName(int field) {
        switch (field) {
            case Calendar.ERA:
                return "ERA";
            case Calendar.YEAR:
                return "YEAR";
            case Calendar.MONTH:
                return "MONTH";
            case Calendar.WEEK_OF_YEAR:
                return "WEEK_OF_YEAR";
            case Calendar.WEEK_OF_MONTH:
                return "WEEK_OF_MONTH";
            case Calendar.DAY_OF_MONTH:
                return "DAY_OF_MONTH";
            case Calendar.DAY_OF_YEAR:
                return "DAY_OF_YEAR";
            case Calendar.DAY_OF_WEEK:
                return "DAY_OF_WEEK";
            case Calendar.DAY_OF_WEEK_IN_MONTH:
                return "DAY_OF_WEEK_IN_MONTH";
            case Calendar.AM_PM:
                return "AM_PM";
            case Calendar.HOUR:
                return "HOUR";
            case Calendar.HOUR_OF_DAY:
                return "HOUR_OF_DAY";
            case Calendar.MINUTE:
                return "MINUTE";
            case Calendar.SECOND:
                return "SECOND";
            case Calendar.MILLISECOND:
                return "MILLISECOND";
            case Calendar.ZONE_OFFSET:
                return "ZONE_OFFSET";
            case Calendar.DST_OFFSET:
                return "DST_OFFSET";
            default:
                throw new IllegalStateException(Integer.toString(field));
        }
    }

}
