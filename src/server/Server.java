package server;

import network.TCPServer;

public class Server {
	public static Map world = new Map();
	public static void main(String[] args) {
		TCPServer tcp_server = new TCPServer();
		new Thread(tcp_server::start).start();
		world.loadChunks();
	}
}