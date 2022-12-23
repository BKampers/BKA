/*
** Copyright © Bart Kampers
*/

package bka.awt;

import java.awt.*;
import java.awt.font.*;
import java.awt.geom.*;
import java.util.*;
import java.util.logging.*;


public class DrawStyle {


    public static abstract class Gradient {

        public Gradient() {
        }

        public float[] getFractions() {
            return fractions;
        }

        public void setFractions(float[] fractions) {
            this.fractions = fractions;
        }

        public Color[] getColors() {
            return colors;
        }

        public void setColors(Color[] colors) {
            this.colors = colors;
        }

        protected abstract Paint createPaint(Rectangle2D.Float area);

        protected float[] fractions;
        protected Color[] colors;

    }


    public static class LinearGradient extends Gradient {

        public LinearGradient() {
        }

        public Point2D.Float getRelativePoint1() {
            return relativePoint1;
        }

        public void setRelativePoint1(Point2D.Float relativePoint1) {
            this.relativePoint1 = relativePoint1;
        }

        public Point2D.Float getRelativePoint2() {
            return relativePoint2;
        }

        public void setRelativePoint2(Point2D.Float relativePoint2) {
            this.relativePoint2 = relativePoint2;
        }

        @Override
        protected Paint createPaint(Rectangle2D.Float area) {
            float xStart = area.x + area.width * relativePoint1.x;
            float xEnd = area.x + area.width + area.width * relativePoint2.x ;
            float yStart = area.y + area.height * relativePoint1.y;
            float yEnd = area.y + area.height + area.height * relativePoint2.y;
            return new LinearGradientPaint(xStart, yStart, xEnd, yEnd, fractions, colors);
        }

        private Point2D.Float relativePoint1;
        private Point2D.Float relativePoint2;

    }


    public static class RadialGradient extends Gradient {

        public RadialGradient() {
        }

        public Point2D.Float getRelativeCenter() {
            return relativeCenter;
        }

        public void setRelativeCenter(Point2D.Float relativeCenter) {
            this.relativeCenter = relativeCenter;
        }

        public float getRelativeRadius() {
            return relativeRadius;
        }

        public void setRelativeRadius(float relativeRadius) {
            this.relativeRadius = relativeRadius;
        }

        @Override
        protected Paint createPaint(Rectangle2D.Float area) {
            float radius = Math.min(area.width, area.height) / 2.0f * relativeRadius;
            Point2D.Float center = new Point2D.Float(
                (area.x + (area.width + area.width * relativeCenter.x) / 2.0f),
                (area.y + (area.height + area.height * relativeCenter.y) / 2.0f));
            return new RadialGradientPaint(center, radius, fractions, colors);
        }

        private Point2D.Float relativeCenter;
        private float relativeRadius;

    }


    public DrawStyle() {
    }


    public DrawStyle(DrawStyle drawStyle) {
        this.colors.putAll(drawStyle.colors);
        this.gradients.putAll(drawStyle.gradients);
        this.strokes.putAll(drawStyle.strokes);
        this.fonts.putAll(drawStyle.fonts);
    }


    public Map<Object, Color> getColors() {
        return Collections.unmodifiableMap(colors);
    }


    public void setColors(Map<Object, Color> colors) {
        this.colors.putAll(colors);
    }


    public Map<Object, Stroke> getStrokes() {
        return Collections.unmodifiableMap(strokes);
    }


    public void setStrokes(Map<Object, Stroke> strokes) {
        this.strokes.putAll(strokes);
    }


    public Map<Object, Map<TextAttribute, Object>> getFonts() {
        return Collections.unmodifiableMap(fonts);
    }


    public void setFonts(Map<Object, Map<TextAttribute, Object>> fonts) {
        this.fonts.putAll(fonts);
    }


    public Color getColor(Object key) {
        return colors.get(key);
    }


    public void setColor(Object key, Color color) {
        colors.put(key, color);
    }


    public Gradient getGradient(Object key) {
        return gradients.get(key);
    }


    public void setGradient(Object key, Gradient gradient) {
        gradients.put(key, gradient);
    }


    public Stroke getStroke(Object key) {
        return strokes.get(key);
    }


    public void setStroke(Object key, Stroke stroke) {
        strokes.put(key, stroke);
    }
    
    
    public String getText(Object key) {
        return texts.get(key);
    }
    
    
    public void setText(Object key, String text) {
        texts.put(key, text);
    }


    public Map<TextAttribute, Object> getFont(Object key) {
        return fonts.get(key);
    }


    public void setFont(Object key, Map<TextAttribute, Object> font) {
        fonts.put(key, font);
    }


    public static Gradient createLinearGradient(Color[] colors) {
        return createLinearGradient(createFractions(colors.length), colors);
    }


    public static Gradient createTopBottomGradient(Color[] colors) {
        return createTopBottomGradient(createFractions(colors.length), colors);
    }


    public static Gradient createTopBottomGradient(float[] fractions, Color[] colors) {
        return createLinearGradient(new Point2D.Float(0.5f, 0.0f), new Point2D.Float(-0.5f, 0.0f), fractions, colors);
    }


    public static Gradient createLeftRightGradient(Color[] colors) {
        return createLeftRightGradient(createFractions(colors.length), colors);
    }


    public static Gradient createLeftRightGradient(float[] fractions, Color[] colors) {
        return createLinearGradient(new Point2D.Float(0.0f, 0.5f), new Point2D.Float(0.0f, -0.5f), fractions, colors);
    }


    public static Gradient createLinearGradient(float[] fractions, Color[] colors) {
        return createLinearGradient(new Point2D.Float(), new Point2D.Float(), fractions, colors);
    }


    public static Gradient createLinearGradient(Point2D.Float relativePoint1, Point2D.Float relativePoint2, float[] fractions, Color[] colors) {
        LinearGradient gradient = new LinearGradient();
        gradient.relativePoint1 = relativePoint1;
        gradient.relativePoint2 = relativePoint2;
        gradient.fractions = fractions;
        gradient.colors = colors;
        return gradient;
    }
    
    
    public static Gradient createRadialGradient(Color[] colors) {
        return createRadialGradient(createFractions(colors.length), colors);
    }


    public static Gradient createRadialGradient(float[] fractions, Color[] colors) {
        return createRadialGradient(new Point2D.Float(), 1.0f, fractions, colors);
    }


    public static Gradient createRadialGradient(Point2D.Float relativeCenter, float[] fractions, Color[] colors) {
        return createRadialGradient(relativeCenter, 1.0f, fractions, colors);
    }


    public static Gradient createRadialGradient(float relativeRadius, float[] fractions, Color[] colors) {
        return createRadialGradient(new Point2D.Float(), relativeRadius, fractions, colors);
    }


    public static Gradient createRadialGradient(Point2D.Float relativeCenter, float relativeRadius, float[] fractions, Color[] colors) {
        RadialGradient gradient = new RadialGradient();
        gradient.relativeCenter = relativeCenter;
        gradient.relativeRadius = relativeRadius;
        gradient.fractions = fractions;
        gradient.colors = colors;
        return gradient;
    }


    public Paint createGradientPaint(Object key, Rectangle2D.Float area) {
        DrawStyle.Gradient gradient = getGradient(key);
        if (gradient != null) {
            try {
                return gradient.createPaint(area);
            }
            catch (RuntimeException ex) {
                Logger.getLogger(DrawStyle.class.getName()).log(Level.FINEST, "Invalid gradient", ex);
            }
        }
        return null;
    }


    private static float[] createFractions(int length) throws IllegalArgumentException {
        if (length < 2) {
            throw new IllegalArgumentException("Two or more colors required");
        }
        float[] fractions = new float[length];
        float step = 1.0f / (length - 1);
        for (int i = 0; i < length; ++i) {
            fractions[i] = i * step;
        }
        return fractions;
    }


    private final Map<Object, Color> colors = new HashMap<>();
    private final Map<Object, Gradient> gradients = new HashMap<>();
    private final Map<Object, Stroke> strokes = new HashMap<>();
    private final Map<Object, String> texts = new HashMap<>();
    private final Map<Object, Map<TextAttribute, Object>> fonts = new HashMap<>();

}
