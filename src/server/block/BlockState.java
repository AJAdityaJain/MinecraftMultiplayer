package server.block;

import java.io.DataInputStream;
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
            default:
                System.out.println("Block not found");
                System.exit(-1);
                return -1;
        }
    }

    public int serialize() {
        return blockType.ordinal();
    }

    public static BlockState deserialize(DataInputStream dis) throws IOException {
        return new BlockState(BlockEnum.values()[dis.readInt()]);
    }
}
