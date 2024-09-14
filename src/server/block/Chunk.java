package server.block;

import java.util.ArrayList;
import java.util.Random;

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
        Random rand = new Random();
        for(int i = 0; i < CHUNK_SIZE; i++) {
            for(int j = 0; j < CHUNK_SIZE; j++) {
                for(int k = 0; k < CHUNK_SIZE; k++) {
                    if(j < 8){
                        blocks[i][j][k] = 1;
                    } else if(j < 9) {
                        blocks[i][j][k] = 2;
                    }  else if(j < 10 &&  rand.nextInt(2) == 0) {
                        blocks[i][j][k] = 2;
                    }
                    else {
                        blocks[i][j][k] = 0;
                    }
                }
            }
        }
    }


    public BlockState getBlock(int x, int y, int z) {
        if(x < 0 || x >= 16 || y < 0 || y >= 16 || z < 0 || z >= 16) {
            return dictionary.get(1);
        }
        return dictionary.get(blocks[x][y][z]);
    }

    public boolean isAir(int x, int y, int z) {
        return getBlock(x, y, z).blockType == BlockState.BlockEnum.AIR;
    }

}