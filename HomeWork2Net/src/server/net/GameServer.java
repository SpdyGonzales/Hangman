package server.net;

import java.net.StandardSocketOptions;
import java.nio.channels.SocketChannel;
import java.util.ArrayList;
import java.util.Iterator;
import java.util.List;
import java.io.IOException;
import java.net.ServerSocket;
import java.nio.channels.SelectionKey;
import java.nio.channels.Selector;
import java.nio.channels.ServerSocketChannel;
import java.net.SocketException;
import java.net.InetSocketAddress;
import java.util.concurrent.LinkedBlockingQueue;

import server.controller.Controller;

public class GameServer {
    private static final int LINGER_TIME = 5000;
    private static final int TIMEOUT_HALF_HOUR = 1800000;
    private static int portNo = 1337;
    private static Selector selector;
    private ServerSocketChannel listeningSocketChannel;
    private final LinkedBlockingQueue<SelectionKey> keyList = new LinkedBlockingQueue<>();

    public static void main(String[] args) {
        GameServer server = new GameServer();
        server.start();
    }

    private void start() {
        try{
            selector = Selector.open();
            initListeningSocketChannel();

            while(true){
                while (!keyList.isEmpty()) {
                    keyList.poll().interestOps(SelectionKey.OP_WRITE);
                }
                this.selector.select();
                for (SelectionKey key : this.selector.selectedKeys()) {

                    if(!key.isValid()){
                        continue;
                    }
                    if (key.isAcceptable()) {
                        startClientHandler(key);
                    } else if (key.isReadable()) {
                        receiveFromClient(key);
                    } else if (key.isWritable()) {
                        sendToClient(key);
                    }
                    selector.selectedKeys().remove(key);
                }
            }
        }catch (IOException e) {
            e.printStackTrace();
        }
    }
    private void startClientHandler(SelectionKey key) throws IOException {
        ServerSocketChannel serverSocketChannel = (ServerSocketChannel) key.channel();
        SocketChannel clientChannel = serverSocketChannel.accept();
        clientChannel.configureBlocking(false);
        Clienthandler handler = new Clienthandler(this, clientChannel);
        SelectionKey selectionKey = clientChannel.register(selector, SelectionKey.OP_READ, handler);
        handler.setKey(selectionKey);
    }

    private void receiveFromClient(SelectionKey key) throws IOException {
        Clienthandler clientHandler = (Clienthandler) key.attachment();
        try {
            clientHandler.receiveMsg();
        } catch (IOException ie) {
            System.out.println("Client has closed Connection");
            removeClient(key);
        }
    }
    private void sendToClient(SelectionKey key) throws IOException {
        Clienthandler clientHandler = (Clienthandler) key.attachment();
        try {
            clientHandler.sendMsg();
            key.interestOps(SelectionKey.OP_READ);
        }catch (IOException clientHasClosedConnection) {
            System.out.println("Client has closed Connection");
            removeClient(key);
        }
    }
    private void removeClient(SelectionKey clientKey) throws IOException {
        Clienthandler clientHandler = (Clienthandler) clientKey.attachment();
        clientHandler.disconnectClient();
        clientKey.cancel();
    }

    private void initListeningSocketChannel() throws IOException {
        listeningSocketChannel = ServerSocketChannel.open();
        listeningSocketChannel.configureBlocking(false);
        listeningSocketChannel.bind(new InetSocketAddress(portNo));
        listeningSocketChannel.register(selector, SelectionKey.OP_ACCEPT);
    }
    public void selectorWakeup() {
        selector.wakeup();
    }
    public void addSendRequest(SelectionKey selectionKey) {
        keyList.add(selectionKey);
    }
}
