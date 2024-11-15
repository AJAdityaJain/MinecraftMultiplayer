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
import server.block.BlockState;
import server.block.Chunk;
import server.Map;

import java.net.InetSocketAddress;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.util.HashMap;
import java.util.Scanner;

import static network.NetworkConstants.*;


public class Client {
	private static final Map world = new Map();
	private static Renderer renderer;
	private static Camera player;
	private static TCPClient tcp_client;
	private static final HashMap<Byte, Vector3f> players = new HashMap<>();
	private static boolean updateMesh = true;
	private static boolean updateMeshData = false;


	public static void addChunk(Chunk chunk) {
		world.addChunk(chunk);
	}

	public static void main(String[] args) throws InterruptedException {
		Path currentRelativePath = Paths.get("");
		String s = currentRelativePath.toAbsolutePath().toString();
		System.out.println("Current absolute path is: " + s);
		System.setProperty("org.lwjgl.librarypath", s + "\\lib\\natives");
		System.setProperty("org.lwjgl.classpath", s + "\\lib\\jars");
		player = new Camera();

		Scanner myObj = new Scanner(System.in);
		System.out.print("Enter IP: ");
		String ip = myObj.nextLine();
		System.out.print("Enter Port: ");
		int port = myObj.nextInt();
		tcp_client = new TCPClient(
				new InetSocketAddress(ip, port),
				(code,stream,toRead) -> {
					switch (code) {
						case S2C_PLAYER_JOIN:{
							byte id = stream.readByte();
							System.out.println("Player #" + id +" joined. Now " + players.size() + " players");
							players.put(id, new Vector3f(8, 14, 8));
							break;
						}
						case S2C_PLAYER_LEAVE: {
							byte id = stream.readByte();
							System.out.println("Player #" + id + " left");
							players.remove(id);
							break;
						}
						case S2C_PLAYER_MOVE: {
							byte id = stream.readByte();
							float x = stream.readFloat();
							float y = stream.readFloat();
							float z = stream.readFloat();
							players.put(id, new Vector3f(x, y, z));
							break;
						}
						case S2C_CHUNK_SEND:{
							System.out.print("▦");
							byte[] data = new byte[toRead];
							stream.readFully(data);
							Client.addChunk(Chunk.deserialize(data));
							break;
						}
						case S2C_BLOCK_PLACE:{
							System.out.print("▣");
							int x = stream.readInt();
							int y = stream.readInt();
							int z = stream.readInt();
							BlockState b = BlockState.deserialize(stream);
							world.setBlock(x, y, z, b);
							updateMesh = true;
							break;
//							Client.addChunk(Chunk.deserialize(data));

						}
					}
				},
				() -> {
                    try {
						while(tcp_client.running) {
							if(updateMesh){
								System.out.println("Updating Mesh data");
								Mesh.genGreedyMeshFromMap(world);
								updateMesh = false;
								updateMeshData = true;
							}
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
			for (int z = 0; z < 3 ; z++) {
				tcp_client.requestChunk(x, 0, z);
			}
		}

		DisplayManager.createDisplay();
		Loader.loadTexture();
		renderer = new Renderer(new StaticShader());

		Thread.sleep(100);
		mainLoop();

		tcp_client.stop();
		renderer.clean();
		Loader.cleanUp();
		DisplayManager.closeDisplay();
	}

	private static void mainLoop() {
//		Mesh.genGreedyMeshFromMap(world);
		StaticEntity world_mesh = null;

		long time = System.currentTimeMillis();
		long new_time;
		int frame_idx_k = 0;
		int samples = DisplayManager.FPS_CAP * 1000;
		float delta_time = 0;

		while (!Display.isCloseRequested()) {
			if(updateMeshData){
				System.out.println("Updating Mesh from data");
				updateMeshData = false;
				if(world_mesh != null) world_mesh.model.clean();
				world_mesh = new StaticEntity(Mesh.genGreedyMesh(), new Vector3f(0, 0, 0));
			}
			if(player.input(world,delta_time)){
//				players.put((byte) -1, player.getPosition());
				updateMesh = true;
			}
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

//			cube_model.bind();
//			for (Vector3f p : players.values()) {
//				renderer.render(cube_model, p, 0, 0, 0, new Vector3f(1, 1, 1));
//			}
//			cube_model.unbind();

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