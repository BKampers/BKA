/*
** © Bart Kampers
*/

package bka.swing.popup;

import java.awt.*;
import java.util.function.*;
import javax.swing.*;
import javax.swing.event.*;


public class ColorChooserPopupModel extends DefaultPopupModel<Color> {
 
    
    public ColorChooserPopupModel(Rectangle bounds, Color initialValue, Consumer<Color> onApply) {
        super(bounds, initialValue, onApply);
    }
    
    
    @Override
    public Component getComponent() {
        colorChooser.setPreviewPanel(getPreviewPanel());
        colorChooser.setColor(getInitialValue());
        return colorChooser;
    }


    @Override
    public void bindListener(Runnable whenReady) {
        colorChooser.getSelectionModel().addChangeListener(new ColorChangeListener(whenReady));
    }


    @Override
    protected Color getNewValue() {
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


    private final JColorChooser colorChooser = new JColorChooser();

}
