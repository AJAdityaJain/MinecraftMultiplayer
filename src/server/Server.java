package server;

import network.TCPClientHandler;

import java.io.IOException;
import java.net.*;
import java.util.HashMap;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;

import static network.NetworkConstants.*;

public class Server {
	public static Map world = new Map();

	private static DatagramSocket udpSocket;

	public static final HashMap<SocketAddress, TCPClientHandler> clients = new HashMap<>();
	private static final ExecutorService clientPool = Executors.newFixedThreadPool(MAX_CLIENTS);
	private static byte id_counter = 0;

	public static void udpSendActiveClients(SocketAddress client) {
		byte[] buf = new byte[]{S2C_PLAYER_JOIN, 0};
		byte id = clients.get(client).getId();
		for (SocketAddress client_ : clients.keySet()) {
			if (clients.get(client_).getId() != id) {
				try {
					buf[0] = clients.get(client_).getId();
					DatagramPacket response = new DatagramPacket(buf, buf.length,client);
					udpSocket.send(response);
				} catch (IOException e) {
					System.out.println("Error sending active clients: " + e.getMessage());
				}
			}
		}
	}

	public static void udpBroadcast(byte[] buf, byte exclude) throws IOException {
		for (SocketAddress client : clients.keySet()) {
			if (clients.get(client).getId() != exclude) {
				DatagramPacket response = new DatagramPacket(buf, buf.length, client);
				udpSocket.send(response);
			}
		}
	}
	public static void udpBroadcast(byte[] buf, SocketAddress exclude) throws IOException {
		for (SocketAddress client : clients.keySet()) {
			if (!client.equals(exclude)) {
				DatagramPacket response = new DatagramPacket(buf, buf.length, client);
				udpSocket.send(response);
			}
		}
	}

	private static void tcp() {
		System.out.println("TCP Server started...");
		try (ServerSocket serverSocket = new ServerSocket(TCP_PORT)) {
			while (RUNNING) {
				Socket clientSocket = serverSocket.accept();
				clients.put(clientSocket.getRemoteSocketAddress(),new TCPClientHandler(clientSocket, id_counter));
				id_counter++;
				clientPool.execute(clients.get(clientSocket.getRemoteSocketAddress()));
			}
		}
		catch (IOException e) {System.out.println("Error starting the server: " + e.getMessage());}
	}

	private static void udp() {
		System.out.println("UDP Server started...");
		try{
			udpSocket = new DatagramSocket(UDP_PORT);
			byte[] buf = new byte[PACKET_SIZE];
			while (RUNNING) {
				DatagramPacket packet = new DatagramPacket(buf, buf.length);
				udpSocket.receive(packet);
				buf = packet.getData();
				switch (buf[0]){
					case C2S_PLAYER_MOVE : {
						SocketAddress sa =  packet.getSocketAddress();
						buf[0] = S2C_PLAYER_MOVE;
						buf[1] = clients.get(sa).getId();
						udpBroadcast(buf, sa);
					}
				}
			}
			udpSocket.close();
		} catch (IOException ex) {
			System.out.println("Error starting the server: " + ex.getMessage());
		}
	}

	public static void main(String[] args) {
		new Thread(Server::tcp).start();
		new Thread(Server::udp).start();
		world.loadChunks();
	}
}
