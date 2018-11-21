package client.controller;

import client.net.ServerCommunication;

import java.io.IOException;
import java.net.InetAddress;

public class Controller {
    ServerCommunication sc = new ServerCommunication();
    public void connect(InetAddress ia, int port){
        sc.connectServer(ia, port);
    }
    public void sendGuess(String guess) throws IOException {
        sc.sendGuess(guess);
    }

    public void startGame() throws IOException {
        sc.startGame();
    }
    public void quit() {
        try {
            sc.disconnectServer();
        } catch (IOException e) {
            e.printStackTrace();
        }
    }
}
