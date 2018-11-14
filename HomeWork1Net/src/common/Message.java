package common;

import java.io.Serializable;

public class Message implements Serializable{
    private int tries;
    private char [] word;
    private int score;
    private Boolean didWin;

    public Message(char[] word, int tries, int score, Boolean didWin){
        this.word = word;
        this.tries = tries;
        this.score = score;
        this.didWin = didWin;
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
    public Boolean getStatus(){
        return didWin;
    }
}
