package bka.demo.clock;

import java.io.*;
import java.util.logging.*;

/**
 */
public class ClockDemo extends javax.swing.JFrame {

    public ClockDemo() throws IOException {
        initComponents();
        mainPanel.add(new ClockPanel());
    }

    /**
     * This method is called from within the constructor to initialize the form. WARNING: Do NOT modify this code. The content of this method is
     * always regenerated by the Form Editor.
     */
    @SuppressWarnings("unchecked")
    // <editor-fold defaultstate="collapsed" desc="Generated Code">//GEN-BEGIN:initComponents
    private void initComponents() {

        mainPanel = new javax.swing.JPanel();
        deprecatedPanel = new javax.swing.JPanel();

        setDefaultCloseOperation(javax.swing.WindowConstants.EXIT_ON_CLOSE);

        mainPanel.setLayout(new javax.swing.BoxLayout(mainPanel, javax.swing.BoxLayout.LINE_AXIS));

        deprecatedPanel.setPreferredSize(new java.awt.Dimension(400, 400));
        deprecatedPanel.setLayout(new java.awt.BorderLayout());
        mainPanel.add(deprecatedPanel);

        getContentPane().add(mainPanel, java.awt.BorderLayout.LINE_END);

        pack();
    }// </editor-fold>//GEN-END:initComponents

    /**
     * @param args the command line arguments
     */
    public static void main(String args[]) {
        setLookAndFeel("Nimbus");
        /* Create and display the form */
        java.awt.EventQueue.invokeLater(() -> {
            try {
                new ClockDemo().setVisible(true);
            }
            catch (IOException ex) {
                Logger.getLogger(ClockDemo.class.getName()).log(Level.SEVERE, ex.getMessage());
            }
        });
    }

    // <editor-fold defaultstate="collapsed" desc="Set look and feel">
    /**
     * Set the given look and feel with given name If not available, stay with the default look and feel. For details see
     * http://download.oracle.com/javase/tutorial/uiswing/lookandfeel/plaf.html
     */
    private static void setLookAndFeel(String name) {
        try {
            for (javax.swing.UIManager.LookAndFeelInfo info : javax.swing.UIManager.getInstalledLookAndFeels()) {
                if (name.equals(info.getName())) {
                    javax.swing.UIManager.setLookAndFeel(info.getClassName());
                    return;
                }
            }
        }
        catch (ReflectiveOperationException | javax.swing.UnsupportedLookAndFeelException ex) {
            java.util.logging.Logger.getLogger(ClockDemo.class.getName()).log(java.util.logging.Level.SEVERE, null, ex);
        }
    }
    // </editor-fold>

    // Variables declaration - do not modify//GEN-BEGIN:variables
    private javax.swing.JPanel deprecatedPanel;
    private javax.swing.JPanel mainPanel;
    // End of variables declaration//GEN-END:variables

}
