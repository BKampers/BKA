package bka.demo.swing;

import bka.awt.*;
import java.util.*;
import java.util.logging.*;
import javax.swing.*;


/**
 */
public final class SwingUtil {

    private SwingUtil() {
        // Util class should not be instantiated.
    }

    public static void setLookAndFeel(String name) {
        Arrays.stream(UIManager.getInstalledLookAndFeels())
            .filter(info -> name.equals(info.getName()))
            .findAny()
            .ifPresentOrElse(
                SwingUtil::setLookAndFeel,
                () -> getLogger().log(Level.WARNING, "No such Look and Feel: ''{0}''", name));
    }

    private static void setLookAndFeel(UIManager.LookAndFeelInfo lookAndFeelInfo) {
        try {
            UIManager.setLookAndFeel(lookAndFeelInfo.getClassName());
        }
        catch (ReflectiveOperationException | UnsupportedLookAndFeelException ex) {
            getLogger().log(Level.WARNING, "Could not set Look and Feel", ex);
        }
    }

    public static void setApplicationName(String name) {
        Platform.system().ifPresent(platform -> {
            if (platform == Platform.MAC) {
                System.setProperty("apple.awt.application.name", name);
            }
        });
    }

    private static Logger getLogger() {
        return Logger.getLogger(SwingUtil.class.getName());
    }

}
