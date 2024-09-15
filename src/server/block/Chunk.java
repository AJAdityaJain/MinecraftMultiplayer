package server.block;

import network.NetworkConstants;

import java.io.*;
import java.util.ArrayList;
import java.util.Random;

import static network.NetworkConstants.PACKET_SIZE;

public class Chunk {
    public static final int CHUNK_SIZE = 16;


    private final byte[][][] blocks = new byte[CHUNK_SIZE][CHUNK_SIZE][CHUNK_SIZE];
    public final int chunkX, chunkY, chunkZ;

    //Max size 256
    private final ArrayList<BlockState> dictionary = new ArrayList<>();

    public Chunk(int x , int y, int z) {
        chunkX = x;
        chunkY = y;
        chunkZ = z;
    }

    public void generate(){
        dictionary.add(new BlockState(BlockState.BlockEnum.AIR));
        dictionary.add(new BlockState(BlockState.BlockEnum.STONE));
        dictionary.add(new BlockState(BlockState.BlockEnum.DIRT));
        new Random();
        for(int i = 0; i < CHUNK_SIZE; i++) {
            for(int j = 0; j < CHUNK_SIZE; j++) {
                for(int k = 0; k < CHUNK_SIZE; k++) {
                    if(j < 8){
                        blocks[i][j][k] = 1;
                    } else if(j < 9) {
                        blocks[i][j][k] = 2;
//                    }  else if(j < 10 &&  rand.nextInt(2) == 0) {
                    }  else if(j < 10 && (i > 6 && i < 10 && k > 6 && k < 10)) {
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
            return new BlockState(BlockState.BlockEnum.NONE);
        }
        return dictionary.get(blocks[x][y][z]);
    }

    public byte[] serialize(byte type) throws IOException {
        ByteArrayOutputStream bos = new ByteArrayOutputStream();
        DataOutputStream dos = new DataOutputStream(bos);

        // Write message type
        dos.writeByte(type);
        dos.writeByte(0);

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
        data[1] = (byte) Math.ceil(((float)data.length)/PACKET_SIZE);
        return data;
    }

    // Deserialize Chunk from byte array
    public static Chunk deserialize(byte[] data) {
        try {
            ByteArrayInputStream bis = new ByteArrayInputStream(data);
            DataInputStream dis = new DataInputStream(bis);

            dis.readByte(); // Skip message type
            dis.readByte(); // Skip padding

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

            return chunk;
        }
        catch (Exception e) {
            System.out.println("Corrupt chunk data");
            System.exit(-1);
        }
        return null;
    }
}