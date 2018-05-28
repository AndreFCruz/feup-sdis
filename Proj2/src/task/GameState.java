package task;

import java.io.Serializable;

/**
  * Holds the current game state
  */
public class GameState implements Serializable {

    protected static final long serialVersionUID = 102L;

    /**
      * Current game board
      */
    protected Board board;

    /**
      * Current game player (whose turn it is)
      */
    protected Player currentPlayer;

    public Board getBoard() {
        return board;
    }

    public Player getCurrentPlayer() {
        return currentPlayer;
    }

}
