package server.block;

import java.util.ArrayList;

public class Chunk {
    public static final int CHUNK_SIZE = 16;
    public static final int CHUNK_SIZE_SQUARED = 256;
    public static final int CHUNK_SIZE_CUBED = 4096;


    private final byte[][][] blocks = new byte[CHUNK_SIZE][CHUNK_SIZE][CHUNK_SIZE];
    public final int chunkX, chunkY, chunkZ;

    //Max size 256
    public final ArrayList<BlockState> dictionary = new ArrayList<>();
    //16x(16x16)

    public Chunk(int x , int y, int z) {
        chunkX = x * CHUNK_SIZE;
        chunkY = y * CHUNK_SIZE;
        chunkZ = z * CHUNK_SIZE;
        dictionary.add(new BlockState(BlockState.BlockEnum.AIR));
        dictionary.add(new BlockState(BlockState.BlockEnum.STONE));
        dictionary.add(new BlockState(BlockState.BlockEnum.DIRT));

        for (int i = 0; i < 6; i++) {
            for (int j = 0; j < 5; j++) {
                if(i == 2 && j == 2 || i == 3 && j == 2)
                    blocks[i][0][j] = 2;
                else
                    blocks[i][0][j] = 1;
            }
        }
    }


    public BlockState getBlock(int x, int y, int z) {
        if(x < 0 || x >= 16 || y < 0 || y >= 16 || z < 0 || z >= 16) {
            return dictionary.getFirst();
        }
        return dictionary.get(blocks[x][y][z]);
    }

    public boolean isAir(int x, int y, int z) {
        return getBlock(x, y, z).blockType == BlockState.BlockEnum.AIR;
    }

}