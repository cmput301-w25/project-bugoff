/**
 * The {@code InputValidator} class provides static methods for validating user input
 * in the AddMood functionality. It ensures that mood selections and textual inputs meet
 * the required criteria.
 *
 * Key Features:
 *
 *     Validates mood selections to ensure they are not empty or default values.
 *     Checks the length of reason text input to ensure it does not exceed the character limit.
 *     Provides descriptive error messages for invalid inputs.
 *
 */
package com.example.whimsy;

/**
 * The {@code InputValidator} class provides static methods for validating user input
 * in the AddMood functionality. It ensures that mood selections and textual inputs meet
 * the required criteria.
 */
public class InputValidator {

    /**
     * Validates that the provided mood is valid.
     * Assumes that the first spinner option is "Select an Emotion" which is considered invalid.
     *
     * @param mood the mood selected by the user.
     * @return {@code true} if the mood is valid; {@code false} otherwise.
     */
    public static boolean isValidMood(String mood) {
        return mood != null && !mood.trim().isEmpty() && !mood.equals("Select an Emotion");
    }

    /**
     * Validates the reason text input.
     *
     * @param reason the reason text provided by the user.
     * @return {@code null} if the reason is valid; otherwise, returns a descriptive error message.
     */
    public static String validateReason(String reason) {
        if (reason == null) return null;
        if (reason.length() > 200) {
            return "Reason exceeds 200 characters limit";
        }
        return null;
    }
}
