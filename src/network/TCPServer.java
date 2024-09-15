package network;

import server.Server;

import java.io.*;
import java.net.ServerSocket;
import java.net.Socket;
import java.util.Arrays;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;

import static network.NetworkConstants.*;

class ClientHandler implements Runnable {
    private final Socket clientSocket;


    public ClientHandler(Socket clientSocket) {
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
                System.out.println("Received from client: " + Arrays.toString(buffer).substring(0,128));
                ByteArrayInputStream bis = new ByteArrayInputStream(buffer);
                DataInputStream dis = new DataInputStream(bis);
                deserializeMessages(dis,output);
            }

            System.out.println("Client disconnected: " + clientSocket.getInetAddress());

        } catch (IOException e) {
            System.out.println("Client has been disconnected: " + e.getMessage());
        } finally {
            try {
                clientSocket.close();
            } catch (IOException e) {
                System.out.println("Error closing client socket: " + e.getMessage());
            }
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

public class TCPServer {
    private final int MAX_CLIENTS = 2;
    private final int PORT = 8080;
    private boolean running = true;
    private final ExecutorService clientPool = Executors.newFixedThreadPool(MAX_CLIENTS);

    public void start() {
        System.out.println("Server started...");
        try (ServerSocket serverSocket = new ServerSocket(PORT)) {
            while (running) {
                Socket clientSocket = serverSocket.accept();
                System.out.println("\nNew client connected: " + clientSocket.getInetAddress());
                clientPool.execute(new ClientHandler(clientSocket));
            }
        }
        catch (IOException e) {System.out.println("Error starting the server: " + e.getMessage());}
    }
}