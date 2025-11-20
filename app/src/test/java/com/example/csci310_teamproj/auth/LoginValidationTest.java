package com.example.csci310_teamproj.auth;

import static org.junit.Assert.*;
import org.junit.Test;

public class LoginValidationTest {

    public boolean validate(String email, String password) {
        return !(email.isEmpty() || password.isEmpty());
    }

    @Test
    public void testEmailEmpty() {
        assertFalse(validate("", "123"));
    }

    @Test
    public void testPasswordEmpty() {
        assertFalse(validate("test@usc.edu", ""));
    }

    @Test
    public void testBothEmpty() {
        assertFalse(validate("", ""));
    }

    @Test
    public void testAllGood() {
        assertTrue(validate("test@usc.edu", "123"));
    }
}
