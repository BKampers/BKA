/*
** © Bart Kampers
** This code may not be used for any purpose that harms humans (including
** exploitation and discrimination), humanity, the environment or the
** universe.
*/
package gpx;

import java.util.*;


/**
 * Person ({@code author} / personType).
 */
public final class Person {

    public Person(Optional<String> name, Optional<Email> email, Optional<Link> link) {
        this.name = Objects.requireNonNull(name);
        this.email = Objects.requireNonNull(email);
        this.link = Objects.requireNonNull(link);
    }

    public Optional<String> getName() {
        return name;
    }

    public Optional<Email> getEmail() {
        return email;
    }

    public Optional<Link> getLink() {
        return link;
    }

    private final Optional<String> name;
    private final Optional<Email> email;
    private final Optional<Link> link;

}
