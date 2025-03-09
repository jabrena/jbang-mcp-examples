import static org.junit.jupiter.api.Assertions.*;

import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

import okhttp3.mockwebserver.MockResponse;
import okhttp3.mockwebserver.MockWebServer;
import okhttp3.mockwebserver.RecordedRequest;

import java.io.IOException;

public class MCPRealWeatherTest {

    private MockWebServer mockCoordinatesServer;
    private MockWebServer mockForecastServer;
    private MCPRealWeather weatherService;
    
    // Sample test data
    private static final String SAMPLE_CITY = "Berlin";
    private static final String SAMPLE_COUNTRY = "DE";
    
    private static final String SAMPLE_COORDINATES_RESPONSE = """
            {
              "results": [
                {
                  "id": 12345,
                  "name": "Berlin",
                  "latitude": 52.52,
                  "longitude": 13.41,
                  "country": "Germany",
                  "country_code": "de"
                }
              ]
            }
            """;
    
    private static final String SAMPLE_WEATHER_RESPONSE = """
            {
              "current_weather": {
                "temperature": 20.5,
                "windspeed": 12.3,
                "weathercode": 1
              }
            }
            """;

    @BeforeEach
    void setUp() throws IOException {
        // Setup mock servers
        mockCoordinatesServer = new MockWebServer();
        mockForecastServer = new MockWebServer();
        
        mockCoordinatesServer.start();
        mockForecastServer.start();
        
        // Configure the class under test to use our mock servers
        String mockCoordinatesUrl = mockCoordinatesServer.url("/v1/search?name=%s&format=json").toString();
        String mockForecastUrl = mockForecastServer.url("/v1/forecast?latitude=%s&longitude=%s&current_weather=true").toString();
        
        weatherService = new MCPRealWeather(mockForecastUrl, mockCoordinatesUrl);
    }
    
    @AfterEach
    void tearDown() throws IOException {
        mockCoordinatesServer.shutdown();
        mockForecastServer.shutdown();
    }
    
    @Test
    void testGetWeatherSuccess() throws Exception {
        // Setup mock responses
        mockCoordinatesServer.enqueue(new MockResponse()
            .setBody(SAMPLE_COORDINATES_RESPONSE)
            .setResponseCode(200)
            .addHeader("Content-Type", "application/json"));
            
        mockForecastServer.enqueue(new MockResponse()
            .setBody(SAMPLE_WEATHER_RESPONSE)
            .setResponseCode(200)
            .addHeader("Content-Type", "application/json"));
        
        // Call the method under test
        String result = weatherService.getWeather(SAMPLE_CITY, SAMPLE_COUNTRY);
        
        // Verify results
        assertTrue(result.contains("20.5°C"), "Result should contain temperature");
        assertTrue(result.contains("12.3 km/h"), "Result should contain wind speed");
        assertTrue(result.contains("Partly cloudy"), "Result should contain weather condition");
        
        // Verify request to coordinates API
        RecordedRequest coordinatesRequest = mockCoordinatesServer.takeRequest();
        assertTrue(coordinatesRequest.getPath().contains(SAMPLE_CITY), 
            "Should contain city name in coordinates request");
        
        // Verify request to forecast API
        RecordedRequest forecastRequest = mockForecastServer.takeRequest();
        assertTrue(forecastRequest.getPath().contains("52.52"), 
            "Should contain correct latitude in forecast request");
        assertTrue(forecastRequest.getPath().contains("13.41"), 
            "Should contain correct longitude in forecast request");
    }
    
    @Test
    void testGetWeatherCoordinatesNotFound() {
        // Setup mock responses with empty results
        mockCoordinatesServer.enqueue(new MockResponse()
            .setBody("{ \"results\": [] }")
            .setResponseCode(200)
            .addHeader("Content-Type", "application/json"));
        
        // Call the method under test
        String result = weatherService.getWeather(SAMPLE_CITY, SAMPLE_COUNTRY);
        
        // Verify results
        assertEquals(String.format("Sorry, I couldn't find the weather for %s, %s", SAMPLE_CITY, SAMPLE_COUNTRY), result);
    }
    
    @Test
    void testGetWeatherCoordinatesServerError() {
        // Setup mock responses with server error
        mockCoordinatesServer.enqueue(new MockResponse()
            .setResponseCode(500));
        
        // Call the method under test
        String result = weatherService.getWeather(SAMPLE_CITY, SAMPLE_COUNTRY);
        
        // Verify results
        assertEquals(String.format("Sorry, I couldn't find the weather for %s, %s", SAMPLE_CITY, SAMPLE_COUNTRY), result);
    }
    
    @Test
    void testGetWeatherForecastServerError() throws Exception {
        // Setup mock responses
        mockCoordinatesServer.enqueue(new MockResponse()
            .setBody(SAMPLE_COORDINATES_RESPONSE)
            .setResponseCode(200)
            .addHeader("Content-Type", "application/json"));
            
        mockForecastServer.enqueue(new MockResponse()
            .setResponseCode(500));
        
        // Call the method under test
        String result = weatherService.getWeather(SAMPLE_CITY, SAMPLE_COUNTRY);
        
        // Verify results
        assertEquals(String.format("Sorry, I couldn't find the weather for %s, %s", SAMPLE_CITY, SAMPLE_COUNTRY), result);
    }
    
    @Test
    void testCountryCodeMatching() {
        // Test with uppercase country code
        mockCoordinatesServer.enqueue(new MockResponse()
            .setBody(SAMPLE_COORDINATES_RESPONSE)
            .setResponseCode(200));
            
        mockForecastServer.enqueue(new MockResponse()
            .setBody(SAMPLE_WEATHER_RESPONSE)
            .setResponseCode(200));
        
        String result = weatherService.getWeather(SAMPLE_CITY, "DE");
        assertTrue(result.contains("20.5°C"));
        
        // Test with lowercase country code
        mockCoordinatesServer.enqueue(new MockResponse()
            .setBody(SAMPLE_COORDINATES_RESPONSE)
            .setResponseCode(200));
            
        mockForecastServer.enqueue(new MockResponse()
            .setBody(SAMPLE_WEATHER_RESPONSE)
            .setResponseCode(200));
        
        result = weatherService.getWeather(SAMPLE_CITY, "de");
        assertTrue(result.contains("20.5°C"));
    }
} 