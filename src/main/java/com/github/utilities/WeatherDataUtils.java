package com.github.utilities;

import java.io.IOException;
import java.net.URI;
import java.net.http.HttpClient;
import java.net.http.HttpRequest;
import java.net.http.HttpResponse;
import java.util.Optional;
import java.util.logging.Level;
import java.util.logging.Logger;

import com.sun.net.httpserver.HttpExchange;

import redis.clients.jedis.RedisClient;

public class WeatherDataUtils {
    private final static Logger logger = Logger.getLogger(WeatherDataUtils.class.getName());

    private WeatherDataUtils() {}

    /**
    * Builds URL for API request to Visual Crossing API.
    *
    * @param  cacheKey  cache key to convert for use in the URL
    * @return String containing for Visual Crossing request 
    */
    private static String buildURL(String cacheKey) {
        return new StringBuilder("https://weather.visualcrossing.com/VisualCrossingWebServices/rest/services/timeline/")
            .append(CacheKeyBuilder.buildPathFromKey(cacheKey))
            .append("?key=")
            .append(System.getenv("WEATHER_API_KEY")).toString();
    }

    /**
    * Sends request to Visual Crossings weather API.
    *
    * @param exchange  the HTTP exchange to send the response through
    * @param  cacheKey  cache key to convert for use in the URL
    * @return Optional of HTTP response, non-empty if successfully request to Visual Crossing 
    */
    public static Optional<HttpResponse<String>> returnWeatherResponse(HttpExchange exchange, String cacheKey) {
        HttpClient weatherClient = HttpClient.newBuilder().build();
        HttpRequest weatherRequest = HttpRequest.newBuilder()
                    .uri(URI.create(buildURL(cacheKey)))
                    .GET()
                    .build();

        try {
            return Optional.of(weatherClient.send(weatherRequest, HttpResponse.BodyHandlers.ofString()));
        } catch (IOException | InterruptedException e) {
            logger.log(Level.SEVERE, "Error accessing Visual Crossing weather data.", e);
            ResponseUtils.returnServerError(exchange, e.getMessage());

            return Optional.empty();
        }
    }

    /**
    * Sends request to Visual Crossings weather API and stores response in cache if successful.
    *
    * @param exchange  the HTTP exchange to send the response through
    * @param redis  Redis client used for caching result with TTL in Redis
    * @param  cacheKey  cache key to convert for use in the URL
    */
    public static void sendWeatherRequest(HttpExchange exchange, RedisClient redis, String cacheKey) {
        Optional<HttpResponse<String>> response = returnWeatherResponse(exchange, cacheKey);

        if (response.isEmpty()) {
            return;
        }

        String respBody = response.get().body();
        int respCode = response.get().statusCode();

        if (respCode == 200 && redis != null) {
            redis.setex(cacheKey, 300, respBody);
        }

        ResponseUtils.returnOK(exchange, respBody);
    }
}
