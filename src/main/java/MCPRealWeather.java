///usr/bin/env jbang "$0" "$@" ; exit $?
//DEPS io.quarkus:quarkus-bom:3.21.1@pom
//DEPS io.quarkiverse.mcp:quarkus-mcp-server-stdio:1.0.0
//FILES application.properties
//DEPS com.squareup.okhttp3:okhttp:3.14.9
//DEPS com.fasterxml.jackson.core:jackson-databind:2.18.3
//FILES application.properties

import java.io.IOException;
import java.util.Objects;
import java.util.Optional;
import java.util.stream.StreamSupport;
import java.util.Locale;

import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;

import okhttp3.OkHttpClient;
import okhttp3.Request;
import okhttp3.Response;

import io.quarkiverse.mcp.server.Tool;
import io.quarkiverse.mcp.server.ToolArg;

public class MCPRealWeather {

  private static final String DEFAULT_FORECAST_API_URL = "https://api.open-meteo.com/v1/forecast?latitude=%s&longitude=%s&current_weather=true";
  private static final String DEFAULT_COORDINATES_API_URL = "https://geocoding-api.open-meteo.com/v1/search?name=%s&format=json";

  private final ObjectMapper objectMapper = new ObjectMapper();
  private final OkHttpClient httpClient = new OkHttpClient();

  private String forecastApiUrl;
  private String coordinatesApiUrl;

  public MCPRealWeather() {
    this(DEFAULT_FORECAST_API_URL, DEFAULT_COORDINATES_API_URL);
  }

  //For testing purposes
  public MCPRealWeather(String forecastApiUrl, String coordinatesApiUrl) {
    this.forecastApiUrl = forecastApiUrl;
    this.coordinatesApiUrl = coordinatesApiUrl;
  }

  @Tool(description = "A tool to get the current weather a given city and country code")
  public String getWeather(
    @ToolArg(description = "The city to get the weather for") String city, 
    @ToolArg(description = "The country code to get the weather for") String countryCode) {

    return getCoordinates(city, countryCode)
        .flatMap(this::fetchWeather)
        .orElse(String.format("Sorry, I couldn't find the weather for %s, %s", city, countryCode));
  }

  private Optional<String> executeHTTPRequest(String url) {
    Request request = new Request.Builder().url(url).build();
    
    try (Response response = httpClient.newCall(request).execute()) {
      if (response.isSuccessful() && response.body() != null) {
        return Optional.ofNullable(response.body().string());
      }
      System.err.println("Request failed with status: " + response.code());
      return Optional.empty();
    } catch (IOException e) {
      System.err.println("Error executing HTTP request: " + e.getMessage());
      return Optional.empty();
    }
  }

  private Optional<double[]> extractCoordinates(String jsonResponse, String countryCode) {
    try {
      JsonNode jsonNode = objectMapper.readTree(jsonResponse);
      JsonNode resultsArray = jsonNode.get("results");
      
      if (Objects.isNull(resultsArray) || !resultsArray.isArray()) {
        return Optional.empty();
      }
      
      return StreamSupport.stream(resultsArray.spliterator(), false)
          .filter(node -> matchesCountry(node, countryCode))
          .findFirst()
          .map(node -> new double[] {
              node.get("latitude").asDouble(),
              node.get("longitude").asDouble()
          });
    } catch (IOException e) {
      System.err.println("Error parsing coordinates data: " + e.getMessage());
      return Optional.empty();
    }
  }

  private Optional<double[]> getCoordinates(String city, String countryCode) {
    String url = String.format(coordinatesApiUrl, city);
    
    return executeHTTPRequest(url)
        .flatMap(response -> extractCoordinates(response, countryCode));
  }

  private String getWeatherDescription(int code) {
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

  private Optional<String> parseWeatherData(String jsonResponse) {
    try {
      JsonNode jsonNode = objectMapper.readTree(jsonResponse);
      JsonNode currentWeather = jsonNode.get("current_weather");
      
      if (currentWeather != null) {
        double temperature = currentWeather.get("temperature").asDouble();
        double windspeed = currentWeather.get("windspeed").asDouble();
        int weatherCode = currentWeather.get("weathercode").asInt();
        String weatherDesc = getWeatherDescription(weatherCode);

        String result = String.format(
            Locale.US,
            "Current temperature: %.1f°C, Windspeed: %.1f km/h, Condition: %s",
            temperature, windspeed, weatherDesc);
        
        return Optional.of(result);
      }
      return Optional.empty();
    } catch (IOException e) {
      System.err.println("Error parsing weather data: " + e.getMessage());
      return Optional.empty();
    }
  }

  private Optional<String> fetchWeather(double[] coords) {
    String url = String.format(forecastApiUrl, coords[0], coords[1]);
    
    return executeHTTPRequest(url)
        .flatMap(this::parseWeatherData);
  }
  
  private boolean matchesCountry(JsonNode node, String countryCode) {
    if (node == null || countryCode == null) {
      return false;
    }
    
    String countryLower = countryCode.toLowerCase();
    
    // Check country_code field which contains actual codes like "de"
    JsonNode countryCodeNode = node.get("country_code");
    if (countryCodeNode != null && countryLower.equals(countryCodeNode.asText().toLowerCase())) {
      return true;
    }
    
    // Fallback to country name check, but this usually contains full names like "Germany"
    JsonNode countryNode = node.get("country");
    if (countryNode != null && countryNode.asText().toLowerCase().startsWith(countryLower)) {
      return true;
    }
    
    return false;
  }
}
