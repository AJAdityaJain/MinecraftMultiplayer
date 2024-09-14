package entities;

import engine.models.VAO;
import org.lwjgl.util.vector.Vector3f;

public class StaticEntity extends Entity {

    public StaticEntity(VAO model, Vector3f position) {
        super(model, position);
    }
}
