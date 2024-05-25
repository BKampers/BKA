package bka.awt;

import java.awt.*;
import java.util.*;
import static org.junit.jupiter.api.Assertions.assertEquals;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.BeforeAll;
import org.junit.jupiter.api.Test;
import static org.mockito.Mockito.*;


public class CustomizedRendererTest {
    
    @BeforeAll
    public static void beforeAll() {
        renderer = new CustomizedRenderer(
            DEFAULT_ATTRIBUTES,
            (attributes, graphics) -> {
                graphics.setPaint(attributes.getPaint(PAINT_KEY));
                graphics.setStroke(attributes.getStroke(STROKE_KEY));
            });
    }
    
    @BeforeEach
    public void beforeEach() {
        graphicsMock = mock(Graphics2D.class);
    }
    
    @Test
    public void testGetAttributes() {
        assertEquals(DEFAULT_ATTRIBUTES.getPaint(PAINT_KEY), renderer.getAttributes().getPaint(PAINT_KEY));
        assertEquals(DEFAULT_ATTRIBUTES.getStroke(STROKE_KEY), renderer.getAttributes().getStroke(STROKE_KEY));
    }
    
    @Test
    public void testPaint() {
        renderer.paint(graphicsMock);
         verify(graphicsMock, times(1)).setPaint(DEFAULT_ATTRIBUTES.getPaint(PAINT_KEY));
         verify(graphicsMock, times(1)).setStroke(DEFAULT_ATTRIBUTES.getStroke(STROKE_KEY));
        
    }

    @Test
    public void testCustomizedCopy() {
        PaintAttributes newAttributes = new PaintAttributes(
            Map.of(PAINT_KEY, Color.WHITE),
            Map.of(STROKE_KEY, THICK_STROKE));
        CustomizedRenderer copy = new CustomizedRenderer(newAttributes, renderer);
         copy.paint(graphicsMock);
         verify(graphicsMock, times(1)).setPaint(newAttributes.getPaint(PAINT_KEY));
         verify(graphicsMock, times(1)).setStroke(newAttributes.getStroke(STROKE_KEY));
    }
    
    private static CustomizedRenderer renderer;
    private Graphics2D graphicsMock;
    
    private static final Object STROKE_KEY = "stroke_key";
    private static final Object PAINT_KEY = "paint_key";
    private static final BasicStroke BASIC_STROKE = new BasicStroke();
    private static final BasicStroke THICK_STROKE = new BasicStroke(16);
    private static final PaintAttributes DEFAULT_ATTRIBUTES = new PaintAttributes(
        Map.of(PAINT_KEY, Color.BLACK), 
        Map.of(STROKE_KEY, BASIC_STROKE));
    
}
