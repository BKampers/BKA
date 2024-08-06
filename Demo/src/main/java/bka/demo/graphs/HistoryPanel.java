/*
** © Bart Kampers
*/
package bka.demo.graphs;

import bka.awt.*;
import bka.awt.graphcanvas.*;
import bka.awt.graphcanvas.history.*;
import java.awt.*;
import java.awt.event.*;
import java.util.*;
import java.util.function.*;
import javax.swing.*;


public class HistoryPanel extends JPanel {

    /**
     * @param graphPanel
     * @param canvas
     * @param bundle
     */
    public HistoryPanel(JPanel graphPanel, GraphCanvas canvas, UnaryOperator<String> bundle) {
        this.graphPanel = Objects.requireNonNull(graphPanel);
        this.canvas = Objects.requireNonNull(canvas);
        this.bundle = Objects.requireNonNull(bundle);
        initComponents();
        canvas.getDrawHistory().addListener(this::updateHistoryList);
        InputMap im = list.getInputMap();
        Arrays.stream(im.allKeys()).forEach(key -> im.put(key, "none"));
        list.addMouseListener(new ListMouseAdapter());
        list.addKeyListener(new ListKeyAdapter());
        

    }

    private void updateHistoryList(DrawHistory history) {
        model.removeAllElements();
        history.getMutattions().forEach(mutation -> model.addElement(bundle.apply(mutation.getBundleKey())));
        if (history.getIndex() > 0) {
            int index = history.getIndex() - 1;
            list.setSelectedIndex(index);
            Rectangle selection = list.getCellBounds(index, index);
            scrollPane.getViewport().scrollRectToVisible(selection);
            scrollPane.getVerticalScrollBar().setValue(selection.y);
        }
    }

    
    /**
     * This method is called from within the constructor to initialize the form. WARNING: Do NOT modify this code. The content of this method is always
     * regenerated by the Form Editor.
     */
    @SuppressWarnings("unchecked")
    // <editor-fold defaultstate="collapsed" desc="Generated Code">//GEN-BEGIN:initComponents
    private void initComponents() {

        scrollPane = new javax.swing.JScrollPane();
        list = new javax.swing.JList<>();
        list.setCellRenderer(new CellRenderer());

        setLayout(new javax.swing.BoxLayout(this, javax.swing.BoxLayout.LINE_AXIS));

        list.setModel(model);
        list.setSelectionMode(javax.swing.ListSelectionModel.SINGLE_SELECTION);
        scrollPane.setViewportView(list);

        add(scrollPane);
    }// </editor-fold>//GEN-END:initComponents

    private void select(int target) {
        DrawHistory history = canvas.getDrawHistory();
        if (history.getIndex() != target) {
            while (history.getIndex() < target && history.canRedo()) {
                history.redo();
            }
            while (history.getIndex() > target && history.canUndo()) {
                history.undo();
            }
            graphPanel.repaint();
        }
    }

    
    private class CellRenderer extends DefaultListCellRenderer {

        @Override
        public Component getListCellRendererComponent(javax.swing.JList list, Object value, int index, boolean isSelected, boolean cellHasFocus) {
            Component renderer = super.getListCellRendererComponent(list, value, index, isSelected, cellHasFocus);
            if (isSelected) {
                renderer.setForeground(Color.BLACK);
            }
            else if (index < canvas.getDrawHistory().getIndex()) {
                renderer.setForeground(Color.BLACK);
            }
            else {
                renderer.setForeground(Color.GRAY);
            }
            return renderer;
        }
    }


    private class ListMouseAdapter extends MouseAdapter {

        @Override
        public void mouseReleased(MouseEvent event) {
            select(list.getSelectedIndex() + 1);
        }

    }


    private class ListKeyAdapter extends KeyAdapter {

        @Override
        public void keyReleased(KeyEvent event) {
            Keyboard keyboard = Keyboard.getInstance();
            if (keyboard.isUndo(event) || keyboard.isNavigateUp(event)) {
                canvas.getDrawHistory().undo();
                graphPanel.repaint();
            }
            else if (keyboard.isRedo(event) || keyboard.isNavigateDown(event)) {
                canvas.getDrawHistory().redo();
                graphPanel.repaint();
            }
            else if (keyboard.isNavigatePageUp(event)) {
                select(list.getFirstVisibleIndex());
            }
            else if (keyboard.isNavigatePageDown(event)) {
                select(list.getLastVisibleIndex() + 1);
            }
            else if (keyboard.isNavigateHome(event)) {
                select(0);
            }
            else if (keyboard.isNavigateEnd(event)) {
                select(model.getSize());
            }
        }
    }
    
    
    // Variables declaration - do not modify//GEN-BEGIN:variables
    private javax.swing.JList<String> list;
    private javax.swing.JScrollPane scrollPane;
    // End of variables declaration//GEN-END:variables

    private final JPanel graphPanel;
    private final GraphCanvas canvas;
    private final UnaryOperator<String> bundle;
    
    private final DefaultListModel<String> model = new DefaultListModel<>();
    
}
