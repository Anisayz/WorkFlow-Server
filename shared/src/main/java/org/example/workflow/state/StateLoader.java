package org.example.workflow.state;

import org.example.workflow.db.repository.*;
import org.example.workflow.model.*;

import java.util.List;
import java.util.logging.Logger;

/**
 * StateLoader — runs ONCE per server at startup.
 *
 * Reads every record from the JSON files through the repositories
 * and bulk-loads them into AppState so all four servers start
 * with a fully warm in-memory cache.
 *
 * Usage (in every server's main()):
 *
 *   StateLoader.load();
 *   new TcpServer(8080).start();   // or UdpServerApp, RmiServerApp, JmsServerApp
 *
 * After this call returns, repositories no longer hit the JSON files
 * on reads — they query AppState directly. JSON files are only touched
 * on writes (create / update / delete) via the repository save() methods,
 * which also call the matching AppState.put*() to keep the cache in sync.
 */
public final class StateLoader {

    private static final Logger log = Logger.getLogger(StateLoader.class.getName());

    private StateLoader() {}

    public static void load() {
        log.info("[StateLoader] Starting state load...");
        long start = System.currentTimeMillis();

        AppState state = AppState.getInstance();

        loadUsers(state);
        loadTeams(state);
        loadTasks(state);
        loadMessages(state);

        long elapsed = System.currentTimeMillis() - start;
        log.info(String.format("[StateLoader] Done in %d ms — %s", elapsed, state));
    }

    // Private loaders — one per entity, each catches its own failures
    // so a corrupt messages file doesn't prevent users from loading.

    private static void loadUsers(AppState state) {
        try {
            List<User> users = new UserRepository().loadAll();
            users.forEach(state::putUser);
            log.info(String.format("[StateLoader] Loaded %d users", users.size()));
        } catch (Exception e) {
            log.severe("[StateLoader] Failed to load users: " + e.getMessage());
            throw new StateLoadException("users", e);
        }
    }

    private static void loadTeams(AppState state) {
        try {
            List<Team> teams = new TeamRepository().loadAll();
            teams.forEach(state::putTeam);
            log.info(String.format("[StateLoader] Loaded %d teams", teams.size()));
        } catch (Exception e) {
            log.severe("[StateLoader] Failed to load teams: " + e.getMessage());
            throw new StateLoadException("teams", e);
        }
    }

    private static void loadTasks(AppState state) {
        try {
            List<Task> tasks = new TaskRepository().loadAll();
            tasks.forEach(state::putTask);
            log.info(String.format("[StateLoader] Loaded %d tasks", tasks.size()));
        } catch (Exception e) {
            log.severe("[StateLoader] Failed to load tasks: " + e.getMessage());
            throw new StateLoadException("tasks", e);
        }
    }

    private static void loadMessages(AppState state) {
        try {
            List<Message> messages = new MessageRepository().loadAll();
            messages.forEach(state::putMessage);
            log.info(String.format("[StateLoader] Loaded %d messages", messages.size()));
        } catch (Exception e) {
            // Messages failing is non-fatal — server can still run without history
            log.warning("[StateLoader] Failed to load messages (non-fatal): " + e.getMessage());
        }
    }

    // Exception

    public static class StateLoadException extends RuntimeException {
        public StateLoadException(String entity, Throwable cause) {
            super("Critical failure loading entity: " + entity, cause);
        }
    }

    //Useful if a teammate manually edits a JSON file and needs to hot reload without restarting the server
    public static void reload() {
        log.info("[StateLoader] Reloading state...");
        AppState state = AppState.getInstance();
        // clear existing data first
        state.getUsers().keySet().forEach(state::removeUser);
        state.getTeams().keySet().forEach(state::removeTeam);
        state.getTasks().keySet().forEach(state::removeTask);
        load();
    }
}