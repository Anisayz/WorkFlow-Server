package org.example.workflow.state;

import org.example.workflow.model.*;

import java.time.Instant;
import java.util.*;
import java.util.concurrent.ConcurrentHashMap;

/**
 * AppState — central in-memory store.
 *
 * Loaded ONCE at startup via StateLoader.load().
 * Shared across all four servers (TCP, UDP, RMI, JMS).
 *
 * This class is a DUMB STORE — no query logic lives here.
 * All business queries belong in the repository layer.
 *
 * Thread-safety: all maps are ConcurrentHashMap.
 */
public final class AppState {


    private static volatile AppState instance;

    private AppState() {}

    public static AppState getInstance() {
        if (instance == null) {
            synchronized (AppState.class) {
                if (instance == null) {
                    instance = new AppState();
                }
            }
        }
        return instance;
    }


    private final Map<UUID, User>    users    = new ConcurrentHashMap<>();
    private final Map<UUID, Team>    teams    = new ConcurrentHashMap<>();
    private final Map<UUID, Task>    tasks    = new ConcurrentHashMap<>();
    private final Map<UUID, Message> messages = new ConcurrentHashMap<>();

    /**
     * Active authenticated sessions.
     * Key   = session token (UUID generated at login).
     * Value = authenticated User.
     * Owned by: TCP auth module.
     */
    private final Map<UUID, User> sessions = new ConcurrentHashMap<>();

    /**
     * Online user presence.
     * Key   = user UUID.
     * Value = last heartbeat timestamp (epoch millis).
     * Owned by: UDP presence module.
     */
    private final Map<UUID, Long> onlineUsers = new ConcurrentHashMap<>();

    public Map<UUID, User>    getUsers()    { return Collections.unmodifiableMap(users); }
    public Map<UUID, Team>    getTeams()    { return Collections.unmodifiableMap(teams); }
    public Map<UUID, Task>    getTasks()    { return Collections.unmodifiableMap(tasks); }
    public Map<UUID, Message> getMessages() { return Collections.unmodifiableMap(messages); }

    // Direct lookups — O(1), used heavily by all repositories
    public Optional<User>    getUserById(UUID id)    { return Optional.ofNullable(users.get(id)); }
    public Optional<Team>    getTeamById(UUID id)    { return Optional.ofNullable(teams.get(id)); }
    public Optional<Task>    getTaskById(UUID id)    { return Optional.ofNullable(tasks.get(id)); }
    public Optional<Message> getMessageById(UUID id) { return Optional.ofNullable(messages.get(id)); }


    public void putUser(User u)       { users.put(u.getId(), u); }
    public void putTeam(Team t)       { teams.put(t.getId(), t); }
    public void putTask(Task t)       { tasks.put(t.getId(), t); }
    public void putMessage(Message m) { messages.put(m.getId(), m); }

    public void removeUser(UUID id)    { users.remove(id); }
    public void removeTeam(UUID id)    { teams.remove(id); }
    public void removeTask(UUID id)    { tasks.remove(id); }
    public void removeMessage(UUID id) { messages.remove(id); }

    // Session management — owned by TCP auth module

    /** Creates a new session after a successful login. Returns the session token. */
    public UUID createSession(User user) {
        UUID token = UUID.randomUUID();
        sessions.put(token, user);
        return token;
    }

    /** Looks up the authenticated User for a given session token. */
    public Optional<User> getUserBySession(UUID token) {
        return Optional.ofNullable(sessions.get(token));
    }

    /** Removes a session on logout or timeout. */
    public void invalidateSession(UUID token) {
        User user = sessions.remove(token);
        if (user != null) setOffline(user.getId());
    }

    /** Removes all sessions for a user (forced logout). */
    public void invalidateAllSessionsForUser(UUID userId) {
        sessions.entrySet().removeIf(e -> {
            if (userId.equals(e.getValue().getId())) {
                setOffline(userId);
                return true;
            }
            return false;
        });
    }

    public boolean         isSessionValid(UUID token)  { return sessions.containsKey(token); }
    public int             getActiveSessionCount()     { return sessions.size(); }
    public Map<UUID, User> getSessions()               { return Collections.unmodifiableMap(sessions); }

    // Presence management — owned by UDP module

    /** Marks a user online, recording the current heartbeat timestamp. */
    public void setOnline(UUID userId)  { onlineUsers.put(userId, Instant.now().toEpochMilli()); }

    /** Marks a user offline. */
    public void setOffline(UUID userId) { onlineUsers.remove(userId); }

    public boolean         isOnline(UUID userId)      { return onlineUsers.containsKey(userId); }
    public long            getLastHeartbeat(UUID userId) { return onlineUsers.getOrDefault(userId, -1L); }
    public Set<UUID>       getOnlineUserIds()         { return Collections.unmodifiableSet(onlineUsers.keySet()); }
    public Map<UUID, Long> getOnlineUsers()           { return Collections.unmodifiableMap(onlineUsers); }

    /**
     * Evicts users whose last heartbeat is older than thresholdMillis.
     * Called by UDP HeartbeatMonitor on a scheduled thread.
     *
     * @return the evicted UUIDs so the caller can broadcast offline events
     */
    public Set<UUID> evictStalePresence(long thresholdMillis) {
        long cutoff = Instant.now().toEpochMilli() - thresholdMillis;
        Set<UUID> evicted = new HashSet<>();
        onlineUsers.entrySet().removeIf(e -> {
            if (e.getValue() < cutoff) {
                evicted.add(e.getKey());
                return true;
            }
            return false;
        });
        return evicted;
    }

    // Diagnostics

    @Override
    public String toString() {
        return String.format(
                "AppState { users=%d, teams=%d, tasks=%d, messages=%d, sessions=%d, online=%d }",
                users.size(), teams.size(), tasks.size(),
                messages.size(), sessions.size(), onlineUsers.size()
        );
    }
}
