package org.example.workflow.rmi.client.test;

import org.example.workflow.model.Task;
import org.example.workflow.model.User;
import org.example.workflow.model.enums.Priority;
import org.example.workflow.model.enums.Role;
import org.example.workflow.model.enums.TaskStatus;
import org.example.workflow.rmi.client.RmiClient;
import org.example.workflow.rmi.client.TaskController;
import org.example.workflow.rmi.service.TaskService;

import java.time.LocalDate;
import java.util.List;
import java.util.UUID;

/**
 * ══════════════════════════════════════════════════════════════════
 *  CLIENT DE TEST RMI — WorkFlow
 * ══════════════════════════════════════════════════════════════════
 *
 *  Ce fichier est un client de test standalone.
 *  Il ne nécessite PAS le module TCP ni JavaFX.
 *
 *  AVANT de lancer ce test :
 *  ─────────────────────────
 *  1. Assurez-vous que le dossier "data/" existe dans le répertoire
 *     de travail (là où vous lancez le serveur).
 *  2. Lancez d'abord RmiServerApp (le serveur RMI).
 *  3. Lancez ensuite ce fichier.
 *
 *  Le test va :
 *  ────────────
 *  → Pinger le serveur
 *  → Créer une tâche (en tant que leader)
 *  → Lister les tâches de l'équipe
 *  → Démarrer la tâche (EN_COURS)
 *  → Compléter la tâche (DONE)
 *  → Ajouter un membre par username
 *  → Lister les membres de l'équipe
 *  → Supprimer la tâche
 *
 *  IMPORTANT : les UUIDs du leader, membre et équipe doivent exister
 *  dans data/users.json et data/teams.json.
 *  Utilisez SeedData.main() pour créer des données de test.
 */
public class RmiTestClient {

    // ─── Remplacer ces UUIDs par ceux qui existent dans vos fichiers JSON ───
    // Après avoir lancé SeedData.main(), les UUIDs sont affichés dans la console.
    private static UUID LEADER_ID = UUID.fromString("00000000-0000-0000-0000-000000000001");
    private static UUID MEMBER_ID = UUID.fromString("00000000-0000-0000-0000-000000000002");
    private static UUID TEAM_ID   = UUID.fromString("00000000-0000-0000-0000-000000000010");

    public static void main(String[] args) throws Exception {

        // ── 0. Si les UUIDs par défaut sont utilisés, générer les données de test ──
        System.out.println("╔══════════════════════════════════════════════╗");
        System.out.println("║      WorkFlow — Test Client RMI              ║");
        System.out.println("╚══════════════════════════════════════════════╝");
        System.out.println();

        // ── 1. Connexion au serveur RMI ───────────────────────────────────────────
        System.out.println("[ ] Connexion au serveur RMI...");
        RmiClient rmiClient = new RmiClient();
        rmiClient.connect(); // localhost:1099
        System.out.println("[✓] Connecté !\n");

        // ── 2. Ping ───────────────────────────────────────────────────────────────
        TaskService svc = rmiClient.getTaskService();
        System.out.println("[ ] Ping...");
        System.out.println("[✓] " + svc.ping() + "\n");

        // ── 3. TaskController (simulant l'utilisateur leader connecté) ────────────
        TaskController leaderCtrl = new TaskController(svc, LEADER_ID);
        TaskController memberCtrl = new TaskController(svc, MEMBER_ID);

        // ── 4. Créer une tâche ────────────────────────────────────────────────────
        System.out.println("[ ] Création d'une tâche...");
        Task created = leaderCtrl.createTask(
                "Implémenter le module RMI",
                "Créer l'interface TaskService et son implémentation",
                MEMBER_ID,
                TEAM_ID,
                Priority.HIGH,
                LocalDate.now().plusDays(7)
        );
        printTask("Tâche créée", created);

        if (created == null) {
            System.err.println("[✗] Échec de la création. Vérifiez que les UUIDs existent dans les JSON.");
            System.err.println("    → Lancez SeedData.main() pour créer des données de test.");
            return;
        }

        UUID taskId = created.getId();

        // ── 5. Lister les tâches de l'équipe ──────────────────────────────────────
        System.out.println("\n[ ] Tâches de l'équipe :");
        List<Task> teamTasks = leaderCtrl.getTeamTasks(TEAM_ID);
        teamTasks.forEach(t -> System.out.println("    → " + t));

        // ── 6. Démarrer la tâche (membre) ─────────────────────────────────────────
        System.out.println("\n[ ] Démarrage de la tâche (membre)...");
        Task started = memberCtrl.startTask(taskId);
        printTask("Tâche démarrée", started);

        // ── 7. Compléter la tâche (membre) ────────────────────────────────────────
        System.out.println("\n[ ] Complétion de la tâche (membre)...");
        Task completed = memberCtrl.completeTask(taskId);
        printTask("Tâche complétée", completed);

        // ── 8. Tenter de modifier le statut d'une tâche non assignée (doit échouer) ──
        System.out.println("\n[ ] Test permission : leader essaie de changer statut...");
        Task leaderUpdate = leaderCtrl.updateStatus(taskId, TaskStatus.PENDING);
        printTask("Reset par le leader", leaderUpdate);

        // ── 9. Lister mes tâches (membre) ─────────────────────────────────────────
        System.out.println("\n[ ] Mes tâches (membre) :");
        List<Task> myTasks = memberCtrl.getMyTasks();
        myTasks.forEach(t -> System.out.println("    → " + t));

        // ── 10. Ajouter un membre par username ────────────────────────────────────
        System.out.println("\n[ ] Ajout membre par username (leader)...");
        // On essaie d'ajouter l'utilisateur MEMBER_ID à nouveau (doit échouer proprement)
        User addedMember = leaderCtrl.addMember(TEAM_ID, "member");
        if (addedMember != null) {
            System.out.println("[✓] Membre ajouté : " + addedMember);
        } else {
            System.out.println("[~] Membre déjà présent ou introuvable (comportement attendu).");
        }

        // ── 11. Lister les membres de l'équipe ────────────────────────────────────
        System.out.println("\n[ ] Membres de l'équipe :");
        List<User> members = leaderCtrl.getTeamMembers(TEAM_ID);
        members.forEach(m -> System.out.println("    → " + m));

        // ── 12. Supprimer la tâche (leader) ───────────────────────────────────────
        System.out.println("\n[ ] Suppression de la tâche (leader)...");
        boolean deleted = leaderCtrl.deleteTask(taskId);
        System.out.println(deleted ? "[✓] Tâche supprimée." : "[✗] Échec suppression.");

        // ── 13. Vérifier que la tâche n'existe plus ───────────────────────────────
        System.out.println("\n[ ] Vérification suppression...");
        Task ghost = leaderCtrl.getTask(taskId);
        System.out.println(ghost == null ? "[✓] Tâche bien supprimée (null retourné)." : "[✗] La tâche existe encore : " + ghost);

        System.out.println("\n══════════════════════════════════════════════");
        System.out.println("  Tests terminés !");
        System.out.println("══════════════════════════════════════════════");
    }

    private static void printTask(String label, Task task) {
        if (task == null) {
            System.out.println("[✗] " + label + " : null (erreur côté serveur)");
        } else {
            System.out.println("[✓] " + label + " :");
            System.out.println("    id       = " + task.getId());
            System.out.println("    title    = " + task.getTitle());
            System.out.println("    status   = " + task.getStatus());
            System.out.println("    priority = " + task.getPriority());
            System.out.println("    assignee = " + task.getAssigneeName());
            System.out.println("    dueDate  = " + task.getFormattedDueDate());
        }
    }
}
