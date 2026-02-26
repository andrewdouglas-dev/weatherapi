package com.github.utilities;

import java.time.LocalDate;
import java.util.Optional;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

public class PathParser {
    private PathParser() {}

    public static String[] parseURI(String path) {
        if (path == null || path.isEmpty()) {
            path = "/";
        }

        if (path.startsWith("/")) {
            path = path.substring(1);
        }

        if (path.endsWith("/")) {
            path = path.substring(0,path.length()-1);
        }

        return path.isEmpty() ? new String[0] : path.substring(1).split("/");
    }

    public static boolean isValidWeatherPath(String[] pathSegments) {
        return 4 <= pathSegments.length && pathSegments.length <= 6;
    }


    /**
    * Returns true/false if provided zipCode is a valid U.S. zipcode.
    *
    * @param  zipCode  zipcode to validate
    * @return      boolean representing if zipCode is valid or not
    */
    public static boolean isValidZipcode(String zipCode) {
        Pattern zipCodePattern = Pattern.compile("\\d{5}");
        Matcher match = zipCodePattern.matcher(zipCode);

        return match.matches();
    }

    public static boolean isValidDate(String date) {
        try {
            LocalDate.parse(date);
            return true;
        } catch (Exception e) {
            return false;
        }
    }

    public static Optional<String> extractZipCode(String[] pathSegments) {
        if (pathSegments.length < 4 || !isValidZipcode(pathSegments[3])) {
            return Optional.empty();
        }

        return Optional.of(pathSegments[3]);
    }

    public static Optional<String> extractStartDate(String[] pathSegments) {
        if (pathSegments.length < 5 && !isValidDate(pathSegments[4])) {
            return Optional.empty();
        }

        return Optional.of(pathSegments[4]);
    }

    public static Optional<String> extractEndDate(String[] pathSegments) {
        if (pathSegments.length < 6 && !isValidDate(pathSegments[5])) {
            return Optional.empty();
        }

        return Optional.of(pathSegments[5]);
    }
}
