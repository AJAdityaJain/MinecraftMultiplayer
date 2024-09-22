package client.rendering;


import client.models.VAO;
import entities.Camera;
import entities.DynamicEntity;
import entities.StaticEntity;
import org.lwjgl.opengl.Display;
import org.lwjgl.opengl.GL11;
import org.lwjgl.util.vector.Matrix4f;

import client.shader.StaticShader;
import client.util.Maths;

import org.lwjgl.util.vector.Vector3f;

import static client.rendering.DisplayManager.*;

public class Renderer {
	private final static Vector3f UNIT = new Vector3f(1,1,1);

	private final StaticShader shader;

    public Renderer(StaticShader shader){

		this.shader = shader;
		this.shader.start();
		Matrix4f projectionMatrix = new Matrix4f();
		projectionMatrix.m00 = (float) (1f / Math.tan(FOV / 2f));
		projectionMatrix.m11 = projectionMatrix.m00 * (float) Display.getWidth() / Display.getHeight();
		projectionMatrix.m22 = -((FAR_PLANE + NEAR_PLANE) / (FAR_PLANE - NEAR_PLANE));
		projectionMatrix.m23 = -1;
		projectionMatrix.m32 = -((2 * NEAR_PLANE * FAR_PLANE) / (FAR_PLANE - NEAR_PLANE));
		projectionMatrix.m33 = 0;
		this.shader.loadProjectionMatrix(projectionMatrix);
	}

	public void render(StaticEntity entity){
		shader.loadTransformationMatrix(
				Maths.createSimpleTransformationMatrix(entity.getPosition())
		);		entity.model.render();
	}
	public void renderWorld(StaticEntity world){
		shader.loadTransformationMatrix(Maths.identity);
		world.model.render();
	}
	public void render(DynamicEntity entity){
		shader.loadTransformationMatrix(
				Maths.createTransformationMatrix(
						entity.getPosition(),entity.getRotX(), entity.getRotY(), entity.getRotZ(), UNIT
				)
		);
		entity.model.render();
	}

	public void render(VAO model, Vector3f position, float rx, float ry, float rz, Vector3f scale) {
		shader.loadTransformationMatrix(
				Maths.createTransformationMatrix(
						position,rx, ry, rz, scale
				)
		);
		model.render();
	}

	public void clean() {
		shader.stop();
		shader.cleanUp();
	}

	public void prepare(Camera camera) {
		shader.loadViewMatrix(camera);
		GL11.glClear(GL11.GL_COLOR_BUFFER_BIT | GL11.GL_DEPTH_BUFFER_BIT);

	}
}
