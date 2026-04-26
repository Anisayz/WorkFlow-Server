package org.example.workflow.db;

import com.google.gson.*;
import com.google.gson.stream.*;
import com.google.gson.reflect.TypeToken;
import org.example.workflow.model.*;

import java.io.IOException;
import java.time.LocalDate;
import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;
import java.util.List;
import java.util.UUID;

/**
 * DatabaseManager
 *
 * Single source of truth for:
 *   1. The shared Gson instance (all type adapters registered here)
 *   2. Pre-built JsonFileHandler instances, one per entity
 *
 * Repositories obtain their handler via DatabaseManager.getInstance().get*Handler().
 * This guarantees all repositories share the same Gson config and file paths.
 */
public final class DatabaseManager {

    // ── File paths ────────────────────────────────────────────────────────────
    private static final String DATA_DIR     = "data/";
    public  static final String USERS_FILE   = DATA_DIR + "users.json";
    public  static final String TEAMS_FILE   = DATA_DIR + "teams.json";
    public  static final String TASKS_FILE   = DATA_DIR + "tasks.json";
    public  static final String MESSAGES_FILE= DATA_DIR + "messages.json";

    // ── Singleton ─────────────────────────────────────────────────────────────
    private static volatile DatabaseManager instance;

    public static DatabaseManager getInstance() {
        if (instance == null) {
            synchronized (DatabaseManager.class) {
                if (instance == null) instance = new DatabaseManager();
            }
        }
        return instance;
    }

    // ── Gson & handlers ───────────────────────────────────────────────────────
    private final Gson                   gson;
    private final JsonFileHandler<User>    userHandler;
    private final JsonFileHandler<Team>    teamHandler;
    private final JsonFileHandler<Task>    taskHandler;
    private final JsonFileHandler<Message> messageHandler;

    private DatabaseManager() {
        this.gson = buildGson();
        this.userHandler    = new JsonFileHandler<>(USERS_FILE,    gson, new TypeToken<List<User>>(){});
        this.teamHandler    = new JsonFileHandler<>(TEAMS_FILE,    gson, new TypeToken<List<Team>>(){});
        this.taskHandler    = new JsonFileHandler<>(TASKS_FILE,    gson, new TypeToken<List<Task>>(){});
        this.messageHandler = new JsonFileHandler<>(MESSAGES_FILE, gson, new TypeToken<List<Message>>(){});
    }

    public Gson                   getGson()           { return gson; }
    public JsonFileHandler<User>    getUserHandler()    { return userHandler; }
    public JsonFileHandler<Team>    getTeamHandler()    { return teamHandler; }
    public JsonFileHandler<Task>    getTaskHandler()    { return taskHandler; }
    public JsonFileHandler<Message> getMessageHandler() { return messageHandler; }

    // ── Gson builder ──────────────────────────────────────────────────────────

    private static Gson buildGson() {
        return new GsonBuilder()
                .registerTypeAdapter(UUID.class,          new UUIDAdapter())
                .registerTypeAdapter(LocalDateTime.class,  new LocalDateTimeAdapter())
                .registerTypeAdapter(LocalDate.class,      new LocalDateAdapter())
                .setPrettyPrinting()
                .serializeNulls()
                .create();
    }

    // ── Type adapters ─────────────────────────────────────────────────────────

    /** Serializes UUID as a plain JSON string: "550e8400-e29b-41d4-a716-446655440000" */
    private static class UUIDAdapter extends TypeAdapter<UUID> {
        @Override
        public void write(JsonWriter out, UUID v) throws IOException {
            if (v == null) { out.nullValue(); return; }
            out.value(v.toString());
        }
        @Override
        public UUID read(JsonReader in) throws IOException {
            if (in.peek() == JsonToken.NULL) { in.nextNull(); return null; }
            return UUID.fromString(in.nextString());
        }
    }

    private static final DateTimeFormatter DT_FMT = DateTimeFormatter.ISO_LOCAL_DATE_TIME;
    private static final DateTimeFormatter D_FMT  = DateTimeFormatter.ISO_LOCAL_DATE;

    /** Serializes LocalDateTime as ISO string: "2025-04-26T14:30:00" */
    private static class LocalDateTimeAdapter extends TypeAdapter<LocalDateTime> {
        @Override
        public void write(JsonWriter out, LocalDateTime v) throws IOException {
            if (v == null) { out.nullValue(); return; }
            out.value(v.format(DT_FMT));
        }
        @Override
        public LocalDateTime read(JsonReader in) throws IOException {
            if (in.peek() == JsonToken.NULL) { in.nextNull(); return null; }
            return LocalDateTime.parse(in.nextString(), DT_FMT);
        }
    }

    /** Serializes LocalDate as ISO string: "2025-04-26" */
    private static class LocalDateAdapter extends TypeAdapter<LocalDate> {
        @Override
        public void write(JsonWriter out, LocalDate v) throws IOException {
            if (v == null) { out.nullValue(); return; }
            out.value(v.format(D_FMT));
        }
        @Override
        public LocalDate read(JsonReader in) throws IOException {
            if (in.peek() == JsonToken.NULL) { in.nextNull(); return null; }
            return LocalDate.parse(in.nextString(), D_FMT);
        }
    }
}