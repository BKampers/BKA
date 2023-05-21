/*
** © Bart Kampers
*/

package bka.demo.clock.weatherstation;

import bka.awt.clock.*;
import java.awt.*;
import java.util.*;
import java.util.function.*;

public class ClockPanel extends javax.swing.JPanel {

    public enum Measurement {
        TEMPERATURE(station -> station.getTemperature()),
        CHILL(station -> computeValue(station.getChill(), () -> station.getTemperature())),
        HUMIDITY(station -> station.getHumidity()),
        WIND_DIRECTION(station -> station.getWindDirection()),
        WIND_SPEED(station -> station.getWindSpeed()),
        SQUALL(station -> computeValue(station.getSquall(), value -> value / 3.6, () -> station.getWindSpeed())),
        PRESSURE(station -> station.getPressure()),
        VISIBILITY(station -> computeValue(station.getVisibility(), value -> value / 1000));

        private Measurement(Function<WeatherStation, Double> provider) {
            this.provider = provider;
        }

        public Double getValue(WeatherStation station) {
            return provider.apply(station);
        }

        private static Double computeValue(Double value, Supplier<Double> alternative) {
            return computeValue(value, Function.identity(), alternative);
        }

        private static Double computeValue(Double value, Function<Double, Double> processor) {
            return computeValue(value, processor, () -> null);
        }

        private static Double computeValue(Double value, Function<Double, Double> processor, Supplier<Double> alternative) {
            if (value == null) {
                return alternative.get();
            }
            return processor.apply(value);
        }

        private final Function<WeatherStation, Double> provider;
    }


    public ClockPanel(Scale scale, int markerInterval) {
        this(scale, markerInterval, new FormattedValueRenderer(NO_DATA_COLOR, FONT));
    }

    public ClockPanel(Scale scale, int markerInterval, FormattedValueRenderer markers) {
        super();
        setDimension();
        renderer = new ClockRenderer(new Point(RADIUS, RADIUS), scale);
        this.markers = markers;
        renderer.addClockFace(RADIUS, Color.WHITE);
        renderer.addMarkerRingRenderer(NUMBER_MARKER_RADIUS, markerInterval, markers);
        java.util.Timer timer = new java.util.Timer();
        timer.schedule(updateTask, UPDATE_INTERVAL, UPDATE_INTERVAL);
    }

    private void setDimension() {
        Dimension size = new Dimension(RADIUS * 2, RADIUS * 2);
        setPreferredSize(size);
        setMinimumSize(size);
        setSize(size);
    }

    public void addFineMarkers(double interval, Predicate<Double> isMajor) {
        getRenderer().addMarkerRingRenderer(FINE_MARKER_RADIUS, interval, MAJOR_MARKER_LENGTH, MINOR_MARKER_LENGTH, isMajor, Color.LIGHT_GRAY, MARKER_WIDTH);
    }

    public void addText(String text) {
        Point center = renderer.getCenter();
        getRenderer().add(new TextRenderer(new Point(center.x, center.y + RADIUS / 3), text, FONT, Color.BLUE));
    }

    public void addNeedle(Measurement measurement, Paint paint) {
        addNeedle(measurement, paint, 0);
    }

    public void addCardinalNeedle(Measurement measurement, Paint paint, double defaultValue) {
        addNeedle(measurement, new CardinalArrowRenderer(), paint, defaultValue);
    }

    public void addNeedle(Measurement measurement, Paint paint, double defaultValue) {
        addNeedle(measurement, new ArrowRenderer(), paint, defaultValue);
    }

    @Override
    public void paint(Graphics graphics) {
        renderer.paint((Graphics2D) graphics);
    }

    private void addNeedle(Measurement measurement, ArrowRenderer arrowRenderer, Paint paint, double defaultValue) {
        needles.put(measurement, new Needle(renderer, arrowRenderer, paint, defaultValue));
    }

    public void update(WeatherStation station) {
        needles.forEach((measurement, needle) -> {
            Double value = measurement.getValue(station);
            if (value != null) {
                markers.setPaint(Color.BLUE);
                needle.applyPaint();
                updateTask.setValue(measurement, value);
            }
            else {
                markers.setPaint(NO_DATA_COLOR);
                needle.applyPaint(NO_DATA_COLOR);
                updateTask.removeValue(measurement);
            }
        });
    }

    public ClockRenderer getRenderer() {
        return renderer;
    }


    private class Needle {

        public Needle(ClockRenderer clockRenderer, ArrowRenderer arrowRenderer, Paint paint, double defaultValue) {
            this.arrowRenderer = arrowRenderer;
            needleRenderer = clockRenderer.addNeedleRenderer(arrowRenderer);
            needleRenderer.setValue(defaultValue);
            this.paint = paint;
        }

        public void setValue(double value) {
            needleRenderer.setValue(value);
        }

        public void addValue(double increment) {
            setValue(needleRenderer.getValue() + increment);
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

        private final NeedleRenderer needleRenderer;
        private final ArrowRenderer arrowRenderer;
        private final Paint paint;
    }


    private static class ArrowRenderer implements bka.awt.Renderer {

        @Override
        public void paint(Graphics2D graphics) {
            graphics.setPaint(paint);
            graphics.setStroke(stroke);
            graphics.fillOval(0 - RADIUS / 20, 0 - RADIUS / 20, RADIUS / 10, RADIUS / 10);
            graphics.drawLine(0, RADIUS / 10, 0, -(RADIUS / 10) * 7);
            graphics.drawLine(4, -(RADIUS / 10 * 6), 0, -(RADIUS / 10) * 7);
            graphics.drawLine(-4, -(RADIUS / 10 * 6), 0, -(RADIUS / 10) * 7);
        }

        public void setPaint(Paint paint) {
            this.paint = paint;
        }

        protected Stroke getStroke() {
            return stroke;
        }

        protected Paint getPaint() {
            return paint;
        }

        private Paint paint = NO_DATA_COLOR;
        private final Stroke stroke = new BasicStroke(RADIUS * 0.03f, BasicStroke.CAP_ROUND, BasicStroke.JOIN_MITER);
    }


    private static class CardinalArrowRenderer extends ArrowRenderer {

        @Override
        public void paint(Graphics2D graphics) {
            graphics.setPaint(getPaint());
            graphics.setStroke(getStroke());
            graphics.fillPolygon(
                new Polygon(
                    new int[]{ 0, RADIUS / -10, RADIUS / 10 },
                    new int[]{ RADIUS / 10, RADIUS / -10, RADIUS / -10 },
                    3)
            );
            graphics.drawLine(0, 0, 0, -(RADIUS / 10) * 7);
            graphics.drawLine(RADIUS / 20, -(RADIUS / 20) * 14, 0, -(RADIUS / 20) * 12);
            graphics.drawLine(RADIUS / -20, -(RADIUS / 20) * 14, 0, -(RADIUS / 20) * 12);
            graphics.drawLine(RADIUS / 20, -(RADIUS / 20) * 12, 0, -(RADIUS / 20) * 10);
            graphics.drawLine(RADIUS / -20, -(RADIUS / 20) * 12, 0, -(RADIUS / 20) * 10);
        }

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
            }
        }

        @Override
        public void run() {
            for (Map.Entry<Measurement, Needle> entry : needles.entrySet()) {
                Measurement measurement = entry.getKey();
                synchronized (targetValues) {
                    Double targetValue = targetValues.get(measurement);
                    if (targetValue != null) {
                        Needle needle = entry.getValue();
                        double step = Math.abs(needle.getScale().getMaxValue() - needle.getScale().getMinValue()) * INCREMENT_FRACTION;
                        double distance = targetValue - needle.needleRenderer.getValue();
                        if (Math.abs(distance) > step) {
                            needle.addValue((distance < 0) ? -step : step);
                        }
                        else {
                            needle.setValue(targetValues.get(measurement));
                            targetValues.remove(entry.getKey());
                        }
                    }
                }
            }
            repaint();
        }

        private final Map<Measurement, Double> targetValues = new HashMap<>();

    }


    private final UpdateTask updateTask = new UpdateTask();
    private final LinkedHashMap<Measurement, Needle> needles = new LinkedHashMap<>();

    private final ClockRenderer renderer;
    private final FormattedValueRenderer markers;

    private static final Font FONT = new Font(Font.DIALOG, Font.PLAIN, 12);
    private static final Color NO_DATA_COLOR = new Color(0xE1E1E1);
    private static final int RADIUS = 100;
    private static final double NUMBER_MARKER_RADIUS = 0.85 * RADIUS;
    private static final double FINE_MARKER_RADIUS = 0.79 * RADIUS;
    private static final float MARKER_WIDTH = RADIUS / 100f;
    private static final int MINOR_MARKER_LENGTH = (int) (RADIUS * 0.07);
    private static final int MAJOR_MARKER_LENGTH = (int) (RADIUS * 0.1);

    private static final long UPDATE_INTERVAL = 100;
    private static final double INCREMENT_FRACTION = 0.01;

}
