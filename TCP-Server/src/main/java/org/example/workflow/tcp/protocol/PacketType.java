package org.example.workflow.tcp.protocol;

public enum PacketType {
    LOGIN,
    REGISTER,
    LOGOUT,
    CHAT_SEND,
    CHAT_HISTORY,
    CHAT_MESSAGE,
    GET_TEAM_MEMBERS,   // client → server: { "teamId": "..." }
    // server → client: { "members": [ User, ... ] }
    SUCCESS,
    ERROR
}