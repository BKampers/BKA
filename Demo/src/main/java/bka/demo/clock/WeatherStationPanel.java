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
        cardinalMarkers = new FormattedValueRenderer(NO_DATA_COLOR, null, cardinalFormat);
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

    private static double degrees(String cardinalDirection) {
        try {
            return cardinalFormat.parse(cardinalDirection).doubleValue();
        }
        catch (ParseException ex) {
            Logger.getLogger(WeatherStationPanel.class.getName()).log(Level.WARNING, null, ex);
            return 0.0;
        }
    }

    @Override
    public void paint(Graphics graphics) {
        thermometer.paint((Graphics2D) graphics);
        windRose.paint((Graphics2D) graphics);
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

    private static final CardinalNumberFormat cardinalFormat = new CardinalNumberFormat();
}
