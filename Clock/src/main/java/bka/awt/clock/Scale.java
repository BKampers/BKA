package bka.awt.clock;

public final class Scale {

    public Scale(double minValue, double maxValue) {
        this(minValue, maxValue, 0.0, 1.0);
    }

    public Scale(double minValue, double maxValue, double minAngle, double maxAngle) {
        setValueRange(minValue, maxValue);
        setAngleRange(minAngle, maxAngle);
    }

    public final void setValueRange(double min, double max) {
        minValue = min;
        maxValue = max;
    }

    public final void setAngleRange(double min, double max) {
        minAngle = min;
        maxAngle = max;
    }

    public double getMinValue() {
        return minValue;
    }

    public double getMaxValue() {
        return maxValue;
    }

    public double getMinAngle() {
        return minAngle;
    }

    public double getMaxAngle() {
        return maxAngle;
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

    private double minValue;
    private double maxValue;
    private double minAngle;
    private double maxAngle;

}
