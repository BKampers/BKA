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
        temperatureNeedle = renderer.addNeedleRenderer(RADIUS, Color.BLACK, 3f);
    }

    public void setStation(WeatherStation station) {
        if (station != null) {
            temperatureNeedle.setValue(station.getTemperature());
            Logger.getLogger(WeatherStationPanel.class.getName()).log(Level.INFO, "{0} {1}", new Object[]{ station.getStationName(), station.getTemperature() });
        }
        repaint();
    }

    @Override
    public void paint(Graphics graphics) {
        renderer.paint((Graphics2D) graphics);
    }


    private final ClockRenderer renderer = new ClockRenderer(new Point(RADIUS, RADIUS), new Scale(-50, 50, -0.4, 0.4));
    private final NeedleRenderer temperatureNeedle;
    private final MarkerRingRenderer temperatureMarkers;

    private static final int RADIUS = 100;
}
