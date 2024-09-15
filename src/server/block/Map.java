package server.block;

import java.util.ArrayList;

public class Map {
    public final ArrayList<Chunk> loadedChunks = new ArrayList<>();
    private Chunk cached;

    public Map() {}

    public void addChunk(Chunk chunk){
        loadedChunks.add(chunk);
        cached = chunk;
    }

    public void loadChunks() {
        // Load the first chunk
        for (int i = 0; i < 16; i++) {
            for (int j = 0; j < 16; j++) {
                loadedChunks.add(new Chunk(i, 0, j));
                loadedChunks.getLast().generate();
            }
        }
        cached = loadedChunks.getFirst();
    }

    public BlockState getBlock(int x, int y, int z){
        int X = x / Chunk.CHUNK_SIZE;
        int Y = y / Chunk.CHUNK_SIZE;
        int Z = z / Chunk.CHUNK_SIZE;
        x = x % Chunk.CHUNK_SIZE;
        y = y % Chunk.CHUNK_SIZE;
        z = z % Chunk.CHUNK_SIZE;
        if(cached.chunkX == X && cached.chunkY == Y && cached.chunkZ == Z){
            return cached.getBlock(x, y, z);
        }

        for(Chunk c : loadedChunks){
            if(c.chunkX == X && c.chunkY == Y && c.chunkZ == Z){
                cached = c;
                return c.getBlock(x, y, z);
            }
        }

        System.out.print("|");
        return new BlockState(BlockState.BlockEnum.NONE);
    }

    public boolean isAir(int x, int y, int z){
        return getBlock(x, y, z).blockType == BlockState.BlockEnum.AIR;
    }

}
