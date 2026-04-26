package org.example.workflow.model;

import org.example.workflow.model.enums.Priority;
import org.example.workflow.model.enums.TaskStatus;

import java.io.Serializable;
import java.time.LocalDate;
import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;
import java.util.UUID;

public class Task implements Serializable {

    private static final long serialVersionUID = 1L;

    private static final DateTimeFormatter DATE_FMT =
            DateTimeFormatter.ofPattern("MMM d, yyyy");

    private UUID          id;
    private String        title;
    private String        description;
    private UUID          assigneeId;       // null if unassigned
    private String        assigneeName;     // cached display name
    private UUID          teamId;
    private TaskStatus    status;
    private Priority      priority;
    private LocalDate     dueDate;
    private LocalDateTime createdAt;
    private LocalDateTime updatedAt;

    public Task() {
        this.status    = TaskStatus.PENDING;
        this.priority  = Priority.MEDIUM;
        this.createdAt = LocalDateTime.now();
        this.updatedAt = LocalDateTime.now();
    }

    public Task(UUID id, String title, String description,
                UUID assigneeId, String assigneeName,
                UUID teamId, Priority priority, LocalDate dueDate) {
        this();
        this.id           = id;
        this.title        = title;
        this.description  = description;
        this.assigneeId   = assigneeId;
        this.assigneeName = assigneeName;
        this.teamId       = teamId;
        this.priority     = priority;
        this.dueDate      = dueDate;
    }

    public UUID          getId()                        { return id; }
    public void          setId(UUID id)                 { this.id = id; }

    public String        getTitle()                     { return title; }
    public void          setTitle(String t)             { this.title = t; }

    public String        getDescription()               { return description; }
    public void          setDescription(String d)       { this.description = d; }

    public UUID          getAssigneeId()                { return assigneeId; }
    public void          setAssigneeId(UUID a)          { this.assigneeId = a; }

    public String        getAssigneeName()              { return assigneeName; }
    public void          setAssigneeName(String a)      { this.assigneeName = a; }

    public UUID          getTeamId()                    { return teamId; }
    public void          setTeamId(UUID t)              { this.teamId = t; }

    public TaskStatus    getStatus()                    { return status; }
    public void          setStatus(TaskStatus s)        { this.status = s; touch(); }

    public Priority      getPriority()                  { return priority; }
    public void          setPriority(Priority p)        { this.priority = p; }

    public LocalDate     getDueDate()                   { return dueDate; }
    public void          setDueDate(LocalDate d)        { this.dueDate = d; }

    public LocalDateTime getCreatedAt()                 { return createdAt; }
    public void          setCreatedAt(LocalDateTime c)  { this.createdAt = c; }

    public LocalDateTime getUpdatedAt()                 { return updatedAt; }
    public void          setUpdatedAt(LocalDateTime u)  { this.updatedAt = u; }

    /** Called automatically on every status change. */
    public void touch() { this.updatedAt = LocalDateTime.now(); }

    public boolean isOverdue() {
        return dueDate != null
                && LocalDate.now().isAfter(dueDate)
                && status != TaskStatus.DONE;
    }

    public boolean isDone() { return status == TaskStatus.DONE; }

    public String getFormattedDueDate() {
        return dueDate == null ? "No due date" : dueDate.format(DATE_FMT);
    }

    @Override
    public String toString() {
        return "Task{id=" + id + ", title='" + title +
                "', status=" + status + ", assignee='" + assigneeName + "'}";
    }

    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (!(o instanceof Task)) return false;
        Task t = (Task) o;
        return id != null && id.equals(t.id);
    }

    @Override
    public int hashCode() { return id != null ? id.hashCode() : 0; }
}