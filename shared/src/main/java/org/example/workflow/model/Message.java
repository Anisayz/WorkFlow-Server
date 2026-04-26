package org.example.workflow.model;

import java.io.Serializable;
import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;
import java.util.UUID;

public class Message implements Serializable {

    private static final long serialVersionUID = 1L;

    private static final DateTimeFormatter TIME_FMT =
            DateTimeFormatter.ofPattern("HH:mm");
    private static final DateTimeFormatter DATE_TIME_FMT =
            DateTimeFormatter.ofPattern("MMM d, HH:mm");

    private UUID          id;
    private UUID          senderId;
    private String        senderName;     // cached display name
    private UUID          teamId;
    private String        content;
    private LocalDateTime sentAt;

    public Message() {
        this.sentAt = LocalDateTime.now();
    }

    public Message(UUID id, UUID senderId, String senderName,
                   UUID teamId, String content) {
        this.id         = id;
        this.senderId   = senderId;
        this.senderName = senderName;
        this.teamId     = teamId;
        this.content    = content;
        this.sentAt     = LocalDateTime.now();
    }

    public UUID          getId()                       { return id; }
    public void          setId(UUID id)                { this.id = id; }

    public UUID          getSenderId()                 { return senderId; }
    public void          setSenderId(UUID s)           { this.senderId = s; }

    public String        getSenderName()               { return senderName; }
    public void          setSenderName(String s)       { this.senderName = s; }

    public UUID          getTeamId()                   { return teamId; }
    public void          setTeamId(UUID t)             { this.teamId = t; }

    public String        getContent()                  { return content; }
    public void          setContent(String c)          { this.content = c; }

    public LocalDateTime getSentAt()                   { return sentAt; }
    public void          setSentAt(LocalDateTime s)    { this.sentAt = s; }

    public String getFormattedTime() {
        if (sentAt == null) return "";
        if (sentAt.toLocalDate().equals(java.time.LocalDate.now()))
            return sentAt.format(TIME_FMT);
        return sentAt.format(DATE_TIME_FMT);
    }

    @Override
    public String toString() {
        return "Message{id=" + id + ", sender='" + senderName +
                "', content='" + content + "'}";
    }

    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (!(o instanceof Message)) return false;
        Message m = (Message) o;
        return id != null && id.equals(m.id);
    }

    @Override
    public int hashCode() { return id != null ? id.hashCode() : 0; }
}