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

    /**
    * Sends a OK (200) response to the client, with a provided body.
    *
    * @param exchange  the HTTP exchange to send the response through
    * @param body String body to attach to response
    * 
    */
    public static void returnOK(HttpExchange exchange, String body) {
        if (body == null) {
            body = "{}";
        }

        sendResponseWithBody(exchange, 200, body);
    }
    
    /**
    * Sends a Bad Request (400) response to the client.
    *
    * @param  exchange  the HTTP exchange to send the response through
    * 
    */
    public static void returnBadRequest(HttpExchange exchange) {
        sendResponseNoBody(exchange, 400);
    }

    /**
    * Sends a Method Not Allowed (405) response to the client.
    *
    * @param  exchange  the HTTP exchange to send the response through
    * 
    */
    public static void returnMethodNotAllowed(HttpExchange exchange) {
        sendResponseNoBody(exchange, 405);
    }

    /**
    * Sends a Too Many Requests (429) response to the client.
    *
    * @param  exchange  the HTTP exchange to send the response through
    * 
    */
    public static void returnTooManyRequests(HttpExchange exchange) {
        sendResponseNoBody(exchange, 429);
    }

    /**
    * Sends a Server Error (500) response to the client, with a provided body.
    *
    * @param exchange  the HTTP exchange to send the response through
    * @param error String error body to attach to response
    * 
    */
    public static void returnServerError(HttpExchange exchange, String error) {
        sendErrorResponse(exchange, 500, error);
    }

    /**
    * Sends a custom response to the client, with a provided status code & body.
    *
    * @param exchange  the HTTP exchange to send the response through
    * @param statusCode Integer representing custom status code
    * @param body String body to attach to response
    * 
    */
    public static void returnCustom(HttpExchange exchange, int statusCode, String body) {
        sendResponseWithBody(exchange, statusCode, body);
    }

    /**
    * Sends an error response to the client, with a provided status code & body.
    *
    * @param exchange  the HTTP exchange to send the response through
    * @param statusCode Integer representing custom status code
    * @param error String body to attach to response
    * 
    */
    private static void sendErrorResponse(HttpExchange exchange, int statusCode, String error) {
        String json = error == null || error.isBlank() 
            ? "{}"
            : "{\"error\": \"" + error.replace("\"","\\\"") + "\"}";

        sendResponseWithBody(exchange, statusCode, json);
    }

    /**
    * Sends a response to the client, with a provided status code & no body.
    *
    * @param exchange  the HTTP exchange to send the response through
    * @param statusCode Integer representing custom status code
    * 
    */
    private static void sendResponseNoBody(HttpExchange exchange, int statusCode) {
        exchange.getResponseHeaders().set("Content-Type", "application/json");
        
        try {
            //setting response headers
            exchange.sendResponseHeaders(statusCode, -1);

        } catch (IOException ex) {
            logger.log(Level.SEVERE, "Error attempting to write data.", ex);
        }
    }

    /**
    * Sends a response to the client, with a provided status code & body.
    *
    * @param exchange  the HTTP exchange to send the response through
    * @param statusCode Integer representing custom status code
    * @param body String body to attach to response
    * 
    */
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
