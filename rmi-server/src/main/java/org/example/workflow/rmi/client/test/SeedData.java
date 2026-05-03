package org.example.workflow.rmi.client.test;

import com.google.gson.Gson;
import com.google.gson.GsonBuilder;
import com.google.gson.TypeAdapter;
import com.google.gson.stream.JsonReader;
import com.google.gson.stream.JsonToken;
import com.google.gson.stream.JsonWriter;
import org.example.workflow.model.Team;
import org.example.workflow.model.User;
import org.example.workflow.model.enums.Role;

import java.io.FileWriter;
import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Paths;
import java.time.LocalDate;
import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;
import java.util.List;
import java.util.UUID;

/**
 * ══════════════════════════════════════════════════════════════════
 *  SeedData — Crée des données de test dans data/
 * ══════════════════════════════════════════════════════════════════
 *
 *  Lancez ce main() UNE FOIS avant de démarrer le serveur RMI.
 *  Il crée :
 *    data/users.json  → 1 leader + 1 membre
 *    data/teams.json  → 1 équipe avec les 2 membres
 *    data/tasks.json  → fichier vide (prêt à recevoir des tâches)
 *
 *  Les UUIDs affichés dans la console sont ceux à coller dans
 *  RmiTestClient.java (LEADER_ID, MEMBER_ID, TEAM_ID).
 *
 *  NOTE : Les UUIDs sont fixes (hardcodés) pour faciliter les tests.
 */
public class SeedData {

    // UUIDs fixes pour faciliter les tests
    static final UUID LEADER_ID = UUID.fromString("00000000-0000-0000-0000-000000000001");
    static final UUID MEMBER_ID = UUID.fromString("00000000-0000-0000-0000-000000000002");
    static final UUID TEAM_ID   = UUID.fromString("00000000-0000-0000-0000-000000000010");

    public static void main(String[] args) throws IOException {
        // Créer le dossier data/ s'il n'existe pas
        Files.createDirectories(Paths.get("data"));

        Gson gson = buildGson();

        // ── Users ─────────────────────────────────────────────────────────────────
        User leader = new User();
        leader.setId(LEADER_ID);
        leader.setUsername("leader");
        leader.setEmail("leader@workflow.com");
        leader.setHashedPassword("$2a$10$fakehashfortest");  // BCrypt fictif pour les tests
        leader.setRole(Role.LEADER);
        leader.setTeamId(TEAM_ID);

        User member = new User();
        member.setId(MEMBER_ID);
        member.setUsername("member");
        member.setEmail("member@workflow.com");
        member.setHashedPassword("$2a$10$fakehashfortest");
        member.setRole(Role.MEMBER);
        member.setTeamId(TEAM_ID);

        writeJson("data/users.json", gson, List.of(leader, member));

        // ── Teams ─────────────────────────────────────────────────────────────────
        Team team = new Team(TEAM_ID, "Équipe RMI", "INVITE123", LEADER_ID);
        team.addMember(MEMBER_ID);

        writeJson("data/teams.json", gson, List.of(team));

        // ── Tasks (vide au départ) ────────────────────────────────────────────────
        writeJson("data/tasks.json", gson, List.of());

        // ── Messages (vide) ───────────────────────────────────────────────────────
        writeJson("data/messages.json", gson, List.of());

        // ── Résumé ────────────────────────────────────────────────────────────────
        System.out.println("╔══════════════════════════════════════════════╗");
        System.out.println("║       SeedData — Données créées              ║");
        System.out.println("╚══════════════════════════════════════════════╝");
        System.out.println();
        System.out.println("  LEADER_ID = " + LEADER_ID);
        System.out.println("  MEMBER_ID = " + MEMBER_ID);
        System.out.println("  TEAM_ID   = " + TEAM_ID);
        System.out.println();
        System.out.println("  Fichiers créés :");
        System.out.println("    data/users.json    → 2 utilisateurs");
        System.out.println("    data/teams.json    → 1 équipe");
        System.out.println("    data/tasks.json    → vide");
        System.out.println("    data/messages.json → vide");
        System.out.println();
        System.out.println("  Ces UUIDs sont déjà dans RmiTestClient.java.");
        System.out.println("  Démarrez maintenant RmiServerApp puis RmiTestClient.");
    }

    private static <T> void writeJson(String path, Gson gson, List<T> data) throws IOException {
        try (FileWriter writer = new FileWriter(path)) {
            gson.toJson(data, writer);
        }
        System.out.println("[SeedData] Écrit : " + path);
    }

    /** Gson identique à celui du DatabaseManager du projet */
    private static Gson buildGson() {
        return new GsonBuilder()
                .registerTypeAdapter(UUID.class, new TypeAdapter<UUID>() {
                    @Override public void write(JsonWriter out, UUID v) throws IOException {
                        if (v == null) { out.nullValue(); return; }
                        out.value(v.toString());
                    }
                    @Override public UUID read(JsonReader in) throws IOException {
                        if (in.peek() == JsonToken.NULL) { in.nextNull(); return null; }
                        return UUID.fromString(in.nextString());
                    }
                })
                .registerTypeAdapter(LocalDateTime.class, new TypeAdapter<LocalDateTime>() {
                    private final DateTimeFormatter fmt = DateTimeFormatter.ISO_LOCAL_DATE_TIME;
                    @Override public void write(JsonWriter out, LocalDateTime v) throws IOException {
                        if (v == null) { out.nullValue(); return; }
                        out.value(v.format(fmt));
                    }
                    @Override public LocalDateTime read(JsonReader in) throws IOException {
                        if (in.peek() == JsonToken.NULL) { in.nextNull(); return null; }
                        return LocalDateTime.parse(in.nextString(), fmt);
                    }
                })
                .registerTypeAdapter(LocalDate.class, new TypeAdapter<LocalDate>() {
                    private final DateTimeFormatter fmt = DateTimeFormatter.ISO_LOCAL_DATE;
                    @Override public void write(JsonWriter out, LocalDate v) throws IOException {
                        if (v == null) { out.nullValue(); return; }
                        out.value(v.format(fmt));
                    }
                    @Override public LocalDate read(JsonReader in) throws IOException {
                        if (in.peek() == JsonToken.NULL) { in.nextNull(); return null; }
                        return LocalDate.parse(in.nextString(), fmt);
                    }
                })
                .setPrettyPrinting()
                .serializeNulls()
                .create();
    }
}
