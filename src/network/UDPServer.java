package network;

import java.io.IOException;
import java.net.*;
import java.nio.ByteBuffer;
import java.util.Arrays;

import static network.NetworkConstants.*;

public class UDPServer {
    public void start() {
        System.out.println("UDP Server started...");
        try{
            DatagramSocket socket = new DatagramSocket(UDP_PORT);
            byte[] buf = new byte[PACKET_SIZE];
            while (RUNNING) {
                DatagramPacket packet = new DatagramPacket(buf, buf.length);
                socket.receive(packet);
                buf = packet.getData();
                switch (buf[0]){
                    case C2S_PLAYER_MOVE : {
                        float x = ByteBuffer.wrap(new byte[]{buf[1], buf[2], buf[3], buf[4]}).getFloat();
                        float y = ByteBuffer.wrap(new byte[]{buf[5], buf[6], buf[7], buf[8]}).getFloat();
                        float z = ByteBuffer.wrap(new byte[]{buf[9], buf[10], buf[11], buf[12]}).getFloat();
                        SocketAddress sa =  packet.getSocketAddress();
                        System.out.println("Received player move: " + x + " " + y + " " + z + " from " + sa);
                        for (SocketAddress client : TCPServer.clients.keySet()) {
//                            System.out.println(client);
                            if (!client.equals(sa)) {
                                buf[0] = S2C_PLAYER_MOVE;
                                DatagramPacket response = new DatagramPacket(buf, buf.length, client);
                                //DATA FOR MORE THAN 2 CLIENT
                                socket.send(response);
                            }
                        }
                    }

                }
            }
            socket.close();
        } catch (IOException ex) {
            System.out.println("Error starting the server: " + ex.getMessage());
        }
    }
}