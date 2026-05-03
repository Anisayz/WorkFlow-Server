package org.example.workflow.rmi.service;

import org.example.workflow.model.Task;
import org.example.workflow.model.User;
import org.example.workflow.model.enums.Priority;
import org.example.workflow.model.enums.TaskStatus;

import java.rmi.Remote;
import java.rmi.RemoteException;
import java.time.LocalDate;
import java.util.List;
import java.util.UUID;

/**
 * Interface RMI partagée entre le serveur et le client.
 * Toute méthode Remote doit déclarer RemoteException.
 *
 * Conventions :
 *  - requesterId  = UUID de l'utilisateur qui fait l'appel (vérifié côté serveur)
 *  - sessionToken = UUID du token de session (créé par le TCP auth module)
 */
public interface TaskService extends Remote {

    String SERVICE_NAME = "WorkFlow/TaskService";
    int    RMI_PORT     = 1099;

    // ══════════════════════════════════════════════════════════════════
    //  TÂCHES
    // ══════════════════════════════════════════════════════════════════

    /**
     * Créer une nouvelle tâche.
     * Réservé au LEADER de l'équipe.
     */
    Task createTask(UUID requesterId,
                    String title,
                    String description,
                    UUID assigneeId,
                    UUID teamId,
                    Priority priority,
                    LocalDate dueDate) throws RemoteException;

    /**
     * Mettre à jour le statut d'une tâche.
     * Le LEADER peut modifier n'importe quelle tâche de son équipe.
     * Le MEMBRE peut seulement modifier ses propres tâches (IN_PROGRESS / DONE).
     */
    Task updateStatus(UUID requesterId, UUID taskId, TaskStatus newStatus) throws RemoteException;

    /**
     * Ré-assigner une tâche à un autre membre.
     * Réservé au LEADER.
     */
    Task reassignTask(UUID requesterId, UUID taskId, UUID newAssigneeId) throws RemoteException;

    /**
     * Modifier la date limite d'une tâche.
     * Réservé au LEADER.
     */
    Task updateDueDate(UUID requesterId, UUID taskId, LocalDate newDueDate) throws RemoteException;

    /**
     * Modifier la priorité d'une tâche.
     * Réservé au LEADER.
     */
    Task updatePriority(UUID requesterId, UUID taskId, Priority newPriority) throws RemoteException;

    /**
     * Supprimer une tâche.
     * Réservé au LEADER.
     */
    boolean deleteTask(UUID requesterId, UUID taskId) throws RemoteException;

    /** Récupérer toutes les tâches d'une équipe (triées par dueDate). */
    List<Task> getTasksByTeam(UUID requesterId, UUID teamId) throws RemoteException;

    /** Récupérer les tâches assignées à l'utilisateur connecté. */
    List<Task> getMyTasks(UUID requesterId) throws RemoteException;

    /** Récupérer une tâche par son ID. */
    Task getTaskById(UUID requesterId, UUID taskId) throws RemoteException;

    // ══════════════════════════════════════════════════════════════════
    //  MEMBRES D'ÉQUIPE
    // ══════════════════════════════════════════════════════════════════

    /**
     * Ajouter un membre à l'équipe via son username.
     * Réservé au LEADER.
     */
    User addMemberByUsername(UUID requesterId, UUID teamId, String username) throws RemoteException;

    /**
     * Retirer un membre de l'équipe.
     * Réservé au LEADER.
     */
    boolean removeMember(UUID requesterId, UUID teamId, UUID userId) throws RemoteException;

    /** Récupérer tous les membres d'une équipe. */
    List<User> getTeamMembers(UUID requesterId, UUID teamId) throws RemoteException;

    // ══════════════════════════════════════════════════════════════════
    //  UTILITAIRE
    // ══════════════════════════════════════════════════════════════════

    /** Vérifie que le serveur RMI est vivant. */
    String ping() throws RemoteException;
}
