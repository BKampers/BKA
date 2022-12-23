/*
** Copyright © Bart Kampers
*/

package bka.awt.chart.render;

import bka.awt.chart.custom.*;
import bka.awt.chart.grid.*;
import java.awt.*;
import java.util.List;
import java.util.*;

public class DefaultAxisRenderer extends AxisRenderer {

    public DefaultAxisRenderer(ChartRenderer.AxisPosition position, Color color) {
        this(position, new AxisStyle(color), null);
    }

    public DefaultAxisRenderer(ChartRenderer.AxisPosition position, AxisStyle style) {
        this(position, style, null);
    }

    public DefaultAxisRenderer(ChartRenderer.AxisPosition position, AxisStyle style, Object key) {
        super(position, key);
        this.style = Objects.requireNonNull(style);
    }

    @Override
    public void drawXAxis(Graphics2D g2d, Locale locale) {
        XAxis renderer = new XAxis(g2d, locale);
        renderer.draw();
    }
    
    @Override
    public void drawYAxis(Graphics2D g2d, Locale locale) {
        YAxis renderer = new YAxis(g2d, locale);
        renderer.draw();
    }

    private String getTitleText(String title) {
        return String.format(style.getTitleFormat(), title);
    }

    private String getUnitText(String unit) {
        return String.format(style.getUnitFormat(), unit);
    }

    private int xAxisPixelPosition() {
        switch (getPosition()) {
            case MINIMUM:
                return xMin();
            case MAXIMUM:
                return xMax();
            case ORIGIN:
                return (getXRange().isInitialized()) ? x0() : xMin();
            default:
                throw new IllegalStateException(getPosition().name());
        }
    }

    private int yAxisPixelPosition() {
        switch (getPosition()) {
            case MINIMUM:
                return yMin();
            case MAXIMUM:
                return yMax();
            case ORIGIN:
                return (getYRange().isInitialized()) ? y0() : yMin();
            default:
                throw new IllegalStateException(getPosition().name());
        }
    }

    private void drawArrow(Graphics2D g2d, int x, int y) {
        Font font = g2d.getFont();
        g2d.setFont(new Font("Courier", font.getStyle(), font.getSize()));
        g2d.drawString(" \u2192", x, y);
        g2d.setFont(font);
    }
    
    /**
     */
    private class XAxis {

        XAxis(Graphics2D g2d, Locale locale) {
            this.g2d = g2d;
            this.locale = locale;
            fontMetrics = g2d.getFontMetrics();
        }

        void draw() {
            g2d.setStroke(new BasicStroke());
            if (style.getAxisColor() != null) {
                drawAxis();
            }
            xMarkerLists().forEach(this::drawMarkerList);
            if (getTitle() != null && style.getTitleColor() != null) {
                drawTitle();
            }
            if (getUnit() != null && style.getUnitColor() != null) {
                drawUnit();
            }
        }

        private void drawAxis() {
            g2d.setColor(style.getAxisColor());
            g2d.drawLine(xMin, yAxis, xMax, yAxis);
        }

        private void drawTitle() {
            int width = fontMetrics.stringWidth(getTitle());
            int xPos = xMin + (xMax() - xMin) / 2 + width / 2;
            int yPos = yMin() + fontMetrics.getHeight() * 3;
            g2d.setColor(style.getTitleColor());
            g2d.drawString(getTitle(), xPos, yPos);
            drawArrow(g2d, xPos + width, yPos);
        }

        private void drawUnit() {
            g2d.setColor(style.getUnitColor());
            g2d.drawString(getUnitText(getUnit()), xMax() + 3, yMin() + fontMetrics.getHeight() / 4);
        }

        private void drawMarkerList(Grid.MarkerList markerList) {
            List<Number> values = markerList.getValues();
            int count = values.size();
            for (int i = 0; i < count; ++i) {
                Number value = values.get(i);
                int x = xPixel(value);
                Date d = new Date(value.longValue());
                if (xMin <= x && x <= xMax) {
                    if (drawMarker && style.getMarkerColor() != null) {
                        drawMarker(x);
                    }
                }
                if (style.getLabelColor() != null) {
                    drawLabel(markerList, i, x);
                }
            }
            drawMarker = false;
            labelLine++;
        }

        private void drawMarker(int x) {
            g2d.setColor(style.getMarkerColor());
            g2d.drawLine(x, yAxis - 2, x, yAxis + 2);
        }

        private void drawLabel(Grid.MarkerList markerList, int i, int xLabel) {
            List<Number> values = markerList.getValues();
            String label = markerList.getLabel(locale, values.get(i));
            if (label != null) {
                g2d.setColor(style.getLabelColor());
                int yLabel = yAxis + style.getLabelOffset() + fontMetrics.getHeight() * labelLine * positionSignum;
                if (label.endsWith(">")) {
                    if (i < values.size() - 1) {
                        label = label.substring(0, label.length() - 1);
                        int xNext = xPixel(values.get(i + 1));
                        drawLabelBetweenMarkers(label, xLabel, xNext, yLabel);
                    }
                }
                else {
                    drawLabelAtMarker(label, xLabel, yLabel);
                }
            }
        }

        private void drawLabelAtMarker(String label, int xMarker, int yLabel) {
            if (xMin <= xMarker && xMarker <= xMax) {
                int width = fontMetrics.stringWidth(label);
                g2d.drawString(label, xMarker - width / 2, yLabel);
            }
        }

        private void drawLabelBetweenMarkers(String label, int xMarker, int xNext, int yLabel) {
            int width = fontMetrics.stringWidth(label);
            int xLabel = xMarker + (xNext - xMarker) / 2 - width / 2;
            if (xMin <= xLabel && xLabel + width <= xMax) {
                g2d.drawString(label, xLabel, yLabel);
            }
            else if (xMin > xLabel && xMarker - width >= xMin) {
                g2d.drawString(label, xMin, yLabel);
            }
            else if (xLabel + width > xMax && xMarker < xMax - width) {
                g2d.drawString(label, xMax - width, yLabel);
            }
        }

        private final int xMin = xMin();
        private final int xMax = xMax();
        private final int yAxis = yAxisPixelPosition();
        private final int positionSignum = (getPosition() == ChartRenderer.AxisPosition.MAXIMUM) ? -1 : 1;

        private final Graphics2D g2d;
        private final Locale locale;
        private final FontMetrics fontMetrics;

        private int labelLine = 1;
        private boolean drawMarker = true;
    }

    /**
     */
    private class YAxis {

        YAxis(Graphics2D g2d, Locale locale) {
            this.g2d = g2d;
            this.locale = locale;
            fontMetrics = g2d.getFontMetrics();
        }

        void draw() {
            if (style.getAxisColor() != null) {
                drawAxis();
            }
            yMarkerLists().forEach(this::drawMarkerList);
            if (getTitle() != null && style.getTitleColor() != null) {
                drawTitle(fontMetrics, labelColumn);
            }
            if (getUnit() != null && style.getUnitColor() != null) {
                drawUnit(fontMetrics);
            }
        }

        private void drawAxis() {
            g2d.setColor(style.getAxisColor());
            g2d.drawLine(x, yMin, x, yMax);
        }

        private void drawMarkerList(Grid.MarkerList markers) {
            int columnWidth = 0;
            List<Number> values = markers.getValues();
            for (Number value : values) {
                int y = yPixel(value);
                if (yMax <= y && y <= yMin) {
                    if (drawMarker && style.getMarkerColor() != null) {
                        drawMarker(y);
                    }
                    if (style.getLabelColor() != null) {
                        columnWidth = drawLabel(markers.getLabel(locale, value), y, columnWidth);
                    }
                }
            }
            if (labelColumn <= 0) {
                labelColumn -= columnWidth;
            }
            else {
                labelColumn += columnWidth;
            }
            drawMarker = false;
        }

        private void drawMarker(int y) {
            g2d.setColor(style.getMarkerColor());
            g2d.drawLine(x - 2, y, x + 2, y);
        }

        private int drawLabel(String label, int y, int columnWidth) {
            g2d.setColor(style.getLabelColor());
            if (label != null) {
                int width = fontMetrics.stringWidth(label);
                columnWidth = Math.max(width, columnWidth);
                g2d.drawString(label, x + labelColumn - width, y + fontMetrics.getDescent());
            }
            return columnWidth;
        }

        private void drawTitle(FontMetrics fontMetrics, int labelColumn) {
            int width = fontMetrics.stringWidth(getTitle());
            int xPos = x + labelColumn - fontMetrics.getHeight();
            int yPos = yMax + (yMin - yMax) / 2 + width / 2;
            g2d.rotate(-0.5 * Math.PI, xPos, yPos);
            g2d.setColor(style.getTitleColor());
            g2d.drawString(getTitleText(getTitle()), xPos, yPos);
            drawArrow(g2d, xPos + width, yPos);
            g2d.rotate(+0.5 * Math.PI, xPos, yPos);
        }

        private void drawUnit(FontMetrics fontMetrics) {
            String string = getUnitText(getUnit());
            g2d.setColor(style.getUnitColor());
            g2d.drawString(string, x + style.getLabelOffset() - fontMetrics.stringWidth(string), yMax - fontMetrics.getHeight());
        }

        private final int x = xAxisPixelPosition();
        private final int yMin = yMin();
        private final int yMax = yMax();

        private final Graphics2D g2d;
        private final Locale locale;
        private final FontMetrics fontMetrics;

        private int labelColumn = style.getLabelOffset();
        private boolean drawMarker = true;
    }


    private final AxisStyle style;

}
