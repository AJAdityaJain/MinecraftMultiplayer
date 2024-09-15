package network;

import server.Server;

import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;
import java.net.ServerSocket;
import java.net.Socket;
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
                System.out.println("Received from client: " + new String(buffer, 0, bytesRead));

                // Serialize and send the chunk in multiple packets
                byte[] out_buffer = Server.world.loadedChunks.getFirst().serialize(S2C_CHUNK_SEND);
                System.out.println("Total length of out_buffer: " + out_buffer.length);

                int totalBytesSent = 0;

                // Loop to send data in chunks
                while (totalBytesSent < out_buffer.length) {
                    int bytesToSend = Math.min(PACKET_SIZE, out_buffer.length - totalBytesSent);
                    output.write(out_buffer, totalBytesSent, bytesToSend);
                    output.flush();
                    totalBytesSent += bytesToSend;
                }

                System.out.println("Sent chunk to client");

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