/*
** © Bart Kampers
*/
package bka.swing.popup;

import java.awt.*;
import java.util.function.*;
import javax.swing.*;


public class TextFieldPopupModel extends DefaultPopupModel<String> {
        

    public TextFieldPopupModel(Rectangle bounds, String initialValue, Consumer<String> onApply) {
        super(bounds, initialValue, onApply);
    }
    
    
    @Override
    public void bindListener(Runnable whenReady) {
        textField.addActionListener(evt -> whenReady.run());
    }

    
    @Override
    public Component getComponent() {
        textField.setText(getInitialValue());
        return textField;
    }
    
    
    @Override
    protected String getNewValue() {
        return textField.getText();
    }
    

    private final JTextField textField = new JTextField();

}
