import org.json.simple.JSONArray;
import org.json.simple.JSONObject;
import org.json.simple.parser.JSONParser;

import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;
import java.util.Scanner;

import javax.net.ssl.HttpsURLConnection;
import java.io.IOException;
import java.net.URL;
import java.net.URLEncoder;
import java.nio.charset.StandardCharsets;

// Data from external API and return it.
// GUI will display this data to the user.

public class WeatherApp {

    // Get weather data for given location
    public static JSONObject getWeatherData(String locationName) {

        try {

            // First get latitude and longitude
            JSONArray locationData = getLocationData(locationName);

            if (locationData == null || locationData.isEmpty()) {
                System.out.println("Location not found: " + locationName);
                return null;
            }

            // Get first location result
            JSONObject location = (JSONObject) locationData.get(0);

            // IMPORTANT:
            // JSON keys are lowercase "latitude" and "longitude"
            double latitude =
                    ((Number) location.get("latitude")).doubleValue();

            double longitude =
                    ((Number) location.get("longitude")).doubleValue();

            System.out.println("Location: " + location.get("name"));
            System.out.println("Latitude: " + latitude);
            System.out.println("Longitude: " + longitude);

            // Weather API URL
            String urlString =
                    "https://api.open-meteo.com/v1/forecast?"
                            + "latitude=" + latitude
                            + "&longitude=" + longitude
                            + "&hourly=temperature_2m"
                            + ",weather_code"
                            + ",wind_speed_10m"
                            + ",relative_humidity_2m"
                            + "&timezone=Asia%2FKolkata";

            // API call
            HttpsURLConnection conn = fetchApiResponse(urlString);

            if (conn == null) {
                return null;
            }

            // Check response
            if (conn.getResponseCode() != 200) {

                System.out.println("Error: Could not connect to Weather API");

                conn.disconnect();
                return null;
            }

            // Read API response
            StringBuilder resultJson = new StringBuilder();

            Scanner scanner =
                    new Scanner(conn.getInputStream());

            while (scanner.hasNextLine()) {
                resultJson.append(scanner.nextLine());
            }

            scanner.close();
            conn.disconnect();

            // Parse JSON
            JSONParser parser = new JSONParser();

            JSONObject resultJsonObj = (JSONObject) parser.parse(resultJson.toString());

            // Get hourly data
            JSONObject hourly = (JSONObject) resultJsonObj.get("hourly");

            // Get time array
            JSONArray time = (JSONArray) hourly.get("time");

            // Find current hour
            int index = findIndexOfCurrentTime(time);

            // Temperature

            JSONArray temperatureData = (JSONArray) hourly.get("temperature_2m");

            double temperature = ((Number) temperatureData.get(index)).doubleValue();

            // Weather code

            JSONArray weatherCodeData = (JSONArray) hourly.get("weather_code");

            long weatherCode = ((Number) weatherCodeData.get(index)).longValue();

            String weatherCondition = convertWeatherCode(weatherCode);

            // Humidity

            JSONArray relativeHumidity = (JSONArray) hourly.get("relative_humidity_2m");

            long humidity = ((Number) relativeHumidity.get(index)).longValue();

            // Wind speed

            JSONArray windspeedData =(JSONArray) hourly.get("wind_speed_10m");

            double windspeed = ((Number) windspeedData.get(index)).doubleValue();

            // Create JSON object
            // for GUI/frontend

            JSONObject weatherData = new JSONObject();

            weatherData.put(
                    "temperature",
                    temperature
            );

            weatherData.put(
                    "weather_condition",
                    weatherCondition
            );

            weatherData.put(
                    "humidity",
                    humidity
            );

            weatherData.put("windspeed", windspeed);

            return weatherData;

        } catch (Exception e) {

            System.out.println("Error while getting weather data:");

            e.printStackTrace();
        }

        return null;
    }

    // Get geographic coordinates for given location

    public static JSONArray getLocationData(String locationName) {

        try {

            // Convert city name into URL-safe format
            String encodedLocation = URLEncoder.encode(locationName, StandardCharsets.UTF_8);

            // Geocoding API
            String urlString =
                    "https://geocoding-api.open-meteo.com/v1/search?"
                            + "name=" + encodedLocation
                            + "&count=10"
                            + "&language=en"
                            + "&format=json";

            System.out.println("Geocoding API URL:");

            System.out.println(urlString);

            // API call
            HttpsURLConnection conn = fetchApiResponse(urlString);

            if (conn == null) {
                return null;
            }

            // Check response
            if (conn.getResponseCode() != 200) {

                System.out.println("Error: Could not connect to Geocoding API");

                conn.disconnect();
                return null;
            }

            // Read response
            StringBuilder resultJson = new StringBuilder();

            Scanner scanner = new Scanner(conn.getInputStream());

            while (scanner.hasNextLine()) {
                resultJson.append(scanner.nextLine());
            }

            scanner.close();
            conn.disconnect();

            // Parse JSON
            JSONParser parser = new JSONParser();

            JSONObject resultJsonObj = (JSONObject) parser.parse(resultJson.toString());

            // Get location results
            JSONArray locationData =(JSONArray) resultJsonObj.get("results");

            return locationData;

        } catch (Exception e) {

            System.out.println(
                    "Error while getting location data:"
            );

            e.printStackTrace();
        }

        return null;
    }

    // Create API connection

    private static HttpsURLConnection fetchApiResponse(String urlString) {

        try {

            URL url = new URL(urlString);

            HttpsURLConnection conn = (HttpsURLConnection) url.openConnection();

            // Set request method
            conn.setRequestMethod("GET");

            // Connection timeout
            conn.setConnectTimeout(10000);

            // Read timeout
            conn.setReadTimeout(10000);

            // Connect
            conn.connect();

            return conn;

        } catch (IOException e) {

            System.out.println("Error while connecting to API:");

            e.printStackTrace();
        }

        return null;
    }

    // Find current hour

    private static int findIndexOfCurrentTime(JSONArray timeList) {

        String currentTime = getCurrentTime();

        System.out.println("Current time: " + currentTime);

        // Search for matching time
        for (int i = 0;
             i < timeList.size();
             i++) {

            String time =
                    (String) timeList.get(i);

            if (time.equals(currentTime)) {

                return i;
            }
        }

        // If exact time is not found
        // return first value
        return 0;
    }

    // Get current time

    private static String getCurrentTime() {

        LocalDateTime currentDateTime = LocalDateTime.now();

        /*
         * Open-Meteo hourly time looks like:
         *
         * 2026-08-10T01:00
         *
         * Therefore use this format.
         */

        DateTimeFormatter formatter =
                DateTimeFormatter.ofPattern(
                        "yyyy-MM-dd'T'HH:00"
                );

        return currentDateTime.format(formatter);
    }

    // Convert Open-Meteo weather code
    // into readable condition

    private static String convertWeatherCode(
            long weathercode
    ) {

        String weatherCondition;

        if (weathercode == 0L) {

            weatherCondition = "Clear";

        } else if (weathercode == 1L
                || weathercode == 2L
                || weathercode == 3L) {

            weatherCondition = "Cloudy";

        } else if (
                (weathercode >= 51L
                        && weathercode <= 67L)
                        ||
                        (weathercode >= 80L
                                && weathercode <= 99L)
        ) {

            weatherCondition = "Rain";

        } else if (
                weathercode >= 71L
                        && weathercode <= 77L
        ) {

            weatherCondition = "Snow";

        } else if (
                weathercode == 45L
                        || weathercode == 48L
        ) {

            weatherCondition = "Fog";

        } else {

            weatherCondition = "Unknown";
        }

        return weatherCondition;
    }}