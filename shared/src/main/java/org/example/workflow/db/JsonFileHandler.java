package org.example.workflow.db;

import com.google.gson.Gson;
import com.google.gson.reflect.TypeToken;

import java.io.*;
import java.lang.reflect.Type;
import java.nio.charset.StandardCharsets;
import java.nio.file.*;
import java.util.ArrayList;
import java.util.List;
import java.util.logging.Logger;

/**
 * JsonFileHandler
 *
 * Low-level read/write of a single JSON file that stores a List<T>.
 * Every repository owns one instance pointed at its file.
 *
 * File layout:  data/users.json  →  [ { ... }, { ... } ]
 *
 * Thread-safety: readAll() and writeAll() are synchronized on the file path
 * string so concurrent calls from different server threads don't corrupt the file.
 */
public class JsonFileHandler<T> {

    private static final Logger log = Logger.getLogger(JsonFileHandler.class.getName());

    private final Path   filePath;
    private final Gson   gson;
    private final Type   listType;

    /**
     * @param filePath  path to the JSON file  (e.g. "data/users.json")
     * @param gson      shared Gson instance from DatabaseManager (has all adapters)
     * @param typeToken TypeToken for the list, e.g. new TypeToken<List<User>>(){}
     */
    public JsonFileHandler(String filePath, Gson gson, TypeToken<List<T>> typeToken) {
        this.filePath = Paths.get(filePath);
        this.gson     = gson;
        this.listType = typeToken.getType();
        ensureFileExists();
    }

    // ── Read ─────────────────────────────────────────────────────────────────

    /**
     * Reads all records from the JSON file.
     * Called only by StateLoader.load() at startup — not at runtime.
     */
    public synchronized List<T> readAll() {
        try (Reader reader = Files.newBufferedReader(filePath, StandardCharsets.UTF_8)) {
            List<T> result = gson.fromJson(reader, listType);
            return result != null ? result : new ArrayList<>();
        } catch (IOException e) {
            log.severe("Failed to read " + filePath + ": " + e.getMessage());
            return new ArrayList<>();
        }
    }

    // ── Write ─────────────────────────────────────────────────────────────────

    /**
     * Overwrites the entire JSON file with the provided list.
     * Repositories call this after every create / update / delete.
     * Writes to a temp file first, then atomically replaces, so a crash
     * mid-write never leaves a corrupt file.
     */
    public synchronized void writeAll(List<T> records) {
        Path tmp = filePath.resolveSibling(filePath.getFileName() + ".tmp");
        try (Writer writer = Files.newBufferedWriter(tmp, StandardCharsets.UTF_8)) {
            gson.toJson(records, listType, writer);
            Files.move(tmp, filePath,
                    StandardCopyOption.REPLACE_EXISTING,
                    StandardCopyOption.ATOMIC_MOVE);
        } catch (IOException e) {
            log.severe("Failed to write " + filePath + ": " + e.getMessage());
            try { Files.deleteIfExists(tmp); } catch (IOException ignored) {}
        }
    }

    // ── Init ──────────────────────────────────────────────────────────────────

    /** Creates the file and parent directories if they don't exist yet. */
    private void ensureFileExists() {
        try {
            Files.createDirectories(filePath.getParent());
            if (!Files.exists(filePath)) {
                Files.write(filePath, "[]".getBytes(StandardCharsets.UTF_8));
                log.info("Created empty JSON file: " + filePath);
            }
        } catch (IOException e) {
            throw new RuntimeException("Cannot initialise JSON file: " + filePath, e);
        }
    }
}