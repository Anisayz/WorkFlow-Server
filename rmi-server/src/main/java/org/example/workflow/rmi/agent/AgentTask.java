package org.example.workflow.rmi.agent;

import java.io.Serializable;

public class AgentTask implements Serializable {
    private static final long serialVersionUID = 1L;

    private String id;
    private String title;
    private String description;
    private String assigneeId;
    private String assigneeName;
    private String teamId;
    private String status;        // "PENDING" | "IN_PROGRESS" | "DONE"
    private String priority;      // "HIGH" | "MEDIUM" | "LOW"
    private String dueDate;       // ISO date string or null

    public AgentTask() {}

    public AgentTask(String id, String title, String description,
                     String assigneeId, String assigneeName, String teamId,
                     String status, String priority, String dueDate) {
        this.id = id; this.title = title; this.description = description;
        this.assigneeId = assigneeId; this.assigneeName = assigneeName;
        this.teamId = teamId; this.status = status; this.priority = priority;
        this.dueDate = dueDate;
    }

    public String getId()           { return id; }
    public String getTitle()        { return title; }
    public String getDescription()  { return description; }
    public String getAssigneeId()   { return assigneeId; }
    public String getAssigneeName() { return assigneeName; }
    public String getTeamId()       { return teamId; }
    public String getStatus()       { return status; }
    public String getPriority()     { return priority; }
    public String getDueDate()      { return dueDate; }
}