package org.example.workflow.rmi.interfaces;

import java.rmi.Remote;
import java.rmi.RemoteException;

//String IDs, JSON string returns, mirrors the client interface exactly.

public interface TeamService extends Remote {

    String SERVICE_NAME = "WorkFlow/TeamService";
    int    RMI_PORT     = 1099;

    String createTeam(String requesterId, String name,
                      String description) throws RemoteException;

    String joinTeam(String requesterId, String inviteCode) throws RemoteException;

    String addMemberByUsername(String requesterId, String teamId,
                               String username) throws RemoteException;

    boolean removeMember(String requesterId, String teamId,
                         String userId) throws RemoteException;

    String getTeamMembers(String requesterId, String teamId) throws RemoteException;

    String getMyTeam(String requesterId) throws RemoteException;

    boolean disbandTeam(String requesterId, String teamId) throws RemoteException;

    String getAvailableUsers(String requesterId) throws RemoteException;

    String ping() throws RemoteException;
}