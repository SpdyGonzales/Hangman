package client.view;

import common.Message;

public class Outputhandler {
    public void printMsg(Message message){
        if(message.getStatus() == null) {
            if(message.getWord() == null){
                System.out.println("Please type 'Start' to begin");
            }else{
                System.out.println("-----------------------------------");
                System.out.println("Word to Guess: " + String.valueOf(message.getWord()).replaceAll(".(?!$)", "$0 "));
                System.out.println("Tries Left: " + message.getTries());
                System.out.println("Score: " + message.getScore());
                System.out.println("-----------------------------------");
            }
        }else{
            System.out.println("-----------------------------------");
            if(message.getStatus()){
                System.out.println("You are correct. Word was: " + String.valueOf(message.getWord()));
            }else{
                System.out.println("You lost. Word was: " + String.valueOf(message.getWord()));
            }
            System.out.println("-----------------------------------");
        }
    }
    public void printNxtLine(String s){
        System.out.println(s);
    }
}
