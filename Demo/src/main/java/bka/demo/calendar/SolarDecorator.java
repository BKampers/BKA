/*
** © Bart Kampers
*/
package bka.demo.calendar;

import bka.calendar.events.*;
import java.time.*;
import java.util.*;
import java.util.logging.*;
import java.util.stream.*;


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

        public Period opposite() {
            return switch (this) {
                case ASTRONOMICAL_SUNRISE_TWILIGHT ->
                    ASTRONOMICAL_SUNSET_TWILIGHT;
                case NAUTICAL_SUNRISE_TWILIGHT ->
                    NAUTICAL_SUNSET_TWILIGHT;
                case CIVIL_SUNRISE_TWILIGHT ->
                    CIVIL_SUNSET_TWILIGHT;
                case DAYTIME ->
                    NIGHTTIME;
                case CIVIL_SUNSET_TWILIGHT ->
                    CIVIL_SUNRISE_TWILIGHT;
                case NAUTICAL_SUNSET_TWILIGHT ->
                    NAUTICAL_SUNRISE_TWILIGHT;
                case ASTRONOMICAL_SUNSET_TWILIGHT ->
                    ASTRONOMICAL_SUNRISE_TWILIGHT;
                case NIGHTTIME ->
                    DAYTIME;
                default ->
                    throw new IllegalStateException(name());
            };
        }

        private final double zenith;
    }

    public SolarDecorator(SolarEventCalculator solarEventCalculator) {
        this.solarEventCalculator = Objects.requireNonNull(solarEventCalculator);
    }

    public SortedMap<Period, Arc> getArcs() {
        synchronized (arcs) {
            return new TreeMap<>(arcs.entrySet().stream()
                .filter(this::hasEnd)
                .collect(Collectors.toMap(Map.Entry::getKey, this::createArc)));
        }
    }

    private boolean hasEnd(Map.Entry<Period, Arc> entry) {
        return entry.getValue().getEnd().isPresent();
    }

    private Arc createArc(Map.Entry<Period, Arc> entry) {
        Arc arc = new Arc();
        arc.setStart(getStart(entry));
        arc.setEnd(entry.getValue().getEnd());
        return arc;
    }

    private Optional<Double> getStart(Map.Entry<Period, Arc> entry) {
        if (entry.getValue().getStart().isPresent()) {
            return entry.getValue().getStart();
        }
        Arc opposite = arcs.get(entry.getKey().opposite());
        if (opposite.getStart().isEmpty()) {
            throw new IllegalStateException("Start time for arc not available");
        }
        return opposite.getStart();
    }

    public void calculateArcs(LocalDate date) {
        if (!date.equals(this.date)) {
            synchronized (arcs) {
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
    }

    public Optional<Double> calculateSolarNoonHour(LocalDate date) {
        Optional<LocalDateTime> sunrise = solarEventCalculator.sunrise(Zenith.CIVIL, date);
        if (sunrise.isEmpty()) {
            return Optional.empty();
        }
        Optional<LocalDateTime> sunset = solarEventCalculator.sunset(Zenith.CIVIL, date);
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
