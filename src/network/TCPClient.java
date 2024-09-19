package network;

import client.Client;
import org.lwjgl.util.vector.Vector3f;
import server.block.Chunk;

import java.io.*;
import java.net.InetSocketAddress;
import java.net.Socket;

import static network.NetworkConstants.*;

public class TCPClient {

	private final Socket tcp_socket;
	private final OutputStream tcp_output;
	private final MessageListener tcp_listener;

	private final DataOutputStream stream;


	public boolean running;

	public interface MessageListener {
		void onMessageReceived(byte code, DataInputStream stream) throws IOException;
	}

	public TCPClient(InetSocketAddress addr, MessageListener tcp, Runnable loop) {
        try {
			this.tcp_socket = new Socket(addr.getHostName(), addr.getPort());

			this.tcp_output = tcp_socket.getOutputStream();

			this.stream = new DataOutputStream(tcp_output);
			this.tcp_listener = tcp;
			this.running = true;

			new Thread(this::listenForTCPMessages).start();
			new Thread(loop).start();
		}
		catch (IOException e) {
			throw new RuntimeException("Error connecting to server: " + e.getMessage());
		}
	}

	private void listenForTCPMessages() {
		try {
			InputStream tcp_input = tcp_socket.getInputStream();
			DataInputStream dis = new DataInputStream(tcp_input);
			byte code = NULL;
			short toRead = 0;
			int available;
			while (running && (available=dis.available())>=0) {

				if (code == NULL && available > 0) {
					code = dis.readByte();
					toRead = (short) (code % 16);
					if (toRead == 0) {
						toRead = dis.readShort();
					}
				} else if (code != NULL && available >= toRead) {
					if (code == S2C_CHUNK_SEND) {
						System.out.print("▦");
						byte[] data = new byte[toRead];
						dis.readFully(data);
						Client.addChunk(Chunk.deserialize(data));
                    } else {
						if(code == S2C_PLAYER_MOVE) System.out.print("▲");
						tcp_listener.onMessageReceived(code, dis);
                    }
                    code = NULL;
                }

			}

		} catch (IOException e) {
			System.out.println("Error reading from server: " + e.getMessage());
			System.exit(-1);
		}
	}

	public void requestChunk(int x, int y, int z){
		try{

			stream.writeByte(C2S_CHUNK_REQUEST);
			stream.writeInt(x);
			stream.writeInt(y);
			stream.writeInt(z);
			stream.flush();
			tcp_output.flush();
		}
		catch (IOException e){
			System.out.println("Error sending message: " + e.getMessage());
		}
	}

	public void sendLocation(Vector3f position) {
		try {
			stream.writeByte(C2S_PLAYER_MOVE);
			stream.writeByte(0);
			stream.writeFloat(position.x);
			stream.writeFloat(position.y);
			stream.writeFloat(position.z);
			stream.flush();
			tcp_output.flush();
		}
		catch (IOException e){
			System.out.println("Error sending message: " + e.getMessage());
		}
	}

	public void stop() {
		try{
			running = false;
			tcp_socket.close();
		}
		catch (IOException e){
			System.out.println("Error closing client: " + e.getMessage());
		}
	}

}
