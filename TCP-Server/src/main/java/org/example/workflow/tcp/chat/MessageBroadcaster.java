package org.example.workflow.tcp.chat;

import org.example.workflow.tcp.server.ClientHandler;

import java.util.Map;
import java.util.UUID;
import java.util.concurrent.ConcurrentHashMap;

// MessageBroadcaster singleton registry of all active ChatRooms, one ChatRoom per teamId, ClientHandler joins/leaves on connect/disconnect
// calls broadcast() when a new message arrives.


public final class MessageBroadcaster {

    private static volatile MessageBroadcaster instance;

    public static MessageBroadcaster getInstance() {
        if (instance == null) {
            synchronized (MessageBroadcaster.class) {
                if (instance == null) instance = new MessageBroadcaster();
            }
        }
        return instance;
    }

    private final Map<UUID, ChatRoom> rooms = new ConcurrentHashMap<>();

    private MessageBroadcaster() {}

    // Adds a client to the room for their team and creates the room if needed
    public void join(UUID teamId, ClientHandler handler) {
        rooms.computeIfAbsent(teamId, ChatRoom::new).join(handler);
    }

    // removes a client from their team room
    public void leave(UUID teamId, ClientHandler handler) {
        ChatRoom room = rooms.get(teamId);
        if (room != null) room.leave(handler);
    }

    //returns the room for a team, creating it if it doesnt exist
    public ChatRoom getOrCreate(UUID teamId) {
        return rooms.computeIfAbsent(teamId, ChatRoom::new);
    }

    public ChatRoom getRoom(UUID teamId) {
        return rooms.get(teamId);
    }
}