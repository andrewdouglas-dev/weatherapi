package com.github.controllers;

import java.io.IOException;
import java.io.OutputStream;
import java.net.URI;
import java.net.http.HttpClient;
import java.net.http.HttpRequest;
import java.net.http.HttpResponse;
import java.nio.charset.StandardCharsets;
import java.time.Instant;
import java.util.logging.Level;
import java.util.logging.Logger;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

import com.sun.net.httpserver.HttpExchange;
import com.sun.net.httpserver.HttpHandler;

import redis.clients.jedis.RedisClient;

public class weatherHandler implements HttpHandler{

    private final Pattern zipCodePattern = Pattern.compile("\\d{5}");
    private final static Logger logger = Logger.getLogger(weatherHandler.class.getName());

    @Override
    public void handle(HttpExchange exchange) throws IOException {
        
        logger.info("Inbound request received.");

        if (exchange.getRequestMethod().equals("GET")) {
            getWeather(exchange);
        } else {
            sendResponse(exchange, 405, "Invalid Request. Send <GET> request to <resource>.");
        }
    }

    public void getWeather(HttpExchange exchange) {
        //Refine Path Segments
        String[] pathSegments = parseURI(exchange.getRequestURI().getPath());
        
        if (pathSegments.length < 4 || pathSegments.length > 6) {
            sendResponse(exchange, 400, "Request sent with incorrect parameters.");

            return;
        }

        String zipCode = pathSegments[3];

        //Validate API Request sent with valid Zipcode
        //Validate Date Times /////////////TO DO////////////
        if (!isMatch(exchange, zipCodePattern, zipCode)) {
            return;
        }        

        boolean hasDates = pathSegments.length > 4;

        //Create Redis Cache to hold results
        try (RedisClient redis = RedisClient.create("redis://weather-redis:6379")) {
            //Rate Limit

            String clientId = exchange.getRemoteAddress().getAddress().getHostAddress();
            
            if (checkRateLimit(redis, clientId)) {
                sendResponse(exchange, 429, "");
                return;
            }

            StringBuilder redisWeatherMatch = new StringBuilder("weather:").append(zipCode);

            if (hasDates) {
                if (pathSegments.length > 5) {
                    redisWeatherMatch.append(":").append(pathSegments[4]);
                }
                if (pathSegments.length == 6) {
                    redisWeatherMatch.append(":").append(pathSegments[5]);
                }
            }

            String redisMatch = redis.get(redisWeatherMatch.toString());

            //Check Cache before querying the API
            if (redisMatch != null) {
                //send Request to Weather API
                logger.info("Pulled from Redis!");

                sendResponse(exchange, 200, redisMatch);
            } else {

                logger.info("Pulled from Weather API");

                sendWeatherRequest(exchange, pathSegments, redis, redisWeatherMatch);
            }
        } catch (Exception e) {
            logger.log(Level.SEVERE, "Error connecting to Redis", e);

            sendWeatherRequest(exchange, pathSegments, null, null);
        }
    }

    private void sendWeatherRequest(HttpExchange exchange, String[] pathSegments, RedisClient redis, StringBuilder cacheWeatherAddress) {
        String parameters;

        if (cacheWeatherAddress.toString().startsWith("weather:")) {
            parameters = cacheWeatherAddress.substring(8).replaceAll(":", "/");
        } else {
            parameters = cacheWeatherAddress.toString().replaceAll(":", "/");
        }

        StringBuilder urlBuilder = new StringBuilder("https://weather.visualcrossing.com/VisualCrossingWebServices/rest/services/timeline/").append(parameters);
        
        urlBuilder.append("?key=").append(System.getenv("WEATHER_API_KEY"));

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
                redis.setex(cacheWeatherAddress.toString(), 300, respBody);
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

    private boolean checkRateLimit(RedisClient redis, String clientID) {
        long currentTimestamp = Instant.now().getEpochSecond() / Long.parseLong(System.getenv("RATE_LIMIT_WINDOW"));
        String key = String.format("ratelimit:$s:$d", clientID, currentTimestamp);

        long counter = redis.incr(key);

        if (counter == 1) {
            redis.expire(key, Long.parseLong(System.getenv("RATE_LIMIT_WINDOW")));
        }

        return counter > Integer.parseInt(System.getenv("RATE_LIMIT_MAX"));
    }

    private String[] parseURI(String path) {
        if (path == null || path.isEmpty()) {
            path = "/";
        }

        if (path.startsWith("/")) {
            path = path.substring(1);
        }

        if (path.endsWith("/")) {
            path = path.substring(0,path.length()-1);
        }

        return path.isEmpty() ? new String[0] : path.substring(1).split("/");
    }

    private boolean isMatch(HttpExchange exchange, Pattern patternToMatch, String value) {
        Matcher match = patternToMatch.matcher(value);
    
        if (!match.matches()) {
            sendResponse(exchange, 400, "Invalid Zipcode.");
        }

        return match.matches();
    }
}
