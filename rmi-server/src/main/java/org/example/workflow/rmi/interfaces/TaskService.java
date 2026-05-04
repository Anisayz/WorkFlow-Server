package org.example.workflow.rmi.interfaces;

import java.rmi.Remote;
import java.rmi.RemoteException;

//String IDs, JSON string returns, mirrors the client interface exactly

public interface TaskService extends Remote {

    String SERVICE_NAME = "WorkFlow/TaskService";
    int    RMI_PORT     = 1099;

    String createTask(String requesterId, String title, String description,
                      String assigneeId, String teamId,
                      String priority, String dueDate) throws RemoteException;

    String updateStatus(String requesterId, String taskId,
                        String newStatus) throws RemoteException;

    String reassignTask(String requesterId, String taskId,
                        String newAssigneeId) throws RemoteException;

    String updateDueDate(String requesterId, String taskId,
                         String newDueDate) throws RemoteException;

    String updatePriority(String requesterId, String taskId,
                          String newPriority) throws RemoteException;

    boolean deleteTask(String requesterId, String taskId) throws RemoteException;

    String getTasksByTeam(String requesterId, String teamId) throws RemoteException;

    String getMyTasks(String requesterId) throws RemoteException;

    String getTaskById(String requesterId, String taskId) throws RemoteException;

    String ping() throws RemoteException;
}