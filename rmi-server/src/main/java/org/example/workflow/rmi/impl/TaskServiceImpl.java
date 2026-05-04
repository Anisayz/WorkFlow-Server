package org.example.workflow.rmi.impl;

import org.example.workflow.db.repository.*;
import org.example.workflow.model.Task;
import org.example.workflow.model.User;
import org.example.workflow.model.enums.*;
import org.example.workflow.rmi.interfaces.TaskService;
import org.example.workflow.util.JsonMapper;

import java.rmi.RemoteException;
import java.rmi.server.UnicastRemoteObject;
import java.time.LocalDate;
import java.util.List;
import java.util.UUID;
import java.util.logging.Logger;


// Receives String IDs  converts to UUID internally  returns JSON strings.

public class TaskServiceImpl extends UnicastRemoteObject implements TaskService {

    private static final Logger log = Logger.getLogger(TaskServiceImpl.class.getName());

    private final TaskRepository taskRepo = new TaskRepository();
    private final TeamRepository teamRepo = new TeamRepository();
    private final UserRepository userRepo = new UserRepository();

    public TaskServiceImpl() throws RemoteException { super(); }

    private UUID uid(String s) throws RemoteException {
        try { return UUID.fromString(s); }
        catch (Exception e) { throw new RemoteException("Invalid UUID: " + s); }
    }

    private User requireUser(String id) throws RemoteException {
        return userRepo.findById(uid(id))
                .orElseThrow(() -> new RemoteException("User not found: " + id));
    }

    private Task requireTask(String id) throws RemoteException {
        return taskRepo.findById(uid(id))
                .orElseThrow(() -> new RemoteException("Task not found: " + id));
    }

    private void requireLeader(String requesterId, UUID teamId) throws RemoteException {
        System.out.println("ur are not the member");
        if (!teamRepo.isLeader(teamId, uid(requesterId)))
            throw new RemoteException("Access denied: leader only.");
    }

    private void requireMember(String requesterId, UUID teamId) throws RemoteException {
        System.out.println("ur are not the member");
        if (!teamRepo.isMember(teamId, uid(requesterId)))
            throw new RemoteException("Access denied: not a team member.");
    }

    //Task operations

    @Override
    public String createTask(String requesterId, String title, String description,
                             String assigneeId, String teamId,
                             String priority, String dueDate) throws RemoteException {
        requireUser(requesterId);
        UUID tId = uid(teamId);
        requireLeader(requesterId, tId);

        UUID     aId          = assigneeId != null ? uid(assigneeId) : null;
        String   assigneeName = aId != null
                ? userRepo.findById(aId).map(User::getUsername).orElse(null) : null;
        Priority p            = priority != null ? Priority.valueOf(priority.toUpperCase()) : Priority.MEDIUM;
        LocalDate due         = dueDate != null ? LocalDate.parse(dueDate) : null;

        Task task = new Task(UUID.randomUUID(), title, description,
                aId, assigneeName, tId, p, due);
        taskRepo.save(task);
        log.info("[RMI] Task created: " + task);
        return JsonMapper.toJson(task);
    }

    @Override
    public String updateStatus(String requesterId, String taskId,
                               String newStatus) throws RemoteException {
        requireUser(requesterId);
        Task task = requireTask(taskId);
        requireMember(requesterId, task.getTeamId());

        User requester = requireUser(requesterId);
        if (requester.getRole() == Role.MEMBER
                && !uid(requesterId).equals(task.getAssigneeId()))
            throw new RemoteException("Access denied: can only update your own tasks.");

        taskRepo.updateStatus(uid(taskId), TaskStatus.valueOf(newStatus.toUpperCase()));
        return JsonMapper.toJson(requireTask(taskId));
    }

    @Override
    public String reassignTask(String requesterId, String taskId,
                               String newAssigneeId) throws RemoteException {
        requireUser(requesterId);
        Task task = requireTask(taskId);
        requireLeader(requesterId, task.getTeamId());

        String name = userRepo.findById(uid(newAssigneeId))
                .map(User::getUsername)
                .orElseThrow(() -> new RemoteException("Assignee not found."));
        taskRepo.assign(uid(taskId), uid(newAssigneeId), name);
        return JsonMapper.toJson(requireTask(taskId));
    }

    @Override
    public String updateDueDate(String requesterId, String taskId,
                                String newDueDate) throws RemoteException {
        requireUser(requesterId);
        Task task = requireTask(taskId);
        requireLeader(requesterId, task.getTeamId());
        task.setDueDate(LocalDate.parse(newDueDate));
        task.touch();
        taskRepo.save(task);
        return JsonMapper.toJson(task);
    }

    @Override
    public String updatePriority(String requesterId, String taskId,
                                 String newPriority) throws RemoteException {
        requireUser(requesterId);
        Task task = requireTask(taskId);
        requireLeader(requesterId, task.getTeamId());
        task.setPriority(Priority.valueOf(newPriority.toUpperCase()));
        task.touch();
        taskRepo.save(task);
        return JsonMapper.toJson(task);
    }

    @Override
    public boolean deleteTask(String requesterId, String taskId) throws RemoteException {
        requireUser(requesterId);
        Task task = requireTask(taskId);
        requireLeader(requesterId, task.getTeamId());
        taskRepo.delete(uid(taskId));
        return true;
    }

    @Override
    public String getTasksByTeam(String requesterId, String teamId) throws RemoteException {
        requireUser(requesterId);
        requireMember(requesterId, uid(teamId));
        List<Task> tasks = taskRepo.findByTeam(uid(teamId));
        return JsonMapper.toJson(tasks);
    }

    @Override
    public String getMyTasks(String requesterId) throws RemoteException {
        requireUser(requesterId);
        return JsonMapper.toJson(taskRepo.findByAssignee(uid(requesterId)));
    }

    @Override
    public String getTaskById(String requesterId, String taskId) throws RemoteException {
        requireUser(requesterId);
        Task task = requireTask(taskId);
        requireMember(requesterId, task.getTeamId());
        return JsonMapper.toJson(task);
    }

    @Override
    public String ping() throws RemoteException { return "pong — TaskService alive"; }
}