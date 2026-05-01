package org.example.workflow.tcp.protocol;

import java.io.Serializable;

/**
 * Server → Client
 *
 * success      true / false
 * message      human-readable result or error text
 * data         JSON string of the returned object (client-safe User on login)
 * sessionToken UUID string — set on successful LOGIN, null otherwise
 */
public class ResponsePacket implements Serializable {

    private static final long serialVersionUID = 1L;

    private PacketType type;
    private boolean    success;
    private String     message;
    private String     data;
    private String     sessionToken;

    public ResponsePacket() {}

    /** Success constructor */
    public ResponsePacket(PacketType type, String message, String data, String sessionToken) {
        this.type         = type;
        this.success      = true;
        this.message      = message;
        this.data         = data;
        this.sessionToken = sessionToken;
    }

    /** Error constructor */
    public ResponsePacket(PacketType type, String errorMessage) {
        this.type    = type;
        this.success = false;
        this.message = errorMessage;
    }

    public PacketType getType()                 { return type; }
    public void       setType(PacketType t)     { this.type = t; }
    public boolean    isSuccess()               { return success; }
    public void       setSuccess(boolean s)     { this.success = s; }
    public String     getMessage()              { return message; }
    public void       setMessage(String m)      { this.message = m; }
    public String     getData()                 { return data; }
    public void       setData(String d)         { this.data = d; }
    public String     getSessionToken()         { return sessionToken; }
    public void       setSessionToken(String t) { this.sessionToken = t; }

    @Override
    public String toString() {
        return "ResponsePacket{type=" + type + ", success=" + success + ", message='" + message + "'}";
    }
}