package task.tictactoe;

import task.GameState;
import task.Player;

public class TicTacToeState extends GameState {

    private Player crosses;
    private Player noughts;

    public TicTacToeState() {
        board = new TicTacToeBoard();

        crosses = new TicTacToePlayer("CROSSES");
        noughts = new TicTacToePlayer("NOUGHTS");
        currentPlayer = crosses;
    }

    public boolean currentPlayerWins() {
        return playerWins(currentPlayer);
    }

    public boolean opponentPlayerWins() {
        Player opponent = getOpponent();
        return playerWins(opponent);
    }

    private boolean playerWins(Player player) {
        TicTacToeBoard tBoard = (TicTacToeBoard) board;
        TicTacToeBoard.Cell currentCell = getPlayerCell(currentPlayer);

        return tBoard.hasLine(currentCell);
    }

    private TicTacToeBoard.Cell getPlayerCell(Player player) {
        if(player == crosses)
            return TicTacToeBoard.Cell.CROSS;

        return TicTacToeBoard.Cell.NOUGH;
    }

    private Player getOpponent() {
        if(currentPlayer == crosses)
            return noughts;

        return crosses;
    }
}
