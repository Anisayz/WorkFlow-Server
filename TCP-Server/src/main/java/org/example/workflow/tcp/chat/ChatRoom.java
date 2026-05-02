package org.example.workflow.tcp.chat;

import org.example.workflow.tcp.protocol.ResponsePacket;
import org.example.workflow.tcp.server.ClientHandler;

import java.util.Set;
import java.util.UUID;
import java.util.concurrent.ConcurrentHashMap;

/** chatRoom holds all connected ClientHandlers for one team
 One instance per teamId, managed by MessageBroadcaster
 Thread safe: backed by a ConcurrentHashMap key set.
 */
public class ChatRoom {

    private final UUID                  teamId;
    private final Set<ClientHandler>    members = ConcurrentHashMap.newKeySet();

    public ChatRoom(UUID teamId) {
        this.teamId = teamId;
    }

    public UUID getTeamId() { return teamId; }

    public void join(ClientHandler handler) {
        members.add(handler);
    }

    public void leave(ClientHandler handler) {
        members.remove(handler);
    }

    public int getMemberCount() { return members.size(); }

    /**sends a packet to every connected member in this rooom and each send is independent so one failing client does not block others*/
    public void broadcast(ResponsePacket packet) {
        for (ClientHandler handler : members) {
            try {
                handler.sendResponse(packet);
            } catch (Exception ignored) {
                // stale handler — will be cleaned up when its socket closes
            }
        }
    }

    /**Sends a packet to every connected member except the sender*/
    public void broadcastExcept(ResponsePacket packet, ClientHandler exclude) {
        for (ClientHandler handler : members) {
            if (handler == exclude) continue;
            try {
                handler.sendResponse(packet);
            } catch (Exception ignored) {}
        }
    }
}