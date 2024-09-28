/*
** © Bart Kampers
*/
package bka.demo.calendar;

import bka.calendar.events.*;
import java.awt.*;
import java.time.*;
import java.util.*;


public class SolarDecorator {

    public static final Color DAYTIME_COLOR = new Color(0xb1d0fd);
    public static final Color CIVIL_TWILIGHT_COLOR = new Color(0x99b3e4);
    public static final Color NAUTICAL_TWILIGHT_COLOR = new Color(0x7c96c9);
    public static final Color ASTRONOMICAL_TWILIGHT_COLOR = new Color(0x6c82ba);
    public static final Color NIGHTTIME_COLOR = new Color(0x4b5b97);

    public enum Event {
        ASTRONOMICAL_SUNRISE(Zenith.ASTRONOMICAL),
        NAUTICAL_SUNRISE(Zenith.NAUTICAL),
        CIVIL_SUNRISE(Zenith.CIVIL),
        OFFICIAL_SUNRISE(Zenith.OFFICIAL),
        OFFICIAL_SUNSET(Zenith.OFFICIAL),
        CIVIL_SUNSET(Zenith.CIVIL),
        NAUTICAL_SUNSET(Zenith.NAUTICAL),
        ASTRONOMICAL_SUNSET(Zenith.ASTRONOMICAL);

        private Event(double zenith) {
            this.zenith = zenith;
        }

        public boolean isSunrise() {
            return switch (this) {
                case ASTRONOMICAL_SUNRISE, NAUTICAL_SUNRISE, CIVIL_SUNRISE, OFFICIAL_SUNRISE ->
                    true;
                case OFFICIAL_SUNSET, CIVIL_SUNSET, NAUTICAL_SUNSET, ASTRONOMICAL_SUNSET ->
                    false;
                default ->
                    throw new IllegalStateException(name());
            };
        }

        private final double zenith;
    }

    public SolarDecorator(SolarEventCalculator solarEventCalculator) {
        this.solarEventCalculator = Objects.requireNonNull(solarEventCalculator);
    }

//    public static void main(String[] args) {
//        new SolarDecorator(new SolarEventCalculator(new Location(51.48080, 5.64965), TimeZone.getTimeZone("Europe/Amsterdam"))).calculateArcs(LocalDate.now());
//    }

    public TreeMap<Event, Arc> calculateArcs(LocalDate date) {
        TreeMap<Event, Arc> calculated = new TreeMap<>();
        calculated.put(Event.ASTRONOMICAL_SUNRISE, new Arc(decimalHour(solarEventCalculator.sunset(Event.ASTRONOMICAL_SUNRISE.zenith, date)), decimalHour(solarEventCalculator.sunrise(Event.ASTRONOMICAL_SUNRISE.zenith, date)), NIGHTTIME_COLOR, ARC_STROKE));
        calculated.put(Event.NAUTICAL_SUNRISE, new Arc(calculated.get(Event.ASTRONOMICAL_SUNRISE).end, decimalHour(solarEventCalculator.sunrise(Event.NAUTICAL_SUNRISE.zenith, date)), ASTRONOMICAL_TWILIGHT_COLOR, ARC_STROKE));
        calculated.put(Event.CIVIL_SUNRISE, new Arc(calculated.get(Event.NAUTICAL_SUNRISE).end, decimalHour(solarEventCalculator.sunrise(Event.CIVIL_SUNRISE.zenith, date)), NAUTICAL_TWILIGHT_COLOR, ARC_STROKE));
        calculated.put(Event.OFFICIAL_SUNRISE, new Arc(calculated.get(Event.CIVIL_SUNRISE).end, decimalHour(solarEventCalculator.sunrise(Event.OFFICIAL_SUNRISE.zenith, date)), CIVIL_TWILIGHT_COLOR, ARC_STROKE));
        calculated.put(Event.OFFICIAL_SUNSET, new Arc(calculated.get(Event.OFFICIAL_SUNRISE).end, decimalHour(solarEventCalculator.sunset(Event.OFFICIAL_SUNSET.zenith, date)), DAYTIME_COLOR, ARC_STROKE));
        calculated.put(Event.CIVIL_SUNSET, new Arc(calculated.get(Event.OFFICIAL_SUNSET).end, decimalHour(solarEventCalculator.sunset(Event.CIVIL_SUNSET.zenith, date)), CIVIL_TWILIGHT_COLOR, ARC_STROKE));
        calculated.put(Event.NAUTICAL_SUNSET, new Arc(calculated.get(Event.CIVIL_SUNSET).end, decimalHour(solarEventCalculator.sunset(Event.NAUTICAL_SUNSET.zenith, date)), NAUTICAL_TWILIGHT_COLOR, ARC_STROKE));
        calculated.put(Event.ASTRONOMICAL_SUNSET, new Arc(calculated.get(Event.NAUTICAL_SUNSET).end, decimalHour(solarEventCalculator.sunset(Event.ASTRONOMICAL_SUNSET.zenith, date)), ASTRONOMICAL_TWILIGHT_COLOR, ARC_STROKE));
//        calculated.forEach((event, arc) -> System.out.println("" + event.zenith + ": " + arc.start + " => " + arc.end));
        return calculated;
    }

    private static double decimalHour(LocalDateTime dateTime) {
        return dateTime.getHour() + dateTime.getMinute() / 60;
    }

    private static Color arcColor(Event event) {
        return switch (event) {
            case ASTRONOMICAL_SUNRISE ->
                NIGHTTIME_COLOR;
            case ASTRONOMICAL_SUNSET, NAUTICAL_SUNRISE ->
                ASTRONOMICAL_TWILIGHT_COLOR;
            case NAUTICAL_SUNSET, CIVIL_SUNRISE ->
                NAUTICAL_TWILIGHT_COLOR;
            case CIVIL_SUNSET, OFFICIAL_SUNRISE ->
                CIVIL_TWILIGHT_COLOR;
            case OFFICIAL_SUNSET ->
                DAYTIME_COLOR;
            default ->
                throw new IllegalStateException(event.name());
        };
    }

    private LocalDateTime solarEventDateTime(Event event, final LocalDate date) {
        if (event.isSunrise()) {
            return solarEventCalculator.sunrise(event.zenith, date);
        }
        return solarEventCalculator.sunset(event.zenith, date);
    }

    public class Arc {

        private Arc(double start, double end, Color color, Stroke stroke) {
            this.start = start;
            this.end = end;
            this.color = color;
            this.stroke = stroke;
        }

        public void setStart(double start) {
            this.start = start;
        }

        public void setEnd(double end) {
            this.end = end;
        }

        public double getStart() {
            return start;
        }

        public double getEnd() {
            return end;
        }

        public Color getColor() {
            return color;
        }

        public Stroke getStroke() {
            return stroke;
        }

        private double start;
        private double end;
        private final Color color;
        private final Stroke stroke;
    }


    private final SolarEventCalculator solarEventCalculator;

    private static final BasicStroke ARC_STROKE = new BasicStroke(4f);
}
