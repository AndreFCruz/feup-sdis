package task.tictactoe;

import task.AdversarialSearchProblem;
import task.GameState;
import task.Player;

import java.util.Collection;

public class TicTacToeState extends GameState {

    private Player crosses;
    private Player noughts;

    public TicTacToeState() {
        board = new TicTacToeBoard();

        crosses = new TicTacToePlayer("CROSSES");
        noughts = new TicTacToePlayer("NOUGHTS");
        currentPlayer = crosses;
    }

}
