package org.example.workflow.rmi.agent;

import java.io.Serializable;

public class TaskPreview implements Serializable {
    private static final long serialVersionUID = 1L;

    private String title;
    private String status;
    private String dueDate;  // ISO string or null

    public TaskPreview() {}
    public TaskPreview(String title, String status, String dueDate) {
        this.title = title; this.status = status; this.dueDate = dueDate;
    }
    public String getTitle()   { return title; }
    public String getStatus()  { return status; }
    public String getDueDate() { return dueDate; }
}