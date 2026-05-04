package org.example.workflow;

import java.net.DatagramPacket;
import java.net.DatagramSocket;
import java.net.InetSocketAddress;
import java.nio.ByteBuffer;
import java.util.Map;
import java.util.Set;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.Executors;
import java.util.concurrent.ScheduledExecutorService;
import java.util.concurrent.TimeUnit;


public class HeartbeatServer {

    // ── Config ───────────────────────────────────────────────────────
    private static final int  TIMEOUT_SECONDS = 15;
    private static final int  CHECK_INTERVAL  = 5;
    private static final int  DEFAULT_PORT    = 9090;
    private static final int  BUFFER_SIZE     = 64;

    // ── Packet types ─────────────────────────────────────────────────
    public static final byte TYPE_HEARTBEAT = 0x01;
    public static final byte TYPE_ACK       = 0x02;
    public static final byte TYPE_USERLIST  = 0x03;

    // ── State ────────────────────────────────────────────────────────

    private final ConcurrentHashMap<Long, Long>              lastSeen  = new ConcurrentHashMap<>();

    private final ConcurrentHashMap<Long, InetSocketAddress> addresses = new ConcurrentHashMap<>();

    private final int port;
    private DatagramSocket socket;

    // ────────────────────────────────────────────────────────────────

    public HeartbeatServer(int port) {
        this.port = port;
    }

    // ── Public query API ─────────────────────────────────────────────

    public Set<Long> getOnlineUsers()      { return lastSeen.keySet(); }
    public boolean   isOnline(long userId) { return lastSeen.containsKey(userId); }

    // ── Main loop ────────────────────────────────────────────────────

    public void run() throws Exception {
        startExpiryScheduler();

        socket = new DatagramSocket(port);
        System.out.println("[HeartbeatServer] Listening on UDP port " + port);

        byte[] buf = new byte[BUFFER_SIZE];

        while (true) {
            DatagramPacket incoming = new DatagramPacket(buf, buf.length);
            socket.receive(incoming);

            InetSocketAddress sender = new InetSocketAddress(
                    incoming.getAddress(), incoming.getPort());

            handlePacket(incoming.getData(), incoming.getLength(), sender);
        }
    }

    // ── Packet handling ──────────────────────────────────────────────

    private void handlePacket(byte[] data, int length, InetSocketAddress sender) {
        if (length < 9) return;

        byte type = data[0];
        if (type != TYPE_HEARTBEAT) return;

        long userId = ByteBuffer.wrap(data, 1, 8).getLong();

        boolean isNew = !lastSeen.containsKey(userId);

        lastSeen.put(userId, System.currentTimeMillis());
        addresses.put(userId, sender);

        if (isNew) {
            System.out.printf("[HeartbeatServer] ONLINE  userId=%d  total=%d%n",
                    userId, lastSeen.size());
        }

        // Send ACK back to this client
        sendAck(sender);

        // Broadcast updated user list to ALL clients
        broadcastUserList();
    }

    // ── Outgoing packets ─────────────────────────────────────────────

    private void sendAck(InetSocketAddress dest) {
        try {
            ByteBuffer buf = ByteBuffer.allocate(5);
            buf.put(TYPE_ACK);
            buf.putInt(lastSeen.size());
            send(buf.array(), dest);
        } catch (Exception e) {
            System.err.println("[HeartbeatServer] ACK send error: " + e.getMessage());
        }
    }


    private void broadcastUserList() {
        Long[] userIds = lastSeen.keySet().toArray(new Long[0]);
        int count = userIds.length;

        ByteBuffer buf = ByteBuffer.allocate(5 + count * 8);
        buf.put(TYPE_USERLIST);
        buf.putInt(count);
        for (long id : userIds) buf.putLong(id);

        byte[] packet = buf.array();

        for (InetSocketAddress addr : addresses.values()) {
            try {
                send(packet, addr);
            } catch (Exception e) {
                System.err.println("[HeartbeatServer] USERLIST send error to "
                        + addr + ": " + e.getMessage());
            }
        }
    }

    private void send(byte[] data, InetSocketAddress dest) throws Exception {
        DatagramPacket dp = new DatagramPacket(
                data, data.length, dest.getAddress(), dest.getPort());
        socket.send(dp);
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
        long cutoff  = System.currentTimeMillis() - (TIMEOUT_SECONDS * 1000L);
        boolean[] anyExpired = {false};

        lastSeen.entrySet().removeIf(entry -> {
            boolean expired = entry.getValue() < cutoff;
            if (expired) {
                addresses.remove(entry.getKey());
                System.out.printf("[HeartbeatServer] OFFLINE userId=%d  total=%d%n",
                        entry.getKey(), lastSeen.size() - 1);
                anyExpired[0] = true;
            }
            return expired;
        });

        // Broadcast updated list only if something changed
        if (anyExpired[0]) broadcastUserList();
    }

    // ── Entry point ──────────────────────────────────────────────────

    public static void main(String[] args) throws Exception {
        int port = DEFAULT_PORT;
        if (args.length > 0) {
            try {
                port = Integer.parseInt(args[0]);
            } catch (NumberFormatException e) {
                System.err.println("[HeartbeatServer] Invalid port, using " + DEFAULT_PORT);
            }
        }
        new HeartbeatServer(port).run();
    }
}