package client;

import client.models.Loader;
import client.models.VAO;
import client.rendering.DisplayManager;
import client.rendering.Renderer;
import client.shader.StaticShader;
import client.util.Mesh;
import entities.Camera;
import entities.StaticEntity;
import network.DummyPlayer;
import network.Logger;
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
	private static final HashMap<Byte, DummyPlayer> players = new HashMap<>();
	private static boolean updateMesh = true;
	private static boolean updateMeshData = false;

	public static void log(String s,byte lvl){
		tcp_client.log(s, lvl);

	}

	public static void addChunk(Chunk chunk) {
		world.addChunk(chunk);
	}

	public static void main(String[] args) throws InterruptedException {
		Path currentRelativePath = Paths.get("");
		String s = currentRelativePath.toAbsolutePath().toString();
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
							players.put(id, new DummyPlayer(0,0,0,0, 8));
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
							float rx = stream.readFloat();
							float ry = stream.readFloat();
							DummyPlayer d = players.get(id);
							if(d == null) d = new DummyPlayer(x,y,z,rx,ry);
							else d.set(x, y, z,rx, ry);
							break;
						}
						case S2C_CHUNK_SEND:{
							System.out.print("▦");
							byte[] data = new byte[toRead];
							stream.readFully(data);
							Chunk ch = Chunk.deserialize(data);
							Client.addChunk(ch);
							boolean b = false;
							for (Vector3f v : world.loadingChunks){
								if (v.x == ch.chunkX && v.y == ch.chunkY && v.z == ch.chunkZ){
									world.loadingChunks.remove(v);
									b = true;
								}
							}
							if(!b) {
								Client.log("Received chunk was not requested", Logger.ERROR);
								System.exit(-2);
							}
							break;
						}
						case S2C_BLOCK_PLACE:{
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
								Mesh.genGreedyMeshFromMap(world);
								updateMesh = false;
								updateMeshData = true;
							}
                            //noinspection BusyWait
                            Thread.sleep(100);
							tcp_client.sendPlayer(player.getPosition(),player.getRotX(),player.getRotY());
						}
                    } catch (InterruptedException e) {
						Client.log("Interrupt Exception: " + e.getMessage(), Logger.ERROR);
					}
				}
		);

		int px = (int)(player.getPosition().x/16);
		int py = (int)(player.getPosition().y/16);
		int pz = (int)(player.getPosition().z/16);
		world.tryLoad(px, py, pz, tcp_client);


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
		VAO cube_model = Mesh.genCubeMesh();

		long time = System.currentTimeMillis();
		long new_time;
		int frame_idx_k = 0;
		int samples = DisplayManager.FPS_CAP * 1000;
		float delta_time = 0;

		while (!Display.isCloseRequested()) {
			if(updateMeshData){
				updateMeshData = false;
				if(world_mesh != null) world_mesh.model.clean();
				world_mesh = new StaticEntity(Mesh.genGreedyMesh(), new Vector3f(0, 0, 0));
			}
			if(player.input(world, tcp_client,delta_time)){
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

			cube_model.bind();
			for (DummyPlayer p : players.values()) {
				renderer.render(cube_model, p.position, p.rotX, p.rotY, 0, new Vector3f(1, 1, 1));
			}
			cube_model.unbind();

			DisplayManager.updateDisplay();

			frame_idx_k += 1000;
			if (frame_idx_k == samples) {
				int px = (int)(player.getPosition().x/16);
				int py = (int)(player.getPosition().y/16);
				int pz = (int)(player.getPosition().z/16);
				updateMesh = world.tryUnload(px, py, pz);
				if(updateMesh) world.tryLoad(px, py, pz, tcp_client);



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