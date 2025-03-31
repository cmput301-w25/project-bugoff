package com.example.whimsy;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertFalse;
import static org.junit.Assert.assertNull;
import static org.junit.Assert.assertTrue;

import org.junit.Test;

public class InputValidatorTest {

    // ---------- Tests for isValidMood() ----------

    @Test
    public void testValidMood_returnsTrue() {
        assertTrue(InputValidator.isValidMood("Happy"));
    }

    @Test
    public void testNullMood_returnsFalse() {
        assertFalse(InputValidator.isValidMood(null));
    }

    @Test
    public void testEmptyMood_returnsFalse() {
        assertFalse(InputValidator.isValidMood(""));
    }

    @Test
    public void testWhitespaceMood_returnsFalse() {
        assertFalse(InputValidator.isValidMood("   "));
    }

    @Test
    public void testDefaultSpinnerText_returnsFalse() {
        assertFalse(InputValidator.isValidMood("Select an Emotion"));
    }

    // ---------- Tests for validateReason() ----------

    @Test
    public void testNullReason_returnsNull() {
        assertNull(InputValidator.validateReason(null));
    }

    @Test
    public void testValidReason_returnsNull() {
        assertNull(InputValidator.validateReason("I felt great today!"));
    }

    @Test
    public void testTooLongReason_returnsErrorMessage() {
        StringBuilder longReason = new StringBuilder();
        for (int i = 0; i < 201; i++) {
            longReason.append("a");
        }
        assertEquals("Reason exceeds 200 characters limit", InputValidator.validateReason(longReason.toString()));
    }
}