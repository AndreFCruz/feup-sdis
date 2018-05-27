package task.tictactoe;

import task.AdversarialSearchProblem;
import task.GameState;
import task.Player;

import java.util.ArrayList;
import java.util.Collection;

public class TicTacToe implements AdversarialSearchProblem {
    @Override
    public boolean isStateTerminal(GameState state) {
        TicTacToeState tState = (TicTacToeState) state;

        if(tState.currentPlayerWins())
            return true;

        if(tState.opponentPlayerWins())
            return true;

        return tState.isBoardFull();
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
    public int utilityOfState(GameState state, Player maximizer) {
        TicTacToeState tState = (TicTacToeState) state;

        if(tState.playerWins(maximizer))
            return 1;

        Player opponent = tState.getOpponent(maximizer);
        if(tState.playerWins(opponent))
            return -1;

        /*
        if(tState.currentPlayerWins())
            return 1;

        if(tState.opponentPlayerWins())
            return -1;
            */

        return 0;
    }

}
