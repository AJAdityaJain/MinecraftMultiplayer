package server;

import network.TCPServer;
import server.block.BlockState;

import java.io.ByteArrayOutputStream;
import java.io.DataOutputStream;
import java.io.IOException;
import java.net.SocketAddress;

import static network.NetworkConstants.RUNNING;
import static network.NetworkConstants.S2C_BLOCK_PLACE;
import static network.TCPServer.broadcastPosition;


public class Server {
	public static final Map world = new Map();

	public static void main(String[] args) throws InterruptedException, IOException {
		new Thread(TCPServer::startTCPServer).start();
		world.loadChunks();
		Thread.sleep(10000);
		ByteArrayOutputStream stream = new ByteArrayOutputStream();
		DataOutputStream out = new DataOutputStream(stream);
		out.writeByte(S2C_BLOCK_PLACE);
		out.writeByte(0);
		out.writeByte(0);
		out.writeInt(0);
		out.writeInt(12);
		out.writeInt(0);
		out.writeInt(new BlockState(BlockState.BlockEnum.STONE).serialize());
		byte[] data = stream.toByteArray();
		short l = (short) (data.length -3);
		data[1] = (byte) (l >> 8);
		data[2] = (byte) l;


		TCPServer.clients.forEach((id, client) -> {
			try {
				client.sendMessage(data);
			} catch (Exception e) {
				e.printStackTrace();
			}
		});



		while (RUNNING) {
			for (SocketAddress id : TCPServer.clients.keySet()) {
                //noinspection BusyWait
                Thread.sleep(100);
				broadcastPosition(id);
			}

		}
	}
}
