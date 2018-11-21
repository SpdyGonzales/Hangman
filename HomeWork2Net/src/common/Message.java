package common;

import java.io.Serializable;
import common.Status;

public class Message implements Serializable{
    private int tries;
    private char [] word;
    private int score;
    private Status status;

    public Message(char[] word, int tries, int score, Status status){
        this.word = word;
        this.tries = tries;
        this.score = score;
        this.status = status;
    }
    public int getTries(){
        return tries;
    }
    public int getScore(){
        return score;
    }

    public char[] getWord() {
        return word;
    }
    public Status getStatus(){
        return status;
    }
}
