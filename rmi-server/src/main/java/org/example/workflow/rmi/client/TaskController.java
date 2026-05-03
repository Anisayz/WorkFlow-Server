package org.example.workflow.rmi.client;

import org.example.workflow.model.Task;
import org.example.workflow.model.User;
import org.example.workflow.model.enums.Priority;
import org.example.workflow.model.enums.TaskStatus;
import org.example.workflow.rmi.service.TaskService;

import java.rmi.RemoteException;
import java.time.LocalDate;
import java.util.List;
import java.util.UUID;
import java.util.logging.Logger;

/**
 * Contrôleur côté client — couche entre l'UI JavaFX et le stub RMI.
 *
 * Instanciation (après login TCP) :
 *   TaskController ctrl = new TaskController(rmiClient.getTaskService(), loggedInUser.getId());
 *
 * Toutes les méthodes :
 *  - passent automatiquement le requesterId (l'utilisateur connecté)
 *  - retournent null / false / liste vide en cas d'erreur (log en console)
 *  - le TODO indique où afficher l'erreur dans JavaFX
 */
public class TaskController {

    private static final Logger log = Logger.getLogger(TaskController.class.getName());

    private final TaskService service;
    private final UUID currentUserId;

    public TaskController(TaskService service, UUID currentUserId) {
        this.service = service;
        this.currentUserId = currentUserId;
    }

    // ══════════════════════════════════════════════════════════════════
    //  TÂCHES — Actions LEADER
    // ══════════════════════════════════════════════════════════════════

    /** Créer une tâche (leader uniquement). */
    public Task createTask(String title, String description,
                           UUID assigneeId, UUID teamId,
                           Priority priority, LocalDate dueDate) {
        try {
            return service.createTask(currentUserId, title, description,
                    assigneeId, teamId, priority, dueDate);
        } catch (RemoteException e) {
            return handleError("createTask", e);
        }
    }

    /** Ré-assigner une tâche (leader uniquement). */
    public Task reassignTask(UUID taskId, UUID newAssigneeId) {
        try {
            return service.reassignTask(currentUserId, taskId, newAssigneeId);
        } catch (RemoteException e) {
            return handleError("reassignTask", e);
        }
    }

    /** Modifier la date limite (leader uniquement). */
    public Task updateDueDate(UUID taskId, LocalDate newDueDate) {
        try {
            return service.updateDueDate(currentUserId, taskId, newDueDate);
        } catch (RemoteException e) {
            return handleError("updateDueDate", e);
        }
    }

    /** Modifier la priorité (leader uniquement). */
    public Task updatePriority(UUID taskId, Priority newPriority) {
        try {
            return service.updatePriority(currentUserId, taskId, newPriority);
        } catch (RemoteException e) {
            return handleError("updatePriority", e);
        }
    }

    /** Supprimer une tâche (leader uniquement). */
    public boolean deleteTask(UUID taskId) {
        try {
            return service.deleteTask(currentUserId, taskId);
        } catch (RemoteException e) {
            log.warning("[TaskController] deleteTask : " + e.getMessage());
            // TODO: Platform.runLater(() -> showAlert("Erreur", e.getMessage()));
            return false;
        }
    }

    // ══════════════════════════════════════════════════════════════════
    //  TÂCHES — Actions MEMBRE (et leader)
    // ══════════════════════════════════════════════════════════════════

    /** Démarrer une tâche → EN_COURS. */
    public Task startTask(UUID taskId) {
        return updateStatus(taskId, TaskStatus.IN_PROGRESS);
    }

    /** Marquer une tâche comme terminée. */
    public Task completeTask(UUID taskId) {
        return updateStatus(taskId, TaskStatus.DONE);
    }

    /** Remettre une tâche en attente. */
    public Task resetTask(UUID taskId) {
        return updateStatus(taskId, TaskStatus.PENDING);
    }

    /** Mettre à jour le statut (méthode générique). */
    public Task updateStatus(UUID taskId, TaskStatus newStatus) {
        try {
            return service.updateStatus(currentUserId, taskId, newStatus);
        } catch (RemoteException e) {
            return handleError("updateStatus", e);
        }
    }

    // ══════════════════════════════════════════════════════════════════
    //  TÂCHES — Consultation
    // ══════════════════════════════════════════════════════════════════

    /** Toutes les tâches de l'équipe (tableau de bord). */
    public List<Task> getTeamTasks(UUID teamId) {
        try {
            return service.getTasksByTeam(currentUserId, teamId);
        } catch (RemoteException e) {
            log.warning("[TaskController] getTeamTasks : " + e.getMessage());
            return List.of();
        }
    }

    /** Mes tâches assignées. */
    public List<Task> getMyTasks() {
        try {
            return service.getMyTasks(currentUserId);
        } catch (RemoteException e) {
            log.warning("[TaskController] getMyTasks : " + e.getMessage());
            return List.of();
        }
    }

    /** Récupérer une tâche par ID. */
    public Task getTask(UUID taskId) {
        try {
            return service.getTaskById(currentUserId, taskId);
        } catch (RemoteException e) {
            return handleError("getTask", e);
        }
    }

    // ══════════════════════════════════════════════════════════════════
    //  MEMBRES D'ÉQUIPE
    // ══════════════════════════════════════════════════════════════════

    /** Ajouter un membre par username (leader uniquement). */
    public User addMember(UUID teamId, String username) {
        try {
            return service.addMemberByUsername(currentUserId, teamId, username);
        } catch (RemoteException e) {
            log.warning("[TaskController] addMember : " + e.getMessage());
            // TODO: showAlert("Erreur", e.getMessage());
            return null;
        }
    }

    /** Retirer un membre (leader uniquement). */
    public boolean removeMember(UUID teamId, UUID userId) {
        try {
            return service.removeMember(currentUserId, teamId, userId);
        } catch (RemoteException e) {
            log.warning("[TaskController] removeMember : " + e.getMessage());
            return false;
        }
    }

    /** Lister tous les membres de l'équipe. */
    public List<User> getTeamMembers(UUID teamId) {
        try {
            return service.getTeamMembers(currentUserId, teamId);
        } catch (RemoteException e) {
            log.warning("[TaskController] getTeamMembers : " + e.getMessage());
            return List.of();
        }
    }

    // ══════════════════════════════════════════════════════════════════
    //  Gestion des erreurs
    // ══════════════════════════════════════════════════════════════════

    private Task handleError(String method, RemoteException e) {
        log.warning("[TaskController] " + method + " : " + e.getMessage());
        // TODO (JavaFX) :
        //   Platform.runLater(() ->
        //       new Alert(Alert.AlertType.ERROR, e.getMessage(), ButtonType.OK).showAndWait()
        //   );
        return null;
    }

    public UUID getCurrentUserId() { return currentUserId; }
}
