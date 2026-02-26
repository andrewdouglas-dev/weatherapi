package com.github.utilities;

import java.util.Optional;

public class CacheKeyBuilder {
    private CacheKeyBuilder() {}

    /**
    * Builds cache key based on zipCode, start date, and end date in format: weather:zipCode:startDate:endDate.
    * Start and end dates are not required in order to build the key.
    *
    * @param  zipCode  String representing the requested zipCode
    * @param  startDate Optional<String> of start date
    * @param  endDate Optional<String> of end date
    * @return String representing cache key
    */
    public static String buildKey(String zipCode, Optional<String> startDate, Optional<String> endDate) {
        StringBuilder cacheKeyBuilder = new StringBuilder("weather:").append(zipCode);

        if (startDate.isPresent()) {
            cacheKeyBuilder.append("/").append(startDate.get());
        }
        if (startDate.isPresent() && endDate.isPresent()) {
            cacheKeyBuilder.append("/").append(endDate.get());
        }

        return cacheKeyBuilder.toString();
    }

    /**
    * Replaces ":" in cache key with "/" and returns that string as Path.
    *
    * @param  cacheKey  cache key to convert to path
    * @return String representing path
    */
    public static String buildPathFromKey(String cacheKey) {
        if (cacheKey.startsWith("weather:")) {
            return cacheKey.substring(8).replaceAll(":", "/");
        }
        
        return cacheKey.replaceAll(":", "/");
    }
}
