/*
** © Bart Kampers
*/

package bka.calendar.events;

import java.time.*;
import java.util.*;

/**
 * Calculates sunrise times and sunset times for given location and time zone.
 */
public class SolarEventCalculator {

    public SolarEventCalculator(Location location, TimeZone timeZone) {
        latitude = Math.toRadians(location.getLatitude());
        baseLongitudeHour = Math.toRadians(location.getLongitude()) / HOUR_ANGLE;
        this.timeZone = Objects.requireNonNull(timeZone);
    }

    /**
     * @param solarZenith of sunrise to compute, in radians @see Zenith class
     * @param date to compute the sunrise for
     * @return sunrise <code>LocalDateTime</code> or <code>null</code> for no sunrise
     */
    public LocalDateTime sunrise(double solarZenith, LocalDate date) {
        return solarEventTime(solarZenith, date, SUNRISE_HOUR_OFFSET);
    }

    /**
     * @param solarZenith of sunet to compute, in radians @see Zenith class
     * @param date to compute the sunset for
     * @return sunset <code>LocalDateTime</code> or <code>null</code> for no sunset
     */
    public LocalDateTime sunset(double solarZenith, LocalDate date) {
        return solarEventTime(solarZenith, date, SUNSET_HOUR_OFFSET);
    }

    private LocalDateTime solarEventTime(double zenith, LocalDate date, int eventHourOffset) {
        double longitudeHour = longitudeHour(date, eventHourOffset);
        double sunTrueLongitude = sunTrueLongitude(meanAnomaly(longitudeHour));
        double cosineSunLocalHour = cosineSunLocalHour(zenith, sunTrueLongitude);
        if (Double.isNaN(cosineSunLocalHour)) {
            return null;
        }
        double sunLocalHour = (eventHourOffset == SUNRISE_HOUR_OFFSET)
            ? sunriseLocalHour(Math.acos(cosineSunLocalHour))
            : sunsetLocalHour(Math.acos(cosineSunLocalHour));
        return zoneDateTime(date, localMeanTime(sunTrueLongitude, longitudeHour, sunLocalHour) - baseLongitudeHour);
    }

    private LocalDateTime zoneDateTime(LocalDate date, double localMeanTime) {
        LocalDateTime meanDateTime = localDateTimeOf(date, localMeanTime);
        return meanDateTime.plusMinutes(zoneOffset(meanDateTime));
    }

    private int zoneOffset(LocalDateTime meanDateTime) {
        return timeZone.getOffset(meanDateTime.toEpochSecond(ZoneOffset.UTC) * 1000L) / MILLIS_PER_MINUTE;
    }

    private static LocalDateTime localDateTimeOf(LocalDate date, double hours) {
        return LocalDateTime.of(date, LocalTime.MIDNIGHT).plusMinutes((int) Math.round(hours * MINUTES_PER_HOUR));
    }

    private double longitudeHour(LocalDate date, int eventHourOffset) {
        return date.getDayOfYear() + (eventHourOffset - baseLongitudeHour) / HOURS_PER_DAY;
    }

    /**
     * @param meanAnomaly
     * @return the Sun's true longitude for the given the sun's mean anomaly
     */
    private static double sunTrueLongitude(double meanAnomaly) {
        return normalizedAngle(
            meanAnomaly
            + Math.sin(meanAnomaly) * Math.PI * 0.0106
            + Math.sin(meanAnomaly * 2.0) * Math.PI * 0.0001
            + Math.PI * 1.5702
        );
    }

    private double cosineSunLocalHour(double zenith, double sunTrueLongitude) {
        double sinSunDeclination = sinOfSunDeclination(sunTrueLongitude);
        double cosine
            = (Math.cos(zenith) - sinSunDeclination * Math.sin(latitude))
            / (cosineOfSunDeclination(sinSunDeclination) * Math.cos(latitude));
        if (cosine < -1.0 || 1.0 < cosine) {
            return Double.NaN;
        }
        return cosine;
    }

    private static double sinOfSunDeclination(double sunTrueLongitude) {
        return Math.sin(sunTrueLongitude) * 0.39782;
    }

    private static double cosineOfSunDeclination(double sinSunDeclination) {
        return Math.cos(Math.asin(sinSunDeclination));
    }

    private static double sunriseLocalHour(double localHour) {
        return (FULL_ANGLE - localHour) / HOUR_ANGLE;
    }

    private static double sunsetLocalHour(double localHour) {
        return localHour / HOUR_ANGLE;
    }

    /**
     * @param longitudeHour
     * @return the sun's mean anomaly for the given longitude hour in radians
     */
    private static double meanAnomaly(double longitudeHour) {
        return Math.toRadians(0.9856 * longitudeHour) - 0.01827 * Math.PI;
    }

    private static double localMeanTime(double sunTrueLongitude, double longitudeHour, double sunLocalHour) {
        return normalizedHour(sunLocalHour + rightAscension(sunTrueLongitude) - longitudeHour * 0.06571 - 6.622);
    }

    /**
     * @param sunTrueLongitude
     * @return Sun's right ascension for the given Sun's true longitude, in radians
     */
    private static double rightAscension(double sunTrueLongitude) {
        return (normalizedAngle(Math.atan(Math.tan(sunTrueLongitude) * 0.91764)) + augend(sunTrueLongitude)) / HOUR_ANGLE;
    }

    private static double augend(double sunTrueLongitude) {
        double rightAscension = normalizedAngle(Math.atan(Math.tan(sunTrueLongitude) * 0.91764));
        return quadrant(sunTrueLongitude) - quadrant(rightAscension);
    }

    private static double normalizedAngle(double angle) {
        return normalized(angle, FULL_ANGLE);
    }

    private static double normalizedHour(double hour) {
        return normalized(hour, HOURS_PER_DAY);
    }

    private static double normalized(double value, double bound) {
        if (value < 0.0) {
            return value + bound;
        }
        if (value > bound) {
            return value - bound;
        }
        return value;
    }

    private static double quadrant(double angle) {
        return Math.floor(angle / RIGHT_ANGLE) * RIGHT_ANGLE;
    }

    private final double latitude;
    private final double baseLongitudeHour;
    private final TimeZone timeZone;

    private static final double FULL_ANGLE = 2 * Math.PI;
    private static final double RIGHT_ANGLE = 0.5 * Math.PI;
    private static final double HOUR_ANGLE = Math.PI / 12;

    private static final double HOURS_PER_DAY = 24;
    private static final double MINUTES_PER_HOUR = 60;
    private static final int MILLIS_PER_MINUTE = 60000;

    private static final int SUNRISE_HOUR_OFFSET = 6;
    private static final int SUNSET_HOUR_OFFSET = 18;

}
