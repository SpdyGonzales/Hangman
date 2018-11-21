package server.model;

import common.Message;
import common.Status;

import java.io.BufferedReader;
import java.io.FileReader;
import java.io.IOException;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;
import java.util.Random;

public class GameStats {
    private int tries;
    private int score;
    private char [] word;
    private char [] userVisibleWord;
    List<String> words = new ArrayList<>();
    Random random = new Random();
    boolean wordFound = false;
    Message mes;

    public Message gameStarts(){
        BufferedReader reader = null;
        this.score = 0;
        try {
            reader = new BufferedReader(new FileReader("/Users/Erik/IdeaProjects/HomeWork1Net/words.txt"));
            String word;
            while ((word = reader.readLine()) != null) {
                words.add(word);
            }
        }catch (IOException e) {}
        return generateWord();
    }

    public Message generateWord(){
        String randWord = words.get(random.nextInt(words.size())).toLowerCase();
        char[] wordArray = randWord.toCharArray();
        this.word = wordArray;
        userVisibleWord = new char[wordArray.length];
        tries = wordArray.length;
        Arrays.fill(userVisibleWord,'_');
        mes = new Message(userVisibleWord, tries, score, Status.NULL);
        return mes;
    }
    public Message checkGuess(String guess){
        char[] guessArray = guess.toCharArray();
        if(guessArray.length > 1){
            if(Arrays.equals(guessArray, word)){
                userVisibleWord = word;
                this.score++;
                mes = new Message(userVisibleWord, tries, score, Status.WIN);
            }else{
                this.tries--;
            }
        }else {
            for (int i = 0; i <= word.length-1;i++){
                if(guessArray[0] == word[i]){
                    userVisibleWord[i] = guessArray[0];
                    wordFound = true;
                }
            }
            if(!wordFound){
                tries--;
            }
            wordFound = false;
        }
        if(tries == 0){
            score--;
            mes = new Message(word, tries, score, Status.LOSE);
        }else{
            if(Arrays.equals(userVisibleWord, word)){
                this.score ++;
                mes = new Message(userVisibleWord, tries, score, Status.WIN);
            }else{
                mes = new Message(userVisibleWord, tries, score, Status.NULL);
            }
        }
        return mes;
    }
}
