/*
** © Bart Kampers
*/

package bka.demo.clock;

import bka.awt.clock.*;
import java.awt.*;
import java.io.*;
import java.util.*;
import java.util.logging.*;
import javax.swing.*;


public class WeatherStationPanel extends JPanel {

    public interface DataInitializedListener {

        void dataInitialized(Collection<WeatherStation> stations);
    }

    public WeatherStationPanel() {
        renderer.addClockFace(RADIUS, Color.WHITE);
        temperatureMarkers = renderer.addNumberRingRenderer(RADIUS * 0.9, 10, Color.BLUE);
        temperatureNeedle = renderer.addNeedleRenderer(RADIUS, Color.BLACK, 3f);
        dataReadTask = new DataReadTask();
        timer.schedule(dataReadTask, 100, 10 * 60 * 1000);
    }
    private final DataReadTask dataReadTask;

    void setSelectedStation(String station) {
        selectedStation = station;
        update();
    }

    @Override
    public void paint(Graphics graphics) {
        renderer.paint((Graphics2D) graphics);
    }

    public void addListener(DataInitializedListener listener) {
        listeners.add(listener);
    }

    private void update() {
        WeatherStation found = dataReadTask.stations.stream()
            .filter(station -> Objects.equals(station.getStationName(), selectedStation))
            .findAny().orElse(null);
        if (found != null) {
            System.out.printf("%s %s: %.1f\n", new Date(), found.getStationName(), found.getTemperature());
            if (found.getTemperature() != null) {
                temperatureNeedle.setValue(found.getTemperature());
            }
            else {
                //
            }
            repaint();
        }
    }

    private class DataReadTask extends TimerTask {

        @Override
        public void run() {
            try {
                synchronized (lock) {
                    stations = WeatherStationReader.getStations();
                    update();
                    listeners.forEach(listener -> listener.dataInitialized(Collections.unmodifiableCollection(stations)));
                }
            }
            catch (IOException ex) {
                Logger.getLogger(WeatherStationPanel.class.getName()).log(Level.WARNING, null, ex);
            }
        }

        private Collection<WeatherStation> stations;
        private final Object lock = new Object();

    }

    private String selectedStation;

    private final ClockRenderer renderer = new ClockRenderer(new Point(RADIUS, RADIUS), new Scale(-50, 50, -0.4, 0.4));
    private final NeedleRenderer temperatureNeedle;
    private final MarkerRingRenderer temperatureMarkers;

    private final java.util.Timer timer = new java.util.Timer("DataReadTimer");
    private final Collection<DataInitializedListener> listeners = Collections.synchronizedCollection(new ArrayList<>());

    private static final int RADIUS = 100;
}
