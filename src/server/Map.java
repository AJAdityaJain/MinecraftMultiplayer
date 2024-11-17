package server;

import network.TCPClient;
import org.lwjgl.util.vector.Vector3f;
import server.block.BlockState;
import server.block.Chunk;

import java.io.IOException;
import java.util.concurrent.CopyOnWriteArrayList;

import static client.rendering.DisplayManager.*;
import static network.NetworkConstants.S2C_CHUNK_SEND;

public class Map {
    public final CopyOnWriteArrayList<Vector3f> loadingChunks = new CopyOnWriteArrayList<>();
    public final CopyOnWriteArrayList<Chunk> loadedChunks = new CopyOnWriteArrayList<>();
    private Chunk cached;

    public Map() {}

    public void addChunk(Chunk chunk){
        loadedChunks.add(chunk);
        cached = chunk;
    }

    void loadChunks() {
        // Load the first chunk
        for (int i = 0; i < 16; i++) {
            for (int j = 0; j < 16; j++) {
                loadedChunks.add(new Chunk(i, 0, j));
                loadedChunks.getLast().generate();
            }
        }
        cached = loadedChunks.getFirst();
    }

    void loadChunk(int x, int y, int z) {
        // Load the first chunk
        loadedChunks.add(new Chunk(x,y,z));
        loadedChunks.getLast().generate();
        cached = loadedChunks.getLast();
    }

    public void setBlock(int x, int y, int z, BlockState state){
        int X = x / Chunk.CHUNK_SIZE;
        int Y = y / Chunk.CHUNK_SIZE;
        int Z = z / Chunk.CHUNK_SIZE;
        x = x % Chunk.CHUNK_SIZE;
        y = y % Chunk.CHUNK_SIZE;
        z = z % Chunk.CHUNK_SIZE;
        if(cached.chunkX == X && cached.chunkY == Y && cached.chunkZ == Z){
            cached.setBlock(x, y, z, state);
            return;
        }

        for(Chunk c : loadedChunks){
            if(c.chunkX == X && c.chunkY == Y && c.chunkZ == Z){
                c.setBlock(x, y, z, state);
                return;
            }
        }
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
        return new BlockState(BlockState.BlockEnum.NONE);
    }

    public boolean isAir(int x, int y, int z){
        return getBlock(x, y, z).blockType == BlockState.BlockEnum.AIR;
    }

    public byte[] serializeChunk(int x, int y, int z) throws IOException {
        if(cached.chunkX == x && cached.chunkY == y && cached.chunkZ == z){
            return cached.serialize(S2C_CHUNK_SEND);
        }
        for(Chunk c : loadedChunks){
            if(c.chunkX == x && c.chunkY == y && c.chunkZ == z){
                return c.serialize(S2C_CHUNK_SEND);
            }
        }
        loadChunk(x,y,z);
        return cached.serialize(S2C_CHUNK_SEND);
    }

    public boolean tryUnload(int px, int py, int pz) {
        boolean ret = false;
        for (Chunk c : loadedChunks) {
            if ((c.chunkX - px) * (c.chunkX - px) + (c.chunkY - py) * (c.chunkY - py) + (c.chunkZ - pz) * (c.chunkZ - pz) > RENDER_DISTANCE_SQ) {
                ret = true;
                loadedChunks.remove(c);
            }
        }
        return ret;
    }

    public void tryLoad(int px, int py, int pz, TCPClient tcp_client) {
        for (int i = -RENDER_DISTANCE; i < RENDER_DISTANCE+1; i ++)
            for (int j = -RENDER_HEIGHT; j < RENDER_HEIGHT+1; j ++)
                k:for (int k = -RENDER_DISTANCE; k < RENDER_DISTANCE+1; k ++){
                    if(i * i + k * k <= RENDER_DISTANCE_SQ && i+px >=0 && j + py >= 0 && k + pz >= 0){
                        for (Vector3f v: loadingChunks) {
                            if (v.x == i + px && v.y == j + py && v.z == k + pz) {
                                continue k;
                            }
                        }
                        for (Chunk c : loadedChunks) {
                            if (c.chunkX == i + px && c.chunkY == j + py && c.chunkZ == k + pz) {
                                continue k;
                            }
                        }
                        tcp_client.requestChunk(i+px, j + py, k+pz);
                        loadingChunks.add(new Vector3f(i+px, j + py, k+pz));
                    }
                }
    }
}
