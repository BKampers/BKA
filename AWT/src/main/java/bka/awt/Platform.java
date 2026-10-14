package bka.awt;

import java.util.*;


public enum Platform {
    MAC("Mac"),
    WINDOWS("Windows");

    private Platform(String hint) {
        this.hint = hint;
    }

    public static Optional<Platform> system() {
        String osName = System.getProperty("os.name");
        return Arrays.stream(values())
            .filter(platform -> osName.contains(platform.hint))
            .findAny();
    }

    private final String hint;

}
