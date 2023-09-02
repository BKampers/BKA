/*
** © Bart Kampers
*/
package bka.demo.graphs;

import bka.awt.*;

public class GraphEditorDemo extends javax.swing.JFrame {

    public GraphEditorDemo() {
        initComponents();
        canvas.getDrawHistory().addListener(this::updateHistoryList);
        historyList.addMouseListener(new HistoryListMouseAdapter());
        historyList.addKeyListener(new HistoryListKeyAdapter());
    }

    private void updateHistoryList(DrawHistory history) {
        historyListModel.removeAllElements();
        history.getMutattions().forEach(this::displayMutation);
        if (history.getIndex() > 0) {
            int index = history.getIndex() - 1;
            historyList.setSelectedIndex(index);
            java.awt.Rectangle selection = historyList.getCellBounds(index, index);
            historyScrollPane.getViewport().scrollRectToVisible(selection);
            historyScrollPane.getVerticalScrollBar().setValue(selection.y);
        }
    }

    private void displayMutation(Mutation mutation) {
        historyListModel.addElement(switch (mutation.getType()) {
            case INSERTION ->
                insertionDisplayText(mutation);
            case DELETION ->
                deletionDisplayText(mutation);
            case RELOCATION ->
                relocationDisplayText(mutation);
            case SHAPE_CHANGE ->
                shapeChangeDisplayText(mutation);
            default ->
                throw new IllegalStateException(mutation.getType().name());
        });
    }

    private static String insertionDisplayText(Mutation mutation) {
        return (mutation.getEdges().isEmpty()) ? "Vertex added" : "Edge added";
    }

    private static String deletionDisplayText(Mutation mutation) {
        if (mutation.getVertices().size() == 1) {
            return "Vertex deleted";
        }
        if (mutation.getEdges().size() == 1) {
            return "Edge deleted";
        }
        return "Selection deleted";
    }

    private static String relocationDisplayText(Mutation mutation) {
        return (mutation.getVertices().size() == 1) ? "Vertex relocated" : "Selection relocated";
    }

    private static String shapeChangeDisplayText(Mutation mutation) {
        return (mutation.getVertices().isEmpty()) ? "Edge transformed" : "Vertex resized";
    }

    /**
     * This method is called from within the constructor to initialize the form. WARNING: Do NOT modify this code. The content of this method is
     * always regenerated by the Form Editor.
     */
    @SuppressWarnings("unchecked")
    // <editor-fold defaultstate="collapsed" desc="Generated Code">//GEN-BEGIN:initComponents
    private void initComponents() {

        javax.swing.JScrollPane graphScrollPane = new javax.swing.JScrollPane();
        graphPanel = new GraphPanel();
        historyPanel = new javax.swing.JPanel();
        historyScrollPane = new javax.swing.JScrollPane();
        historyList = new javax.swing.JList<>();
        historyList.setCellRenderer(new HistoryListCellRenderer());

        setDefaultCloseOperation(javax.swing.WindowConstants.EXIT_ON_CLOSE);
        setTitle("Graph editor");

        graphScrollPane.setHorizontalScrollBarPolicy(javax.swing.ScrollPaneConstants.HORIZONTAL_SCROLLBAR_ALWAYS);
        graphScrollPane.setVerticalScrollBarPolicy(javax.swing.ScrollPaneConstants.VERTICAL_SCROLLBAR_ALWAYS);

        graphPanel.addMouseMotionListener(new java.awt.event.MouseMotionAdapter() {
            public void mouseDragged(java.awt.event.MouseEvent evt) {
                graphPanelMouseDragged(evt);
            }
            public void mouseMoved(java.awt.event.MouseEvent evt) {
                graphPanelMouseMoved(evt);
            }
        });
        graphPanel.addMouseListener(new java.awt.event.MouseAdapter() {
            public void mouseClicked(java.awt.event.MouseEvent evt) {
                graphPanelMouseClicked(evt);
            }
            public void mousePressed(java.awt.event.MouseEvent evt) {
                graphPanelMousePressed(evt);
            }
            public void mouseReleased(java.awt.event.MouseEvent evt) {
                graphPanelMouseReleased(evt);
            }
        });
        graphPanel.addKeyListener(new java.awt.event.KeyAdapter() {
            public void keyReleased(java.awt.event.KeyEvent evt) {
                graphPanelKeyReleased(evt);
            }
        });

        javax.swing.GroupLayout graphPanelLayout = new javax.swing.GroupLayout(graphPanel);
        graphPanel.setLayout(graphPanelLayout);
        graphPanelLayout.setHorizontalGroup(
            graphPanelLayout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
            .addGap(0, 400, Short.MAX_VALUE)
        );
        graphPanelLayout.setVerticalGroup(
            graphPanelLayout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
            .addGap(0, 381, Short.MAX_VALUE)
        );

        graphScrollPane.setViewportView(graphPanel);

        getContentPane().add(graphScrollPane, java.awt.BorderLayout.CENTER);

        historyPanel.setLayout(new javax.swing.BoxLayout(historyPanel, javax.swing.BoxLayout.LINE_AXIS));

        historyScrollPane.setHorizontalScrollBarPolicy(javax.swing.ScrollPaneConstants.HORIZONTAL_SCROLLBAR_NEVER);
        historyScrollPane.setMinimumSize(new java.awt.Dimension(150, 400));
        historyScrollPane.setPreferredSize(new java.awt.Dimension(150, 400));

        historyList.setModel(historyListModel);
        historyList.setSelectionMode(javax.swing.ListSelectionModel.SINGLE_SELECTION);
        historyScrollPane.setViewportView(historyList);

        historyPanel.add(historyScrollPane);

        getContentPane().add(historyPanel, java.awt.BorderLayout.EAST);

        pack();
    }// </editor-fold>//GEN-END:initComponents

    private void graphPanelMouseClicked(java.awt.event.MouseEvent evt) {//GEN-FIRST:event_graphPanelMouseClicked
        updateGraphPanel(canvas.handleMouseClicked(evt));
        graphPanel.requestFocus();
    }//GEN-LAST:event_graphPanelMouseClicked

    private void graphPanelMouseDragged(java.awt.event.MouseEvent evt) {//GEN-FIRST:event_graphPanelMouseDragged
        updateGraphPanel(canvas.handleMouseDragged(evt));
    }//GEN-LAST:event_graphPanelMouseDragged

    private void graphPanelMousePressed(java.awt.event.MouseEvent evt) {//GEN-FIRST:event_graphPanelMousePressed
        updateGraphPanel(canvas.handleMousePressed(evt));
    }//GEN-LAST:event_graphPanelMousePressed

    private void graphPanelMouseReleased(java.awt.event.MouseEvent evt) {//GEN-FIRST:event_graphPanelMouseReleased
        updateGraphPanel(canvas.handleMouseReleased(evt));
    }//GEN-LAST:event_graphPanelMouseReleased

    private void graphPanelMouseMoved(java.awt.event.MouseEvent evt) {//GEN-FIRST:event_graphPanelMouseMoved
        updateGraphPanel(canvas.handleMouseMoved(evt));
    }//GEN-LAST:event_graphPanelMouseMoved

    private void graphPanelKeyReleased(java.awt.event.KeyEvent evt) {//GEN-FIRST:event_graphPanelKeyReleased
        updateGraphPanel(canvas.handleKeyReleased(evt));
    }//GEN-LAST:event_graphPanelKeyReleased

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
        catch (ReflectiveOperationException | javax.swing.UnsupportedLookAndFeelException ex) {
            java.util.logging.Logger.getLogger(GraphEditorDemo.class.getName()).log(java.util.logging.Level.SEVERE, null, ex);
        }
        //</editor-fold>
        //</editor-fold>

        /* Create and display the form */
        java.awt.EventQueue.invokeLater(() -> {
            new GraphEditorDemo().setVisible(true);
        });
    }

    private void updateGraphPanel(ComponentUpdate update){
        if (java.awt.Cursor.DEFAULT_CURSOR <= update.getCursorType() && update.getCursorType() <= java.awt.Cursor.MOVE_CURSOR) {
            setGraphPanelCursor(update.getCursorType());
        }
        if (update.needsRepeaint()) {
            graphPanel.repaint();
        }
    }

    private void setGraphPanelCursor(int type) {
        if (graphPanel.getCursor().getType() != type) {
            graphPanel.setCursor(new java.awt.Cursor(type));
        }
    }


    private class GraphPanel extends javax.swing.JPanel {

        @Override
        public void paint(java.awt.Graphics graphics) {
            super.paint(graphics);
            canvas.paint((java.awt.Graphics2D) graphics);
        }
    }


    private class HistoryListCellRenderer extends javax.swing.DefaultListCellRenderer {

        @Override
        public java.awt.Component getListCellRendererComponent(javax.swing.JList list, Object value, int index, boolean isSelected, boolean cellHasFocus) {
            java.awt.Component renderer = super.getListCellRendererComponent(list, value, index, isSelected, cellHasFocus);
            if (isSelected) {
                renderer.setForeground(java.awt.Color.BLACK);
            }
            else if (index < canvas.getDrawHistory().getIndex()) {
                renderer.setForeground(java.awt.Color.BLACK);
            }
            else {
                renderer.setForeground(java.awt.Color.GRAY);
            }
            return renderer;
        }
    }


    private class HistoryListMouseAdapter extends java.awt.event.MouseAdapter {

        @Override
        public void mouseReleased(java.awt.event.MouseEvent event) {
            int target = historyList.getSelectedIndex() + 1;
            if (canvas.getDrawHistory().getIndex() != target) {
                while (canvas.getDrawHistory().getIndex() < target) {
                    canvas.getDrawHistory().redo();
                }
                while (canvas.getDrawHistory().getIndex() > target) {
                    canvas.getDrawHistory().undo();
                }
                graphPanel.repaint();
            }
        }
    }


    private class HistoryListKeyAdapter extends java.awt.event.KeyAdapter {

        @Override
        public void keyReleased(java.awt.event.KeyEvent event) {
            if (Keyboard.getInstance().isUndo(event)) {
                canvas.getDrawHistory().undo();
                graphPanel.repaint();
            }
            else if (Keyboard.getInstance().isRedo(event)) {
                canvas.getDrawHistory().redo();
                graphPanel.repaint();
            }
        }
    }


    // Variables declaration - do not modify//GEN-BEGIN:variables
    private javax.swing.JPanel graphPanel;
    private javax.swing.JList<String> historyList;
    private javax.swing.JPanel historyPanel;
    private javax.swing.JScrollPane historyScrollPane;
    // End of variables declaration//GEN-END:variables

    private final GraphCanvas canvas = new GraphCanvas();
    private final javax.swing.DefaultListModel<String> historyListModel = new javax.swing.DefaultListModel<>();

}
