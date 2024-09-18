package network;

public class NetworkConstants {

    //0 -> Blank byte which will convert to byte_ID

    // X:4, Y:4, Z:4
    public static final byte C2S_CHUNK_REQUEST  = (byte) 100;
    // ID, 0, X:4, Y:4, Z:4
    public static final byte C2S_PLAYER_MOVE    = (byte) 103;



    // Chunk serial format
    public static final byte S2C_CHUNK_SEND     = (byte) 200;
    // ID
    public static final byte S2C_PLAYER_JOIN    = (byte) 201;
    // ID
    public static final byte S2C_PLAYER_LEAVE   = (byte) 202;
    // ID, FROM, X:4, Y:4, Z:4
    public static final byte S2C_PLAYER_MOVE    = (byte) 203;

    public static final int PACKET_SIZE = 1024;
    public static final int TCP_PORT = 8080;
    public static final int UDP_PORT = 8081;

    public static boolean RUNNING = true;
    public static final int MAX_CLIENTS = 2;

}
