/*
** © Bart Kampers
*/

package bka.demo.clock;

import bka.awt.clock.*;
import java.awt.*;
import java.util.logging.*;
import javax.swing.*;


public class WeatherStationPanel extends JPanel {

    public WeatherStationPanel() {
        renderer.addClockFace(RADIUS, Color.WHITE);
        temperatureMarkers = renderer.addNumberRingRenderer(RADIUS * 0.9, 10, Color.BLUE);
        chillNeedle = renderer.addNeedleRenderer(chillArrow);
        temperatureNeedle = renderer.addNeedleRenderer(temperatureArrow);
    }

    public void setStation(WeatherStation station) {
        if (station != null) {
            chillNeedle.setValue((station.getChill() != null) ? station.getChill() : (station.getTemperature() != null) ? station.getTemperature() : -60);
            chillArrow.setPaint((station.getChill() != null) ? Color.BLUE : null);
            temperatureNeedle.setValue((station.getTemperature() != null) ? station.getTemperature() : -60);
            temperatureArrow.setPaint((station.getTemperature() != null) ? Color.RED : null);
            Logger.getLogger(WeatherStationPanel.class.getName()).log(Level.INFO, "{0}: {1}; {2}", new Object[]{ station.getStationName(), station.getTemperature(), station.getChill() });
        }
        repaint();
    }

    @Override
    public void paint(Graphics graphics) {
        renderer.paint((Graphics2D) graphics);
    }

    private class ArrowRenderer implements bka.awt.Renderer {

        @Override
        public void paint(Graphics2D graphics) {
            graphics.setPaint((paint != null) ? paint : Color.GRAY);
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
    
    private final ClockRenderer renderer = new ClockRenderer(new Point(RADIUS, RADIUS), new Scale(-50, 50, -0.4, 0.4));
    private final ArrowRenderer chillArrow = new ArrowRenderer();
    private final NeedleRenderer chillNeedle;
    private final ArrowRenderer temperatureArrow = new ArrowRenderer();
    private final NeedleRenderer temperatureNeedle;
    private final MarkerRingRenderer temperatureMarkers;

    private static final int RADIUS = 100;
}
