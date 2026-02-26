package com.github.utilities;

import java.time.Instant;

import redis.clients.jedis.RedisClient;

public class RateLimiter {
    private RateLimiter() {}

    /**
    * Returns true/false if Rate Limit has been exceeded by an individual client.
    * Uses basic Fixed window algorithm for rate limiting.
    *
    * @param  redis  RedisClient cache to check for client counter
    * @param  clientID  Remote address of client who sent request
    * @return boolean representing if client exceeded the number of attempts in the window
    */
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
