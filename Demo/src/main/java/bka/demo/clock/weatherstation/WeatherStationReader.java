/*
** © Bart Kampers
*/
package bka.demo.clock.weatherstation;

import bka.text.cardinal.*;
import com.fasterxml.jackson.databind.*;
import com.fasterxml.jackson.dataformat.xml.*;
import com.fasterxml.jackson.dataformat.xml.annotation.*;
import java.io.*;
import java.net.*;
import java.nio.charset.*;
import java.text.*;
import java.time.*;
import java.time.format.*;
import java.util.*;
import java.util.stream.*;


public class WeatherStationReader {

    public static Collection<WeatherStation> getStations() throws IOException {
        String page = loadPage();
        LocalDateTime timestamp = getTimestamp(page);
        XmlMapper xmlMapper = new XmlMapper();
        xmlMapper.configure(DeserializationFeature.FAIL_ON_UNKNOWN_PROPERTIES, false);
        final TableHeadRow tableHead = xmlMapper.readValue(getTableHead(page), TableHead.class).getRows().get(0);
        return xmlMapper.readValue(getTable(page), TableBody.class)
            .getRows().stream()
            .map(row -> new Station(tableHead, row, timestamp))
            .collect(Collectors.toList());
    }

    private static LocalDateTime getTimestamp(String page) throws IOException {
        try {
            int dateStart = page.indexOf(TIMESTAMP_START_TAG) + TIMESTAMP_START_TAG.length();
            int dateEnd = page.indexOf(TIMESTAMP_END_TAG, dateStart);
            DateTimeFormatter formatter = DateTimeFormatter.ofPattern(TIMESTAMP_PATTERN, Locale.forLanguageTag("nl"));
            return LocalDateTime.parse(page.substring(dateStart, dateEnd), formatter);
        }
        catch (RuntimeException ex) {
            throw contentException("timestamp", ex);
        }
    }

    private static String getTableHead(String page) throws IOException {
        try {
            int tableStart = page.indexOf(TABLE_HEAD_START_TAG);
            int tableEnd = page.indexOf(TABLE_HEAD_END_TAG, tableStart + TABLE_START_TAG.length()) + TABLE_END_TAG.length();
            return page.substring(tableStart, tableEnd).replace("<br>", "").replace("&nbsp;", "").replace("&deg;", "");
        }
        catch (RuntimeException ex) {
            throw contentException("table head", ex);
        }
    }

    private static String getTable(String page) throws IOException {
        try {
            int tableStart = page.indexOf(TABLE_START_TAG);
            int tableEnd = page.indexOf(TABLE_END_TAG, tableStart + TABLE_START_TAG.length()) + TABLE_END_TAG.length();
            return page.substring(tableStart, tableEnd);
        }
        catch (RuntimeException ex) {
            throw contentException("measurements table", ex);
        }
    }

    private static IOException contentException(String component, Exception cause) {
        return new IOException("Unexpected page content: could not determine " + component + '.', cause);
    }

    private static String loadPage() throws IOException {
        try {
            return loadPage(new URI(KNMI_LINK));
        }
        catch (URISyntaxException ex) {
            throw new IllegalStateException(ex);
        }
    }

    private static String loadPage(URI uri) throws IOException {
        StringBuilder page = new StringBuilder();
        char[] buffer = new char[4096];
        try ( InputStream stream = uri.toURL().openStream()) {
            Reader reader = new InputStreamReader(stream, StandardCharsets.UTF_8.name());
            int count;
            while ((count = reader.read(buffer)) > 0) {
                page.append(buffer, 0, count);
            }
        }
        return page.toString();
    }

    @JacksonXmlRootElement(localName = TABLE_HEAD)
    private static class TableHead {

        public List<TableHeadRow> getRows() {
            return rows;
        }

        @JacksonXmlProperty(localName = TABLE_ROW)
        @JacksonXmlElementWrapper(useWrapping = false)
        private List<TableHeadRow> rows;
    }

    private static class TableHeadRow {

        public int getColumnCount() {
            return columns.length;
        }

        public String getColumnData(int index) {
            return columns[index].getData();
        }

        public void setColumns(TableHeadData[] columns) {
            this.columns = columns;
        }

        public int indexOf(String data) {
            for (int i = 0; i < columns.length; ++i) {
                if (data.equals(columns[i].getData())) {
                    return i;
                }
            }
            return -1;
        }

        @JacksonXmlProperty(localName = TABLE_HEAD_DATA)
        @JacksonXmlElementWrapper(useWrapping = false)
        private TableHeadData[] columns;
    }

    private static class TableHeadData {

        public String getData() {
            return data;
        }

        public void setData(String data) {
            this.data = data;
        }

        @JacksonXmlText
        private String data;
    }

    @JacksonXmlRootElement(localName = TABLE_BODY)
    private static class TableBody {

        public List<TableRow> getRows() {
            return rows;
        }

        public void setRows(List<TableRow> rows) {
            this.rows = rows;
        }

        @JacksonXmlProperty(localName = TABLE_ROW)
        @JacksonXmlElementWrapper(useWrapping = false)
        private List<TableRow> rows;
    }


    private static class TableRow {

        public String getColumnData(int index) {
            return columns[index].getData();
        }

        public void setColumns(TableData[] columns) {
            this.columns = columns;
        }

        @JacksonXmlProperty(localName = TABLE_DATA)
        @JacksonXmlElementWrapper(useWrapping = false)
        private TableData[] columns;
    }


    private static class TableData {

        public String getDataClass() {
            return dataClass;
        }

        public void setDataClass(String dataClass) {
            this.dataClass = dataClass;
        }

        public String getData() {
            return data;
        }

        public void setData(String data) {
            this.data = data;
        }

        @JacksonXmlProperty(isAttribute = true, localName = DATA_CLASS)
        private String dataClass;

        @JacksonXmlText
        private String data;
    }


    private static class Station implements WeatherStation {

        Station(TableHeadRow head, TableRow row, LocalDateTime timestamp) {
            this.timestamp = Objects.requireNonNull(timestamp);
            IntStream.range(0, head.getColumnCount()).forEach(i -> data.put(head.getColumnData(i), row.getColumnData(i)));
        }

        @Override
        public String getStationName() {
            return Objects.toString(data.get(STATION_NAME_HEADER), "?");
        }

        @Override
        public LocalDateTime getTimestamp() {
            return timestamp;
        }

        @Override
        public Optional<String> getWeatherSummary() {
            String summary = data.get(WEATHER_SUMMARY_HEADER);
            if (summary == null) {
                return Optional.empty();
            }
            return Optional.of(summary);
        }

        @Override
        public Optional<Double> getTemperature() {
            return parseDouble(data.get(TEMPERATURE_HEADER));
        }

        @Override
        public Optional<Double> getChill() {
            return parseDouble(data.get(CHILL_HEADER));
        }

        @Override
        public Optional<Double> getHumidity() {
            return parseDouble(data.get(HUMIDITY_HEADER));
        }

        @Override
        public Optional<Double> getWindDirection() {
            String directionData = data.get(WIND_DIRECTION_HEADER);
            if (directionData == null) {
                return Optional.empty();
            }
            String cardinal = directionData.substring(0, directionData.indexOf(' '));
            try {
                return Optional.of(new CardinalNumberFormat(Locale.of("nl")).parse(cardinal).doubleValue());
            }
            catch (ParseException ex) {
                throw new IllegalStateException("Invalid cardinal direction: " + cardinal);
            }
        }

        @Override
        public Optional<Double> getWindSpeed() {
            return parseDouble(data.get(WIND_SPEED_HEADER));
        }

        @Override
        public Optional<Double> getSquall() {
            return parseDouble(data.get(SQUALL_HEADER));
        }

        @Override
        public Optional<Double> getVisibility() {
            return parseDouble(data.get(VISIBILITY_HEADER));
        }

        @Override
        public Optional<Double> getPressure() {
            return parseDouble(data.get(PRESSURE_HEADER));
        }

        private Optional<Double> parseDouble(String data) {
            if (data == null) {
                return Optional.empty();
            }
            return Optional.of(Double.valueOf(data));
        }

        private final LocalDateTime timestamp;
        private final Map<String, String> data = new HashMap<>();
    }

    private static final String KNMI_LINK = "https://www.knmi.nl/nederland-nu/weer/waarnemingen";

    private static final String TABLE_HEAD = "thead";
    private static final String TABLE_BODY = "tbody";
    private static final String TABLE_ROW = "tr";
    private static final String TABLE_HEAD_DATA = "th";
    private static final String TABLE_DATA = "td";
    private static final String DATA_CLASS = "class";

    private static final String TABLE_HEAD_START_TAG = "<thead>";
    private static final String TABLE_HEAD_END_TAG = "</thead>";
    private static final String TABLE_START_TAG = "<tbody>";
    private static final String TABLE_END_TAG = "</tbody>";
    private static final String TIMESTAMP_START_TAG = "<h2>Waarnemingen ";
    private static final String TIMESTAMP_END_TAG = " uur</h2>";
    private static final String TIMESTAMP_PATTERN = "d MMMM yyyy HH:mm";

    private static final String STATION_NAME_HEADER = "Station";
    private static final String WEATHER_SUMMARY_HEADER = "Weer";
    private static final String TEMPERATURE_HEADER = "Temp(C)";
    private static final String CHILL_HEADER = "Chill(C)";
    private static final String HUMIDITY_HEADER = "RV(%)";
    private static final String WIND_DIRECTION_HEADER = "Wind(bft)";
    private static final String WIND_SPEED_HEADER = "Wind(m/s)";
    private static final String SQUALL_HEADER = "Windstoot(km/uur)";
    private static final String VISIBILITY_HEADER = "Zicht(m)";
    private static final String PRESSURE_HEADER = "Druk(hPa)";


}
