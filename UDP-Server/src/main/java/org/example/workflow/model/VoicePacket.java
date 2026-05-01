package org.example.workflow.model;

import java.util.UUID;

public class VoicePacket {

    public static final byte AUDIO = 0;
    public static final byte JOIN  = 1;
    public static final byte LEAVE = 2;

    public byte    type;
    public UUID    userId;
    public UUID    roomId;
    public int     sequence;
    public long    timestamp;
    public byte[]  audioData;

    public boolean isAudio() { return type == AUDIO; }
}