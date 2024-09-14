import engine.util.Mesh;
import entities.StaticEntity;
import org.lwjgl.opengl.Display;
 import org.lwjgl.opengl.GL11;
 import org.lwjgl.util.vector.Vector3f;

import engine.rendering.DisplayManager;
import engine.models.Loader;
import engine.rendering.Renderer;
import engine.shader.StaticShader;
import entities.Camera;
 import server.block.Chunk;

 public class Main {
	public static void main(String[] args) {

		DisplayManager.createDisplay();
		StaticShader shader = new StaticShader();
		Renderer renderer = new Renderer(shader);

		int TextureArrayID = Loader.loadTexture();
//		Loader.loadAtlas();
		Chunk chunk = new Chunk(0,0,0);
		StaticEntity world = new StaticEntity(Mesh.genGreedyMeshFromChunk(chunk), new Vector3f(0,0,-5));

		System.gc();

		Camera camera = new Camera();
		shader.start();
		world.model.bind();
		while(!Display.isCloseRequested()){
			camera.move();

			GL11.glClear(GL11.GL_COLOR_BUFFER_BIT|GL11.GL_DEPTH_BUFFER_BIT);
				shader.loadViewMatrix(camera);
				renderer.render(world,shader);

			DisplayManager.updateDisplay();
		}

		world.model.unbind();
		world.model.clean();
		shader.stop();
		shader.cleanUp();
		Loader.cleanUp();
		DisplayManager.closeDisplay();

	}

}
