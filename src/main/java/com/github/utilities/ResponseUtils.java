package com.github.utilities;

import java.io.IOException;
import java.io.OutputStream;
import java.nio.charset.StandardCharsets;
import java.util.logging.Level;
import java.util.logging.Logger;

import com.sun.net.httpserver.HttpExchange;

public class ResponseUtils {
    private ResponseUtils() {}

    private final static Logger logger = Logger.getLogger(ResponseUtils.class.getName());

    public static void returnOK(HttpExchange exchange, String body) {
        if (body == null) {
            body = "{}";
        }

        sendResponseWithBody(exchange, 200, body);
    }
    
    public static void returnBadRequest(HttpExchange exchange) {
        sendResponseNoBody(exchange, 400);
    }

    public static void returnMethodNotAllowed(HttpExchange exchange) {
        sendResponseNoBody(exchange, 405);
    }

    public static void returnTooManyRequests(HttpExchange exchange) {
        sendResponseNoBody(exchange, 429);
    }

    public static void returnServerError(HttpExchange exchange, String error) {
        sendErrorResponse(exchange, 500, error);
    }

    public static void returnCustom(HttpExchange exchange, int statusCode, String body) {
        sendResponseWithBody(exchange, statusCode, body);
    }

    private static void sendErrorResponse(HttpExchange exchange, int statusCode, String error) {
        String json = error == null || error.isBlank() 
            ? "{}"
            : "{\"error\": \"" + error.replace("\"","\\\"") + "\"}";

        sendResponseWithBody(exchange, statusCode, json);
    }

    private static void sendResponseNoBody(HttpExchange exchange, int statusCode) {
        exchange.getResponseHeaders().set("Content-Type", "application/json");
        
        try {
            //setting response headers
            exchange.sendResponseHeaders(statusCode, -1);

        } catch (IOException ex) {
            logger.log(Level.SEVERE, "Error attempting to write data.", ex);
        }
    }

    private static void sendResponseWithBody(HttpExchange exchange, int statusCode, String body) {
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
