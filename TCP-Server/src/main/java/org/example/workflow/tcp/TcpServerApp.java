package org.example.workflow.tcp;


import org.example.workflow.state.StateLoader;
import org.example.workflow.tcp.server.TcpServer;

public class TcpServerApp {

    private static final int PORT = 5000;

    public static void main(String[] args) {
        StateLoader.load();
        new TcpServer(PORT).start();
    }
}