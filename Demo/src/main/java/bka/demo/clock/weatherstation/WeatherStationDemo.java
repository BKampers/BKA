package bka.demo.clock.weatherstation;

/*
** © Bart Kampers
*/


import java.io.*;
import java.time.*;
import java.time.format.*;
import java.util.*;
import java.util.logging.*;

public class WeatherStationDemo extends javax.swing.JFrame {

    public WeatherStationDemo() {
        initComponents();
        loadTimer.schedule(new DataReaderTask(), 0, TEN_MINUTES);
    }

    /**
     * This method is called from within the constructor to initialize the form. WARNING: Do NOT modify this code. The content of this method is
     * always regenerated by the Form Editor.
     */
    @SuppressWarnings("unchecked")
    // <editor-fold defaultstate="collapsed" desc="Generated Code">//GEN-BEGIN:initComponents
    private void initComponents() {

        javax.swing.JPanel controlPanel = new javax.swing.JPanel();
        reloadButton = new javax.swing.JButton();
        timestampLabel = new javax.swing.JLabel();
        javax.swing.JPanel weatherPanelPlaceholder = weatherPanel;

        setDefaultCloseOperation(javax.swing.WindowConstants.EXIT_ON_CLOSE);
        setTitle("Weather Station");
        setPreferredSize(new java.awt.Dimension(1200, 400));

        stationComboBox.setModel(stationComboBoxModel);
        stationComboBox.setEnabled(false);
        stationComboBox.addActionListener(new java.awt.event.ActionListener() {
            public void actionPerformed(java.awt.event.ActionEvent evt) {
                stationComboBoxActionPerformed(evt);
            }
        });

        reloadButton.setFont(new java.awt.Font("Lucida Grande", 0, 18)); // NOI18N
        reloadButton.setText("\u27F3");
        reloadButton.addActionListener(new java.awt.event.ActionListener() {
            public void actionPerformed(java.awt.event.ActionEvent evt) {
                reloadButtonActionPerformed(evt);
            }
        });

        timestampLabel.setForeground(new java.awt.Color(102, 102, 0));

        javax.swing.GroupLayout controlPanelLayout = new javax.swing.GroupLayout(controlPanel);
        controlPanel.setLayout(controlPanelLayout);
        controlPanelLayout.setHorizontalGroup(
            controlPanelLayout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
            .addGroup(controlPanelLayout.createSequentialGroup()
                .addGap(22, 22, 22)
                .addComponent(stationComboBox, javax.swing.GroupLayout.PREFERRED_SIZE, 199, javax.swing.GroupLayout.PREFERRED_SIZE)
                .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.RELATED)
                .addComponent(reloadButton, javax.swing.GroupLayout.PREFERRED_SIZE, 41, javax.swing.GroupLayout.PREFERRED_SIZE)
                .addGap(18, 18, 18)
                .addComponent(timestampLabel, javax.swing.GroupLayout.PREFERRED_SIZE, 704, javax.swing.GroupLayout.PREFERRED_SIZE)
                .addContainerGap(javax.swing.GroupLayout.DEFAULT_SIZE, Short.MAX_VALUE))
        );
        controlPanelLayout.setVerticalGroup(
            controlPanelLayout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
            .addGroup(controlPanelLayout.createSequentialGroup()
                .addGap(30, 30, 30)
                .addGroup(controlPanelLayout.createParallelGroup(javax.swing.GroupLayout.Alignment.CENTER)
                    .addComponent(stationComboBox, javax.swing.GroupLayout.PREFERRED_SIZE, 29, javax.swing.GroupLayout.PREFERRED_SIZE)
                    .addComponent(reloadButton, javax.swing.GroupLayout.PREFERRED_SIZE, 29, javax.swing.GroupLayout.PREFERRED_SIZE)
                    .addComponent(timestampLabel, javax.swing.GroupLayout.PREFERRED_SIZE, 27, javax.swing.GroupLayout.PREFERRED_SIZE))
                .addContainerGap(42, Short.MAX_VALUE))
        );

        controlPanelLayout.linkSize(javax.swing.SwingConstants.VERTICAL, new java.awt.Component[] {reloadButton, stationComboBox, timestampLabel});

        getContentPane().add(controlPanel, java.awt.BorderLayout.PAGE_START);

        javax.swing.GroupLayout weatherPanelPlaceholderLayout = new javax.swing.GroupLayout(weatherPanelPlaceholder);
        weatherPanelPlaceholder.setLayout(weatherPanelPlaceholderLayout);
        weatherPanelPlaceholderLayout.setHorizontalGroup(
            weatherPanelPlaceholderLayout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
            .addGap(0, 996, Short.MAX_VALUE)
        );
        weatherPanelPlaceholderLayout.setVerticalGroup(
            weatherPanelPlaceholderLayout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
            .addGap(0, 399, Short.MAX_VALUE)
        );

        getContentPane().add(weatherPanelPlaceholder, java.awt.BorderLayout.CENTER);

        pack();
    }// </editor-fold>//GEN-END:initComponents

    private void stationComboBoxActionPerformed(java.awt.event.ActionEvent evt) {//GEN-FIRST:event_stationComboBoxActionPerformed
        synchronized (mutex) {
            String selectedName = (String) stationComboBoxModel.getSelectedItem();
            if (selectedName != null) {
                WeatherStation station = stationsMap.get(selectedName);
                timestampLabel.setText(timestampText(station.getTimestamp()));
                weatherPanel.setStation(station);
            }
            else {
                weatherPanel.setStation(null);
            }
        }
    }//GEN-LAST:event_stationComboBoxActionPerformed

    private void reloadButtonActionPerformed(java.awt.event.ActionEvent evt) {//GEN-FIRST:event_reloadButtonActionPerformed
        loadTimer.cancel();
        loadTimer = new Timer();
        loadTimer.schedule(new DataReaderTask(), 0, TEN_MINUTES);
    }//GEN-LAST:event_reloadButtonActionPerformed


    private static String timestampText(LocalDateTime timestamp) {
        return timestamp.toLocalDate().format(DateTimeFormatter.ofLocalizedDate(FormatStyle.LONG)) + ' '
            + timestamp.toLocalTime().format(DateTimeFormatter.ofLocalizedTime(FormatStyle.SHORT));
    }

    /**
     * @param args the command line arguments
     */
    public static void main(String args[]) {
        PANEL_LOGGER.setLevel(Level.FINEST);
        ConsoleHandler handler = new ConsoleHandler();
        handler.setLevel(Level.FINEST);
        PANEL_LOGGER.addHandler(handler);
        /* Set the Nimbus look and feel */
        //<editor-fold defaultstate="collapsed" desc=" Look and feel setting code (optional) ">
        /* If Nimbus (introduced in Java SE 6) is not available, stay with the default look and feel.
         * For details see http://download.oracle.com/javase/tutorial/uiswing/lookandfeel/plaf.html 
         */
        try {
            for (javax.swing.UIManager.LookAndFeelInfo info : javax.swing.UIManager.getInstalledLookAndFeels()) {
                if ("Nimbus".equals(info.getName())) {
                    javax.swing.UIManager.setLookAndFeel(info.getClassName());
                    break;
                }
            }
        }
        catch (ReflectiveOperationException | javax.swing.UnsupportedLookAndFeelException ex) {
            java.util.logging.Logger.getLogger(WeatherStationDemo.class.getName()).log(java.util.logging.Level.SEVERE, null, ex);
        }
        //</editor-fold>

        /* Create and display the form */
        java.awt.EventQueue.invokeLater(() -> {
            new WeatherStationDemo().setVisible(true);
        });
    }

    private static String normalizedForAlphabet(String string) {
        return PREFIXES.stream()
            .filter(prefix -> string.startsWith(prefix + ' '))
            .map(prefix -> string.substring(prefix.length() + 1) + ", " + prefix)
            .findAny()
            .orElse(string);
    }

    private class DataReaderTask extends TimerTask {

        @Override
        public void run() {
            try {
                synchronized (mutex) {
                    WeatherStationReader.getStations().forEach(station -> stationsMap.put(normalizedForAlphabet(station.getStationName()), station));
                    String selected = (String) stationComboBoxModel.getSelectedItem();
                    stationComboBoxModel.removeAllElements();
                    stationComboBoxModel.addAll(stationsMap.keySet());
                    if (selected != null && stationsMap.keySet().contains(selected)) {
                        stationComboBoxModel.setSelectedItem(selected);
                    }
                    else {
                        stationComboBoxModel.setSelectedItem(null);
                    }
                    stationComboBox.setEnabled(true);
                }
            }
            catch (IOException ex) {
                Logger.getLogger(WeatherStationDemo.class.getName()).log(Level.WARNING, "Stations could not be retrieved", ex);
            }
        }

    }

    // Variables declaration - do not modify//GEN-BEGIN:variables
    private javax.swing.JButton reloadButton;
    private final javax.swing.JComboBox<String> stationComboBox = new javax.swing.JComboBox<>();
    private javax.swing.JLabel timestampLabel;
    // End of variables declaration//GEN-END:variables

    private final WeatherStationPanel weatherPanel = new WeatherStationPanel();
    private final javax.swing.DefaultComboBoxModel<String> stationComboBoxModel = new javax.swing.DefaultComboBoxModel<>();

    private final SortedMap<String, WeatherStation> stationsMap = new TreeMap<>();

    private Timer loadTimer = new Timer();
    private final Object mutex = new Object();

    private static final int TEN_MINUTES = 10 * 60 * 1000;

    private static final List<String> PREFIXES = List.of("De", "Den", "Het", "'t", "Ter");

    private static final Logger PANEL_LOGGER = Logger.getLogger(WeatherStationPanel.class.getName());

}