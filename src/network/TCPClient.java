package network;

import java.io.*;
import java.net.Socket;

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
			byte[] buffer = new byte[PACKET_SIZE];  // Adjust buffer size if necessary
			ByteArrayOutputStream messageBuffer = new ByteArrayOutputStream(); // Accumulate received bytes
			byte waitForPackets = 0;
			int bytesRead;

			while (running && (bytesRead = input.read(buffer)) != -1) {
				if(waitForPackets == (byte)0){
					waitForPackets = (byte)(buffer[1]-1);
					System.out.println("Waiting for " + waitForPackets + "more packets");
					messageBuffer.write(buffer);
				}
				else{
					waitForPackets--;
					messageBuffer.write(buffer);
					if(waitForPackets == (byte)0){
						System.out.println("Received all packets length" + messageBuffer.size());
						byte[] completeMessage = messageBuffer.toByteArray();
						listener.onMessageReceived(completeMessage);
						messageBuffer.reset();
					}
				}
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
