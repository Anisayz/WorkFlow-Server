package org.example.workflow.db.repository;

import org.example.workflow.db.JsonFileHandler;
import org.example.workflow.state.AppState;

import java.util.List;
import java.util.Optional;
import java.util.UUID;

/**
 * BaseRepository<T>
 *
 * Defines the two-tier read contract all repositories follow:
 *
 *   loadAll()   — reads directly from the JSON file.
 *                 Called ONLY by StateLoader at startup.
 *
 *   findAll()   — reads from AppState (in-memory).
 *                 Called at runtime by all server modules.
 *
 * Subclasses provide the AppState collection and the JsonFileHandler,
 * then implement findById() and save() / delete() on top.
 */
public abstract class BaseRepository<T> {

    protected final JsonFileHandler<T> handler;
    protected final AppState           state;

    protected BaseRepository(JsonFileHandler<T> handler) {
        this.handler = handler;
        this.state   = AppState.getInstance();
    }

    // ── Startup load (JSON → AppState) ────────────────────────────────────────

    /** Reads all records from the JSON file. Called once by StateLoader. */
    public List<T> loadAll() {
        return handler.readAll();
    }

    // ── Runtime reads (AppState) ──────────────────────────────────────────────

    /** Returns all records from the in-memory store. */
    public abstract List<T> findAll();

    /** Looks up a single record by UUID from the in-memory store. */
    public abstract Optional<T> findById(UUID id);

    // ── Writes (AppState + JSON) ──────────────────────────────────────────────

    /**
     * Persists a record: updates AppState and overwrites the JSON file.
     * Used for both create and update.
     */
    public abstract void save(T entity);

    /** Removes a record from AppState and rewrites the JSON file. */
    public abstract void delete(UUID id);

    // ── Shared write helper ───────────────────────────────────────────────────

    /** Flushes the current in-memory list to disk. Called after every mutation. */
    protected void persist() {
        handler.writeAll(findAll());
    }
}