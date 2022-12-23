package bka.awt.clock;

public final class Scale {

    public Scale(double minValue, double maxValue) {
        this(minValue, maxValue, 0.0, 1.0);
    }

    public Scale(double minValue, double maxValue, double minAngle, double maxAngle) {
        this.minValue = minValue;
        this.maxValue = maxValue;
        this.minAngle = minAngle;
        this.maxAngle = maxAngle;
    }

    public double getMinValue() {
        return minValue;
    }

    public double getMaxValue() {
        return maxValue;
    }

    public double radians(double value) {
        return angleRatio(value) * 2d * Math.PI;
    }

    public double degrees(double value) {
        return angleRatio(value) * 360d;
    }

    private double angleRatio(double value) {
        return minAngle + (maxAngle - minAngle) * (value - minValue) / (maxValue - minValue);
    }

    private final double minValue;
    private final double maxValue;
    private final double minAngle;
    private final double maxAngle;

}
