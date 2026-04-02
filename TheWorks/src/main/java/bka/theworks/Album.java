package bka.theworks;

import java.util.*;


/**
 */
public interface Album {

    Optional<String> getTitle();

    Optional<String> getArtist();

    List<List<Track>> getTracks();
}
