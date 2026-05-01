package org.example.workflow;

import org.example.workflow.model.VoicePacket;
import org.example.workflow.network.VoicePacketSerializer;

import java.net.DatagramPacket;
import java.net.DatagramSocket;
import java.net.InetSocketAddress;
import java.util.Arrays;
import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;

public class VoiceServer {

    private static final int DEFAULT_PORT = 4000;
    private static final int BUFFER_SIZE  = 4096;


    private static final ConcurrentHashMap<String, InetSocketAddress> clients
            = new ConcurrentHashMap<>();

    public static void main(String[] args) throws Exception {
        int port = args.length > 0 ? Integer.parseInt(args[0]) : DEFAULT_PORT;

        DatagramSocket socket = new DatagramSocket(port);
        System.out.println("[Server] Voice server started on port " + port);

        byte[] buffer = new byte[BUFFER_SIZE];

        while (true) {
            DatagramPacket incoming = new DatagramPacket(buffer, buffer.length);
            socket.receive(incoming);

            byte[] data = Arrays.copyOf(incoming.getData(), incoming.getLength());
            InetSocketAddress senderAddr = new InetSocketAddress(
                    incoming.getAddress(), incoming.getPort()
            );

            try {
                VoicePacket packet = VoicePacketSerializer.decode(data);
                String senderId    = packet.userId.toString();

                switch (packet.type) {

                    case VoicePacket.JOIN -> {
                        clients.put(senderId, senderAddr);
                        System.out.printf("[Server] JOIN  | %-36s | %s%n",
                                senderId, senderAddr);

                        broadcast(socket, data, senderId);
                    }

                    case VoicePacket.AUDIO -> {

                        clients.putIfAbsent(senderId, senderAddr);
                        broadcast(socket, data, senderId);
                    }

                    case VoicePacket.LEAVE -> {
                        clients.remove(senderId);
                        System.out.printf("[Server] LEAVE | %-36s | %s%n",
                                senderId, senderAddr);
                        broadcast(socket, data, senderId);
                    }
                }

            } catch (Exception e) {
                System.err.println("[Server] Bad packet from " + senderAddr + ": " + e.getMessage());
            }
        }
    }


    private static void broadcast(DatagramSocket socket,
                                  byte[] data,
                                  String excludeId) throws Exception {
        for (Map.Entry<String, InetSocketAddress> entry : clients.entrySet()) {
            if (entry.getKey().equals(excludeId)) continue;

            InetSocketAddress dest = entry.getValue();
            DatagramPacket outgoing = new DatagramPacket(
                    data, data.length, dest.getAddress(), dest.getPort()
            );
            socket.send(outgoing);
        }
    }
}