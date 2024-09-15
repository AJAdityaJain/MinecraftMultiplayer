package client.rendering;


import client.models.VAO;
import entities.DynamicEntity;
import entities.StaticEntity;
import org.lwjgl.opengl.Display;
import org.lwjgl.util.vector.Matrix4f;

import client.shader.StaticShader;
import client.util.Maths;

import org.lwjgl.util.vector.Vector3f;

public class Renderer {
	private static final float ASPECT_RATIO = (float) Display.getWidth() / (float) Display.getHeight();
	private static final float FOV = 90;
	private static final float Y_SCALE = (float) ((1f / Math.tan( (FOV / 2f))) * ASPECT_RATIO);
	private static final float X_SCALE = Y_SCALE / ASPECT_RATIO;
	private static final float NEAR_PLANE = 0.1f;
	private static final float FAR_PLANE = 1000;
	
	private Matrix4f projectionMatrix;
	
	public Renderer(StaticShader shader){
		createProjectionMatrix();
		shader.start();
		shader.loadProjectionMatrix(projectionMatrix);
		shader.stop();
	}

	public void render(StaticEntity entity, StaticShader shader){
		shader.loadTransformationMatrix(
				Maths.createSimpleTransformationMatrix(entity.getPosition())
		);		entity.model.render();
	}
	public void render(DynamicEntity entity, StaticShader shader){
		shader.loadTransformationMatrix(
				Maths.createTransformationMatrix(
						entity.getPosition(),entity.getRotX(), entity.getRotY(), entity.getRotZ(), entity.getScale()
				)
		);
		entity.model.render();
	}

	public void render(VAO model, Vector3f position, float rx, float ry, float rz, Vector3f scale, StaticShader shader) {
		shader.loadTransformationMatrix(
				Maths.createTransformationMatrix(
						position,rx, ry, rz, scale
				)
		);
		model.render();
	}
	
	private void createProjectionMatrix(){

		projectionMatrix = new Matrix4f();
		projectionMatrix.m00 = X_SCALE;
		projectionMatrix.m11 = Y_SCALE;
		projectionMatrix.m22 = -((FAR_PLANE + NEAR_PLANE) / (FAR_PLANE - NEAR_PLANE));
		projectionMatrix.m23 = -1;
		projectionMatrix.m32 = -((2 * NEAR_PLANE * FAR_PLANE) / (FAR_PLANE - NEAR_PLANE));
		projectionMatrix.m33 = 0;
	}

}
