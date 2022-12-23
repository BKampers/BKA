/*
** © Bart Kampers
*/

package bka.awt.chart;

import java.util.*;


public class RangeMap {


    public RangeMap(Number min, Number max) {
        ranges.put(null, new Range(min, max));
    }


    public RangeMap(RangeMap rangeMap) {
        for (Map.Entry<Object, Range> entry : rangeMap.ranges.entrySet()) {
            ranges.put(entry.getKey(), new Range(entry.getValue()));
        }
    }


    public void put(Object key, Number min, Number max) {
        ranges.put(key, new Range(min, max));
    }


    public Range get(Object key) {
        if (! ranges.containsKey(key)) {
            return ranges.get(null);
        }
        return ranges.get(key);
    }


    public Range getDefault() {
        return ranges.get(null);
    }


    public boolean containsKey(Object key) {
        return ranges.containsKey(key);
    }


    public Collection<Range> values() {
        return ranges.values();
    }


    public Map<Object, Range> getRanges() {
        return Collections.unmodifiableMap(ranges);
    }


    @Override
    public boolean equals(Object object) {
        if (object == this) {
            return true;
        }
        if (! (object instanceof RangeMap)) {
            return false;
        }
        return ranges.equals(((RangeMap) object).ranges);
    }


    @Override
    public int hashCode() {
        return ranges.hashCode();
    }


    private final Map<Object, Range> ranges = new HashMap<>();

}
