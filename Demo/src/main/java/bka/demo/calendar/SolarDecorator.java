/*
** © Bart Kampers
*/
package bka.demo.calendar;

import bka.calendar.events.*;
import java.time.*;
import java.util.*;
import java.util.logging.*;


public class SolarDecorator {

    public enum Period {
        NIGHTTIME(Zenith.ASTRONOMICAL),
        ASTRONOMICAL_SUNRISE_TWILIGHT(Zenith.NAUTICAL),
        NAUTICAL_SUNRISE_TWILIGHT(Zenith.CIVIL),
        CIVIL_SUNRISE_TWILIGHT(Zenith.OFFICIAL),
        DAYTIME(Zenith.OFFICIAL),
        CIVIL_SUNSET_TWILIGHT(Zenith.CIVIL),
        NAUTICAL_SUNSET_TWILIGHT(Zenith.NAUTICAL),
        ASTRONOMICAL_SUNSET_TWILIGHT(Zenith.ASTRONOMICAL);

        private Period(double zenith) {
            this.zenith = zenith;
        }

        public double getZenith() {
            return zenith;
        }

        public boolean isBeforeSunrise() {
            return ordinal() < DAYTIME.ordinal();
        }

        private final double zenith;
    }

    public SolarDecorator(SolarEventCalculator solarEventCalculator) {
        this.solarEventCalculator = Objects.requireNonNull(solarEventCalculator);
    }

    public SortedMap<Period, Arc> getArcs() {
        return Collections.unmodifiableSortedMap(arcs);
    }

    public void calculateArcs(LocalDate date) {
        if (!date.equals(this.date)) {
            Optional<Double> start = decimalHour(solarEventCalculator.sunset(Period.NIGHTTIME.getZenith(), date));
            for (Period period : Period.values()) {
                Optional<Double> end = calculateEndTime(date, period);
                Logger.getLogger(SolarDecorator.class.getName()).log(Level.FINE, "{0}: {1} .. {2}", new Object[]{ period, start, end });
                Arc arc = arcs.computeIfAbsent(period, p -> new Arc());
                arc.setStart(start);
                arc.setEnd(end);
                start = end;
            }
            this.date = date;
        }
    }

    public Optional<Double> calculateMidDayHour(LocalDate date) {
        Optional<LocalDateTime> sunrise = solarEventCalculator.sunrise(Zenith.ASTRONOMICAL, date);
        if (sunrise.isEmpty()) {
            return Optional.empty();
        }
        Optional<LocalDateTime> sunset = solarEventCalculator.sunset(Zenith.ASTRONOMICAL, date);
        if (sunset.isEmpty()) {
            return Optional.empty();
        }
        return Optional.of(decimalHour(sunrise.get().plusNanos(Duration.between(sunrise.get(), sunset.get()).toNanos() / 2)));
    }

    private Optional<Double> calculateEndTime(LocalDate date, Period period) {
        return decimalHour(period.isBeforeSunrise()
            ? solarEventCalculator.sunrise(period.getZenith(), date)
            : solarEventCalculator.sunset(period.getZenith(), date));
    }

    private static Optional<Double> decimalHour(Optional<LocalDateTime> dateTime) {
        if (dateTime.isEmpty()) {
            return Optional.empty();
        }
        return Optional.of(dateTime.get().getHour() + dateTime.get().getMinute() / 60d);
    }

    private static double decimalHour(LocalDateTime dateTime) {
        return dateTime.getHour() + dateTime.getMinute() / 60d + dateTime.getSecond() / (60d * 60d);
    }

    public class Arc {

        private void setStart(Optional<Double> start) {
            this.start = start;
        }

        private void setEnd(Optional<Double> end) {
            this.end = end;
        }

        public Optional<Double> getStart() {
            return start;
        }

        public Optional<Double> getEnd() {
            return end;
        }

        private Optional<Double> start;
        private Optional<Double> end;
    }

    private final SolarEventCalculator solarEventCalculator;

    private final TreeMap<Period, Arc> arcs = new TreeMap<>();

    private LocalDate date;

}
