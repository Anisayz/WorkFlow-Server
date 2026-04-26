package org.example.workflow.model.enums;

public enum NotificationType {
    TASK,       // task assigned, updated, or completed
    MESSAGE,    // new chat message
    SYSTEM      // member joined/left, team created, etc.
}