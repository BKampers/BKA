/*
** © Bart Kampers
** This code may not be used for any purpose that harms humans (including
** exploitation and discrimination), humanity, the environment or the
** universe.
*/
package bka.demo.gps;

import gpx.Gpx;
import gpx.Track;
import gpx.Waypoint;
import java.awt.*;
import java.util.*;
import java.util.List;
import javax.swing.*;
import javax.swing.border.*;


/**
 * Sidebar listing tracks and waypoints with checkboxes to toggle visibility on the canvas.
 * Sections can be collapsed and expanded.
 */
public final class GpxLayersPanel extends JPanel {

    public GpxLayersPanel(GpxCanvas canvas) {
        this.canvas = Objects.requireNonNull(canvas);
        setLayout(new BorderLayout());
        setPreferredSize(new Dimension(240, 700));
        content = new JPanel();
        content.setLayout(new BoxLayout(content, BoxLayout.Y_AXIS));
        content.setBorder(new EmptyBorder(8, 8, 8, 8));
        JScrollPane scrollPane = new JScrollPane(content);
        scrollPane.setBorder(null);
        scrollPane.getVerticalScrollBar().setUnitIncrement(16);
        add(scrollPane, BorderLayout.CENTER);
        showEmptyMessage();
    }

    public void setGpx(Gpx gpx) {
        content.removeAll();
        if (gpx.getTracks().isEmpty() && gpx.getWaypoints().isEmpty()) {
            showEmptyMessage();
        }
        else {
            addSection("Tracks", createTrackCheckboxes(gpx.getTracks()));
            addSection("Waypoints", createWaypointCheckboxes(gpx.getWaypoints()));
        }
        content.revalidate();
        content.repaint();
    }

    private void showEmptyMessage() {
        JLabel label = new JLabel("No layers");
        label.setForeground(Color.GRAY);
        label.setAlignmentX(LEFT_ALIGNMENT);
        content.add(label);
    }

    private void addSection(String title, List<JCheckBox> checkboxes) {
        if (checkboxes.isEmpty()) {
            return;
        }
        JPanel items = new JPanel();
        items.setLayout(new BoxLayout(items, BoxLayout.Y_AXIS));
        items.setAlignmentX(LEFT_ALIGNMENT);
        items.setOpaque(false);
        checkboxes.forEach(checkbox -> {
            checkbox.setAlignmentX(LEFT_ALIGNMENT);
            items.add(checkbox);
        });

        JPanel selectionBar = createSelectionBar(checkboxes);
        selectionBar.setAlignmentX(LEFT_ALIGNMENT);

        JToggleButton header = new JToggleButton(expandedTitle(title), true);
        header.setFont(header.getFont().deriveFont(Font.BOLD));
        header.setHorizontalAlignment(SwingConstants.LEFT);
        header.setBorderPainted(false);
        header.setContentAreaFilled(false);
        header.setFocusPainted(false);
        header.setCursor(Cursor.getPredefinedCursor(Cursor.HAND_CURSOR));
        header.setAlignmentX(LEFT_ALIGNMENT);
        header.setMaximumSize(new Dimension(Integer.MAX_VALUE, header.getPreferredSize().height));
        header.addActionListener(event -> {
            boolean expanded = header.isSelected();
            items.setVisible(expanded);
            selectionBar.setVisible(expanded);
            header.setText(expanded ? expandedTitle(title) : collapsedTitle(title));
            content.revalidate();
            content.repaint();
        });

        content.add(header);
        content.add(selectionBar);
        content.add(Box.createVerticalStrut(4));
        content.add(items);
        content.add(Box.createVerticalStrut(12));
    }

    private JPanel createSelectionBar(List<JCheckBox> checkboxes) {
        JPanel bar = new JPanel(new FlowLayout(FlowLayout.LEFT, 0, 0));
        bar.setOpaque(false);
        JButton selectAll = linkButton("All");
        JButton deselectAll = linkButton("None");
        selectAll.addActionListener(event -> setAllSelected(checkboxes, true));
        deselectAll.addActionListener(event -> setAllSelected(checkboxes, false));
        bar.add(selectAll);
        bar.add(new JLabel(" / "));
        bar.add(deselectAll);
        return bar;
    }

    private static JButton linkButton(String text) {
        JButton button = new JButton(text);
        button.setBorderPainted(false);
        button.setContentAreaFilled(false);
        button.setFocusPainted(false);
        button.setCursor(Cursor.getPredefinedCursor(Cursor.HAND_CURSOR));
        button.setMargin(new Insets(0, 0, 0, 0));
        button.setForeground(new Color(0x1F77B4));
        return button;
    }

    private static void setAllSelected(List<JCheckBox> checkboxes, boolean selected) {
        for (JCheckBox checkbox : checkboxes) {
            if (checkbox.isSelected() != selected) {
                checkbox.doClick();
            }
        }
    }

    private static String expandedTitle(String title) {
        return "▼ " + title;
    }

    private static String collapsedTitle(String title) {
        return "▶ " + title;
    }

    private List<JCheckBox> createTrackCheckboxes(List<Track> tracks) {
        List<JCheckBox> checkboxes = new ArrayList<>();
        for (int i = 0; i < tracks.size(); i++) {
            Track track = tracks.get(i);
            String label = track.getName().orElse("Track " + (i + 1));
            JCheckBox checkbox = new JCheckBox(label, true);
            checkbox.addActionListener(event -> canvas.setTrackVisible(track, checkbox.isSelected()));
            checkboxes.add(checkbox);
        }
        return checkboxes;
    }

    private List<JCheckBox> createWaypointCheckboxes(List<Waypoint> waypoints) {
        List<JCheckBox> checkboxes = new ArrayList<>();
        for (int i = 0; i < waypoints.size(); i++) {
            Waypoint waypoint = waypoints.get(i);
            String label = waypoint.getName().orElse("Waypoint " + (i + 1));
            JCheckBox checkbox = new JCheckBox(label, true);
            checkbox.addActionListener(event -> canvas.setWaypointVisible(waypoint, checkbox.isSelected()));
            checkboxes.add(checkbox);
        }
        return checkboxes;
    }


    private final GpxCanvas canvas;
    private final JPanel content;

}
