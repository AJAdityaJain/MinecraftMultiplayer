package server;

import network.TCPServer;
import network.UDPServer;

public class Server {
	public static Map world = new Map();
	public static void main(String[] args) {
		new Thread(new TCPServer()::start).start();
		new Thread(new UDPServer()::start).start();
		world.loadChunks();
	}
}
