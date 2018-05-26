package task.tictactoe;

import task.AdversarialSearchProblem;
import task.Board;
import task.GameState;

import java.util.Collection;

public class TicTacToe implements AdversarialSearchProblem {
    @Override
    public boolean isStateTerminal(GameState state) {
        // spaghetti :(
        TicTacToeBoard board = (TicTacToeBoard) state.getBoard();

        return board.isFull();
    }

    @Override
    public Collection<GameState> successors(GameState state) {
        return null;
    }

    @Override
    public int utilityOfState(GameState state) {
        // TODO: spaghetti again, find a way
        // to refactor this to ease the
        // State->Board relationship on
        // actual implementations
        TicTacToeState tState = (TicTacToeState) state;

        if(tState.currentPlayerWins())
            return 1;

        if(tState.opponentPlayerWins())
            return -1;

        //TODO: verify other cases
        return 0;
    }

}
