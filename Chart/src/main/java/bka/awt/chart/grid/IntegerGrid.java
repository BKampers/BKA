/*
** Copyright © Bart Kampers
*/

package bka.awt.chart.grid;

import java.math.*;
import java.util.*;


public class IntegerGrid extends NumberGrid {


    public IntegerGrid(SortedMap<BigDecimal, BigDecimal> map) {
        super(map);
    }


    public IntegerGrid() {
        super(createDefaultMap());
    }


    private static Map<BigDecimal, BigDecimal> createDefaultMap() {
        Map<BigDecimal, BigDecimal> map =  new TreeMap<>();
        map.put(BigDecimal.valueOf(13, 1), BigDecimal.valueOf(1, 0));
        map.put(BigDecimal.valueOf(2, 0), BigDecimal.valueOf(2, 0));
        map.put(BigDecimal.valueOf(5, 0), BigDecimal.valueOf(5, 0));
        map.put(BigDecimal.valueOf(10, 0), BigDecimal.valueOf(10, 0));
        return map;
    }


    @Override
    protected void compute(Number min, Number max) {
        List<Number> values = new ArrayList<>();
        double low = min.doubleValue();
        double high = max.doubleValue();
        addValues(low, high, values);
        if (values.isEmpty()) {
            values.add(low);
        }
        addMarkerList(new MarkerList(values, "%d"));
    }

    
    private void addValues(double low, double high, List<Number> values) {
        long step = computeStep(low, high);
        long markerValue = round(low / step) * step;
        while (markerValue <= high) {
            values.add(markerValue);
            markerValue += step;
        }
        values.add(markerValue);
    }


    private static long round(double value) {
        long trunk = (long) value;
        return (value < 0) ? trunk - 1 : trunk;
    }


    private long computeStep(double low, double high) {
        BigDecimal range = new BigDecimal(high - low);
        int exponent = range.precision() - range.scale() - 1;
        if (exponent <= 0) {
            return 1;
        }
        BigDecimal significand = range.movePointLeft(exponent);
        BigDecimal step = determineStepSize(significand);
        return step.movePointRight(exponent - 1).longValue();
    }

}
