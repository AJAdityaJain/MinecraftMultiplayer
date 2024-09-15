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
import server.block.Map;

import static network.NetworkConstants.S2C_CHUNK_SEND;


public class Client {
	private static Map world = new Map();

	public static void main(String[] args) throws InterruptedException {
		TCPClient tcp_client = new TCPClient("localhost", 8080, new TCPClient.MessageListener() {
			@Override
			public void onMessageReceived(byte[] message) {
				System.out.println("Received message");
				byte id = message[0];
				switch(id){
					case S2C_CHUNK_SEND:
						System.out.println(message.length);
						world.addChunk(Chunk.deserialize(message));
						System.out.println("Chunk received");
						break;
				}
			}
		});

		System.out.println("Client started");
		tcp_client.sendMessage("Hello Server".getBytes());

		Thread.sleep(10000);
		graphics();

		tcp_client.stop();
	}

	public static void graphics(){
		DisplayManager.createDisplay();
		StaticShader shader = new StaticShader();
		Renderer renderer = new Renderer(shader);

		Loader.loadTexture();
//		Loader.loadAtlas();

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