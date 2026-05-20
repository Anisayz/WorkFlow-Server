package org.example.workflow.rmi.agent;

import java.io.Serializable;
import java.util.List;

public class AgentTeam implements Serializable {
    private static final long serialVersionUID = 1L;

    private String       id;
    private String       name;
    private String       inviteCode;
    private String       leaderId;
    private List<String> memberIds;

    public AgentTeam() {}

    public AgentTeam(String id, String name, String inviteCode,
                     String leaderId, List<String> memberIds) {
        this.id = id; this.name = name; this.inviteCode = inviteCode;
        this.leaderId = leaderId; this.memberIds = memberIds;
    }

    public String       getId()         { return id; }
    public String       getName()       { return name; }
    public String       getInviteCode() { return inviteCode; }
    public String       getLeaderId()   { return leaderId; }
    public List<String> getMemberIds()  { return memberIds; }
}