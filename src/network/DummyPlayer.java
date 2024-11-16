package network;

import org.lwjgl.util.vector.Vector3f;

public class DummyPlayer {
    public final Vector3f position;
    public float rotX, rotY;

    public DummyPlayer(float x, float y, float z, float rotX, float rotY) {
        this.position = new Vector3f(x, y, z);
        this.rotX = rotX;
        this.rotY = rotY;
    }

    public void set(float x, float y, float z, float rx, float ry) {
        position.set(x, y, z);
        rotX = rx;
        rotY = ry;

    }
}
