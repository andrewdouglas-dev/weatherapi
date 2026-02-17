package com.github.controllers;

import java.io.IOException;
import java.io.OutputStream;
import java.net.URI;
import java.net.http.HttpClient;
import java.net.http.HttpRequest;
import java.net.http.HttpResponse;
import java.nio.charset.StandardCharsets;
import java.util.logging.Level;
import java.util.logging.Logger;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

import com.sun.net.httpserver.HttpExchange;
import com.sun.net.httpserver.HttpHandler;

import redis.clients.jedis.RedisClient;

public class weatherHandler implements HttpHandler{

    private final Pattern getPattern = Pattern.compile("/api/v1/weather/\\w{5}");
    private final static Logger logger = Logger.getLogger(weatherHandler.class.getName());

    @Override
    public void handle(HttpExchange exchange) throws IOException {
        
        logger.info("Inbound request received.");

        if (exchange.getRequestMethod().equals("GET") && exchange.getRequestURI().getPath().startsWith("/api/v1/weather/")) {
            getWeather(exchange);
        } else {
            sendResponse(exchange, 404, "Invalid Request. Send request to /api/v1/weather/<5 digit ZipCode>");
        }
    }

    public void getWeather(HttpExchange exchange) {
        Matcher match = getPattern.matcher(exchange.getRequestURI().getPath());
        
        if (!match.matches()) {

            sendResponse(exchange, 400, "Invalid Zipcode.");

            return;
        }

        String path = exchange.getRequestURI().getPath();
        String zipCode = path.substring(path.length()-5);

        //Create Redis Cache to hold results
        try (RedisClient redis = RedisClient.create("redis://weather-redis:6379")) {
            String redisMatch = redis.get("weather:"+zipCode);

            //Check Cache before querying the API
            if (redisMatch != null) {
                //send Request to Weather API
                logger.info("Pulled from Redis!");

                sendResponse(exchange, 200, redisMatch);
            } else {

                logger.info("Pulled from Weather API");

                sendWeatherRequest(exchange, zipCode, redis);
            }
        } catch (Exception e) {
            logger.log(Level.SEVERE, "Error connecting to Redis", e);

            sendWeatherRequest(exchange, zipCode, null);
        }
    }

    private void sendWeatherRequest(HttpExchange exchange, String zipCode, RedisClient redis) {
        HttpClient weatherClient = HttpClient.newBuilder().build();
        HttpRequest weatherRequest = HttpRequest.newBuilder()
                    .uri(URI.create("https://weather.visualcrossing.com/VisualCrossingWebServices/rest/services/timeline/" + zipCode + "?key="+ System.getenv("WEATHER_API_KEY")))
                    .GET()
                    .build();

        try {
            
            HttpResponse<String> weatherResponse = weatherClient.send(weatherRequest, HttpResponse.BodyHandlers.ofString());

            String respBody = weatherResponse.body();
            int respCode = weatherResponse.statusCode();

            if (respCode == 200 && redis != null) {
                redis.setex("weather:"+zipCode, 300, respBody);
            }

            sendResponse(exchange, respCode, respBody);

        } catch (Exception e) {
            // TODO Auto-generated catch block
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
            logger.severe("Error attempting to write data.");
        }
    }
}
