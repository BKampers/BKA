package bka.demo.clock.weatherstation;

/*
** © Bart Kampers
*/

import bka.awt.clock.*;
import bka.demo.clock.*;
import java.awt.*;
import java.io.*;
import java.util.*;
import java.util.function.*;
import java.util.logging.*;
import javax.swing.*;


public class WeatherStationPanel extends JPanel {

    public WeatherStationPanel() {
        clocks.add(createThermometer());
        clocks.add(createHygrometer());
        clocks.add(createWindrose());
        clocks.add(createAnonemeter());
        clocks.add(createBarometer());
        clocks.add(createViewmeter());
    }

    private static ClockControl createThermometer() {
        ClockControl thermometer = new ClockControl(THERMOMETER_CENTER, new Scale(MIN_TEMPERATURE, MAX_TEMPERATURE, MIN_ANGLE, MAX_ANGLE), 10);
        thermometer.addText("\u2103");
        addTemperatureArcs(thermometer.getRenderer());
        thermometer.addFineMarkers(1d, value -> Math.round(value) % 5 == 0);
        thermometer.addNeedle(Measurement.CHILL, Color.BLUE);
        thermometer.addNeedle(Measurement.TEMPERATURE, Color.RED);
        return thermometer;
    }

    private static void addTemperatureArcs(ClockRenderer thermometer) {
        for (int t = MIN_TEMPERATURE; t < MAX_TEMPERATURE; t += 10) {
            thermometer.addArc(ARC_RADIUS, t, t + 9, (t < 0) ? coldColor(t) : warmColor(t), ARC_WIDTH);
        }
    }

    private static Color coldColor(int temperature) {
        return new Color(0, 0, temperature * -4 + 55).brighter();
    }

    private static Color warmColor(int temperature) {
        return new Color(temperature * 4 + 95, 0, 0).brighter();
    }

    private static ClockControl createHygrometer() {
        ClockControl hygrometer = new ClockControl(HYGROMETER_CENTER, new Scale(0, 100, MIN_ANGLE, MAX_ANGLE), 10);
        hygrometer.addText("RH %");
        hygrometer.addFineMarkers(1d, value -> Math.round(value) % 5 == 0);
        hygrometer.addNeedle(Measurement.HUMIDITY, Color.GREEN.darker());
        return hygrometer;
    }

    private static ClockControl createWindrose() {
        ClockControl windRose = new ClockControl(WINDROSE_CENTER, new Scale(0, 360), 45, new FormattedValueRenderer(NO_DATA_COLOR, FONT, CARDINAL_FORMAT));
        addCardinalArcs(windRose.getRenderer());
        for (double angle = 22.5; angle < 360.0; angle += 45.0) {
            windRose.getRenderer().addTiltedMarkerRenderer(NUMBER_MARKER_RADIUS, angle, crossMarker());
        }
        windRose.addFineMarkers(5d, value -> Math.round(value) % 15 == 0);
        windRose.addNeedle(Measurement.WIND_DIRECTION, new CardinalArrowRenderer(), Color.BLACK, 180);
        return windRose;
    }

    private static void addCardinalArcs(ClockRenderer windRose) {
        for (int a = 0; a < 16; ++a) {
            windRose.addArc(ARC_RADIUS, a * 22.5 - 9.5, a * 22.5 + 9.5, (a % 2 == 0) ? ARC_COLOR_EVEN : ARC_COLOR_ODD, ARC_WIDTH);
        }
    }

    private static ClockControl createAnonemeter() {
        ClockControl anonemeter = new ClockControl(ANONEMETER_CENTER, new Scale(0, 40, MIN_ANGLE, MAX_ANGLE), 5);
        anonemeter.addText("m/s");
        anonemeter.addFineMarkers(1d, value -> Math.round(value) % 5 == 0);
        addBeaufortArcs(anonemeter.getRenderer());
        anonemeter.addNeedle(Measurement.SQUALL, Color.RED);
        anonemeter.addNeedle(Measurement.WIND_SPEED, Color.BLACK);
        return anonemeter;
    }

    private static void addBeaufortArcs(ClockRenderer anonemeter) {
        final Font font = new Font(Font.SERIF, Font.ROMAN_BASELINE, 7);
        final double[] beaufortWindSpeeds = { 0.3, 1.6, 3.4, 5.5, 8.0, 10.8, 13.9, 17.2, 20.8, 24.5, 28.5, 32.7, 40.0 };
        final double capMargin = 0.2;
        final int ovalSize = 5;
        for (int i = 1; i < beaufortWindSpeeds.length; ++i) {
            final Color color = beaufortColor(i);
            final double startValue = beaufortWindSpeeds[i - 1];
            final double endValue = beaufortWindSpeeds[i];
            anonemeter.addArc(ARC_RADIUS, startValue + capMargin, endValue - capMargin, color, ARC_WIDTH);
            anonemeter.addTiltedMarkerRenderer(ARC_RADIUS, startValue + (endValue - startValue) / 2d, ovalMarkerRenderer(Integer.toString(i), font, Color.WHITE, ovalSize, color));
        }
    }

    private static Color beaufortColor(int index) {
        return new Color(255 - (12 - index) * 20, (12 - index) * 15, 0);
    }

    private static bka.awt.Renderer ovalMarkerRenderer(String text, Font font, Paint foreground, int size, Paint background) {
        return graphics -> {
            graphics.setPaint(background);
            graphics.fillOval(-size, -size, size * 2, size * 2);
            graphics.setRenderingHint(RenderingHints.KEY_TEXT_ANTIALIASING, RenderingHints.VALUE_TEXT_ANTIALIAS_ON);
            TextRenderer textRenderer = new TextRenderer(new Point(0, 0), text, font, foreground);
            textRenderer.paint(graphics);
        };
    }

    private static ClockControl createBarometer() {
        ClockControl barometer = new ClockControl(BAROMETER_CENTER, new Scale(MIN_PRESSURE, MAX_PRESSURE, MIN_ANGLE, MAX_ANGLE), 20);
        barometer.addText("hPa");
        barometer.addFineMarkers(1d, value -> Math.round(value) % 10 == 0);
        addBarometerSymbols(barometer.getRenderer());
        barometer.addNeedle(Measurement.PRESSURE, Color.BLUE, MIN_PRESSURE);
        return barometer;
    }

    private static void addBarometerSymbols(ClockRenderer barometer) {
        for (Symbol symbol : BAROMETER_SYMBOLS) {
            try {
                barometer.addMarkerRenderer(NUMBER_MARKER_RADIUS, symbol.getValue(), new ImageRenderer(ImageFactory.loadImage(symbol.getImageFilename(), 15, 15)));
            }
            catch (IOException ex) {
                Logger.getLogger(WeatherStationPanel.class.getName()).log(Level.WARNING, "Could not load " + symbol.getImageFilename(), ex);
                barometer.addTiltedMarkerRenderer(NUMBER_MARKER_RADIUS, symbol.getValue(), graphics -> {
                    TextRenderer.centerText(graphics, symbol.getFallbackText());
                });
            }
        }
    }

    private static ClockControl createViewmeter() {
        ClockControl viewmeter = new ClockControl(VIEWMETER_CENTER, new Scale(0, 60, MIN_ANGLE, MAX_ANGLE), 5);
        viewmeter.addFineMarkers(1d, value -> Math.round(value) % 5 == 0);
        viewmeter.addText("km");
        viewmeter.addNeedle(Measurement.VISIBILITY, Color.ORANGE);
        return viewmeter;
    }

    private static bka.awt.Renderer crossMarker() {
        return graphics -> {
            graphics.setPaint(Color.LIGHT_GRAY);
            graphics.setStroke(new BasicStroke(MARKER_WIDTH));
            graphics.drawLine(0, 2, 0, -2);
            graphics.drawLine(-2, 0, 2, 0);
        };
    }

    public void setStation(WeatherStation station) {
        if (station != null) {
            clocks.forEach(clock -> clock.update(station));
            Logger.getLogger(WeatherStationPanel.class.getName()).log(
                Level.FINE,
                "{0}: {1}; {2}; {3}; {4}; {5}; {6}; {7}; {8}",
                new Object[]{
                    station.getStationName(),
                    station.getTemperature(),
                    station.getChill(),
                    station.getHumidity(),
                    station.getWindDirection(),
                    station.getWindSpeed(),
                    station.getSquall(),
                    station.getPressure(),
                    station.getVisibility()
                }
            );
        }
        repaint();
    }

    @Override
    public void paint(Graphics graphics) {
        clocks.forEach(clock -> clock.getRenderer().paint((Graphics2D) graphics));
    }


    private static class ClockControl {

        public ClockControl(Point center, Scale scale, int markerInterval) {
            this(center, scale, markerInterval, new FormattedValueRenderer(NO_DATA_COLOR, FONT));
        }

        public ClockControl(Point center, Scale scale, int markerInterval, FormattedValueRenderer markers) {
            renderer = new ClockRenderer(center, scale);
            this.markers = markers;
            renderer.addClockFace(RADIUS, Color.WHITE);
            renderer.addMarkerRingRenderer(NUMBER_MARKER_RADIUS, markerInterval, markers);
        }

        public void addFineMarkers(double interval, Predicate<Double> isMajor) {
            renderer.addMarkerRingRenderer(FINE_MARKER_RADIUS, interval, MAJOR_MARKER_LENGTH, MINOR_MARKER_LENGTH, isMajor, Color.LIGHT_GRAY, MARKER_WIDTH);
        }

        public void addText(String text) {
            Point center = renderer.getCenter();
            renderer.add(new TextRenderer(new Point(center.x, center.y + RADIUS / 3), text, FONT, Color.BLUE));
        }

        public void addNeedle(Measurement measurement, Paint paint) {
            addNeedle(measurement, paint, 0);
        }

        public void addNeedle(Measurement measurement, Paint paint, double defaultValue) {
            addNeedle(measurement, new ArrowRenderer(), paint, defaultValue);
        }

        public void addNeedle(Measurement measurement, ArrowRenderer arrowRenderer, Paint paint, double defaultValue) {
            needles.put(measurement, new Needle(renderer, arrowRenderer, paint, defaultValue));
        }

        public void update(WeatherStation station) {
            needles.forEach((measurement, needle) -> {
                Double value = measurement.getValue(station);
                markers.setPaint((value != null) ? Color.BLUE : NO_DATA_COLOR);
                needle.setValue(value);
            });
        }

        public ClockRenderer getRenderer() {
            return renderer;
        }
        
        private final ClockRenderer renderer;
        private final FormattedValueRenderer markers;
        private final LinkedHashMap<Measurement, Needle> needles = new LinkedHashMap<>();

    }


    private static class Needle {

        public Needle(ClockRenderer clockRenderer, ArrowRenderer arrowRenderer, Paint paint, double defaultValue) {
            this.arrowRenderer = arrowRenderer;
            needleRenderer = clockRenderer.addNeedleRenderer(arrowRenderer);
            needleRenderer.setValue(defaultValue);
            this.paint = paint;
            this.defaultValue = defaultValue;
        }

        public void setValue(Double value) {
            if (value != null) {
                needleRenderer.setValue(value);
                arrowRenderer.setPaint(paint);
            }
            else {
                needleRenderer.setValue(defaultValue);
                arrowRenderer.setPaint(NO_DATA_COLOR);
            }
        }

        private final NeedleRenderer needleRenderer;
        private final ArrowRenderer arrowRenderer;
        private final Paint paint;
        private final double defaultValue;
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


    private static class Symbol {

        public Symbol(double value, String imageFilename, String fallbackText) {
            this.value = value;
            this.imageFilename = imageFilename;
            this.fallbackText = fallbackText;
        }

        public double getValue() {
            return value;
        }

        public String getImageFilename() {
            return imageFilename;
        }

        public String getFallbackText() {
            return fallbackText;
        }

        private final double value;
        private final String imageFilename;
        private final String fallbackText;
    }


    private enum Measurement {
        TEMPERATURE(station -> station.getTemperature()),
        CHILL(station -> computeValue(station.getChill(), value -> value, () -> station.getTemperature())),
        HUMIDITY(station -> station.getHumidity()),
        WIND_DIRECTION(station -> station.getWindDirection()),
        WIND_SPEED(station -> station.getWindSpeed()),
        SQUALL(station -> computeValue(station.getSquall(), value -> value / 3.6, () -> station.getWindSpeed())),
        PRESSURE(station -> station.getPressure()),
        VISIBILITY(station -> computeValue(station.getVisibility(), value -> value / 1000, () -> null));

        private Measurement(Function<WeatherStation, Double> valueFunction) {
            this.valueFunction = valueFunction;
        }

        public Double getValue(WeatherStation station) {
            return valueFunction.apply(station);
        }

        private static Double computeValue(Double value, Function<Double, Double> processor, Supplier<Double> alternative) {
            if (value == null) {
                return alternative.get();
            }
            return processor.apply(value);
        }

        private final Function<WeatherStation, Double> valueFunction;
    }


    private final Collection<ClockControl> clocks = new ArrayList<>();

    private static final Symbol[] BAROMETER_SYMBOLS = {
        new Symbol(950d, "Resources/raincloud.png", "Rain"),
        new Symbol(990d, "Resources/sun cloud.png", "Change"),
        new Symbol(1030d, "Resources/sun.png", "Fair")
    };

    private static final Font FONT = new Font(Font.DIALOG, Font.PLAIN, 12);

    private static final Color NO_DATA_COLOR = new Color(0xE1E1E1);
    private static final Color ARC_COLOR_EVEN = new Color(0x4A41AF);
    private static final Color ARC_COLOR_ODD = new Color(0x918AE5);

    private static final double MIN_ANGLE = -0.4125;
    private static final double MAX_ANGLE = 0.4125;

    private static final int RADIUS = 100;
    private static final double NUMBER_MARKER_RADIUS = 0.85 * RADIUS;
    private static final double FINE_MARKER_RADIUS = 0.79 * RADIUS;
    private static final double ARC_RADIUS = 0.95 * RADIUS;
    private static final float ARC_WIDTH = (float) RADIUS / 20f;
    private static final float MARKER_WIDTH = RADIUS / 100f;
    private static final int MINOR_MARKER_LENGTH = (int) (RADIUS * 0.07);
    private static final int MAJOR_MARKER_LENGTH = (int) (RADIUS * 0.1);

    private static final Point THERMOMETER_CENTER = new Point(RADIUS, RADIUS);
    private static final Point HYGROMETER_CENTER = new Point(3 * RADIUS, RADIUS);
    private static final Point WINDROSE_CENTER = new Point(5 * RADIUS, RADIUS);
    private static final Point BAROMETER_CENTER = new Point(9 * RADIUS, RADIUS);
    private static final Point ANONEMETER_CENTER = new Point(7 * RADIUS, RADIUS);
    private static final Point VIEWMETER_CENTER = new Point(11 * RADIUS, RADIUS);

    private static final CardinalNumberFormat CARDINAL_FORMAT = new CardinalNumberFormat();

    private static final int MIN_TEMPERATURE = -50;
    private static final int MAX_TEMPERATURE = 50;
    private static final double MIN_PRESSURE = 940;
    private static final double MAX_PRESSURE = 1040;
}
