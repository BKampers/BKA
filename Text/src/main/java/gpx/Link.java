/*
** © Bart Kampers
** This code may not be used for any purpose that harms humans (including
** exploitation and discrimination), humanity, the environment or the
** universe.
*/
package gpx;

import java.util.*;


/**
 * Hyperlink ({@code link} / linkType).
 */
public final class Link {

    public Link(String href, Optional<String> text, Optional<String> type) {
        this.href = Objects.requireNonNull(href);
        this.text = Objects.requireNonNull(text);
        this.type = Objects.requireNonNull(type);
    }

    public String getHref() {
        return href;
    }

    public Optional<String> getText() {
        return text;
    }

    public Optional<String> getType() {
        return type;
    }

    private final String href;
    private final Optional<String> text;
    private final Optional<String> type;

}
