package com.github.controllers;

import java.io.IOException;
import java.io.OutputStream;
import java.net.URI;
import java.net.http.HttpClient;
import java.net.http.HttpRequest;
import java.net.http.HttpResponse;
import java.nio.charset.StandardCharsets;
import java.util.logging.Logger;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

import com.github.utilities.Env;
import com.sun.net.httpserver.HttpExchange;
import com.sun.net.httpserver.HttpHandler;

public class weatherHandler implements HttpHandler{

    private final Pattern getPattern = Pattern.compile("/api/v1/weather/\\w{5}");
    private final static Logger logger = Logger.getLogger(weatherHandler.class.getName());

    @Override
    public void handle(HttpExchange exchange) throws IOException {
        
        logger.info("Request received");

        if (exchange.getRequestMethod().equals("GET") && exchange.getRequestURI().getPath().startsWith("/api/v1/weather/")) {
            getWeather(exchange);
        } else {
            sendResponse(exchange, 400, "Invalid Request send request to /api/v1/weather/<5 digit ZipCode>");
        }
    }

    public void getWeather(HttpExchange exchange) {
        Matcher match = getPattern.matcher(exchange.getRequestURI().getPath());
        
        if (!match.matches()) {
            System.out.println("Invalid Zipcode");
            return;
        }

        String path = exchange.getRequestURI().getPath();
        String zipCode = path.substring(path.length()-5);

        //TODO
        //Create Redis Cache to hold results
        //Check Cache before querying the API

        //send Request to Weather API
        sendWeatherRequest(exchange, zipCode);
    }

    private void sendWeatherRequest(HttpExchange exchange, String zipCode) {
        HttpClient weatherClient = HttpClient.newBuilder().build();
        HttpRequest weatherRequest = HttpRequest.newBuilder()
                    .uri(URI.create("https://weather.visualcrossing.com/VisualCrossingWebServices/rest/services/timeline/" + zipCode + "?key="+ Env.get("WEATHER_API_KEY")))
                    .GET()
                    .build();

        try {
            
            HttpResponse<String> weatherResponse = weatherClient.send(weatherRequest, HttpResponse.BodyHandlers.ofString());

            sendResponse(exchange, weatherResponse.statusCode(), weatherResponse.body());

        } catch (IOException e) {
            // TODO Auto-generated catch block
            logger.severe("Error occured attempting to retrieve data from visual crossing: " + e);
        } catch (InterruptedException e) {
            // TODO Auto-generated catch block
            logger.severe("Error occured attempting to retrieve data from visual crossing: " + e);
        }
    }

    private void sendResponse(HttpExchange exchange, int statusCode, String body) throws IOException {
        exchange.getResponseHeaders().set("Content-Type", "application/json");
        exchange.sendResponseHeaders(statusCode, body.getBytes(StandardCharsets.UTF_8).length);
        
        try (OutputStream os = exchange.getResponseBody()) {
            os.write(body.getBytes(StandardCharsets.UTF_8));
        } catch (Exception e) {
            logger.severe("Error occured writing response: " + e);
        }
    }
}
