/*
** Copyright © Bart Kampers
*/

package bka.demo.chart;


import bka.awt.chart.*;
import bka.awt.chart.custom.*;
import bka.awt.chart.geometry.*;
import bka.awt.chart.render.*;
import java.awt.*;
import java.awt.event.*;
import java.util.*;
import java.util.logging.*;
import javax.swing.*;


public final class ChartPanel extends javax.swing.JPanel {


    public enum DragZoomMode { NONE, X, Y, XY }
    public enum ClickZoomMode { NONE, DOUBLE_CLICK_GRID_AREA }

   
    public ChartPanel(ChartRenderer renderer) {
        this.renderer = Objects.requireNonNull(renderer);
        selectionRectangleColor = UIManager.getColor("Chart.selectionRectangleColor");
        if (selectionRectangleColor == null) {
            selectionRectangleColor = Color.LIGHT_GRAY;
        }
        addListeners();
    }


    public static AxisRenderer createDefaultAxisRenderer() {
        AxisStyle style = new AxisStyle(null);
        style.setAxisColor(getColor("axisColor"));
        style.setMarkerColor(getColor("markerColor"));
        style.setLabelColor(getColor("labelColor"));
        style.setTitleColor(getColor("titleColor"));
        style.setUnitColor(getColor("unitColor"));
        return new DefaultAxisRenderer(ChartRenderer.AxisPosition.ORIGIN, style);
    }


    private static Color getColor(String key) {
        Color color = javax.swing.UIManager.getColor("Chart." + key);
        if (color == null) {
            return Color.BLACK;
        }
        return color;
    }


    public void setDragZoomMode(DragZoomMode dragZoomMode) {
        this.dragZoomMode = Objects.requireNonNull(dragZoomMode);
    }
   
   
   public void setClickZoomMode(ClickZoomMode clickZoomMode) {
       this.clickZoomMode = Objects.requireNonNull(clickZoomMode);
   }
    
    
    @Override
    public void invalidate() {
        synchronized (renderer) {
            renderer.invalidate();
        }
        super.invalidate();
    }
    
    
    @Override
    public void paintComponent(Graphics g) {
        super.paintComponent(g);
        synchronized (renderer) {
            renderer.setLocale(getLocale());
            try {
                renderer.paint((Graphics2D) g, getBounds());
            }
            catch (ChartDataException ex) {
                Logger.getLogger(ChartPanel.class.getName()).log(Level.SEVERE, "Unable to paint chart", ex);
            }
        }
        drawSelectionRectangle((Graphics2D) g);
    }

    
    private void drawSelectionRectangle(Graphics2D g2d) {
        if (dragStartPoint != null && dragEndPoint != null) {
           g2d.setColor(selectionRectangleColor);
           int x = Math.min(dragStartPoint.x, dragEndPoint.x);
           int width = Math.max(dragStartPoint.x, dragEndPoint.x) - x;
           int y = Math.min(dragStartPoint.y, dragEndPoint.y);
           int height = Math.max(dragStartPoint.y, dragEndPoint.y) - y;
           g2d.drawRect(x, y, width, height);
        }
    }
    
    
    private void addListeners() {
        MouseAdapter mouseAdapter = new MouseAdapter();
        addMouseMotionListener(mouseAdapter);
        addMouseListener(mouseAdapter);
    }
    

    private boolean zoomEnabled() {
        return dragZoomMode != DragZoomMode.NONE || clickZoomMode != ClickZoomMode.NONE;
    }


    private class MouseAdapter extends java.awt.event.MouseAdapter {

        @Override
        public void mouseMoved(java.awt.event.MouseEvent evt) {
            Point mousePoint = evt.getPoint();
            synchronized (renderer) {
                if (renderer.chartArea().contains(mousePoint)) {
                    if (defaultCursor == null && zoomEnabled()) {
                        defaultCursor = getCursor();
                        setCursor(new Cursor(ZOOM_CURSOR));
                    }
                    renderer.highlight(mousePoint);
                    repaint();
                }
                else if (defaultCursor != null) {
                    setCursor(defaultCursor);
                    defaultCursor = null;
                }
            }
        }

        @Override
        public void mousePressed(java.awt.event.MouseEvent evt) {
            if (dragZoomMode != DragZoomMode.NONE && evt.getButton() == java.awt.event.MouseEvent.BUTTON1) {
                dragStartPoint = getZoomPoint(evt);
                repaint();
            }
        }

        @Override
        public void mouseDragged(java.awt.event.MouseEvent evt) {
           if (dragZoomMode != DragZoomMode.NONE && dragStartPoint != null) {
                dragEndPoint = getZoomPoint(evt);
                repaint();
            }
        }
        
        private Point getZoomPoint(MouseEvent evt) {
            synchronized (renderer) {
                switch (dragZoomMode) {
                    case X:
                        return new Point(getZoomX(evt), renderer.areaTop());
                    case Y:
                        return new Point(renderer.areaLeft(), getZoomY(evt));
                    case XY:
                        return new Point(getZoomX(evt), getZoomY(evt));
                    default:
                        throw new IllegalStateException(dragZoomMode.name());
                }
            }
        }

        private int getZoomX(MouseEvent evt) {
            return Math.min(Math.max(renderer.areaLeft(), evt.getX()), renderer.areaRight());
        }

        private int getZoomY(MouseEvent evt) {
            return Math.min(Math.max(renderer.areaTop(), evt.getY()), renderer.areaBottom());
        }

        @Override
        public void mouseReleased(java.awt.event.MouseEvent evt) {
            if (dragStartPoint != null && dragEndPoint != null) {
                if (dragZoomMode != DragZoomMode.NONE && evt.getButton() == java.awt.event.MouseEvent.BUTTON1) {
                    adjustWindow();
                }
                dragStartPoint = null;
                dragEndPoint = null;
                repaint();
            }
        }

        private void adjustWindow() {
            synchronized (renderer) {
                switch (dragZoomMode) {
                    case X:
                        renderer.setXWindow(xMin(), xMax());
                        break;
                    case Y:
                        setYWindows();
                        break;
                    case XY:
                        setYWindows();
                        renderer.setXWindow(xMin(), xMax());
                        break;
                }
            }
        }

        private void setYWindows() {
            ChartGeometry geometry = renderer.getChartGeometry();
            int minPixel = geometry.yPixel(yMin());
            int maxPixel = geometry.yPixel(yMax());
            RangeMap rangeMap = geometry.getYDataRanges();
            for (Map.Entry<Object, Range> entry : rangeMap.getRanges().entrySet()) {
                Range range = entry.getValue();
                double min = geometry.yValueByRange(range, minPixel);
                double max = geometry.yValueByRange(range, maxPixel);
                renderer.setYWindow(entry.getKey(), min, max);
            }
        }

        private Number xMin() {
            return renderer.getChartGeometry().xValue(Math.min(dragStartPoint.x, dragEndPoint.x));
        }

        private Number xMax() {
            return renderer.getChartGeometry().xValue(Math.max(dragStartPoint.x, dragEndPoint.x));
        }

        private Number yMin(){
            return renderer.getChartGeometry().yValue(Math.max(dragStartPoint.y, dragEndPoint.y));
        }

        private Number yMax() {
            return renderer.getChartGeometry().yValue(Math.min(dragStartPoint.y, dragEndPoint.y));
        }

        @Override
        public void mouseClicked(java.awt.event.MouseEvent evt) {
            Point point = evt.getPoint();
            synchronized (renderer) {
                if (clickZoomMode == ClickZoomMode.DOUBLE_CLICK_GRID_AREA && renderer.chartArea().contains(point)) {
                    if (evt.getButton() == java.awt.event.MouseEvent.BUTTON1 && evt.getClickCount() == 1) {
                        ChartRenderer.GridMode gridMode = renderer.getGridMode();
                        if (gridMode == ChartRenderer.GridMode.X || gridMode == ChartRenderer.GridMode.Y) {
                            zoom(point);
                            repaint();
                        }
                    }
                }
            }
        }

        private void zoom(Point point) {
            boolean xMode = renderer.getGridMode() == ChartRenderer.GridMode.X;
            ChartGeometry geometry = renderer.getChartGeometry();
            java.util.List<Number> values = (xMode) ? geometry.getXGrid().getValues() : geometry.getYGrid(null).getValues();
            if (values.size() >= 2) {
                Number min = values.get(0);
                boolean zoomed = false;
                int i = 1;
                while (! zoomed && i < values.size()) {
                    Number max = values.get(i);
                    if (xMode && geometry.xPixel(min) <= point.x && point.x <= geometry.xPixel(max)) {
                        renderer.setXWindow(min, max);
                        zoomed = true;
                    }
                    else if (! xMode && geometry.yPixel(min) <= point.y && point.y <= geometry.yPixel(max)) {
                        renderer.setYWindow(min, max);
                        zoomed = true;
                    }
                    else {
                        min = max;
                        i++;
                    }
                }
            }
        }
               
        private Cursor defaultCursor;
        
    }


    private final ChartRenderer renderer;

    private DragZoomMode dragZoomMode = DragZoomMode.NONE;
    private ClickZoomMode clickZoomMode = ClickZoomMode.NONE;
    
    private Point dragStartPoint;
    private Point dragEndPoint;

    private Color selectionRectangleColor;

    private static final int ZOOM_CURSOR = Cursor.HAND_CURSOR;

}
