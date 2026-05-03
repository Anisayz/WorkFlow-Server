package org.example.workflow.rmi.server;

import org.example.workflow.db.repository.TaskRepository;
import org.example.workflow.db.repository.TeamRepository;
import org.example.workflow.db.repository.UserRepository;
import org.example.workflow.model.Task;
import org.example.workflow.model.Team;
import org.example.workflow.model.User;
import org.example.workflow.model.enums.Priority;
import org.example.workflow.model.enums.Role;
import org.example.workflow.model.enums.TaskStatus;
import org.example.workflow.rmi.service.TaskService;

import java.rmi.RemoteException;
import java.rmi.server.UnicastRemoteObject;
import java.time.LocalDate;
import java.util.List;
import java.util.UUID;
import java.util.logging.Logger;

/**
 * Implémentation serveur de TaskService.
 *
 * Chaque méthode :
 *  1. Valide le requesterId (l'utilisateur existe)
 *  2. Vérifie les permissions (LEADER vs MEMBRE)
 *  3. Exécute l'opération via les repositories
 *  4. Retourne le résultat sérialisé au client RMI
 */
public class TaskServiceImpl extends UnicastRemoteObject implements TaskService {

    private static final Logger log = Logger.getLogger(TaskServiceImpl.class.getName());

    private final TaskRepository taskRepo;
    private final TeamRepository teamRepo;
    private final UserRepository userRepo;

    public TaskServiceImpl() throws RemoteException {
        super();
        this.taskRepo = new TaskRepository();
        this.teamRepo = new TeamRepository();
        this.userRepo = new UserRepository();
    }

    // ══════════════════════════════════════════════════════════════════
    //  Helpers de validation
    // ══════════════════════════════════════════════════════════════════

    /** Récupère l'utilisateur ou lance une RemoteException. */
    private User requireUser(UUID userId) throws RemoteException {
        return userRepo.findById(userId)
                .orElseThrow(() -> new RemoteException("Utilisateur introuvable : " + userId));
    }

    /** Récupère la tâche ou lance une RemoteException. */
    private Task requireTask(UUID taskId) throws RemoteException {
        return taskRepo.findById(taskId)
                .orElseThrow(() -> new RemoteException("Tâche introuvable : " + taskId));
    }

    /** Récupère l'équipe ou lance une RemoteException. */
    private Team requireTeam(UUID teamId) throws RemoteException {
        return teamRepo.findById(teamId)
                .orElseThrow(() -> new RemoteException("Équipe introuvable : " + teamId));
    }

    /** Lance une RemoteException si le requester n'est pas LEADER de l'équipe. */
    private void requireLeader(UUID requesterId, UUID teamId) throws RemoteException {
        if (!teamRepo.isLeader(teamId, requesterId)) {
            throw new RemoteException("Accès refusé : seul le leader peut effectuer cette action.");
        }
    }

    /** Lance une RemoteException si le requester n'est pas membre de l'équipe. */
    private void requireMember(UUID requesterId, UUID teamId) throws RemoteException {
        if (!teamRepo.isMember(teamId, requesterId)) {
            throw new RemoteException("Accès refusé : vous n'êtes pas membre de cette équipe.");
        }
    }

    // ══════════════════════════════════════════════════════════════════
    //  TÂCHES
    // ══════════════════════════════════════════════════════════════════

    @Override
    public Task createTask(UUID requesterId, String title, String description,
                           UUID assigneeId, UUID teamId, Priority priority,
                           LocalDate dueDate) throws RemoteException {

        requireUser(requesterId);
        requireLeader(requesterId, teamId);

        // Vérifier que le membre assigné est bien dans l'équipe
        if (assigneeId != null && !teamRepo.isMember(teamId, assigneeId)) {
            throw new RemoteException("L'utilisateur assigné n'est pas membre de cette équipe.");
        }

        // Récupérer le nom de l'assigné pour le cacher dans la tâche
        String assigneeName = null;
        if (assigneeId != null) {
            assigneeName = userRepo.findById(assigneeId)
                    .map(User::getUsername)
                    .orElse(null);
        }

        Task task = new Task(
                UUID.randomUUID(),
                title,
                description,
                assigneeId,
                assigneeName,
                teamId,
                priority,
                dueDate
        );

        taskRepo.save(task);
        log.info("[RMI] Tâche créée : " + task);
        return task;
    }

    @Override
    public Task updateStatus(UUID requesterId, UUID taskId, TaskStatus newStatus) throws RemoteException {
        requireUser(requesterId);
        Task task = requireTask(taskId);
        requireMember(requesterId, task.getTeamId());

        User requester = requireUser(requesterId);

        // Un MEMBRE ne peut modifier que ses propres tâches
        if (requester.getRole() == Role.MEMBER) {
            if (!requesterId.equals(task.getAssigneeId())) {
                throw new RemoteException("Accès refusé : vous ne pouvez modifier que vos propres tâches.");
            }
        }

        taskRepo.updateStatus(taskId, newStatus);
        Task updated = requireTask(taskId);
        log.info("[RMI] Statut mis à jour → " + newStatus + " pour tâche " + taskId);
        return updated;
    }

    @Override
    public Task reassignTask(UUID requesterId, UUID taskId, UUID newAssigneeId) throws RemoteException {
        requireUser(requesterId);
        Task task = requireTask(taskId);
        requireLeader(requesterId, task.getTeamId());

        if (!teamRepo.isMember(task.getTeamId(), newAssigneeId)) {
            throw new RemoteException("Le nouvel assigné n'est pas membre de cette équipe.");
        }

        String newAssigneeName = userRepo.findById(newAssigneeId)
                .map(User::getUsername)
                .orElseThrow(() -> new RemoteException("Utilisateur introuvable : " + newAssigneeId));

        taskRepo.assign(taskId, newAssigneeId, newAssigneeName);
        Task updated = requireTask(taskId);
        log.info("[RMI] Tâche " + taskId + " réassignée à " + newAssigneeName);
        return updated;
    }

    @Override
    public Task updateDueDate(UUID requesterId, UUID taskId, LocalDate newDueDate) throws RemoteException {
        requireUser(requesterId);
        Task task = requireTask(taskId);
        requireLeader(requesterId, task.getTeamId());

        task.setDueDate(newDueDate);
        task.touch();
        taskRepo.save(task);
        log.info("[RMI] DueDate mise à jour pour tâche " + taskId);
        return task;
    }

    @Override
    public Task updatePriority(UUID requesterId, UUID taskId, Priority newPriority) throws RemoteException {
        requireUser(requesterId);
        Task task = requireTask(taskId);
        requireLeader(requesterId, task.getTeamId());

        task.setPriority(newPriority);
        task.touch();
        taskRepo.save(task);
        log.info("[RMI] Priorité mise à jour pour tâche " + taskId);
        return task;
    }

    @Override
    public boolean deleteTask(UUID requesterId, UUID taskId) throws RemoteException {
        requireUser(requesterId);
        Task task = requireTask(taskId);
        requireLeader(requesterId, task.getTeamId());

        taskRepo.delete(taskId);
        log.info("[RMI] Tâche supprimée : " + taskId);
        return true;
    }

    @Override
    public List<Task> getTasksByTeam(UUID requesterId, UUID teamId) throws RemoteException {
        requireUser(requesterId);
        requireMember(requesterId, teamId);
        return taskRepo.findByTeam(teamId);
    }

    @Override
    public List<Task> getMyTasks(UUID requesterId) throws RemoteException {
        requireUser(requesterId);
        return taskRepo.findByAssignee(requesterId);
    }

    @Override
    public Task getTaskById(UUID requesterId, UUID taskId) throws RemoteException {
        requireUser(requesterId);
        Task task = requireTask(taskId);
        requireMember(requesterId, task.getTeamId());
        return task;
    }

    // ══════════════════════════════════════════════════════════════════
    //  MEMBRES D'ÉQUIPE
    // ══════════════════════════════════════════════════════════════════

    @Override
    public User addMemberByUsername(UUID requesterId, UUID teamId, String username) throws RemoteException {
        requireUser(requesterId);
        requireLeader(requesterId, teamId);
        requireTeam(teamId);

        User target = userRepo.findByUsername(username)
                .orElseThrow(() -> new RemoteException("Utilisateur introuvable : " + username));

        if (teamRepo.isMember(teamId, target.getId())) {
            throw new RemoteException("L'utilisateur est déjà membre de cette équipe.");
        }

        teamRepo.addMember(teamId, target.getId());
        target.setTeamId(teamId);
        userRepo.save(target);

        log.info("[RMI] Membre ajouté : " + username + " → équipe " + teamId);
        return target.toClientSafe();
    }

    @Override
    public boolean removeMember(UUID requesterId, UUID teamId, UUID userId) throws RemoteException {
        requireUser(requesterId);
        requireLeader(requesterId, teamId);

        if (userId.equals(requesterId)) {
            throw new RemoteException("Le leader ne peut pas se retirer lui-même de l'équipe.");
        }

        teamRepo.removeMember(teamId, userId);

        // Mettre à jour le teamId de l'utilisateur
        userRepo.findById(userId).ifPresent(u -> {
            u.setTeamId(null);
            userRepo.save(u);
        });

        log.info("[RMI] Membre retiré : " + userId + " de l'équipe " + teamId);
        return true;
    }

    @Override
    public List<User> getTeamMembers(UUID requesterId, UUID teamId) throws RemoteException {
        requireUser(requesterId);
        requireMember(requesterId, teamId);
        return teamRepo.getMembers(teamId).stream()
                .map(User::toClientSafe)
                .toList();
    }

    // ══════════════════════════════════════════════════════════════════
    //  UTILITAIRE
    // ══════════════════════════════════════════════════════════════════

    @Override
    public String ping() throws RemoteException {
        return "pong — WorkFlow RMI Server is alive";
    }
}
