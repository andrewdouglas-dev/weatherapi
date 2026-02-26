package com.github.handlers;

import java.io.IOException;
import java.util.Optional;
import java.util.logging.Logger;

import com.github.utilities.CacheKeyBuilder;
import com.github.utilities.PathParser;
import com.github.utilities.RateLimiter;
import com.github.utilities.ResponseUtils;
import com.github.utilities.WeatherDataUtils;
import com.sun.net.httpserver.HttpExchange;
import com.sun.net.httpserver.HttpHandler;

import redis.clients.jedis.RedisClient;

public class WeatherHandler implements HttpHandler{
    private final static Logger logger = Logger.getLogger(WeatherHandler.class.getName());
    private final RedisClient redis;

    public WeatherHandler() {
        this.redis = RedisClient.create("redis://weather-redis:6379");
    }
    @Override
    public void handle(HttpExchange exchange) throws IOException {
        
        logger.info("Inbound request received.");

        if (!exchange.getRequestMethod().equals("GET")) {
            ResponseUtils.returnMethodNotAllowed(exchange);
            return;
        }

        //Refine Path Segments
        String[] pathSegments = PathParser.parseURI(exchange.getRequestURI().getPath());
        Optional<String> zipCode = PathParser.extractZipCode(pathSegments);
        
        if (!PathParser.isValidWeatherPath(pathSegments) || zipCode.isEmpty()) {
            ResponseUtils.returnBadRequest(exchange);
            return;
        }
        
        String cacheKey = CacheKeyBuilder.buildKey(zipCode, PathParser.extractStartDate(pathSegments), PathParser.extractEndDate(pathSegments));

        String clientId = exchange.getRemoteAddress().getAddress().getHostAddress();
            
        if (RateLimiter.isExceeded(redis, clientId)) {
            ResponseUtils.returnTooManyRequests(exchange);
            return;
        }

        String redisMatch = redis.get(cacheKey);

        if (redisMatch != null) {
            logger.info("Pulled from Redis!");

            ResponseUtils.returnOK(exchange, redisMatch);
        } else {

            logger.info("Pulled from Weather API");

            WeatherDataUtils.sendWeatherRequest(exchange,  redis, cacheKey);
        }
    }
}
