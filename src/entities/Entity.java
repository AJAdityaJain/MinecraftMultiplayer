package entities;

import engine.models.VAO;
import org.lwjgl.util.vector.Vector3f;

public abstract class Entity {

	public final VAO model;
	private Vector3f position;

    protected Entity(VAO model, Vector3f position) {
        this.model = model;
		this.position = position;
    }


    public void increasePosition(float dx, float dy, float dz) {
		this.position.x += dx;
		this.position.y += dy;
		this.position.z += dz;
	}



	public Vector3f getPosition() {
		return position;
	}

	public void setPosition(Vector3f position) {
		this.position = position;
	}



}
