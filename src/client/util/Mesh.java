package client.util;

import client.models.Loader;
import client.models.VAO;
import org.lwjgl.util.vector.Vector3f;
import server.block.BlockState;
import server.block.Chunk;
import server.Map;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.Iterator;
import java.util.List;

import static server.block.Chunk.CHUNK_SIZE;

enum FaceType{
    TOP,
    BOTTOM,
    LEFT,
    RIGHT,
    FRONT,
    BACK
}
class Face{
    public final float x, y, z;
    public final int slice;
    public final FaceType faceType;

    public int width, height;

    public Face(int x, int y, int z, int width, int height,int slice, FaceType direction) {
        this.x = x;
        this.y = y;
        this.z = z;
        this.width = width;
        this.height = height;
        this.slice = slice;
        this.faceType = direction;
    }

}

public class Mesh {
    private static final List<Face> faces = new ArrayList<>();
    private static byte[][][] visited = new byte[16][16][16];
    private static final HashMap<Vector3f,Byte> visitedOutside = new HashMap<>();
    private static int chunkX,chunkY,chunkZ;
    private static float[] vertices;
    private static float[] textureCoords;
    private static int[] indices;


    public static VAO genGreedyMesh() {
        VAO vao = Loader.createTempVAO(vertices,textureCoords,indices);
        vertices = null;
        textureCoords = null;
        indices = null;
        System.gc();
        return vao;
    }
    public static void genGreedyMeshFromMap(Map map) {
        faces.clear();

        for (Chunk c : map.loadedChunks) {
            chunkX = c.chunkX;
            chunkY = c.chunkY;
            chunkZ = c.chunkZ;

            visited = new byte[16][16][16];

            Iterator<java.util.Map.Entry<Vector3f,Byte>> iterator = visitedOutside.entrySet().iterator();

            while (iterator.hasNext()){
                java.util.Map.Entry<Vector3f, Byte> entry = iterator.next();
                Vector3f k = entry.getKey();
                if(k.x >= chunkX*CHUNK_SIZE && k.x < (chunkX+1)*CHUNK_SIZE && k.y >= chunkY*CHUNK_SIZE && k.y < (chunkY+1)*CHUNK_SIZE && k.z >= chunkZ*CHUNK_SIZE && k.z < (chunkZ+1)*CHUNK_SIZE) {
                    visited[(int) (k.x - chunkX*CHUNK_SIZE)][(int) (k.y - chunkY*CHUNK_SIZE)][(int) (k.z - chunkZ*CHUNK_SIZE)] = entry.getValue();
                    iterator.remove();
                }

            }


            for (int x = c.chunkX*CHUNK_SIZE; x < (c.chunkX+1)*CHUNK_SIZE; x++) {
                for (int y = c.chunkY*CHUNK_SIZE; y < (c.chunkY+1)*CHUNK_SIZE; y++) {
                    for (int z = c.chunkZ*CHUNK_SIZE; z < (c.chunkZ+1)*CHUNK_SIZE; z++) {
                        if (tryStart(map, FaceType.RIGHT, x, y, z)) {
                            xAxisSearch(map, x, y, z, FaceType.RIGHT);
                        }
                        if (tryStart(map, FaceType.LEFT, x, y, z)) {
                            xAxisSearch(map, x, y, z, FaceType.LEFT);
                        }
                        if (tryStart(map, FaceType.TOP, x, y, z)) {
                            yAxisSearch(map, x, y, z, FaceType.TOP);
                        }
                        if (tryStart(map, FaceType.BOTTOM, x, y, z)) {
                            yAxisSearch(map, x, y, z, FaceType.BOTTOM);
                        }
                        if (tryStart(map, FaceType.FRONT, x, y, z)) {
                            zAxisSearch(map, x, y, z, FaceType.FRONT);
                        }
                        if (tryStart(map, FaceType.BACK, x, y, z)) {
                            zAxisSearch(map, x, y, z, FaceType.BACK);
                        }
                    }
                }
            }
        }

        System.out.println(visitedOutside.size());

        visited = null;
        vertices = new float[faces.size() * 12];
        textureCoords = new float[faces.size() * 12];
        indices = new int[faces.size() * 6];

        int v = 0;
        int t = 0;
        int i = 0;
        for (Face f : faces) {
            switch(f.faceType ) {
                case FaceType.TOP:{
                    vertices[v] = f.x;
                    vertices[v + 1] = f.y + 1;
                    vertices[v + 2] = f.z + f.height;
                    vertices[v + 3] = f.x;
                    vertices[v + 4] = f.y + 1;
                    vertices[v + 5] = f.z;
                    vertices[v + 6] = f.x + f.width;
                    vertices[v + 7] = f.y + 1;
                    vertices[v + 8] = f.z;
                    vertices[v + 9] = f.x + f.width;
                    vertices[v + 10] = f.y + 1;
                    vertices[v + 11] = f.z + f.height;

                    addUV(textureCoords,f,t,f.slice);

                    indices[i] = (v / 3) + 1;
                    indices[i + 1] = (v / 3);
                    indices[i + 2] = (v / 3) + 3;
                    indices[i + 3] = (v / 3) + 1;
                    indices[i + 4] = (v / 3) + 3;
                    indices[i + 5] = (v / 3) + 2;
                    break;
            }
                case FaceType.BOTTOM:{
                    vertices[v] = f.x;
                    vertices[v + 1] = f.y;
                    vertices[v + 2] = f.z;
                    vertices[v + 3] = f.x;
                    vertices[v + 4] = f.y;
                    vertices[v + 5] = f.z + f.height;
                    vertices[v + 6] = f.x + f.width;
                    vertices[v + 7] = f.y;
                    vertices[v + 8] = f.z + f.height;
                    vertices[v + 9] = f.x + f.width;
                    vertices[v + 10] = f.y;
                    vertices[v + 11] = f.z;

                    addUV(textureCoords,f,t,f.slice);

                    indices[i] = (v / 3) + 1;
                    indices[i + 1] = (v / 3);
                    indices[i + 2] = (v / 3) + 3;
                    indices[i + 3] = (v / 3) + 1;
                    indices[i + 4] = (v / 3) + 3;
                    indices[i + 5] = (v / 3) + 2;
                    break;
                }
                case FaceType.RIGHT:{
                    vertices[v] = f.x + 1;
                    vertices[v + 1] = f.y + f.height;
                    vertices[v + 2] = f.z;
                    vertices[v + 3] = f.x + 1;
                    vertices[v + 4] = f.y;
                    vertices[v + 5] = f.z;
                    vertices[v + 6] = f.x + 1;
                    vertices[v + 7] = f.y;
                    vertices[v + 8] = f.z + f.width;
                    vertices[v + 9] = f.x + 1;
                    vertices[v + 10] = f.y + f.height;
                    vertices[v + 11] = f.z + f.width;

                    addUV(textureCoords,f,t,f.slice);

                    indices[i] = (v / 3) + 1;
                    indices[i + 1] = (v / 3);
                    indices[i + 2] = (v / 3) + 3;
                    indices[i + 3] = (v / 3) + 1;
                    indices[i + 4] = (v / 3) + 3;
                    indices[i + 5] = (v / 3) + 2;
                    break;
                }
                case FaceType.LEFT:{
                    vertices[v] = f.x;
                    vertices[v + 1] = f.y;
                    vertices[v + 2] = f.z;
                    vertices[v + 3] = f.x;
                    vertices[v + 4] = f.y + f.height;
                    vertices[v + 5] = f.z;
                    vertices[v + 6] = f.x;
                    vertices[v + 7] = f.y + f.height;
                    vertices[v + 8] = f.z + f.width;
                    vertices[v + 9] = f.x;
                    vertices[v + 10] = f.y;
                    vertices[v + 11] = f.z + f.width;

                    addUV(textureCoords,f,t,f.slice);

                    indices[i] = (v / 3) + 1;
                    indices[i + 1] = (v / 3);
                    indices[i + 2] = (v / 3) + 3;
                    indices[i + 3] = (v / 3) + 1;
                    indices[i + 4] = (v / 3) + 3;
                    indices[i + 5] = (v / 3) + 2;
                    break;
                }
                case FaceType.FRONT:{
                    vertices[v] = f.x;
                    vertices[v + 1] = f.y + f.height;
                    vertices[v + 2] = f.z + 1;
                    vertices[v + 3] = f.x;
                    vertices[v + 4] = f.y;
                    vertices[v + 5] = f.z + 1;
                    vertices[v + 6] = f.x + f.width;
                    vertices[v + 7] = f.y;
                    vertices[v + 8] = f.z + 1;
                    vertices[v + 9] = f.x + f.width;
                    vertices[v + 10] = f.y + f.height;
                    vertices[v + 11] = f.z + 1;

                    addUV(textureCoords,f,t,f.slice);

                    indices[i] = (v / 3) + 1;
                    indices[i + 1] = (v / 3) + 3;
                    indices[i + 2] = (v / 3);
                    indices[i + 3] = (v / 3) + 1;
                    indices[i + 4] = (v / 3) + 2;
                    indices[i + 5] = (v / 3) + 3;
                    break;
                }
                case FaceType.BACK:{
                    vertices[v] = f.x;
                    vertices[v + 1] = f.y;
                    vertices[v + 2] = f.z;
                    vertices[v + 3] = f.x;
                    vertices[v + 4] = f.y + f.height;
                    vertices[v + 5] = f.z;
                    vertices[v + 6] = f.x + f.width;
                    vertices[v + 7] = f.y + f.height;
                    vertices[v + 8] = f.z;
                    vertices[v + 9] = f.x + f.width;
                    vertices[v + 10] = f.y;
                    vertices[v + 11] = f.z;

                    addUV(textureCoords,f,t,f.slice);

                    indices[i] = (v / 3) + 1;
                    indices[i + 1] = (v / 3) + 3;
                    indices[i + 2] = (v / 3);
                    indices[i + 3] = (v / 3) + 1;
                    indices[i + 4] = (v / 3) + 2;
                    indices[i + 5] = (v / 3) + 3;
                    break;
                }

            }
            v+= 12;
            t+= 12;
            i += 6;
        }
        faces.clear();
    }

    private static void yAxisSearch(Map chunk, int start_x, int start_y, int start_z, FaceType faceType) {
        int dir = faceType == FaceType.TOP ? 1 : -1;
        BlockState block = chunk.getBlock(start_x, start_y, start_z);
        if (block.blockType != BlockState.BlockEnum.AIR && chunk.isAir(start_x, start_y + dir, start_z)) {
            visit( faceType, start_x, start_y, start_z);
            Face f = new Face(start_x, start_y, start_z, 1, 1,block.getSlice(), faceType);

            for (int x = 1; x <= f.width; x++) {
                if (tryVisit(chunk,faceType, start_x + x, start_y, start_z,block.blockType) && chunk.isAir(start_x + x, start_y + dir, start_z)) {
                    visit(faceType,start_x + x, start_y, start_z);
                    f.width++;
                } else {
                    break;
                }
            }
            for (int z = 1; z <= f.height; z++) {
                boolean b = true;
                for (int x = 0; x < f.width; x++) {
                    if (!tryVisit(chunk,faceType,start_x + x, start_y, start_z + z,block.blockType) || !chunk.isAir(start_x + x, start_y + dir, start_z + z)) {
                        b = false;
                        break;

                    }
                }
                if (b) {
                    for (int x = 0; x < f.width; x++) {
                        visit( faceType, start_x + x, start_y, start_z + z);
                    }
                    f.height++;
                } else break;
            }

            faces.add(f);

        }

    }
    private static void xAxisSearch(Map chunk, int start_x, int start_y, int start_z, FaceType faceType) {
        int dir = faceType == FaceType.RIGHT ? 1 : -1;
        BlockState block = chunk.getBlock(start_x, start_y, start_z);

        if (block.blockType != BlockState.BlockEnum.AIR && chunk.isAir(start_x + dir, start_y, start_z)) {
            visit( faceType, start_x, start_y, start_z);
            Face f = new Face(start_x, start_y, start_z, 1, 1,block.getSlice(), faceType);

            for (int y = 1; y <= f.height; y++) {
                if (tryVisit(chunk,faceType, start_x, start_y + y, start_z,block.blockType) && chunk.isAir(start_x + dir, start_y + y, start_z)) {
                    visit(faceType,start_x, start_y + y, start_z);
                    f.height++;
                } else {
                    break;
                }
            }
            for (int z = 1; z <= f.width; z++) {
                boolean b = true;
                for (int y = 0; y < f.height; y++) {
                    if (!tryVisit(chunk,faceType,start_x, start_y + y, start_z + z,block.blockType) || !chunk.isAir(start_x + dir, start_y + y, start_z + z)) {
                        b = false;
                        break;

                    }
                }
                if (b) {
                    for (int y = 0; y < f.height; y++) {
                        visit( faceType, start_x, start_y + y, start_z + z);
                    }
                    f.width++;
                } else break;
            }

            faces.add(f);

        }
    }
    private static void zAxisSearch(Map chunk, int start_x, int start_y, int start_z, FaceType faceType) {
        int dir = faceType == FaceType.FRONT ? 1 : -1;
        BlockState block = chunk.getBlock(start_x, start_y, start_z);
        if (block.blockType != BlockState.BlockEnum.AIR && chunk.isAir(start_x, start_y, start_z + dir)) {
            visit( faceType, start_x, start_y, start_z);
            Face f = new Face(start_x, start_y, start_z, 1, 1, block.getSlice(), faceType);

            for (int x = 1; x <= f.width; x++) {
                if (tryVisit(chunk,faceType, start_x + x, start_y, start_z,block.blockType) && chunk.isAir(start_x + x, start_y, start_z + dir)) {
                    visit(faceType,start_x + x, start_y, start_z);
                    f.width++;
                } else {
                    break;
                }
            }
            for (int y = 1; y <= f.height; y++) {
                boolean b = true;
                for (int x = 0; x < f.width; x++) {
                    if (!tryVisit(chunk,faceType,start_x + x, start_y + y, start_z,block.blockType) || !chunk.isAir(start_x + x, start_y + y, start_z + dir)) {
                        b = false;
                        break;

                    }
                }
                if (b) {
                    for (int x = 0; x < f.width; x++) {
                        visit( faceType, start_x + x, start_y + y, start_z);
                    }
                    f.height++;
                } else break;
            }

            faces.add(f);

        }
    }

    static void addUV(float[] textureCoords, Face f, int t, int slice) {
        textureCoords[t] = f.width;
        textureCoords[t + 1] = f.height;
        textureCoords[t + 2] = slice;

        textureCoords[t + 3] = f.width;
        textureCoords[t + 4] = 0;
        textureCoords[t + 5] = slice;

        textureCoords[t + 6] = 0;
        textureCoords[t + 7] = 0;
        textureCoords[t + 8] = slice;

        textureCoords[t + 9] = 0;
        textureCoords[t + 10] = f.height;
        textureCoords[t + 11] = slice;
    }
    static void visit(FaceType axis, int x, int y, int z) {
        x -= chunkX*CHUNK_SIZE;
        y -= chunkY*CHUNK_SIZE;
        z -= chunkZ*CHUNK_SIZE;
        if(x < 0 || x >= 16 || y < 0 || y >= 16 || z < 0 || z >= 16) {
            Vector3f pos = new Vector3f(x + chunkX*CHUNK_SIZE,y+chunkY*CHUNK_SIZE,z+chunkZ*CHUNK_SIZE);
            if(visitedOutside.containsKey(pos)) {
                visitedOutside.put(pos, (byte) (visitedOutside.get(pos) | (1 << axis.ordinal())));
            } else {
                visitedOutside.put(pos, (byte) (1 << axis.ordinal()));
            }
            return;
        }
        visited[x][y][z] = (byte) (visited[x][y][z] | (1 << axis.ordinal()));
    }
    static boolean hasNotBeenVisited(FaceType axis, int x, int y, int z) {
        x -= chunkX*CHUNK_SIZE;
        y -= chunkY*CHUNK_SIZE;
        z -= chunkZ*CHUNK_SIZE;
        if(x < 0 || x >= 16 || y < 0 || y >= 16 || z < 0 || z >= 16) {
            Vector3f pos = new Vector3f(x + chunkX*CHUNK_SIZE,y+chunkY*CHUNK_SIZE,z+chunkZ*CHUNK_SIZE);
            return !visitedOutside.containsKey(pos) || (visitedOutside.get(pos) & (1 << axis.ordinal())) == 0;
        }
        return (visited[x][y][z] & (1 << axis.ordinal())) == 0;
    }
    static boolean tryVisit(Map c, FaceType axis, int x, int y, int z, BlockState.BlockEnum blockType) {
        return c.getBlock(x, y, z).blockType == blockType && hasNotBeenVisited(axis, x, y, z);
    }
    static boolean tryStart(Map c, FaceType axis, int x, int y, int z) {
        return !c.isAir(x,y,z) && hasNotBeenVisited(axis, x, y, z);
    }

//    public static VAO genCubeMesh() {
//        float[] vertices = {
//                0,1,0,
//                0,0,0,
//                1,0,0,
//                1,1,0,
//
//                0,1,1,
//                0,0,1,
//                1,0,1,
//                1,1,1,
//
//                1,1,0,
//                1,0,0,
//                1,0,1,
//                1,1,1,
//
//                0,1,0,
//                0,0,0,
//                0,0,1,
//                0,1,1,
//
//                0,1,1,
//                0,1,0,
//                1,1,0,
//                1,1,1,
//
//                0,0,1,
//                0,0,0,
//                1,0,0,
//                1,0,1
//
//        };
//
//        float t = 0.5f;
//        float[] textureCoords = {
//                t,t,0,
//                t,1,0,
//                1,1,0,
//                1,t,0,
//                t,t,0,
//                t,1,0,
//                1,1,0,
//                1,t,0,
//                t,t,0,
//                t,1,0,
//                1,1,0,
//                1,t,0,
//                t,t,0,
//                t,1,0,
//                1,1,0,
//                1,t,0,
//                t,t,0,
//                t,1,0,
//                1,1,0,
//                1,t,0,
//                t,t,0,
//                t,1,0,
//                1,1,0,
//                1,t,0
//        };
//
//        int[] indices = {
//                //Back
//                1,0,3,
//                1,3,2,
//
//                //FRONT
//                4,5,7,
//                7,5,6,
//
//                //EAST
//                9,8,11,
//                9,11,10,
//
//                //WEST
//                12,13,15,
//                15,13,14,
//
//                //NORTH
//                17,16,19,
//                17,19,18,
//
//                //SOUTH
//                20,21,23,
//                23,21,22
//
//        };
//        return Loader.createVAO(vertices,textureCoords,indices);
//    }
}
