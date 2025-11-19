package com.example.csci310_teamproj.util;

public class TestMode {
    private static boolean testMode = false;

    public static void enable() {
        testMode = true;
    }

    public static boolean isEnabled() {
        return testMode;
    }
}
