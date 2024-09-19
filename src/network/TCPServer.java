package network;

import org.lwjgl.util.vector.Vector3f;
import server.Server;

import java.io.*;
import java.net.ServerSocket;
import java.net.Socket;
import java.net.SocketAddress;
import java.util.HashMap;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;

import static network.NetworkConstants.*;

public class TCPServer implements Runnable {

    public static final HashMap<SocketAddress, TCPServer> clients = new HashMap<>();
    private static final ExecutorService clientPool = Executors.newFixedThreadPool(MAX_CLIENTS);
    public static ServerSocket serverSocket;
    public static void startTCPServer() {
        System.out.println("TCP Server started...");
        try {
            serverSocket = new ServerSocket(TCP_PORT);
            byte id_counter = 0;
            while (RUNNING) {
                Socket clientSocket = serverSocket.accept();
                clients.put(clientSocket.getRemoteSocketAddress(), new TCPServer(clientSocket, id_counter));
                id_counter++;
                clientPool.execute(clients.get(clientSocket.getRemoteSocketAddress()));
            }
        } catch (IOException e) {
            System.out.println("Error starting the server: " + e.getMessage());
        }
    }

    public static void broadcast(byte[] bytes, byte id) throws IOException {
        for (TCPServer client : clients.values()) {
            if(client.getId() != id){
                client.sendMessage(bytes);
            }
        }
    }

    public static void broadcastPosition(SocketAddress id) {
        Vector3f p = clients.get(id).position;
        byte id_byte = clients.get(id).getId();
        int x = Float.floatToIntBits(p.x);
        int y = Float.floatToIntBits(p.y);
        int z = Float.floatToIntBits(p.z);

        byte[] bytes = {S2C_PLAYER_MOVE, id_byte, (byte) (x >> 24), (byte) (x >> 16), (byte) (x >> 8), (byte) x,
                (byte) (y >> 24), (byte) (y >> 16), (byte) (y >> 8), (byte) y,
                (byte) (z >> 24), (byte) (z >> 16), (byte) (z >> 8), (byte) z};

        for (TCPServer client : clients.values()) {
            if(client.getId() != id_byte){
                try {
                    client.sendMessage(bytes);
                } catch (IOException|NullPointerException e) {
                    try {
                        System.out.println("Error broadcasting position of player #" + id_byte);
                        client.clientSocket.close();
                    }
                    catch (IOException ex) {
                        System.out.println("Error closing client socket: " + ex.getMessage());
                    }
                }
            }
        }
    }


    private final Socket clientSocket;
    private final byte id;
    private final Vector3f position = new Vector3f(8, 14, 8);
    private OutputStream output;

    public TCPServer(Socket clientSocket, byte id) {
        this.clientSocket = clientSocket;
        this.id = id;
    }

    public byte getId() {
        return id;
    }

    @Override
    public void run() {
        try {
            System.out.println("Client connected #" + id);
            broadcast(new byte[]{S2C_PLAYER_JOIN, id}, id);
            output = clientSocket.getOutputStream();

            System.out.println("Client handler started for: " + clientSocket.getInetAddress());

            InputStream input = clientSocket.getInputStream();
            DataInputStream dis = new DataInputStream(input);

            byte code = NULL;
            byte toRead = 0;
            while (clientSocket.isConnected()) {
                if (code == NULL && input.available() > 1 ) {
                    code = dis.readByte();
                    toRead = (byte) (code%16);
                }
                else if(input.available() >= toRead){
                    switch (code){
                        case C2S_PLAYER_MOVE -> {
                            dis.readByte();
                            position.x = dis.readFloat();
                            position.y = dis.readFloat();
                            position.z = dis.readFloat();
                            code = NULL;
                        }
                        case C2S_CHUNK_REQUEST -> {
                            int x = dis.readInt();
                            int y = dis.readInt();
                            int z = dis.readInt();
                            System.out.println("Sending chunk: " + x + " " + y + " " + z);
                            sendMessage(Server.world.serializeChunk(x, y, z));
                            code = NULL;
                        }
                    }
                }
            }
            broadcast(new byte[]{S2C_PLAYER_LEAVE, id}, id);
        } catch (IOException|NullPointerException e) {
            System.out.println("Error closing client socket: " + e.getMessage());
        }
        System.out.println("Client disconnected #" + id);
        clients.remove(clientSocket.getRemoteSocketAddress());

        try {
            output.close();
            clientSocket.close();
        } catch (IOException e) {
            throw new RuntimeException(e);
        }
    }

    public void sendMessage(byte[] message) throws IOException{
        if(output == null) {
            System.out.println("Output is null");
            return;
        }
        output.write(message);
        output.flush();
    }
}
