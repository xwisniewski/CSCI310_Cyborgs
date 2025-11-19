package com.example.csci310_teamproj.auth;


import static org.junit.Assert.*;
import org.junit.Test;

public class RegisterValidationTest_EmailDomain {

    private boolean validDomain(String email) {
        return email.endsWith("@usc.edu");
    }

    @Test
    public void testBadDomain() {
        assertFalse(validDomain("hello@gmail.com"));
    }

    @Test
    public void testCorrectDomain() {
        assertTrue(validDomain("test@usc.edu"));
    }
}
