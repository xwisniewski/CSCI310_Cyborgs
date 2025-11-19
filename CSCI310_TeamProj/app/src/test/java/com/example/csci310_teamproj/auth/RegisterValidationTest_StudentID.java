package com.example.csci310_teamproj.auth;

import static org.junit.Assert.*;

import org.junit.Test;

public class RegisterValidationTest_StudentID {

    private boolean isValidStudentId(String id) {
        return id.length() == 10 && id.matches("\\d{10}");
    }

    @Test
    public void testTooShort() { assertFalse(isValidStudentId("12345")); }

    @Test
    public void testNonDigits() { assertFalse(isValidStudentId("ABCDEFGHIJ")); }

    @Test
    public void testCorrect() { assertTrue(isValidStudentId("1234567890")); }
}
