package network;

import server.Server;

import java.io.*;
import java.net.Socket;
import java.util.Arrays;

import static network.NetworkConstants.C2S_CHUNK_REQUEST;
import static network.NetworkConstants.PACKET_SIZE;

public class TCPClientHandler implements Runnable {
    private final Socket clientSocket;


    public TCPClientHandler(Socket clientSocket) {
        this.clientSocket = clientSocket;
    }

    @Override
    public void run() {
        try (InputStream input = clientSocket.getInputStream();
             OutputStream output = clientSocket.getOutputStream()) {

            byte[] buffer = new byte[PACKET_SIZE];
            int bytesRead;

            System.out.println("Client handler started for: " + clientSocket.getInetAddress());

            // Reading input from client (optional based on your protocol)
            while ((bytesRead = input.read(buffer)) != -1) {
                ByteArrayInputStream bis = new ByteArrayInputStream(buffer);
                DataInputStream dis = new DataInputStream(bis);
                deserializeMessages(dis, output);
            }

        } catch (IOException e) {
            System.out.println("Client connection error: " + e.getMessage());
        }

        try {
            System.out.println("Client disconnected: " + clientSocket.getInetAddress());
            TCPServer.clients.remove(clientSocket.getRemoteSocketAddress());
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

                System.out.println("Received chunk request: " + x + " " + y + " " + z);
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
                System.out.print("?");
                break;
        }
        if(dis.available() > 0){
            deserializeMessages(dis,output);
        }
    }
}
