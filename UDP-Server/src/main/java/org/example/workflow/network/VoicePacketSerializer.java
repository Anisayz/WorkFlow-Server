package org.example.workflow.network;

import org.example.workflow.model.VoicePacket;

import java.nio.ByteBuffer;
import java.util.UUID;

/**
 *   [1]  type        (byte)
 *   [8]  userId MSB  (long)
 *   [8]  userId LSB  (long)
 *   [8]  roomId MSB  (long)
 *   [8]  roomId LSB  (long)
 *   [4]  sequence    (int)
 *   [8]  timestamp   (long)
 *   [*]  audioData
 */
public class VoicePacketSerializer {

    public static byte[] encode(VoicePacket packet) {
        byte[] audio = (packet.audioData != null) ? packet.audioData : new byte[0];

        ByteBuffer buffer = ByteBuffer.allocate(45 + audio.length);
        buffer.put(packet.type);
        buffer.putLong(packet.userId.getMostSignificantBits());
        buffer.putLong(packet.userId.getLeastSignificantBits());
        buffer.putLong(packet.roomId.getMostSignificantBits());
        buffer.putLong(packet.roomId.getLeastSignificantBits());
        buffer.putInt(packet.sequence);
        buffer.putLong(packet.timestamp);
        buffer.put(audio);

        buffer.flip();
        byte[] result = new byte[buffer.limit()];
        buffer.get(result);
        return result;
    }

    public static VoicePacket decode(byte[] bytes) {
        ByteBuffer buffer = ByteBuffer.wrap(bytes);

        VoicePacket packet = new VoicePacket();
        packet.type      = buffer.get();
        packet.userId    = new UUID(buffer.getLong(), buffer.getLong());
        packet.roomId    = new UUID(buffer.getLong(), buffer.getLong());
        packet.sequence  = buffer.getInt();
        packet.timestamp = buffer.getLong();

        packet.audioData = new byte[buffer.remaining()];
        buffer.get(packet.audioData);

        return packet;
    }
}