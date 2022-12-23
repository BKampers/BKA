/*
** © Bart Kampers
*/

package bka.awt.chart.grid;

import bka.awt.chart.*;
import java.util.*;


public abstract class Grid {


    public class MarkerList {

        protected MarkerList(List<Number> values, String format) {
            this.values = values;
            this.format = format;
        }

        public List<Number> getValues() {
            return Collections.unmodifiableList(values);
        }

        public String getLabel(Locale locale, Number value) {
            return label(format, locale, value);
        }

        private final List<Number> values;
        private final String format;
    }


    public void initialize(Range range) {
        initialize(range.getMin(), range.getMax());
    }


    public void initialize(Number min, Number max) {
        markerLists.clear();
        if (min != null && max != null) {
            if (min.equals(max)) {
                compute(min);
            }
            else {
                compute(min, max);
            }
        }
    }

    private void compute(Number value) {
        ArrayList<Number> markerValues = new ArrayList<>();
        markerValues.add(value);
        addMarkerList(new MarkerList(markerValues, labelFormat()));
    }

    protected String labelFormat() {
        return "%s";
    }

    public List<MarkerList> getMarkerLists() {
        return Collections.unmodifiableList(markerLists);
    }


    public List<Number> getValues() {
        if (markerLists.isEmpty()) {
            return Collections.emptyList();
        }
        return Collections.unmodifiableList(markerLists.get(0).getValues());
    }


    protected void addMarkerList(MarkerList markerList) {
        markerLists.add(markerList);
    }


    protected abstract void compute(Number min, Number max);

    protected abstract String label(String format, Locale locale, Number value);


    private final List<MarkerList> markerLists = new ArrayList<>();

}
