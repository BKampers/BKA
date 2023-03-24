package bka.demo.clock.weatherstation;

/*
** © Bart Kampers
*/



import bka.awt.clock.*;
import java.awt.*;
import java.awt.geom.*;
import java.util.logging.*;
import javax.swing.*;


public class WeatherStationPanel extends JPanel {

    public WeatherStationPanel() {
        thermometer.addClockFace(RADIUS, Color.WHITE);
        thermometer.add(new TextRenderer(new Point(RADIUS, RADIUS + RADIUS / 3), "\u2103", Color.BLUE));
        addTemperatureArcs();
        temperatureMarkers = new FormattedValueRenderer(NO_DATA_COLOR);
        thermometer.addMarkerRingRenderer(MARKER_RADIUS, 10, temperatureMarkers);
        chillNeedle = thermometer.addNeedleRenderer(chillArrow);
        temperatureNeedle = thermometer.addNeedleRenderer(temperatureArrow);
        hygrometer.addClockFace(RADIUS, Color.WHITE);
        hygrometer.add(new TextRenderer(new Point(RADIUS * 3, RADIUS + RADIUS / 3), "RH %", Color.BLUE));
        humidityMarkers = new FormattedValueRenderer(NO_DATA_COLOR);
        hygrometer.addMarkerRingRenderer(MARKER_RADIUS, 10, humidityMarkers);
        humidityNeedle = hygrometer.addNeedleRenderer(humidityArrow);
        windRose.addClockFace(RADIUS, Color.WHITE);
        cardinalMarkers = new FormattedValueRenderer(NO_DATA_COLOR, null, cardinalFormat);
        addCardinalArcs();
        windRose.addMarkerRingRenderer(MARKER_RADIUS, 45, cardinalMarkers);
        windDirectionNeedle = windRose.addNeedleRenderer(windDirectionArrow);
        windSpeedMarkers = new FormattedValueRenderer(NO_DATA_COLOR);
        anonemeter.addClockFace(RADIUS, Color.WHITE);
        anonemeter.add(new TextRenderer(new Point(RADIUS * 7, RADIUS + RADIUS / 3), "m/s", Color.BLUE));
        anonemeter.addMarkerRingRenderer(MARKER_RADIUS, 5, windSpeedMarkers);
        addBeaufortArcs();
        squallNeedle = anonemeter.addNeedleRenderer(squallArrow);
        windSpeedNeedle = anonemeter.addNeedleRenderer(windSpeedArrow);
        barometer.addClockFace(RADIUS, Color.WHITE);
        barometer.add(new TextRenderer(new Point(RADIUS * 9, RADIUS + RADIUS / 3), "hPa", Color.BLUE));
        airPressureMarkers = new FormattedValueRenderer(NO_DATA_COLOR);
        barometer.addMarkerRingRenderer(MARKER_RADIUS, 20, airPressureMarkers);
        airPressureNeedle = barometer.addNeedleRenderer(airPressureArrow);
        viewmeter.addClockFace(RADIUS, Color.WHITE);
        viewmeter.add(new TextRenderer(new Point(RADIUS * 11, RADIUS + RADIUS / 3), "km", Color.BLUE));
        visibilityMarkers = new FormattedValueRenderer(NO_DATA_COLOR);
        viewmeter.addMarkerRingRenderer(MARKER_RADIUS, 5, visibilityMarkers);
        visibilityNeedle = viewmeter.addNeedleRenderer(visibilityArrow);
    }

    private void addTemperatureArcs() {
        for (int t = -50; t < 50; t += 10) {
            thermometer.addArc(ARC_RADIUS, t, t + 9, (t < 0) ? coldColor(t) : warmColor(t), ARC_WIDTH);
        }
    }

    private static Color coldColor(int temperature) {
        return new Color(0, 0, temperature * -4 + 55).brighter();
    }

    private static Color warmColor(int temperature) {
        return new Color(temperature * 4 + 95, 0, 0).brighter();
    }

    private void addCardinalArcs() {
        for (int a = 0; a < 16; ++a) {
            windRose.addArc(ARC_RADIUS, a * 22.5 - 9.5, a * 22.5 + 9.5, (a % 2 == 0) ? ARC_COLOR_EVEN : ARC_COLOR_ODD, ARC_WIDTH);
        }
    }

    private void addBeaufortArcs() {
        final double[] beaufortWindSpeeds = { 0.3, 1.6, 3.4, 5.5, 8.0, 10.8, 13.9, 17.2, 20.8, 24.5, 28.5, 32.7, 40.0 };
        for (int i = 1; i < beaufortWindSpeeds.length; ++i) {
            bka.awt.Renderer renderer = beaufortArcRenderer(Integer.toString(i), beaufortWindSpeeds[i - 1], beaufortWindSpeeds[i], beaufortColor(i));
            anonemeter.add(renderer);
        }
    }

    private static Color beaufortColor(int index) {
        return new Color(255 - (12 - index) * 20, (12 - index) * 15, 0);
    }

    private bka.awt.Renderer beaufortArcRenderer(final String index, final double arcStart, final double arcEnd, final Color color) {
        final double cap = 0.2;
        return graphics -> {
            graphics.setPaint(color);
            graphics.setStroke(new BasicStroke(ARC_WIDTH, BasicStroke.CAP_ROUND, BasicStroke.JOIN_MITER));
            final Shape arc = anonemeter.createArc(ARC_RADIUS, arcStart + cap, arcEnd - cap);
            graphics.draw(arc);
            double value = arcStart + (arcEnd - arcStart) / 2d;
            Point2D point = anonemeter.markerPoint(value, ARC_RADIUS);
            graphics.translate(point.getX(), point.getY());
            final int SIZE = 5;
            graphics.fillOval(-SIZE, -SIZE, SIZE * 2, SIZE * 2);
            TextRenderer textRenderer = new TextRenderer(new Point(0, 0), index, new Font(Font.SERIF, Font.ROMAN_BASELINE, 7), Color.WHITE);
            graphics.rotate(anonemeter.radians(value));
            Font savedFont = graphics.getFont();
            textRenderer.paint(graphics);
            graphics.setFont(savedFont);
            graphics.rotate(-anonemeter.radians(value));
            graphics.translate(-point.getX(), -point.getY());
        };
    }

    public void setStation(WeatherStation station) {
        if (station != null) {
            chillNeedle.setValue((station.getChill() != null) ? station.getChill() : (station.getTemperature() != null) ? station.getTemperature() : 0);
            chillArrow.setPaint((station.getChill() != null) ? Color.BLUE : NO_DATA_COLOR);
            temperatureNeedle.setValue((station.getTemperature() != null) ? station.getTemperature() : 0);
            temperatureArrow.setPaint((station.getTemperature() != null) ? Color.RED : NO_DATA_COLOR);
            temperatureMarkers.setPaint((station.getTemperature() != null) ? Color.BLUE : NO_DATA_COLOR);
            humidityNeedle.setValue((station.getHumidity() != null) ? station.getHumidity() : 0);
            humidityArrow.setPaint((station.getHumidity() != null) ? Color.GREEN.darker() : NO_DATA_COLOR);
            humidityMarkers.setPaint((station.getHumidity() != null) ? Color.BLUE : NO_DATA_COLOR);
            windDirectionNeedle.setValue((station.getWindDirection() != null) ? station.getWindDirection() : 0);
            windDirectionArrow.setPaint((station.getWindDirection() != null) ? Color.BLACK : NO_DATA_COLOR);
            cardinalMarkers.setPaint((station.getWindDirection() != null) ? Color.BLUE : NO_DATA_COLOR);
            squallNeedle.setValue((station.getSquall() != null) ? station.getSquall() * (1000d / 3600d) : 0);
            squallArrow.setPaint((station.getSquall() != null) ? Color.RED : NO_DATA_COLOR);
            windSpeedNeedle.setValue((station.getWindSpeed() != null) ? station.getWindSpeed() : 0);
            windSpeedArrow.setPaint((station.getWindSpeed() != null) ? Color.BLACK : NO_DATA_COLOR);
            windSpeedMarkers.setPaint(((station.getWindSpeed() != null) || (station.getSquall()) != null) ? Color.BLUE : NO_DATA_COLOR);
            airPressureNeedle.setValue((station.getPressure() != null) ? station.getPressure() : 0);
            airPressureArrow.setPaint((station.getPressure() != null) ? Color.BLUE : NO_DATA_COLOR);
            airPressureMarkers.setPaint(((station.getPressure()) != null) ? Color.BLUE : NO_DATA_COLOR);
            visibilityNeedle.setValue((station.getVisibility() != null) ? station.getVisibility() / 1000 : 0);
            visibilityArrow.setPaint((station.getVisibility() != null) ? Color.ORANGE : NO_DATA_COLOR);
            visibilityMarkers.setPaint(((station.getVisibility()) != null) ? Color.BLUE : NO_DATA_COLOR);
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
        thermometer.paint((Graphics2D) graphics);
        hygrometer.paint((Graphics2D) graphics);
        windRose.paint((Graphics2D) graphics);
        anonemeter.paint((Graphics2D) graphics);
        barometer.paint((Graphics2D) graphics);
        viewmeter.paint((Graphics2D) graphics);
    }

    private class ArrowRenderer implements bka.awt.Renderer {

        @Override
        public void paint(Graphics2D graphics) {
            graphics.setPaint(paint);
            graphics.setStroke(new BasicStroke(3f, BasicStroke.CAP_ROUND, BasicStroke.JOIN_MITER));
            graphics.fillOval(0 - RADIUS / 20, 0 - RADIUS / 20, RADIUS / 10, RADIUS / 10);
            graphics.drawLine(0, RADIUS / 10, 0, -(RADIUS / 10) * 8);
            graphics.drawLine(5, -(RADIUS / 10 * 7), 0, -(RADIUS / 10) * 8);
            graphics.drawLine(-5, -(RADIUS / 10 * 7), 0, -(RADIUS / 10) * 8);
        }

        public void setPaint(Paint paint) {
            this.paint = paint;
        }

        protected Paint getPaint() {
            return paint;
        }

        private Paint paint = NO_DATA_COLOR;
    }

    private class CardinalArrowRenderer extends ArrowRenderer {

        @Override
        public void paint(Graphics2D graphics) {
            graphics.setPaint(getPaint());
            graphics.setStroke(new BasicStroke(3f, BasicStroke.CAP_ROUND, BasicStroke.JOIN_MITER));
            graphics.fillPolygon(
                new Polygon(
                    new int[]{ 0, RADIUS / -10, RADIUS / 10 },
                    new int[]{ RADIUS / 10, RADIUS / -10, RADIUS / -10 },
                    3)
            );
            graphics.drawLine(0, 0, 0, -(RADIUS / 10) * 8);
            graphics.drawLine(RADIUS / 20, -(RADIUS / 20) * 16, 0, -(RADIUS / 20) * 14);
            graphics.drawLine(RADIUS / -20, -(RADIUS / 20) * 16, 0, -(RADIUS / 20) * 14);
            graphics.drawLine(RADIUS / 20, -(RADIUS / 20) * 14, 0, -(RADIUS / 20) * 12);
            graphics.drawLine(RADIUS / -20, -(RADIUS / 20) * 14, 0, -(RADIUS / 20) * 12);
        }

    }

    private final ClockRenderer thermometer = new ClockRenderer(new Point(RADIUS, RADIUS), new Scale(-50, 50, MIN_ANGLE, MAX_ANGLE));
    private final ArrowRenderer temperatureArrow = new ArrowRenderer();
    private final NeedleRenderer temperatureNeedle;
    private final ArrowRenderer chillArrow = new ArrowRenderer();
    private final NeedleRenderer chillNeedle;
    private final FormattedValueRenderer temperatureMarkers;
    private final ClockRenderer hygrometer = new ClockRenderer(new Point(3 * RADIUS, RADIUS), new Scale(0, 100, MIN_ANGLE, MAX_ANGLE));
    private final ArrowRenderer humidityArrow = new ArrowRenderer();
    private final NeedleRenderer humidityNeedle;
    private final FormattedValueRenderer humidityMarkers;
    private final ClockRenderer windRose = new ClockRenderer(new Point(5 * RADIUS, RADIUS), new Scale(0, 360));
    private final ArrowRenderer windDirectionArrow = new CardinalArrowRenderer();
    private final NeedleRenderer windDirectionNeedle;
    private final FormattedValueRenderer cardinalMarkers;
    private final ClockRenderer anonemeter = new ClockRenderer(new Point(7 * RADIUS, RADIUS), new Scale(0, 40, MIN_ANGLE, MAX_ANGLE));
    private final ArrowRenderer windSpeedArrow = new ArrowRenderer();
    private final NeedleRenderer windSpeedNeedle;
    private final ArrowRenderer squallArrow = new ArrowRenderer();
    private final NeedleRenderer squallNeedle;
    private final FormattedValueRenderer windSpeedMarkers;
    private final ClockRenderer barometer = new ClockRenderer(new Point(9 * RADIUS, RADIUS), new Scale(940, 1040, MIN_ANGLE, MAX_ANGLE));
    private final ArrowRenderer airPressureArrow = new ArrowRenderer();
    private final NeedleRenderer airPressureNeedle;
    private final FormattedValueRenderer airPressureMarkers;
    private final ClockRenderer viewmeter = new ClockRenderer(new Point(11 * RADIUS, RADIUS), new Scale(0, 60, MIN_ANGLE, MAX_ANGLE));
    private final ArrowRenderer visibilityArrow = new ArrowRenderer();
    private final NeedleRenderer visibilityNeedle;
    private final FormattedValueRenderer visibilityMarkers;

    private static final int RADIUS = 100;
    private static final double MIN_ANGLE = -0.4125;
    private static final double MAX_ANGLE = 0.4125;
    private static final Color NO_DATA_COLOR = new Color(225, 225, 225);
    private static final double MARKER_RADIUS = 0.85 * RADIUS;
    private static final double ARC_RADIUS = 0.95 * RADIUS;
    private static final float ARC_WIDTH = 5f;
    private static final Color ARC_COLOR_EVEN = new Color(0x4A41AF);
    private static final Color ARC_COLOR_ODD = new Color(0x918AE5);

    private static final CardinalNumberFormat cardinalFormat = new CardinalNumberFormat();
}
