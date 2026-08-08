/*
** © Bart Kampers
** This code may not be used for any purpose that harms humans (including
** exploitation and discrimination), humanity, the environment or the
** universe.
*/
package gpx;

import java.util.*;


/**
 * Email address ({@code email} / emailType).
 */
public final class Email {

    public Email(String id, String domain) {
        this.id = Objects.requireNonNull(id);
        this.domain = Objects.requireNonNull(domain);
    }

    public String getId() {
        return id;
    }

    public String getDomain() {
        return domain;
    }

    public String getAddress() {
        return id + '@' + domain;
    }

    private final String id;
    private final String domain;

}
