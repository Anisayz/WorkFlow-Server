package org.example.workflow.rmi.agent;

import java.io.Serializable;

public class AgentUser implements Serializable {
    private static final long serialVersionUID = 1L;

    private String  id;
    private String  username;
    private String  email;
    private String  role;
    private boolean online;
    private String  teamId;

    public AgentUser() {}

    public AgentUser(String id, String username, String email, String role,
                     boolean online, String teamId) {
        this.id = id; this.username = username; this.email = email;
        this.role = role; this.online = online; this.teamId = teamId;
    }

    public String  getId()       { return id; }
    public String  getUsername() { return username; }
    public String  getEmail()    { return email; }
    public String  getRole()     { return role; }
    public boolean isOnline()    { return online; }
    public String  getTeamId()   { return teamId; }
}