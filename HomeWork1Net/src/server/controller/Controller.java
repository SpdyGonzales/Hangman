package server.controller;

import common.Message;
import server.model.GameStats;

public class Controller {
    private GameStats game;

    public Message checkGuess(String guess){
        return game.checkGuess(guess);
    }

    public Message startGame(){
        game = new GameStats();
        return game.gameStarts();
    }
    public Message nextGuess(){
        return game.generateWord();
    }
}
