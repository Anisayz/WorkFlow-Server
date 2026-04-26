package org.example.workflow.util;

import java.util.regex.Pattern;

/**
 * ValidationUtils
 *
 * Stateless input validation used during registration and login.
 * Called by the TCP auth module before hitting the database.
 *
 * All methods return a ValidationResult so the caller gets both
 * a boolean and a human-readable error message to send back to the client.
 */
public final class ValidationUtils {

    // ── Constraints ───────────────────────────────────────────────────────────
    private static final int    USERNAME_MIN    = 3;
    private static final int    USERNAME_MAX    = 20;
    private static final int    PASSWORD_MIN    = 6;
    private static final int    PASSWORD_MAX    = 64;
    private static final int    TEAM_NAME_MIN   = 2;
    private static final int    TEAM_NAME_MAX   = 40;

    private static final Pattern USERNAME_PATTERN =
            Pattern.compile("^[a-zA-Z0-9_]+$");

    private static final Pattern EMAIL_PATTERN =
            Pattern.compile("^[^@\\s]+@[^@\\s]+\\.[^@\\s]+$");

    private ValidationUtils() {}

    // ── Validators ────────────────────────────────────────────────────────────

    public static ValidationResult validateUsername(String username) {
        if (username == null || username.trim().isEmpty())
            return ValidationResult.fail("Username must not be blank");
        if (username.length() < USERNAME_MIN)
            return ValidationResult.fail("Username must be at least " + USERNAME_MIN + " characters");
        if (username.length() > USERNAME_MAX)
            return ValidationResult.fail("Username must be at most " + USERNAME_MAX + " characters");
        if (!USERNAME_PATTERN.matcher(username).matches())
            return ValidationResult.fail("Username may only contain letters, digits, and underscores");
        return ValidationResult.ok();
    }

    public static ValidationResult validateEmail(String email) {
        if (email == null || email.trim().isEmpty())
            return ValidationResult.fail("Email must not be blank");
        if (!EMAIL_PATTERN.matcher(email).matches())
            return ValidationResult.fail("Invalid email address");
        return ValidationResult.ok();
    }

    public static ValidationResult validatePassword(String password) {
        if (password == null || password.trim().isEmpty())
            return ValidationResult.fail("Password must not be blank");
        if (password.length() < PASSWORD_MIN)
            return ValidationResult.fail("Password must be at least " + PASSWORD_MIN + " characters");
        if (password.length() > PASSWORD_MAX)
            return ValidationResult.fail("Password must be at most " + PASSWORD_MAX + " characters");
        return ValidationResult.ok();
    }

    public static ValidationResult validateTeamName(String name) {
        if (name == null || name.trim().isEmpty())
            return ValidationResult.fail("Team name must not be blank");
        if (name.length() < TEAM_NAME_MIN)
            return ValidationResult.fail("Team name must be at least " + TEAM_NAME_MIN + " characters");
        if (name.length() > TEAM_NAME_MAX)
            return ValidationResult.fail("Team name must be at most " + TEAM_NAME_MAX + " characters");
        return ValidationResult.ok();
    }

    public static ValidationResult validateInviteCode(String code) {
        if (code == null || code.trim().isEmpty())
            return ValidationResult.fail("Invite code must not be blank");
        if (code.length() != 8)
            return ValidationResult.fail("Invite code must be exactly 8 characters");
        return ValidationResult.ok();
    }

    // ── Generators ────────────────────────────────────────────────────────────

    /**
     * Generates a random 8-character uppercase invite code.
     * Called by TeamRepository when creating a new team.
     *
     * Example output: "A3FX92QK"
     */
    public static String generateInviteCode() {
        String chars = "ABCDEFGHIJKLMNOPQRSTUVWXYZ0123456789";
        StringBuilder sb = new StringBuilder(8);
        java.util.Random rng = new java.util.Random();
        for (int i = 0; i < 8; i++)
            sb.append(chars.charAt(rng.nextInt(chars.length())));
        return sb.toString();
    }

    // ── Result type ───────────────────────────────────────────────────────────

    public static final class ValidationResult {
        private final boolean valid;
        private final String  message;

        private ValidationResult(boolean valid, String message) {
            this.valid   = valid;
            this.message = message;
        }

        public static ValidationResult ok()           { return new ValidationResult(true, null); }
        public static ValidationResult fail(String m) { return new ValidationResult(false, m); }

        public boolean isValid()   { return valid; }
        public boolean isInvalid() { return !valid; }
        public String  getMessage(){ return message; }

        @Override
        public String toString() {
            return valid ? "OK" : "INVALID: " + message;
        }
    }
}