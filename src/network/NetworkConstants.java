package network;

public class NetworkConstants {

    //0 -> Blank byte which will convert to byte_ID

    public static final byte NULL    = (byte) -1;
    //type X:4bytes, Y:4bytes, Z:4bytes
    public static final byte C2S_CHUNK_REQUEST  = 0x7C;
    //type ID:1b, X:4b, Y:4b, Z:4b
    public static final byte C2S_PLAYER_MOVE    = 0x7D;
    // BlockState serial format methods
    public static final byte C2S_BLOCK_PLACE    = 0x70;
    //type short string
    public static final byte C2S_LOG            = 0x60;



    // Chunk serial format methods
    public static final byte S2C_CHUNK_SEND     = 0x40;
    // BlockState serial format methods
    public static final byte S2C_BLOCK_PLACE     = 0x50;
    //type ID:1b
    public static final byte S2C_PLAYER_JOIN    = 0x51;
    //type ID:1b
    public static final byte S2C_PLAYER_LEAVE   = 0x41;
    //type ID:1b, FROM:1b, X:4b, Y:4b, Z:4b
    public static final byte S2C_PLAYER_MOVE    = 0x4D;

    public static final int TCP_PORT = 4444 ;

    @SuppressWarnings("CanBeFinal")
    public static boolean RUNNING = true;
    public static final int MAX_CLIENTS = 2;

}
