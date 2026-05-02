package org.example.workflow.tcp.chat;

import com.google.gson.JsonObject;
import com.google.gson.JsonParser;
import org.example.workflow.db.repository.MessageRepository;
import org.example.workflow.model.Message;
import org.example.workflow.model.User;
import org.example.workflow.state.AppState;
import org.example.workflow.tcp.protocol.PacketType;
import org.example.workflow.tcp.protocol.ResponsePacket;
import org.example.workflow.tcp.server.ClientHandler;
import org.example.workflow.util.JsonMapper;

import java.util.List;
import java.util.UUID;
import java.util.logging.Logger;

//chat logic
// handles CHAT_SEND and CHAT_HISTORY packets from ClientHandler

public class ChatService {

    private static final Logger      log       = Logger.getLogger(ChatService.class.getName());
    private static final int         HISTORY_LIMIT = 50;

    private final MessageRepository  msgRepo   = new MessageRepository();
    private final MessageBroadcaster broadcaster = MessageBroadcaster.getInstance();
    private final AppState           state     = AppState.getInstance();

    // send message
    //Validates, persists, and broadcasts a new chat message
    public ResponsePacket handleSend(String payload, ClientHandler sender) {
        try {
            JsonObject json         = JsonParser.parseString(payload).getAsJsonObject();
            String     tokenStr     = json.get("sessionToken").getAsString();
            String     teamIdStr    = json.get("teamId").getAsString();
            String     content      = json.get("content").getAsString().trim();

            if (content.isBlank())
                return new ResponsePacket(PacketType.ERROR, "Message cannot be empty");

            // Validate session
            UUID token = UUID.fromString(tokenStr);
            User user  = state.getUserBySession(token).orElse(null);
            if (user == null)
                return new ResponsePacket(PacketType.ERROR, "Invalid session");

            UUID teamId = UUID.fromString(teamIdStr);

            // Build and persist message
            Message message = new Message(
                    UUID.randomUUID(),
                    user.getId(),
                    user.getUsername(),
                    teamId,
                    content
            );
            msgRepo.save(message);

            log.info("[ChatService] Message from " + user.getUsername() + " in team " + teamId);

            // Broadcast to all room members (including sender so they get the ACK with server timestamp)
            String      msgJson  = JsonMapper.toJson(message);
            ResponsePacket push  = new ResponsePacket(PacketType.CHAT_MESSAGE, "New message", msgJson, null);
            broadcaster.getOrCreate(teamId).broadcast(push);

            return new ResponsePacket(PacketType.SUCCESS, "Message sent", null, null);

        } catch (Exception e) {
            log.severe("[ChatService] handleSend error: " + e.getMessage());
            return new ResponsePacket(PacketType.ERROR, "Failed to send message: " + e.getMessage());
        }
    }

    //history
    //returns the last N messages for a team

    public ResponsePacket handleHistory(String payload) {
        try {
            JsonObject json      = JsonParser.parseString(payload).getAsJsonObject();
            String     tokenStr  = json.get("sessionToken").getAsString();
            String     teamIdStr = json.get("teamId").getAsString();

            UUID token = UUID.fromString(tokenStr);
            if (!state.isSessionValid(token))
                return new ResponsePacket(PacketType.ERROR, "Invalid session");

            UUID         teamId  = UUID.fromString(teamIdStr);
            List<Message> history = msgRepo.findByTeam(teamId, HISTORY_LIMIT);
            String        json2   = JsonMapper.toJson(history);

            return new ResponsePacket(PacketType.SUCCESS, "History loaded", json2, null);

        } catch (Exception e) {
            log.severe("[ChatService] handleHistory error: " + e.getMessage());
            return new ResponsePacket(PacketType.ERROR, "Failed to load history: " + e.getMessage());
        }
    }

    //room membership

    public void joinRoom(UUID teamId, ClientHandler handler) {
        broadcaster.join(teamId, handler);
        log.info("[ChatService] Client joined room: " + teamId);
    }

    public void leaveRoom(UUID teamId, ClientHandler handler) {
        broadcaster.leave(teamId, handler);
    }
}