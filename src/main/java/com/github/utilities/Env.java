package com.github.utilities;

import io.github.cdimascio.dotenv.Dotenv;

public final class Env {
    public static final Dotenv env = Dotenv.load();

    public static String get(String key) throws IllegalStateException {
        String value = env.get(key);

        if (value == null) {
            throw new IllegalStateException("No key found with: " + key);
        }

        return value;
    }
}
