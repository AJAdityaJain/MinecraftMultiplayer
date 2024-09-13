 import engine.models.TexturedModel;

import org.lwjgl.opengl.Display;
 import org.lwjgl.opengl.GL11;
 import org.lwjgl.util.vector.Vector3f;

import engine.rendering.DisplayManager;
import engine.models.Loader;
import engine.rendering.Renderer;
import engine.shader.StaticShader;
import entities.Camera;
import entities.Entity;

public class Main {
	public static void main(String[] args) {

		DisplayManager.createDisplay();
		StaticShader shader = new StaticShader();
		Renderer renderer = new Renderer(shader);

		float[] vertices = {			
				0,1,0,
				0,0,0,
				1,0,0,
				1,1,0,

				0,1,1,
				0,0,1,
				1,0,1,
				1,1,1,

				1,1,0,
				1,0,0,
				1,0,1,
				1,1,1,

				0,1,0,
				0,0,0,
				0,0,1,
				0,1,1,

				0,1,1,
				0,1,0,
				1,1,0,
				1,1,1,

				0,0,1,
				0,0,0,
				1,0,0,
				1,0,1

		};
		
		float t = 0.5f;
		float[] textureCoords = {
				t,t,
				t,1,
				1,1,
				1,t,
				t,t,
				t,1,
				1,1,
				1,t,
				t,t,
				t,1,
				1,1,
				1,t,
				t,t,
				t,1,
				1,1,
				1,t,
				t,t,
				t,1,
				1,1,
				1,t,
				t,t,
				t,1,
				1,1,
				1,t
		};

		int[] indices = {
				//Back
				1,0,3,
				1,3,2,

				//FRONT
				4,5,7,
				7,5,6,

				//EAST
				9,8,11,
				9,11,10,

				//WEST
				12,13,15,
				15,13,14,

				//NORTH
				17,16,19,
				17,19,18,

				//SOUTH
				20,21,23,
				23,21,22

		};


		TexturedModel staticModel = new TexturedModel(
				Loader.createVAO(vertices,textureCoords,indices),
				Loader.loadTexture("grass")
		);

		Entity entity = new Entity(staticModel, new Vector3f(0,0,-5),0,0,0,1);
		Camera camera = new Camera();
		shader.start();
		staticModel.bind();

		while(!Display.isCloseRequested()){
			entity.increaseRotation(.001f, 0, 0);
			camera.move();

			GL11.glClear(GL11.GL_COLOR_BUFFER_BIT|GL11.GL_DEPTH_BUFFER_BIT);
				shader.loadViewMatrix(camera);
				renderer.render(staticModel,entity.getPosition(),entity.getRotX(),entity.getRotY(),entity.getRotZ(),entity.getScale(),shader);

			DisplayManager.updateDisplay();
		}

		staticModel.unbind();
		shader.stop();
		shader.cleanUp();
		Loader.cleanUp();
		DisplayManager.closeDisplay();

	}

}
