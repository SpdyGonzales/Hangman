package client.startup;

import client.controller.Controller;
import client.view.Interpreter;

import java.net.InetAddress;
import java.net.UnknownHostException;

public class Main {

    private static InetAddress host;
    private static final int PORT = 1337;

    public static void main(String[] args) {

        try {
            host = InetAddress.getLocalHost();
        }catch (UnknownHostException unkHoEx) {
            System.out.println("Host not valid");
        }

        Controller contr = new Controller();
        contr.connect(host, PORT);
        Interpreter interpreter = new Interpreter(contr);
        interpreter.start();
    }
}
