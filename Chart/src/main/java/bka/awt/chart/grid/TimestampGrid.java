/*
** Copyright © Bart Kampers
*/

package bka.awt.chart.grid;

import java.text.*;
import java.util.*;
import java.util.concurrent.*;


public class TimestampGrid extends Grid {
    

    private static final String YEAR_FORMAT = "yyyy>";
    private static final String MONTHS_FORMAT = "d MMM yyyy";
    private static final String MONTH_FORMAT = "MMM yyyy>";
    private static final String DAYS_FORMAT = "d MMM";
    private static final String DAY_FORMAT = "d MMM>";
    private static final String MINUTES_FORMAT = "H:mm";
    private static final String SECONDS_FORMAT = "H:mm:ss";
    private static final String MILLIS_FORMAT = "ss.SSS";

    protected enum StepSize {
        ONE_YEAR(Calendar.YEAR, 1, YEAR_FORMAT),
        SIX_MONTHS(Calendar.MONTH, 6, MONTHS_FORMAT),
        FOUR_MONTHS(Calendar.MONTH, 4, MONTHS_FORMAT),
        THREE_MONTHS(Calendar.MONTH, 3, MONTHS_FORMAT),
        ONE_MONTH(Calendar.MONTH, 1, MONTH_FORMAT),
        TWO_WEEKS(Calendar.WEEK_OF_YEAR, 2, DAYS_FORMAT),
        ONE_WEEK(Calendar.WEEK_OF_YEAR, 1, DAYS_FORMAT),
        FIVE_DAYS(Calendar.DATE, 5, DAYS_FORMAT),
        TWO_DAYS(Calendar.DATE, 2, DAYS_FORMAT),
        ONE_DAY(Calendar.DATE, 1, DAY_FORMAT),
        TWELVE_HOURS(Calendar.HOUR_OF_DAY, 12, MINUTES_FORMAT),
        SIX_HOURS(Calendar.HOUR_OF_DAY, 6, MINUTES_FORMAT),
        FOUR_HOURS(Calendar.HOUR_OF_DAY, 4, MINUTES_FORMAT),
        THREE_HOURS(Calendar.HOUR_OF_DAY, 3, MINUTES_FORMAT),
        TWO_HOURS(Calendar.HOUR_OF_DAY, 2, MINUTES_FORMAT),
        ONE_HOUR(Calendar.HOUR_OF_DAY, 1, MINUTES_FORMAT),
        THIRTY_MINUTES(Calendar.MINUTE, 30, MINUTES_FORMAT),
        TWENTY_MINUTES(Calendar.MINUTE, 20, MINUTES_FORMAT),
        FIFTEEN_MINUTES(Calendar.MINUTE, 15, MINUTES_FORMAT),
        TEN_MINUTES(Calendar.MINUTE, 10, MINUTES_FORMAT),
        FIVE_MINUTES(Calendar.MINUTE, 5, MINUTES_FORMAT),
        TWO_MINUTES(Calendar.MINUTE, 2, MINUTES_FORMAT),
        ONE_MINUTE(Calendar.MINUTE, 1, MINUTES_FORMAT),
        THIRTY_SECONDS(Calendar.SECOND, 30, SECONDS_FORMAT),
        TWENTY_SECONDS(Calendar.SECOND, 20, SECONDS_FORMAT),
        FIFTEEN_SECONDS(Calendar.SECOND, 15, SECONDS_FORMAT),
        TEN_SECONDS(Calendar.SECOND, 10, SECONDS_FORMAT),
        FIVE_SECONDS(Calendar.SECOND, 5, SECONDS_FORMAT),
        TWO_SECONDS(Calendar.SECOND, 2, SECONDS_FORMAT),
        ONE_SECOND(Calendar.SECOND, 1, SECONDS_FORMAT),
        FIVE_HUNDRED_MILLIS(Calendar.MILLISECOND, 500, MILLIS_FORMAT),
        TWO_HUNDRED_MILLIS(Calendar.MILLISECOND, 200, MILLIS_FORMAT),
        ONE_HUNDRED_MILLIS(Calendar.MILLISECOND, 100, MILLIS_FORMAT),
        FIFTY_MILLIS(Calendar.MILLISECOND, 50, MILLIS_FORMAT),
        TWENTY_MILLIS(Calendar.MILLISECOND, 20, MILLIS_FORMAT),
        TEN_MILLIS(Calendar.MILLISECOND, 10, MILLIS_FORMAT),
        FIVE_MILLIS(Calendar.MILLISECOND, 5, MILLIS_FORMAT),
        ONE_MILLI(Calendar.MILLISECOND, 1, MILLIS_FORMAT);


        StepSize(int field, int amount, String format) {
            this.field = field;
            this.amount = amount;
            this.format = format;
        }

        final int field;
        final int amount;
        final String format;

    }

    @Override
    protected String labelFormat() {
        return MONTHS_FORMAT + ' ' + SECONDS_FORMAT;
    }

    @Override
    public String label(String format, Locale locale, Number value) {
        SimpleDateFormat dateFormat = new SimpleDateFormat(format, locale);
        return dateFormat.format(new Date(value.longValue()));
    }
    
    
    @Override
    protected void compute(Number min, Number max) {
        StepSize stepSize = primaryStepSize(min.longValue(), max.longValue());
        populatePrimaryMarkers(min.longValue(), max.longValue(), stepSize);
        populateSecondaryMarkers(min.longValue(), max.longValue(), stepSize);
    }
    
    
    private void populatePrimaryMarkers(long min, long max, StepSize stepSize) {
        Calendar calendar = getCalendar(min, stepSize);
        List<Number> markerValues = new ArrayList<>();
        markerValues.add(calendar.getTimeInMillis());
        addMarkerValues(calendar, max, stepSize, markerValues, true);
        addMarkerList(new MarkerList(markerValues, stepSize.format));
    }
    
    
    private void populateSecondaryMarkers(long min, long max, StepSize stepSize) {
        StepSize secondaryStepSize = secondaryStepSize(stepSize);
        if (secondaryStepSize != null) {
            Calendar calendar = getCalendar(min, secondaryStepSize);
            List<Number> markerValues = new ArrayList<>();
            markerValues.add(min);
            addMarkerValues(calendar, max, secondaryStepSize, markerValues, false);
            markerValues.add(max);
            String format = secondaryStepSize.format;
            if (! format.endsWith(">")) {
                format += ">";
            }
            addMarkerList(new MarkerList(markerValues, format));
        }
    }

    
    private void addMarkerValues(Calendar calendar, long max, StepSize stepSize, List<Number> markerValues, boolean including) {
        boolean ready = false;
        while (! ready) {
            calendar.add(stepSize.field, stepSize.amount);
            ready = calendar.getTimeInMillis() > max;
            if (including || ! ready) {
                markerValues.add(calendar.getTimeInMillis());
            }
        }
    }
    
    
    private static Calendar getCalendar(long timestamp, StepSize stepSize) {
        Calendar calendar = Calendar.getInstance();
        calendar.setTimeInMillis(timestamp);
        adjust(calendar, stepSize);
        round(calendar, stepSize);
        return calendar;
    }
    
    
    private static void adjust(Calendar calendar, StepSize stepSize) {
        switch (stepSize.field) {
            case Calendar.YEAR:
                calendar.set(Calendar.DAY_OF_YEAR, calendar.getMinimum(Calendar.DAY_OF_YEAR));
                break;
            case Calendar.MONTH:
                calendar.set(Calendar.DATE, calendar.getMinimum(Calendar.DATE));
                break;
            case Calendar.WEEK_OF_YEAR:
                calendar.add(Calendar.DAY_OF_WEEK, calendar.getActualMinimum(Calendar.DAY_OF_WEEK) - calendar.get(Calendar.DAY_OF_WEEK));
                calendar.set(Calendar.HOUR_OF_DAY, calendar.getMinimum(Calendar.HOUR_OF_DAY));
                break;
            case Calendar.DATE:
                calendar.add(Calendar.HOUR_OF_DAY, calendar.getActualMinimum(Calendar.HOUR_OF_DAY) - calendar.get(Calendar.HOUR_OF_DAY));
                break;
            case Calendar.HOUR_OF_DAY:
                calendar.add(Calendar.HOUR_OF_DAY, - (calendar.get(Calendar.HOUR_OF_DAY) % stepSize.amount));
                break;
        }
    }


    private static void round(Calendar calendar, StepSize stepSize) {
        switch (stepSize.field) {
            case Calendar.YEAR:
            case Calendar.MONTH:
            case Calendar.WEEK_OF_YEAR:
            case Calendar.DATE:
            case Calendar.HOUR_OF_DAY:
                //low.set(Calendar.MINUTE, 0); <- This affects DST offset, do not use it
                calendar.add(Calendar.MILLISECOND, - calendar.get(Calendar.MILLISECOND));
                calendar.add(Calendar.SECOND, - calendar.get(Calendar.SECOND));
                calendar.add(Calendar.MINUTE, - calendar.get(Calendar.MINUTE));
                break;
            case Calendar.MINUTE:
                //low.clear(Calendar.SECOND); <- This affects DST offset, do not use it
                long round = TimeUnit.MINUTES.toMillis(stepSize.amount);
                calendar.setTimeInMillis((calendar.getTimeInMillis() / round) * round);
                break;
            case Calendar.SECOND:
                //low.clear(Calendar.MILLISECOND); <- This affects DST offset, do not use it
                round = TimeUnit.SECONDS.toMillis(stepSize.amount);
                calendar.setTimeInMillis((calendar.getTimeInMillis() / round) * round);
                break;
            case Calendar.MILLISECOND:
                round = stepSize.amount;
                calendar.setTimeInMillis((calendar.getTimeInMillis() / round) * round);
                break;
        }
    }


    private StepSize primaryStepSize(long min, long max) {
        Calendar test = Calendar.getInstance();
        StepSize[] sizes = getStepSizes();
        StepSize stepSize = sizes[0];
        for (StepSize size : sizes) {
            test.setTimeInMillis(min);
            test.add(size.field, size.amount * MAX_GRID_AREAS);
            if (test.getTimeInMillis() <= max) {
                return stepSize;
            }
            stepSize = size;
        }
        return stepSize;
    }


    private static StepSize secondaryStepSize(StepSize primary) {
        switch (primary.field) {
            case Calendar.MILLISECOND:
                return StepSize.ONE_HOUR;
            case Calendar.SECOND:
            case Calendar.MINUTE:
            case Calendar.HOUR_OF_DAY:
                return StepSize.ONE_DAY;
            default:
                return null;
        }
    }

    
    protected StepSize[] getStepSizes() {
        return StepSize.values();
    }


    private static final int MAX_GRID_AREAS = 11;

}
