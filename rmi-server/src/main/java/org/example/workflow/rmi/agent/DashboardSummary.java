package org.example.workflow.rmi.agent;

import java.io.Serializable;
import java.util.List;

/**
 * DashboardSummary — result of DashboardSummaryAgent.
 *
 * Carries TWO sets of task stats:
 *   - my* fields    → tasks assigned to the current user
 *   - team* fields  → all tasks in the team (used when leader views dashboard)
 *
 * The client picks which set to display based on the user's role.
 */
public class DashboardSummary implements Serializable {
    private static final long serialVersionUID = 2L;

    private String              teamName;
    private int                 totalMembers;
    private int                 onlineMembers;
    private List<String>        onlineMemberNames;
    private List<String>        offlineMemberNames;

    // User's own tasks — used by member, also used in My Tasks list for everyone
    private int                 myTasksDueToday;
    private int                 myTasksInProgress;
    private int                 myTasksCompleted;
    private List<TaskPreview>   myTopTasks;

    // Team-wide tasks — used by leader for the stat cards
    private int                 teamTasksDueToday;
    private int                 teamTasksInProgress;
    private int                 teamTasksCompleted;

    public DashboardSummary() {}

    public String              getTeamName()           { return teamName; }
    public void                setTeamName(String s)   { this.teamName = s; }

    public int                 getTotalMembers()       { return totalMembers; }
    public void                setTotalMembers(int n)  { this.totalMembers = n; }

    public int                 getOnlineMembers()      { return onlineMembers; }
    public void                setOnlineMembers(int n) { this.onlineMembers = n; }

    public List<String>        getOnlineMemberNames()  { return onlineMemberNames; }
    public void                setOnlineMemberNames(List<String> l) { this.onlineMemberNames = l; }

    public List<String>        getOfflineMemberNames() { return offlineMemberNames; }
    public void                setOfflineMemberNames(List<String> l){ this.offlineMemberNames = l; }

    public int                 getMyTasksDueToday()       { return myTasksDueToday; }
    public void                setMyTasksDueToday(int n)  { this.myTasksDueToday = n; }

    public int                 getMyTasksInProgress()     { return myTasksInProgress; }
    public void                setMyTasksInProgress(int n){ this.myTasksInProgress = n; }

    public int                 getMyTasksCompleted()      { return myTasksCompleted; }
    public void                setMyTasksCompleted(int n) { this.myTasksCompleted = n; }

    public List<TaskPreview>   getMyTopTasks()            { return myTopTasks; }
    public void                setMyTopTasks(List<TaskPreview> l) { this.myTopTasks = l; }

    public int                 getTeamTasksDueToday()       { return teamTasksDueToday; }
    public void                setTeamTasksDueToday(int n)  { this.teamTasksDueToday = n; }

    public int                 getTeamTasksInProgress()     { return teamTasksInProgress; }
    public void                setTeamTasksInProgress(int n){ this.teamTasksInProgress = n; }

    public int                 getTeamTasksCompleted()      { return teamTasksCompleted; }
    public void                setTeamTasksCompleted(int n) { this.teamTasksCompleted = n; }
}