package network;

import org.lwjgl.util.vector.Vector3f;
import server.Server;
import server.block.BlockState;

import java.io.*;
import java.net.ServerSocket;
import java.net.Socket;
import java.net.SocketAddress;
import java.util.HashMap;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;

import static java.lang.System.exit;
import static network.NetworkConstants.*;

public class TCPClientHandler implements Runnable {

    public static final HashMap<SocketAddress, TCPClientHandler> clients = new HashMap<>();
    private static final ExecutorService clientPool = Executors.newFixedThreadPool(MAX_CLIENTS);
    public static ServerSocket serverSocket;
    public static void startTCPServer() {
        Logger.log("Starting TCP server on port " + TCP_PORT, Logger.INFO);
        try {
            serverSocket = new ServerSocket(TCP_PORT);
            byte id_counter = 0;
            while (RUNNING) {
                Socket clientSocket = serverSocket.accept();
                clients.put(clientSocket.getRemoteSocketAddress(), new TCPClientHandler(clientSocket, id_counter));
                id_counter++;
                clientPool.execute(clients.get(clientSocket.getRemoteSocketAddress()));
            }
        } catch (IOException e) {
            Logger.log("Error starting TCP server: " + e.getMessage(), Logger.ERROR);
        }
    }
    public static void broadcast(byte[] bytes, byte from) throws IOException {
        for (TCPClientHandler client : clients.values()) {
            if(client.getId() != from){
                client.sendMessage(bytes);
            }
        }
    }
    public static void broadcastPosition(SocketAddress id) {
        try {
            TCPClientHandler p = clients.get(id);
            byte id_byte = clients.get(id).getId();
            byte[] bytes = s2cPlayerMoveBytes(p, id_byte);

            for (TCPClientHandler client : clients.values()) {
                if (client.getId() != id_byte) {
                    client.sendMessage(bytes);
                }
            }
        } catch (IOException | NullPointerException e) {
            Logger.log("Error broadcasting position: " + e.getMessage(), Logger.ERROR);
            exit(-1);
        }
    }

    private static byte[] s2cPlayerMoveBytes(TCPClientHandler p, byte id_byte) {
        int x = Float.floatToIntBits(p.position.x);
        int y = Float.floatToIntBits(p.position.y);
        int z = Float.floatToIntBits(p.position.z);
        int rx = Float.floatToIntBits(p.rx);
        int ry = Float.floatToIntBits(p.ry);

        return new byte[]{S2C_PLAYER_MOVE, id_byte,
                (byte) (x >> 24), (byte) (x >> 16), (byte) (x >> 8), (byte) x,
                (byte) (y >> 24), (byte) (y >> 16), (byte) (y >> 8), (byte) y,
                (byte) (z >> 24), (byte) (z >> 16), (byte) (z >> 8), (byte) z,
                (byte) (rx >> 24), (byte) (rx >> 16), (byte) (rx >> 8), (byte) rx,
                (byte) (ry >> 24), (byte) (ry >> 16), (byte) (ry >> 8), (byte) ry
        };
    }



    public long lastUpdate = 0;

    private final Socket clientSocket;
    private final byte id;
    private final Vector3f position = new Vector3f(0, 0, 0);
    private float rx, ry;
    private OutputStream output;

    public TCPClientHandler(Socket clientSocket, byte id) {
        this.clientSocket = clientSocket;
        this.id = id;
    }

    public byte getId() {
        return id;
    }

    @Override
    public void run() {
        try {
            Logger.log("Client connected: " + clientSocket.getInetAddress(), Logger.WARNING);

            broadcast(new byte[]{S2C_PLAYER_JOIN, id}, id);
            lastUpdate = System.currentTimeMillis();
            output = clientSocket.getOutputStream();
            for (SocketAddress oid : clients.keySet()) {
                if(clients.get(oid).id != id){
                    sendMessage(new byte[]{S2C_PLAYER_JOIN,clients.get(oid).id});
                }
            }

            InputStream input = clientSocket.getInputStream();
            DataInputStream dis = new DataInputStream(input);

            byte code = NULL;
            short toRead = 0;
            while (clientSocket.isConnected()) {
                if (code == NULL && input.available() > 1 ) {
                    code = dis.readByte();
                    toRead = (byte) (code%16);
                }
                else if(code != NULL && toRead == 0 && input.available() >= 2){
                    toRead = dis.readShort();
                }
                else if(input.available() >= toRead){
                    switch (code){
                        case C2S_PLAYER_MOVE -> {
                            lastUpdate = System.currentTimeMillis();
                            position.x = dis.readFloat();
                            position.y = dis.readFloat();
                            position.z = dis.readFloat();
                            rx = dis.readFloat();
                            ry = dis.readFloat();

                            code = NULL;
                        }
                        case C2S_CHUNK_REQUEST -> {
                            int x = dis.readInt();
                            int y = dis.readInt();
                            int z = dis.readInt();
                            sendMessage(Server.world.serializeChunk(x, y, z));
                            code = NULL;
                        }
                        case C2S_BLOCK_PLACE -> {
                            Logger.log("Block placed", Logger.INFO);
                            ByteArrayOutputStream o = new ByteArrayOutputStream();
                            DataOutputStream dos = new DataOutputStream(o);
                            dos.writeByte(S2C_BLOCK_PLACE);
                            dos.writeShort(toRead);
                            int x = dis.readInt();
                            int y = dis.readInt();
                            int z = dis.readInt();
                            BlockState bs = BlockState.deserialize(dis);
                            Server.world.setBlock(x, y, z, bs);

                            dos.writeInt(x);
                            dos.writeInt(y);
                            dos.writeInt(z);
                            bs.serialize(dos);
                            broadcast(o.toByteArray(), id);
                            code = NULL;
                        }
                        case C2S_LOG -> {
                            byte[] data = new byte[toRead];
                            dis.readFully(data);
                            byte id = dis.readByte();

                            Logger.log(new String(data), id);
                            code = NULL;
                        }
                        default -> code = NULL;
                    }
                }
            }
        } catch (IOException|NullPointerException e) {
            Logger.log("Error handling client: " + e.getMessage(), Logger.ERROR);
        }

        try {
            output.close();
            clientSocket.close();
        } catch (IOException e) {
            System.out.println("Error closing client socket: " + e.getMessage());
            System.exit(-1);
        }
    }

    public void sendMessage(byte[] message) throws IOException{
        if(output == null) {
            Logger.log("Error sending message: output stream is null", Logger.ERROR);
            System.exit(-1);
            return;
        }
        output.write(message);
        output.flush();
    }
}
