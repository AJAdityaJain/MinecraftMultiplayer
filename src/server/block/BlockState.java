package server.block;

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

    public BlockEnum blockType;

    public BlockState(BlockEnum id) {
        this.blockType = id;
    }
}
