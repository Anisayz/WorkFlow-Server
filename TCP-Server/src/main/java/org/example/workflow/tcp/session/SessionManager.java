package org.example.workflow.tcp.session;

import org.example.workflow.model.User;
import org.example.workflow.state.AppState;
import org.example.workflow.tcp.server.ClientHandler;

import java.util.Map;
import java.util.Optional;
import java.util.UUID;
import java.util.concurrent.ConcurrentHashMap;

/**
 * SessionManager — server-side session registry.
 *
 * Two responsibilities:
 *   1. Delegates session token ↔ User mapping to AppState
 *      (so all 4 servers can validate tokens)
 *   2. Maintains a TCP-specific token ↔ ClientHandler map
 *      so the TCP module can push messages to a specific connected client
 */
public final class SessionManager {

    private static volatile SessionManager instance;

    public static SessionManager getInstance() {
        if (instance == null) {
            synchronized (SessionManager.class) {
                if (instance == null) instance = new SessionManager();
            }
        }
        return instance;
    }

    private final AppState                    appState = AppState.getInstance();
    /** Token → the live ClientHandler thread for that session */
    private final Map<UUID, ClientHandler>    handlers = new ConcurrentHashMap<>();

    private SessionManager() {}

    // ── Session lifecycle ─────────────────────────────────────────────────────

    /** Creates a session in AppState and registers the ClientHandler. */
    public UUID createSession(User user) {
        UUID token = appState.createSession(user);
        appState.setOnline(user.getId());
        return token;
    }

    /** Registers the ClientHandler so messages can be pushed to this client. */
    public void registerHandler(UUID token, ClientHandler handler) {
        handlers.put(token, handler);
    }

    /** Removes both the AppState session and the handler mapping. */
    public void invalidateSession(UUID token) {
        handlers.remove(token);
        appState.invalidateSession(token); // also calls setOffline internally
    }

    /** Called when a client disconnects without sending LOGOUT. */
    public void onClientDisconnected(UUID token) {
        if (token != null) invalidateSession(token);
    }

    // ── Lookups ───────────────────────────────────────────────────────────────

    public Optional<User>          getUserByToken(UUID token)    { return appState.getUserBySession(token); }
    public boolean                 isValid(UUID token)           { return appState.isSessionValid(token); }
    public Optional<ClientHandler> getHandler(UUID token)        { return Optional.ofNullable(handlers.get(token)); }
    public int                     getActiveCount()              { return handlers.size(); }
}