package client.net;

import client.view.Outputhandler;
import common.Message;
import common.MessageBufferHandler;

import java.io.IOException;
import java.io.ObjectInputStream;
import java.io.ByteArrayInputStream;
import java.io.PrintWriter;
import java.net.InetAddress;
import java.net.InetSocketAddress;
import java.net.Socket;
import java.nio.ByteBuffer;
import java.nio.channels.SelectionKey;
import java.nio.channels.Selector;
import java.nio.channels.SocketChannel;
import java.util.concurrent.LinkedBlockingQueue;

public class ServerCommunication {
    private static final int TIMEOUT_HALF_HOUR = 1800000;
    private static final int TIMEOUT_HALF_MINUTE = 30000;
    private Socket socket;
    private boolean connected;
    private Outputhandler oph;
    private final LinkedBlockingQueue<SelectionKey> keyList = new LinkedBlockingQueue<>();
    private final LinkedBlockingQueue<Message> fromServer = new LinkedBlockingQueue<>();
    private final LinkedBlockingQueue<ByteBuffer> toServer = new LinkedBlockingQueue<>();
    private final ByteBuffer serverMes = ByteBuffer.allocateDirect(8192);
    private SocketChannel socketChannel;
    private InetSocketAddress serverAddress;
    private volatile boolean timeToSend = false;
    private Selector selector;
    private MessageBufferHandler mesHandler;

    public void connectServer(InetAddress host, int port){
        serverAddress = new InetSocketAddress(host, port);
        mesHandler = new MessageBufferHandler();
        oph = new Outputhandler();
        new Thread(new Listener()).start();
    }

    public void disconnectServer() throws IOException{
        oph.printNxtLine("Disconnecting from Server");
        connected = false;
        socketChannel.close();
        socketChannel.keyFor(selector).cancel();
    }

    public void sendGuess(String guess) throws IOException {
        addToSendList(guess);
    }

    public void startGame() throws IOException {
        addToSendList("start");
    }

    private void addToSendList(String mesToBeSent){
        synchronized (toServer) {
            toServer.add(ByteBuffer.wrap(mesToBeSent.getBytes()));
        }

        this.timeToSend = true;
        selector.wakeup();
    }
    private void writeToServer(SelectionKey key) throws IOException {
        synchronized (toServer) {
            while (toServer.size() > 0) {
                ByteBuffer message = toServer.poll();
                socketChannel.write(message);
                if (message.hasRemaining()){
                    return;
                }
            }
        }

        key.interestOps(SelectionKey.OP_READ);
    }

    private void recvFromServer(SelectionKey key) throws IOException {
        serverMes.clear();
        int numOfReadBytes = socketChannel.read(serverMes);
        if (numOfReadBytes == -1) {
            throw new IOException("Couldn't receive message");
        }
        fromServer.add(mesHandler.makeMesMessage(extractMessageFromBuffer()));
        while (fromServer.size() > 0) {
            oph.printMsg(fromServer.poll());
        }
    }

    private String extractMessageFromBuffer() {
        serverMes.flip();
        byte[] bytes = new byte[serverMes.remaining()];
        serverMes.get(bytes);
        return new String(bytes);
    }

    private void initConnection() throws IOException {
        socketChannel = SocketChannel.open();
        socketChannel.configureBlocking(false);
        socketChannel.connect(serverAddress);
        connected = true;
    }

    private void completeLastConnection(SelectionKey key) throws IOException {
        socketChannel.finishConnect();
        key.interestOps(SelectionKey.OP_READ);
    }

    private class Listener implements Runnable {

        public void run() {
            try {
                initConnection();
                selector = Selector.open();
                socketChannel.register(selector, SelectionKey.OP_CONNECT);
                while (connected) {
                    if (timeToSend) {
                        socketChannel.keyFor(selector).interestOps(SelectionKey.OP_WRITE);
                        timeToSend = false;
                    }
                    selector.select();
                    for (SelectionKey key : selector.selectedKeys()) {
                        if (!key.isValid()) {
                            continue;
                        }
                        if (key.isConnectable()) {
                            completeLastConnection(key);
                        } else if (key.isReadable()) {
                            recvFromServer(key);
                        } else if (key.isWritable()) {
                            writeToServer(key);
                        }
                        selector.selectedKeys().remove(key);
                    }
                }
            } catch (Throwable connectionFailure) {
                if (connected){
                    oph.printNxtLine("Connection lost");
                }
            }
        }
    }
}

