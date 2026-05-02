package org.example.workflow.tcp.auth;

import com.google.gson.JsonObject;
import com.google.gson.JsonParser;
import org.example.workflow.db.repository.UserRepository;
import org.example.workflow.model.User;
import org.example.workflow.model.enums.Role;
import org.example.workflow.tcp.protocol.PacketType;
import org.example.workflow.tcp.protocol.ResponsePacket;
import org.example.workflow.tcp.session.SessionManager;
import org.example.workflow.util.JsonMapper;
import org.example.workflow.util.PasswordUtils;
import org.example.workflow.util.ValidationUtils;
import org.example.workflow.util.ValidationUtils.ValidationResult;

import java.util.UUID;
import java.util.logging.Logger;

/**
 * AuthService — server-side authentication logic.
 *
 * Called by ClientHandler on every LOGIN, REGISTER, LOGOUT packet.
 * All methods return a ResponsePacket ready to be sent back to the client.
 */
public class AuthService {

    private static final Logger        log         = Logger.getLogger(AuthService.class.getName());
    private final        UserRepository userRepo    = new UserRepository();
    private final        SessionManager sessionMgr  = SessionManager.getInstance();

    // ── Login ─────────────────────────────────────────────────────────────────

    /**
     * Authenticates a user and opens a session.
     *
     * Expected payload: { "username": "...", "password": "..." }
     */
    public ResponsePacket login(String payload) {
        try {
            JsonObject json     = JsonParser.parseString(payload).getAsJsonObject();
            log.info("[AuthService] Login payload received: " + json);
            String     email    = json.get("email").getAsString().trim();   // ← was "username"
            String     password = json.get("password").getAsString();

            // 1. Basic validation
            ValidationResult vEmail = ValidationUtils.validateEmail(email);
            if (vEmail.isInvalid())
                return new ResponsePacket(PacketType.ERROR, vEmail.getMessage());

            ValidationResult vPass = ValidationUtils.validatePassword(password);
            if (vPass.isInvalid())
                return new ResponsePacket(PacketType.ERROR, vPass.getMessage());

            // 2. User lookup by email  ← was findByUsername
            User user = userRepo.findByEmail(email).orElse(null);
            if (user == null)
                return new ResponsePacket(PacketType.ERROR, "No account found for that email");

            // 3. Password check
            if (!PasswordUtils.verify(password, user.getHashedPassword()))
                return new ResponsePacket(PacketType.ERROR, "Incorrect password");

            // 4. Create session + mark online
            UUID   token    = sessionMgr.createSession(user);
            String tokenStr = token.toString();

            log.info("[AuthService] LOGIN success — email=" + email + " token=" + tokenStr);

            // 5. Return client-safe User + session token
            String userData = JsonMapper.toJson(user.toClientSafe());
            return new ResponsePacket(PacketType.SUCCESS, "Login successful", userData, tokenStr);

        } catch (Exception e) {
            log.severe("[AuthService] LOGIN error: " + e.getMessage());
            return new ResponsePacket(PacketType.ERROR, "Login failed: " + e.getMessage());
        }
    }

    // ── Register ──────────────────────────────────────────────────────────────

    /**
     * Registers a new user. Does NOT auto-login — client must call LOGIN after.
     *
     * Expected payload: { "username": "...", "email": "...", "password": "...", "role": "LEADER|MEMBER" }
     */
    public ResponsePacket register(String payload) {
        try {
            JsonObject json     = JsonParser.parseString(payload).getAsJsonObject();
            String     username = json.get("username").getAsString().trim();
            String     email    = json.get("email").getAsString().trim();
            String     password = json.get("password").getAsString();
            Role       role     = Role.valueOf(json.get("role").getAsString().toUpperCase());

            // 1. Validate all fields
            ValidationResult vUser = ValidationUtils.validateUsername(username);
            if (vUser.isInvalid())
                return new ResponsePacket(PacketType.ERROR, vUser.getMessage());

            ValidationResult vEmail = ValidationUtils.validateEmail(email);
            if (vEmail.isInvalid())
                return new ResponsePacket(PacketType.ERROR, vEmail.getMessage());

            ValidationResult vPass = ValidationUtils.validatePassword(password);
            if (vPass.isInvalid())
                return new ResponsePacket(PacketType.ERROR, vPass.getMessage());

            // 2. Uniqueness checks
            if (userRepo.usernameExists(username))
                return new ResponsePacket(PacketType.ERROR, "Username already taken");
            if (userRepo.emailExists(email))
                return new ResponsePacket(PacketType.ERROR, "Email already registered");

            // 3. Create and persist user
            User user = new User(
                    UUID.randomUUID(),
                    username,
                    email,
                    PasswordUtils.hash(password),
                    role
            );
            userRepo.save(user);

            log.info("[AuthService] REGISTER success — user=" + username);

            return new ResponsePacket(PacketType.SUCCESS, "Registration successful", null, null);

        } catch (IllegalArgumentException e) {
            return new ResponsePacket(PacketType.ERROR, "Invalid role — must be LEADER or MEMBER");
        } catch (Exception e) {
            log.severe("[AuthService] REGISTER error: " + e.getMessage());
            return new ResponsePacket(PacketType.ERROR, "Registration failed: " + e.getMessage());
        }
    }

    // ── Logout ────────────────────────────────────────────────────────────────

    /**
     * Invalidates a session.
     *
     * Expected payload: { "sessionToken": "uuid-string" }
     */
    public ResponsePacket logout(String payload) {
        try {
            JsonObject json     = JsonParser.parseString(payload).getAsJsonObject();
            String     tokenStr = json.get("sessionToken").getAsString();
            UUID       token    = UUID.fromString(tokenStr);

            sessionMgr.invalidateSession(token);

            log.info("[AuthService] LOGOUT — token=" + tokenStr);
            return new ResponsePacket(PacketType.SUCCESS, "Logged out", null, null);

        } catch (Exception e) {
            log.severe("[AuthService] LOGOUT error: " + e.getMessage());
            return new ResponsePacket(PacketType.ERROR, "Logout failed: " + e.getMessage());
        }
    }
}