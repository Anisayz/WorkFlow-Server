package org.example.workflow.tcp.protocol;

import java.io.Serializable;

/**
 * Client → Server
 *
 * LOGIN    payload: { "username": "...", "password": "..." }
 * REGISTER payload: { "username": "...", "email": "...", "password": "...", "role": "LEADER|MEMBER" }
 * LOGOUT   payload: { "sessionToken": "uuid-string" }
 */
public class RequestPacket implements Serializable {

    private static final long serialVersionUID = 1L;

    private PacketType type;
    private String     payload; // JSON string of the request data

    public RequestPacket() {}

    public RequestPacket(PacketType type, String payload) {
        this.type    = type;
        this.payload = payload;
    }

    public PacketType getType()             { return type; }
    public void       setType(PacketType t) { this.type = t; }
    public String     getPayload()          { return payload; }
    public void       setPayload(String p)  { this.payload = p; }

    @Override
    public String toString() { return "RequestPacket{type=" + type + "}"; }
}