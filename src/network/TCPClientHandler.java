package network;

import server.Server;

import java.io.*;
import java.net.Socket;

import static network.NetworkConstants.*;
import static server.Server.*;

public class TCPClientHandler implements Runnable {
    private final Socket clientSocket;
    private final byte id;


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
            System.out.println("Client connected #" + id);
            udpBroadcast(new byte[]{S2C_PLAYER_JOIN, id}, id);
            udpSendActiveClients(clientSocket.getRemoteSocketAddress());

            InputStream input = clientSocket.getInputStream();
            OutputStream output = clientSocket.getOutputStream();

            byte[] buffer = new byte[PACKET_SIZE];
            int bytesRead;

            System.out.println("Client handler started for: " + clientSocket.getInetAddress());

            // Reading input from client (optional based on your protocol)
            ByteArrayInputStream bis = new ByteArrayInputStream(buffer);
            DataInputStream dis = new DataInputStream(bis);
            while ((bytesRead = input.read(buffer)) != -1) {
                deserializeMessages(dis, output);
                bis.reset();
            }

            System.out.println("Client disconnected #" + id);
            udpBroadcast(new byte[]{S2C_PLAYER_LEAVE, id}, id);
            Server.clients.remove(clientSocket.getRemoteSocketAddress());
            clientSocket.close();
        } catch (IOException e) {
            System.out.println("Error closing client socket: " + e.getMessage());
        }

    }

    private void deserializeMessages(DataInputStream dis, OutputStream output) throws IOException {
        switch (dis.readByte()) {
            case C2S_CHUNK_REQUEST:

                int x = dis.readInt();
                int y = dis.readInt();
                int z = dis.readInt();

                byte[] out_buffer = Server.world.serializeChunk(x,y,z);

                int totalBytesSent = 0;
                while (totalBytesSent < out_buffer.length) {
                    int bytesToSend = Math.min(PACKET_SIZE, out_buffer.length - totalBytesSent);
                    output.write(out_buffer, totalBytesSent, bytesToSend);
                    output.flush();
                    totalBytesSent += bytesToSend;
                }

                break;
            default:
                break;
        }
        if(dis.available() > 0){
            deserializeMessages(dis,output);
        }
    }
}
