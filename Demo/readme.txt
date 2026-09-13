To run the calendar demo:
mvn compile exec:java -Djava.util.logging.config.file="calendar-logging.properties" -Dsun.java2d.metal=false -Dexec.mainClass="bka.demo.calendar.CalendarDemo"

To run the Weather station demo:
mvn compile exec:java -Djava.util.logging.config.file="weatherstation-logging.properties" -Dsun.java2d.metal=false -Dexec.mainClass="bka.demo.clock.weatherstation.WeatherStationDemo"
