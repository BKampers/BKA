/*
** © Bart Kampers
*/

package bka.demo.clock.weatherstation;

import bka.awt.clock.*;
import java.awt.*;
import java.text.*;
import java.util.*;
import java.util.function.*;

public class ClockPanel extends javax.swing.JPanel {

    public ClockPanel(Scale scale, int markerInterval) {
        this(scale, markerInterval, new FormattedValueRenderer(NO_DATA_COLOR, FONT));
    }

    public ClockPanel(Scale scale, int markerInterval, NumberFormat format) {
        this(scale, markerInterval, new FormattedValueRenderer(NO_DATA_COLOR, FONT, format));
    }

    public ClockPanel(Scale scale, int markerInterval, FormattedValueRenderer markerRenderer) {
        super();
        setDimension();
        clockRenderer = new ClockRenderer(new Point(RADIUS, RADIUS), scale);
        mainMarkerRenderer = markerRenderer;
        continuousScale = normalized(scale.getMinAngle()) == normalized(scale.getMaxAngle());
        clockRenderer.addClockFace(RADIUS, Color.WHITE);
        clockRenderer.addMarkerRingRenderer(NUMBER_MARKER_RADIUS, markerInterval, mainMarkerRenderer);
        Timer timer = new Timer();
        timer.schedule(updateTask, UPDATE_INTERVAL, UPDATE_INTERVAL);
    }

    private static int normalized(double value) {
        return (int) Math.round((value - Math.floor(value)) * 10000);
    }

    private void setDimension() {
        Dimension size = new Dimension(RADIUS * 2, RADIUS * 2);
        setPreferredSize(size);
        setMinimumSize(size);
        setSize(size);
    }

    @Override
    public void paint(Graphics graphics) {
        clockRenderer.paint((Graphics2D) graphics);
    }

    public void addFineMarkers(double interval, Predicate<Double> isMajor) {
        clockRenderer.addMarkerRingRenderer(FINE_MARKER_RADIUS, interval, MAJOR_MARKER_LENGTH, MINOR_MARKER_LENGTH, isMajor, FINE_MARKER_COLOR, MARKER_WIDTH);
    }

    public void addText(String text) {
        Point center = clockRenderer.getCenter();
        clockRenderer.add(new TextRenderer(new Point(center.x, center.y + RADIUS / 3), text, FONT, MARKER_COLOR));
    }

    public void addArc(double start, double end, Paint paint) {
        clockRenderer.addArc(ARC_RADIUS, start, end, paint, ARC_WIDTH);
    }

    public void addArcMarker(double value, bka.awt.Renderer markerRenderer) {
        clockRenderer.addTiltedMarkerRenderer(ARC_RADIUS, value, markerRenderer);
    }

    public void addMarker(double value, bka.awt.Renderer markerRenderer) {
        clockRenderer.addTiltedMarkerRenderer(NUMBER_MARKER_RADIUS, value, graphics -> {
            graphics.setPaint(FINE_MARKER_COLOR);
            graphics.setStroke(new BasicStroke(MARKER_WIDTH));
            markerRenderer.paint(graphics);
        });
    }

    public void addNeedle(Measurement measurement, Paint paint) {
        addNeedle(measurement, paint, 0);
    }

    public void addCardinalNeedle(Measurement measurement, Paint paint, double defaultValue) {
        addNeedle(measurement, ArrowRenderer.cardinalArrowRenderer(RADIUS, NO_DATA_COLOR), paint, defaultValue);
    }

    public void addNeedle(Measurement measurement, Paint paint, double defaultValue) {
        addNeedle(measurement, ArrowRenderer.defaultArrowRenderer(RADIUS, NO_DATA_COLOR), paint, defaultValue);
    }

    public void addNeedle(Measurement measurement, ArrowRenderer arrowRenderer, Paint paint, double defaultValue) {
        needles.put(measurement, new Needle(clockRenderer, arrowRenderer, paint, defaultValue));
    }

    public void update(WeatherStation station) {
        needles.forEach((measurement, needle) -> {
            Double value = measurement.getValue(station);
            if (value != null) {
                mainMarkerRenderer.setPaint(MARKER_COLOR);
                needle.applyPaint();
                updateTask.setValue(measurement, value);
            }
            else {
                mainMarkerRenderer.setPaint(NO_DATA_COLOR);
                needle.applyPaint(NO_DATA_COLOR);
                updateTask.removeValue(measurement);
            }
        });
    }

    private class Needle {

        public Needle(ClockRenderer clockRenderer, ArrowRenderer arrowRenderer, Paint paint, double defaultValue) {
            this.arrowRenderer = arrowRenderer;
            needleRenderer = clockRenderer.addNeedleRenderer(arrowRenderer);
            needleRenderer.setValue(defaultValue);
            this.paint = paint;
            incrementStep = Math.abs(getScale().getMaxValue() - getScale().getMinValue()) * UPDATE_INCREMENT_FRACTION;
        }

        public double getValue() {
            return needleRenderer.getValue();
        }

        public void setValue(double value) {
            needleRenderer.setValue(value);
        }

        public void addValue(double increment) {
            double value = needleRenderer.getValue() + increment;
            if (value < needleRenderer.getScale().getMinValue()) {
                value = needleRenderer.getScale().getMaxValue() + (value - needleRenderer.getScale().getMinValue());
            }
            else if (value > needleRenderer.getScale().getMaxValue()) {
                value = needleRenderer.getScale().getMinValue() + (value - needleRenderer.getScale().getMaxValue());
            }
            setValue(value);
        }

        public void applyPaint() {
            applyPaint(paint);
        }

        public void applyPaint(Paint paint) {
            arrowRenderer.setPaint(paint);
        }

        public Scale getScale() {
            return needleRenderer.getScale();
        }

        public double getAnimationStep() {
            return incrementStep;
        }

        private final NeedleRenderer needleRenderer;
        private final ArrowRenderer arrowRenderer;
        private final Paint paint;
        final double incrementStep;
    }


    private class UpdateTask extends TimerTask {

        public void setValue(Measurement measurement, Double targetValue) {
            synchronized (targetValues) {
                targetValues.put(measurement, targetValue);
            }
        }

        public void removeValue(Measurement measurement) {
            synchronized (targetValues) {
                targetValues.remove(measurement);
                repaint();
            }
        }

        @Override
        public void run() {
            synchronized (targetValues) {
                if (targetValues.isEmpty()) {
                    return;
                }
                needles.forEach((measurement, needle) -> {
                    Double targetValue = targetValues.get(measurement);
                    if (targetValue != null) {
                        move(needle, targetValue, measurement);
                    }
                });
                repaint();
            }
        }

        private void move(Needle needle, Double targetValue, Measurement measurement) {
            double distance = distance(needle, targetValue);
            double step = needle.getAnimationStep();
            if (Math.abs(distance) > step) {
                needle.addValue((distance < 0) ? -step : step);
            }
            else {
                needle.setValue(targetValues.get(measurement));
                targetValues.remove(measurement);
            }
        }

        private double distance(Needle needle, double targetValue) {
            double distance = targetValue - needle.getValue();
            if (continuousScale) {
                double alternative = alternativeDistance(needle.getScale(), needle.getValue(), targetValue);
                if (Math.abs(alternative) < Math.abs(distance)) {
                    return alternative;
                }
            }
            return distance;
        }

        private double alternativeDistance(Scale scale, double actual, double target) {
            if (actual < target) {
                return -(actual - scale.getMinValue() + scale.getMaxValue() - target);
            }
            return target - scale.getMinValue() + scale.getMaxValue() - actual;
        }

        private final Map<Measurement, Double> targetValues = new HashMap<>();

    }


    private final UpdateTask updateTask = new UpdateTask();
    private final LinkedHashMap<Measurement, Needle> needles = new LinkedHashMap<>();

    private final ClockRenderer clockRenderer;
    private final FormattedValueRenderer mainMarkerRenderer;
    private final boolean continuousScale;

    private static final Color MARKER_COLOR = Color.BLUE;
    private static final Color FINE_MARKER_COLOR = Color.LIGHT_GRAY;
    private static final Color NO_DATA_COLOR = new Color(0xE1E1E1);
    private static final Font FONT = new Font(Font.DIALOG, Font.PLAIN, 12);

    private static final int RADIUS = 100;
    private static final float ARC_WIDTH = (float) RADIUS / 20f;
    private static final double ARC_RADIUS = 0.95 * RADIUS;
    private static final double NUMBER_MARKER_RADIUS = 0.85 * RADIUS;
    private static final double FINE_MARKER_RADIUS = 0.79 * RADIUS;
    private static final float MARKER_WIDTH = RADIUS / 100f;
    private static final int MINOR_MARKER_LENGTH = (int) (RADIUS * 0.07);
    private static final int MAJOR_MARKER_LENGTH = (int) (RADIUS * 0.1);

    private static final long UPDATE_INTERVAL = 100;
    private static final double UPDATE_INCREMENT_FRACTION = 0.01;

}
