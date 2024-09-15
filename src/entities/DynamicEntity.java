package entities;

import client.models.VAO;
import org.lwjgl.util.vector.Vector3f;

public class DynamicEntity extends Entity {
    protected float rotX, rotY, rotZ;
    protected Vector3f hitBox;
    protected Vector3f scale;
    protected Vector3f velocity = new Vector3f(0, 0, 0);
    protected Vector3f acceleration = new Vector3f(0, 0, 0);
    protected static float g = 0.1f;
    public boolean onGround = false;
    protected final float speed;

    public void tick(float delta_time){
        velocity.x += delta_time * acceleration.x;
        velocity.y += delta_time * acceleration.y;
if (!onGround) velocity.y -= delta_time *g;
        velocity.z += delta_time * acceleration.z;
        position.x += delta_time * velocity.x;
        position.y += delta_time * velocity.y;
        position.z += delta_time * velocity.z;
    }
    public DynamicEntity(VAO model, Vector3f position,Vector3f hitBox, float rotX, float rotY, float rotZ,
                  float scale, float speed) {
        super(model, position);
        this.speed = speed;
        this.hitBox = hitBox;
        this.rotX = rotX;
        this.rotY = rotY;
        this.rotZ = rotZ;
        this.scale = new Vector3f(scale, scale, scale);
    }

    public Vector3f getHitBox() {
        return hitBox;
    }

    public Vector3f getVelocity() {
        return velocity;
    }

    public void stopVelocityX() {
        velocity.x = 0;
    }
    public void stopVelocityY() {
        velocity.y = 0;
    }
    public void stopVelocityZ() {
        velocity.z = 0;
    }


    public void increaseRotation(float dx, float dy, float dz) {
        this.rotX += dx;
        this.rotY += dy;
        this.rotZ += dz;
    }

    public float getRotX() {
        return rotX;
    }

    public void setRotX(float rotX) {
        this.rotX = rotX;
    }

    public float getRotY() {
        return rotY;
    }

    public void setRotY(float rotY) {
        this.rotY = rotY;
    }

    public float getRotZ() {
        return rotZ;
    }

    public void setRotZ(float rotZ) {
        this.rotZ = rotZ;
    }

    public Vector3f getScale() {
        return scale;
    }

    public void setScale(Vector3f scale) {
        this.scale = scale;
    }

}
