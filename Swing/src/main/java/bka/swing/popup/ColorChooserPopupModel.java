/*
** © Bart Kampers
*/

package bka.swing.popup;

import java.awt.*;
import java.util.*;
import java.util.function.*;
import javax.swing.*;
import javax.swing.event.*;


public class ColorChooserPopupModel extends DefaultPopupModel<Color> {

    /**
     * Create a ColorChooserPopupModel with a new color chooser, having no recent colors
     *
     * @param bounds of the popup
     * @param initialValue initial color
     * @param onApply call back to perform when a color is chosen
     */
    public ColorChooserPopupModel(Rectangle bounds, Color initialValue, Consumer<Color> onApply) {
        this(bounds, initialValue, onApply, new JColorChooser());
    }

    /**
     * Create a ColorChooserPopupModel with the color chooser that has previously chosen colors for given key, as recent colors.
     *
     * @param bounds of the popup
     * @param initialValue initial color
     * @param onApply call back to perform when a color is chosen
     */
    public ColorChooserPopupModel(Object key, Rectangle bounds, Color initialValue, Consumer<Color> onApply) {
        this(bounds, initialValue, onApply, choosers.computeIfAbsent(Objects.requireNonNull(key), chooser -> new JColorChooser()));
    }

    private ColorChooserPopupModel(Rectangle bounds, Color initialValue, Consumer<Color> onApply, JColorChooser colorChooser) {
        super(bounds, initialValue, onApply);
        this.colorChooser = colorChooser;
    }

    @Override
    public Component getComponent() {
        colorChooser.setPreviewPanel(getPreviewPanel());
        colorChooser.setColor(getInitialValue());
        return colorChooser;
    }

    @Override
    public void bindListener(Runnable whenReady) {
        colorChangeListener = new ColorChangeListener(whenReady);
        colorChooser.getSelectionModel().addChangeListener(colorChangeListener);
    }

    @Override
    protected Color getNewValue() {
        colorChooser.getSelectionModel().removeChangeListener(colorChangeListener);
        return colorChooser.getColor();
    }

    protected JPanel getPreviewPanel() {
        return new JPanel();
    }


    private class ColorChangeListener implements ChangeListener {

        ColorChangeListener(Runnable whenReady) {
            this.whenReady = whenReady;
        }

        @Override
        public void stateChanged(ChangeEvent evt) {
            whenReady.run();
        }

        private final Runnable whenReady;

    }


    private final JColorChooser colorChooser;
    private ColorChangeListener colorChangeListener;

    private static final Map<Object, JColorChooser> choosers = new HashMap<>();

}
