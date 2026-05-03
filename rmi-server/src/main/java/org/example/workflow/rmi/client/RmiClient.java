package org.example.workflow.rmi.client;

import org.example.workflow.rmi.service.TaskService;

import java.rmi.NotBoundException;
import java.rmi.RemoteException;
import java.rmi.registry.LocateRegistry;
import java.rmi.registry.Registry;
import java.util.logging.Logger;

/**
 * Classe de connexion RMI côté client.
 *
 * Usage (depuis un contrôleur JavaFX ou une classe de test) :
 *
 *   RmiClient client = new RmiClient();
 *   client.connect();                          // localhost:1099
 *   TaskService svc = client.getTaskService();
 *   svc.ping();
 */
public class RmiClient {

    private static final Logger log = Logger.getLogger(RmiClient.class.getName());

    private TaskService taskService;

    /** Se connecte au serveur RMI en localhost sur le port par défaut. */
    public void connect() throws Exception {
        connect("localhost", TaskService.RMI_PORT);
    }

    /** Se connecte à un serveur RMI distant. */
    public void connect(String host, int port) throws Exception {
        try {
            Registry registry = LocateRegistry.getRegistry(host, port);
            taskService = (TaskService) registry.lookup(TaskService.SERVICE_NAME);
            log.info("[RMI Client] Connecté au serveur RMI → " + host + ":" + port);
        } catch (RemoteException e) {
            throw new Exception("Impossible de joindre le serveur RMI (" + host + ":" + port + ") : " + e.getMessage(), e);
        } catch (NotBoundException e) {
            throw new Exception("Service '" + TaskService.SERVICE_NAME + "' non enregistré dans le registre.", e);
        }
    }

    /**
     * Retourne le stub RMI.
     * À utiliser dans les contrôleurs JavaFX : taskCtrl = new TaskController(client.getTaskService(), userId);
     */
    public TaskService getTaskService() {
        if (taskService == null) {
            throw new IllegalStateException("Non connecté. Appelez connect() d'abord.");
        }
        return taskService;
    }

    public boolean isConnected() {
        return taskService != null;
    }

    /** Vérifie la connexion avec un ping. */
    public boolean ping() {
        try {
            String response = taskService.ping();
            log.info("[RMI Client] Ping → " + response);
            return true;
        } catch (Exception e) {
            return false;
        }
    }
}
