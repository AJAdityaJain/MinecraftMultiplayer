package server;

import server.block.BlockState;
import server.block.Chunk;

import java.io.IOException;
import java.util.concurrent.CopyOnWriteArrayList;

import static network.NetworkConstants.S2C_CHUNK_SEND;

public class MapWriter {
    public final CopyOnWriteArrayList<Chunk> loadedChunks = new CopyOnWriteArrayList<>();

    void loadChunks() {
        // Load the first chunk
        for (int i = 0; i < 16; i++) {
            for (int j = 0; j < 16; j++) {
                loadedChunks.add(new Chunk(i, 0, j,0));
                loadedChunks.getLast().generate();
            }
        }
    }

    void loadChunk(int x, int y, int z) {
        // Load the first chunk
        loadedChunks.add(new Chunk(x,y,z,0));
        loadedChunks.getLast().generate();
    }

    public void setBlock(int x, int y, int z, BlockState state){
        int X = x / 16;
        int Y = y / 16;
        int Z = z / 16;
        x = x % 16;
        y = y % 16;
        z = z % 16;

        for(Chunk c : loadedChunks){
            if(c.chunkX == X && c.chunkY == Y && c.chunkZ == Z){
                c.setBlock(x, y, z, state);
                return;
            }
        }
    }

    public BlockState getBlock(int x, int y, int z){
        int X = x / 16;
        int Y = y / 16;
        int Z = z / 16;
        x = x % 16;
        y = y % 16;
        z = z % 16;

        for(Chunk c : loadedChunks){
            if(c.chunkX == X && c.chunkY == Y && c.chunkZ == Z){
                return c.getBlock(x, y, z);
            }
        }
        return new BlockState(BlockState.BlockEnum.NONE);
    }

    public boolean isAir(int x, int y, int z){
        return getBlock(x, y, z).blockType == BlockState.BlockEnum.AIR;
    }

    public byte[] serializeChunk(int x, int y, int z) throws IOException {
        for(Chunk c : loadedChunks){
            if(c.chunkX == x && c.chunkY == y && c.chunkZ == z){
                return c.serialize(S2C_CHUNK_SEND,1);
            }
        }
        loadChunk(x,y,z);
        return loadedChunks.getLast().serialize(S2C_CHUNK_SEND,1);
    }
}
