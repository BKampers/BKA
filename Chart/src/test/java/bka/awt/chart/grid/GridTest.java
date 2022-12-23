/*
 * © Bart Kampers
 */

package bka.awt.chart.grid;

import java.util.*;
import java.util.concurrent.*;
import org.junit.*;
import static org.junit.Assert.*;


public class GridTest {

    @After
    public void cleanup() {
        grid = null;
    }


    @Test
    public void numberGridTest() {
        grid = new NumberGrid();
        testGrid(1, 20, new String[] { "0", "2", "4", "6", "8", "10", "12", "14", "16", "18", "20" });
        testGrid(0.0, 250.0, new String[] { "0", "25", "50", "75", "100", "125", "150", "175", "200", "225", "250" });
        testGrid(0.0, 25.0, new String[] { "0.0", "2.5", "5.0", "7.5", "10.0", "12.5", "15.0", "17.5", "20.0", "22.5", "25.0" });
        testGrid(0.0, 2.5, new String[] { "0.00", "0.25", "0.50", "0.75", "1.00", "1.25", "1.50", "1.75", "2.00", "2.25", "2.50" });
        testGrid(0.0, 0.25, new String[] { "0.000", "0.025", "0.050", "0.075", "0.100", "0.125", "0.150", "0.175", "0.200", "0.225", "0.250" });
        testGrid(0.0, 0.1, new String[] { "0.00", "0.01", "0.02", "0.03", "0.04", "0.05", "0.06", "0.07", "0.08", "0.09", "0.10", "0.11" });
        testGrid(0.0, 0.2, new String[] { "0.000", "0.025", "0.050", "0.075", "0.100", "0.125", "0.150", "0.175", "0.200", "0.225" });
        testGrid(0.0, 0.3, new String[] { "0.000", "0.025", "0.050", "0.075", "0.100", "0.125", "0.150", "0.175", "0.200", "0.225", "0.250", "0.275", "0.300" });
        testGrid(0.0, 0.4, new String[] { "0.00", "0.05", "0.10", "0.15", "0.20", "0.25", "0.30", "0.35", "0.40", "0.45" });
        testGrid(0.0, 0.5, new String[] { "0.00", "0.10", "0.20", "0.30", "0.40", "0.50" });
        testGrid(0.0, 0.6, new String[] { "0.00", "0.10", "0.20", "0.30", "0.40", "0.50", "0.60" });
        testGrid(0.0, 0.7, new String[] { "0.00", "0.10", "0.20", "0.30", "0.40", "0.50", "0.60", "0.70" });
        testGrid(0.0, 0.8, new String[] { "0.00", "0.10", "0.20", "0.30", "0.40", "0.50", "0.60", "0.70", "0.80", "0.90" });
        testGrid(0.0, 0.9, new String[] { "0.00", "0.10", "0.20", "0.30", "0.40", "0.50", "0.60", "0.70", "0.80", "0.90", "1.00" });
        testGrid(0.0, 1.0, new String[] { "0.0", "0.1", "0.2", "0.3", "0.4", "0.5", "0.6", "0.7", "0.8", "0.9", "1.0" });
        testGrid(0.0, 1.1, new String[] { "0.0", "0.1", "0.2", "0.3", "0.4", "0.5", "0.6", "0.7", "0.8", "0.9", "1.0", "1.1", "1.2" });
        testGrid(0.0, 1.2, new String[] { "0.0", "0.1", "0.2", "0.3", "0.4", "0.5", "0.6", "0.7", "0.8", "0.9", "1.0", "1.1", "1.2" });
        testGrid(0.0, 1.3, new String[] { "0.0", "0.1", "0.2", "0.3", "0.4", "0.5", "0.6", "0.7", "0.8", "0.9", "1.0", "1.1", "1.2", "1.3", "1.4" });
        testGrid(0.0, 1.4, new String[] { "0.0", "0.1", "0.2", "0.3", "0.4", "0.5", "0.6", "0.7", "0.8", "0.9", "1.0", "1.1", "1.2", "1.3", "1.4" });
        testGrid(0.0, 1.5, new String[] { "0.0", "0.2", "0.4", "0.6", "0.8", "1.0", "1.2", "1.4", "1.6" });
    }


    @Test
    public void integerGridTest() {
        grid = new IntegerGrid();
        testGrid(0, 0.1, new String[] { "0", "1" });
        testGrid(0, 1, new String[] { "0", "1", "2" });
        testGrid(0, 2, new String[] { "0", "1", "2", "3" });
        testGrid(0, 3, new String[] { "0", "1", "2", "3", "4" });
        testGrid(0, 4, new String[] { "0", "1", "2", "3", "4", "5" });
        testGrid(0, 5, new String[] { "0", "1", "2", "3", "4", "5", "6" });
        testGrid(0, 6, new String[] { "0", "1", "2", "3", "4", "5", "6", "7" });
        testGrid(0, 7, new String[] { "0", "1", "2", "3", "4", "5", "6", "7", "8" });
        testGrid(0, 8, new String[] { "0", "1", "2", "3", "4", "5", "6", "7", "8", "9" });
        testGrid(0, 9, new String[] { "0", "1", "2", "3", "4", "5", "6", "7", "8", "9", "10" });
        testGrid(0, 10, new String[] { "0", "1", "2", "3", "4", "5", "6", "7", "8", "9", "10", "11" });
        testGrid(0, 20, new String[] { "0", "5", "10", "15", "20", "25" });
        testGrid(0, 30, new String[] { "0", "5", "10", "15", "20", "25", "30", "35" });
        testGrid(0, 50, new String[] { "0", "10", "20", "30", "40", "50", "60" });
        testGrid(0, 60, new String[] { "0", "10", "20", "30", "40", "50", "60", "70" });
        testGrid(0, 70, new String[] { "0", "10", "20", "30", "40", "50", "60", "70", "80" });
        testGrid(0, 80, new String[] { "0", "10", "20", "30", "40", "50", "60", "70", "80", "90" });
        testGrid(0, 90, new String[] { "0", "10", "20", "30", "40", "50", "60", "70", "80", "90", "100" });
        testGrid(0, 100, new String[] { "0", "10", "20", "30", "40", "50", "60", "70", "80", "90", "100", "110" });
        testGrid(0, 110, new String[] { "0", "10", "20", "30", "40", "50", "60", "70", "80", "90", "100", "110", "120" });
        testGrid(0, 120, new String[] { "0", "10", "20", "30", "40", "50", "60", "70", "80", "90", "100", "110", "120", "130" });
        testGrid(0, 130, new String[] { "0", "20", "40", "60", "80", "100", "120", "140" });
        testGrid(0, 140, new String[] { "0", "20", "40", "60", "80", "100", "120", "140", "160" });
        testGrid(0, 150, new String[] { "0", "20", "40", "60", "80", "100", "120", "140", "160" });
        testGrid(0, 160, new String[] { "0", "20", "40", "60", "80", "100", "120", "140", "160", "180" });
        testGrid(0, 170, new String[] { "0", "20", "40", "60", "80", "100", "120", "140", "160", "180" });
        testGrid(0, 180, new String[] { "0", "20", "40", "60", "80", "100", "120", "140", "160", "180", "200" });
        testGrid(0, 190, new String[] { "0", "20", "40", "60", "80", "100", "120", "140", "160", "180", "200" });
        testGrid(0, 200, new String[] { "0", "50", "100", "150", "200", "250" });
        testGrid(0, 240, new String[] { "0", "50", "100", "150", "200", "250" });
        testGrid(0, 250, new String[] { "0", "50", "100", "150", "200", "250", "300" });
        testGrid(0, 290, new String[] { "0", "50", "100", "150", "200", "250", "300" });
        testGrid(0, 300, new String[] { "0", "50", "100", "150", "200", "250", "300", "350" });
        testGrid(0, 450, new String[] { "0", "50", "100", "150", "200", "250", "300", "350", "400", "450", "500" });
        testGrid(0, 500, new String[] { "0", "100", "200", "300", "400", "500", "600" });
        testGrid(1, 20, new String[] { "0", "2", "4", "6", "8", "10", "12", "14", "16", "18", "20", "22" });
    }


    @Test
    public void timestampGridTest() {
        grid = new TimestampGrid();
        testGrid(timestamp(1970, 0, 1), timestamp(1970, 11, 31), new String[] { "1 Jan 1970", "1 Apr 1970", "1 Jul 1970", "1 Oct 1970", "1 Jan 1971" });
        testGrid(TimeUnit.SECONDS.toMillis(1), TimeUnit.SECONDS.toMillis(4), new String[][] {
            { "01.000", "01.500", "02.000", "02.500", "03.000", "03.500", "04.000", "04.500" },
            { "1:00>" , "1:00>"}
        });
        testGrid(TimeUnit.MINUTES.toMillis(1), TimeUnit.MINUTES.toMillis(10), new String[][] {
            { "1:01", "1:02", "1:03", "1:04", "1:05", "1:06", "1:07", "1:08", "1:09", "1:10", "1:11" },
            { "1 Jan>" , "1 Jan>"}
        });
        testGrid(0, 1000, new String[][] {
            { "00.000", "00.100", "00.200", "00.300", "00.400", "00.500", "00.600", "00.700", "00.800", "00.900", "01.000", "01.100" },
            { "1:00>", "1:00>" }
        });
    }


    @Test
    public void mapGridTest() {
        Map<Number, String> map = new HashMap<>();
        map.put(1, "one");
        map.put(2, "two");
        map.put(3, "three");
        grid = new MapGrid(map);
        testGrid(1, 3, new String[] { "one", "two", "three" });
    }


    private static long timestamp(int year, int month, int date) {
        Calendar calendar = Calendar.getInstance();
        calendar.set(year, month, date);
        return calendar.getTimeInMillis();
    }


    private void testGrid(Number min, Number max, String[] expectedLabels) {
        testGrid(min, max, new String[][] { expectedLabels });
    }


    private void testGrid(Number min, Number max, String[][] expectedLabels) {
        grid.initialize(min, max);
        assertLabelsEqual(expectedLabels, grid.getMarkerLists());
    }


    private void assertLabelsEqual(String[][] expectedLabels, List<Grid.MarkerList> markerLists) {
        assertEquals(expectedLabels.length, markerLists.size());
        for (int row = 0; row < markerLists.size(); ++row) {
            assertLabelsEqual(expectedLabels[row], markerLists.get(row));
        }
    }


    private void assertLabelsEqual(String[] expectedLabels, Grid.MarkerList markerList) {
        assertEquals(expectedLabels.length, markerList.getValues().size());
        for (int col = 0; col < markerList.getValues().size(); ++col) {
            assertEquals(expectedLabels[col], markerList.getLabel(Locale.ENGLISH, markerList.getValues().get(col)));
        }
    }


    private Grid grid;



}