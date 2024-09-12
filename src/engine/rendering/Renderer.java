package engine.rendering;

import engine.models.TexturedModel;

import org.lwjgl.opengl.Display;
import org.lwjgl.opengl.GL11;
import org.lwjgl.opengl.GL13;
import org.lwjgl.opengl.GL20;
import org.lwjgl.opengl.GL30;
import org.lwjgl.util.vector.Matrix4f;

import engine.shader.StaticShader;
import engine.util.Maths;

import entities.Entity;

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

	public void prepare() {
		GL11.glClear(GL11.GL_COLOR_BUFFER_BIT|GL11.GL_DEPTH_BUFFER_BIT);
	}

	public void render(Entity entity, StaticShader shader) {
		TexturedModel model = entity.getModel();
		GL30.glBindVertexArray(model.getVaoID());
		GL20.glEnableVertexAttribArray(0);
		GL20.glEnableVertexAttribArray(1);
		shader.loadTransformationMatrix(Maths.createTransformationMatrix(entity.getPosition(),
				entity.getRotX(), entity.getRotY(), entity.getRotZ(), entity.getScale())
		);
		GL13.glActiveTexture(GL13.GL_TEXTURE0);
		GL11.glBindTexture(GL11.GL_TEXTURE_2D, model.getTextureID());
		GL11.glDrawElements(GL11.GL_TRIANGLES, model.getVertexCount(), GL11.GL_UNSIGNED_INT, 0);
		GL20.glDisableVertexAttribArray(0);
		GL20.glDisableVertexAttribArray(1);
		GL30.glBindVertexArray(0);
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
