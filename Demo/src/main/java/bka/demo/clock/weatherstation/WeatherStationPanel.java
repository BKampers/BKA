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
        addTemperatureArcs(thermometer);
        thermometer.addFineMarkers(1d, value -> Math.round(value) % 5 == 0);
        thermometer.addNeedle(Measurement.CHILL, Color.BLUE);
        thermometer.addNeedle(Measurement.TEMPERATURE, Color.RED);
        return thermometer;
    }

    private static void addTemperatureArcs(ClockPanel thermometer) {
        for (int t = MIN_TEMPERATURE; t < MAX_TEMPERATURE; t += 10) {
            thermometer.addArc(t, t + 9, (t < 0) ? coldColor(t) : warmColor(t));
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
        hygrometer.addNeedle(Measurement.HUMIDITY, Color.GREEN.darker());
        return hygrometer;
    }

    private ClockPanel createWindrose() {
        ClockPanel windRose = new ClockPanel(new Scale(0, 360), 45, new CardinalNumberFormat());
        addCardinalArcs(windRose);
        for (double angle = 22.5; angle < 360.0; angle += 45.0) {
            windRose.addTiltedMarker(angle, CROSS_MARKER);
        }
        windRose.addFineMarkers(5d, value -> Math.round(value) % 15 == 0);
        windRose.addCardinalNeedle(Measurement.WIND_DIRECTION, Color.BLACK);
        return windRose;
    }

    private static void addCardinalArcs(ClockPanel windRose) {
        for (int a = 0; a < 16; ++a) {
            windRose.addArc(a * 22.5 - 9.5, a * 22.5 + 9.5, (a % 2 == 0) ? ARC_COLOR_EVEN : ARC_COLOR_ODD);
        }
    }

    private ClockPanel createAnonemeter() {
        ClockPanel anonemeter = new ClockPanel(new Scale(0, 40, MIN_ANGLE, MAX_ANGLE), 5);
        anonemeter.addText("m/s");
        anonemeter.addFineMarkers(1d, value -> Math.round(value) % 5 == 0);
        addBeaufortArcs(anonemeter);
        anonemeter.addNeedle(Measurement.SQUALL, Color.RED);
        anonemeter.addNeedle(Measurement.WIND_SPEED, Color.BLACK);
        return anonemeter;
    }

    private static void addBeaufortArcs(ClockPanel anonemeter) {
        Font font = new Font(Font.SERIF, Font.ROMAN_BASELINE, 7);
        double[] beaufortWindSpeeds = { 0.3, 1.6, 3.4, 5.5, 8.0, 10.8, 13.9, 17.2, 20.8, 24.5, 28.5, 32.7, 40.0 };
        double capMargin = 0.2;
        int ovalSize = 5;
        for (int i = 1; i < beaufortWindSpeeds.length; ++i) {
            Color color = beaufortColor(i);
            double startValue = beaufortWindSpeeds[i - 1];
            double endValue = beaufortWindSpeeds[i];
            anonemeter.addArc(startValue + capMargin, endValue - capMargin, color);
            anonemeter.addArcMarker(startValue + (endValue - startValue) / 2d, ovalMarkerRenderer(Integer.toString(i), font, Color.WHITE, ovalSize, color));
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
        addBarometerSymbols(barometer);
        barometer.addNeedle(Measurement.PRESSURE, Color.BLUE, MIN_PRESSURE);
        return barometer;
    }

    private static void addBarometerSymbols(ClockPanel barometer) {
        for (Symbol symbol : BAROMETER_SYMBOLS) {
            try {
                barometer.addMarker(symbol.getValue(), new ImageRenderer(ImageFactory.loadImage(symbol.getImageFilename(), BAROMETER_SYMBOL_SIZE, BAROMETER_SYMBOL_SIZE)));
            }
            catch (IOException ex) {
                Logger.getLogger(WeatherStationPanel.class.getName()).log(Level.WARNING, "Could not load " + symbol.getImageFilename(), ex);
                barometer.addTiltedMarker(symbol.getValue(), graphics -> {
                    TextRenderer.centerText(graphics, symbol.getFallbackText());
                });
            }
        }
    }

    private ClockPanel createViewmeter() {
        ClockPanel viewmeter = new ClockPanel(new Scale(0, 60, MIN_ANGLE, MAX_ANGLE), 5);
        viewmeter.addFineMarkers(1d, value -> Math.round(value) % 5 == 0);
        viewmeter.addText("km");
        viewmeter.addNeedle(Measurement.VISIBILITY, Color.ORANGE);
        return viewmeter;
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
    private static final int BAROMETER_SYMBOL_SIZE = 15;

    private static final bka.awt.Renderer CROSS_MARKER = graphics -> {
        graphics.drawLine(0, 2, 0, -2);
        graphics.drawLine(-2, 0, 2, 0);
    };

    private static final Color ARC_COLOR_EVEN = new Color(0x4A41AF);
    private static final Color ARC_COLOR_ODD = new Color(0x918AE5);

    private static final double MIN_ANGLE = -0.4125;
    private static final double MAX_ANGLE = 0.4125;

    private static final int MIN_TEMPERATURE = -50;
    private static final int MAX_TEMPERATURE = 50;
    private static final double MIN_PRESSURE = 940;
    private static final double MAX_PRESSURE = 1040;
}
