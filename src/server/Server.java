package server;

import network.TCPClientHandler;

import java.io.IOException;
import java.net.SocketAddress;

import static network.NetworkConstants.RUNNING;
import static network.NetworkConstants.S2C_PLAYER_LEAVE;
import static network.TCPClientHandler.broadcastPosition;
import static network.TCPClientHandler.clients;


public class Server {
	public static final Map world = new Map();
	public static int TPS = 20;
	public static int TICK_TIME = 1000 / TPS;

	public static void main(String[] args) throws InterruptedException, IOException {
		new Thread(TCPClientHandler::startTCPServer).start();
		world.loadChunks();
		Thread.sleep(100);

		long lastTick = System.currentTimeMillis();
		int i = 0;
		while (RUNNING) {
			if(System.currentTimeMillis() - lastTick < TICK_TIME){
                //noinspection BusyWait
                Thread.sleep(TICK_TIME - (System.currentTimeMillis() - lastTick));
			}

			for (SocketAddress id : TCPClientHandler.clients.keySet()) {
				broadcastPosition(id);
			}


			i++;
			if (i == TPS*5) {
				long l = System.currentTimeMillis();
				for (SocketAddress addr : TCPClientHandler.clients.keySet()) {
					TCPClientHandler p = TCPClientHandler.clients.get(addr);
					long delta = l - p.lastUpdate;
					if(delta > 2000 && delta < 100000){
						System.out.println("Player " + p.getId() + " timed out");
						TCPClientHandler.broadcast(new byte[]{S2C_PLAYER_LEAVE, p.getId()}, p.getId());
						clients.remove(addr);
					}
				}
				i = 0;
			}
		}
	}
}