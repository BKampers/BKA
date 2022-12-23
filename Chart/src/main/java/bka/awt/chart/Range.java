/*
** © Bart Kampers
*/

package bka.awt.chart;

import java.util.*;


public class Range {


    public Range(Range range) {
        min = range.min;
        max = range.max;
    }


    /**
     * @param min null allowed
     * @param max null allowed
     * @throws IllegalArgumentException if min > max
     */
    public Range(Number min, Number max) {
        set(min, max);
    }


    /**
     * Set min and max
     * @param min null allowed
     * @param max null allowed
     * @throws IllegalArgumentException if min and max both not null and min > max
     */
    public final void set(Number min, Number max) {
        validate(min, max);
        this.min = min;
        this.max = max;
    }

    /**
     * @return true if both min and max are not null, false otherwise
     */
    public boolean isInitialized() {
        return min != null && max != null;
    }

   
    public Number getMin() {
        return min;
    }


    /**
     * Set min
     * @param min null allowed
     * @throws IllegalArgumentException if min and this.max both not null and min > this.max
     */
    public void setMin(Number min) {
        validate(min, max);
        this.min = min;
    }


    public Number getMax() {
        return max;
    }


    /**
     * Set max
     * @param max null allowed
     * @throws IllegalArgumentException if this.min and max both not null and this.min > max
     */
    public void setMax(Number max) {
        validate(min, max);
        this.max = max;
    }


    private static void validate(Number min, Number max) {
        if (min != null && max != null && min.doubleValue() > max.doubleValue()) {
            throw new IllegalArgumentException(min + " > " + max);
        }
    }


    /**
     * Adjust min and max to number
     *
     * @param number
     * @throws RuntimeException if number is null
     *
     * @see adjustMin
     * @see adjustMax
     */
    public void adjust(Number number) {
        adjustMin(number);
        adjustMax(number);
    }


    /**
     * Set min to number if min is null or min < number
     *
     * @param number
     * @throws RuntimeException if number is null
     */
    public void adjustMin(Number number) {
        Objects.requireNonNull(number);
        if (min == null || number.doubleValue() < min.doubleValue()) {
            min = number;
        }
    }


    /**
     * Set max to number if max is null or number >&gt; max
     *
     * @param number
     * @throws RuntimeException if number is null
     */
    public void adjustMax(Number number) {
        Objects.requireNonNull(number);
        if (max == null || number.doubleValue() > max.doubleValue()) {
            max = number;
        }
    }

    /**
     * @param number
     * @return true if given number is between this range's min and max,
     *         where min is handled as negative-infinity if null and max is handles as positive-infinity if null
     */
    public boolean includes(Number number) {
        return (min == null || min.doubleValue() <= number.doubleValue()) && (max == null || number.doubleValue() <= max.doubleValue());
    }
    
    
    @Override
    public int hashCode() {
        return Objects.hash(min, max);
    }


    @Override
    public boolean equals(Object object) {
        if (object == this) {
            return true;
        }
        if (! (object instanceof Range)) {
            return false;
        }
        return equals(((Range) object).min, ((Range) object).max);
    }


    public boolean equals(Number min, Number max) {
        return Objects.equals(this.min, min) && Objects.equals(this.max, max);
    }


    private Number min;
    private Number max;

}
