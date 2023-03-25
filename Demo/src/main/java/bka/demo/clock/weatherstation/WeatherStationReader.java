package bka.demo.clock.weatherstation;

/*
** © Bart Kampers
*/


import com.fasterxml.jackson.databind.*;
import com.fasterxml.jackson.dataformat.xml.*;
import com.fasterxml.jackson.dataformat.xml.annotation.*;
import java.io.*;
import java.net.*;
import java.text.*;
import java.time.*;
import java.time.format.*;
import java.util.*;
import java.util.stream.*;


public class WeatherStationReader {

    public static Collection<WeatherStation> getStations() throws IOException {
        final String page = loadPage();
        final LocalDateTime timestamp = getTimestamp(page);
        XmlMapper xmlMapper = new XmlMapper();
        xmlMapper.configure(DeserializationFeature.FAIL_ON_UNKNOWN_PROPERTIES, false);
        return xmlMapper.readValue(getTable(page), TableBody.class)
            .getRows().stream().map(row -> new Station(row, timestamp)).collect(Collectors.toList());
    }

    private static LocalDateTime getTimestamp(String page) {
        final String START_TAG = "<h2>Waarnemingen ";
        final String END_TAG = " uur</h2>";
        int dateStart = page.indexOf(START_TAG) + START_TAG.length();
        int dateEnd = page.indexOf(END_TAG, dateStart);
        DateTimeFormatter formatter = DateTimeFormatter.ofPattern("dd MMMM yyyy HH:mm");
        return LocalDateTime.parse(page.substring(dateStart, dateEnd), formatter);
    }

    private static String getTable(String page) throws IOException {
        final String START_TAG = "<tbody>";
        final String END_TAG = "</tbody>";
        int tableStart = page.indexOf(START_TAG);
        int tableEnd = page.indexOf(END_TAG, tableStart + START_TAG.length()) + END_TAG.length();
        return page.substring(tableStart, tableEnd);
    }

    private static String loadPage() throws IOException {
        StringBuilder page = new StringBuilder();
        URL url = new URL("https://www.knmi.nl/nederland-nu/weer/waarnemingen");
        try (InputStream stream = url.openStream()) {
            char[] buffer = new char[4096];
            Reader reader = new InputStreamReader(stream, "UTF-8");
            int count;
            while ((count = reader.read(buffer)) > 0) {
                page.append(buffer, 0, count);
            }
        }
        return page.toString();
    }


    @JacksonXmlRootElement(localName = "tbody")
    private static class TableBody {

        public List<TableRow> getRows() {
            return rows;
        }

        public void setRows(List<TableRow> rows) {
            this.rows = rows;
        }

        @JacksonXmlProperty(localName = "tr")
        @JacksonXmlElementWrapper(useWrapping = false)
        private List<TableRow> rows;
    }


    private static class TableRow {

        public TableData[] getColumns() {
            return columns;
        }

        public void setColumns(TableData[] columns) {
            this.columns = columns;
        }

        @JacksonXmlProperty(localName = "td")
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

        @JacksonXmlProperty(isAttribute = true, localName = "class")
        private String dataClass;

        @JacksonXmlText
        private String data;
    }


    private static class Station implements WeatherStation {

        Station(TableRow row, LocalDateTime timestamp) {
            this.row = Objects.requireNonNull(row);
            this.timestamp = Objects.requireNonNull(timestamp);
        }

        @Override
        public String getStationName() {
            return column(0);
        }

        @Override
        public LocalDateTime getTimestamp() {
            return timestamp;
        }

        @Override
        public String getWeatherSummary() {
            return column(1);
        }

        @Override
        public Double getTemperature() {
            return parseDouble(column(2));
        }

        @Override
        public Double getChill() {
            return parseDouble(column(3));
        }

        @Override
        public Double getHumidity() {
            return parseDouble(column(4));
        }

        @Override
        public Double getWindDirection() {
            String directionData = column(5);
            if (directionData == null) {
                return null;
            }
            String cardinal = directionData.substring(0, directionData.indexOf(' '));
            try {
                return new CardinalNumberFormat().parse(cardinal).doubleValue();
            }
            catch (ParseException ex) {
                throw new IllegalStateException("Invalid cardinal direction: " + cardinal);
            }
        }

        @Override
        public Double getWindSpeed() {
            return parseDouble(column(6));
        }

        @Override
        public Double getSquall() {
            return parseDouble(column(7));
        }

        @Override
        public Double getVisibility() {
            return parseDouble(column(8));
        }

        @Override
        public Double getPressure() {
            return parseDouble(column(9));
        }

        private Double parseDouble(String data) {
            if (data == null) {
                return null;
            }
            return Double.parseDouble(data);
        }

        private String column(int index) {
            return row.getColumns()[index].getData();
        }

        private final TableRow row;
        private final LocalDateTime timestamp;


    }

}
