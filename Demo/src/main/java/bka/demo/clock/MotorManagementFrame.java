/*
 * © Bart Kampers
 */
package bka.demo.clock;

import bka.awt.clock.*;
import java.awt.*;
import java.awt.image.*;
import java.io.*;
import java.text.*;
import java.util.*;
import java.util.logging.*;
import javax.imageio.*;

/**
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

        rpmPanel = new ClockRendererPanel();
        loadPanel = new ClockRendererPanel();
        temperaturePanel = new ClockRendererPanel();

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

    @Override
    public void paint(Graphics graphics) {
        super.paint(graphics);

    }

    private void initializeRpmClockRenderer() {
        Dimension size = rpmPanel.getSize();
        Scale scale = new Scale(0, 10, -0.4, 0.4);
        Point center = new Point(size.width / 2, size.height / 2);
        int diameter = Math.min(size.width, size.height);
        int radius = diameter / 2 - 32;
        rpmClockRenderer = new ClockRenderer(center, scale);
        ((ClockRendererPanel) rpmPanel).setRenderer(rpmClockRenderer);
        rpmClockRenderer.addMarkerRingRenderer(radius, 1, MotorManagementFrame::paintImageMarker);
        rpmClockRenderer.addMarkerRingRenderer(radius + 12, 0.5, graphics -> {
            graphics.setPaint(Color.BLACK);
            graphics.drawLine(-1, 0, 1, 0);
            graphics.drawLine(0, -1, 0, 1);
        });
        rpmClockRenderer.add(new Text("RPM", center, new Font(Font.SANS_SERIF, Font.PLAIN, 14), Color.DARK_GRAY));
        rpmClockRenderer.add(new Text("\u00d7 1000", new Point(center.x, center.y + 15), new Font(Font.SANS_SERIF, Font.PLAIN, 14), Color.DARK_GRAY));
        rpmClockRenderer.addArc(radius - 10, 7.5, 10.0, Color.RED, 8f);
        NeedleRenderer needle = rpmClockRenderer.addNeedleRenderer(new NeedleImageRenderer(needleImage(), new Point(0, 0)));
        needle.setValue(5);
    }

    private static Image needleImage() {
        int width = 11;
        int height = 29;
        int[] rgb = new int[]{
            0x00000000, 0x00000000, 0x00000000, 0x00000000, 0x00000000, 0xFF000000, 0x00000000, 0x00000000, 0x00000000, 0x00000000, 0x00000000,
            0x00000000, 0x00000000, 0x00000000, 0x00000000, 0xFF000000, 0xFF00FF00, 0xFF000000, 0x00000000, 0x00000000, 0x00000000, 0x00000000,
            0x00000000, 0x00000000, 0x00000000, 0xFF000000, 0xFF00FF00, 0xFF00FF00, 0xFF00FF00, 0xFF000000, 0x00000000, 0x00000000, 0x00000000,
            0x00000000, 0x00000000, 0xFF000000, 0xFF00FF00, 0xFF00FF00, 0xFF00FF00, 0xFF00FF00, 0xFF00FF00, 0xFF000000, 0x00000000, 0x00000000,
            0x00000000, 0xFF000000, 0xFF00FF00, 0xFF00FF00, 0xFF00FF00, 0xFF00FF00, 0xFF00FF00, 0xFF00FF00, 0xFF00FF00, 0xFF000000, 0x00000000,
            0xFF000000, 0xFF000000, 0xFF000000, 0xFF000000, 0xFF000000, 0xFF00FF00, 0xFF000000, 0xFF000000, 0xFF000000, 0xFF000000, 0xFF000000,
            0x00000000, 0x00000000, 0x00000000, 0x00000000, 0xFF000000, 0xFF00FF00, 0xFF000000, 0x00000000, 0x00000000, 0x00000000, 0x00000000,
            0x00000000, 0x00000000, 0x00000000, 0x00000000, 0xFF000000, 0xFF00FF00, 0xFF000000, 0x00000000, 0x00000000, 0x00000000, 0x00000000,
            0x00000000, 0x00000000, 0x00000000, 0x00000000, 0xFF000000, 0xFF00FF00, 0xFF00FF00, 0x00000000, 0x00000000, 0x00000000, 0x00000000,
            0x00000000, 0x00000000, 0x00000000, 0x00000000, 0xFF000000, 0xFF00FF00, 0xFF000000, 0x00000000, 0x00000000, 0x00000000, 0x00000000,
            0x00000000, 0x00000000, 0x00000000, 0x00000000, 0xFF000000, 0xFF00FF00, 0xFF000000, 0x00000000, 0x00000000, 0x00000000, 0x00000000,
            0x00000000, 0x00000000, 0x00000000, 0x00000000, 0xFF000000, 0xFF00FF00, 0xFF000000, 0x00000000, 0x00000000, 0x00000000, 0x00000000,
            0x00000000, 0x00000000, 0x00000000, 0x00000000, 0xFF000000, 0xFF00FF00, 0xFF000000, 0x00000000, 0x00000000, 0x00000000, 0x00000000,
            0x00000000, 0x00000000, 0x00000000, 0x00000000, 0xFF000000, 0xFF00FF00, 0xFF000000, 0x00000000, 0x00000000, 0x00000000, 0x00000000,
            0x00000000, 0x00000000, 0x00000000, 0x00000000, 0xFF000000, 0xFF00FF00, 0xFF000000, 0x00000000, 0x00000000, 0x00000000, 0x00000000,
            0x00000000, 0x00000000, 0x00000000, 0x00000000, 0xFF000000, 0xFF00FF00, 0xFF000000, 0x00000000, 0x00000000, 0x00000000, 0x00000000,
            0x00000000, 0x00000000, 0x00000000, 0x00000000, 0xFF000000, 0xFF00FF00, 0xFF000000, 0x00000000, 0x00000000, 0x00000000, 0x00000000,
            0x00000000, 0x00000000, 0x00000000, 0x00000000, 0xFF000000, 0xFF00FF00, 0xFF000000, 0x00000000, 0x00000000, 0x00000000, 0x00000000,
            0x00000000, 0x00000000, 0x00000000, 0x00000000, 0xFF000000, 0xFF00FF00, 0xFF000000, 0x00000000, 0x00000000, 0x00000000, 0x00000000,
            0x00000000, 0x00000000, 0x00000000, 0x00000000, 0xFF000000, 0xFF00FF00, 0xFF000000, 0x00000000, 0x00000000, 0x00000000, 0x00000000,
            0x00000000, 0x00000000, 0x00000000, 0x00000000, 0xFF000000, 0xFF00FF00, 0xFF000000, 0x00000000, 0x00000000, 0x00000000, 0x00000000,
            0x00000000, 0x00000000, 0x00000000, 0x00000000, 0xFF000000, 0xFF00FF00, 0xFF000000, 0x00000000, 0x00000000, 0x00000000, 0x00000000,
            0x00000000, 0x00000000, 0x00000000, 0x00000000, 0xFF000000, 0xFF00FF00, 0xFF000000, 0x00000000, 0x00000000, 0x00000000, 0x00000000,
            0x00000000, 0x00000000, 0x00000000, 0x00000000, 0xFF000000, 0xFF00FF00, 0xFF000000, 0x00000000, 0x00000000, 0x00000000, 0x00000000,
            0x00000000, 0x00000000, 0x00000000, 0x00000000, 0xFF000000, 0xFF00FF00, 0xFF000000, 0x00000000, 0x00000000, 0x00000000, 0x00000000,
            0x00000000, 0x00000000, 0x00000000, 0x00000000, 0xFF000000, 0xFF00FF00, 0xFF000000, 0x00000000, 0x00000000, 0x00000000, 0x00000000,
            0x00000000, 0x00000000, 0x00000000, 0x00000000, 0xFF000000, 0xFF00FF00, 0xFF000000, 0x00000000, 0x00000000, 0x00000000, 0x00000000,
            0x00000000, 0x00000000, 0x00000000, 0x00000000, 0xFF000000, 0xFF00FF00, 0xFF000000, 0x00000000, 0x00000000, 0x00000000, 0x00000000,
            0x00000000, 0x00000000, 0x00000000, 0x00000000, 0xFF000000, 0xFF000000, 0xFF000000, 0x00000000, 0x00000000, 0x00000000, 0x00000000, };
        BufferedImage result = new BufferedImage(width, height, BufferedImage.TYPE_INT_ARGB);
        result.setRGB(0, 0, width, height, rgb, 0, width);
        return result;
    }

    private static void paintImageMarker(Graphics2D graphics, double value) {
        NumberFormat defaultFormat = new DecimalFormat();
        defaultFormat.setGroupingUsed(false);
        Image image = getImage(defaultFormat.format(value));
        graphics.drawImage(image, image.getWidth(null) / -2, image.getHeight(null) / -2, null);
    }

    private static Image getImage(String text) {
        ArrayList<Image> images = new ArrayList<>(text.length());
        int width = 0;
        int height = 0;
        int i = 0;
        for (char ch : text.toCharArray()) {
            if ('0' <= ch && ch <= '9') {
                Image image = get(ch - '0');
                images.add(image);
                width += image.getWidth(null);
                height = Math.max(height, image.getHeight(null));
                i++;
            }
        }
        BufferedImage image = new BufferedImage(width,height,BufferedImage.TYPE_INT_ARGB);
        Graphics2D graphics = image.createGraphics();
        int x = 0;
        for (Image charImage : images) {
            int y = (height - charImage.getHeight(null)) / 2;
            graphics.drawImage(charImage, x, y, null);
            x += charImage.getWidth(null);
        }
        graphics.dispose();
        return image;
    }

    private static Image get(int digit) {
        final int width=8;
        final int height = 7;
        byte[] bitmap = NUMBERS[digit];
        int[] rgb = new int[width * height];
        for (int row = 0; row < height; ++row) {
            byte pattern = bitmap[row];
            for (int column = 0; column < width; ++ column) {
                rgb[row * width + column] = ((pattern & 0b10000000) == 0) ? 0x00FFFFFF : 0xFF000000;
                pattern <<= 1;
            }
        }
        BufferedImage result = new BufferedImage(width, height, BufferedImage.TYPE_INT_ARGB);
        result.setRGB(0, 0, width, height, rgb, 0, width);
        return result;
    }

    private static final byte[][] NUMBERS = {
        {
            (byte) 0b01111110,
            (byte) 0b10000001,
            (byte) 0b10000001,
            (byte) 0b10000001,
            (byte) 0b10000001,
            (byte) 0b10000001,
            (byte) 0b01111110
        },
        {
            (byte) 0b00001000,
            (byte) 0b00011000,
            (byte) 0b00111000,
            (byte) 0b00001000,
            (byte) 0b00001000,
            (byte) 0b00001000,
            (byte) 0b01111110
        },
        {
            (byte) 0b01111110,
            (byte) 0b00000001,
            (byte) 0b00000001,
            (byte) 0b01111110,
            (byte) 0b10000000,
            (byte) 0b10000000,
            (byte) 0b01111110
        },
        {
            (byte) 0b11111110,
            (byte) 0b00000001,
            (byte) 0b00000001,
            (byte) 0b01111110,
            (byte) 0b00000001,
            (byte) 0b00000001,
            (byte) 0b11111110
        },
        {
            (byte) 0b10000001,
            (byte) 0b10000001,
            (byte) 0b10000001,
            (byte) 0b01111111,
            (byte) 0b00000001,
            (byte) 0b00000001,
            (byte) 0b00000001
        },
        {
            (byte) 0b01111110,
            (byte) 0b10000000,
            (byte) 0b10000000,
            (byte) 0b01111110,
            (byte) 0b00000001,
            (byte) 0b00000001,
            (byte) 0b01111110
        },
        {
            (byte) 0b01111110,
            (byte) 0b10000000,
            (byte) 0b10000000,
            (byte) 0b11111110,
            (byte) 0b10000001,
            (byte) 0b10000001,
            (byte) 0b01111110
        },
        {
            (byte) 0b11111111,
            (byte) 0b10000001,
            (byte) 0b00000010,
            (byte) 0b00000100,
            (byte) 0b00001000,
            (byte) 0b00010000,
            (byte) 0b00100000
        },
        {
            (byte) 0b01111110,
            (byte) 0b10000001,
            (byte) 0b10000001,
            (byte) 0b01111110,
            (byte) 0b10000001,
            (byte) 0b10000001,
            (byte) 0b01111110
        },
        {
            (byte) 0b01111110,
            (byte) 0b10000001,
            (byte) 0b10000001,
            (byte) 0b01111111,
            (byte) 0b00000001,
            (byte) 0b00000001,
            (byte) 0b01111110
        }
    };

//    private static Image normalized(Image image) {
//        int width = image.getWidth(null);
//        int height = image.getHeight(null);
//        int adaptedWidth = odd(width);
//        int adaptedHeight = odd(height);
//        if (adaptedWidth == width && adaptedHeight == height) {
//            return image;
//        }
//        BufferedImage bi = new BufferedImage(width, height, BufferedImage.TYPE_INT_ARGB);
//        Graphics2D g2d = bi.createGraphics();
//        g2d.drawImage(image, 0, 0, null);
//        g2d.dispose();
//        int[] rgb = new int[adaptedWidth * adaptedHeight];
//        bi.getRGB(0, 0, adaptedWidth, adaptedHeight, rgb, 0, adaptedWidth);
//        for (int x = 0; x < adaptedWidth; ++x) {
//            for (int y = 0; y < adaptedHeight; ++y) {
//                if (rgb[x + y * adaptedWidth] != 0) {
//                    rgb[x + y * adaptedWidth] = 0xFF000000 | (x + y * adaptedWidth);
//                }
//            }
//        }
//        BufferedImage result = new BufferedImage(adaptedWidth, adaptedHeight, BufferedImage.TYPE_INT_ARGB);
//        result.setRGB(0, 0, adaptedWidth, adaptedHeight, rgb, 0, adaptedWidth);
//        return result;
//    }
//
//    private static int odd(int value) {
//        return (value > 0 && value % 2 == 0) ? value - 1 : value;
//    }

    private void initializeLoadClockRenderer() {
        Dimension size = loadPanel.getSize();
        Scale scale = new Scale(0, 100, -0.4, 0.4);
        Point center = new Point(size.width / 2, size.height / 2);
        loadClockRenderer = new ClockRenderer(center, scale);
        ((ClockRendererPanel) loadPanel).setRenderer(loadClockRenderer);
        int radius = Math.min(size.width, size.height) / 2 - 32;
        loadClockRenderer.addNonTiltedMarkerRingRenderer(radius, 10, new FormattedValueRenderer(Color.BLACK, new Font(Font.SANS_SERIF, Font.BOLD, 12)));
        loadClockRenderer.addMarkerRingRenderer(radius + 12, 1, 2, 5, 5, Color.BLACK, 1f);
        loadClockRenderer.add(new Text("load %", center, new Font(Font.SANS_SERIF, Font.PLAIN, 14), Color.DARK_GRAY));
        NeedleRenderer needle = loadClockRenderer.addNeedleRenderer(new ShapeRenderer(new Polygon(new int[]{ -1, 0, 4, -1 }, new int[]{ -4, -radius, -4, -4 }, 4), Color.ORANGE.darker(), new BasicStroke(5f, BasicStroke.CAP_ROUND, BasicStroke.JOIN_MITER)));
        needle.setValue(37);
    }

    private void initializeTemperatureClockRenderer() {
        Dimension size = temperaturePanel.getSize();
        Scale scale = new Scale(-50, 200, -0.5, 0.0);
        Point center = new Point(size.width / 2, size.height / 2);
        temperatureClockRenderer = new ClockRenderer(center, scale);
        ((ClockRendererPanel) temperaturePanel).setRenderer(temperatureClockRenderer);
        int radius = Math.min(size.width, size.height) / 2 - 32;
        temperatureClockRenderer.addClockFace(radius, new Color(0x7F7FFF7F), Color.BLACK, 3f);
        temperatureClockRenderer.addNonTiltedMarkerRingRenderer(radius + 12, 50, new FormattedValueRenderer(Color.BLACK, new Font(Font.SANS_SERIF, Font.BOLD, 10)));
        temperatureClockRenderer.addMarkerRingRenderer(radius, 50, new ImageRenderer(MARKER_IMAGE));
        temperatureClockRenderer.add(new Text("\u2103", new Point(center.x, center.y - radius / 2), new Font(Font.SANS_SERIF, Font.PLAIN, 14), Color.DARK_GRAY));
        Shape needleShape = new Polygon(new int[]{ -1, 0, 4, -1 }, new int[]{ -1, -radius, -1, -1 }, 4);
        Stroke needleStroke = new BasicStroke(3f, BasicStroke.CAP_ROUND, BasicStroke.JOIN_MITER);
        ShapeRenderer airNeedleRenderer = new ShapeRenderer(needleShape, Color.BLUE.darker(), needleStroke);
        NeedleRenderer airNeedle = temperatureClockRenderer.addNeedleRenderer(airNeedleRenderer::paint);
        airNeedle.setValue(17);
        NeedleRenderer engineNeedle = temperatureClockRenderer.addNeedleRenderer(new ShapeRenderer(needleShape, Color.RED.darker(), needleStroke));
        engineNeedle.setValue(90);
    }

    private class ClockRendererPanel extends javax.swing.JPanel {

        public void setRenderer(ClockRenderer renderer) {
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

        private ClockRenderer renderer;

    }

    // Variables declaration - do not modify//GEN-BEGIN:variables
    private javax.swing.JPanel loadPanel;
    private javax.swing.JPanel rpmPanel;
    private javax.swing.JPanel temperaturePanel;
    // End of variables declaration//GEN-END:variables

    private ClockRenderer rpmClockRenderer;
    private ClockRenderer loadClockRenderer;
    private ClockRenderer temperatureClockRenderer;

    private static final Image NEEDLE_IMAGE;
    private static final Image MARKER_IMAGE;

    static {
        NEEDLE_IMAGE = loadImage("Resources/Needle1.png");
//        MARKER_IMAGE = loadImage("Resources/Flash.png").getScaledInstance(11, 11, Image.SCALE_SMOOTH);
        MARKER_IMAGE = loadSvgImage("Resources/circle.svg", 11, 11);
    }

    private static Image loadSvgImage(String filename, int width, int height) {
        try {
            return new SvgToRasterizeImageConverter().transcodeSVGToBufferedImage(new File(filename), width, height);
        }
        catch (IOException ex) {
            Logger.getLogger(MotorManagementFrame.class.getName()).log(Level.SEVERE, null, ex);
            return null;
        }
    }

    private static Image loadImage(String filename) {
        try {
            return ImageIO.read(new File(filename));
        }
        catch (IOException ex) {
            Logger.getLogger(MotorManagementFrame.class.getName()).log(Level.SEVERE, null, ex);
            return null;
        }
    }


//     private final Needle rpmNeedle = new PolygonNeedle(50);

}
