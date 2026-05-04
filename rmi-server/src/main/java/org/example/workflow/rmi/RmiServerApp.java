package org.example.workflow.rmi;

import org.example.workflow.rmi.agent.AgentHost;
import org.example.workflow.rmi.agent.server.AgentHostImpl;
import org.example.workflow.rmi.impl.*;
import org.example.workflow.rmi.interfaces.*;
import org.example.workflow.state.StateLoader;

import java.rmi.registry.LocateRegistry;
import java.rmi.registry.Registry;
import java.util.logging.Logger;

public class RmiServerApp {

    private static final Logger log = Logger.getLogger(RmiServerApp.class.getName());
    private static final int    PORT = 1099;

    public static void main(String[] args) {
        System.setProperty("java.rmi.server.hostname", "127.0.0.1");
        try {
            log.info("[RmiServer] Loading state...");
            StateLoader.load();

            // Instantiate services
            TaskServiceImpl taskService = new TaskServiceImpl();
            TeamServiceImpl teamService = new TeamServiceImpl();
            AgentHostImpl agentHost   = new AgentHostImpl();
            // Get or create registry
            Registry registry;
            try {
                registry = LocateRegistry.createRegistry(PORT);
                log.info("[RmiServer] Created RMI registry on port " + PORT);
            } catch (Exception e) {
                registry = LocateRegistry.getRegistry(PORT);
                log.info("[RmiServer] Using existing RMI registry on port " + PORT);
            }

            // Bind both services
            registry.rebind(TaskService.SERVICE_NAME, taskService);
            registry.rebind(TeamService.SERVICE_NAME, teamService);
            registry.rebind(AgentHost.SERVICE_NAME,   agentHost);
            System.out.println("RMI server started on port " + PORT + "");
            Thread.currentThread().join();


        } catch (Exception e) {
            log.severe("[RmiServer] Fatal error: " + e.getMessage());
            e.printStackTrace();
            System.exit(1);
        }
    }
}