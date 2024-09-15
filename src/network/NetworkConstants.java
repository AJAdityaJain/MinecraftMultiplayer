package network;

public class NetworkConstants {
    public static final byte C2S_CHUNK_REQUEST  = (byte) 0;
    public static final byte C2S_PLAYER_JOIN    = (byte) 1;
    public static final byte C2S_PLAYER_LEAVE   = (byte) 2;
    public static final byte C2S_PLAYER_MOVE    = (byte) 3;



    public static final byte S2C_CHUNK_SEND     = (byte) 255;
    public static final byte S2C_PLAYER_JOIN    = (byte) 254;
    public static final byte S2C_PLAYER_LEAVE   = (byte) 253;
    public static final byte S2C_PLAYER_MOVE    = (byte) 252;

    public static final int PACKET_SIZE = 1024;
}
