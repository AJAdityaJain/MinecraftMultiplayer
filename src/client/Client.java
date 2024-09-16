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
import org.lwjgl.util.vector.Vector3f;
import server.block.Chunk;
import server.Map;

import static network.NetworkConstants.S2C_CHUNK_SEND;


public class Client {
	private static final Map world = new Map();
	private static Renderer renderer;

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


		System.out.println("Client started. Requesting Chunks");
		for (int x = 0; x < 3; x++) {
			for (int z = 0; z < 3; z++) {
				tcp_client.requestChunk(x, 0, z);
			}
		}

		DisplayManager.createDisplay();
		Loader.loadTexture();
		renderer = new Renderer(new StaticShader());

		Thread.sleep(500);
		mainLoop(new Camera());

		tcp_client.stop();
		renderer.clean();
		Loader.cleanUp();
		DisplayManager.closeDisplay();
	}

	private static void mainLoop(Camera camera) {
		StaticEntity world_mesh = new StaticEntity(Mesh.genGreedyMeshFromChunk(world), new Vector3f(0, 0, 0));

		long time = System.currentTimeMillis();
		long new_time;
		int frame_idx_k = 0;
		int samples = DisplayManager.FPS_CAP * 1000;
		float delta_time = 0;

		while (!Display.isCloseRequested()) {
			camera.input(delta_time);
			Vector3f v = camera.getVelocity();
			cameraCollision(camera, v.x * delta_time, 0);
			cameraCollision(camera, v.y * delta_time, 1);
			cameraCollision(camera, v.z * delta_time, 2);
			cameraOnGround(camera);
			Display.setTitle(String.valueOf(camera.onGround));
			camera.tick(delta_time);

			renderer.prepare(camera);

			world_mesh.model.bind();
			renderer.renderWorld(world_mesh);
			world_mesh.model.unbind();

			DisplayManager.updateDisplay();

			frame_idx_k += 1000;
			if (frame_idx_k == samples) {
				new_time = System.currentTimeMillis();


				delta_time = (float) (new_time - time) / samples;
				Display.setTitle("FPS: " + Math.round(1 / delta_time));

				time = new_time;
				frame_idx_k = 0;
			}
		}
	}

	private static void cameraCollision(Camera camera, float va_dt, int a) {
		Vector3f hit = camera.getHitBox();


		Vector3f min = camera.getPosition();
		if (a == 0) min.x += va_dt;
		if (a == 1) min.y += va_dt;
		if (a == 2) min.z += va_dt;
		min.x -= hit.x / 2;
		min.y -= hit.y;//cam at top
		min.z -= hit.z / 2;

		Vector3f max = new Vector3f(min);
		max.x += hit.x;
		max.y += 0;
		max.z += hit.z;

		for (int x = (int) min.x; x <= (int) max.x; x++) {
			for (int y = (int) min.y; y <= (int) max.y; y++) {
				for (int z = (int) min.z; z <= (int) max.z; z++) {
					if (!world.isAir(x, y, z)) {
						if (a == 0) camera.stopVelocityX();
						else if (a == 1) {
							camera.stopVelocityY();
							if (va_dt < 0) camera.onGround = true;
						} else if (a == 2) camera.stopVelocityZ();
					}
				}
			}
		}
	}

	private static void cameraOnGround(Camera camera) {
		Vector3f hit = camera.getHitBox();

		Vector3f min = camera.getPosition();
		min.y -= .001f;
		min.x -= hit.x / 2;
		min.y -= hit.y;//cam at top
		min.z -= hit.z / 2;

		Vector3f max = new Vector3f(min);
		max.x += hit.x;
		max.y += 0;
		max.z += hit.z;

		camera.onGround = false;
		for (int x = (int) min.x; x <= (int) max.x; x++) {
			for (int y = (int) min.y; y <= (int) max.y; y++) {
				for (int z = (int) min.z; z <= (int) max.z; z++) {
					if (!world.isAir(x, y, z)) {
						camera.onGround = true;
					}
				}
			}
		}
	}
}