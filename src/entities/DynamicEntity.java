package entities;

import client.models.VAO;
import org.lwjgl.util.vector.Vector3f;

public class DynamicEntity extends Entity {
    protected float rotX, rotY;
    protected final Vector3f hitBox;
    protected final Vector3f velocity = new Vector3f(0, 0, 0);
    protected final Vector3f acceleration = new Vector3f(0, 0, 0);
    protected static final float g = 0;//32.656f;//Minecraft accurate units
    public boolean onGround = false;
    protected final float speed;

    public void tick(float delta_time){
        velocity.x += delta_time * acceleration.x;
        velocity.y += delta_time * acceleration.y;
        if (!onGround)
            velocity.y -= delta_time *g;
        velocity.z += delta_time * acceleration.z;
        position.x += delta_time * velocity.x;
        position.y += delta_time * velocity.y;
        position.z += delta_time * velocity.z;
    }
    public DynamicEntity(VAO model, Vector3f position,Vector3f hitBox, float rotX, float rotY, float rotZ,
                  float speed) {
        super(model, position);
        this.speed = speed;
        this.hitBox = hitBox;
        this.rotX = rotX;
        this.rotY = rotY;
    }

    public Vector3f getHitBox() {
        return hitBox;
    }

    public Vector3f getVelocity() {
        return new Vector3f(velocity);
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
}
