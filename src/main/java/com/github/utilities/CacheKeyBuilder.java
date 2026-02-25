package com.github.utilities;

public class CacheKeyBuilder {
    public static String forWeather(String[] pathSegments) {
        boolean hasDates = pathSegments.length > 4;

        StringBuilder cacheKeyBuilder = new StringBuilder("weather:").append(pathSegments[3]);

        if (hasDates) {
            if (pathSegments.length > 5) {
                cacheKeyBuilder.append(":").append(pathSegments[4]);
            }
            if (pathSegments.length == 6) {
                cacheKeyBuilder.append(":").append(pathSegments[5]);
            }
        }

        return cacheKeyBuilder.toString();
    }

    public static String keyToPath(String cacheKey) {
        if (cacheKey.startsWith("weather:")) {
            return cacheKey.substring(8).replaceAll(":", "/");
        }
        
        return cacheKey.replaceAll(":", "/");
    }
}
