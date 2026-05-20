package org.example.workflow.rmi.agent.server;

import org.example.workflow.rmi.agent.AgentTask;
import org.example.workflow.rmi.agent.AgentTeam;
import org.example.workflow.rmi.agent.AgentUser;
import org.example.workflow.rmi.agent.ServerContext;
import org.example.workflow.db.repository.TaskRepository;
import org.example.workflow.db.repository.TeamRepository;
import org.example.workflow.db.repository.UserRepository;
import org.example.workflow.model.Task;
import org.example.workflow.model.Team;
import org.example.workflow.model.User;
import org.example.workflow.state.AppState;

import java.util.List;
import java.util.UUID;
import java.util.stream.Collectors;

/**
 * ServerContextImpl — server-side implementation of ServerContext.
 *
 * Reads from AppState/repositories and converts server models (UUID-based)
 * into agent DTOs (String-based).
 */
public class ServerContextImpl implements ServerContext {

    private final UserRepository userRepo = new UserRepository();
    private final TeamRepository teamRepo = new TeamRepository();
    private final TaskRepository taskRepo = new TaskRepository();
    private final AppState       state    = AppState.getInstance();

    // ── Helpers ───────────────────────────────────────────────────────────────

    private UUID parse(String id) {
        try { return UUID.fromString(id); }
        catch (Exception e) { return null; }
    }

    private AgentUser toAgent(User u) {
        if (u == null) return null;
        boolean online = state.isOnline(u.getId());
        return new AgentUser(
                u.getId().toString(),
                u.getUsername(),
                u.getEmail(),
                u.getRole() != null ? u.getRole().name() : null,
                online,
                u.getTeamId() != null ? u.getTeamId().toString() : null
        );
    }

    private AgentTeam toAgent(Team t) {
        if (t == null) return null;
        List<String> memberIds = t.getMemberIds().stream()
                .map(UUID::toString).collect(Collectors.toList());
        return new AgentTeam(
                t.getId().toString(),
                t.getName(),
                t.getInviteCode(),
                t.getLeaderId() != null ? t.getLeaderId().toString() : null,
                memberIds
        );
    }

    private AgentTask toAgent(Task t) {
        if (t == null) return null;
        return new AgentTask(
                t.getId().toString(),
                t.getTitle(),
                t.getDescription(),
                t.getAssigneeId() != null ? t.getAssigneeId().toString() : null,
                t.getAssigneeName(),
                t.getTeamId() != null ? t.getTeamId().toString() : null,
                t.getStatus() != null ? t.getStatus().name() : null,
                t.getPriority() != null ? t.getPriority().name() : null,
                t.getDueDate() != null ? t.getDueDate().toString() : null
        );
    }

    // ── ServerContext API ─────────────────────────────────────────────────────

    @Override
    public AgentUser getUser(String userId) {
        UUID uid = parse(userId);
        if (uid == null) return null;
        return userRepo.findById(uid).map(this::toAgent).orElse(null);
    }

    @Override
    public AgentTeam getTeam(String teamId) {
        UUID tid = parse(teamId);
        if (tid == null) return null;
        return teamRepo.findById(tid).map(this::toAgent).orElse(null);
    }

    @Override
    public List<AgentUser> getTeamMembers(String teamId) {
        UUID tid = parse(teamId);
        if (tid == null) return List.of();
        return teamRepo.getMembers(tid).stream()
                .map(this::toAgent).collect(Collectors.toList());
    }

    @Override
    public List<AgentTask> getUserTasks(String userId) {
        UUID uid = parse(userId);
        if (uid == null) return List.of();
        return taskRepo.findByAssignee(uid).stream()
                .map(this::toAgent).collect(Collectors.toList());
    }

    @Override
    public List<AgentTask> getTeamTasks(String teamId) {
        UUID tid = parse(teamId);
        if (tid == null) return List.of();
        return taskRepo.findByTeam(tid).stream()
                .map(this::toAgent).collect(Collectors.toList());
    }
}