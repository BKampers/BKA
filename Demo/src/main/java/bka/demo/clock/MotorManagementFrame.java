/*
 * © Bart Kampers
 */
package bka.demo.clock;

import bka.awt.clock.*;
import java.awt.*;
import java.io.*;
import java.util.logging.*;
import javax.imageio.*;

/**
 *
 * @author bartkampers
 */
public class MotorManagementFrame extends javax.swing.JFrame {

    /**
     * Creates new form MotorManagementFrame
     */
    public MotorManagementFrame() {
        initComponents();
        initializeRpmClockRenderer();
        initializeLoadClockRenderer();
        initializeTemperatureClockRenderer();
    }

    /**
     * This method is called from within the constructor to initialize the form. WARNING: Do NOT modify this code. The content of this method is
     * always regenerated by the Form Editor.
     */
    @SuppressWarnings("unchecked")
    // <editor-fold defaultstate="collapsed" desc="Generated Code">//GEN-BEGIN:initComponents
    private void initComponents() {

        rpmPanel = new ClockRendererPanel(rpmClockRenderer);
        loadPanel = new ClockRendererPanel(loadClockRenderer);
        temperaturePanel = new ClockRendererPanel(temperatureClockRenderer);

        setDefaultCloseOperation(javax.swing.WindowConstants.EXIT_ON_CLOSE);
        setPreferredSize(new java.awt.Dimension(700, 700));
        setSize(new java.awt.Dimension(700, 700));

        rpmPanel.setBorder(javax.swing.BorderFactory.createBevelBorder(javax.swing.border.BevelBorder.RAISED));
        rpmPanel.setMaximumSize(new java.awt.Dimension(250, 250));
        rpmPanel.setMinimumSize(new java.awt.Dimension(250, 250));
        rpmPanel.setPreferredSize(new java.awt.Dimension(250, 250));
        rpmPanel.setSize(new java.awt.Dimension(250, 250));

        javax.swing.GroupLayout rpmPanelLayout = new javax.swing.GroupLayout(rpmPanel);
        rpmPanel.setLayout(rpmPanelLayout);
        rpmPanelLayout.setHorizontalGroup(
            rpmPanelLayout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
            .addGap(0, 0, Short.MAX_VALUE)
        );
        rpmPanelLayout.setVerticalGroup(
            rpmPanelLayout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
            .addGap(0, 696, Short.MAX_VALUE)
        );

        getContentPane().add(rpmPanel, java.awt.BorderLayout.CENTER);

        loadPanel.setBorder(javax.swing.BorderFactory.createBevelBorder(javax.swing.border.BevelBorder.RAISED));
        loadPanel.setMaximumSize(new java.awt.Dimension(250, 250));
        loadPanel.setMinimumSize(new java.awt.Dimension(250, 250));
        loadPanel.setPreferredSize(new java.awt.Dimension(250, 250));

        javax.swing.GroupLayout loadPanelLayout = new javax.swing.GroupLayout(loadPanel);
        loadPanel.setLayout(loadPanelLayout);
        loadPanelLayout.setHorizontalGroup(
            loadPanelLayout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
            .addGap(0, 246, Short.MAX_VALUE)
        );
        loadPanelLayout.setVerticalGroup(
            loadPanelLayout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
            .addGap(0, 696, Short.MAX_VALUE)
        );

        getContentPane().add(loadPanel, java.awt.BorderLayout.EAST);

        temperaturePanel.setBorder(javax.swing.BorderFactory.createBevelBorder(javax.swing.border.BevelBorder.RAISED));
        temperaturePanel.setMaximumSize(new java.awt.Dimension(150, 150));
        temperaturePanel.setMinimumSize(new java.awt.Dimension(150, 150));
        temperaturePanel.setPreferredSize(new java.awt.Dimension(150, 150));
        temperaturePanel.setSize(new java.awt.Dimension(150, 150));

        javax.swing.GroupLayout temperaturePanelLayout = new javax.swing.GroupLayout(temperaturePanel);
        temperaturePanel.setLayout(temperaturePanelLayout);
        temperaturePanelLayout.setHorizontalGroup(
            temperaturePanelLayout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
            .addGap(0, 146, Short.MAX_VALUE)
        );
        temperaturePanelLayout.setVerticalGroup(
            temperaturePanelLayout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
            .addGap(0, 696, Short.MAX_VALUE)
        );

        getContentPane().add(temperaturePanel, java.awt.BorderLayout.WEST);

        pack();
    }// </editor-fold>//GEN-END:initComponents

    /**
     * @param args the command line arguments
     */
    public static void main(String args[]) {
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
        catch (ClassNotFoundException ex) {
            java.util.logging.Logger.getLogger(MotorManagementFrame.class.getName()).log(java.util.logging.Level.SEVERE, null, ex);
        }
        catch (InstantiationException ex) {
            java.util.logging.Logger.getLogger(MotorManagementFrame.class.getName()).log(java.util.logging.Level.SEVERE, null, ex);
        }
        catch (IllegalAccessException ex) {
            java.util.logging.Logger.getLogger(MotorManagementFrame.class.getName()).log(java.util.logging.Level.SEVERE, null, ex);
        }
        catch (javax.swing.UnsupportedLookAndFeelException ex) {
            java.util.logging.Logger.getLogger(MotorManagementFrame.class.getName()).log(java.util.logging.Level.SEVERE, null, ex);
        }
        //</editor-fold>

        /* Create and display the form */
        java.awt.EventQueue.invokeLater(() -> {
            new MotorManagementFrame().setVisible(true);
        });
    }

    private void initializeRpmClockRenderer() {
        Dimension size = rpmPanel.getSize();
        Scale scale = new Scale(0, 10, -0.4, 0.4);
        Point center = new Point(size.width / 2, size.height / 2);
        int radius = Math.min(size.width, size.height) / 2 - 32;
        rpmClockRenderer.setBackground(Color.WHITE);
        SimpleValueRing valueRing = new SimpleValueRing(center, radius, scale);
        valueRing.setFont(new Font(Font.SANS_SERIF, Font.BOLD, 12));
        valueRing.setMarkersRotated(true);
        rpmClockRenderer.addRing(valueRing);
        SimpleMarkerRing markerRing = new SimpleMarkerRing(center, radius + 12, scale, 0.5);
        rpmClockRenderer.addRing(markerRing);
        rpmClockRenderer.addText(new Text("RPM", center, new Font(Font.SANS_SERIF, Font.PLAIN, 14), Color.DARK_GRAY));
        rpmClockRenderer.addText(new Text("\u00d7 1000", new Point(center.x, center.y + 15), new Font(Font.SANS_SERIF, Font.PLAIN, 14), Color.DARK_GRAY));
        ArcRing arcRing = new ArcRing(center, radius - 10, scale, java.util.List.of(new ArcRing.Arc(7.5, 10, Color.RED, new BasicStroke(8))));
        rpmClockRenderer.addRing(arcRing);
        PolygonNeedle needle = new PolygonNeedle(center, scale, radius);//new ImageNeedle(center, scale, NEEDLE_IMAGE, new Point(13, 95));
        needle.setStroke(new BasicStroke(5f, BasicStroke.CAP_ROUND, BasicStroke.JOIN_MITER));
        needle.setPaint(Color.ORANGE.darker());
        needle.setPolygon(new Polygon(new int[]{ -1, 0, 4, -1 }, new int[]{ -4, -radius, -4, -4 }, 4));
        needle.setValue(2.3);
        rpmClockRenderer.addNeedle(needle);
    }

    private void initializeLoadClockRenderer() {
        Dimension size = loadPanel.getSize();
        Scale scale = new Scale(0, 100, -0.4, 0.4);
        Point center = new Point(size.width / 2, size.height / 2);
        int radius = Math.min(size.width, size.height) / 2 - 32;
        SimpleValueRing valueRing = new SimpleValueRing(center, radius, scale);
        valueRing.setInterval(10);
        valueRing.setFont(new Font(Font.SANS_SERIF, Font.BOLD, 12));
        loadClockRenderer.addRing(valueRing);
//        loadClockRenderer.addRing(new ImageMarkerRing(center, radius + 12, scale, 1, MARKER_IMAGE, new Dimension(1, 1), MARKER_IMAGE, new Dimension(2, 2)));
        loadClockRenderer.addRing(new SimpleMarkerRing(center, radius + 12, scale, 1));
        loadClockRenderer.addText(new Text("load %", center, new Font(Font.SANS_SERIF, Font.PLAIN, 14), Color.DARK_GRAY));
        PolygonNeedle needle = new PolygonNeedle(center, scale, radius);
        needle.setStroke(new BasicStroke(5f, BasicStroke.CAP_ROUND, BasicStroke.JOIN_MITER));
        needle.setPaint(Color.ORANGE.darker());
        needle.setPolygon(new Polygon(new int[]{ -1, 0, 4, -1 }, new int[]{ -4, -radius, -4, -4 }, 4));
        needle.setValue(37);
        loadClockRenderer.addNeedle(needle);
    }

    private void initializeTemperatureClockRenderer() {
        Dimension size = temperaturePanel.getSize();
        Scale scale = new Scale(-50, 200, -0.5, 0.0);
        Point center = new Point(size.width / 2, size.height / 2);
        int radius = Math.min(size.width, size.height) / 2 - 32;
        SimpleValueRing valueRing = new SimpleValueRing(center, radius + 12, scale);
        valueRing.setInterval(50);
        valueRing.setFont(new Font(Font.SANS_SERIF, Font.BOLD, 10));
        temperatureClockRenderer.addRing(valueRing);
//        temperatureClockRenderer.addRing(new SimpleMarkerRing(center, radius, scale, 50));
        temperatureClockRenderer.addRing(new ImageMarkerRing(center, radius + 12, scale, 50, MARKER_IMAGE, new Dimension(10, 10), MARKER_IMAGE, new Dimension(20, 20)));
        temperatureClockRenderer.addText(new Text("\u2103", new Point(center.x, center.y - radius / 2), new Font(Font.SANS_SERIF, Font.PLAIN, 14), Color.DARK_GRAY));
        PolygonNeedle airNeedle = new PolygonNeedle(center, scale, radius);
        airNeedle.setStroke(new BasicStroke(3f, BasicStroke.CAP_ROUND, BasicStroke.JOIN_MITER));
        airNeedle.setPaint(Color.BLUE.darker());
        airNeedle.setPolygon(new Polygon(new int[]{ -1, 0, 4, -1 }, new int[]{ -1, -radius, -1, -1 }, 4));
        airNeedle.setValue(17);
        temperatureClockRenderer.addNeedle(airNeedle);
        PolygonNeedle engineNeedle = new PolygonNeedle(center, scale, radius);
        engineNeedle.setStroke(new BasicStroke(3f, BasicStroke.CAP_ROUND, BasicStroke.JOIN_MITER));
        engineNeedle.setPaint(Color.RED.darker());
        engineNeedle.setPolygon(new Polygon(new int[]{ -1, 0, 4, -1 }, new int[]{ 0, -radius, 0, 0 }, 4));
        engineNeedle.setValue(90);
        temperatureClockRenderer.addNeedle(engineNeedle);
    }

    private class ClockRendererPanel extends javax.swing.JPanel {

        private ClockRendererPanel(ClockRenderer renderer) {
            this.renderer = renderer;
        }

        @Override
        public void paint(Graphics graphics) {
            renderer.paint((Graphics2D) graphics);
        }

        @Override
        public void paintComponent(Graphics graphics) {
            renderer.paint((Graphics2D) graphics);
        }

        private final ClockRenderer renderer;

    }

    // Variables declaration - do not modify//GEN-BEGIN:variables
    private javax.swing.JPanel loadPanel;
    private javax.swing.JPanel rpmPanel;
    private javax.swing.JPanel temperaturePanel;
    // End of variables declaration//GEN-END:variables

    private final SimpleClock rpmClockRenderer = new SimpleClock();
    private final ClockRenderer loadClockRenderer = new SimpleClock();
    private final ClockRenderer temperatureClockRenderer = new SimpleClock();

    private static final Image NEEDLE_IMAGE;
    private static final Image MARKER_IMAGE;

    static {
        NEEDLE_IMAGE = loadImage("Resources/Needle1.png");
        MARKER_IMAGE = loadImage("Resources/Flash.png").getScaledInstance(10, 10, Image.SCALE_SMOOTH);
    }

    private static Image loadImage(String filename) {
        try {
            return ImageIO.read(new File(filename));//.getScaledInstance(35, 100, java.awt.Image.SCALE_SMOOTH);
        }
        catch (IOException ex) {
            Logger.getLogger(MotorManagementFrame.class.getName()).log(Level.SEVERE, null, ex);
            return null;
        }
    }


//     private final Needle rpmNeedle = new PolygonNeedle(50);

}
