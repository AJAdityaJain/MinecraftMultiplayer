package engine.util;

import engine.models.Loader;
import engine.models.VAO;
import server.block.BlockState;
import server.block.Chunk;

import java.util.ArrayList;
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
    final List<Face> faces;

    public Mesh() {
        this.faces = new ArrayList<>();
    }

    public VAO genGreedyMeshFromChunk(Chunk c) {
        byte[][][] visited = new byte[16][16][16];

        for (int x = 0; x < CHUNK_SIZE; x++) {
            for (int y = 0; y < CHUNK_SIZE; y++) {
                for (int z = 0; z < CHUNK_SIZE; z++) {
                    if (tryStart(c,visited,FaceType.RIGHT,x , y, z)) {
                        xAxisSearch(c, visited, x, y, z, FaceType.RIGHT);
                    }
                    if (tryStart(c,visited,FaceType.LEFT,x , y, z)) {
                        xAxisSearch(c, visited, x, y, z, FaceType.LEFT);
                    }
                    if (tryStart(c,visited,FaceType.TOP,x , y, z)) {
                        yAxisSearch(c, visited, x, y, z, FaceType.TOP);
                    }
                    if (tryStart(c,visited,FaceType.BOTTOM,x , y, z)) {
                        yAxisSearch(c, visited, x, y, z, FaceType.BOTTOM);
                    }
                    if (tryStart(c,visited,FaceType.FRONT,x , y, z)) {
                        zAxisSearch(c, visited, x, y, z, FaceType.FRONT);
                    }
                    if (tryStart(c,visited,FaceType.BACK,x , y, z)) {
                        zAxisSearch(c, visited, x, y, z, FaceType.BACK);
                    }
                }
            }
        }
        float[] vertices = new float[faces.size() * 12];
        float[] textureCoords = new float[faces.size() * 12];
        int[] indices = new int[faces.size() * 6];

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

            System.out.println(f.x + " " + f.y + " " + f.z + " " + f.width + " " + f.height + " " + f.faceType.name());
            v+= 12;
            t+= 12;
            i += 6;
        }
        return Loader.createVAO(vertices,textureCoords,indices);
    }

    private void yAxisSearch(Chunk chunk, byte[][][] visited, int start_x, int start_y, int start_z, FaceType faceType) {
        int dir = faceType == FaceType.TOP ? 1 : -1;
        BlockState block = chunk.getBlock(start_x, start_y, start_z);
        if (block.blockType != BlockState.BlockEnum.AIR && chunk.isAir(start_x, start_y + dir, start_z)) {
            visit(visited, faceType, start_x, start_y, start_z);
            Face f = new Face(start_x, start_y, start_z, 1, 1,block.getSlice(), faceType);

            for (int x = 1; x <= f.width; x++) {
                if (tryVisit(chunk,visited,faceType, start_x + x, start_y, start_z,block.blockType) && chunk.isAir(start_x + x, start_y + dir, start_z)) {
                    visit(visited,faceType,start_x + x, start_y, start_z);
                    f.width++;
                } else {
                    break;
                }
            }
            for (int z = 1; z <= f.height; z++) {
                boolean b = true;
                for (int x = 0; x < f.width; x++) {
                    if (!tryVisit(chunk,visited,faceType,start_x + x, start_y, start_z + z,block.blockType) || !chunk.isAir(start_x + x, start_y + dir, start_z + z)) {
                        b = false;
                        break;

                    }
                }
                if (b) {
                    for (int x = 0; x < f.width; x++) {
                        visit(visited, faceType, start_x + x, start_y, start_z + z);
                    }
                    f.height++;
                } else break;
            }

            faces.add(f);

        }

    }
    private void xAxisSearch(Chunk chunk, byte[][][] visited, int start_x, int start_y, int start_z, FaceType faceType) {
        int dir = faceType == FaceType.RIGHT ? 1 : -1;
        BlockState block = chunk.getBlock(start_x, start_y, start_z);

        if (block.blockType != BlockState.BlockEnum.AIR && chunk.isAir(start_x + dir, start_y, start_z)) {
            visit(visited, faceType, start_x, start_y, start_z);
            Face f = new Face(start_x, start_y, start_z, 1, 1,block.getSlice(), faceType);

            for (int y = 1; y <= f.height; y++) {
                if (tryVisit(chunk,visited,faceType, start_x, start_y + y, start_z,block.blockType) && chunk.isAir(start_x + dir, start_y + y, start_z)) {
                    visit(visited,faceType,start_x, start_y + y, start_z);
                    f.height++;
                } else {
                    break;
                }
            }
            for (int z = 1; z <= f.width; z++) {
                boolean b = true;
                for (int y = 0; y < f.height; y++) {
                    if (!tryVisit(chunk,visited,faceType,start_x, start_y + y, start_z + z,block.blockType) || !chunk.isAir(start_x + dir, start_y + y, start_z + z)) {
                        b = false;
                        break;

                    }
                }
                if (b) {
                    for (int y = 0; y < f.height; y++) {
                        visit(visited, faceType, start_x, start_y + y, start_z + z);
                    }
                    f.width++;
                } else break;
            }

            faces.add(f);

        }
    }
    private void zAxisSearch(Chunk chunk, byte[][][] visited, int start_x, int start_y, int start_z, FaceType faceType) {
        int dir = faceType == FaceType.FRONT ? 1 : -1;
        BlockState block = chunk.getBlock(start_x, start_y, start_z);
        if (block.blockType != BlockState.BlockEnum.AIR && chunk.isAir(start_x, start_y, start_z + dir)) {
            visit(visited, faceType, start_x, start_y, start_z);
            Face f = new Face(start_x, start_y, start_z, 1, 1, block.getSlice(), faceType);

            for (int x = 1; x <= f.width; x++) {
                if (tryVisit(chunk,visited,faceType, start_x + x, start_y, start_z,block.blockType) && chunk.isAir(start_x + x, start_y, start_z + dir)) {
                    visit(visited,faceType,start_x + x, start_y, start_z);
                    f.width++;
                } else {
                    break;
                }
            }
            for (int y = 1; y <= f.height; y++) {
                boolean b = true;
                for (int x = 0; x < f.width; x++) {
                    if (!tryVisit(chunk,visited,faceType,start_x + x, start_y + y, start_z,block.blockType) || !chunk.isAir(start_x + x, start_y + y, start_z + dir)) {
                        b = false;
                        break;

                    }
                }
                if (b) {
                    for (int x = 0; x < f.width; x++) {
                        visit(visited, faceType, start_x + x, start_y + y, start_z);
                    }
                    f.height++;
                } else break;
            }

            faces.add(f);

        }
    }

    private void addUV(float[] textureCoords, Face f, int t, int slice) {
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
    private void visit(byte[][][] visited,FaceType axis, int x, int y, int z) {
        visited[x][y][z] = (byte) (visited[x][y][z] | (1 << axis.ordinal()));
    }
    private boolean hasNotBeenVisited(byte[][][] visited, FaceType axis, int x, int y, int z) {
        return (visited[x][y][z] & (1 << axis.ordinal())) == 0;
    }
    private boolean tryVisit(Chunk c, byte[][][] visited, FaceType axis, int x, int y, int z, BlockState.BlockEnum blockType) {
        return c.getBlock(x, y, z).blockType == blockType && hasNotBeenVisited(visited, axis, x, y, z);
    }
    private boolean tryStart(Chunk c, byte[][][] visited, FaceType axis, int x, int y, int z) {
        return !c.isAir(x,y,z) && hasNotBeenVisited(visited, axis, x, y, z);
    }
}
