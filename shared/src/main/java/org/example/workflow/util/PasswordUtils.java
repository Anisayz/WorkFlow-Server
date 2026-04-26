package org.example.workflow.util;

import org.mindrot.jbcrypt.BCrypt;

/**
 * PasswordUtils
 *
 * Wraps jBCrypt for password hashing and verification.
 * Used exclusively by the TCP auth module (AuthService).
 *
 * Never store or log plain-text passwords anywhere.
 * Never send hashedPassword to the client — always call User.toClientSafe() first.
 */
public final class PasswordUtils {

    private static final int BCRYPT_ROUNDS = 12; // work factor — increase as hardware improves

    private PasswordUtils() {}

    /**
     * Hashes a plain-text password.
     * Store the returned string in User.hashedPassword.
     *
     * @param plainPassword  raw password from the registration request
     * @return               BCrypt hash string
     * @throws IllegalArgumentException if password is null or blank
     */
    public static String hash(String plainPassword) {
        if (plainPassword == null || plainPassword.trim().isEmpty())
            throw new IllegalArgumentException("Password must not be blank");
        return BCrypt.hashpw(plainPassword, BCrypt.gensalt(BCRYPT_ROUNDS));
    }

    /**
     * Verifies a plain-text password against a stored BCrypt hash.
     *
     * @param plainPassword   raw password from the login request
     * @param hashedPassword  stored hash from User.getHashedPassword()
     * @return true if the password matches
     */
    public static boolean verify(String plainPassword, String hashedPassword) {
        if (plainPassword == null || hashedPassword == null) return false;
        try {
            return BCrypt.checkpw(plainPassword, hashedPassword);
        } catch (IllegalArgumentException e) {
            // hashedPassword was not a valid BCrypt string
            return false;
        }
    }
}