package com.github.utilities;

import java.time.Instant;

import redis.clients.jedis.RedisClient;

public class RateLimiter {
    private RateLimiter() {}

    public static boolean isExceeded(RedisClient redis, String clientID) {
        long currentTimestamp = Instant.now().getEpochSecond() / Long.parseLong(System.getenv("RATE_LIMIT_WINDOW"));
        String key = String.format("ratelimit:$s:$d", clientID, currentTimestamp);

        long counter = redis.incr(key);

        if (counter == 1) {
            redis.expire(key, Long.parseLong(System.getenv("RATE_LIMIT_WINDOW")));
        }

        return counter > Integer.parseInt(System.getenv("RATE_LIMIT_MAX"));
    }
}
