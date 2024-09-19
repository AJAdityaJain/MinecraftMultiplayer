package server;

import network.TCPServer;

import java.net.SocketAddress;

import static network.NetworkConstants.RUNNING;
import static network.TCPServer.broadcastPosition;


public class Server {
	public static Map world = new Map();

	public static void main(String[] args) throws InterruptedException {
		new Thread(TCPServer::startTCPServer).start();
		world.loadChunks();

		while (RUNNING) {
			for (SocketAddress id : TCPServer.clients.keySet()) {
                //noinspection BusyWait
                Thread.sleep(100);
				broadcastPosition(id);
			}

		}
	}
}
