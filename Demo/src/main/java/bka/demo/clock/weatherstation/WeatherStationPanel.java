package bka.demo.clock.weatherstation;

/*
** © Bart Kampers
*/

import bka.awt.clock.*;
import bka.demo.clock.*;
import java.awt.*;
import java.io.*;
import java.util.*;
import java.util.logging.*;
import javax.swing.*;


public class WeatherStationPanel extends JPanel {

    public WeatherStationPanel() {
        super();
        initComponents();
    }

    private void initComponents() {
        setLayout(null);
        int x = 0;
        for (ClockPanel panel : clocks) {
            Dimension size = panel.getSize();
            panel.setBounds(x, 0, size.width, size.height);
            x += size.width;
            add(panel);
        }
    }

    private ClockPanel createThermometer() {
        ClockPanel thermometer = new ClockPanel(new Scale(MIN_TEMPERATURE, MAX_TEMPERATURE, MIN_ANGLE, MAX_ANGLE), 10);
        thermometer.addText("\u2103");
        addTemperatureArcs(thermometer.getRenderer());
        thermometer.addFineMarkers(1d, value -> Math.round(value) % 5 == 0);
        thermometer.addNeedle(ClockPanel.Measurement.CHILL, Color.BLUE);
        thermometer.addNeedle(ClockPanel.Measurement.TEMPERATURE, Color.RED);
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

    private ClockPanel createHygrometer() {
        ClockPanel hygrometer = new ClockPanel(new Scale(0, 100, MIN_ANGLE, MAX_ANGLE), 10);
        hygrometer.addText("RH %");
        hygrometer.addFineMarkers(1d, value -> Math.round(value) % 5 == 0);
        hygrometer.addNeedle(ClockPanel.Measurement.HUMIDITY, Color.GREEN.darker());
        return hygrometer;
    }

    private ClockPanel createWindrose() {
        ClockPanel windRose = new ClockPanel(new Scale(0, 360), 45, new FormattedValueRenderer(NO_DATA_COLOR, FONT, CARDINAL_FORMAT));
        addCardinalArcs(windRose.getRenderer());
        for (double angle = 22.5; angle < 360.0; angle += 45.0) {
            windRose.getRenderer().addTiltedMarkerRenderer(NUMBER_MARKER_RADIUS, angle, crossMarker());
        }
        windRose.addFineMarkers(5d, value -> Math.round(value) % 15 == 0);
        windRose.addCardinalNeedle(ClockPanel.Measurement.WIND_DIRECTION, Color.BLACK, 180);
        return windRose;
    }

    private static void addCardinalArcs(ClockRenderer windRose) {
        for (int a = 0; a < 16; ++a) {
            windRose.addArc(ARC_RADIUS, a * 22.5 - 9.5, a * 22.5 + 9.5, (a % 2 == 0) ? ARC_COLOR_EVEN : ARC_COLOR_ODD, ARC_WIDTH);
        }
    }

    private ClockPanel createAnonemeter() {
        ClockPanel anonemeter = new ClockPanel(new Scale(0, 40, MIN_ANGLE, MAX_ANGLE), 5);
        anonemeter.addText("m/s");
        anonemeter.addFineMarkers(1d, value -> Math.round(value) % 5 == 0);
        addBeaufortArcs(anonemeter.getRenderer());
        anonemeter.addNeedle(ClockPanel.Measurement.SQUALL, Color.RED);
        anonemeter.addNeedle(ClockPanel.Measurement.WIND_SPEED, Color.BLACK);
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

    private ClockPanel createBarometer() {
        ClockPanel barometer = new ClockPanel(new Scale(MIN_PRESSURE, MAX_PRESSURE, MIN_ANGLE, MAX_ANGLE), 20);
        barometer.addText("hPa");
        barometer.addFineMarkers(1d, value -> Math.round(value) % 10 == 0);
        addBarometerSymbols(barometer.getRenderer());
        barometer.addNeedle(ClockPanel.Measurement.PRESSURE, Color.BLUE, MIN_PRESSURE);
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

    private ClockPanel createViewmeter() {
        ClockPanel viewmeter = new ClockPanel(new Scale(0, 60, MIN_ANGLE, MAX_ANGLE), 5);
        viewmeter.addFineMarkers(1d, value -> Math.round(value) % 5 == 0);
        viewmeter.addText("km");
        viewmeter.addNeedle(ClockPanel.Measurement.VISIBILITY, Color.ORANGE);
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


    private final Collection<ClockPanel> clocks = java.util.List.of(
        createThermometer(),
        createHygrometer(),
        createWindrose(),
        createAnonemeter(),
        createBarometer(),
        createViewmeter());

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
//    private static final double FINE_MARKER_RADIUS = 0.79 * RADIUS;
    private static final double ARC_RADIUS = 0.95 * RADIUS;
    private static final float ARC_WIDTH = (float) RADIUS / 20f;
    private static final float MARKER_WIDTH = RADIUS / 100f;

    private static final CardinalNumberFormat CARDINAL_FORMAT = new CardinalNumberFormat();

    private static final int MIN_TEMPERATURE = -50;
    private static final int MAX_TEMPERATURE = 50;
    private static final double MIN_PRESSURE = 940;
    private static final double MAX_PRESSURE = 1040;
}
