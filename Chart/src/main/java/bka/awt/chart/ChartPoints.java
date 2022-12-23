/*
** Copyright © Bart Kampers
*/

package bka.awt.chart;

import java.util.*;


public class ChartPoints implements Iterable<ChartPoint> {


    public ChartPoints() {
        points = new ArrayList<>();
    }


    public ChartPoints(Map<Number, Number> source) {
        points = new ArrayList<>(source.size());
        source.forEach((x, y) -> add(x, y));
    }


    public final void add(Number x, Number y) {
        add(x, y, false);
    }


    public final void add(Number x, Number y, boolean outsideWindow) {
        points.add(new ChartPoint(x, y, outsideWindow));
    }


    public void add(ChartPoint point) {
        points.add(point);
    }


    @Override
    public final Iterator<ChartPoint> iterator() {
        return points.iterator();
    }


    public final boolean isEmpty() {
        return points.isEmpty();
    }


    public final int size() {
        return points.size();
    }


    public ChartPoint get(int index) {
        return points.get(index);
    }


    private final List<ChartPoint> points;

}
