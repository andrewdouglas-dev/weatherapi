package com.github;

import java.io.IOException;
import java.net.InetSocketAddress;

import com.github.controllers.weatherHandler;
import com.sun.net.httpserver.HttpServer;

/**
 * Hello world!
 *
 */
public class App 
{
    public static void main( String[] args ) throws IOException
    {
        int port = 8080;

        HttpServer server = HttpServer.create(new InetSocketAddress(port), 0);

        server.createContext("/api/v1/weather", new weatherHandler());

        server.setExecutor(null);
        server.start();
    }
}
