package server.block;


import client.Client;
import network.Logger;
import server.util.FastNoiseLite;

import java.io.*;
import java.util.ArrayList;
import java.util.Random;

public class Chunk {
    private static final FastNoiseLite noise = new FastNoiseLite();


    public final int size;
    private final byte[][][] blocks;
    public final int chunkX, chunkY, chunkZ;

    //Max size 256
    private final ArrayList<BlockState> dictionary = new ArrayList<>();

    public Chunk(int x , int y, int z, int LOD) {
        chunkX = x;
        chunkY = y;
        chunkZ = z;
        size = 16 >> LOD;
        blocks = new byte[size][size][size];
    }

    private int genBlock(int x, int y, int z) {
        if(size == 16){
            double Continentals = Math.pow(1.3,4 + noise.GetNoise (x + size*chunkX,z + size*chunkZ)*4);
            int surface = Math.clamp((int) Continentals, 0, 16);
            if( y == surface) {
                return 3;
            }
            else if(surface-y <3 && surface-y > 0) {
                return 2;
            }
            else if(y < surface) {
                return 1;
            }
            return 0;
        }
        else{
            throw new UnsupportedOperationException("Not implemented");
        }
    }
    public void generate(){
        noise.SetNoiseType(FastNoiseLite.NoiseType.OpenSimplex2);

        dictionary.add(new BlockState(BlockState.BlockEnum.AIR));
        dictionary.add(new BlockState(BlockState.BlockEnum.STONE));
        dictionary.add(new BlockState(BlockState.BlockEnum.DIRT));
        dictionary.add(new BlockState(BlockState.BlockEnum.GRASS));
        new Random();
        for(int i = 0; i < 16; i++) {
            for(int j = 0; j < 16; j++) {
                for(int k = 0; k < 16; k++) {
                    blocks[i][j][k] = (byte) genBlock(i, j, k);
                }
            }
        }

//        for( int i = 0; i < 16; i ++){
//            for(int k = 0; k < 16; k ++){
//                if(0!= (i+k)%3)
//                    blocks[i][0][k] = 1;
//            }
//        }
    }

    //local coordinates
    public void setBlock(int x, int y, int z, BlockState state) {
        if(x < 0 || x >= size || y < 0 || y >= size || z < 0 || z >= size) {
            return;
        }
        if(!dictionary.contains(state)) {
            dictionary.add(state);
        }
        blocks[x][y][z] = (byte) dictionary.indexOf(state);
    }

    //local coordinates
    public BlockState getBlock(int x, int y, int z) {
        if(x < 0 || x >= size || y < 0 || y >= size || z < 0 || z >= size) {
            System.out.println(x+ ", "+ y + ", " + z);
            System.out.println(1/0);
            return new BlockState(BlockState.BlockEnum.NONE);
        }
        return dictionary.get(blocks[x][y][z]);
    }


    public byte[] serialize(byte type, int LOD_target) throws IOException {
        if(size!= 16){
            throw new UnsupportedOperationException("Not implemented");
        }
        ByteArrayOutputStream bos = new ByteArrayOutputStream();
        DataOutputStream dos = new DataOutputStream(bos);

        dos.writeByte(type);
        dos.writeByte(0);
        dos.writeByte(0);

        // Write chunk coordinates
        dos.writeByte(LOD_target);
        dos.writeInt(chunkX);
        dos.writeInt(chunkY);
        dos.writeInt(chunkZ);

        // Write block data (16x16x16 bytes)
        int sz = 16 >> LOD_target;
        for (int i = 0; i < sz; i++) {
            for (int j = 0; j < sz; j++) {
                for (int k = 0; k < sz; k++) {
                    dos.write(blocks[i][j][k]);
                }
            }
        }

        // Write dictionary size and each BlockState
        dos.writeInt(dictionary.size());
        for (BlockState state : dictionary) {
            // Assuming BlockState has its own serialize method
            state.serialize(dos);
        }

        dos.flush();
        byte[] data = bos.toByteArray();
        data[2] = (byte) ((data.length-3) & 0xFF);//first byte of the length
        data[1] = (byte) (((data.length-3) >> 8) & 0xFF);//second byte of the length
        return data;
    }

    public static Chunk deserialize(byte[] data) {
        try {
            ByteArrayInputStream bis = new ByteArrayInputStream(data);
            DataInputStream dis = new DataInputStream(bis);

            // Read chunk coordinates
            int LOD = dis.readByte();
            int chunkX = dis.readInt();
            int chunkY = dis.readInt();
            int chunkZ = dis.readInt();

            Chunk chunk = new Chunk(chunkX, chunkY, chunkZ, LOD);

            // Read block data (16x16x16 bytes)
            for (int i = 0; i < chunk.size; i++) {
                for (int j = 0; j < chunk.size; j++) {
                    dis.readFully(chunk.blocks[i][j]);
                }
            }

            // Read dictionary size and populate BlockState
            int dictionarySize = dis.readInt();
            for (int i = 0; i < dictionarySize; i++) {
                // Assuming BlockState has its own deserialize method
                BlockState state = BlockState.deserialize(dis);
                chunk.dictionary.add(state);
            }

            return chunk;
        }
        catch (Exception e) {
            Client.log("Failed to deserialize chunk", Logger.ERROR);
            System.exit(-16);
        }
        return null;
    }
}