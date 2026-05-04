package org.example.workflow.rmi.agent;

import java.io.Serializable;
import java.rmi.Remote;
import java.rmi.RemoteException;

/**
 * AgentHost — the RMI service that accepts mobile agents.
 *
 * Client sends an Agent → server deserializes it → server creates ServerContext
 * → server calls agent.execute(context) → result returns to client.
 */
public interface AgentHost extends Remote {

    String SERVICE_NAME = "WorkFlow/AgentHost";
    int    RMI_PORT     = 1099;

    /**
     * Dispatches an agent to the server for execution.
     * Returns the result of agent.execute(serverContext).
     */
    Serializable dispatch(Agent<? extends Serializable> agent) throws RemoteException;
}