package client;

import client.models.Loader;
import client.models.VAO;
import client.rendering.DisplayManager;
import client.rendering.Renderer;
import client.shader.StaticShader;
import client.util.Mesh;
import entities.Camera;
import entities.DynamicEntity;
import entities.StaticEntity;
import network.TCP_UDP_Client;
import org.lwjgl.opengl.Display;
import org.lwjgl.util.vector.Vector3f;
import server.block.Chunk;
import server.Map;

import java.nio.ByteBuffer;
import java.util.HashMap;

import static network.NetworkConstants.*;


public class Client {
	private static final Map world = new Map();
	private static Renderer renderer;
	private static Camera player;
	private static TCP_UDP_Client tcp_client;
	private static final HashMap<Byte, Vector3f> players = new HashMap<>();

	public static void main(String[] args) throws InterruptedException {
		player = new Camera();

		tcp_client = new TCP_UDP_Client(
				message -> {
					byte id = message[0];
					switch (id) {
						case S2C_CHUNK_SEND:
							System.out.print("▦");
							world.addChunk(Chunk.deserialize(message));
							break;
					}
				},
				buf -> {
					byte id = buf[0];
					switch (id) {
						case S2C_PLAYER_JOIN:
							System.out.println("Player #" + buf[1] +" joined. Now " + players.size() + " players");
							players.put(buf[1], new Vector3f(8, 14, 8));
							break;
						case S2C_PLAYER_LEAVE:
							System.out.println("Player #" + buf[1] +" left");
							players.remove(buf[1]);
							break;
						case S2C_PLAYER_MOVE:
							int asInt = (buf[5] & 0xFF)
									| ((buf[4] & 0xFF) << 8)
									| ((buf[3] & 0xFF) << 16)
									| ((buf[2] & 0xFF) << 24);
							float x = Float.intBitsToFloat(asInt);
							asInt = (buf[9] & 0xFF)
									| ((buf[8] & 0xFF) << 8)
									| ((buf[7] & 0xFF) << 16)
									| ((buf[6] & 0xFF) << 24);
							float y = Float.intBitsToFloat(asInt);
							asInt = (buf[13] & 0xFF)
									| ((buf[12] & 0xFF) << 8)
									| ((buf[11] & 0xFF) << 16)
									| ((buf[10] & 0xFF) << 24);
							float z = Float.intBitsToFloat(asInt);

							players.put(buf[1], new Vector3f(x, y, z));
							break;
					}
				},
				() -> {
                    try {
						Thread.sleep(10000);
							while(tcp_client.running) {
                                //noinspection BusyWait
                                Thread.sleep(100);
							tcp_client.sendLocation(player.getPosition());
						}
                    } catch (InterruptedException e) {
                        throw new RuntimeException(e);
                    }
				}
		);


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
		mainLoop();

		tcp_client.stop();
		renderer.clean();
		Loader.cleanUp();
		DisplayManager.closeDisplay();
	}

	private static void mainLoop() {
		StaticEntity world_mesh = new StaticEntity(Mesh.genGreedyMeshFromChunk(world), new Vector3f(0, 0, 0));
		VAO cube_model =  Mesh.genCubeMesh();

		long time = System.currentTimeMillis();
		long new_time;
		int frame_idx_k = 0;
		int samples = DisplayManager.FPS_CAP * 1000;
		float delta_time = 0;

		while (!Display.isCloseRequested()) {
			player.input(delta_time);
			Vector3f v = player.getVelocity();
			cameraCollision(v.x * delta_time, 0);
			cameraCollision(v.y * delta_time, 1);
			cameraCollision(v.z * delta_time, 2);
			cameraOnGround();
			player.tick(delta_time);

			renderer.prepare(player);

			world_mesh.model.bind();
			renderer.renderWorld(world_mesh);
			world_mesh.model.unbind();

			cube_model.bind();
			for (Vector3f p : players.values()) {
				renderer.render(cube_model, p, 0, 0, 0, new Vector3f(1, 1, 1));
			}
			cube_model.unbind();

			DisplayManager.updateDisplay();

			frame_idx_k += 1000;
			if (frame_idx_k == samples) {
				Display.setTitle(players.size() + " players online");
				new_time = System.currentTimeMillis();

				delta_time = (float) (new_time - time) / samples;
//				Display.setTitle("FPS: " + Math.round(1 / delta_time));

				time = new_time;
				frame_idx_k = 0;
			}
		}
	}

	private static void cameraCollision(float va_dt, int a) {
		Vector3f hit = player.getHitBox();


		Vector3f min = player.getPosition();
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
						if (a == 0) player.stopVelocityX();
						else if (a == 1) {
							player.stopVelocityY();
							if (va_dt < 0) player.onGround = true;
						} else if (a == 2) player.stopVelocityZ();
					}
				}
			}
		}
	}

	private static void cameraOnGround() {
		Vector3f hit = player.getHitBox();

		Vector3f min = player.getPosition();
		min.y -= .001f;
		min.x -= hit.x / 2;
		min.y -= hit.y;//cam at top
		min.z -= hit.z / 2;

		Vector3f max = new Vector3f(min);
		max.x += hit.x;
		max.y += 0;
		max.z += hit.z;

		player.onGround = false;
		for (int x = (int) min.x; x <= (int) max.x; x++) {
			for (int y = (int) min.y; y <= (int) max.y; y++) {
				for (int z = (int) min.z; z <= (int) max.z; z++) {
					if (!world.isAir(x, y, z)) {
						player.onGround = true;
					}
				}
			}
		}
	}
}