package server.block;

import java.io.DataInputStream;
import java.io.DataOutputStream;
import java.io.IOException;

public class BlockState {
    public enum BlockEnum{
        NONE,
        AIR,
        DIRT,
        GRASS,
        STONE,
        LOG,
        LEAVES,

    }

    public final BlockEnum blockType;

    public BlockState(BlockEnum id) {
        this.blockType = id;
    }

    public int getSlice(){
        switch (blockType){
            case DIRT:
                return 0;
            case STONE:
                return 1;
            case GRASS:
                return 2;
            default:
                System.out.println("Block not found : " + blockType);
                System.exit(-1);
                return -1;
        }
    }

    public final int getSerializedSize(){
        return 4;
    }
    public void serialize(DataOutputStream dos) throws IOException {
        dos.writeInt(blockType.ordinal());
    }

    public static BlockState deserialize(DataInputStream dis) throws IOException {
        return new BlockState(
                BlockEnum.values()[dis.readInt()]
        );
    }
}
