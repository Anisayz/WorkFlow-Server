package org.example.workflow.tcp.server;

import java.io.IOException;
import java.net.ServerSocket;
import java.net.Socket;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;
import java.util.logging.Logger;

/**
 * TcpServer — listens on port 5000 and spawns a ClientHandler thread per connection.
 * Uses a cached thread pool so the number of threads scales with connected clients.
 */
public class TcpServer {

    private static final Logger  log  = Logger.getLogger(TcpServer.class.getName());

    private final int            port;
    private final ExecutorService pool = Executors.newCachedThreadPool();
    private volatile boolean     running;

    public TcpServer(int port) { this.port = port; }

    public void start() {
        running = true;
        log.info("[TcpServer] Listening on port " + port);

        try (ServerSocket serverSocket = new ServerSocket(port)) {
            while (running) {
                Socket client = serverSocket.accept();
                pool.execute(new ClientHandler(client));
            }
        } catch (IOException e) {
            if (running) log.severe("[TcpServer] Fatal error: " + e.getMessage());
        } finally {
            pool.shutdown();
        }
    }

    public void stop() { running = false; }
}