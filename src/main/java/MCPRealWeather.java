///usr/bin/env jbang "$0" "$@" ; exit $?
//DEPS com.squareup.okhttp3:okhttp:3.14.9
//DEPS com.fasterxml.jackson.core:jackson-databind:2.18.3
//DEPS io.quarkus:quarkus-bom:3.17.6@pom
//DEPS io.quarkiverse.mcp:quarkus-mcp-server-stdio:1.0.0.Alpha2
//DEPS io.quarkus:quarkus-qute
//FILES application.properties

import java.io.IOException;
import java.util.Optional;
import java.util.stream.Stream;
import java.util.stream.StreamSupport;

import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;

import okhttp3.OkHttpClient;
import okhttp3.Request;
import okhttp3.Response;

import io.quarkiverse.mcp.server.Tool;
import io.quarkiverse.mcp.server.ToolArg;

public class MCPRealWeather {

  private static final String FORECAST_API_URL = "https://api.open-meteo.com/v1/forecast?latitude=%s&longitude=%s&current_weather=true";
  private static final String COORDINATES_API_URL = "https://geocoding-api.open-meteo.com/v1/search?name=%s&format=json";

  @Tool(description = "A tool to get the current weather a given city and country code")
  public String getWeather(
    @ToolArg(description = "The city to get the weather for") String city, 
    @ToolArg(description = "The country code to get the weather for") String countryCode) {

    double[] coords = getCoordinates(city, countryCode);
    if (coords == null) {
      return String.format("Sorry, I couldn't find the location for %s, %s", city, countryCode);
    }
    String url = String.format(FORECAST_API_URL, coords[0], coords[1]);
    OkHttpClient client = new OkHttpClient();
    ObjectMapper objectMapper = new ObjectMapper();

    Request request = new Request.Builder()
        .url(url)
        .build();

    try {
      Response response = client.newCall(request).execute();
      if (response.isSuccessful() && response.body() != null) {
        String jsonResponse = response.body().string();

        // Parse JSON
        JsonNode jsonNode = objectMapper.readTree(jsonResponse);

        // Extract current weather data
        JsonNode currentWeather = jsonNode.get("current_weather");
        if (currentWeather != null) {
          double temperature = currentWeather.get("temperature").asDouble();
          double windspeed = currentWeather.get("windspeed").asDouble();
          int weatherCode = currentWeather.get("weathercode").asInt();
          String weatherDesc = getWeatherDescription(weatherCode);

          return String.format("Current temperature: %.1f°C, Windspeed: %.1f km/h, Condition: %s",
              temperature, windspeed, weatherDesc);
        } else {
          System.err.println("Request failed with status: " + response.code());
        }
      } else {
        System.err.println("Request failed  with status: " + response.code());
      }
    } catch (IOException e) {
      System.err.println("Error executing HTTP request: " + e.getMessage());
      e.printStackTrace();
    }
    return null;
  }

  private static double[] getCoordinates(String city, String countryCode) {

    OkHttpClient client = new OkHttpClient();
    ObjectMapper objectMapper = new ObjectMapper();
    String url = String.format(COORDINATES_API_URL, city);

    Request request = new Request.Builder()
        .url(url)
        .build();

    try {
      Response response = client.newCall(request).execute();
      if (response.isSuccessful() && response.body() != null) {
        String jsonResponse = response.body().string();

        // Parse JSON
        JsonNode jsonNode = objectMapper.readTree(jsonResponse);

        // Filter results to find the one in the right country
        JsonNode resultsArray = jsonNode.get("results");

        if (resultsArray != null && resultsArray.isArray()) {
          Stream<JsonNode> resultStream = 
            StreamSupport.stream(resultsArray.spliterator(), false);

          Optional<JsonNode> matchingNode = resultStream
            .filter(node -> 
              countryCode.toLowerCase().equals(
                node.get("country").asText().toLowerCase()) ||
              countryCode.toLowerCase().equals(
                node.get("country_code").asText().toLowerCase()))
            .findFirst();
          double[] coordinates = matchingNode
            .map((node) -> new double[]{
                    node.get("latitude").asDouble(-1.0),
                    node.get("longitude").asDouble(-1.0)
                })
            .orElse(null);          
          return coordinates;
        }
      } else {
        System.err.println("Request failed  with status: " + response.code());
      }
    } catch (IOException e) {
      System.err.println("Error executing HTTP request: " + e.getMessage());
      e.printStackTrace();
    }
    return null;
  }

  private static String getWeatherDescription(int code) {
    return switch (code) {
      case 0 -> "Clear sky";
      case 1, 2, 3 -> "Partly cloudy";
      case 45, 48 -> "Foggy";
      case 51, 53, 55 -> "Drizzle";
      case 61, 63, 65 -> "Rainy";
      case 71, 73, 75 -> "Snowy";
      case 95, 96, 99 -> "Stormy";
      default -> "Unknown conditions";
    };
  }
}
