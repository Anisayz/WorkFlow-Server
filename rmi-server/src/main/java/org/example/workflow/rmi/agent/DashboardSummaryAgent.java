package org.example.workflow.rmi.agent;

import java.io.Serializable;
import java.time.LocalDate;
import java.util.ArrayList;
import java.util.Comparator;
import java.util.List;

public class DashboardSummaryAgent implements Agent<DashboardSummary> {
    private static final long serialVersionUID = 2L;

    private final String userId;
    private final String teamId;
    private final int    topTasksLimit;

    public DashboardSummaryAgent(String userId, String teamId, int topTasksLimit) {
        this.userId        = userId;
        this.teamId        = teamId;
        this.topTasksLimit = topTasksLimit;
    }

    @Override
    public DashboardSummary execute(ServerContext ctx) {
        DashboardSummary summary = new DashboardSummary();

        // ── Team info ─────────────────────────────────────────────────────────
        AgentTeam       team    = teamId != null ? ctx.getTeam(teamId)        : null;
        List<AgentUser> members = teamId != null ? ctx.getTeamMembers(teamId) : new ArrayList<>();

        if (team != null) summary.setTeamName(team.getName());
        summary.setTotalMembers(members.size());

        List<String> online  = new ArrayList<>();
        List<String> offline = new ArrayList<>();
        for (AgentUser m : members) {
            if (m.isOnline()) online.add(m.getUsername());
            else              offline.add(m.getUsername());
        }
        summary.setOnlineMembers(online.size());
        summary.setOnlineMemberNames(online);
        summary.setOfflineMemberNames(offline);

        LocalDate today = LocalDate.now();

        // ── My tasks stats + top tasks list ───────────────────────────────────
        List<AgentTask> myTasks = ctx.getUserTasks(userId);
        int myDueToday = 0, myInProgress = 0, myCompleted = 0;
        for (AgentTask t : myTasks) {
            String s = t.getStatus();
            if ("IN_PROGRESS".equals(s)) myInProgress++;
            else if ("DONE".equals(s))   myCompleted++;
            if (!"DONE".equals(s) && t.getDueDate() != null) {
                try { if (today.equals(LocalDate.parse(t.getDueDate()))) myDueToday++; }
                catch (Exception ignored) {}
            }
        }
        summary.setMyTasksDueToday(myDueToday);
        summary.setMyTasksInProgress(myInProgress);
        summary.setMyTasksCompleted(myCompleted);

        // Top N active tasks for the user
        List<AgentTask> active = new ArrayList<>();
        for (AgentTask t : myTasks) if (!"DONE".equals(t.getStatus())) active.add(t);
        active.sort(new DueDateComparator());

        List<TaskPreview> top = new ArrayList<>();
        int limit = Math.min(topTasksLimit, active.size());
        for (int i = 0; i < limit; i++) {
            AgentTask t = active.get(i);
            top.add(new TaskPreview(t.getTitle(), t.getStatus(), t.getDueDate()));
        }
        summary.setMyTopTasks(top);

        // ── Team-wide task stats (for leaders) ────────────────────────────────
        if (teamId != null) {
            List<AgentTask> teamTasks = ctx.getTeamTasks(teamId);
            int teamDueToday = 0, teamInProgress = 0, teamCompleted = 0;
            for (AgentTask t : teamTasks) {
                String s = t.getStatus();
                if ("IN_PROGRESS".equals(s)) teamInProgress++;
                else if ("DONE".equals(s))   teamCompleted++;
                if (!"DONE".equals(s) && t.getDueDate() != null) {
                    try { if (today.equals(LocalDate.parse(t.getDueDate()))) teamDueToday++; }
                    catch (Exception ignored) {}
                }
            }
            summary.setTeamTasksDueToday(teamDueToday);
            summary.setTeamTasksInProgress(teamInProgress);
            summary.setTeamTasksCompleted(teamCompleted);
        }

        return summary;
    }

    private static class DueDateComparator
            implements Comparator<AgentTask>, Serializable {
        private static final long serialVersionUID = 1L;
        @Override
        public int compare(AgentTask a, AgentTask b) {
            String da = a.getDueDate();
            String db = b.getDueDate();
            if (da == null && db == null) return 0;
            if (da == null) return 1;
            if (db == null) return -1;
            return da.compareTo(db);
        }
    }
}