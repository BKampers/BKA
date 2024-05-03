/*
** © Bart Kampers
*/
package bka.demo.graphs;

import bka.awt.*;
import bka.awt.graphcanvas.*;
import bka.awt.graphcanvas.history.*;
import bka.swing.popup.*;
import java.awt.*;
import java.awt.event.*;
import java.awt.image.*;
import java.util.List;
import java.util.*;
import java.util.function.*;
import java.util.stream.*;
import javax.swing.*;

public class GraphEditorDemo extends JFrame {

    public GraphEditorDemo() {
        initComponents();
        Map<Object, Stroke> defaultStrokes = Map.of(VertexPaintable.BORDER_STROKE_KEY, new BasicStroke());
        defaultStrokes.entrySet().stream().collect(Collectors.toCollection(ArrayList::new));
        populateVertexSelectorPanel(List.of(
            new VertexFactory(RoundVertexPaintable::new, defaultStrokes, paints(Color.BLACK, Color.BLACK)),
            new VertexFactory(SquareVertexPaintable::new, defaultStrokes, paints(Color.BLACK, Color.WHITE))
        ));
        canvas.getDrawHistory().addListener(this::updateHistoryList);
        historyList.addMouseListener(new HistoryListMouseAdapter());
        historyList.addKeyListener(new HistoryListKeyAdapter());
    }

    private static Map<Object, Paint> paints(Paint borderPaint, Paint fillPaint) {
        return Map.of(
            VertexPaintable.BORDER_PAINT_KEY, borderPaint,
            VertexPaintable.FILL_PAINT_KEY, fillPaint);
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
        javax.swing.JScrollPane graphScrollPane = new javax.swing.JScrollPane();
        graphPanel = new GraphPanel();
        historyPanel = new javax.swing.JPanel();
        historyScrollPane = new javax.swing.JScrollPane();
        historyList = new javax.swing.JList<>();
        historyList.setCellRenderer(new HistoryListCellRenderer());
        componentPanel = new javax.swing.JPanel();
        vertexSelectorPanel = new javax.swing.JPanel();
        undirectedEdgeRadioButton = new javax.swing.JRadioButton();
        directedEdgeRadioButton = new javax.swing.JRadioButton();

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
            .addGap(0, 442, Short.MAX_VALUE)
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

        vertexSelectorPanel.setLocation(new java.awt.Point(0, 5));
        vertexSelectorPanel.setLayout(new javax.swing.BoxLayout(vertexSelectorPanel, javax.swing.BoxLayout.Y_AXIS));

        edgeSelector.add(undirectedEdgeRadioButton);
        undirectedEdgeRadioButton.setSelected(true);
        undirectedEdgeRadioButton.setText(BUNDLE.getString("UndirectedEdge"));

        edgeSelector.add(directedEdgeRadioButton);
        directedEdgeRadioButton.setText(BUNDLE.getString("DirectedEdge"));

        javax.swing.GroupLayout componentPanelLayout = new javax.swing.GroupLayout(componentPanel);
        componentPanel.setLayout(componentPanelLayout);
        componentPanelLayout.setHorizontalGroup(
            componentPanelLayout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
            .addGroup(componentPanelLayout.createSequentialGroup()
                .addContainerGap(30, Short.MAX_VALUE)
                .addGroup(componentPanelLayout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
                    .addComponent(directedEdgeRadioButton)
                    .addComponent(undirectedEdgeRadioButton))
                .addContainerGap())
            .addGroup(componentPanelLayout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
                .addGroup(javax.swing.GroupLayout.Alignment.TRAILING, componentPanelLayout.createSequentialGroup()
                    .addContainerGap(76, Short.MAX_VALUE)
                    .addComponent(vertexSelectorPanel, javax.swing.GroupLayout.PREFERRED_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.PREFERRED_SIZE)
                    .addContainerGap(76, Short.MAX_VALUE)))
        );
        componentPanelLayout.setVerticalGroup(
            componentPanelLayout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
            .addGroup(componentPanelLayout.createSequentialGroup()
                .addGap(151, 151, 151)
                .addComponent(undirectedEdgeRadioButton)
                .addGap(18, 18, 18)
                .addComponent(directedEdgeRadioButton, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, Short.MAX_VALUE)
                .addGap(246, 246, 246))
            .addGroup(componentPanelLayout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
                .addGroup(javax.swing.GroupLayout.Alignment.TRAILING, componentPanelLayout.createSequentialGroup()
                    .addContainerGap(236, Short.MAX_VALUE)
                    .addComponent(vertexSelectorPanel, javax.swing.GroupLayout.PREFERRED_SIZE, 171, javax.swing.GroupLayout.PREFERRED_SIZE)
                    .addContainerGap(54, Short.MAX_VALUE)))
        );

        getContentPane().add(componentPanel, java.awt.BorderLayout.WEST);

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

    private void populateVertexSelectorPanel(List<VertexFactory> factories) {
        factories.forEach(factory -> {
            JToggleButton button = new JToggleButton();
            vertexButtons.put(button, factory);
            button.setIcon(createIcon(factory.getDefaultInstance()));
            button.addMouseListener(new VertexButtonMouseAdapter(factory.getDefaultInstance()));
            vertexSelectorPanel.add(button);
        });
    }

    private void ensureSingleSelection(MouseEvent event) {
        if (((JToggleButton) event.getSource()).isSelected()) {
            vertexButtons.keySet().stream()
                .filter(vertexButton -> !vertexButton.equals(event.getSource()))
                .forEach(vertexButton -> vertexButton.setSelected(false));
        }
    }

    private void showVertexContextMenu(MouseEvent evt, Paintable paintable) {
        JPopupMenu menu = new JPopupMenu();
        addColorMenuItems(paintable, evt.getPoint(), menu, (key, color) -> {
            paintable.setPaint(key, color);
            ((JToggleButton) evt.getSource()).setIcon(createIcon(paintable));
        });
        if (menu.getComponentCount() > 0) {
            menu.show((Component) evt.getSource(), evt.getX(), evt.getY());
        }
    }

    /**
     * @param args the command line arguments
     */
    public static void main(String args[]) {
        SelectNimbusLookAndFeel();
        /* Create and display the form */
        EventQueue.invokeLater(() -> new GraphEditorDemo().setVisible(true));
    }

    private static void SelectNimbusLookAndFeel() {
        try {
            for (javax.swing.UIManager.LookAndFeelInfo info : javax.swing.UIManager.getInstalledLookAndFeels()) {
                if ("Nimbus".equals(info.getName())) {
                    javax.swing.UIManager.setLookAndFeel(info.getClassName());
                    return;
                }
            }
        }
        catch (ReflectiveOperationException | javax.swing.UnsupportedLookAndFeelException ex) {
            java.util.logging.Logger.getLogger(GraphEditorDemo.class.getName()).log(java.util.logging.Level.SEVERE, null, ex);
        }
    }

    private void updateGraphPanel(CanvasUpdate update){
        if (Cursor.DEFAULT_CURSOR <= update.getCursorType() && update.getCursorType() <= Cursor.MOVE_CURSOR) {
            setGraphPanelCursor(update.getCursorType());
        }
        if (update.needsRepeaint()) {
            graphPanel.repaint();
        }
    }

    private void setGraphPanelCursor(int type) {
        if (graphPanel.getCursor().getType() != type) {
            graphPanel.setCursor(new Cursor(type));
        }
    }

    private void addColorMenuItems(Paintable paintable, Point location, JPopupMenu menu, BiConsumer<Object, Color> onApply) {
        paintable.getPaintKeys().forEach(paintKey -> {
            JMenuItem paintItem = new JMenuItem(getBundleText(paintKey.toString()));
            paintItem.addActionListener(evt -> pickColor(location, paintable, paintKey, onApply));
            menu.add(paintItem);
        });
    }

    private void pickColor(Point location, Paintable paintable, Object key, BiConsumer<Object, Color> onApply) {
        PopupControl.show(
            graphPanel,
            new ColorChooserPopupModel(
                graphPanel,
                colorChooserBounds(location),
                castToColor(paintable.getPaint(key)),
                color -> onApply.accept(key, color)));
    }

    private static String getBundleText(String key) {
        if (!BUNDLE.containsKey(key)) {
            return "<html><i>" + key + "</i></html>";
        }
        return BUNDLE.getString(key);
    }


    private class VertexFactory {

        public VertexFactory(Function<Dimension, VertexPaintable> newInstance, Map<Object, Stroke> defaultStrokes, Map<Object, Paint> defaultPaints) {
            this.newInstance = newInstance;
            defaultInstance = newInstance.apply(VERTEX_ICON_DIMENSION);
            defaultStrokes.forEach(defaultInstance::setStroke);
            defaultPaints.forEach(defaultInstance::setPaint);
        }

        public VertexPaintable getDefaultInstance() {
            return defaultInstance;
        }

        public VertexPaintable getCopyInstance() {
            VertexPaintable copyInstance = newInstance.apply(VERTEX_ICON_DIMENSION);
            copy(defaultInstance, copyInstance);
            return copyInstance;
        }

        private void copy(Paintable source, Paintable target) {
            copy(source.getPaintKeys(), target::setPaint, source::getPaint);
            copy(source.getStrokeKeys(), target::setStroke, source::getStroke);
        }

        private <T> void copy(Collection<Object> keys, BiConsumer<Object, T> setter, Function<Object, T> getter) {
            keys.forEach(key -> setter.accept(key, getter.apply(key)));
        }

        private final Function<Dimension, VertexPaintable> newInstance;
        private final VertexPaintable defaultInstance;

    }


    private class GraphPanel extends javax.swing.JPanel {

        @Override
        public void paint(Graphics graphics) {
            super.paint(graphics);
            canvas.paint((Graphics2D) graphics);
        }
    }


    private class VertexButtonMouseAdapter extends MouseAdapter {

        VertexButtonMouseAdapter(VertexPaintable paintable) {
            this.paintable = paintable;
        }

        @Override
        public void mouseClicked(MouseEvent event) {
            switch (event.getButton()) {
                case MouseEvent.BUTTON1 ->
                    ensureSingleSelection(event);
                case MouseEvent.BUTTON3 ->
                    showVertexContextMenu(event, paintable);
            }
        }

        private final VertexPaintable paintable;

    }


    private class HistoryListCellRenderer extends javax.swing.DefaultListCellRenderer {

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


    private class HistoryListMouseAdapter extends MouseAdapter {

        @Override
        public void mouseReleased(MouseEvent event) {
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


    private class HistoryListKeyAdapter extends KeyAdapter {

        @Override
        public void keyReleased(KeyEvent event) {
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

    private static Rectangle colorChooserBounds(Point location) {
        return new Rectangle(location.x, location.y, 600, 300);
    }

    private static Color castToColor(Paint paint) {
        if (paint instanceof Color) {
            return (Color) paint;
        }
        return null;
    }

    private static Icon createIcon(Paintable paintable) {
        return createIcon(paintable, null);
    }

    private static Icon createIcon(Paintable paintable, Color highlight) {
        BufferedImage image = new BufferedImage(15, 15, BufferedImage.TYPE_INT_ARGB);
        Graphics2D graphics = image.createGraphics();
        graphics.setRenderingHint(RenderingHints.KEY_ANTIALIASING, RenderingHints.VALUE_ANTIALIAS_ON);
        graphics.translate(image.getWidth() / 2, image.getHeight() / 2);
        paintable.paint(graphics);
        ImageIcon icon = new ImageIcon(image);
        return icon;
    }

    private final GraphCanvas canvas = new GraphCanvas(new ApplicationContext() {

        @Override
        public void showVertexMenu(VertexComponent vertex, Point location) {
            JPopupMenu menu = new JPopupMenu();
            addPaintMenus(vertex, location, menu);
            if (menu.getComponentCount() > 0) {
                menu.show(graphPanel, location.x, location.y);
            }
        }
        
        @Override
        public void showEdgeMenu(EdgeComponent edge, Point location) {
            JPopupMenu menu = new JPopupMenu();
            JCheckBoxMenuItem directedMenuItem = new JCheckBoxMenuItem(getBundleText("Directed"), edge.isDirected());
            directedMenuItem.addActionListener(evt -> getCanvas().setDirected(edge, directedMenuItem.getState()));
            menu.add(directedMenuItem, edge.isDirected());
            if (edge.isDirected()) {
                JMenuItem revertMenuItem = new JMenuItem(getBundleText("Revert"));
                revertMenuItem.addActionListener(evt -> getCanvas().revert(edge));
                menu.add(revertMenuItem);
            }
            addPaintMenus(edge, location, menu);
            menu.show(graphPanel, location.x, location.y);
        }

        private void addPaintMenus(GraphComponent element, Point location, JPopupMenu menu) {
            element.getCustomizablePaintables().forEach(paintable -> addColorMenuItems(paintable, location, menu, (key, newColor) -> getCanvas().changePaint(paintable, key, newColor)));
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
        public void requestUpdate(CanvasUpdate update) {
            updateGraphPanel(update);
        }

        @Override
        public VertexComponent createVertexComponent(Point location) {
            Optional<JToggleButton> vertexButton = vertexButtons.keySet().stream()
                .filter(toggleButton -> toggleButton.isSelected())
                .findAny();
            if (vertexButton.isEmpty()) {
                return null;
            }
            return new VertexComponent(vertexButtons.get(vertexButton.get()).getCopyInstance(), location);
        }

        @Override
        public EdgeComponent createEdgeComponent(VertexComponent origin, VertexComponent terminus) {
            EdgeComponent edgeRenderer = new EdgeComponent(origin, terminus);
            edgeRenderer.setDirected(directedEdgeRadioButton.isSelected());
            return edgeRenderer;
        }

        private static final int POPUP_WIDTH = 50;
        private static final int POPUP_HEIGHT = 20;

    });

    
    // Variables declaration - do not modify//GEN-BEGIN:variables
    private javax.swing.JPanel componentPanel;
    private javax.swing.JRadioButton directedEdgeRadioButton;
    private javax.swing.ButtonGroup edgeSelector;
    private javax.swing.JPanel graphPanel;
    private javax.swing.JList<String> historyList;
    private javax.swing.JPanel historyPanel;
    private javax.swing.JScrollPane historyScrollPane;
    private javax.swing.JRadioButton undirectedEdgeRadioButton;
    private javax.swing.JPanel vertexSelectorPanel;
    // End of variables declaration//GEN-END:variables

    private final Map<JToggleButton, VertexFactory> vertexButtons = new HashMap<>();

    private final javax.swing.DefaultListModel<String> historyListModel = new javax.swing.DefaultListModel<>();

    private static final ResourceBundle BUNDLE = ResourceBundle.getBundle("GraphEditor");

    private static final Dimension VERTEX_ICON_DIMENSION = new Dimension(12, 12);

}
