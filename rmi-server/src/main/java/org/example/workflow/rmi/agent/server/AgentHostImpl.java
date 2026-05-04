package org.example.workflow.rmi.agent.server;

import org.example.workflow.rmi.agent.Agent;
import org.example.workflow.rmi.agent.AgentHost;
import org.example.workflow.rmi.agent.ServerContext;

import java.io.Serializable;
import java.rmi.RemoteException;
import java.rmi.server.UnicastRemoteObject;
import java.util.logging.Logger;

/**
 * AgentHostImpl — runs incoming mobile agents on the server.
 *
 * Each call creates a fresh ServerContext and executes the agent.
 * Whatever the agent returns is sent back to the client over RMI.
 */
public class AgentHostImpl extends UnicastRemoteObject implements AgentHost {

    private static final Logger log = Logger.getLogger(AgentHostImpl.class.getName());

    public AgentHostImpl() throws RemoteException { super(); }

    @Override
    public Serializable dispatch(Agent<? extends Serializable> agent) throws RemoteException {
        if (agent == null) throw new RemoteException("Agent cannot be null");
        try {
            log.info("[AgentHost] Executing agent: " + agent.getClass().getSimpleName());
            ServerContext ctx = new ServerContextImpl();
            return agent.execute(ctx);
        } catch (Exception e) {
            log.severe("[AgentHost] Agent execution failed: " + e.getMessage());
            throw new RemoteException("Agent execution failed: " + e.getMessage(), e);
        }
    }
}