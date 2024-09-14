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
import server.block.Map;

public class Main {
	public static void main(String[] args) {

		DisplayManager.createDisplay();
		StaticShader shader = new StaticShader();
		Renderer renderer = new Renderer(shader);

		int TextureArrayID = Loader.loadTexture();
//		Loader.loadAtlas();
		Map world = new Map();
		StaticEntity world_mesh = new StaticEntity(Mesh.genGreedyMeshFromChunk(world), new Vector3f(0,0,0));


		Camera camera = new Camera();
		shader.start();
		world_mesh.model.bind();

		long time = System.currentTimeMillis();
		int frame_idx = 0;
		int samples = 1000;

		while(!Display.isCloseRequested()){
			camera.move();

			GL11.glClear(GL11.GL_COLOR_BUFFER_BIT|GL11.GL_DEPTH_BUFFER_BIT);
				shader.loadViewMatrix(camera);
				renderer.render(world_mesh,shader);

			DisplayManager.updateDisplay();
			frame_idx++;
			if(frame_idx == samples){
				long new_time = System.currentTimeMillis();
				Display.setTitle("FPS: " + Math.round((1000.0f * samples) / (new_time - time)));
				time = new_time;
				frame_idx = 0;
			}
		}

		world_mesh.model.unbind();
		world_mesh.model.clean();
		shader.stop();
		shader.cleanUp();
		Loader.cleanUp();
		DisplayManager.closeDisplay();

	}

}
