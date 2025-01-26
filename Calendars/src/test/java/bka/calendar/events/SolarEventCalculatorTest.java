/*
** © Bart Kampers
*/

package bka.calendar.events;

import java.time.*;
import java.util.*;
import org.junit.*;
import static org.junit.jupiter.api.Assertions.assertEquals;

public class SolarEventCalculatorTest {

    @Before
    public void init() {
        calculator = new SolarEventCalculator(new Location(51.48069, 5.64986), TimeZone.getTimeZone("Europe/Amsterdam"));
    }

    @Test
    public void testCalendar() {
        LocalDate date = LocalDate.of(2022, Month.OCTOBER, 3);
        assertEquals(Optional.of(LocalDateTime.of(date, LocalTime.of(5, 49, 5))), calculator.sunrise(Zenith.ASTRONOMICAL, date));
        assertEquals(Optional.of(LocalDateTime.of(date, LocalTime.of(6, 28, 41))), calculator.sunrise(Zenith.NAUTICAL, date));
        assertEquals(Optional.of(LocalDateTime.of(date, LocalTime.of(7, 7, 24))), calculator.sunrise(Zenith.CIVIL, date));
        assertEquals(Optional.of(LocalDateTime.of(date, LocalTime.of(7, 40, 38))), calculator.sunrise(Zenith.OFFICIAL, date));
        assertEquals(Optional.of(LocalDateTime.of(date, LocalTime.of(19, 11, 32))), calculator.sunset(Zenith.OFFICIAL, date));
        assertEquals(Optional.of(LocalDateTime.of(date, LocalTime.of(19, 44, 46))), calculator.sunset(Zenith.CIVIL, date));
        assertEquals(Optional.of(LocalDateTime.of(date, LocalTime.of(20, 23, 28))), calculator.sunset(Zenith.NAUTICAL, date));
        assertEquals(Optional.of(LocalDateTime.of(date, LocalTime.of(21, 3, 0))), calculator.sunset(Zenith.ASTRONOMICAL, date));
    }

    private SolarEventCalculator calculator;

}