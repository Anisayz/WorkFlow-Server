package org.example.workflow;

import java.net.DatagramPacket;
import java.net.DatagramSocket;
import java.nio.ByteBuffer;
import java.util.Map;
import java.util.Set;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.Executors;
import java.util.concurrent.ScheduledExecutorService;
import java.util.concurrent.TimeUnit;

public class HeartbeatServer {

    private static final int TIMEOUT_SECONDS  = 15;
    private static final int CHECK_INTERVAL   = 5;
    private static final int DEFAULT_PORT     = 9090;
    private static final int BUFFER_SIZE      = 16;
    private static final byte HEARTBEAT_TYPE  = 0x01;


    private final Map<Long, Long> onlineClients = new ConcurrentHashMap<>();

    private final int port;

    public HeartbeatServer(int port) {
        this.port = port;
    }


    /** Returns the set of currently online user IDs (as longs / UUID MSBs). */
    public Set<Long> getOnlineUsers() {
        return onlineClients.keySet();
    }

    /** Returns true if the given userId has a live heartbeat. */
    public boolean isOnline(long userId) {
        return onlineClients.containsKey(userId);
    }



    public void run() throws Exception {
        startExpiryScheduler();

        try (DatagramSocket socket = new DatagramSocket(port)) {
            System.out.println("[HeartbeatServer] Listening on UDP port " + port);

            byte[] buf = new byte[BUFFER_SIZE];

            while (true) {
                DatagramPacket incoming = new DatagramPacket(buf, buf.length);
                socket.receive(incoming);

                handlePacket(incoming.getData(), incoming.getLength());
            }
        }
    }



    private void handlePacket(byte[] data, int length) {
        if (length < 9) return;

        byte type = data[0];
        if (type != HEARTBEAT_TYPE) return;

        long userId = ByteBuffer.wrap(data, 1, 8).getLong();

        boolean isNew = !onlineClients.containsKey(userId);
        onlineClients.put(userId, System.currentTimeMillis());

        if (isNew) {
            System.out.println("[HeartbeatServer] Client online:  userId=" + userId
                    + "  (total online: " + onlineClients.size() + ")");
        }
    }

    private void startExpiryScheduler() {
        ScheduledExecutorService scheduler = Executors.newSingleThreadScheduledExecutor(r -> {
            Thread t = new Thread(r, "heartbeat-expiry");
            t.setDaemon(true);
            return t;
        });

        scheduler.scheduleAtFixedRate(this::expireClients,
                CHECK_INTERVAL, CHECK_INTERVAL, TimeUnit.SECONDS);
    }

    private void expireClients() {
        long now     = System.currentTimeMillis();
        long cutoff  = now - (TIMEOUT_SECONDS * 1000L);

        onlineClients.entrySet().removeIf(entry -> {
            boolean expired = entry.getValue() < cutoff;
            if (expired) {
                System.out.println("[HeartbeatServer] Client offline: userId=" + entry.getKey()
                        + "  (total online: " + (onlineClients.size() - 1) + ")");
            }
            return expired;
        });
    }



    public static void main(String[] args) throws Exception {
        int port = DEFAULT_PORT;

        if (args.length > 0) {
            try {
                port = Integer.parseInt(args[0]);
            } catch (NumberFormatException e) {
                System.err.println("[HeartbeatServer] Invalid port '" + args[0] + "', using " + DEFAULT_PORT);
            }
        }

        new HeartbeatServer(port).run();
    }
}