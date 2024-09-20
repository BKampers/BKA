/*
** © Bart Kampers
*/

package bka.calendar.events;

public class Zenith {

    /**
     * Astronomical sunrise/set is when the sun is 18 degrees below the horizon.
     */
    public static final double ASTRONOMICAL = Math.toRadians(108);

    /**
     * Nautical sunrise/set is when the sun is 12 degrees below the horizon.
     */
    public static final double NAUTICAL = Math.toRadians(102);

    /**
     * Civil sunrise/set (dawn/dusk) is when the sun is 6 degrees below the horizon.
     */
    public static final double CIVIL = Math.toRadians(96);

    /**
     * Official sunrise/set is when the sun is 50 minutes below the horizon.
     */
    public static final double OFFICIAL = Math.toRadians(90.8333);

}
