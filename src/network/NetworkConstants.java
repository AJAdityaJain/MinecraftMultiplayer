package network;

public class NetworkConstants {
    public static final byte C2S_CHUNK_REQUEST  = (byte) 100;
    public static final byte C2S_PLAYER_JOIN    = (byte) 101;
    public static final byte C2S_PLAYER_LEAVE   = (byte) 102;
    public static final byte C2S_PLAYER_MOVE    = (byte) 103;



    public static final byte S2C_CHUNK_SEND     = (byte) 200;
    public static final byte S2C_PLAYER_JOIN    = (byte) 201;
    public static final byte S2C_PLAYER_LEAVE   = (byte) 202;
    public static final byte S2C_PLAYER_MOVE    = (byte) 203;

    public static final int PACKET_SIZE = 1024;
    public static final int TCP_PORT = 8080;
    public static final int UDP_PORT = 8081;
    public static boolean RUNNING = true;
}
