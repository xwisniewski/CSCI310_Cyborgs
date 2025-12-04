package com.example.csci310_teamproj.util;

public class TestUtils {
    public static boolean isRunningTest() {
        try {
            Class.forName("androidx.test.espresso.Espresso");
            return true; // We're in androidTest
        } catch (ClassNotFoundException e) {
            return false; // Normal app run
        }
    }
}
