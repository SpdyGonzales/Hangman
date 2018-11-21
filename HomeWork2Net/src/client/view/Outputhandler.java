package client.view;

import common.Message;
import common.Status;

public class Outputhandler {
    public void printMsg(Message message){
        System.out.println("-----------------------------------");
        if(message.getStatus() == Status.STARTNEW) {
                System.out.println("Please type 'Start' to begin");
        }else if(message.getStatus() == Status.WIN){
            System.out.println("You are correct. Word was: " + String.valueOf(message.getWord()));
        }else if(message.getStatus() == Status.LOSE){
                System.out.println("You lost. Word was: " + String.valueOf(message.getWord()));
        }else{
            System.out.println("Word to Guess: " + String.valueOf(message.getWord()).replaceAll(".(?!$)", "$0 "));
            System.out.println("Tries Left: " + message.getTries());
            System.out.println("Score: " + message.getScore());
        }
        System.out.println("-----------------------------------");
    }

    public void printNxtLine(String s){
        System.out.println(s);
    }
}
