package server.net;

import common.Message;
import server.controller.Controller;

import java.io.*;
import java.net.Socket;

public class Clienthandler implements Runnable{
    private final Socket clientSocket;
    private final GameServer server;
    private BufferedReader fromClient;
    private ObjectOutputStream toClient;
    private boolean connected;
    private boolean sessionStarted;
    private boolean gameStarted;
    private Controller contr;
    private Message mes;

    Clienthandler(GameServer server, Socket clientSocket) {
        this.server = server;
        this.clientSocket = clientSocket;
        connected = true;
    }
    private void disconnectClient() {
        try {
            clientSocket.close();
        } catch (IOException ioe) {
            ioe.printStackTrace();
        }
        connected = false;
        sessionStarted = false;
        server.removeHandler(this);
    }
    public void sendResponse() throws IOException {
        toClient.writeObject(mes);
        toClient.flush();
        toClient.reset();
        if(mes.getStatus() != null){
            gameStarted = false;
        }
    }
    public void run(){
        try{
            boolean autoFlush = true;
            fromClient = new BufferedReader(new InputStreamReader(clientSocket.getInputStream()));
            toClient = new ObjectOutputStream(clientSocket.getOutputStream());
        }catch(IOException ioe){
            throw new UncheckedIOException(ioe);
        }
        while(connected){
            try{
                String msg = fromClient.readLine().toLowerCase();
                switch (msg) {
                    case "start":
                        if(!sessionStarted){
                            contr = new Controller();
                            mes = contr.startGame();
                            sessionStarted = true;
                            gameStarted = true;
                        }else if(!gameStarted){
                            mes = contr.nextGuess();
                            gameStarted = true;
                        }else{
                            mes = contr.checkGuess(msg);
                        }
                        sendResponse();
                        break;
                    case "quit":
                        if(sessionStarted){
                            disconnectClient();
                        }
                        break;
                    default:
                        if(sessionStarted && gameStarted){
                            mes = contr.checkGuess(msg);
                        }else{
                            mes = new Message(null, 0, 0, null);
                        }
                        sendResponse();
                        break;
                }
            }catch(IOException e){

            }

        }

    }
}
