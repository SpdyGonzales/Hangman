package client.view;

import client.controller.Controller;

import java.util.Scanner;

public class Interpreter implements Runnable {
    private final Scanner console = new Scanner(System.in);
    private boolean receivingCmds = false;
    private Controller contr;

    public Interpreter(Controller controller) {
        this.contr = controller;
    }

    public void start() {
        if (receivingCmds) {
            return;
        }
        receivingCmds = true;
        new Thread(this).start();
    }

    @Override
    public void run() {
        while (receivingCmds) {
            try {
                String cmd = console.nextLine().toLowerCase();
                if(!cmd.equals("")) {
                    switch (cmd) {
                        case "start":
                            contr.startGame();
                            break;
                        case "quit":
                            contr.quit();
                            break;
                        default:
                            contr.sendGuess(cmd);
                    }
                }
            }catch (Exception e) {}
        }
    }

}
