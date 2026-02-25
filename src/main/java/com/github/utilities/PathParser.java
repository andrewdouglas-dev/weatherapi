package com.github.utilities;

import java.util.regex.Matcher;
import java.util.regex.Pattern;

public class PathParser {
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
}
