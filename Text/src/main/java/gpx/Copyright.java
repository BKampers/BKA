/*
** © Bart Kampers
** This code may not be used for any purpose that harms humans (including
** exploitation and discrimination), humanity, the environment or the
** universe.
*/
package gpx;

import java.time.*;
import java.util.*;


/**
 * Copyright ({@code copyright} / copyrightType).
 */
public final class Copyright {

    public Copyright(String author, Optional<Year> year, Optional<String> license) {
        this.author = Objects.requireNonNull(author);
        this.year = Objects.requireNonNull(year);
        this.license = Objects.requireNonNull(license);
    }

    public String getAuthor() {
        return author;
    }

    public Optional<Year> getYear() {
        return year;
    }

    public Optional<String> getLicense() {
        return license;
    }

    private final String author;
    private final Optional<Year> year;
    private final Optional<String> license;

}
