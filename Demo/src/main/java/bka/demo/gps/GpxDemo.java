/*
** © Bart Kampers
** This code may not be used for any purpose that harms humans (including
** exploitation and discrimination), humanity, the environment or the
** universe.
*/
package bka.demo.gps;

import bka.text.parser.gpx.GpxParser;
import gpx.Gpx;
import java.awt.*;
import java.io.*;
import java.nio.file.*;
import java.util.*;
import java.util.logging.*;
import javax.swing.*;
import javax.swing.filechooser.FileNameExtensionFilter;
import javax.xml.parsers.*;
import org.xml.sax.*;


/**
 * Swing demo that opens a GPX file and draws its tracks and waypoints.
 */
public final class GpxDemo extends JFrame {

    public GpxDemo() {
        super("GPX Viewer");
        setDefaultCloseOperation(WindowConstants.EXIT_ON_CLOSE);
        setMinimumSize(new Dimension(1000, 600));
        canvas = new GpxCanvas();
        layersPanel = new GpxLayersPanel(canvas);
        add(createToolbar(), BorderLayout.NORTH);
        add(layersPanel, BorderLayout.WEST);
        add(canvas, BorderLayout.CENTER);
        pack();
        setLocationRelativeTo(null);
    }

    public static void main(String[] args) {
        setLookAndFeel("Nimbus");
        EventQueue.invokeLater(() -> new GpxDemo().setVisible(true));
    }

    private JComponent createToolbar() {
        JPanel toolbar = new JPanel(new FlowLayout(FlowLayout.LEFT));
        JButton openButton = new JButton("Open GPX…");
        openButton.addActionListener(event -> selectGpxFile());
        fileLabel = new JLabel("No file selected");
        toolbar.add(openButton);
        toolbar.add(fileLabel);
        return toolbar;
    }

    private void selectGpxFile() {
        JFileChooser chooser = new JFileChooser();
        chooser.setFileFilter(new FileNameExtensionFilter("GPX files", "gpx"));
        if (lastDirectory != null) {
            chooser.setCurrentDirectory(lastDirectory.toFile());
        }
        if (chooser.showOpenDialog(this) == JFileChooser.APPROVE_OPTION) {
            loadGpxFile(chooser);
        }
    }

    private void loadGpxFile(JFileChooser chooser) throws HeadlessException {
        Path path = chooser.getSelectedFile().toPath();
        lastDirectory = path.getParent();
        try {
            Gpx gpx = new GpxParser().parse(path);
            canvas.setGpx(gpx);
            layersPanel.setGpx(gpx);
            fileLabel.setText(path.getFileName().toString());
            setTitle("GPX Viewer — " + path.getFileName());
        }
        catch (IOException | SAXException | ParserConfigurationException ex) {
            JOptionPane.showMessageDialog(this, ex.getMessage(), "Failed to open GPX file", JOptionPane.ERROR_MESSAGE);
        }
    }

    private static void setLookAndFeel(String name) {
        Arrays.stream(UIManager.getInstalledLookAndFeels())
            .filter(info -> name.equals(info.getName()))
            .findAny()
            .ifPresentOrElse(
                GpxDemo::setLookAndFeel, 
                lookAndFeelNotPresent(name));
    }

    private static Runnable lookAndFeelNotPresent(String name) {
        return () -> getLogger().log(Level.WARNING, "Look and feel ''{0}'' is not present", name);
    }
    
    private static void setLookAndFeel(UIManager.LookAndFeelInfo info) {
        try {
            UIManager.setLookAndFeel(info.getClassName());
        }
        catch (ReflectiveOperationException | UnsupportedLookAndFeelException ex) {
            getLogger().log(Level.WARNING, "Could not set look and feel", ex);
        }
    }

    private static Logger getLogger() {
        return Logger.getLogger(GpxDemo.class.getName());
    }

    private final GpxCanvas canvas;
    private final GpxLayersPanel layersPanel;
    private JLabel fileLabel;
    private Path lastDirectory;

}
