package com.github;

import java.io.IOException;
import java.net.InetSocketAddress;
import java.util.logging.Logger;

import com.github.handlers.WeatherHandler;
import com.sun.net.httpserver.HttpServer;

public class App {
    private final static Logger logger = Logger.getLogger(App.class.getName());
    public static void main( String[] args ) throws IOException {
        String port = System.getenv("PORT");

        HttpServer server = HttpServer.create(new InetSocketAddress(Integer.parseInt(port)), 0);

        server.createContext("/api/v1/weather", new WeatherHandler());

        server.setExecutor(null);
        server.start();

        logger.info("Server started on http://localhost:" + port);

        Runtime.getRuntime().addShutdownHook(new Thread(() -> {
            logger.info("Shutting down server");
            server.stop(0);
        }));
    }
}
