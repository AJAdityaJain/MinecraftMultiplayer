package entities;

import client.models.VAO;
import org.lwjgl.util.vector.Vector3f;

public abstract class Entity {

	public final VAO model;
	protected Vector3f position;

    protected Entity(VAO model, Vector3f position) {
        this.model = model;
		this.position = position;
    }


    public void increasePosition(Vector3f dPos) {
		position.x += dPos.x;
		position.y += dPos.y;
		position.z += dPos.z;
	}



	public Vector3f getPosition() {
		return position;
	}

	public void setPosition(Vector3f position) {
		this.position = position;
	}



}
