package org.example.workflow.tcp;


import org.example.workflow.state.StateLoader;
import org.example.workflow.tcp.server.TcpServer;

/**
 * TcpServerApp — entry point for the TCP server module.
 *
 * Boot sequence:
 *   1. StateLoader.load()  — fills AppState from JSON files
 *   2. TcpServer.start()   — opens port 5000 and starts accepting clients
 */
public class TcpServerApp {

    private static final int PORT = 5000;

    public static void main(String[] args) {
        StateLoader.load();
        new TcpServer(PORT).start();
    }
}