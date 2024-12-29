package server;

import network.Logger;
import network.TCPClient;
import org.lwjgl.util.vector.Vector3f;
import server.block.BlockState;
import server.block.Chunk;

import java.util.concurrent.CopyOnWriteArrayList;

import static client.rendering.DisplayManager.*;

public class Map {
    public final CopyOnWriteArrayList<Vector3f>  loadingChunks = new CopyOnWriteArrayList<>();
    public final CopyOnWriteArrayList<Chunk> loadedChunks = new CopyOnWriteArrayList<>();
    private Chunk cached;

    public Map() {}

    public void addChunk(Chunk chunk){
        loadedChunks.add(chunk);
        cached = chunk;
    }

    public void setBlock(int x, int y, int z, BlockState state){
        int X = x / 16;
        int Y = y / 16;
        int Z = z / 16;
        x = x % 16;
        y = y % 16;
        z = z % 16;
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
        int X = x / 16;
        int Y = y / 16;
        int Z = z / 16;

        x = x % 16;
        y = y % 16;
        z = z % 16;
//        if(cached.chunkX == X && cached.chunkY == Y && cached.chunkZ == Z){
//            return cached.getBlock(x, y, z);
//        }

        for(Chunk c : loadedChunks){
            if(c.chunkX == X && c.chunkY == Y && c.chunkZ == Z){
                int sz = c.size;
                boolean p = true;
                if(sz > 8)         System.out.println("AAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAaaHUH " + sz);
                if(x >= sz) {
                    x -= sz;
                    X += 1;
                    p = false;
                }
                if(y >= sz) {
                    y -= sz;
                    Y += 1;
                    p = false;
                }
                if(z >= sz) {
                    z -= sz;
                    Z += 1;
                    p = false;
                }

                if(x < 0 ) {
                    X -= 1;
                    p = false;
                }
                if(y < 0){
                    Y -= 1;
                    p = false;
                }
                if(z < 0){
                    Z -= 1;
                    p = false;
                }

                if(p)return c.getBlock(x, y, z);
                else {
                    for(Chunk c1 : loadedChunks){
                        if(c1.chunkX == X && c1.chunkY == Y && c1.chunkZ == Z) {
                            if(x < 0){
                                x += c1.size;
                            }
                            if(y < 0){
                                y += c1.size;
                            }
                            if( z < 0){
                                z += c1.size;
                            }
                            return c1.getBlock(x, y, z);
                        }
                    }
                    break;
                }
            }
        }
//        System.out.println(x + "[]" + y + "{]" + z);
        return new BlockState(BlockState.BlockEnum.STONE);
    }

    public boolean isAir(int x, int y, int z){
        return getBlock(x, y, z).blockType == BlockState.BlockEnum.AIR;
    }

    public boolean tryUnload(int px, int py, int pz) {
        boolean ret = false;
        for (Chunk c : loadedChunks) {
            if ((c.chunkX - px) * (c.chunkX - px) + (c.chunkY - py) * (c.chunkY - py) + (c.chunkZ - pz) * (c.chunkZ - pz) > RENDER_DISTANCE_SQ) {
                ret = true;
                loadedChunks.remove(c);
                cached = loadedChunks.getFirst();
            }
        }
        return ret;
    }

    public void tryLoad(int px, int py, int pz, TCPClient tcp_client) {
        for (int i = -RENDER_DISTANCE; i < RENDER_DISTANCE+1; i ++)
            for (int j = -RENDER_HEIGHT; j < RENDER_HEIGHT+1; j ++)
                k:for (int k = -RENDER_DISTANCE; k < RENDER_DISTANCE+1; k ++){
                    int d = i * i + j * j + k * k;
                    if(d <= RENDER_DISTANCE_SQ && i+px >=0 && j + py >= 0 && k + pz >= 0){
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
                        tcp_client.requestChunk(i+px, j + py, k+pz);//Math.floor((4.99f*d)/RENDER_DISTANCE_SQ));
                        System.out.println("Requested chunk: " + (i+px) + " " + (j + py) + " " + (k+pz));
                        loadingChunks.add(new Vector3f(i+px, j + py, k+pz));
                    }
                }
    }
}
