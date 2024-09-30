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

    public TreeMap<Period, Arc> getArcs() {
        return new TreeMap<>(arcs);
    }

    public void calculateArcs(LocalDate date) {
        if (!date.equals(this.date)) {
            double start = decimalHour(solarEventCalculator.sunset(Period.NIGHTTIME.getZenith(), date));
            for (Period period : Period.values()) {
                double end = calculateEndTime(date, period);
                Logger.getLogger(SolarDecorator.class.getName()).log(Level.FINE, "{0}: {1} .. {2}", new Object[]{ period.name(), start, end });
                Arc arc = arcs.computeIfAbsent(period, key -> new Arc());
                arc.setStart(start);
                arc.setEnd(end);
                start = end;
            }
            this.date = date;
        }
    }

    private double calculateEndTime(LocalDate date, Period period) {
        return decimalHour(period.isBeforeSunrise()
            ? solarEventCalculator.sunrise(period.getZenith(), date)
            : solarEventCalculator.sunset(period.getZenith(), date));
    }

    private static double decimalHour(LocalDateTime dateTime) {
        return dateTime.getHour() + dateTime.getMinute() / 60d;
    }

    public class Arc {

        private void setStart(double start) {
            this.start = start;
        }

        private void setEnd(double end) {
            this.end = end;
        }

        public double getStart() {
            return start;
        }

        public double getEnd() {
            return end;
        }

        private double start;
        private double end;
    }

    private final SolarEventCalculator solarEventCalculator;

    private final Map<Period, Arc> arcs = new HashMap<>();

    private LocalDate date;

}
