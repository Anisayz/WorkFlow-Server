package org.example.workflow.model;

import com.google.gson.annotations.SerializedName;

import java.io.Serializable;
import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.List;
import java.util.UUID;

public class Team implements Serializable {

    private static final long serialVersionUID = 1L;

    private UUID          id;
    private String        name;
    private String        description;
    private String        inviteCode;
    private UUID          leaderId;
    @SerializedName("members")
    private List<UUID>    memberIds;
    private LocalDateTime createdAt;

    public Team() {
        this.memberIds = new ArrayList<>();
        this.createdAt = LocalDateTime.now();
    }

    public Team(UUID id, String name, String inviteCode, UUID leaderId) {
        this();
        this.id         = id;
        this.name       = name;
        this.inviteCode = inviteCode;
        this.leaderId   = leaderId;
        this.memberIds.add(leaderId); // leader is always a member
    }

    public UUID          getId()                        { return id; }
    public void          setId(UUID id)                 { this.id = id; }

    public String        getName()                      { return name; }
    public void          setName(String n)              { this.name = n; }

    public String        getDescription()               { return description; }
    public void          setDescription(String d)       { this.description = d; }

    public String        getInviteCode()                { return inviteCode; }
    public void          setInviteCode(String c)        { this.inviteCode = c; }

    public UUID          getLeaderId()                  { return leaderId; }
    public void          setLeaderId(UUID l)            { this.leaderId = l; }

    public List<UUID>    getMemberIds()                 { return memberIds; }
    public void          setMemberIds(List<UUID> m)     { this.memberIds = m; }

    public LocalDateTime getCreatedAt()                 { return createdAt; }
    public void          setCreatedAt(LocalDateTime c)  { this.createdAt = c; }

    public void    addMember(UUID userId)    { if (!memberIds.contains(userId)) memberIds.add(userId); }
    public void    removeMember(UUID userId) { memberIds.remove(userId); }
    public int     getMemberCount()          { return memberIds.size(); }
    public boolean hasMember(UUID userId)    { return memberIds.contains(userId); }
    public boolean isLeader(UUID userId)     { return userId != null && userId.equals(leaderId); }

    @Override
    public String toString() {
        return "Team{id=" + id + ", name='" + name + "', members=" + memberIds.size() + "}";
    }

    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (!(o instanceof Team)) return false;
        Team t = (Team) o;
        return id != null && id.equals(t.id);
    }

    @Override
    public int hashCode() { return id != null ? id.hashCode() : 0; }
}