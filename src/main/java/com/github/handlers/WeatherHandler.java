package com.github.handlers;

import java.io.IOException;
import java.io.OutputStream;
import java.net.URI;
import java.net.http.HttpClient;
import java.net.http.HttpRequest;
import java.net.http.HttpResponse;
import java.nio.charset.StandardCharsets;
import java.util.logging.Level;
import java.util.logging.Logger;

import com.github.utilities.CacheKeyBuilder;
import com.github.utilities.PathParser;
import com.github.utilities.RateLimiter;
import com.github.utilities.ResponseUtils;
import com.sun.net.httpserver.HttpExchange;
import com.sun.net.httpserver.HttpHandler;

import redis.clients.jedis.RedisClient;

public class WeatherHandler implements HttpHandler{
    private final static Logger logger = Logger.getLogger(WeatherHandler.class.getName());

    @Override
    public void handle(HttpExchange exchange) throws IOException {
        
        logger.info("Inbound request received.");

        if (exchange.getRequestMethod().equals("GET")) {
            handleGetWeather(exchange);
        } else {
            sendResponse(exchange, 405, "Invalid Request. Send <GET> request to <resource>.");
        }
    }

    public void handleGetWeather(HttpExchange exchange) {
        //Refine Path Segments
        String[] pathSegments = PathParser.parseURI(exchange.getRequestURI().getPath());
        
        if (!PathParser.isValidWeatherPath(pathSegments)) {
            sendResponse(exchange, 400, "Request sent with incorrect parameters.");
            return;
        }

        String zipCode = pathSegments[3];

        if (!PathParser.isValidZipcode(zipCode)) {
            sendResponse(exchange, 400, "Request sent with incorrect parameters.");

            return;
        }
        
        String cacheKey = CacheKeyBuilder.forWeather(pathSegments);

        //Create Redis Cache to hold results
        try (RedisClient redis = RedisClient.create("redis://weather-redis:6379")) {
            //Rate Limit

            String clientId = exchange.getRemoteAddress().getAddress().getHostAddress();
            
            if (RateLimiter.isExceeded(redis, clientId)) {
                ResponseUtils.returnTooManyRequests(exchange);
                return;
            }

            String redisMatch = redis.get(cacheKey);

            //Check Cache before querying the API
            if (redisMatch != null) {
                //send Request to Weather API
                logger.info("Pulled from Redis!");

                sendResponse(exchange, 200, redisMatch);
            } else {

                logger.info("Pulled from Weather API");

                sendWeatherRequest(exchange,  redis, cacheKey);
            }
        } catch (Exception e) {
            logger.log(Level.SEVERE, "Error connecting to Redis", e);

            sendWeatherRequest(exchange, null, cacheKey);
        }
    }

    private void sendWeatherRequest(HttpExchange exchange, RedisClient redis, String cacheKey) {
        StringBuilder urlBuilder = new StringBuilder("https://weather.visualcrossing.com/VisualCrossingWebServices/rest/services/timeline/")
            .append(CacheKeyBuilder.keyToPath(cacheKey))
            .append("?key=")
            .append(System.getenv("WEATHER_API_KEY"));

        HttpClient weatherClient = HttpClient.newBuilder().build();
        HttpRequest weatherRequest = HttpRequest.newBuilder()
                    .uri(URI.create(urlBuilder.toString()))
                    .GET()
                    .build();

        try {
            
            HttpResponse<String> weatherResponse = weatherClient.send(weatherRequest, HttpResponse.BodyHandlers.ofString());

            String respBody = weatherResponse.body();
            int respCode = weatherResponse.statusCode();

            if (respCode == 200 && redis != null) {
                redis.setex(cacheKey, 300, respBody);
            }

            sendResponse(exchange, respCode, respBody);

        } catch (Exception e) {

            // TODO Add Automatic Retry
            
            logger.log(Level.SEVERE, "Error occured attempting to retrieve data from visual crossing", e);

            sendResponse(exchange, 500, null);
        }
    }

    private void sendResponse(HttpExchange exchange, int statusCode, String body) {
        exchange.getResponseHeaders().set("Content-Type", "application/json");
        
        try (OutputStream os = exchange.getResponseBody()) {
            //setting response headers
            exchange.sendResponseHeaders(statusCode, body.getBytes(StandardCharsets.UTF_8).length);

            //writing body to response
            os.write(body.getBytes(StandardCharsets.UTF_8));

        } catch (IOException ex) {
            logger.log(Level.SEVERE, "Error attempting to write data.", ex);
        }
    }
}
