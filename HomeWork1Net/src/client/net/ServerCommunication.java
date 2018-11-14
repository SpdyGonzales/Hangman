package client.net;

import client.view.Outputhandler;
import common.Message;

import java.io.IOException;
import java.io.ObjectInputStream;
import java.io.ObjectOutputStream;
import java.io.PrintWriter;
import java.net.InetAddress;
import java.net.InetSocketAddress;
import java.net.Socket;

public class ServerCommunication {
    private static final int TIMEOUT_HALF_HOUR = 1800000;
    private static final int TIMEOUT_HALF_MINUTE = 30000;
    private Socket socket;
    private PrintWriter toServer;
    private ObjectInputStream fromServer;
    private boolean connected;
    private Outputhandler oph;

    public void connectServer(InetAddress host, int port){
        try{
            socket = new Socket(host,port);
            oph = new Outputhandler();
            //socket.connect(new InetSocketAddress(host, port),TIMEOUT_HALF_MINUTE);
            //socket.setSoTimeout(TIMEOUT_HALF_HOUR);
            connected = true;
            toServer = new PrintWriter(socket.getOutputStream(),true);
            fromServer = new ObjectInputStream(socket.getInputStream());
            new Thread(new Listener()).start();
        }catch(IOException ioe){
            System.err.println("Setup failed");
        }
    }

    public void disconnectServer() throws IOException{
        oph.printNxtLine("Disconnecting from Server");
        socket.close();
        socket = null;
        connected = false;
    }

    public void sendGuess(String guess) throws IOException {
        toServer.println(guess);
        toServer.flush();
    }

    public void startGame() throws IOException {
        toServer.println("start");
        toServer.flush();
    }
    private class Listener implements Runnable {

        public void run() {
            try {
                while (true) {
                    Message message = (Message) fromServer.readObject();
                    oph.printMsg(message);
                }
            } catch (Throwable connectionFailure) {
                if (connected){
                    oph.printNxtLine("Connection lost");
                }
            }
        }
    }
}

