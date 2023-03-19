package bka.demo.clock.weatherstation;

/*
** © Bart Kampers
*/



public interface WeatherStation {

    String getStationName();

    String getWeatherSummary();

    Double getTemperature(); //* celcius

    Double getChill(); //* celcius

    Double getHumidity(); //* percent

    Double getWindDirection(); // degrees clockwise

    Double getWindSpeed(); //* m/s

    Double getSquall(); //* km/h

    Double getVisibility(); //* meters

    Double getPressure(); //* hPa

}
