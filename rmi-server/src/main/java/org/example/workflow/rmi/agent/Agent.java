package org.example.workflow.rmi.agent;

import java.io.Serializable;

/**
 * Agent — the mobile agent contract.
 *
 * An Agent is a Serializable object that travels (via RMI) to the server,
 * executes there using ServerContext, and returns a Serializable result.
 *
 * The agent class must exist on BOTH client and server classpath
 * with the IDENTICAL fully-qualified name.
 */
public interface Agent<T extends Serializable> extends Serializable {
    T execute(ServerContext context);
}