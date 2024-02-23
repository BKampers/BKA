/*
** © Bart Kampers
*/
package bka.demo.graphs;

import bka.awt.*;
import bka.demo.graphs.history.*;
import bka.swing.popup.*;
import java.awt.*;
import java.util.*;
import java.util.function.*;
import javax.swing.*;

public class GraphEditorDemo extends javax.swing.JFrame {

    public GraphEditorDemo() {
        initComponents();
        canvas.getDrawHistory().addListener(this::updateHistoryList);
        historyList.addMouseListener(new HistoryListMouseAdapter());
        historyList.addKeyListener(new HistoryListKeyAdapter());
    }

    private void updateHistoryList(DrawHistory history) {
        historyListModel.removeAllElements();
        history.getMutattions().forEach(mutation -> historyListModel.addElement(getBundleText(mutation.getBundleKey())));
        if (history.getIndex() > 0) {
            int index = history.getIndex() - 1;
            historyList.setSelectedIndex(index);
            Rectangle selection = historyList.getCellBounds(index, index);
            historyScrollPane.getViewport().scrollRectToVisible(selection);
            historyScrollPane.getVerticalScrollBar().setValue(selection.y);
        }
    }

    /**
     * This method is called from within the constructor to initialize the form. WARNING: Do NOT modify this code. The content of this method is
     * always regenerated by the Form Editor.
     */
    @SuppressWarnings("unchecked")
    // <editor-fold defaultstate="collapsed" desc="Generated Code">//GEN-BEGIN:initComponents
    private void initComponents() {

        edgeSelector = new javax.swing.ButtonGroup();
        edgeMenu = new javax.swing.JMenu();
        directedCheckBoxMenuItem = new javax.swing.JCheckBoxMenuItem();
        javax.swing.JScrollPane graphScrollPane = new javax.swing.JScrollPane();
        graphPanel = new GraphPanel();
        historyPanel = new javax.swing.JPanel();
        historyScrollPane = new javax.swing.JScrollPane();
        historyList = new javax.swing.JList<>();
        historyList.setCellRenderer(new HistoryListCellRenderer());
        elementPanel = new javax.swing.JPanel();
        undirectedEdgeRadioButton = new javax.swing.JRadioButton();
        directedEdgeRadioButton = new javax.swing.JRadioButton();

        edgeMenu.setText("jMenu1");

        directedCheckBoxMenuItem.setSelected(true);
        directedCheckBoxMenuItem.setText("jCheckBoxMenuItem1");
        edgeMenu.add(directedCheckBoxMenuItem);

        setDefaultCloseOperation(javax.swing.WindowConstants.EXIT_ON_CLOSE);
        setTitle(BUNDLE.getString("ApplicationTitle"));

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
            public void keyPressed(java.awt.event.KeyEvent evt) {
                graphPanelKeyPressed(evt);
            }
            public void keyReleased(java.awt.event.KeyEvent evt) {
                graphPanelKeyReleased(evt);
            }
        });

        javax.swing.GroupLayout graphPanelLayout = new javax.swing.GroupLayout(graphPanel);
        graphPanel.setLayout(graphPanelLayout);
        graphPanelLayout.setHorizontalGroup(
            graphPanelLayout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
            .addGap(0, 444, Short.MAX_VALUE)
        );
        graphPanelLayout.setVerticalGroup(
            graphPanelLayout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
            .addGap(0, 413, Short.MAX_VALUE)
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

        edgeSelector.add(undirectedEdgeRadioButton);
        undirectedEdgeRadioButton.setSelected(true);
        undirectedEdgeRadioButton.setText(BUNDLE.getString("UndirectedEdge"));

        edgeSelector.add(directedEdgeRadioButton);
        directedEdgeRadioButton.setText(BUNDLE.getString("DirectedEdge"));

        javax.swing.GroupLayout elementPanelLayout = new javax.swing.GroupLayout(elementPanel);
        elementPanel.setLayout(elementPanelLayout);
        elementPanelLayout.setHorizontalGroup(
            elementPanelLayout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
            .addGroup(javax.swing.GroupLayout.Alignment.TRAILING, elementPanelLayout.createSequentialGroup()
                .addContainerGap(javax.swing.GroupLayout.DEFAULT_SIZE, Short.MAX_VALUE)
                .addGroup(elementPanelLayout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
                    .addComponent(directedEdgeRadioButton)
                    .addComponent(undirectedEdgeRadioButton))
                .addContainerGap())
        );
        elementPanelLayout.setVerticalGroup(
            elementPanelLayout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
            .addGroup(elementPanelLayout.createSequentialGroup()
                .addGap(122, 122, 122)
                .addComponent(undirectedEdgeRadioButton)
                .addGap(18, 18, 18)
                .addComponent(directedEdgeRadioButton, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, Short.MAX_VALUE)
                .addGap(246, 246, 246))
        );

        getContentPane().add(elementPanel, java.awt.BorderLayout.WEST);

        pack();
    }// </editor-fold>//GEN-END:initComponents

    private void graphPanelMouseClicked(java.awt.event.MouseEvent evt) {//GEN-FIRST:event_graphPanelMouseClicked
        updateGraphPanel(canvas.handleMouseClicked(evt));
    }//GEN-LAST:event_graphPanelMouseClicked

    private void graphPanelMouseDragged(java.awt.event.MouseEvent evt) {//GEN-FIRST:event_graphPanelMouseDragged
        updateGraphPanel(canvas.handleMouseDragged(evt));
    }//GEN-LAST:event_graphPanelMouseDragged

    private void graphPanelMousePressed(java.awt.event.MouseEvent evt) {//GEN-FIRST:event_graphPanelMousePressed
        updateGraphPanel(canvas.handleMousePressed(evt));
    }//GEN-LAST:event_graphPanelMousePressed

    private void graphPanelMouseReleased(java.awt.event.MouseEvent evt) {//GEN-FIRST:event_graphPanelMouseReleased
        updateGraphPanel(canvas.handleMouseReleased(evt));
        graphPanel.requestFocus();
    }//GEN-LAST:event_graphPanelMouseReleased

    private void graphPanelMouseMoved(java.awt.event.MouseEvent evt) {//GEN-FIRST:event_graphPanelMouseMoved
        updateGraphPanel(canvas.handleMouseMoved(evt));
    }//GEN-LAST:event_graphPanelMouseMoved

    private void graphPanelKeyReleased(java.awt.event.KeyEvent evt) {//GEN-FIRST:event_graphPanelKeyReleased
        updateGraphPanel(canvas.handleKeyReleased(evt));
    }//GEN-LAST:event_graphPanelKeyReleased

    private void graphPanelKeyPressed(java.awt.event.KeyEvent evt) {//GEN-FIRST:event_graphPanelKeyPressed
        updateGraphPanel(canvas.handleKeyPressed(evt));
    }//GEN-LAST:event_graphPanelKeyPressed

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

    private static String getBundleText(String key) {
        if (!BUNDLE.containsKey(key)) {
            return "<html><i>" + key + "</i></html>";
        }
        return BUNDLE.getString(key);
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

    private GraphCanvas getCanvas() {
        return canvas;
    }


    private final GraphCanvas canvas = new GraphCanvas(new ApplicationContext() {

        @Override
        public void showEdgeMenu(EdgeRenderer edge, Point location) {
            JPopupMenu menu = new JPopupMenu();
            JCheckBoxMenuItem directedMenuItem = new JCheckBoxMenuItem(getBundleText("Directed"), edge.isDirected());
            directedMenuItem.addActionListener(evt -> getCanvas().setDirected(edge, directedMenuItem.getState()));
            menu.add(directedMenuItem, edge.isDirected());
            menu.show(graphPanel, location.x, location.y);
        }

        @Override
        public void editString(String input, Point location, Consumer<String> onApply) {
            PopupControl.show(
                graphPanel,
                new TextFieldPopupModel(
                    new Rectangle(location.x - POPUP_WIDTH / 2, location.y - POPUP_HEIGHT / 2, POPUP_WIDTH, POPUP_HEIGHT),
                    input,
                    onApply)
            );
        }

        @Override
        public void requestUpdate(ComponentUpdate update) {
            updateGraphPanel(update);
        }

        @Override
        public EdgeRenderer createEdgeRenderer(VertexRenderer origin) {
            EdgeRenderer edgeRenderer = new EdgeRenderer(origin);
            edgeRenderer.setDirected(directedEdgeRadioButton.isSelected());
            return edgeRenderer;
        }

        private static final int POPUP_WIDTH = 50;
        private static final int POPUP_HEIGHT = 20;

    });


    // Variables declaration - do not modify//GEN-BEGIN:variables
    private javax.swing.JCheckBoxMenuItem directedCheckBoxMenuItem;
    private javax.swing.JRadioButton directedEdgeRadioButton;
    private javax.swing.JMenu edgeMenu;
    private javax.swing.ButtonGroup edgeSelector;
    private javax.swing.JPanel elementPanel;
    private javax.swing.JPanel graphPanel;
    private javax.swing.JList<String> historyList;
    private javax.swing.JPanel historyPanel;
    private javax.swing.JScrollPane historyScrollPane;
    private javax.swing.JRadioButton undirectedEdgeRadioButton;
    // End of variables declaration//GEN-END:variables

    private final javax.swing.DefaultListModel<String> historyListModel = new javax.swing.DefaultListModel<>();

    private static final ResourceBundle BUNDLE = ResourceBundle.getBundle("GraphEditor");

}