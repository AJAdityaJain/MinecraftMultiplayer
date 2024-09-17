package server.block;


import server.util.FastNoiseLite;

import java.io.*;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Random;

import static network.NetworkConstants.PACKET_SIZE;

public class Chunk {
    public static final int CHUNK_SIZE = 16;
    private static final FastNoiseLite noise = new FastNoiseLite();


    private final byte[][][] blocks = new byte[CHUNK_SIZE][CHUNK_SIZE][CHUNK_SIZE];
    public final int chunkX, chunkY, chunkZ;

    //Max size 256
    private final ArrayList<BlockState> dictionary = new ArrayList<>();

    public Chunk(int x , int y, int z) {
        chunkX = x;
        chunkY = y;
        chunkZ = z;
    }

    private int genBlock(int x, int y, int z) {
        double Continentals = Math.pow(1.3,4 + noise.GetNoise (x + CHUNK_SIZE*chunkX,z + CHUNK_SIZE*chunkZ)*4);
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
    public void generate(){
        noise.SetNoiseType(FastNoiseLite.NoiseType.OpenSimplex2);

        dictionary.add(new BlockState(BlockState.BlockEnum.AIR));
        dictionary.add(new BlockState(BlockState.BlockEnum.STONE));
        dictionary.add(new BlockState(BlockState.BlockEnum.DIRT));
        dictionary.add(new BlockState(BlockState.BlockEnum.GRASS));
        new Random();
        for(int i = 0; i < CHUNK_SIZE; i++) {
            for(int j = 0; j < CHUNK_SIZE; j++) {
                for(int k = 0; k < CHUNK_SIZE; k++) {
                    blocks[i][j][k] = (byte) genBlock(i, j, k);
                }
            }
        }
    }


    public BlockState getBlock(int x, int y, int z) {
        if(x < 0 || x >= 16 || y < 0 || y >= 16 || z < 0 || z >= 16) {
            return new BlockState(BlockState.BlockEnum.NONE);
        }
        return dictionary.get(blocks[x][y][z]);
    }

    public byte[] serialize(byte type) throws IOException {
        ByteArrayOutputStream bos = new ByteArrayOutputStream();
        DataOutputStream dos = new DataOutputStream(bos);

        dos.writeByte(0);
        dos.writeByte(0);
        dos.writeByte(type);

        // Write chunk coordinates
        dos.writeInt(chunkX);
        dos.writeInt(chunkY);
        dos.writeInt(chunkZ);

        // Write block data (16x16x16 bytes)
        for (int i = 0; i < CHUNK_SIZE; i++) {
            for (int j = 0; j < CHUNK_SIZE; j++) {
                dos.write(blocks[i][j]);
            }
        }

        // Write dictionary size and each BlockState
        dos.writeInt(dictionary.size());
        for (BlockState state : dictionary) {
            // Assuming BlockState has its own serialize method
            dos.writeInt(state.serialize());
        }

        dos.flush();
        byte[] data = bos.toByteArray();
        data[1] = (byte) ((data.length-2) & 0xFF);//first byte of the length
        data[0] = (byte) (((data.length-2) >> 8) & 0xFF);//second byte of the length
        return data;
    }

    // Deserialize Chunk from byte array
    public static Chunk deserialize(byte[] data) {
        try {
            ByteArrayInputStream bis = new ByteArrayInputStream(data);
            DataInputStream dis = new DataInputStream(bis);

//            dis.readByte(); // Skip message length
//            dis.readByte(); // Skip message length
            dis.readByte(); // Skip chunk message type


            // Read chunk coordinates
            int chunkX = dis.readInt();
            int chunkY = dis.readInt();
            int chunkZ = dis.readInt();

            Chunk chunk = new Chunk(chunkX, chunkY, chunkZ);

            // Read block data (16x16x16 bytes)
            for (int i = 0; i < CHUNK_SIZE; i++) {
                for (int j = 0; j < CHUNK_SIZE; j++) {
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

            System.out.println("Deserialized chunk");
            return chunk;
        }
        catch (Exception e) {
            System.out.println("Corrupt chunk data");
            System.exit(-1);
        }
        return null;
    }
}