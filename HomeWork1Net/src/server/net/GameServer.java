package server.net;

import java.util.ArrayList;
import java.util.List;
import java.io.IOException;
import java.net.ServerSocket;
import java.net.Socket;
import java.net.SocketException;
import server.controller.Controller;

public class GameServer {
    private static final int LINGER_TIME = 5000;
    private static final int TIMEOUT_HALF_HOUR = 1800000;
    private final Controller contr = new Controller();
    private final List<Clienthandler> clients = new ArrayList<>();
    private static int portNo = 1337;

    public static void main(String[] args) {
        GameServer server = new GameServer();
        server.start();
    }

    private void start() {
        try {
            ServerSocket listeningSocket = new ServerSocket(portNo);
            while (true) {
                Socket clientSocket = listeningSocket.accept();
                try{
                    clientSocket.setSoLinger(true, LINGER_TIME);
                    clientSocket.setSoTimeout(TIMEOUT_HALF_HOUR);
                    Clienthandler handler = new Clienthandler(this, clientSocket);
                    synchronized (clients) {
                        clients.add(handler);
                    }
                    Thread handlerThread = new Thread(handler);
                    handlerThread.setPriority(Thread.MAX_PRIORITY);
                    handlerThread.start();
                }catch (SocketException e){
                    System.err.println("Socket setup failed");
                }
            }
        }catch (IOException e) {
            System.err.println("Server setup failed");
        }
    }
    void removeHandler(Clienthandler handler) {
        synchronized (clients) {
            clients.remove(handler);
        }
    }
}
