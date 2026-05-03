package org.example.workflow.rmi.server;

import org.example.workflow.rmi.service.TaskService;
import org.example.workflow.state.StateLoader;

import java.rmi.registry.LocateRegistry;
import java.rmi.registry.Registry;
import java.util.logging.Logger;

/**
 * Point d'entrée du serveur RMI WorkFlow.
 *
 * Démarrage :
 *   java -cp rmi-server-fat.jar org.example.workflow.rmi.server.RmiServerApp
 *
 * Ce serveur :
 *  1. Charge l'état depuis les fichiers JSON (StateLoader)
 *  2. Crée le registre RMI sur le port 1099
 *  3. Enregistre TaskService sous le nom "WorkFlow/TaskService"
 */
public class RmiServerApp {

    private static final Logger log = Logger.getLogger(RmiServerApp.class.getName());

    public static void main(String[] args) {
        try {
            // 1. Charger toutes les données JSON en mémoire (AppState)
            log.info("[RMI Server] Chargement de l'état depuis les fichiers JSON...");
            StateLoader.load();

            // 2. Instancier l'implémentation du service
            TaskServiceImpl taskService = new TaskServiceImpl();

            // 3. Créer le registre RMI (ou se connecter à un existant)
            Registry registry;
            try {
                registry = LocateRegistry.createRegistry(TaskService.RMI_PORT);
                log.info("[RMI Server] Registre RMI créé sur le port " + TaskService.RMI_PORT);
            } catch (Exception e) {
                // Le registre existe déjà (démarré par un autre module)
                registry = LocateRegistry.getRegistry(TaskService.RMI_PORT);
                log.info("[RMI Server] Registre RMI existant utilisé sur le port " + TaskService.RMI_PORT);
            }

            // 4. Enregistrer le service
            registry.rebind(TaskService.SERVICE_NAME, taskService);

            System.out.println();
            System.out.println("╔══════════════════════════════════════════════╗");
            System.out.println("║       WorkFlow — Serveur RMI démarré         ║");
            System.out.println("║  Port    : " + TaskService.RMI_PORT + "                             ║");
            System.out.println("║  Service : " + TaskService.SERVICE_NAME + "  ║");
            System.out.println("╚══════════════════════════════════════════════╝");
            System.out.println();

        } catch (Exception e) {
            log.severe("[RMI Server] Erreur fatale au démarrage : " + e.getMessage());
            e.printStackTrace();
            System.exit(1);
        }
    }
}
