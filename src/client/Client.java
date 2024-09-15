package client;

import client.models.Loader;
import client.rendering.DisplayManager;
import client.rendering.Renderer;
import client.shader.StaticShader;
import client.util.Mesh;
import entities.Camera;
import entities.StaticEntity;
import network.TCPClient;
import org.lwjgl.opengl.Display;
import org.lwjgl.opengl.GL11;
import org.lwjgl.util.vector.Vector3f;
import server.block.Chunk;
import server.Map;

import static network.NetworkConstants.S2C_CHUNK_SEND;


public class Client {
	private static final Map world = new Map();

	public static void main(String[] args) throws InterruptedException {
		TCPClient tcp_client = new TCPClient("localhost", 8080, message -> {
            byte id = message[0];
            switch (id) {
                case S2C_CHUNK_SEND:
                    System.out.println("Received chunk");
                    world.addChunk(Chunk.deserialize(message));
                    break;
            }
        });
		System.out.println("Client started");

		DisplayManager.createDisplay();
		StaticShader shader = new StaticShader();
		Renderer renderer = new Renderer(shader);
		Loader.loadTexture();

		for (int x = 0; x < 4; x++) {
			for (int z = 0; z < 4; z++) {
				Thread.sleep(10);
				tcp_client.requestChunk(x, 0, z);
			}
		}

		StaticEntity world_mesh = new StaticEntity(Mesh.genGreedyMeshFromChunk(world), new Vector3f(0, 0, 0));

		Camera camera = new Camera();
		shader.start();
		world_mesh.model.bind();

		long time = System.currentTimeMillis();
		long new_time;
		int frame_idx_k = 0;
		int samples = 1000 * 1000;
		float delta_time = 0;

		while (!Display.isCloseRequested()) {
			camera.move(delta_time);
			cameraCollision(camera);

			camera.tick(delta_time);

			GL11.glClear(GL11.GL_COLOR_BUFFER_BIT | GL11.GL_DEPTH_BUFFER_BIT);
			shader.loadViewMatrix(camera);
			renderer.render(world_mesh, shader);


			DisplayManager.updateDisplay();
			frame_idx_k+=1000;
			if (frame_idx_k == samples) {
				new_time = System.currentTimeMillis();
				delta_time = (float) (new_time - time) / samples;
				Display.setTitle("FPS: " + Math.round(1 / delta_time));
				time = new_time;
				frame_idx_k = 0;
			}
		}

		tcp_client.stop();
		world_mesh.model.unbind();
		world_mesh.model.clean();
		shader.stop();
		shader.cleanUp();
		Loader.cleanUp();
		DisplayManager.closeDisplay();
	}

	private static void cameraCollision(Camera camera) {
		Vector3f pos = camera.getPosition();
		Vector3f vel = camera.getVelocity();
		Vector3f hit = camera.getHitBox();
		float y_off = 0.2f;
		boolean pass = true;
		for (int dy = (int) -hit.y; dy <= 0; dy ++){
			for (float dz = hit.z/-2; dz <= hit.z/2; dz ++){
				if(
						pass && (
								!world.isAir((int)(pos.x + vel.x - hit.x/2), (int)(pos.y + dy + y_off), (int)(pos.z + dz))
										||  !world.isAir((int)(pos.x + vel.x + hit.x/2), (int)(pos.y + dy + y_off), (int)(pos.z + dz))
						)){
					pass = false;
					camera.stopVelocityX();
					break;
				}
			}
		}
		if(pass)pos.x += vel.x;
		pass = true;
		for (int dy = (int) -hit.y; dy <= 0; dy ++){
			for (float dx = hit.x/-2; dx <= hit.x/2; dx ++){
				if(
						pass && (
								!world.isAir((int)(pos.x + dx), (int)(pos.y + dy + y_off), (int)(pos.z + vel.z - hit.z/2))
										||  !world.isAir((int)(pos.x + dx), (int)(pos.y + dy + y_off), (int)(pos.z + vel.z + hit.z/2))
						)){
					pass = false;
					camera.stopVelocityZ();
					break;
				}
			}
		}
		if(pass)pos.z += vel.z;
		pass = true;
		for (float dx = hit.x/-2; dx <= hit.x/2; dx ++){
			for (float dz = hit.z/-2; dz <= hit.z/2; dz ++) {
				if (pass) {
					if (!world.isAir((int) (pos.x + dx), (int) (pos.y + vel.y - hit.y + y_off), (int) (pos.z + dz))) {
						if(!camera.onGround) camera.onGround = true;
						pass = false;
						camera.stopVelocityY();
						break;
					}
					if (!world.isAir((int) (pos.x + dx), (int) (pos.y + vel.y + y_off), (int) (pos.z + dz))) {
						pass = false;
						camera.stopVelocityY();
						break;
					}
				}
			}
		}
		if(pass)pos.y += vel.y;
	}
}