import engine.models.TexturedModel;

import org.lwjgl.opengl.Display;
import org.lwjgl.util.vector.Vector3f;

import engine.rendering.DisplayManager;
import engine.rendering.Loader;
import engine.rendering.Renderer;
import engine.shader.StaticShader;
import entities.Camera;
import entities.Entity;

public class Main {

	public static void main(String[] args) {

		DisplayManager.createDisplay();
		Loader loader = new Loader();
		StaticShader shader = new StaticShader();
		Renderer renderer = new Renderer(shader);

		float[] vertices = {			
				-0.5f,0.5f,-0.5f,
				-0.5f,-0.5f,-0.5f,
				0.5f,-0.5f,-0.5f,
				0.5f,0.5f,-0.5f,
				
				-0.5f,0.5f,0.5f,
				-0.5f,-0.5f,0.5f,
				0.5f,-0.5f,0.5f,
				0.5f,0.5f,0.5f,
				
				0.5f,0.5f,-0.5f,
				0.5f,-0.5f,-0.5f,
				0.5f,-0.5f,0.5f,
				0.5f,0.5f,0.5f,
				
				-0.5f,0.5f,-0.5f,
				-0.5f,-0.5f,-0.5f,
				-0.5f,-0.5f,0.5f,
				-0.5f,0.5f,0.5f,
				
				-0.5f,0.5f,0.5f,
				-0.5f,0.5f,-0.5f,
				0.5f,0.5f,-0.5f,
				0.5f,0.5f,0.5f,
				
				-0.5f,-0.5f,0.5f,
				-0.5f,-0.5f,-0.5f,
				0.5f,-0.5f,-0.5f,
				0.5f,-0.5f,0.5f
				
		};
		
		float[] textureCoords = {
				0,0,
				0,1,
				1,1,
				1,0,			
				0,0,
				0,1,
				1,1,
				1,0,			
				0,0,
				0,1,
				1,1,
				1,0,
				0,0,
				0,1,
				1,1,
				1,0,
				0,0,
				0,1,
				1,1,
				1,0,
				0,0,
				0,1,
				1,1,
				1,0
		};
		
		int[] indices = {
				1,0,3,
				1,3,2,

				4,5,7,
				7,5,6,

				9,8,11,
				9,11,10,

				12,13,15,
				15,13,14,	

				17,16,19,
				17,19,18,

				20,21,23,
				23,21,22

		};
		
		int[] model = loader.loadToVAO(vertices,textureCoords,indices);

		TexturedModel staticModel = new TexturedModel(model[0],model[1],loader.loadTexture("dirt"));

		Entity entity = new Entity(staticModel, new Vector3f(0,0,-5),0,0,0,1);
		
		Camera camera = new Camera();
		
		while(!Display.isCloseRequested()){
			entity.increaseRotation(.01f, 0, 0);
			camera.move();

			renderer.prepare();
			shader.start();
				shader.loadViewMatrix(camera);
				renderer.render(entity,shader);
			shader.stop();

			DisplayManager.updateDisplay();
		}

		shader.cleanUp();
		loader.cleanUp();
		DisplayManager.closeDisplay();

	}

}
