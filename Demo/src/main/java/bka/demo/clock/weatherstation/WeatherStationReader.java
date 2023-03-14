package bka.demo.clock.weatherstation;

/*
** © Bart Kampers
*/


import com.fasterxml.jackson.databind.*;
import com.fasterxml.jackson.dataformat.xml.*;
import com.fasterxml.jackson.dataformat.xml.annotation.*;
import java.io.*;
import java.net.*;
import java.util.*;
import java.util.stream.*;


public class WeatherStationReader {

    public static Collection<WeatherStation> getStations() throws IOException {
        XmlMapper xmlMapper = new XmlMapper();
        xmlMapper.configure(DeserializationFeature.FAIL_ON_UNKNOWN_PROPERTIES, false);
        return xmlMapper.readValue(getTable(), TableBody.class)
            .getRows().stream().map(row -> new Station(row)).collect(Collectors.toList());
    }

    private static String getTable() throws IOException {
        final String START_TAG = "<tbody>";
        final String END_TAG = "</tbody>";
        String page = loadPage();
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

        Station(TableRow row) {
            this.row = row;
        }

        @Override
        public String getStationName() {
            return row.getColumns()[0].getData();
        }

        @Override
        public String getWeatherSummary() {
            return row.getColumns()[1].getData();
        }

        @Override
        public Double getTemperature() {
            return parseDouble(row.getColumns()[2]);
        }

        @Override
        public Double getChill() {
            return parseDouble(row.getColumns()[3]);
        }

        @Override
        public Double getHumidity() {
            return parseDouble(row.getColumns()[4]);
        }

        @Override
        public String getWindDirection() {
            if (row.getColumns()[5].getData() == null) {
                return null;
            }
            return row.getColumns()[5].getData().substring(0, row.getColumns()[5].getData().indexOf(' '));
        }

        @Override
        public Double getWindSpeed() {
            return parseDouble(row.getColumns()[6]);
        }

        @Override
        public Double getSquall() {
            return parseDouble(row.getColumns()[7]);
        }

        @Override
        public Double getVisibility() {
            return parseDouble(row.getColumns()[8]);
        }

        @Override
        public Double getPressure() {
            return parseDouble(row.getColumns()[9]);
        }

        private Double parseDouble(TableData data) {
            if (data.getData() == null) {
                return null;
            }
            return Double.parseDouble(data.getData());
        }

        private TableRow row;

    }

}
