package org.example.workflow.db.repository;

import org.example.workflow.db.DatabaseManager;
import org.example.workflow.model.Task;
import org.example.workflow.model.enums.Priority;
import org.example.workflow.model.enums.TaskStatus;

import java.util.*;
import java.util.stream.Collectors;

public class TaskRepository extends BaseRepository<Task> {

    public TaskRepository() {
        super(DatabaseManager.getInstance().getTaskHandler());
    }

    // ── BaseRepository ────────────────────────────────────────────────────────

    @Override
    public List<Task> findAll() {
        return Collections.unmodifiableList(
                new ArrayList<>(state.getTasks().values())
        );
    }

    @Override
    public Optional<Task> findById(UUID id) {
        return state.getTaskById(id);
    }

    @Override
    public void save(Task task) {
        state.putTask(task);
        persist();
    }

    @Override
    public void delete(UUID id) {
        state.removeTask(id);
        persist();
    }

    // ── Queries ───────────────────────────────────────────────────────────────

    public List<Task> findByTeam(UUID teamId) {
        return state.getTasks().values().stream()
                .filter(t -> teamId.equals(t.getTeamId()))
                .sorted(Comparator.comparing(Task::getDueDate,
                        Comparator.nullsLast(Comparator.naturalOrder())))
                .collect(Collectors.toList());
    }

    public List<Task> findByAssignee(UUID userId) {
        return state.getTasks().values().stream()
                .filter(t -> userId.equals(t.getAssigneeId()))
                .collect(Collectors.toList());
    }

    public List<Task> findByTeamAndStatus(UUID teamId, TaskStatus status) {
        return findByTeam(teamId).stream()
                .filter(t -> status == t.getStatus())
                .collect(Collectors.toList());
    }

    public List<Task> findByTeamAndPriority(UUID teamId, Priority priority) {
        return findByTeam(teamId).stream()
                .filter(t -> priority == t.getPriority())
                .collect(Collectors.toList());
    }

    public List<Task> findUnassigned(UUID teamId) {
        return findByTeam(teamId).stream()
                .filter(t -> t.getAssigneeId() == null)
                .collect(Collectors.toList());
    }

    public List<Task> findOverdue(UUID teamId) {
        return findByTeam(teamId).stream()
                .filter(Task::isOverdue)
                .collect(Collectors.toList());
    }

    /** Status counts for a team's Kanban board: { PENDING=3, IN_PROGRESS=1, DONE=5 } */
    public Map<TaskStatus, Long> getStatusSummary(UUID teamId) {
        return findByTeam(teamId).stream()
                .collect(Collectors.groupingBy(Task::getStatus, Collectors.counting()));
    }

    // ── Status mutation ───────────────────────────────────────────────────────

    /** Convenience used by the RMI module — updates status and persists. */
    public void updateStatus(UUID taskId, TaskStatus newStatus) {
        findById(taskId).ifPresent(task -> {
            task.setStatus(newStatus); // touch() is called inside setStatus()
            save(task);
        });
    }

    public void assign(UUID taskId, UUID userId, String assigneeName) {
        findById(taskId).ifPresent(task -> {
            task.setAssigneeId(userId);
            task.setAssigneeName(assigneeName);
            save(task);
        });
    }
}