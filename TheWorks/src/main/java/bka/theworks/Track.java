package bka.theworks;

import java.time.*;
import java.util.*;


/**
 */
public interface Track {

    String getTitle();

    Optional<String> getArtist();

    OptionalInt getYear();

    Duration getDuration();

    Optional<ZonedDateTime> getPlayDate();
}
