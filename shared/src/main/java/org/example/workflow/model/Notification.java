package org.example.workflow.model;

import org.example.workflow.model.enums.NotificationType;

import java.io.Serializable;
import java.time.LocalDateTime;
import java.util.UUID;

public class Notification implements Serializable {

    private static final long serialVersionUID = 1L;

    private UUID             id;
    private UUID             recipientId;  // determines JMS topic: "user.{recipientId}.notify"
    private NotificationType type;
    private String           title;
    private String           subtitle;
    private UUID             referenceId;  // taskId / messageId / userId depending on type
    private LocalDateTime    createdAt;

    public Notification() {
        this.createdAt = LocalDateTime.now();
    }

    public Notification(UUID id, UUID recipientId, NotificationType type,
                        String title, String subtitle, UUID referenceId) {
        this.id          = id;
        this.recipientId = recipientId;
        this.type        = type;
        this.title       = title;
        this.subtitle    = subtitle;
        this.referenceId = referenceId;
        this.createdAt   = LocalDateTime.now();
    }

    public UUID             getId()                         { return id; }
    public void             setId(UUID id)                  { this.id = id; }

    public UUID             getRecipientId()                { return recipientId; }
    public void             setRecipientId(UUID r)          { this.recipientId = r; }

    public NotificationType getType()                       { return type; }
    public void             setType(NotificationType t)     { this.type = t; }

    public String           getTitle()                      { return title; }
    public void             setTitle(String t)              { this.title = t; }

    public String           getSubtitle()                   { return subtitle; }
    public void             setSubtitle(String s)           { this.subtitle = s; }

    public UUID             getReferenceId()                { return referenceId; }
    public void             setReferenceId(UUID r)          { this.referenceId = r; }

    public LocalDateTime    getCreatedAt()                  { return createdAt; }
    public void             setCreatedAt(LocalDateTime c)   { this.createdAt = c; }

    @Override
    public String toString() {
        return "Notification{id=" + id + ", type=" + type +
                ", recipient=" + recipientId + ", title='" + title + "'}";
    }
}