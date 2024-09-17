package network;

import java.io.*;
import java.net.ServerSocket;
import java.net.Socket;
import java.net.SocketAddress;
import java.util.HashMap;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;

import static network.NetworkConstants.*;

public class TCPServer {
    static private final int MAX_CLIENTS = 2;
    static private final ExecutorService clientPool = Executors.newFixedThreadPool(MAX_CLIENTS);
    static public final HashMap<SocketAddress, TCPClientHandler> clients = new HashMap<>();

    public void start() {
        System.out.println("TCP Server started...");
        try (ServerSocket serverSocket = new ServerSocket(TCP_PORT)) {
            while (RUNNING) {
                Socket clientSocket = serverSocket.accept();
                System.out.println("\nNew client connected: " + clientSocket.getInetAddress());
                clients.put(clientSocket.getRemoteSocketAddress(),new TCPClientHandler(clientSocket));
                clientPool.execute(clients.get(clientSocket.getRemoteSocketAddress()));
            }
        }
        catch (IOException e) {System.out.println("Error starting the server: " + e.getMessage());}
    }
}