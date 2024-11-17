package network;

import client.Client;
import org.lwjgl.util.vector.Vector3f;
import server.block.BlockState;

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
		void onMessageReceived(byte code, DataInputStream stream, int toRead) throws IOException;
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
			System.out.println("Error connecting to server: " + e.getMessage());
			System.exit(-7);
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
						tcp_listener.onMessageReceived(code, dis,toRead);
                    code = NULL;
                }

			}

		} catch (IOException e) {
			Client.log("Error reading TCP message: " + e.getMessage(), Logger.ERROR);
			System.exit(-8);
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
			Client.log("Error requesting chunk: " + e.getMessage(), Logger.ERROR);
			System.exit(-9);
		}
	}

	public void sendPlayer(Vector3f position, float rx, float ry) {
		{
			try {
				stream.writeByte(C2S_PLAYER_MOVE);
				stream.writeFloat(position.x);
				stream.writeFloat(position.y);
				stream.writeFloat(position.z);
				stream.writeFloat(rx);
				stream.writeFloat(ry);
				stream.flush();
				tcp_output.flush();
			} catch (IOException e) {
				Client.log("Error sending player position: " + e.getMessage(), Logger.ERROR);
			}
		}
	}

	public void updateBlock(BlockState state, int x, int y, int z) {
		try {
			System.out.print("▣");
			short sz = (short) (12 + state.getSerializedSize());

			stream.writeByte(C2S_BLOCK_PLACE);
			stream.writeShort(sz);
			stream.writeInt(x);
			stream.writeInt(y);
			stream.writeInt(z);
			state.serialize(stream);
			stream.flush();
			tcp_output.flush();
		} catch (IOException e) {
			log("Error sending block update: " + e.getMessage(), Logger.ERROR);
			System.exit(-10);
		}
	}

	public void log(String message, byte id) {
		try {
			byte[] data = message.getBytes();
			stream.writeByte(C2S_LOG);
			stream.writeShort(data.length);
			stream.write(data);
			stream.writeByte(id);
			stream.flush();
			tcp_output.flush();
		} catch (IOException e) {
			System.out.println(message);
			System.out.println("Error sending this log: " + e.getMessage());
		}
	}

	public void stop() {
		try{
			running = false;
			tcp_socket.close();
		}
		catch (IOException e){
			log("Error stopping client: " + e.getMessage(), Logger.ERROR);
			System.exit(-11);
		}
	}

}
