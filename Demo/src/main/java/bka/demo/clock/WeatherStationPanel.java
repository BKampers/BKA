/*
** © Bart Kampers
*/

package bka.demo.clock;

import bka.awt.clock.*;
import java.awt.*;
import java.text.*;
import java.util.logging.*;
import javax.swing.*;


public class WeatherStationPanel extends JPanel {

    public WeatherStationPanel() {
        thermometer.addClockFace(RADIUS, Color.WHITE);
        temperatureMarkers = new FormattedValueRenderer(NO_DATA_COLOR);
        thermometer.addMarkerRingRenderer(RADIUS * 0.9, 10, temperatureMarkers);
        chillNeedle = thermometer.addNeedleRenderer(chillArrow);
        temperatureNeedle = thermometer.addNeedleRenderer(temperatureArrow);
        windRose.addClockFace(RADIUS, Color.WHITE);
        cardinalMarkers = new FormattedValueRenderer(NO_DATA_COLOR, null, new CardinalNumberFormat());
        windRose.addMarkerRingRenderer(RADIUS * 0.9, 45, cardinalMarkers);
        windDirectionNeedle = windRose.addNeedleRenderer(windDirectionArrow);
    }

    public void setStation(WeatherStation station) {
        if (station != null) {
            chillNeedle.setValue((station.getChill() != null) ? station.getChill() : (station.getTemperature() != null) ? station.getTemperature() : -60);
            chillArrow.setPaint((station.getChill() != null) ? Color.BLUE : null);
            temperatureNeedle.setValue((station.getTemperature() != null) ? station.getTemperature() : -60);
            temperatureArrow.setPaint((station.getTemperature() != null) ? Color.RED : null);
            temperatureMarkers.setPaint((station.getTemperature() != null) ? Color.BLUE : NO_DATA_COLOR);
            windDirectionNeedle.setValue((station.getWindDirection() != null) ? degrees(station.getWindDirection()) : 0);
            windDirectionArrow.setPaint((station.getWindDirection() != null) ? Color.BLACK : null);
            cardinalMarkers.setPaint((station.getWindDirection() != null) ? Color.BLUE : NO_DATA_COLOR);
            Logger.getLogger(WeatherStationPanel.class.getName()).log(Level.FINE, "{0}: {1}; {2}; {3}", new Object[]{ station.getStationName(), station.getTemperature(), station.getChill(), station.getWindDirection() });
        }
        repaint();
    }

    @Override
    public void paint(Graphics graphics) {
        thermometer.paint((Graphics2D) graphics);
        windRose.paint((Graphics2D) graphics);
    }

    private static double degrees(String cardinalDirection) {
        switch (cardinalDirection) {
            case "N":
                return 0.0;
            case "NNO":
                return 22.5;
            case "NO":
                return 45.0;
            case "ONO":
                return 67.5;
            case "O":
                return 90.0;
            case "OZO":
                return 112.5;
            case "ZO":
                return 135.0;
            case "ZZO":
                return 157.5;
            case "Z":
                return 180.0;
            case "ZZW":
                return 202.5;
            case "ZW":
                return 225.0;
            case "WZW":
                return 247.5;
            case "W":
                return 270.0;
            case "WNW":
                return 292.5;
            case "NW":
                return 315.0;
            case "NNW":
                return 337.5;
            default:
                throw new IllegalStateException("Invalid cardinal direction: '" + cardinalDirection + '\'');
        }
    }

    private class CardinalNumberFormat extends NumberFormat {

        @Override
        public StringBuffer format(double number, StringBuffer toAppendTo, FieldPosition position) {
            return format(Math.round(number), toAppendTo, position);
        }

        @Override
        public StringBuffer format(long number, StringBuffer toAppendTo, FieldPosition position) {
            String result = switch ((int) (number % 360)) {
                case 0:
                    yield "N";
                case 45:
                    yield "NO";
                case 90:
                    yield "O";
                case 135:
                    yield "ZO";
                case 180:
                    yield "Z";
                case 225:
                    yield "ZW";
                case 270:
                    yield "W";
                case 315:
                    yield "NW";
                default:
                    throw new IllegalArgumentException("Cannot convert " + number + " to cardinal direction");
            };
            return toAppendTo.append(result);
        }

        @Override
        public Number parse(String string, ParsePosition position) {
            return degrees(string);
        }

    }

    private class ArrowRenderer implements bka.awt.Renderer {

        @Override
        public void paint(Graphics2D graphics) {
            graphics.setPaint((paint != null) ? paint : NO_DATA_COLOR);
            graphics.setStroke(new BasicStroke(3f, BasicStroke.CAP_ROUND, BasicStroke.JOIN_MITER));
            graphics.fillOval(0 - RADIUS / 20, 0 - RADIUS / 20, RADIUS / 10, RADIUS / 10);
            graphics.drawLine(0, RADIUS / 10, 0, -(RADIUS / 10) * 9);
            graphics.drawLine(5, -(RADIUS / 10 * 8), 0, -(RADIUS / 10) * 9);
            graphics.drawLine(-5, -(RADIUS / 10 * 8), 0, -(RADIUS / 10) * 9);
        }

        public void setPaint(Paint paint) {
            this.paint = paint;
        }

        private Paint paint;
    }

    private final ClockRenderer thermometer = new ClockRenderer(new Point(RADIUS, RADIUS), new Scale(-50, 50, -0.4125, 0.4125));
    private final ArrowRenderer temperatureArrow = new ArrowRenderer();
    private final NeedleRenderer temperatureNeedle;
    private final ArrowRenderer chillArrow = new ArrowRenderer();
    private final NeedleRenderer chillNeedle;
    private final FormattedValueRenderer temperatureMarkers;
    private final ArrowRenderer windDirectionArrow = new ArrowRenderer();
    private final ClockRenderer windRose = new ClockRenderer(new Point(3 * RADIUS, RADIUS), new Scale(0, 360));
    private final NeedleRenderer windDirectionNeedle;
    private final FormattedValueRenderer cardinalMarkers;

    private static final int RADIUS = 100;
    private static final Color NO_DATA_COLOR = new Color(225, 225, 225);
}
