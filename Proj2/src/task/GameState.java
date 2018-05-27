package task;

import java.io.Serializable;

public class GameState implements Serializable {

    protected static final long serialVersionUID = 102L;

    protected Board board;
    protected Player currentPlayer;

    public Board getBoard() {
        return board;
    }

    public Player getCurrentPlayer() {
        return currentPlayer;
    }

}
