package server.block;

import java.util.ArrayList;

public class Chunk<T extends Number> {
    //Max size 256
    public ArrayList<BlockState> dictionary = new ArrayList<>();
    //16x(16x16)
    public T[][] blocks;//new T[16][256];

}
