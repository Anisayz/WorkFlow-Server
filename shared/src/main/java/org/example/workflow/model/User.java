package org.example.workflow.model;

import org.example.workflow.model.enums.Role;
import java.io.Serializable;
import java.util.UUID;

public class User implements Serializable {

    private static final long serialVersionUID = 1L;

    private UUID   id;
    private String username;
    private String email;
    private String hashedPassword;   // BCrypt — never transmitted to client
    private Role   role;
    private UUID   teamId;           // null if not in a team

    public User() {}

    public User(UUID id, String username, String email,
                String hashedPassword, Role role) {
        this.id             = id;
        this.username       = username;
        this.email          = email;
        this.hashedPassword = hashedPassword;
        this.role           = role;
    }

    public UUID   getId()                        { return id; }
    public void   setId(UUID id)                 { this.id = id; }

    public String getUsername()                  { return username; }
    public void   setUsername(String u)          { this.username = u; }

    public String getEmail()                     { return email; }
    public void   setEmail(String e)             { this.email = e; }

    public String getHashedPassword()            { return hashedPassword; }
    public void   setHashedPassword(String h)    { this.hashedPassword = h; }

    public Role   getRole()                      { return role; }
    public void   setRole(Role r)                { this.role = r; }

    public UUID   getTeamId()                    { return teamId; }
    public void   setTeamId(UUID t)              { this.teamId = t; }

    public boolean isLeader() { return role == Role.LEADER; }

    /**
     * Returns a safe copy stripped of the password hash.
     * Always call this before sending a User over TCP or RMI.
     */
    public User toClientSafe() {
        User safe = new User();
        safe.id       = this.id;
        safe.username = this.username;
        safe.email    = this.email;
        safe.role     = this.role;
        safe.teamId   = this.teamId;
        return safe;
    }

    public String getInitial() {
        if (username == null || username.trim().isEmpty()) return "?";
        return String.valueOf(username.charAt(0)).toUpperCase();
    }

    @Override
    public String toString() {
        return "User{id=" + id + ", username='" + username + "', role=" + role + "}";
    }

    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (!(o instanceof User)) return false;
        User u = (User) o;
        return id != null && id.equals(u.id);
    }

    @Override
    public int hashCode() { return id != null ? id.hashCode() : 0; }
}