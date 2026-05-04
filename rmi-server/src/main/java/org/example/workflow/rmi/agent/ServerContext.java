package org.example.workflow.rmi.agent;

import java.util.List;

/**
 * ServerContext — exposes server-side data to mobile agents.
 *
 * Implemented on the server as ServerContextImpl, which reads from AppState.
 * Agents call these methods inside execute() to access data without making
 * separate RMI round-trips.
 *
 * Returns lightweight DTOs (AgentUser/AgentTeam/AgentTask) so the agent
 * pattern is independent of the server's UUID-based model classes.
 */
public interface ServerContext {

    AgentUser getUser(String userId);

    AgentTeam getTeam(String teamId);

    List<AgentUser> getTeamMembers(String teamId);

    List<AgentTask> getUserTasks(String userId);

    List<AgentTask> getTeamTasks(String teamId);
}