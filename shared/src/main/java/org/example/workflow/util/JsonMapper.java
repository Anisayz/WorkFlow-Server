package org.example.workflow.util;

import com.google.gson.*;
import com.google.gson.stream.*;

import java.io.IOException;
import java.time.LocalDate;
import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;
import java.util.UUID;

/**
 * JsonMapper
 *
 * Self-contained JSON serialization utility for network transmission.
 * Used by all four server modules to serialize/deserialize protocol packets.
 *
 * Owns its own Gson instance with all required type adapters.
 * This is NOT for file I/O — that is handled by JsonFileHandler.
 */
public final class JsonMapper {

    private static final Gson GSON = new GsonBuilder()
            .registerTypeAdapter(UUID.class,          new UUIDAdapter())
            .registerTypeAdapter(LocalDateTime.class,  new LocalDateTimeAdapter())
            .registerTypeAdapter(LocalDate.class,      new LocalDateAdapter())

            .serializeNulls()
            .create();

    private JsonMapper() {}

    // ── Public API ────────────────────────────────────────────────────────────

    public static String toJson(Object object) {
        return GSON.toJson(object);
    }

    public static <T> T fromJson(String json, Class<T> clazz) {
        if (json == null || json.trim().isEmpty()) return null;
        return GSON.fromJson(json, clazz);
    }

    /** Exposes the Gson instance for cases that need TypeToken (e.g. List<T>). */
    public static Gson getGson() {
        return GSON;
    }

    // ── Type adapters ─────────────────────────────────────────────────────────

    /** UUID ↔ plain JSON string: "550e8400-e29b-41d4-a716-446655440000" */
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

    /** LocalDateTime ↔ ISO string: "2025-04-26T14:30:00" */
    private static class LocalDateTimeAdapter extends TypeAdapter<LocalDateTime> {
        private static final DateTimeFormatter FMT = DateTimeFormatter.ISO_LOCAL_DATE_TIME;
        @Override
        public void write(JsonWriter out, LocalDateTime v) throws IOException {
            if (v == null) { out.nullValue(); return; }
            out.value(v.format(FMT));
        }
        @Override
        public LocalDateTime read(JsonReader in) throws IOException {
            if (in.peek() == JsonToken.NULL) { in.nextNull(); return null; }
            return LocalDateTime.parse(in.nextString(), FMT);
        }
    }

    /** LocalDate ↔ ISO string: "2025-04-26" */
    private static class LocalDateAdapter extends TypeAdapter<LocalDate> {
        private static final DateTimeFormatter FMT = DateTimeFormatter.ISO_LOCAL_DATE;
        @Override
        public void write(JsonWriter out, LocalDate v) throws IOException {
            if (v == null) { out.nullValue(); return; }
            out.value(v.format(FMT));
        }
        @Override
        public LocalDate read(JsonReader in) throws IOException {
            if (in.peek() == JsonToken.NULL) { in.nextNull(); return null; }
            return LocalDate.parse(in.nextString(), FMT);
        }
    }
}