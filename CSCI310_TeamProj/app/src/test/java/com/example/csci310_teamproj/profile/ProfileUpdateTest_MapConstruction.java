package com.example.csci310_teamproj.profile;

import static org.junit.Assert.*;
import org.junit.Test;

import java.util.Map;
import java.util.HashMap;

public class ProfileUpdateTest_MapConstruction {

    private Map<String, Object> buildMap(String birth, String bio, String aff) {
        Map<String, Object> updates = new HashMap<>();
        updates.put("birthDate", birth);
        updates.put("bio", bio);
        updates.put("affiliation", aff);
        return updates;
    }

    @Test
    public void testMapKeys() {
        Map<String, Object> m = buildMap("01/01/2000", "bio", "Viterbi");

        assertTrue(m.containsKey("birthDate"));
        assertTrue(m.containsKey("bio"));
        assertTrue(m.containsKey("affiliation"));
        assertEquals(3, m.size());
    }
}
