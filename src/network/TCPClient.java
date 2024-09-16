package network;

import java.io.*;
import java.net.Socket;
import java.util.Arrays;

import static network.NetworkConstants.C2S_CHUNK_REQUEST;
import static network.NetworkConstants.PACKET_SIZE;

public class TCPClient {

	private final Socket socket;
	private final InputStream input;
	private final OutputStream output;
	private final MessageListener listener;
	private boolean running;

	public interface MessageListener {
		void onMessageReceived(byte[] message);
	}

	public TCPClient(String serverAddress, int serverPort, MessageListener listener) {
		try {
			this.socket = new Socket(serverAddress, serverPort);
			this.input = socket.getInputStream();
			this.output = socket.getOutputStream();
			this.listener = listener;
			this.running = true;
			// Start a thread to listen for server messages
			new Thread(this::listenForServerMessages).start();
		}
		catch (IOException e) {
			throw new RuntimeException("Error connecting to server: " + e.getMessage());
		}
	}

	private void listenForServerMessages() {

		try {
			DataInputStream dis = new DataInputStream(input);
			while (running && input.available()>=0) {
				short len = (short) ((dis.readByte() << 8) | (dis.readByte() & 0xff));
				byte[] buffer = dis.readNBytes(len);
				listener.onMessageReceived(buffer);
			}

		} catch (IOException e) {
			System.out.println("Error reading from server: " + e.getMessage());
		}
	}

	public void sendMessage(byte[] message) {
		try{
			output.write(message);
			output.flush();
		}
		catch (IOException e){
			System.out.println("Error sending message: " + e.getMessage());
		}
	}

	public void requestChunk(int x, int y, int z){
		try{
			ByteArrayOutputStream bos = new ByteArrayOutputStream();
			DataOutputStream dos = new DataOutputStream(bos);

			dos.writeByte(C2S_CHUNK_REQUEST);
			dos.writeInt(x);
			dos.writeInt(y);
			dos.writeInt(z);
			dos.flush();


			output.write(bos.toByteArray());
			output.flush();
		}
		catch (IOException e){
			System.out.println("Error sending message: " + e.getMessage());
		}
	}

	public void stop() {
		try{
			running = false;
			socket.close();
		}
		catch (IOException e){
			System.out.println("Error closing client: " + e.getMessage());
		}
	}

}
