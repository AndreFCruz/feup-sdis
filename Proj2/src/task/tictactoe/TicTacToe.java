package task.tictactoe;

import task.AdversarialSearchProblem;
import task.GameState;
import task.Player;

import java.util.ArrayList;
import java.util.Collection;

public class TicTacToe implements AdversarialSearchProblem {
    @Override
    public boolean isStateTerminal(GameState state) {
        TicTacToeBoard board = (TicTacToeBoard) state.getBoard();
        return board.isFull();
    }

    @Override
    public Collection<GameState> successors(GameState state) {
        Collection<GameState> successors = new ArrayList<GameState>();
        TicTacToeBoard board = (TicTacToeBoard) state.getBoard();

        Player currentPlayer = state.getCurrentPlayer();
        TicTacToeBoard.Cell currentCell = ((TicTacToeState) state).getPlayerCell(currentPlayer);

        for(int row = 0; row < TicTacToeBoard.N_ROWS; row++) {
            for(int col = 0; col < TicTacToeBoard.N_COLS; col++) {
                if(board.isFreeCell(row, col)) {
                    GameState successor = getSuccessor(state, row, col, currentCell);
                    successors.add(successor);
                }
            }
        }

        return successors;
    }

    public GameState getSuccessor(GameState state, int row, int col, TicTacToeBoard.Cell value) {
        TicTacToeBoard board = (TicTacToeBoard) state.getBoard();
        TicTacToeBoard clonedBoard = null;

        try {
            clonedBoard = board.clone();
        } catch (CloneNotSupportedException e) {
            e.printStackTrace();
        }

        clonedBoard.setCell(row, col, value);

        TicTacToeBoard.Cell nextCell = TicTacToeBoard.getOppositeCell(value);
        GameState successor = new TicTacToeState(clonedBoard, nextCell);
        return successor;
    }

    @Override
    public int utilityOfState(GameState state) {
        TicTacToeState tState = (TicTacToeState) state;

        if(tState.currentPlayerWins())
            return 1;

        if(tState.opponentPlayerWins())
            return -1;

        //TODO: verify other cases
        return 0;
    }

}
