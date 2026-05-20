package org.example.workflow.tcp.server;

import com.google.gson.JsonObject;
import com.google.gson.JsonParser;
import org.example.workflow.model.Team;
import org.example.workflow.model.User;
import org.example.workflow.state.AppState;
import org.example.workflow.tcp.protocol.PacketType;
import org.example.workflow.tcp.protocol.ResponsePacket;
import org.example.workflow.util.JsonMapper;
import com.google.gson.annotations.SerializedName;
import java.util.ArrayList;
import java.util.List;
import java.util.Optional;
import java.util.UUID;

/**
 * TeamService — server-side handler for team-related requests.
 *
 * Queries AppState directly (no DB hit — state is warm from StateLoader).
 */
public class TeamService {

    private final AppState appState = AppState.getInstance();

    /**
     * Handles GET_TEAM_MEMBERS request.
     *
     * Expected payload: { "teamId": "<UUID string>" }
     * Response payload: { "members": [ { User JSON }, ... ] }
     */
    public ResponsePacket getTeamMembers(String payload) {
        try {
            JsonObject json   = JsonParser.parseString(payload).getAsJsonObject();
            UUID       teamId = UUID.fromString(json.get("teamId").getAsString());

            Optional<Team> teamOpt = appState.getTeamById(teamId);
            if (teamOpt.isEmpty()) {
                return new ResponsePacket(PacketType.ERROR, "Team not found: " + teamId);  // ← was returning payload
            }


            Team       team    = teamOpt.get();
            List<User> members = resolveMembers(team);
            System.out.println(members.size());
            String responsePayload = JsonMapper.toJson(new MembersResponse(members));
            return new ResponsePacket(PacketType.SUCCESS, "OK", responsePayload, null);  // ← 4-arg constructor

        } catch (IllegalArgumentException e) {
            return new ResponsePacket(PacketType.ERROR, "Invalid teamId format");
        } catch (Exception e) {
            return new ResponsePacket(PacketType.ERROR, "Failed to fetch team members: " + e.getMessage());
        }
    }
    private List<User> resolveMembers(Team team) {
        List<User> result = new ArrayList<>();
        for (UUID memberId : team.getMemberIds()) {
            appState.getUserById(memberId).ifPresent(result::add);
        }
        return result;
    }

    // ── Inner DTO ────────────────────────────────────────────────────

    /** Wraps the member list for JSON serialization. */
    public static class MembersResponse {
        public final List<User> members;
        public MembersResponse(List<User> members) { this.members = members; }
    }
}