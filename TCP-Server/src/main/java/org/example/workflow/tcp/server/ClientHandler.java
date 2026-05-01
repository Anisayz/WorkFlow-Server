package org.example.workflow.tcp.server;

import org.example.workflow.tcp.auth.AuthService;
import org.example.workflow.tcp.protocol.PacketType;
import org.example.workflow.tcp.protocol.RequestPacket;
import org.example.workflow.tcp.protocol.ResponsePacket;
import org.example.workflow.tcp.session.SessionManager;
import org.example.workflow.util.JsonMapper;

import java.io.*;
import java.net.Socket;
import java.util.UUID;
import java.util.logging.Logger;

/**
 * ClientHandler — one thread per connected client.
 *
 * Reads RequestPackets (newline-delimited JSON) in a loop,
 * dispatches to AuthService, and writes ResponsePackets back.
 */
public class ClientHandler implements Runnable {

    private static final Logger     log        = Logger.getLogger(ClientHandler.class.getName());

    private final Socket            socket;
    private final AuthService       authService = new AuthService();
    private final SessionManager    sessionMgr  = SessionManager.getInstance();

    private BufferedReader          reader;
    private PrintWriter             writer;
    private UUID                    sessionToken; // set after successful LOGIN

    public ClientHandler(Socket socket) {
        this.socket = socket;
    }

    // ── Main loop ─────────────────────────────────────────────────────────────

    @Override
    public void run() {
        try {
            reader = new BufferedReader(new InputStreamReader(socket.getInputStream()));
            writer = new PrintWriter(new OutputStreamWriter(socket.getOutputStream()), true);

            log.info("[ClientHandler] Client connected: " + socket.getInetAddress());

            String line;
            while ((line = reader.readLine()) != null) {
                RequestPacket  request  = JsonMapper.fromJson(line, RequestPacket.class);
                ResponsePacket response = dispatch(request);
                sendResponse(response);
            }

        } catch (IOException e) {
            log.warning("[ClientHandler] Connection lost: " + e.getMessage());
        } finally {
            cleanup();
        }
    }

    // ── Dispatch ──────────────────────────────────────────────────────────────

    private ResponsePacket dispatch(RequestPacket request) {
        if (request == null || request.getType() == null)
            return new ResponsePacket(PacketType.ERROR, "Malformed packet");

        return switch (request.getType()) {
            case LOGIN    -> handleLogin(request);
            case REGISTER -> authService.register(request.getPayload());
            case LOGOUT   -> handleLogout(request);
            default       -> new ResponsePacket(PacketType.ERROR, "Unknown packet type");
        };
    }

    private ResponsePacket handleLogin(RequestPacket request) {
        ResponsePacket response = authService.login(request.getPayload());
        if (response.isSuccess()) {
            sessionToken = UUID.fromString(response.getSessionToken());
            sessionMgr.registerHandler(sessionToken, this);
        }
        return response;
    }

    private ResponsePacket handleLogout(RequestPacket request) {
        ResponsePacket response = authService.logout(request.getPayload());
        if (response.isSuccess()) sessionToken = null;
        return response;
    }

    // ── I/O ───────────────────────────────────────────────────────────────────

    /** Sends a ResponsePacket as a single JSON line. */
    public synchronized void sendResponse(ResponsePacket response) {
        writer.println(JsonMapper.toJson(response));
    }

    // ── Cleanup ───────────────────────────────────────────────────────────────

    private void cleanup() {
        sessionMgr.onClientDisconnected(sessionToken);
        try { socket.close(); } catch (IOException ignored) {}
        log.info("[ClientHandler] Client disconnected: " + socket.getInetAddress());
    }
}