package server.net;

import common.Message;
import common.MessageBufferHandler;
import common.Status;
import server.controller.Controller;
import java.io.IOException;
import java.nio.ByteBuffer;
import java.nio.channels.SelectionKey;
import java.nio.channels.Selector;
import java.nio.channels.ServerSocketChannel;
import java.nio.channels.SocketChannel;
import java.security.PublicKey;
import java.util.Iterator;
import java.util.StringJoiner;
import java.util.concurrent.ForkJoinPool;
import java.io.*;
import java.net.Socket;
import java.util.concurrent.LinkedBlockingQueue;

public class Clienthandler implements Runnable{

    private boolean sessionStarted;
    private boolean gameStarted;
    private Controller contr;
    private Message mes;
    private SocketChannel clientChannel;
    private GameServer server;
    private SelectionKey key;
    private Controller controller;
    private final ByteBuffer clientMes = ByteBuffer.allocateDirect(8192);
    private final LinkedBlockingQueue<String> fromClient = new LinkedBlockingQueue<>();
    private final LinkedBlockingQueue<ByteBuffer> toClient = new LinkedBlockingQueue<>();
    private MessageBufferHandler mesHandler;

    Clienthandler(GameServer server, SocketChannel clientChannel) {
        this.clientChannel = clientChannel;
        this.server = server;
        mesHandler = new MessageBufferHandler();
    }

    private void sendResponse() throws IOException {
        ByteBuffer bufferMessage = ByteBuffer.wrap(mesHandler.makeMesString(mes).getBytes());
        if(mes.getStatus() == Status.WIN || mes.getStatus() == Status.LOSE){
            gameStarted = false;
        }

        synchronized (toClient) {
            toClient.add(bufferMessage);
        }

        server.addSendRequest(this.key);
        server.selectorWakeup();
    }

    public void setKey(SelectionKey key){
        this.key = key;
    }

    void disconnectClient() throws IOException {
        clientChannel.close();
    }

    public void receiveMsg() throws IOException {
        clientMes.clear();
        int numOfReadBytes;
        numOfReadBytes = clientChannel.read(clientMes);
        if (numOfReadBytes == -1) {
            throw new IOException("Client has closed connection.");
        }
        String recvdString = extractMessageFromBuffer();
        fromClient.add(recvdString);
        ForkJoinPool.commonPool().execute(this);

    }
    public void sendMsg() throws IOException {
        synchronized (toClient) {
            while (toClient.size() > 0) {
                clientChannel.write(toClient.poll());
            }
        }
    }
    private String extractMessageFromBuffer() {
        clientMes.flip();
        byte[] bytes = new byte[clientMes.remaining()];
        clientMes.get(bytes);
        return new String(bytes);
    }
    public void run(){
        Iterator<String> iter = fromClient.iterator();
        while(iter.hasNext()){
            try{
                String msg = iter.next().toLowerCase();
                iter.remove();
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
                            mes = new Message("string".toCharArray(), 0, 0, Status.STARTNEW);
                        }
                        sendResponse();
                        break;
                }
            }catch(IOException e){

            }

        }

    }
}
