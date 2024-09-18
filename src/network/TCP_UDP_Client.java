package network;

import client.Client;
import entities.Camera;
import org.lwjgl.util.vector.Vector3f;

import java.io.*;
import java.net.DatagramPacket;
import java.net.DatagramSocket;
import java.net.InetAddress;
import java.net.Socket;
import java.nio.ByteBuffer;

import static network.NetworkConstants.*;

public class TCP_UDP_Client {

	private final Socket tcp_socket;
	private final OutputStream tcp_output;
	private final MessageListener tcp_listener;

	private final DatagramSocket udp_socket;
	private final MessageListener udp_listener;

	private static final ByteArrayOutputStream bos = new ByteArrayOutputStream();
	private static final DataOutputStream dos = new DataOutputStream(bos);


	public boolean running;

	public interface MessageListener {
		void onMessageReceived(byte[] message);
	}

	public TCP_UDP_Client(MessageListener tcp, MessageListener udp, Runnable loop) {
        try {
			this.tcp_socket = new Socket("localhost", TCP_PORT);
			this.udp_socket = new DatagramSocket(tcp_socket.getLocalPort());

			this.tcp_output = tcp_socket.getOutputStream();

			this.tcp_listener = tcp;
			this.udp_listener = udp;
			this.running = true;
			// Start a thread to listen for server messages
			new Thread(this::listenForTCPMessages).start();
			new Thread(this::listenForUDPMessages).start();
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
			while (running && tcp_input.available()>=0) {
				short len = (short) ((dis.readByte() << 8) | (dis.readByte() & 0xff));
				byte[] buffer = dis.readNBytes(len);
				tcp_listener.onMessageReceived(buffer);
			}

		} catch (IOException e) {
			System.out.println("Error reading from server: " + e.getMessage());
			System.exit(-1);
		}
	}

	private void listenForUDPMessages(){
		try{

			byte[] buf = new byte[PACKET_SIZE];
			while (running){
				DatagramPacket packet = new DatagramPacket(buf, buf.length);
				udp_socket.receive(packet);
				udp_listener.onMessageReceived(buf);
			}
		}
		catch (IOException e){
			System.out.println("Error receiving UDP packet: " + e.getMessage());
			System.exit(-1);
		}
	}

	private void sendTCPMessage(byte[] message) throws IOException {
		tcp_output.write(message);
		tcp_output.flush();
	}

	private void sendUDPPacket(byte[] buf) throws IOException {
		DatagramPacket packet = new DatagramPacket(buf, buf.length, InetAddress.getByName("localhost"), UDP_PORT);
		udp_socket.send(packet);
	}

	public void requestChunk(int x, int y, int z){
		try{

			dos.writeByte(C2S_CHUNK_REQUEST);
			dos.writeInt(x);
			dos.writeInt(y);
			dos.writeInt(z);
			dos.flush();

			sendTCPMessage(bos.toByteArray());
		}
		catch (IOException e){
			System.out.println("Error sending message: " + e.getMessage());
		}
	}

	public void sendLocation(Vector3f position) {
		try {
			dos.writeByte(C2S_PLAYER_MOVE);
			dos.writeByte(0);
			dos.writeFloat(position.x);
			dos.writeFloat(position.y);
			dos.writeFloat(position.z);
			dos.flush();
			sendUDPPacket(bos.toByteArray());
			bos.reset();
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
