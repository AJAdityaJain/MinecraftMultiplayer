import engine.util.Mesh;
 import org.lwjgl.opengl.Display;
 import org.lwjgl.opengl.GL11;
 import org.lwjgl.util.vector.Vector3f;

import engine.rendering.DisplayManager;
import engine.models.Loader;
import engine.rendering.Renderer;
import engine.shader.StaticShader;
import entities.Camera;
import entities.Entity;
 import server.block.Chunk;

 public class Main {
	public static void main(String[] args) {

		DisplayManager.createDisplay();
		StaticShader shader = new StaticShader();
		Renderer renderer = new Renderer(shader);

		int TextureArrayID = Loader.loadTexture();
//		Loader.loadAtlas();
		Chunk chunk = new Chunk(0,0,0);
		Mesh mesh = new Mesh();

		Entity entity = new Entity(mesh.genGreedyMeshFromChunk(chunk), new Vector3f(0,0,-5),0,0,0,1);
		Camera camera = new Camera();
		shader.start();
		entity.model.bind();

		while(!Display.isCloseRequested()){
			entity.increaseRotation(.001f, 0, 0);
			camera.move();

			GL11.glClear(GL11.GL_COLOR_BUFFER_BIT|GL11.GL_DEPTH_BUFFER_BIT);
				shader.loadViewMatrix(camera);
				renderer.render(entity,shader);

			DisplayManager.updateDisplay();
		}

		entity.model.unbind();
		entity.model.clean();
		shader.stop();
		shader.cleanUp();
		Loader.cleanUp();
		DisplayManager.closeDisplay();

	}

}
