package com.github.utilities;

import java.util.Optional;

public class CacheKeyBuilder {
    private CacheKeyBuilder() {}

    public static String buildKey(Optional<String> zipCode, Optional<String> startDate, Optional<String> endDate) {
        StringBuilder cacheKeyBuilder = new StringBuilder("weather:").append(zipCode.get());

        if (startDate.isPresent()) {
            cacheKeyBuilder.append("/").append(startDate.get());
        }
        if (endDate.isPresent()) {
            cacheKeyBuilder.append("/").append(endDate.get());
        }

        return cacheKeyBuilder.toString();
    }

    public static String buildPathFromKey(String cacheKey) {
        if (cacheKey.startsWith("weather:")) {
            return cacheKey.substring(8).replaceAll(":", "/");
        }
        
        return cacheKey.replaceAll(":", "/");
    }
}
