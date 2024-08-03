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
        Map<Object, Stroke> defaultStrokes = Map.of(VertexPaintable.BORDER_STROKE_KEY, SOLID_STROKE);
        populateVertexSelectorPanel(List.of(
            new VertexFactory(RoundVertexPaintable::new, defaultStrokes, paints(Color.BLACK, Color.BLACK)),
            new VertexFactory(SquareVertexPaintable::new, defaultStrokes, paints(Color.BLACK, Color.WHITE))
        ));
        DiamondPaintable diamondPaintable = new DiamondPaintable(() -> ICON_MID_LEFT, () -> ICON_MID_RIGHT);
        diamondPaintable.setStroke(DiamondPaintable.DIAMOND_BORDER_STROKE_KEY, SOLID_STROKE);
        diamondPaintable.setPaint(DiamondPaintable.DIAMOND_BORDER_PAINT_KEY, Color.BLACK);
        diamondPaintable.setPaint(DiamondPaintable.DIAMOND_FILL_PAINT_KEY, Color.BLACK);
        populateEdgeSelectorPanel(List.of(
            new EdgeFactory(false),
            new EdgeFactory(true),
            new EdgeFactory(diamondPaintable)));
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

        javax.swing.JScrollPane graphScrollPane = new javax.swing.JScrollPane();
        graphPanel = new GraphPanel();
        historyPanel = new javax.swing.JPanel();
        historyScrollPane = new javax.swing.JScrollPane();
        historyList = new javax.swing.JList<>();
        historyList.setCellRenderer(new HistoryListCellRenderer());
        componentPanel = new javax.swing.JPanel();
        vertexSelectorPanel = new javax.swing.JPanel();
        edgeSelectorPanel = new javax.swing.JPanel();

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
            .addGap(0, 579, Short.MAX_VALUE)
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
        vertexSelectorPanel.setVerifyInputWhenFocusTarget(false);
        vertexSelectorPanel.setLayout(new javax.swing.BoxLayout(vertexSelectorPanel, javax.swing.BoxLayout.Y_AXIS));

        edgeSelectorPanel.setLocation(new java.awt.Point(0, 5));
        edgeSelectorPanel.setVerifyInputWhenFocusTarget(false);
        edgeSelectorPanel.setLayout(new javax.swing.BoxLayout(edgeSelectorPanel, javax.swing.BoxLayout.Y_AXIS));

        javax.swing.GroupLayout componentPanelLayout = new javax.swing.GroupLayout(componentPanel);
        componentPanel.setLayout(componentPanelLayout);
        componentPanelLayout.setHorizontalGroup(
            componentPanelLayout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
            .addGroup(javax.swing.GroupLayout.Alignment.TRAILING, componentPanelLayout.createSequentialGroup()
                .addContainerGap(152, Short.MAX_VALUE)
                .addGroup(componentPanelLayout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
                    .addComponent(edgeSelectorPanel, javax.swing.GroupLayout.PREFERRED_SIZE, 100, javax.swing.GroupLayout.PREFERRED_SIZE)
                    .addGroup(componentPanelLayout.createSequentialGroup()
                        .addComponent(vertexSelectorPanel, javax.swing.GroupLayout.PREFERRED_SIZE, 100, javax.swing.GroupLayout.PREFERRED_SIZE)
                        .addGap(136, 136, 136))))
        );
        componentPanelLayout.setVerticalGroup(
            componentPanelLayout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
            .addGroup(componentPanelLayout.createSequentialGroup()
                .addContainerGap(217, Short.MAX_VALUE)
                .addComponent(vertexSelectorPanel, javax.swing.GroupLayout.PREFERRED_SIZE, 171, javax.swing.GroupLayout.PREFERRED_SIZE)
                .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.RELATED)
                .addComponent(edgeSelectorPanel, javax.swing.GroupLayout.PREFERRED_SIZE, 171, javax.swing.GroupLayout.PREFERRED_SIZE)
                .addGap(26, 26, 26))
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
            button.addMouseListener(new SelectorMouseAdapter(factory, vertexButtons.keySet()));
            vertexSelectorPanel.add(button);
        });
    }
    
    private void populateEdgeSelectorPanel(List<EdgeFactory> factories) {
        factories.forEach(factory -> {
            JToggleButton button = new JToggleButton();
            edgeButtons.put(button, factory);
            button.setIcon(createIcon(factory.getDefaultInstance()));
            button.addMouseListener(new SelectorMouseAdapter(factory, edgeButtons.keySet()));
            edgeSelectorPanel.add(button);
        });
    }

    /**
     * @param args the command line arguments
     */
    public static void main(String args[]) {
        selectNimbusLookAndFeel();
        /* Create and display the form */
        EventQueue.invokeLater(() -> new GraphEditorDemo().setVisible(true));
    }

    private static void selectNimbusLookAndFeel() {
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
        paintable.getPaintKeys().stream().filter(key -> !DiamondPaintable.DIAMOND_BORDER_PAINT_KEY.equals(key))
            .forEach(paintKey -> menu.add(createColorMenuItem(paintKey, paintable, location, onApply)));
    }

    private JMenuItem createColorMenuItem(Object paintKey, Paintable paintable, Point location, BiConsumer<Object, Color> onApply) {
        JMenuItem paintItem = new JMenuItem(getBundleText(paintKey.toString()));
        paintItem.addActionListener(event -> pickColor(location, paintable, paintKey, onApply));
        return paintItem;
    }

    private void addColorMenuItems(Factory factory, Point location, JPopupMenu menu, BiConsumer<Object, Color> onApply) {
        Paintable paintable = factory.getDefaultInstance();
        paintable.getPaintKeys().forEach(paintKey -> menu.add(createPaintItem(factory, paintKey, paintable, location, onApply)));
    }

    private JMenuItem createPaintItem(Factory factory, Object paintKey, Paintable paintable, Point location, BiConsumer<Object, Color> onApply) {
        JMenuItem paintItem = new JMenuItem(getBundleText(paintKey.toString()));
        Paintable iconPaintable = factory.getCopyInstance();
        iconPaintable.getPaintKeys().stream()
            .filter(key -> !key.equals(paintKey))
            .forEach(key -> iconPaintable.setPaint(key, TRANSPARENT));
        iconPaintable.getStrokeKeys().forEach(key -> iconPaintable.setStroke(key, SOLID_STROKE));
        paintItem.setIcon(createIcon(iconPaintable));
        paintItem.addActionListener(event -> pickColor(location, paintable, paintKey, onApply));
        return paintItem;
    }

    private void addStrokesMenus(Factory factory, JPopupMenu menu, BiConsumer<Object, Stroke> onApply) {
        factory.getDefaultInstance().getStrokeKeys().forEach(strokeKey -> createStrokesMenu(factory, strokeKey, menu, onApply));
    }

    private void createStrokesMenu(Factory factory, Object strokeKey, JPopupMenu popupMenu, BiConsumer<Object, Stroke> onApply) {
        JMenu strokesMenu = new JMenu(getBundleText(strokeKey.toString()));
        Paintable iconPaintable = factory.getCopyInstance();
        modifyPaint(VertexPaintable.BORDER_PAINT_KEY, iconPaintable, Color.BLACK);
        modifyPaint(PolygonPaintable.LINE_PAINT_KEY, iconPaintable, Color.BLACK);
        modifyPaint(VertexPaintable.FILL_PAINT_KEY, iconPaintable, TRANSPARENT);
        modifyPaint(ArrowheadPaintable.ARROWHEAD_PAINT_KEY, iconPaintable, TRANSPARENT);
        modifyPaint(DiamondPaintable.DIAMOND_BORDER_PAINT_KEY, iconPaintable, TRANSPARENT);
        modifyPaint(DiamondPaintable.DIAMOND_FILL_PAINT_KEY, iconPaintable, TRANSPARENT);

        strokesMenu.setIcon(createIcon(iconPaintable));
        strokes().forEach(stroke -> strokesMenu.add(createSrokeItem(factory, strokeKey, stroke, onApply)));
        popupMenu.add(strokesMenu);
    }

    private static List<Stroke> strokes() {
        return List.of(
            new BasicStroke(),
            dashedStroke(new float[]{ 1f, 2f }),
            dashedStroke(new float[]{ 3f }),
            new BasicStroke(2f),
            new BasicStroke(4f)
        );
    }

    private static Stroke dashedStroke(float[] dash) {
        return new BasicStroke(1f, BasicStroke.CAP_SQUARE, BasicStroke.JOIN_MITER, 10f, dash, 0f);
    }

    private JMenuItem createSrokeItem(Factory factory, Object strokeKey, Stroke stroke, BiConsumer<Object, Stroke> onApply) {
        Paintable strokePaintable = factory.getCopyInstance();
        modifyPaint(VertexPaintable.BORDER_PAINT_KEY, strokePaintable, Color.BLACK);
        modifyPaint(PolygonPaintable.LINE_PAINT_KEY, strokePaintable, Color.BLACK);
        modifyPaint(VertexPaintable.FILL_PAINT_KEY, strokePaintable, TRANSPARENT);
        modifyPaint(ArrowheadPaintable.ARROWHEAD_PAINT_KEY, strokePaintable, TRANSPARENT);
        modifyPaint(DiamondPaintable.DIAMOND_BORDER_PAINT_KEY, strokePaintable, TRANSPARENT);
        modifyPaint(DiamondPaintable.DIAMOND_FILL_PAINT_KEY, strokePaintable, TRANSPARENT);
        modifyStroke(VertexPaintable.BORDER_STROKE_KEY, strokePaintable, stroke);
        modifyStroke(PolygonPaintable.LINE_STROKE_KEY, strokePaintable, stroke);
        JMenuItem strokeItem = new JMenuItem();
        strokeItem.setIcon(createIcon(strokePaintable));
        strokeItem.addActionListener(event -> onApply.accept(strokeKey, stroke));
        return strokeItem;
    }

    private static void modifyPaint(Object key, Paintable paintable, Paint paint) {
        if (paintable.getPaintKeys().contains(key)) {
            paintable.setPaint(key, paint);
        }
    }

    private static void modifyStroke(Object key, Paintable paintable, Stroke stroke) {
        if (paintable.getStrokeKeys().contains(key)) {
            paintable.setStroke(key, stroke);
        }
    }

    private void addPaintsMenus(VertexFactory factory, JPopupMenu menu, Point location, BiConsumer<Object, Color> onApply) {
        factory.getDefaultInstance().getPaintKeys().forEach(paintKey -> menu.add(createPaintItem(factory, paintKey, factory.getDefaultInstance(), location, onApply)));
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

    private Factory createFactory(GraphComponent component) {
        if (component instanceof VertexComponent) {
            return createVertexFactory((VertexComponent) component);
        }
        if (component instanceof EdgeComponent) {
            return createEdgeFactory((EdgeComponent) component);
        }
        throw new IllegalArgumentException();
    }

    private VertexFactory createVertexFactory(VertexComponent component) {
        Paintable paintable = component.getPaintable();
        if (paintable instanceof RoundVertexPaintable) {
            return new VertexFactory(RoundVertexPaintable::new, paintable);
        }
        if (paintable instanceof SquareVertexPaintable) {
            return new VertexFactory(SquareVertexPaintable::new, paintable);
        }
        throw new IllegalStateException("Unsupported component");
    }

    private EdgeFactory createEdgeFactory(EdgeComponent component) {
        return new EdgeFactory(component);
    }

    private <K, V> Map<K, V> toMap(Collection<K> keys, Function<K, V> getter) {
        return keys.stream().collect(Collectors.toMap(Function.identity(), getter::apply));
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
        return createIcon(paintable::paint);
    }

    private static Icon createIcon(Consumer<Graphics2D> canvas) {
        BufferedImage image = new BufferedImage(16, 16, BufferedImage.TYPE_INT_ARGB);
        Graphics2D graphics = image.createGraphics();
        graphics.setRenderingHint(RenderingHints.KEY_ANTIALIASING, RenderingHints.VALUE_ANTIALIAS_ON);
        graphics.translate(image.getWidth() / 2, image.getHeight() / 2);
        canvas.accept(graphics);
        ImageIcon icon = new ImageIcon(image);
        graphics.dispose();
        return icon;
    }
    
    private void copy(Paintable source, Paintable target) {
        copy(source.getPaintKeys(), target::setPaint, source::getPaint);
        copy(source.getStrokeKeys(), target::setStroke, source::getStroke);
    }

    private static <T> void copy(Collection<Object> keys, BiConsumer<Object, T> setter, Function<Object, T> getter) {
        keys.forEach(key -> setter.accept(key, getter.apply(key)));
    }

 
    private class VertexFactory implements Factory {

        public VertexFactory(Function<Dimension, VertexPaintable> newInstance, Map<Object, Stroke> defaultStrokes, Map<Object, Paint> defaultPaints) {
            this.newInstance = newInstance;
            defaultInstance = newInstance.apply(VERTEX_ICON_DIMENSION);
            defaultStrokes.forEach(defaultInstance::setStroke);
            defaultPaints.forEach(defaultInstance::setPaint);
        }

        public VertexFactory(Function<Dimension, VertexPaintable> newInstance, Paintable paintable) {
            this(newInstance, toMap(paintable.getStrokeKeys(), paintable::getStroke), toMap(paintable.getPaintKeys(), paintable::getPaint));
        }

        @Override
        public VertexPaintable getDefaultInstance() {
            return defaultInstance;
        }

        @Override
        public VertexPaintable getCopyInstance() {
            VertexPaintable copyInstance = newInstance.apply(VERTEX_ICON_DIMENSION);
            copy(defaultInstance, copyInstance);
            return copyInstance;
        }

        private final Function<Dimension, VertexPaintable> newInstance;
        private final VertexPaintable defaultInstance;

    }
    
    
    private class EdgeFactory implements Factory {

        public EdgeFactory(EdgeDecorationPaintable decorationPaintable) {
            defaultInstance = new EdgePaintable(() -> ICON_MID_LEFT, () -> ICON_MID_RIGHT, decorationPaintable, true);
            defaultInstance.polygonPaintable.setPaint(PolygonPaintable.LINE_PAINT_KEY, Color.BLACK);
            defaultInstance.polygonPaintable.setStroke(PolygonPaintable.LINE_STROKE_KEY, SOLID_STROKE);
        }
        
        public EdgeFactory(boolean directed) {
            defaultInstance = new EdgePaintable(() -> ICON_MID_LEFT, () -> ICON_MID_RIGHT, directed);
            defaultInstance.polygonPaintable.setPaint(PolygonPaintable.LINE_PAINT_KEY, Color.BLACK);
            defaultInstance.polygonPaintable.setStroke(PolygonPaintable.LINE_STROKE_KEY, SOLID_STROKE);
            defaultInstance.decorationPaintable.setPaint(ArrowheadPaintable.ARROWHEAD_PAINT_KEY, Color.BLACK);
            defaultInstance.decorationPaintable.setStroke(ArrowheadPaintable.ARROWHEAD_STROKE_KEY, SOLID_STROKE);
        }

        public EdgeFactory(EdgeComponent component) {
            this(component.isDirected());
            copy(component.getPaintable(), defaultInstance);
            copy(component.getDecorationPaintable(), defaultInstance.decorationPaintable);
        }
        
        @Override
        public EdgePaintable getDefaultInstance() {
            return defaultInstance;
        }
        
        @Override
        public EdgePaintable getCopyInstance() {
            EdgeDecorationPaintable decorationPaintable = (defaultInstance.decorationPaintable instanceof ArrowheadPaintable) 
                ? new ArrowheadPaintable(defaultInstance.decorationPaintable.getStartPoint(), defaultInstance.decorationPaintable.getEndPoint()) 
                : new DiamondPaintable(defaultInstance.decorationPaintable.getStartPoint(), defaultInstance.decorationPaintable.getEndPoint());
            EdgePaintable copyInstance = new EdgePaintable(() -> ICON_MID_LEFT, () -> ICON_MID_RIGHT, decorationPaintable, defaultInstance.isDirected());
            copy(defaultInstance.polygonPaintable, copyInstance.polygonPaintable);
            copy(defaultInstance.decorationPaintable, copyInstance.decorationPaintable);
            return copyInstance;
        }

        public boolean isDirected() {
            return defaultInstance.isDirected();
        }
        
        private final EdgePaintable defaultInstance;
        
    }


    private interface Factory {
        Paintable getDefaultInstance();
        Paintable getCopyInstance();
    }


    private class EdgePaintable extends Paintable {

        public EdgePaintable(Supplier<Point> start, Supplier<Point> end, boolean directed) {
            this(start, end, new ArrowheadPaintable(start, end), directed);
        }
        
        public EdgePaintable(Supplier<Point> start, Supplier<Point> end, EdgeDecorationPaintable decorationPaintable, boolean directed) {
            this.directed = directed;
            polygonPaintable = PolygonPaintable.create(List.of(start.get(), end.get()));
            this.decorationPaintable = (decorationPaintable != null)
                ? decorationPaintable
                : new ArrowheadPaintable(start, end);
        }
        
        public EdgePaintable(Supplier<Point> start, Supplier<Point> end) {
            this(start, end, null, false);
        }
        
        @Override
        public void paint(Graphics2D graphics) {
            polygonPaintable.paint(graphics);
            if (directed) {
                decorationPaintable.paint(graphics);
            }
        }
        
        @Override
        public void paint(Graphics2D graphics, Paint paint, Stroke stroke) {
            polygonPaintable.paint(graphics, paint, stroke);
            if (directed) {
                decorationPaintable.paint(graphics, paint, stroke);
            }
        }

        @Override
        public Collection<Object> getPaintKeys() {
            ArrayList keys = new ArrayList(polygonPaintable.getPaintKeys());
            if (directed) {
                keys.addAll(decorationPaintable.getPaintKeys());
            }
            return keys;
        }

        @Override
        public Collection<Object> getStrokeKeys() {
            return List.of(PolygonPaintable.LINE_STROKE_KEY);
        }

        @Override
        public final void setPaint(Object key, Paint paint) {
            if (polygonPaintable.getPaintKeys().contains(key)) {
                polygonPaintable.setPaint(key, paint);
            }
            else if (decorationPaintable.getPaintKeys().contains(key)) {
                decorationPaintable.setPaint(key, paint);
            }
            else {
                throw new IllegalArgumentException(key.toString());
            }
        }
        
        @Override
        public final void setStroke(Object key, Stroke stroke) {
            if (polygonPaintable.getStrokeKeys().contains(key)) {
                polygonPaintable.setStroke(key, stroke);
            }
            else if (decorationPaintable.getStrokeKeys().contains(key)) {
                decorationPaintable.setStroke(key, stroke);
            }
            else {
                throw new IllegalArgumentException(key.toString());
            }
        }
        
        public boolean isDirected() {
            return directed;
        }
        
        private final PolygonPaintable polygonPaintable;
        private final EdgeDecorationPaintable decorationPaintable;
        private final boolean directed;
        
    };
        

    private class GraphPanel extends JPanel {

        @Override
        public void paint(Graphics graphics) {
            super.paint(graphics);
            canvas.paint((Graphics2D) graphics);
        }

    }


    private class SelectorMouseAdapter extends MouseAdapter {
        
         public SelectorMouseAdapter(Factory factory, Set<JToggleButton> buttons) {
            this.factory = factory;
            this.buttons = buttons;
        }
        
        @Override
        public void mouseClicked(MouseEvent event) {
            switch (event.getButton()) {
                case MouseEvent.BUTTON1 ->
                    ensureSingleSelection(buttons, (JToggleButton) event.getSource());
                case MouseEvent.BUTTON3 ->
                    showContextMenu(event, factory.getDefaultInstance());
            }
        }
        
        private void ensureSingleSelection(Collection<JToggleButton> buttons, JToggleButton clickedButton) {
            if (clickedButton.isSelected()) {
                buttons.stream()
                    .filter(button -> !button.equals(clickedButton))
                    .forEach(button -> button.setSelected(false));
            }
        }
        
        private void showContextMenu(MouseEvent event, Paintable paintable) {
            JToggleButton button = (JToggleButton) event.getSource();
            JPopupMenu menu = new JPopupMenu();
            addColorMenuItems(factory, event.getPoint(), menu, applyColorChange(button, paintable));
            addStrokesMenus(factory, menu, applyStrokeChange(button, paintable));
            if (menu.getComponentCount() > 0) {
                menu.show(button, event.getX(), event.getY());
            }
        }

        private BiConsumer<Object, Color> applyColorChange(JToggleButton button, Paintable paintable) {
            return (key, color) -> {
                paintable.setPaint(key, color);
                button.setIcon(createIcon(paintable));
            };
        }

        private BiConsumer<Object, Stroke> applyStrokeChange(JToggleButton button, Paintable paintable) {
            return (key, stroke) -> {
                paintable.setStroke(key, stroke);
                button.setIcon(createIcon(paintable));
            };
        }

        private final Factory factory;
        private final Set<JToggleButton> buttons;

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


    private final GraphCanvas canvas = new GraphCanvas(new ApplicationContext() {

        @Override
        public void showVertexMenu(VertexComponent vertex, Point location) {
            JPopupMenu menu = new JPopupMenu();
            addPaintsMenus(createVertexFactory(vertex), menu, location, (key, paint) -> getCanvas().changePaint(vertex.getPaintable(), key, paint));
            addStrokesMenus(createVertexFactory(vertex), menu, (key, stroke) -> getCanvas().changeStroke(vertex.getPaintable(), key, stroke));
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
            addStrokeMenus(edge, menu);
            menu.show(graphPanel, location.x, location.y);
        }

        private void addPaintMenus(GraphComponent component, Point location, JPopupMenu menu) {
            component.getCustomizablePaintables().forEach(paintable -> addColorMenuItems(paintable, location, menu, (key, newPaint) -> getCanvas().changePaint(paintable, key, newPaint)));
        }

        private void addStrokeMenus(GraphComponent component, JPopupMenu menu) {
            component.getCustomizablePaintables().forEach(paintable -> {
                if (!(paintable instanceof EdgeDecorationPaintable)) {
                    addStrokesMenus(createFactory(component), menu, (key, newStroke) -> getCanvas().changeStroke(paintable, key, newStroke));
                }
            });
        }

        @Override
        public void editString(String input, Point location, Consumer<String> onApply) {
            PopupControl.show(graphPanel,
                new TextFieldPopupModel(
                    popupRectangle(location),
                    input,
                    onApply)
            );
        }

        private Rectangle popupRectangle(Point location) {
            return new Rectangle(location.x - POPUP_WIDTH / 2, location.y - POPUP_HEIGHT / 2, POPUP_WIDTH, POPUP_HEIGHT);
        }

        @Override
        public void requestUpdate(CanvasUpdate update) {
            updateGraphPanel(update);
        }

        @Override
        public Optional<VertexComponent> createVertexComponent(Point location) {
            Optional<Map.Entry<JToggleButton, VertexFactory>> selectedOption = vertexButtons.entrySet().stream()
                .filter(entry -> entry.getKey().isSelected())
                .findAny();
            if (selectedOption.isEmpty()) {
                return Optional.empty();
            }
            return Optional.of(new VertexComponent(selectedOption.get().getValue().getCopyInstance(), location));
        }
        
        @Override 
        public boolean createEdgeAllowed() {
            return edgeButtons.keySet().stream().anyMatch(button -> button.isSelected());
        }

        @Override
        public Optional<EdgeComponent> createEdgeComponent(VertexComponent origin, VertexComponent terminus) {
            Optional<Map.Entry<JToggleButton, EdgeFactory>> selectedOption = edgeButtons.entrySet().stream()
                .filter(entry -> entry.getKey().isSelected())
                .findAny();
            if (selectedOption.isEmpty()) {
                return Optional.empty();
            }
            EdgePaintable paintable = selectedOption.get().getValue().getCopyInstance();
            BiFunction<Supplier<Point>, Supplier<Point>, EdgeDecorationPaintable> decorationPaintable = (left, right) -> {
                EdgeDecorationPaintable p = (paintable.decorationPaintable instanceof DiamondPaintable) 
                    ? new DiamondPaintable(left, right)
                    : new ArrowheadPaintable(left, right);
                copy(paintable.decorationPaintable, p);
                return p;
            };
            EdgeComponent edge = new EdgeComponent(origin, terminus, paintable.polygonPaintable, decorationPaintable);
            edge.setDirected(selectedOption.get().getValue().isDirected());
            return Optional.of(edge);
        }

        private static final int POPUP_WIDTH = 50;
        private static final int POPUP_HEIGHT = 20;

    });

    
    // Variables declaration - do not modify//GEN-BEGIN:variables
    private javax.swing.JPanel componentPanel;
    private javax.swing.JPanel edgeSelectorPanel;
    private javax.swing.JPanel graphPanel;
    private javax.swing.JList<String> historyList;
    private javax.swing.JPanel historyPanel;
    private javax.swing.JScrollPane historyScrollPane;
    private javax.swing.JPanel vertexSelectorPanel;
    // End of variables declaration//GEN-END:variables

    private final Map<JToggleButton, VertexFactory> vertexButtons = new HashMap<>();
    private final Map<JToggleButton, EdgeFactory> edgeButtons = new HashMap<>();

    private final javax.swing.DefaultListModel<String> historyListModel = new javax.swing.DefaultListModel<>();

    private static final ResourceBundle BUNDLE = ResourceBundle.getBundle("GraphEditor");

    private static final Point ICON_MID_LEFT = new Point(-8, 0);
    private static final Point ICON_MID_RIGHT = new Point(8, 0);

    private static final Dimension VERTEX_ICON_DIMENSION = new Dimension(12, 12);

    private static final Color TRANSPARENT = new Color(0, true);
    private static final BasicStroke SOLID_STROKE = new BasicStroke();

}
