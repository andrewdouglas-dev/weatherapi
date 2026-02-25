package com.github.utilities;

import java.io.IOException;
import java.io.OutputStream;
import java.util.logging.Level;
import java.util.logging.Logger;

import com.sun.net.httpserver.HttpExchange;

public class ResponseUtils {
    private final static Logger logger = Logger.getLogger(ResponseUtils.class.getName());

    public static void returnTooManyRequests(HttpExchange exchange) {
        exchange.getResponseHeaders().set("Content-Type", "application/json");
        
        try (OutputStream os = exchange.getResponseBody()) {
            //setting response headers
            exchange.sendResponseHeaders(429, 0);

            //writing body to response
            os.write(0);

        } catch (IOException ex) {
            logger.log(Level.SEVERE, "Error attempting to write data.", ex);
        }
    }
}
