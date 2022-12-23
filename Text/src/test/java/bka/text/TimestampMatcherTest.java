/*
 * © Bart Kampers
 */
package bka.text;

import java.text.*;
import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertNull;
import org.junit.*;

public class TimestampMatcherTest {



    @Before
    public void setUp() {
        matcher = new TimestampMatcher();
    }


    @Test
    public void testGetDateFullFormat() throws ParseException {
        SimpleDateFormat format = new SimpleDateFormat("yyyy-MM-dd'T'HH:mm:ss.SSS");
        assertEquals(format.parse("1970-01-01T00:00:00.000"), matcher.getDate("1970-01"));
        assertEquals(format.parse("1970-01-01T00:00:00.000"), matcher.getDate("1970-01-01"));
        assertEquals(format.parse("1970-01-01T12:00:00.000"), matcher.getDate("1970-01-01T12"));
        assertEquals(format.parse("1970-01-01T12:15:00.000"), matcher.getDate("1970-01-01T12:15"));
        assertEquals(format.parse("1970-01-01T12:00:30.000"), matcher.getDate("1970-01-01T12:00:30"));
        assertEquals(format.parse("1970-01-01T11:58:00.100"), matcher.getDate("1970-01-01T11:58:00.1"));
        assertEquals(format.parse("1970-01-01T11:58:00.250"), matcher.getDate("1970-01-01T11:58:00.25"));
        assertEquals(format.parse("1970-01-01T11:58:00.009"), matcher.getDate("1970-01-01T11:58:00.009"));
        assertEquals(format.parse("1970-01-01T11:58:00.000"), matcher.getDate("1970-01-01T11:58:00.0004"));
        assertEquals(format.parse("1970-01-01T11:58:00.001"), matcher.getDate("1970-01-01T11:58:00.0005"));
        assertEquals(format.parse("1970-01-01T11:58:01.000"), matcher.getDate("1970-01-01T11:58:00.9999"));
    }


    @Test
    public void testGetDateShortFormats() throws ParseException {
        assertEquals(new SimpleDateFormat("yyyy-MM").parse("1970-01"), matcher.getDate("1970-01"));
        assertEquals(new SimpleDateFormat("yyyy-MM-dd").parse("1970-01-01"), matcher.getDate("1970-01-01"));
        assertEquals(new SimpleDateFormat("yyyy-MM-dd'T'HH").parse("1970-01-01T12"), matcher.getDate("1970-01-01T12"));
        assertEquals(new SimpleDateFormat("yyyy-MM-dd'T'HH:mm").parse("1970-01-01T12:15"), matcher.getDate("1970-01-01T12:15"));
        assertEquals(new SimpleDateFormat("yyyy-MM-dd'T'HH:mm:ss").parse("1970-01-01T12:00:30"), matcher.getDate("1970-01-01T12:00:30"));
    }


    @Test
    public void testIvalidInput() {
        assertNull(matcher.getDate(""));
        assertNull(matcher.getDate("1970"));
        assertNull(matcher.getDate("1970-JAN"));
    }


    private TimestampMatcher matcher;

}
